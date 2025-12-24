package com.example.hello.Repository;

import com.example.hello.Entity.Session;
import com.example.hello.Enum.TokenName;
import com.example.hello.Entity.Token;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findBySessionAndTokenName(Session session, TokenName tokenName);
    @EntityGraph(attributePaths = {"session"})
    Optional<Token> findByTokenValue(String tokenValue);
}
