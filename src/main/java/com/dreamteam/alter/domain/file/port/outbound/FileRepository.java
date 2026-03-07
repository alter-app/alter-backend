package com.dreamteam.alter.domain.file.port.outbound;

import com.dreamteam.alter.domain.file.entity.File;

import java.util.List;

public interface FileRepository {
    File save(File file);
    List<File> saveAll(List<File> files);
}
