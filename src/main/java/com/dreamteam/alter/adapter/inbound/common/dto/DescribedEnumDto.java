package com.dreamteam.alter.adapter.inbound.common.dto;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public record DescribedEnumDto<T>(T value, String description) {

    public static <T> DescribedEnumDto<T> of(T value, Map<T, String> descriptions) {
        if (ObjectUtils.isEmpty(value)) {
            return null;
        }

        String description = descriptions.get(value);

        if (StringUtils.isEmpty(description)) {
            description = "-";
        }

        return new DescribedEnumDto<>(value, description);
    }

}
