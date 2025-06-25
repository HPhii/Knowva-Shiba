package com.example.demo.repository;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.FlashcardAccessControl;
import com.example.demo.model.entity.flashcard.FlashcardSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlashcardAccessControlRepository extends JpaRepository<FlashcardAccessControl, Long> {
    Optional<FlashcardAccessControl> findByFlashcardSetAndInvitedUser(FlashcardSet flashcardSet, User invitedUser);
}
