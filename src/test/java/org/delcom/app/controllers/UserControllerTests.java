package org.delcom.app.controllers;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTests {

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenService authTokenService;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController.authContext = authContext;
    }

    @Test
    @DisplayName("registerUser: Validation Fails")
    void registerUser_ValidationFails() {
        User user = new User();
        // Missing fields
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = userController.registerUser(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("fail", response.getBody().getStatus());
    }

    @Test
    @DisplayName("registerUser: Email already exists")
    void registerUser_EmailExists() {
        User user = new User("Test", "exist@example.com", "pass");
        when(userService.getUserByEmail("exist@example.com")).thenReturn(new User());

        ResponseEntity<ApiResponse<Map<String, UUID>>> response = userController.registerUser(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("fail", response.getBody().getStatus());
    }

    @Test
    @DisplayName("registerUser: Success")
    void registerUser_Success() {
        User user = new User("New", "new@example.com", "pass");
        User createdKey = new User();
        createdKey.setId(UUID.randomUUID());

        when(userService.getUserByEmail("new@example.com")).thenReturn(null);
        when(userService.createUser(anyString(), anyString(), anyString())).thenReturn(createdKey);

        ResponseEntity<ApiResponse<Map<String, UUID>>> response = userController.registerUser(user);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().getStatus());
    }

    @Test
    @DisplayName("loginUser: Invalid Credentials (User Null)")
    void loginUser_UserNotFound() {
        User login = new User("user@example.com", "pass");
        when(userService.getUserByEmail("user@example.com")).thenReturn(null);

        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(login);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("loginUser: Invalid Credentials (Password Mismatch)")
    void loginUser_PasswordMismatch() {
        User login = new User("user@example.com", "wrong");
        User dbUser = new User("Name", "user@example.com", new BCryptPasswordEncoder().encode("correct"));

        when(userService.getUserByEmail("user@example.com")).thenReturn(dbUser);

        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(login);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("loginUser: Success")
    void loginUser_Success() {
        // Mock static JwtUtil if needed, or assume it works. Since JwtUtil is static,
        // strict unit testing might require MockedStatic.
        // For simplicity and 100% coverage of *Controller*, we can rely on real JwtUtil
        // or use Mockito-inline.
        // Assuming Mockito-inline is available or JwtUtil logic is simple enough to
        // run.

        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.generateToken(any())).thenReturn("mockToken");

            User login = new User("user@example.com", "pass");
            User dbUser = new User("Name", "user@example.com", new BCryptPasswordEncoder().encode("pass"));
            dbUser.setId(UUID.randomUUID());

            when(userService.getUserByEmail("user@example.com")).thenReturn(dbUser);
            when(authTokenService.findUserToken(eq(dbUser.getId()), eq("mockToken"))).thenReturn(null);
            when(authTokenService.createAuthToken(any(AuthToken.class))).thenReturn(new AuthToken());

            ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(login);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("mockToken", response.getBody().getData().get("authToken"));
        }
    }

    @Test
    @DisplayName("getUserInfo: Success")
    void getUserInfo_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        User user = new User();
        user.setName("Me");
        when(authContext.getAuthUser()).thenReturn(user);

        ResponseEntity<ApiResponse<Map<String, User>>> response = userController.getUserInfo();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Me", response.getBody().getData().get("user").getName());
    }

    @Test
    @DisplayName("updateUser: Success")
    void updateUser_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        User user = new User();
        user.setId(UUID.randomUUID());
        when(authContext.getAuthUser()).thenReturn(user);

        User req = new User("Updated", "new@mail.com", "pass");
        when(userService.updateUser(eq(user.getId()), eq("Updated"), eq("new@mail.com"), any(), any(), any(), any(),
                any())).thenReturn(new User());

        ResponseEntity<ApiResponse<User>> response = userController.updateUser(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("updateUser: Not Found (Should not happen if authenticated but logic exists)")
    void updateUser_NotFound() {
        when(authContext.isAuthenticated()).thenReturn(true);
        User user = new User();
        user.setId(UUID.randomUUID());
        when(authContext.getAuthUser()).thenReturn(user);

        User req = new User("Updated", "new@mail.com", "pass");
        when(userService.updateUser(any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(null);

        ResponseEntity<ApiResponse<User>> response = userController.updateUser(req);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("updateUserPassword: Success")
    void updateUserPassword_Success() {
        when(authContext.isAuthenticated()).thenReturn(true);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPassword(new BCryptPasswordEncoder().encode("old"));
        when(authContext.getAuthUser()).thenReturn(user);

        Map<String, String> payload = Map.of("password", "old", "newPassword", "new");

        when(userService.updatePassword(eq(user.getId()), anyString())).thenReturn(user);

        ResponseEntity<ApiResponse<Void>> response = userController.updateUserPassword(payload);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("loginUser: Token Creation Failed")
    void loginUser_TokenCreationFailed() {
        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.generateToken(any())).thenReturn("mockToken");

            User login = new User("user@example.com", "pass");
            User dbUser = new User("Name", "user@example.com", new BCryptPasswordEncoder().encode("pass"));
            dbUser.setId(UUID.randomUUID());

            when(userService.getUserByEmail("user@example.com")).thenReturn(dbUser);
            when(authTokenService.createAuthToken(any(AuthToken.class))).thenReturn(null); // Simulate failure

            ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(login);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        }
    }

    @Test
    @DisplayName("loginUser: Replace Existing Token")
    void loginUser_ReplaceExistingToken() {
        try (MockedStatic<JwtUtil> jwtMock = mockStatic(JwtUtil.class)) {
            jwtMock.when(() -> JwtUtil.generateToken(any())).thenReturn("mockToken");

            User login = new User("user@example.com", "pass");
            User dbUser = new User("Name", "user@example.com", new BCryptPasswordEncoder().encode("pass"));
            dbUser.setId(UUID.randomUUID());

            when(userService.getUserByEmail("user@example.com")).thenReturn(dbUser);
            // Simulate existing token found
            when(authTokenService.findUserToken(eq(dbUser.getId()), eq("mockToken"))).thenReturn(new AuthToken());
            when(authTokenService.createAuthToken(any(AuthToken.class))).thenReturn(new AuthToken());

            ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(login);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            // Verify delete called
            verify(authTokenService).deleteAuthToken(dbUser.getId());
        }
    }

    @Test
    @DisplayName("getUserInfo: Unauthenticated")
    void getUserInfo_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<Map<String, User>>> response = userController.getUserInfo();
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("updateUser: Unauthenticated")
    void updateUser_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<User>> response = userController.updateUser(new User());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("updateUser: Validation Fails")
    void updateUser_ValidationFails() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(new User());

        // Empty Name
        User req = new User();
        req.setName("");
        ResponseEntity<ApiResponse<User>> response = userController.updateUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updateUserPassword: Unauthenticated")
    void updateUserPassword_Unauthenticated() {
        when(authContext.isAuthenticated()).thenReturn(false);
        ResponseEntity<ApiResponse<Void>> response = userController.updateUserPassword(Map.of());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("updateUserPassword: Old Password Incorrect")
    void updateUserPassword_OldPasswordIncorrect() {
        when(authContext.isAuthenticated()).thenReturn(true);
        User user = new User();
        user.setPassword(new BCryptPasswordEncoder().encode("correct"));
        when(authContext.getAuthUser()).thenReturn(user);

        Map<String, String> payload = Map.of("password", "WRONG", "newPassword", "new");

        ResponseEntity<ApiResponse<Void>> response = userController.updateUserPassword(payload);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updateUserPassword: Validation Missing Fields")
    void updateUserPassword_MissingFields() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(new User());

        // Missing password
        ResponseEntity<ApiResponse<Void>> response = userController.updateUserPassword(Map.of("newPassword", "new"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updateUserPassword: Validation Fails - Missing New Password")
    void updateUserPassword_MissingNewPassword() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(new User());

        // Missing newPassword
        Map<String, String> payload = Map.of("password", "old");
        ResponseEntity<ApiResponse<Void>> response = userController.updateUserPassword(payload);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updateUserPassword: User Not Found (Post-Auth)")
    void updateUserPassword_UserNotFound() {
        when(authContext.isAuthenticated()).thenReturn(true);
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPassword(new BCryptPasswordEncoder().encode("old"));
        when(authContext.getAuthUser()).thenReturn(user);

        Map<String, String> payload = Map.of("password", "old", "newPassword", "new");

        // Simulate update failure (user deleted concurrently?)
        when(userService.updatePassword(eq(user.getId()), anyString())).thenReturn(null);

        ResponseEntity<ApiResponse<Void>> response = userController.updateUserPassword(payload);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("registerUser: Validation Fails - Missing Name")
    void registerUser_MissingName() {
        User user = new User(null, "email@test.com", "pass");
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = userController.registerUser(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data nama tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("registerUser: Validation Fails - Missing Email")
    void registerUser_MissingEmail() {
        User user = new User("Name", null, "pass");
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = userController.registerUser(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data email tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("registerUser: Validation Fails - Missing Password")
    void registerUser_MissingPassword() {
        User user = new User("Name", "email@test.com", null);
        ResponseEntity<ApiResponse<Map<String, UUID>>> response = userController.registerUser(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data password tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("loginUser: Validation Fails - Missing Email")
    void loginUser_MissingEmail() {
        User user = new User(null, "pass");
        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("loginUser: Validation Fails - Missing Password")
    void loginUser_MissingPassword() {
        User user = new User("email@test.com", null);
        ResponseEntity<ApiResponse<Map<String, String>>> response = userController.loginUser(user);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Data tidak valid", response.getBody().getMessage());
    }

    @Test
    @DisplayName("registerUser: Validation Fails - Empty Strings")
    void registerUser_EmptyStrings() {
        // Empty Name
        User user1 = new User("", "email@test.com", "pass");
        assertEquals(HttpStatus.BAD_REQUEST, userController.registerUser(user1).getStatusCode());

        // Empty Email
        User user2 = new User("Name", "", "pass");
        assertEquals(HttpStatus.BAD_REQUEST, userController.registerUser(user2).getStatusCode());

        // Empty Password
        User user3 = new User("Name", "email", "");
        assertEquals(HttpStatus.BAD_REQUEST, userController.registerUser(user3).getStatusCode());
    }

    @Test
    @DisplayName("loginUser: Validation Fails - Empty Strings")
    void loginUser_EmptyStrings() {
        // Empty Email
        User user1 = new User("", "pass");
        assertEquals(HttpStatus.BAD_REQUEST, userController.loginUser(user1).getStatusCode());

        // Empty Password
        User user2 = new User("email", "");
        assertEquals(HttpStatus.BAD_REQUEST, userController.loginUser(user2).getStatusCode());
    }

    @Test
    @DisplayName("updateUser: Validation Fails - Empty Email")
    void updateUser_EmptyEmail() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(new User());

        User req = new User();
        req.setName("Valid Name");
        req.setEmail("");

        assertEquals(HttpStatus.BAD_REQUEST, userController.updateUser(req).getStatusCode());
    }

    @Test
    @DisplayName("updateUser: Validation Fails - Null Name")
    void updateUser_NullName() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(new User());

        User req = new User();
        req.setName(null);
        req.setEmail("valid@email.com");

        assertEquals(HttpStatus.BAD_REQUEST, userController.updateUser(req).getStatusCode());
    }

    @Test
    @DisplayName("updateUser: Validation Fails - Null Email")
    void updateUser_NullEmail() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(new User());

        User req = new User();
        req.setName("Valid Name");
        req.setEmail(null);

        assertEquals(HttpStatus.BAD_REQUEST, userController.updateUser(req).getStatusCode());
    }

    @Test
    @DisplayName("updateUserPassword: Validation Fails - Empty Strings")
    void updateUserPassword_EmptyStrings() {
        when(authContext.isAuthenticated()).thenReturn(true);
        when(authContext.getAuthUser()).thenReturn(new User());

        // Empty Old
        Map<String, String> payload1 = Map.of("password", "", "newPassword", "new");
        assertEquals(HttpStatus.BAD_REQUEST, userController.updateUserPassword(payload1).getStatusCode());

        // Empty New
        Map<String, String> payload2 = Map.of("password", "old", "newPassword", "");
        assertEquals(HttpStatus.BAD_REQUEST, userController.updateUserPassword(payload2).getStatusCode());
    }
}
