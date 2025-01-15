package com.pharmacy.intelrx.auxilliary.services;

import com.pharmacy.intelrx.auxilliary.models.User;
import com.pharmacy.intelrx.auxilliary.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.security.web.session.HttpSessionDestroyedEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UserStatusService implements ApplicationListener<SessionDestroyedEvent> {

    private final UserRepository userRepository;
    private final Map<String, Long> userLastActivity = new HashMap<>();


    @Transactional
    public void userLoggedIn(String username) {

        var checkUser = userRepository.findByEmail(username);
        if (checkUser.isPresent()) {
            //set user status on logged in
            User user = checkUser.get();
            user.setUpdatedAt(LocalDateTime.now());
            user.setUserStatus("Online");
            userRepository.save(user);
        }
        userLastActivity.put(username, System.currentTimeMillis());
    }

    @Transactional
    public void userLoggedOut(String username) {

        var checkUser = userRepository.findByEmail(username);
        if (checkUser.isPresent()) {
            //set user status on logged in
            User user = checkUser.get();
            //user.setUpdatedAt(LocalDateTime.now());
            user.setUserStatus("Offline");
            userRepository.save(user);
        }
        userLastActivity.remove(username);
    }

    public boolean isUserActive(String username) {
        Long lastActivity = userLastActivity.get(username);
        if (lastActivity != null) {
            // Check if the user was active within a certain time frame (e.g., 5 minutes)
            long currentTime = System.currentTimeMillis();
            long inactiveTime = currentTime - lastActivity;


            return inactiveTime < 300000; // 5 minutes in milliseconds
        }
        return false;
    }

    @Override
    public void onApplicationEvent(SessionDestroyedEvent event) {
        if (event instanceof HttpSessionDestroyedEvent) {
            // Handle session destruction events
            String sessionId = ((HttpSessionDestroyedEvent) event).getSession().getId();
            // Remove user from the userLastActivity map based on sessionId if needed
        }
    }


}
