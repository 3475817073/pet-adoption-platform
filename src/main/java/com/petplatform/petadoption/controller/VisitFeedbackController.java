package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.*;
import com.petplatform.petadoption.service.AdoptionApplicationService;
import com.petplatform.petadoption.service.NotificationService;
import com.petplatform.petadoption.service.UserService;
import com.petplatform.petadoption.service.VisitRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visit")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VisitFeedbackController {

    private final VisitRecordService visitRecordService;
    private final AdoptionApplicationService adoptionApplicationService;
    private final UserService userService;
    private final NotificationService notificationService;

    /**
     * 领养人提交回访反馈
     */
    @PostMapping("/submit-feedback")
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            User adopter = userService.findByUsername(username);
            
            if (adopter == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            Long applicationId = Long.valueOf(request.get("applicationId").toString());
            AdoptionApplication application = adoptionApplicationService.findById(applicationId);

            if (application == null) {
                return ResponseEntity.badRequest().body("申请不存在");
            }

            if (!application.getAdopter().getId().equals(adopter.getId())) {
                return ResponseEntity.badRequest().body("无权限操作");
            }

            if (application.getStatus() != ApplicationStatus.APPROVED) {
                return ResponseEntity.badRequest().body("只能对已通过的申请提交反馈");
            }

            // 创建回访反馈记录
            VisitRecord feedback = new VisitRecord();
            feedback.setApplication(application);
            feedback.setVisitor(adopter); // 这里visitor实际上是反馈提交者
            feedback.setVisitType("USER_FEEDBACK");
            feedback.setVisitTime(LocalDateTime.now());
            feedback.setPetStatus((String) request.get("petStatus")); // 适应良好/适应中/存在问题
            feedback.setContent((String) request.get("content")); // 宠物近况描述
            feedback.setFeedback((String) request.get("feedback")); // 领养者反馈
            feedback.setNeedFollowUp(Boolean.valueOf(request.getOrDefault("needFollowUp", "false").toString()));
            
            if (request.get("nextVisitTime") != null && !request.get("nextVisitTime").toString().isEmpty()) {
                feedback.setNextVisitTime(LocalDateTime.parse(request.get("nextVisitTime").toString()));
            }

            visitRecordService.save(feedback);

            // 通知管理员有新反馈
            List<User> admins = userService.findByRole(Role.ADMIN);
            if (!admins.isEmpty()) {
                notificationService.createNotification(
                    admins.get(0).getId(),
                    "VISIT_NOTICE",
                    "新回访反馈",
                    "【" + adopter.getUsername() + "】提交了宠物【" + application.getPet().getName() + "】的回访反馈",
                    feedback.getId(),
                    "系统"
                );
            }

            return ResponseEntity.ok("反馈提交成功，感谢您的配合！");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("提交失败：" + e.getMessage());
        }
    }

    /**
     * 查询当前用户需要反馈的领养记录
     */
    @GetMapping("/my-feedbacks")
    public ResponseEntity<?> getMyFeedbacks(@RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            // 查询该用户所有已通过的领养申请
            List<AdoptionApplication> applications = adoptionApplicationService.findByAdopterId(user.getId());
            
            return ResponseEntity.ok(applications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.APPROVED)
                .collect(java.util.stream.Collectors.toList()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询某个申请的反馈历史
     */
    @GetMapping("/feedback-history/{applicationId}")
    public ResponseEntity<?> getFeedbackHistory(@PathVariable Long applicationId) {
        try {
            List<VisitRecord> records = visitRecordService.findByApplicationId(applicationId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询失败：" + e.getMessage());
        }
    }
}
