package com.example.paymentsystem.merchant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MerchantController.class)
class MerchantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MerchantService merchantService;

    @Test
    @DisplayName("가맹점을 등록하면 201 Created를 반환한다")
    void shouldRegisterMerchant() throws Exception  {
        // Given
        Merchant merchant = new Merchant("테스트 가맹점");

        when(merchantService.registerMerchant("테스트 가맹점"))
                .thenReturn(merchant);

        // When & Then
        mockMvc.perform(post("/api/merchants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "테스트 가맹점"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.merchantId").value(merchant.getMerchantId().toString()))
                .andExpect(jsonPath("$.name").value("테스트 가맹점"))
                .andExpect(jsonPath("$.status").value(merchant.getStatus().name()));
    }

    @Test
    @DisplayName("존재하는 가맹점을 조회하면 200 OK를 반환한다")
    void shouldGetMerchant() throws Exception {
        // Given
        Merchant merchant = new Merchant("테스트 가맹점");
        UUID merchantId = merchant.getMerchantId();

        // 여기서 Mock Service의 getMerchant() 반환값 설정
        when(merchantService.getMerchant(merchantId))
                .thenReturn(merchant);

        // When & Then
        // MockMvc GET 요청과 응답 검증
        mockMvc.perform(get("/api/merchants/{merchantId}", merchantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantId").value(merchant.getMerchantId().toString()))
                .andExpect(jsonPath("$.name").value("테스트 가맹점"))
                .andExpect(jsonPath("$.status").value(merchant.getStatus().name()));
    }

    @Test
    @DisplayName("공백 이름으로 가맹점을 등록하면 400 Bad Request를 반환한다")
    void shouldRejectBlankMerchantName() throws Exception {
        // Given
        // 공백 이름을 가진 JSON 요청 준비
        // When & Then
        // MockMvc POST 요청 후 400 응답 검증
        mockMvc.perform(post("/api/merchants")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "   "}
                        """))
                .andExpect(status().isBadRequest());

        // Mock Service에 어떤 메서드 호출도 발생하지 않았는지 검증
        verifyNoInteractions(merchantService);
    }

    @Test
    @DisplayName("존재하지 않는 가맹점을 조회하면 404 Not Found를 반환한다")
    void shouldReturn404WhenMerchantNotFound() throws Exception {

        // Given
        UUID unknownId = UUID.randomUUID();

        // Mock Service가 예외를 발생시키도록 설정
        when(merchantService.getMerchant(unknownId))
                .thenThrow(new MerchantNotFoundException(unknownId));

        // When & Then
        // MockMvc GET 요청
        // HTTP 404 검증
        mockMvc.perform(get("/api/merchants/{merchantId}", unknownId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("100자를 초과하는 이름으로 가맹점을 등록하면 400 Bad Request를 반환한다")
    void shouldReject100UpMerchantName() throws Exception {

        // Given
        // 100자 초과 이름을 가진 JSON 요청 준비
        // When & Then
        // MockMvc POST 요청 후 400 응답 검증
        String name = "A".repeat(101);
        String json = """
                        {"name": "%s"}
                        """.formatted(name);

        mockMvc.perform(post("/api/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isBadRequest());

        // Mock Service에 어떤 메서드 호출도 발생하지 않았는지 검증
        verifyNoInteractions(merchantService);
    }
}
