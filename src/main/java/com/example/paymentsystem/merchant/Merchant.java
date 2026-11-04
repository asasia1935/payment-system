package com.example.paymentsystem.merchant;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "merchants")
public class Merchant {
    @Id
    @Column(name = "merchant_id")
    private UUID merchantId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MerchantStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Hibernate가 사용하는 기본 생성자
    protected Merchant() {

    }

    public Merchant(String name) {
        // 이름 검증
        if (name == null) {
            throw new IllegalArgumentException("가맹점 이름이 올바르지 않습니다.");
        }

        if (name.isBlank()) {
            throw new IllegalArgumentException("가맹점 이름은 공백일 수 없습니다.");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException("가맹점 이름은 100자를 초과할 수 없습니다.");
        }

        // 필드 초기화
        this.merchantId = UUID.randomUUID();
        this.name = name;
        this.status = MerchantStatus.ACTIVE;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
}
