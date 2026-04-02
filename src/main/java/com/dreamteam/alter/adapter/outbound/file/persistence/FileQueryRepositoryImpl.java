package com.dreamteam.alter.adapter.outbound.file.persistence;

import com.dreamteam.alter.domain.file.entity.File;
import com.dreamteam.alter.domain.file.entity.QFile;
import com.dreamteam.alter.domain.file.port.outbound.FileQueryRepository;
import com.dreamteam.alter.domain.file.type.FileStatus;
import com.dreamteam.alter.domain.file.type.FileTargetType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FileQueryRepositoryImpl implements FileQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<File> findById(String id) {
        QFile qFile = QFile.file;
        File file = queryFactory
            .selectFrom(qFile)
            .where(
                qFile.id.eq(id),
                qFile.status.ne(FileStatus.DELETED)
            )
            .fetchOne();
        return Optional.ofNullable(file);
    }

    @Override
    public List<File> findAllByIdIn(List<String> ids) {
        QFile qFile = QFile.file;
        return queryFactory
            .selectFrom(qFile)
            .where(
                qFile.id.in(ids),
                qFile.status.ne(FileStatus.DELETED)
            )
            .fetch();
    }

    @Override
    public List<File> findAllByTargetTypeAndTargetId(FileTargetType targetType, String targetId) {
        QFile qFile = QFile.file;
        return queryFactory
            .selectFrom(qFile)
            .where(
                qFile.targetType.eq(targetType),
                qFile.targetId.eq(targetId),
                qFile.status.eq(FileStatus.ATTACHED)
            )
            .fetch();
    }

    @Override
    public List<File> findOrphanFiles(LocalDateTime before) {
        QFile qFile = QFile.file;
        return queryFactory
            .selectFrom(qFile)
            .where(
                qFile.status.eq(FileStatus.PENDING),
                qFile.createdAt.lt(before)
            )
            .fetch();
    }

    @Override
    public Optional<File> findByTargetTypeAndTargetId(FileTargetType targetType, String targetId) {
        QFile qFile = QFile.file;
        return Optional.ofNullable(
            queryFactory
            .selectFrom(qFile)
            .where(
                qFile.targetType.eq(targetType),
                qFile.targetId.eq(targetId),
                qFile.status.eq(FileStatus.ATTACHED)
            )
            .fetchOne()
        );
    }
}
