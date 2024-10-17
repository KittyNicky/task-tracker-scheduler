package com.kittynicky.tasktrackerscheduler.database.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(schema = "public",
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username", name = "uix_users_username"),
                @UniqueConstraint(columnNames = "email", name = "uix_user_email")
        })
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Exclude
    private Long id;

    @Column(name = "username", nullable = false)
    @Size(max = 64)
    private String username;

    @Column(name = "email", nullable = false)
    @Email
    @Size(max = 255)
    private String email;

    @Column(name = "password", nullable = false)
    @NotBlank
    @Size(max = 255)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private String password;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Task> tasks = new ArrayList<>();
}
