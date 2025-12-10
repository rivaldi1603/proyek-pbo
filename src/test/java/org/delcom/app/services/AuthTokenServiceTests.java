package org.delcom.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthTokenServiceTests {

    @Mock
    private AuthTokenRepository authTokenRepository;

    @InjectMocks
    private AuthTokenService authTokenService;

    @Test
    @DisplayName("createAuthToken should save and return token")
    void createAuthToken_ShouldSaveAndReturnToken() {
        UUID userId = UUID.randomUUID();
        AuthToken authToken = new AuthToken(userId, "token");

        when(authTokenRepository.save(any(AuthToken.class))).thenReturn(authToken);

        AuthToken result = authTokenService.createAuthToken(authToken);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("token", result.getToken());
        verify(authTokenRepository).save(authToken);
    }

    @Test
    @DisplayName("findUserToken should return token if found")
    void findUserToken_ShouldReturnToken_IfFound() {
        UUID userId = UUID.randomUUID();
        String tokenStr = "token123";
        AuthToken authToken = new AuthToken(userId, tokenStr);

        when(authTokenRepository.findUserToken(userId, tokenStr)).thenReturn(authToken);

        AuthToken result = authTokenService.findUserToken(userId, tokenStr);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(tokenStr, result.getToken());
    }

    @Test
    @DisplayName("deleteAuthToken should call repository delete")
    void deleteAuthToken_ShouldCallRepositoryDelete() {
        UUID userId = UUID.randomUUID();

        authTokenService.deleteAuthToken(userId);

        verify(authTokenRepository).deleteByUserId(userId);
    }
}
