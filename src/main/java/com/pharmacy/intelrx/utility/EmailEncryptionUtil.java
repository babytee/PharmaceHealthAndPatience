package com.pharmacy.intelrx.utility;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;

@RequiredArgsConstructor
@Component
public class EmailEncryptionUtil {
    private final UserRepository userRepository;

    public String encrypt(User user) throws Exception {
        // Generate a secure 256-bit AES key
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, new SecureRandom());
        SecretKey secretKey = keyGenerator.generateKey();

        // Generate a unique random IV for each encryption
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] iv = cipher.getIV();

        // Encrypt the email with AES/GCM
        byte[] encryptedEmail = cipher.doFinal(user.getEmail().getBytes());

        var getUser = userRepository.findByEmail(user.getEmail());

        if (getUser.isPresent()) {
            user.setEncryptedEmail(Base64.getUrlEncoder().encodeToString(encryptedEmail));
            user.setEncryptedKey(Base64.getUrlEncoder().encodeToString(secretKey.getEncoded()));
            user.setIvBytes(Base64.getUrlEncoder().encodeToString(iv));
            userRepository.save(user);
        }

        // Combine and encode all parts for transmission
        return Base64.getUrlEncoder().encodeToString(encryptedEmail);

    }

    public String decrypt(String emailEncrypted) {
        var getUser = userRepository.findByEncryptedEmail(emailEncrypted);
        if(!getUser.isPresent()){
            return "activated";
        }
        User user = getUser.get();
        if (user.isStatus() && user.getEncryptedKey() == null) {
            return "activated";
        }
        String email = user.getEmail();
        user.setStatus(true);
        user.setEncryptedEmail("");
        user.setEncryptedKey("");
        user.setIvBytes("");
        userRepository.save(user);
        return email;
    }
}
