package com.example.hello.Users.Authentication.Entity;

import com.example.hello.Users.User.Enum.TokenName;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "token")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "token_id")
    UUID tokenId;

    @Column(name = "token")
    String tokenValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    Session session;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    TokenName tokenName;
}
