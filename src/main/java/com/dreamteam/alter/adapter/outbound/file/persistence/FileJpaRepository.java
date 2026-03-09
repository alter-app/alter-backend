package com.dreamteam.alter.adapter.outbound.file.persistence;

import com.dreamteam.alter.domain.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileJpaRepository extends JpaRepository<File, String> {
}
