package com.dreamteam.alter.domain.report.port.outbound;

import com.dreamteam.alter.domain.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
