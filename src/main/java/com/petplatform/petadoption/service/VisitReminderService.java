package com.petplatform.petadoption.service;

import com.petplatform.petadoption.entity.Notification;
import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.entity.VisitRecord;
import com.petplatform.petadoption.repository.VisitRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitReminderService {
    
    private final VisitRecordRepository visitRecordRepository;
    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * 每天早上9点检查未来3天内需要回访的任务
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkUpcomingVisits() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysLater = now.plusDays(3);
        
        List<VisitRecord> upcomingVisits = visitRecordRepository.findByVisitTimeBetweenAndVisitType(
            now, threeDaysLater, "PLANNED");
        
        for (VisitRecord visit : upcomingVisits) {
            try {
                // 获取管理员列表
                List<User> admins = userService.findByRole(com.petplatform.petadoption.entity.Role.ADMIN);
                
                for (User admin : admins) {
                    notificationService.createNotification(
                        admin.getId(),
                        "VISIT_NOTICE",
                        "回访提醒",
                        "您需要在 " + visit.getVisitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + 
                        " 对【" + visit.getApplication().getAdopter().getUsername() + "】进行回访",
                        visit.getId(),
                        "系统"
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 每周一早上9点检查超期未完成的回访
     */
    @Scheduled(cron = "0 0 9 * * MON")
    public void checkOverdueVisits() {
        LocalDateTime now = LocalDateTime.now();
        
        List<VisitRecord> overdueVisits = visitRecordRepository.findByVisitTimeBeforeAndVisitType(
            now, "PLANNED");
        
        for (VisitRecord visit : overdueVisits) {
            try {
                List<User> admins = userService.findByRole(com.petplatform.petadoption.entity.Role.ADMIN);
                
                for (User admin : admins) {
                    notificationService.createNotification(
                        admin.getId(),
                        "VISIT_NOTICE",
                        "超期回访警告",
                        "【" + visit.getApplication().getAdopter().getUsername() + "】的回访已超期，请尽快完成",
                        visit.getId(),
                        "系统"
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
