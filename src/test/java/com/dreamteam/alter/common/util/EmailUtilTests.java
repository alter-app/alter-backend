package com.dreamteam.alter.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailUtilTests {

    @Test
    void normalizeTrimsAndLowercases() {
        assertThat(EmailUtil.normalize("  Foo@Example.COM \n")).isEqualTo("foo@example.com");
    }

    @Test
    void normalizeKeepsNull() {
        assertThat(EmailUtil.normalize(null)).isNull();
    }
}
