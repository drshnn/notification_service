package com.example.notification.repository;

import com.example.notification.domain.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, UserPreference.UserPreferenceId> {
}
