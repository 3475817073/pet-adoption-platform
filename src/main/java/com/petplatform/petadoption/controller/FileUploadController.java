package com.petplatform.petadoption.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 * 负责处理图片等静态资源的上传请求，并将文件存储至本地服务器
 */
@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    /** 从配置文件中读取的本地文件上传根路径 */
    @Value("${spring.upload.path}")
    private String uploadPath;

    /**
     * 上传图片文件
     * @param file 前端上传的文件对象
     * @return 包含文件访问 URL 和文件名的映射结果，或错误信息
     */
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("文件不能为空");
            }

            /*
             * 校验文件类型，确保仅允许上传图片格式
             */
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("只能上传图片文件");
            }

            /*
             * 确保上传目录存在，若不存在则自动创建
             */
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            /*
             * 生成唯一文件名并保存文件到本地磁盘
             */
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String newFilename = UUID.randomUUID().toString() + extension;

            Path filePath = Paths.get(uploadPath, newFilename);
            Files.write(filePath, file.getBytes());

            String fileUrl = "/uploads/" + newFilename;

            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", newFilename);

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("上传失败：" + e.getMessage());
        }
    }
}
