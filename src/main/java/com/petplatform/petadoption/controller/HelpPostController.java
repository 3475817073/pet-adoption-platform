package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.Comment;
import com.petplatform.petadoption.entity.HelpPost;
import com.petplatform.petadoption.entity.Role;
import com.petplatform.petadoption.entity.User;
import com.petplatform.petadoption.service.CommentService;
import com.petplatform.petadoption.service.HelpPostService;
import com.petplatform.petadoption.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 互助交流控制器
 * 处理互助帖子的发布、查询以及评论（含回复）的增删查逻辑
 */
@RestController
@RequestMapping("/api/help")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HelpPostController {

    private final HelpPostService helpPostService;
    private final UserService userService;
    private final CommentService commentService;

    /**
     * 分页获取互助帖子列表
     */
    @GetMapping("/list")
    public ResponseEntity<Page<HelpPost>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(required = false) String petType) {
        Sort.Direction direction = "oldest".equals(sortBy) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createTime"));

        if (petType != null && !petType.isEmpty()) {
            return ResponseEntity.ok(helpPostService.findApprovedByType(petType, pageable));
        } else {
            return ResponseEntity.ok(helpPostService.findApprovedPage(pageable));
        }
    }



    /**
     * 发布新的互助帖子（支持城市和照片）
     */
    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("用户名不能为空");
            }

            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            String title = (String) request.get("title");
            String content = (String) request.get("content");
            String category = (String) request.get("category");

            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("标题不能为空");
            }
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("内容不能为空");
            }
            if (category == null || category.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("分类不能为空");
            }

            HelpPost post = new HelpPost();
            post.setUser(user);
            post.setTitle(title.trim());
            post.setContent(content.trim());
            post.setCategory(category.trim());

            // 新增：设置城市
            Object cityObj = request.get("city");
            if (cityObj != null && !cityObj.toString().trim().isEmpty() && !"null".equals(cityObj.toString())) {
                post.setCity(cityObj.toString().trim());
            }

            // 新增：设置照片 URL 列表（JSON 格式）
            Object photoUrlsObj = request.get("photoUrls");
            if (photoUrlsObj != null && !photoUrlsObj.toString().trim().isEmpty() && !"null".equals(photoUrlsObj.toString())) {
                post.setPhotoUrls(photoUrlsObj.toString());
            }

            post.setStatus(com.petplatform.petadoption.entity.PostStatus.PENDING);

            helpPostService.save(post);
            return ResponseEntity.ok("发布成功，请等待管理员审核");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("发布失败：" + e.getMessage());
        }
    }

    /**
     * 获取指定帖子的评论列表（组装为树形结构，包含主评论及其回复）
     */
    @GetMapping("/comments/{postId}")
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        try {
            List<Comment> allComments = commentService.findByPostId(postId);

            /*
             * 遍历所有评论，筛选出顶级评论（parent为null），并为其嵌套对应的回复列表
             */
            List<Map<String, Object>> result = new ArrayList<>();

            for (Comment comment : allComments) {
                if (comment.getParent() == null) {
                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("id", comment.getId());
                    commentMap.put("user", comment.getUser().getUsername());
                    commentMap.put("content", comment.getContent());
                    commentMap.put("createTime", comment.getCreateTime().toString());

                    List<Map<String, Object>> replies = new ArrayList<>();
                    for (Comment reply : allComments) {
                        if (reply.getParent() != null && reply.getParent().getId().equals(comment.getId())) {
                            Map<String, Object> replyMap = new HashMap<>();
                            replyMap.put("id", reply.getId());
                            replyMap.put("user", reply.getUser().getUsername());
                            replyMap.put("content", reply.getContent());
                            replyMap.put("createTime", reply.getCreateTime().toString());
                            replyMap.put("replyTo", reply.getParent().getUser().getUsername());
                            replies.add(replyMap);
                        }
                    }
                    commentMap.put("replies", replies);
                    result.add(commentMap);
                }
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace(); // 打印详细日志以便排查
            return ResponseEntity.badRequest().body("查询评论失败：" + e.getMessage());
        }
    }


    /**
     * 添加评论或回复
     */
    @PostMapping("/comment")
    public ResponseEntity<?> addComment(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            Long postId = Long.valueOf(request.get("postId").toString());
            HelpPost post = helpPostService.findById(postId);
            if (post == null) {
                return ResponseEntity.badRequest().body("帖子不存在");
            }

            Comment comment = new Comment();
            comment.setPost(post);
            comment.setUser(user);
            comment.setContent((String) request.get("content"));

            /*
             * 如果请求中包含 parentId，则将其作为回复处理，建立父子关联
             */
            if (request.get("parentId") != null) {
                Long parentId = Long.valueOf(request.get("parentId").toString());
                Comment parent = commentService.findById(parentId);
                if (parent != null) {
                    comment.setParent(parent);
                }
            }

            commentService.save(comment);
            return ResponseEntity.ok("评论成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("评论失败：" + e.getMessage());
        }
    }

    /**
     * 删除指定的评论（仅限评论发布者本人操作）
     */
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @RequestParam String username) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            Comment comment = commentService.findById(commentId);
            if (comment == null) {
                return ResponseEntity.badRequest().body("评论不存在");
            }

            if (!comment.getUser().getId().equals(user.getId())) {
                return ResponseEntity.badRequest().body("无权限删除");
            }

            commentService.deleteById(commentId);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败：" + e.getMessage());
        }
    }

    /**
     * 删除互助帖子（管理员可删所有，普通用户仅可删自己的）
     */
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @RequestParam String username) {
        try {
            // 1. 获取当前操作用户
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            // 2. 获取目标帖子
            HelpPost post = helpPostService.findById(postId);
            if (post == null) {
                return ResponseEntity.badRequest().body("帖子不存在");
            }

            // 3. 权限校验
            boolean isAdmin = (user.getRole() == Role.ADMIN);
            boolean isOwner = post.getUser().getId().equals(user.getId());

            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(403).body("您没有权限删除该帖子");
            }

            // 4. 先删除该帖子下的所有评论，避免外键报错
            commentService.deleteByPostId(postId);

            // 5. 执行删除帖子
            helpPostService.deleteById(postId);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取已拒绝的帖子列表（仅限管理员，分页）
     */
    @GetMapping("/rejected")
    public ResponseEntity<?> getRejectedPosts(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            if (user.getRole() != Role.ADMIN) {
                return ResponseEntity.badRequest().body("无权限访问");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
            Page<HelpPost> posts = helpPostService.findRejectedPage(pageable);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取待审核的帖子列表（仅限管理员，分页）
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingPosts(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            if (user.getRole() != Role.ADMIN) {
                return ResponseEntity.badRequest().body("无权限访问");
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createTime"));
            Page<HelpPost> posts = helpPostService.findPendingPage(pageable);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("查询失败：" + e.getMessage());
        }
    }

    /**
     * 审核帖子
     */
    @PostMapping("/review/{postId}")
    public ResponseEntity<?> reviewPost(
            @PathVariable Long postId,
            @RequestParam String username,
            @RequestParam String action) {
        try {
            User admin = userService.findByUsername(username);
            if (admin == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }
            if (admin.getRole() != Role.ADMIN) {
                return ResponseEntity.badRequest().body("无权限操作");
            }

            HelpPost post = helpPostService.findById(postId);
            if (post == null) {
                return ResponseEntity.badRequest().body("帖子不存在");
            }

            if (post.getStatus() != com.petplatform.petadoption.entity.PostStatus.PENDING) {
                return ResponseEntity.badRequest().body("该帖子已审核，不能重复操作");
            }

            if ("approve".equals(action)) {
                post.setStatus(com.petplatform.petadoption.entity.PostStatus.APPROVED);
            } else if ("reject".equals(action)) {
                post.setStatus(com.petplatform.petadoption.entity.PostStatus.REJECTED);
            } else {
                return ResponseEntity.badRequest().body("无效的操作");
            }

            helpPostService.save(post);
            return ResponseEntity.ok("审核成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("审核失败：" + e.getMessage());
        }
    }

    /**
     * 根据宠物类型查询相关帖子（用于宠物详情页显示讨论）
     */
    @GetMapping("/by-pet-type/{type}")
    public ResponseEntity<Page<HelpPost>> getPostsByPetType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return ResponseEntity.ok(helpPostService.findApprovedByType(type, pageable));
    }

    /**
     * 获取单个帖子详情（含关联宠物信息）
     */
    @GetMapping("/{id}")
    public ResponseEntity<HelpPost> getPostDetail(@PathVariable Long id) {
        HelpPost post = helpPostService.findById(id);
        return post != null ? ResponseEntity.ok(post) : ResponseEntity.notFound().build();
    }

    /**
     * 统计某类宠物的相关帖子数量
     */
    @GetMapping("/count-by-pet-type")
    public ResponseEntity<Long> countByPetType(@RequestParam String petType) {
        long count = helpPostService.countByRelatedPetType(petType);
        return ResponseEntity.ok(count);
    }


}


