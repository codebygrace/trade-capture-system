package com.technicalchallenge.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_privilege")
@IdClass(UserPrivilegeId.class)
public class UserPrivilege {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "privilege_id")
    private Long privilegeId;

    // Completed the relationship mapping between Application User and Privilege
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private ApplicationUser applicationUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "privilege_id", insertable = false, updatable = false)
    private Privilege privilege;
}
