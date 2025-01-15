//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.pharmacy.intelrx.auxilliary.services;

import com.pharmacy.intelrx.admin.models.Admin;
import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.admin.repositories.AdminRepository;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Admin> admin = adminRepository.findByEmail(username);
        if (admin.isPresent()) {
            return admin.get(); // Return Admin object if found
        }

        Optional<User> user = userRepository.findByEmail(username);
        if (user.isPresent()) {
            return user.get(); // Return User object if found
        }

        throw new UsernameNotFoundException(String.valueOf(HttpStatus.UNAUTHORIZED));
    }

}

