package com.example.demo.service.scheduler;

import com.example.demo.model.entity.User;
import com.example.demo.model.entity.flashcard.FlashcardProgress;
import com.example.demo.model.enums.NotificationType;
import com.example.demo.model.io.response.object.EmailDetails;
import com.example.demo.repository.FlashcardProgressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.intface.IEmailService;
import com.example.demo.service.intface.INotificationService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    private final FlashcardProgressRepository flashcardProgressRepository;
    private final UserRepository userRepository;
    private final IEmailService emailService;
    private final INotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void sendDailyFlashcardNotifications() {
        LocalDate today = LocalDate.now();
        List<FlashcardProgress> dueFlashcards = flashcardProgressRepository.findByNextDueDate(today);

        // group flashcards by user
        Map<User, List<FlashcardProgress>> userFlashcardsMap = dueFlashcards.stream()
                .collect(Collectors.groupingBy(FlashcardProgress::getUser));

        // send notification emails to each user
        for (Map.Entry<User, List<FlashcardProgress>> entry : userFlashcardsMap.entrySet()) {
            User user = entry.getKey();
            List<FlashcardProgress> flashcards = entry.getValue();
//            sendNotificationEmail(user, flashcards);

            // Tạo notification
            String message = "Bạn có " + flashcards.size() +
                    " flashcard đến hạn ôn tập hôm nay. Hãy bắt đầu để không quên kiến thức nhé!";
            notificationService.createNotification(user.getId(), NotificationType.REMINDER, message, null);
        }
    }

    private void sendNotificationEmail(User user, List<FlashcardProgress> flashcards) {
        String subject = "Flashcards đến hạn ôn tập";

        Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("userName", user.getFullName());
        contextVariables.put("flashcardCount", flashcards.size());

        EmailDetails emailDetails = new EmailDetails();
        emailDetails.setReceiver(user.getAccount());
        emailDetails.setSubject(subject);

        try {
            emailService.sendMail(emailDetails, "reminder.html", contextVariables);
        } catch (MessagingException e) {
            System.out.println("Error when sending email for user:" + user.getId());
        }
    }
}
