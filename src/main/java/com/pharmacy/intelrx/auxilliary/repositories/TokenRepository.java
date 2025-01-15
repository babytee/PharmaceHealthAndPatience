package com.pharmacy.intelrx.auxilliary.repositories;

import com.pharmacy.intelrx.auxilliary.models.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long>{

    @Query("""
       select t from Token t inner join User u on t.user.id = u.id
       where u.id = :userId and (t.expired = false or t.revoked = false)
    """)
    List<Token> findAllValidTokensByUser(Long userId);

    @Query("""
       select t from Token t inner join Admin a on t.admin.id = a.id
       where a.id = :adminId and (t.expired = false or t.revoked = false)
    """)
    List<Token> findAllValidTokensByAdmin(Long adminId);

    Optional<Token> findByToken(String token);

    @Query("SELECT t FROM Token t WHERE t.token = :token AND (t.expired = :expired OR t.revoked = :revoked)")
    Optional<Token> findByTokenAndExpiredOrRevoked(String token, boolean expired, boolean revoked);
}