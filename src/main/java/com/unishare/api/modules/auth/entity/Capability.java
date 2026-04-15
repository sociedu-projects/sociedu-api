package com.unishare.api.modules.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "capabilities")
@Getter
@Setter
@NoArgsConstructor
public class Capability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 100)
    private String name; // e.g. UPLOAD_DOCUMENT, BOOK_SESSION, MANAGE_USERS
}
