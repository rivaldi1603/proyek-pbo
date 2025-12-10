package org.delcom.app.interceptors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTests {

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private UserService userService;

    @Mock
    private AuthContext authContext;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        responseWriter = new StringWriter();
        lenient().when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Reset SecurityContext
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Public Endpoint: Should allow access without auth")
    void preHandle_PublicEndpoint() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verifyNoInteractions(authTokenService, userService);
    }

    @Test
    @DisplayName("Session Auth: Should allow access if SecurityContext has valid user")
    void preHandle_SessionAuth() throws Exception {
        when(request.getRequestURI()).thenReturn("/pages/dashboard");

        // Setup SecurityContext
        User user = new User();
        user.setId(UUID.randomUUID());

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(authContext).setAuthUser(user);
    }

    @Test
    @DisplayName("Non-API Page: Should allow access if no token provided")
    void preHandle_NonApi_NoToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/home");
        // Ensure SecurityContext is empty/anonymous
        SecurityContextHolder.clearContext();

        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    @DisplayName("API Endpoint: Should fail if no token provided")
    void preHandle_Api_NoToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/workouts");
        SecurityContextHolder.clearContext();

        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(401);
    }

    @Test
    @DisplayName("Bearer Token: Should fail if token format invalid")
    void preHandle_InvalidTokenFormat() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/workouts");
        SecurityContextHolder.clearContext();

        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.string");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(401);
    }

    @Test
    @DisplayName("Bearer Token: Should fail if token expired or not found in DB")
    void preHandle_TokenNotFoundInDB() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/workouts");
        SecurityContextHolder.clearContext();

        // Generate valid JWT but don't mock DB return
        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        when(authTokenService.findUserToken(eq(userId), eq(token))).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(401);
    }

    @Test
    @DisplayName("Bearer Token: Should fail if user not found")
    void preHandle_UserNotFound() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/workouts");
        SecurityContextHolder.clearContext();

        UUID userId = UUID.randomUUID();
        String token = JwtUtil.generateToken(userId);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        AuthToken authToken = new AuthToken(userId, token);
        when(authTokenService.findUserToken(eq(userId), eq(token))).thenReturn(authToken);

        when(userService.getUserById(userId)).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(404);
    }

    @Test
    @DisplayName("Session Auth: Anonymous User")
    void preHandle_SessionAuth_Anonymous() throws Exception {
        when(request.getRequestURI()).thenReturn("/pages/dashboard");

        // Mock anonymous token
        org.springframework.security.authentication.AnonymousAuthenticationToken anonToken = mock(
                org.springframework.security.authentication.AnonymousAuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(anonToken);
        when(anonToken.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        // Should fall through to Token check, which fails because no token
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        // Expect redirect/allow depending on logic.
        // Logic: if not API and no token -> return true (Line 59)
        assertTrue(result);
    }

    @Test
    @DisplayName("Session Auth: Principal Not User")
    void preHandle_SessionAuth_PrincipalNotUser() throws Exception {
        when(request.getRequestURI()).thenReturn("/pages/dashboard");

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("NotAUserObject");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Should fall through to Token check -> No token -> return true for non-api
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, new Object());
        assertTrue(result);
    }

    @Test
    @DisplayName("extractToken: No Bearer Prefix")
    void preHandle_TokenNoBearer() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/data");
        SecurityContextHolder.clearContext();

        when(request.getHeader("Authorization")).thenReturn("Basic 12345");

        // extractToken returns null
        // Line 64 checks if token is null -> sends 401

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(401);
    }

    @Test
    @DisplayName("validateToken: Return False")
    void preHandle_JwtInvalid() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/data");
        SecurityContextHolder.clearContext();
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");

        // We can use real JwtUtil here as it returns false for garbage
        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.validateToken("invalid", true)).thenReturn(false);

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertFalse(result);
            verify(response).setStatus(401);
        }
    }

    @Test
    @DisplayName("extractUserId: Return Null")
    void preHandle_UserIdNull() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/data");
        SecurityContextHolder.clearContext();
        when(request.getHeader("Authorization")).thenReturn("Bearer token");

        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.validateToken("token", true)).thenReturn(true);
            jwtMock.when(() -> JwtUtil.extractUserId("token")).thenReturn(null);

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertFalse(result);
            verify(response).setStatus(401); // "Format token autentikasi tidak valid"
        }
    }

    @Test
    @DisplayName("Session Auth: Not Authenticated")
    void preHandle_SessionAuth_NotAuthenticated() throws Exception {
        when(request.getRequestURI()).thenReturn("/pages/dashboard");
        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(request.getHeader("Authorization")).thenReturn(null);
        assertTrue(authInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("Bearer Token: Success")
    void preHandle_Success() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/workouts");
        SecurityContextHolder.clearContext();

        UUID userId = UUID.randomUUID();
        // Since we are mocking static JwtUtil in other tests, we should probably stick
        // to real here
        // OR mock it if we want consistency. But other tests use try-with-resources for
        // mocking.
        // This test runs outside of those blocks, so it uses REAL JwtUtil.
        String token = JwtUtil.generateToken(userId);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        AuthToken authToken = new AuthToken(userId, token);
        when(authTokenService.findUserToken(eq(userId), eq(token))).thenReturn(authToken);

        User user = new User();
        user.setId(userId);
        when(userService.getUserById(userId)).thenReturn(user);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(authContext).setAuthUser(user);
    }

    @Test
    @DisplayName("Public Endpoint: Error Page")
    void preHandle_ErrorPage() throws Exception {
        when(request.getRequestURI()).thenReturn("/error");
        assertTrue(authInterceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("Non-API Page: With Invalid Token -> Should Fail")
    void preHandle_NonApi_WithInvalidToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/home");
        SecurityContextHolder.clearContext();
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");

        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.validateToken("invalid", true)).thenReturn(false);

            boolean result = authInterceptor.preHandle(request, response, new Object());
            assertFalse(result);
            verify(response).setStatus(401);
        }
    }

    @Test
    @DisplayName("API Endpoint: Empty Bearer Token -> Should Fail")
    void preHandle_Api_EmptyToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/workouts");
        SecurityContextHolder.clearContext();
        // "Bearer " -> extractToken returns ""
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(401);
    }

    @Test
    @DisplayName("Non-API Page: Empty Bearer Token -> Should Allow")
    void preHandle_NonApi_EmptyToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/home");
        SecurityContextHolder.clearContext();
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }
}
