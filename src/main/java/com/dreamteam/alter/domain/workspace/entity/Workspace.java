package com.dreamteam.alter.domain.workspace.entity;

import com.dreamteam.alter.domain.user.entity.ManagerUser;
import com.dreamteam.alter.domain.workspace.type.WorkspaceStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "workspaces")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Workspace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "manager_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ManagerUser managerUser;

    @Column(name = "business_registration_no", length = 50, nullable = false)
    private String businessRegistrationNo;

    @Column(name = "business_name", length = 128, nullable = false)
    private String businessName;

    @Column(name = "business_type", length = 128, nullable = false)
    private String businessType; // Enum 으로 정의 고려

    @Column(name = "contact", length = 13, nullable = false)
    private String contact;

    @Column(name = "description", length = Integer.MAX_VALUE, nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private WorkspaceStatus status;

    @Column(name = "full_address", length = Integer.MAX_VALUE, nullable = false)
    private String fullAddress;

    @Column(name = "province", length = 64, nullable = false)
    private String province;

    @Column(name = "district", length = 64, nullable = false)
    private String district;

    @Column(name = "town", length = 64, nullable = false)
    private String town;

    @Column(name = "latitude", precision = 9, scale = 6, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6, nullable = false)
    private BigDecimal longitude;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Workspace create(
        ManagerUser managerUser,
        String businessRegistrationNo,
        String businessName,
        String businessType,
        String contact,
        String description,
        WorkspaceStatus status,
        String fullAddress,
        String province,
        String district,
        String town,
        BigDecimal latitude,
        BigDecimal longitude
    ) {
        return Workspace.builder()
            .managerUser(managerUser)
            .businessRegistrationNo(businessRegistrationNo)
            .businessName(businessName)
            .businessType(businessType)
            .contact(contact)
            .description(description)
            .status(status)
            .fullAddress(fullAddress)
            .province(province)
            .district(district)
            .town(town)
            .latitude(latitude)
            .longitude(longitude)
            .build();
    }

}
