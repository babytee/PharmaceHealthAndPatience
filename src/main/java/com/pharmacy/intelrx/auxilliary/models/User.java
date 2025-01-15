package com.pharmacy.intelrx.auxilliary.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;

    @Column(name = "birth_month", columnDefinition = "integer default 0")
    private Integer birthMonth;

    @Column(name = "year_of_birth", columnDefinition = "integer default 0")
    private Integer yearOfBirth;

    @Column(name = "day_of_birth", columnDefinition = "integer default 0")
    private Integer dayOfBirth;

    private String gender;
    private String email;
    private String phoneNumber;

    @JsonIgnore
    private String password;

    private String profilePic;

    //private String userType;//employee or pharmacyOwner
    private boolean status = false;//if set to true means email is verified
    private boolean twoFactorAuth = false;
    private String otp;

    @Column(nullable = false) // Assuming you don't want userStatus to be nullable
    @Builder.Default
    private String userStatus = "Offline";//Online,Offline,Suspended

    private String encryptedEmail;
    private String encryptedKey;
    private String ivBytes;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type")
    private UserType userType;//EMPLOYEE or OWNER or BRAND

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Override
    public String getUsername() {
        return email;
    }

    /**
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }


    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
