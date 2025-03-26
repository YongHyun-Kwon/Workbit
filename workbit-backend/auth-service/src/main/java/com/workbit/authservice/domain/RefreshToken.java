package com.workbit.authservice.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(unique = true, nullable = false)
    private String token;

    private LocalDateTime expiryDate;

    private boolean revoked;

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }

}
