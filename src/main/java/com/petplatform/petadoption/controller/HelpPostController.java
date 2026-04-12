package com.petplatform.petadoption.controller;

import com.petplatform.petadoption.entity.Comment;
import com.petplatform.petadoption.entity.HelpPost;
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
     * @param page 页码（从0开始）
     * @param size 每页条数
     * @param sortBy 排序方式：newest（最新）或 oldest（最早）
     * @return 分页的帖子列表
     */
    @GetMapping("/list")
    public ResponseEntity<Page<HelpPost>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sortBy) {
        Sort.Direction direction = "oldest".equals(sortBy) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createTime"));
        return ResponseEntity.ok(helpPostService.findPage(pageable));
    }


    /**
     * 发布新的互助帖子
     * @param request 包含用户名、标题、内容及分类的请求体
     * @return 发布结果响应
     */
    @PostMapping("/publish")
    public ResponseEntity<?> publish(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.badRequest().body("用户不存在");
            }

            HelpPost post = new HelpPost();
            post.setUser(user);
            post.setTitle((String) request.get("title"));
            post.setContent((String) request.get("content"));
            post.setCategory((String) request.get("category"));

            helpPostService.save(post);
            return ResponseEntity.ok("发布成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("发布失败：" + e.getMessage());
        }
    }

    /**
     * 获取指定帖子的评论列表（组装为树形结构，包含主评论及其回复）
     * @param postId 帖子ID
     * @return 结构化后的评论数据列表
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
     * @param request 包含帖子ID、用户名、内容及可选父评论ID的请求体
     * @return 操作结果响应
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
     * @param commentId 待删除的评论ID
     * @param username 当前操作用户名
     * @return 删除结果响应
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
}
