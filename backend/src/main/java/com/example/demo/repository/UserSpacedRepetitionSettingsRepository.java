package com.example.demo.repository;

import com.example.demo.model.entity.flashcard.UserSpacedRepetitionSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSpacedRepetitionSettingsRepository extends JpaRepository<UserSpacedRepetitionSettings, Long> {
    Optional<UserSpacedRepetitionSettings> findByUser_Id(Long userId);
}
