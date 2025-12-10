package org.delcom.app.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("createUser should save and return user")
    void createUser() {
        User user = new User("Test User", "test@example.com", "password");

        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.createUser("Test User", "test@example.com", "password");

        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("getUserByEmail should return user if found")
    void getUserByEmail_Found() {
        User user = new User("User", "email@test.com", "pass");
        when(userRepository.findFirstByEmail("email@test.com")).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail("email@test.com");

        assertNotNull(result);
        assertEquals("email@test.com", result.getEmail());
    }

    @Test
    @DisplayName("getUserByEmail should return null if not found")
    void getUserByEmail_NotFound() {
        when(userRepository.findFirstByEmail("404@test.com")).thenReturn(Optional.empty());

        User result = userService.getUserByEmail("404@test.com");

        assertNull(result);
    }

    @Test
    @DisplayName("getUserById should return user if found")
    void getUserById_Found() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getUserById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("getUserById should return null if not found")
    void getUserById_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.getUserById(id);

        assertNull(result);
    }

    @Test
    @DisplayName("updateUser should update fields and save")
    void updateUser_Success() {
        UUID id = UUID.randomUUID();
        User existingUser = new User("Old", "old@test.com", "pass");
        existingUser.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.updateUser(id, "New", "new@test.com", "Bio", "Prefs", "GYM", 100, 2000);

        assertNotNull(result);
        assertEquals("New", result.getName());
        assertEquals("new@test.com", result.getEmail());
        assertEquals("Bio", result.getBio());
        assertEquals("Prefs", result.getPreferences());
        assertEquals("GYM", result.getFavoriteWorkoutType());
        assertEquals(100, result.getWeeklyDurationGoal());
        assertEquals(2000, result.getDailyCalorieGoal());
    }

    @Test
    @DisplayName("updateUser should return null if user not found")
    void updateUser_NotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userService.updateUser(id, "New", "new@test.com", null, null, null, null, null);

        assertNull(result);
    }

    @Test
    @DisplayName("updateProfilePhoto should update photo path")
    void updateProfilePhoto_Success() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.updateProfilePhoto(id, "new-photo.jpg");

        assertNotNull(result);
        assertEquals("new-photo.jpg", result.getProfilePhoto());
    }

    @Test
    @DisplayName("deleteProfilePhoto should set photo path to null")
    void deleteProfilePhoto_Success() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setProfilePhoto("existing.jpg");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.deleteProfilePhoto(id);

        assertNotNull(result);
        assertNull(result.getProfilePhoto());
    }

    @Test
    @DisplayName("updatePassword should update password")
    void updatePassword_Success() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setPassword("oldPass");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.updatePassword(id, "newPass");

        assertNotNull(result);
        assertEquals("newPass", result.getPassword());
    }

    @Test
    void testCornerUserNotFoundCases() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertNull(userService.updateProfilePhoto(id, "path"));
        assertNull(userService.deleteProfilePhoto(id));
        assertNull(userService.updatePassword(id, "pass"));
    }
}
