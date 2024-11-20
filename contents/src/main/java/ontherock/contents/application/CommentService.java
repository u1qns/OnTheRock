package ontherock.contents.application;

import lombok.extern.slf4j.Slf4j;
import ontherock.contents.domain.Comment;
import ontherock.contents.domain.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private NotificationService notificationService;

    public Comment addComment(long postId, long userId, String content) {
        try {
            Comment comment = new Comment(postId, userId, content);

            notificationService.sendNotification(NotificationService.NotificationType.COMMENT, userId, null);
            return commentRepository.save(comment);
        }catch (Exception e) {
            log.error("Failed to add comment or send notification", e);
            throw new RuntimeException("Failed to add comment", e);
        }

    }

    public List<Comment> getCommentsByPostId(long postId) {
        return commentRepository.findByPostId(postId);
    }

    public void deleteComment(String commentId) {
        commentRepository.deleteById(commentId);
    }
}
