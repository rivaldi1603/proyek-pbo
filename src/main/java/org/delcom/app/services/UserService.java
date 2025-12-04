package org.delcom.app.services;

import java.util.UUID;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(String name, String email, String password) {
        User user = new User(name, email, password);
        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findFirstByEmail(email).orElse(null);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public User updateUser(UUID id, String name, String email, String bio, String preferences,
            String favoriteWorkoutType, Integer weeklyDurationGoal, Integer dailyCalorieGoal) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        user.setName(name);
        user.setEmail(email);
        user.setBio(bio);
        user.setPreferences(preferences);
        user.setFavoriteWorkoutType(favoriteWorkoutType);
        user.setWeeklyDurationGoal(weeklyDurationGoal);
        user.setDailyCalorieGoal(dailyCalorieGoal);
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfilePhoto(UUID id, String photoPath) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        user.setProfilePhoto(photoPath);
        return userRepository.save(user);
    }

    @Transactional
    public User updatePassword(UUID id, String newPassword) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return null;
        }
        user.setPassword(newPassword);
        return userRepository.save(user);
    }

}
