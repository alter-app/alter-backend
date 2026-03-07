package com.dreamteam.alter.domain.file.port.inbound;

public interface FileScheduleService {
    void cleanupOrphanFiles();
}
