package com.example.paymentsystem.merchant;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MerchantRepositoryTest {
    private final MerchantRepository merchantRepository;

    @Autowired
    MerchantRepositoryTest(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("가맹점을 저장하고 ID로 조회할 수 있다")
    void shouldSaveAndFindMerchant() {
        // Given: 가맹점 생성
        Merchant merchant = new Merchant("테스트 가맹점");

        // When: Repository로 저장하고 ID 조회
        merchantRepository.save(merchant);

        entityManager.flush();
        entityManager.clear();

        Merchant found = merchantRepository.findById(merchant.getMerchantId()).orElseThrow();

        // Then: 저장한 이름과 조회한 이름 비교
        assertEquals("테스트 가맹점", found.getName());
    }

    @Test
    @DisplayName("존재하지 않는 가맹점 ID를 조회하면 빈 결과를 반환한다")
    void shouldReturnEmptyForUnknownMerchant() {
        // Given
        UUID unknownId = UUID.randomUUID();

        // When
        Optional<Merchant> result = merchantRepository.findById(unknownId);

        // Then
        assertTrue(result.isEmpty());
    }
}
