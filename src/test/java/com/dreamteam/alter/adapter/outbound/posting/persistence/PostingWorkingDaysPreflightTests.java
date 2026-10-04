package com.dreamteam.alter.adapter.outbound.posting.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
class PostingWorkingDaysPreflightTests {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2");
    private String schema;

    @BeforeEach
    void schemaBeforeV16() throws Exception {
        schema = "preflight_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection connection = connect(); var sql = connection.createStatement()) {
            sql.execute("CREATE SCHEMA " + schema);
            sql.execute("SET search_path TO " + schema);
            sql.execute("CREATE TABLE postings (id bigint PRIMARY KEY)");
            // SQL NULL도 검사할 수 있도록 fixture에서만 nullable로 둔다.
            sql.execute("CREATE TABLE posting_schedules (id bigint PRIMARY KEY, posting_id bigint REFERENCES postings(id), "
                + "working_days jsonb, status varchar(20), positions_needed integer NOT NULL, positions_available integer NOT NULL)");
            sql.execute("INSERT INTO postings VALUES (1), (2), (3)");
        }
    }

    private Connection connect() throws SQLException {
        Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        if (schema != null) {
            try (var sql = connection.createStatement()) { sql.execute("SET search_path TO " + schema); }
        }
        return connection;
    }

    private void schedule(long id, long postingId, String days, String status, int positions) throws SQLException {
        try (Connection connection = connect(); var insert = connection.prepareStatement(
            "INSERT INTO posting_schedules VALUES (?, ?, ?::jsonb, ?, ?, 0)")) {
            insert.setLong(1, id);
            insert.setLong(2, postingId);
            insert.setString(3, days);
            insert.setString(4, status);
            insert.setInt(5, positions);
            insert.executeUpdate();
        }
    }

    private void preflight() throws Exception {
        try (Connection connection = connect(); var sql = connection.createStatement()) {
            sql.execute(Files.readString(Path.of("scripts/sql/preflight-posting-working-days.sql")));
        }
    }

    private List<String> rows(String query) throws SQLException {
        try (Connection connection = connect(); var sql = connection.createStatement(); var result = sql.executeQuery(query)) {
            List<String> rows = new ArrayList<>();
            while (result.next()) rows.add(result.getString(1));
            return rows;
        }
    }

    @Test
    void acceptsAllWeekdaysDuplicatesAndEmptyArraysWithoutChangingDataOrSchema() throws Exception {
        schedule(10, 1, "[\"SUNDAY\",\"MONDAY\",\"MONDAY\"]", "OPEN", 2);
        schedule(11, 1, "[]", "OPEN", 3);
        schedule(12, 1, "[\"FRIDAY\"]", "DELETED", 100);
        schedule(20, 2, "[\"TUESDAY\",\"WEDNESDAY\",\"THURSDAY\",\"SATURDAY\"]", "OPEN", 0);
        List<String> before = rows("SELECT row_to_json(s)::text FROM posting_schedules s ORDER BY id");
        List<String> columns = rows("SELECT table_name || ':' || column_name || ':' || is_nullable || ':' || "
            + "coalesce(column_default, '') FROM information_schema.columns WHERE table_schema = '" + schema
            + "' ORDER BY table_name, ordinal_position");

        preflight();

        assertThat(rows("SELECT row_to_json(s)::text FROM posting_schedules s ORDER BY id")).isEqualTo(before);
        assertThat(rows("SELECT table_name || ':' || column_name || ':' || is_nullable || ':' || "
            + "coalesce(column_default, '') FROM information_schema.columns WHERE table_schema = '" + schema
            + "' ORDER BY table_name, ordinal_position")).isEqualTo(columns);

        try (Connection connection = connect(); var sql = connection.createStatement()) {
            sql.execute(Files.readString(Path.of("src/main/resources/db/migration/V16__refactor_posting_recruitment_and_working_days.sql")));
        }
        assertThat(rows("SELECT posting_schedule_id || ':' || day_of_week FROM posting_schedule_working_days ORDER BY 1"))
            .containsExactly("10:MONDAY", "10:SUNDAY", "12:FRIDAY", "20:SATURDAY", "20:THURSDAY", "20:TUESDAY", "20:WEDNESDAY");
        assertThat(rows("SELECT id || ':' || recruit_count FROM postings ORDER BY id")).containsExactly("1:5", "2:1", "3:1");
        assertThat(rows("SELECT row_to_json(s)::text FROM posting_schedules s ORDER BY id")).isEqualTo(before);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"null", "{}", "\"MONDAY\"", "1", "true", "[null]", "[1]", "[true]", "[{}]", "[[]]",
        "[\"monday\"]", "[\"HOLIDAY\"]", "[\"MONDAY\",null]"})
    void rejectsInvalidDataWithScheduleId(String days) throws Exception {
        schedule(42, 1, days, "OPEN", 1);

        assertThatThrownBy(this::preflight).isInstanceOf(SQLException.class)
            .hasMessageContaining("Invalid working_days for posting_schedules ids: 42");
        assertThat(rows("SELECT count(*) FROM posting_schedules")).containsExactly("1");
    }

    @Test
    void reportsAllInvalidScheduleIdsInOrderIncludingDeletedSchedules() throws Exception {
        schedule(43, 1, "[null]", "OPEN", 1);
        schedule(42, 1, "{}", "DELETED", 1);
        schedule(44, 1, "[]", "OPEN", 1);
        assertThatThrownBy(this::preflight).isInstanceOf(SQLException.class)
            .hasMessageContaining("Invalid working_days for posting_schedules ids: 42, 43");
    }
}
