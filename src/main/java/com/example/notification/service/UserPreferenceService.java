package com.example.notification.service;

import com.example.notification.domain.entity.UserPreference;
import com.example.notification.domain.enums.Channel;
import com.example.notification.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    /**
     * Checks if a user has opted out of a specific channel and category.
     * By default (if no record exists), we assume the user is opted IN.
     *
     * @param userId The recipient/user ID
     * @param channel The notification channel
     * @param category The category (e.g., MARKETING, TRANSACTIONAL)
     * @return true if opted out, false if opted in.
     */
    public boolean isOptedOut(String userId, Channel channel, String category) {
        UserPreference.UserPreferenceId id = new UserPreference.UserPreferenceId(userId, channel, category);
        Optional<UserPreference> preference = userPreferenceRepository.findById(id);

        if (preference.isPresent()) {
            return !preference.get().getIsOptedIn();
        }
        
        // Default behavior: if no preference exists, they are opted in.
        return false;
    }
}
