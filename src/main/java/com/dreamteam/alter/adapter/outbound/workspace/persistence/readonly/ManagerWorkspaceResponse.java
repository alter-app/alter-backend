package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerWorkspaceResponse {

    private Long id;

    private String businessRegistrationNo;

    private String businessName;

    private String businessType;

    private String contact;

    private String description;

    @Enumerated(EnumType.STRING)
    private WorkspaceStatus status;

    private String fullAddress;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private LocalDateTime createdAt;

    private ReputationSummary reputationSummary;

}
