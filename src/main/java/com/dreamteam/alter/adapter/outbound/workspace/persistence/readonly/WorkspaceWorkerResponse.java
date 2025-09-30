package com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly;

import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.type.UserGender;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceWorkerResponse {

    private Long id;

    private String name;

    private String contact;

    @Enumerated(EnumType.STRING)
    private UserGender gender;

    /**
     * User 엔티티로부터 DTO 생성
     */
    public static WorkspaceWorkerResponse from(User user) {
        return new WorkspaceWorkerResponse(
            user.getId(),
            user.getName(),
            user.getContact(),
            user.getGender()
        );
    }

}
