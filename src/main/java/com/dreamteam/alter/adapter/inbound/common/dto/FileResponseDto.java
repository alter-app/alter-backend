package com.dreamteam.alter.adapter.inbound.common.dto;

import com.dreamteam.alter.domain.file.entity.File;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileResponseDto {

    private String fileId;
    private String url;

    public static FileResponseDto of(File file, String url) {
        return new FileResponseDto(file.getId(), url);
    }
}
