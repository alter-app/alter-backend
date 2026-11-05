package com.dreamteam.alter.domain.workspace.command;

import com.dreamteam.alter.domain.user.entity.User;

import java.math.BigDecimal;
import java.util.List;

public record CreateWorkspaceRequestCommand(
    User user,
    String bizName,
    String brn,
    String address,
    String province,
    String district,
    String town,
    BigDecimal latitude,
    BigDecimal longitude,
    Long businessTypeId,
    String businessTypeDetail,
    String contact,
    String workspaceCertFileId,
    String workspaceOwnIdentityFileId,
    String workspaceWarrantFileId,
    List<String> representativeImageFileIds
) {
}
