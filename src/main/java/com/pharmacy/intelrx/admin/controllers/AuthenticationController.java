//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.pharmacy.intelrx.admin.controllers;

import com.pharmacy.intelrx.auxilliary.dto.RegisterRequest;
import com.pharmacy.intelrx.admin.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("adminAuthenticationController")
@RequestMapping({"/api/v1/admin/auth"})
public class AuthenticationController {
    private final AuthenticationService service;
    //private final LogoutService logoutService;

    @PostMapping({"/register"})
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        return this.service.Register(request);
    }

    @PostMapping({"/login"})
    public ResponseEntity<?> login(@RequestBody RegisterRequest request) {
        return this.service.login(request);
    }

//    @PostMapping({"/logout"})
//    public void login() {
//         logoutService.logout();
//    }

//    @PostMapping({"/login"})
//    public ResponseEntity<?> login(@RequestBody RegisterRequest request) {
//        return this.service.login(request);
//    }

//    @PostMapping({"/new-password"})
//    public ResponseEntity<?> new_password(@RequestBody RegisterRequest request) {
//        return this.service.new_password(request);
//    }
//
//    @PostMapping({"/refresh-token"})
//    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        this.service.refreshToken(request, response);
//    }

}
