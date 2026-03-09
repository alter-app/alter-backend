package com.dreamteam.alter.adapter.outbound.file.persistence;

import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.port.outbound.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional
public class FileRepositoryImpl implements FileRepository {

    private final FileJpaRepository fileJpaRepository;

    @Override
    public File save(File file) {
        return fileJpaRepository.save(file);
    }

    @Override
    public List<File> saveAll(List<File> files) {
        return fileJpaRepository.saveAll(files);
    }
}
