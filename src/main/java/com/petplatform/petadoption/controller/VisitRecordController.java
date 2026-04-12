package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.*;
import com.petplatform.petadoption.service.AdoptionApplicationService;
import com.petplatform.petadoption.service.UserService;
import com.petplatform.petadoption.service.VisitRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 回访记录控制器
 * 负责管理领养成功后的回访登记、查询及删除业务
 */
@RestController
@RequestMapping("/api/visit")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class VisitRecordController {

    private final VisitRecordService visitRecordService;
    private final AdoptionApplicationService adoptionApplicationService;
    private final UserService userService;

    /**
     * 添加回访记录
     * @param request 包含申请ID、回访详情及后续计划等信息的请求体
     * @return 操作结果响应
     */
    @PostMapping("/add")
    public ResponseEntity<?> addVisitRecord(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            User visitor = userService.findByUsername(username);

            if (visitor == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            /*
             * 校验权限：仅限管理员执行回访记录添加操作
             */
            if (visitor.getRole() != Role.ADMIN) {
                return ResponseEntity.badRequest().body("无权限操作");
            }

            Long applicationId = Long.valueOf(request.get("applicationId").toString());
            AdoptionApplication application = adoptionApplicationService.findById(applicationId);

            if (application == null) {
                return ResponseEntity.badRequest().body("申请不存在");
            }

            if (application.getStatus() != ApplicationStatus.APPROVED) {
                return ResponseEntity.badRequest().body("只能对已通过的申请进行回访");
            }

            /*
             * 构建回访实体，解析时间字段并处理可选的下一次回访时间
             */
            VisitRecord visitRecord = new VisitRecord();
            visitRecord.setApplication(application);
            visitRecord.setVisitor(visitor);
            visitRecord.setVisitType((String) request.get("visitType"));
            visitRecord.setVisitTime(LocalDateTime.parse((String) request.get("visitTime")));
            visitRecord.setPetStatus((String) request.get("petStatus"));
            visitRecord.setContent((String) request.get("content"));
            visitRecord.setFeedback((String) request.get("feedback"));
            visitRecord.setNeedFollowUp(Boolean.valueOf(request.get("needFollowUp").toString()));

            if (request.get("nextVisitTime") != null && !request.get("nextVisitTime").toString().isEmpty()) {
                visitRecord.setNextVisitTime(LocalDateTime.parse(request.get("nextVisitTime").toString()));
            }

            visitRecordService.save(visitRecord);
            return ResponseEntity.ok("回访记录添加成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("添加失败：" + e.getMessage());
        }
    }

    /**
     * 根据申请ID查询该申请下的所有回访记录
     * @param applicationId 领养申请ID
     * @return 回访记录列表
     */
    @GetMapping("/list/{applicationId}")
    public ResponseEntity<?> getVisitRecords(@PathVariable Long applicationId) {
        try {
            List<VisitRecord> records = visitRecordService.findByApplicationId(applicationId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询失败：" + e.getMessage());
        }
    }

    /**
     * 删除指定的回访记录（仅限管理员）
     * @param id 待删除的回访记录ID
     * @param username 当前操作用户名
     * @return 删除结果响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVisitRecord(
            @PathVariable Long id,
            @RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            if (user == null || user.getRole() != Role.ADMIN) {
                return ResponseEntity.badRequest().body("无权限操作");
            }

            visitRecordService.delete(id);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败：" + e.getMessage());
        }
    }
}
