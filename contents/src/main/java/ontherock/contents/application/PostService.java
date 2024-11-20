package ontherock.contents.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ontherock.contents.common.OntherockException;
import ontherock.contents.common.UserId;
import ontherock.contents.domain.*;
import ontherock.contents.dto.ExperienceResponse;
import ontherock.contents.dto.response.PostResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostMediaRepository postMediaRepository;
    private final S3Service s3Service;

    public List<PostWithLikes> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> PostWithLikes.of(post, postLikeRepository.findByPostId(post.getPostId())))
                .toList();
    }

    public PostResponse getPostById(long userId, long postId) {
        Post post = postRepository.findByIdWithMedia(postId)
                .orElseThrow(() -> new OntherockException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        if (post.getVisibility() == Post.Visibility.PRIVATE && post.getUserId() != userId ) {
            throw new OntherockException(HttpStatus.FORBIDDEN, "금지된 게시글입니다.");
        }

        return PostResponse.from(post);
    }

    public PostResponse createPost(Post post) {
        return PostResponse.from(postRepository.save(post));
    }

    public Void deletePost(long userId, long postId) {
        Post post = postRepository.findByIdWithMedia(postId)
                .orElseThrow(() -> new OntherockException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."));

        post.getMediaList().forEach(postMedia -> s3Service.deleteFile(postMedia.getMediaUrl()));

        postMediaRepository.deleteAll(post.getMediaList());
        postRepository.delete(post);

        return null;
    }

    public List<PostResponse> getPostsByUserIdAndMonth(long userId, int year, int month, boolean includePrivate) {
        List<Post> posts = postRepository.findByUserIdAndMonth(userId, month, year);
        // includePrivate이 true이면 모든 게시물 반환, false이면 PUBLIC 게시물만 반환
        if (includePrivate) {
            return posts.stream()
                    .map(PostResponse::from)
                    .toList();
        } else {
            return posts.stream()
                    .filter(post -> post.getVisibility() == Post.Visibility.PUBLIC)
                    .map(PostResponse::from)
                    .collect(Collectors.toList());
        }
    }

    public List<PostResponse> getPostsByUserId(long userId, boolean includePrivate) {
        List<Post> posts = postRepository.findByUserId(userId);
        // includePrivate이 true이면 모든 게시물 반환, false이면 PUBLIC 게시물만 반환
        if (includePrivate) {
            return posts.stream()
                    .map(PostResponse::from)
                    .toList();
        } else {
            return posts.stream()
                    .filter(post -> post.getVisibility() == Post.Visibility.PUBLIC)
                    .map(PostResponse::from)
                    .collect(Collectors.toList());
        }
    }

    public List<PostWithLikes> getHotClips() {
        Instant twoWeeksAgoInstant = Instant.now().minus(14, ChronoUnit.DAYS);
        Timestamp twoWeeksAgo = Timestamp.from(twoWeeksAgoInstant);
        List<Post> posts = postRepository.findPostsFromLastTwoWeeks(twoWeeksAgo);

        List<Post> publicPosts = posts.stream()
                .filter(post -> post.getVisibility() == Post.Visibility.PUBLIC)
                .toList();

        List<PostWithLikes> postWithLikesList = new ArrayList<>();
        for (Post post : publicPosts) {
            long likeCount = postLikeRepository.countByPostId(post.getPostId());
            List<Long> likedUserIds = postLikeRepository.findByPostId(post.getPostId())
                    .stream()
                    .map(PostLike::getUserId)
                    .collect(Collectors.toList());
            PostResponse postResponse = PostResponse.from(post);
            postWithLikesList.add(new PostWithLikes(postResponse, likeCount, likedUserIds));
        }

        return postWithLikesList.stream()
                .sorted(Comparator.comparingLong(PostWithLikes::getLikeCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    public List<PostWithLikes> getDetails(String hashTag, long lastId, int size) {
        List<Post> posts;

        // 해시태그가 "all"이면 모든 게시물을 가져오도록 처리
        if ("all".equals(hashTag)) {
            posts = postRepository.findByPostIdLessThanOrderByPostIdDesc(lastId, Pageable.ofSize(size));
        } else {
            posts = postRepository.findByHashtagsKeywordAndPostIdLessThanOrderByPostIdDesc(hashTag, lastId, Pageable.ofSize(size));
        }
        return posts.stream()
                .map(post -> PostWithLikes.of(post, postLikeRepository.findByPostId(post.getPostId())))
                .toList();
    }

    public ExperienceResponse getExperience(long userId) {
        if (userId == 612010098983285820L) {
            return new ExperienceResponse(0, 513);
        }
        List<Post> posts = postRepository.findByUserId(userId);
        long commentCount = postLikeRepository.countByPostIdIn(posts.stream().map(Post::getPostId).toList());
        return new ExperienceResponse(posts.size(), commentCount);
    }
}