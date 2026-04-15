package com.unishare.api.modules.auth.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "role_capabilities")
@Getter
@Setter
@NoArgsConstructor
public class RoleCapability {

    @EmbeddedId
    private RoleCapabilityId id = new RoleCapabilityId();

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class RoleCapabilityId implements Serializable {
        @Column(name = "role_id")
        private UUID roleId;

        @Column(name = "capability_id")
        private UUID capabilityId;
    }
}
