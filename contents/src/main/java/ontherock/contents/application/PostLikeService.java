package ontherock.contents.application;

import ontherock.contents.domain.PostLike;
import ontherock.contents.domain.PostLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostLikeService {

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private NotificationService notificationService;

    public void likePost(long postId, long userId) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existingLike.isEmpty()) {
            PostLike like = new PostLike(postId, userId);
            notificationService.sendNotification(NotificationService.NotificationType.LIKE, userId, null);
            postLikeRepository.save(like);
        }
    }

    public void dislikePost(long postId, long userId) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);
        existingLike.ifPresent(postLikeRepository::delete);
    }

    public long getLikeCount(long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    public boolean isPostLikedByUser(long postId, long userId) {
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserId(postId, userId);
        return existingLike.isPresent();
    }
}
