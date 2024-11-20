package ontherock.contents.presentation;

import ontherock.contents.application.PostLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contents")
public class PostLikeController {

    @Autowired
    private PostLikeService postLikeService;

    @PostMapping("/{postId}/likes")
    public ResponseEntity<Void> likePost(@PathVariable long postId, @RequestParam long userId) {
        postLikeService.likePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{postId}/dislikes")
    public ResponseEntity<Void> dislikePost(@PathVariable long postId, @RequestParam long userId) {
        postLikeService.dislikePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<Long> getLikeCount(@PathVariable long postId) {
        long likeCount = postLikeService.getLikeCount(postId);
        return ResponseEntity.ok(likeCount);
    }

    @GetMapping("/{postId}/likes/status")
    public ResponseEntity<Boolean> isPostLikedByUser(@PathVariable long postId, @RequestParam long userId) {
        boolean isLiked = postLikeService.isPostLikedByUser(postId, userId);
        return ResponseEntity.ok(isLiked);
    }
}
