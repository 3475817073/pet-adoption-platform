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

@RestController
@RequestMapping("/api/help")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HelpPostController {

    private final HelpPostService helpPostService;
    private final UserService userService;
    private final CommentService commentService;

    @GetMapping("/list")
    public ResponseEntity<Page<HelpPost>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sortBy) {
        Sort.Direction direction = "oldest".equals(sortBy) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "createTime"));
        return ResponseEntity.ok(helpPostService.findPage(pageable));
    }


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

    @GetMapping("/comments/{postId}")
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        try {
            List<Comment> allComments = commentService.findByPostId(postId);

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
