package com.example.hello.Users.Authentication.Repository;

import com.example.hello.Users.Authentication.Entity.Session;
import com.example.hello.Users.User.Enum.TokenName;
import com.example.hello.Users.Authentication.Entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findBySessionAndTokenName(Session session, TokenName tokenName);
    Optional<Token> findByTokenValue(String tokenValue);
}
