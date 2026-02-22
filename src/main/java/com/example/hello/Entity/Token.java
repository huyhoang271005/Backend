package com.example.hello.Entity;

import com.example.hello.Enum.TokenName;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "token", indexes = {
        @Index(name = "idx_token_session_id", columnList = "session_id"),
        @Index(name = "idx_token_token", columnList = "token")
})
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

    @Column(name = "token", columnDefinition = "VARCHAR(1000)")
    String tokenValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    Session session;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    TokenName tokenName;
}
