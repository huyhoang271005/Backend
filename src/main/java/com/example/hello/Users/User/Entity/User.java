package com.example.hello.Users.User.Entity;

import com.example.hello.Users.Authentication.Entity.Email;
import com.example.hello.Users.Authentication.Entity.Session;
import com.example.hello.Users.Authentication.Entity.VerificationTokens;
import com.example.hello.RolePermission.Entity.Role;
import com.example.hello.Users.User.Enum.UserStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name="[user]")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="user_id")
    UUID userId;

    @Column(unique = true)
    String username;

    String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    UserStatus userStatus;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    Profile profile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<Email> emails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="role_id")
    Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<Session> sessions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    List<VerificationTokens> verificationTokens;

}
