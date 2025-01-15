package com.pharmacy.intelrx.auxilliary.repositories;

import com.pharmacy.intelrx.auxilliary.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User,Long>{
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneNumber(String phoneNumber);

    Boolean existsByEmail(String email);
    Optional<User>findByEncryptedEmail(String encryptedEmail);

}
