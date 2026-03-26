package com.unishare.api.modules.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role_capabilities")
@Getter
@Setter
@NoArgsConstructor
public class RoleCapability {

    @EmbeddedId
    private RoleCapabilityId id = new RoleCapabilityId();

    @Column(name = "role_id", insertable = false, updatable = false)
    private Integer roleId;

    @Column(name = "capability_id", insertable = false, updatable = false)
    private Integer capabilityId;

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    public static class RoleCapabilityId implements java.io.Serializable {
        @Column(name = "role_id")
        private Integer roleId;
        @Column(name = "capability_id")
        private Integer capabilityId;
    }
}
