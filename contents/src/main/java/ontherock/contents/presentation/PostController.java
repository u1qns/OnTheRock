package ontherock.contents.presentation;

import com.amazonaws.Response;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ontherock.contents.application.*;
import ontherock.contents.batch.HotClipsCache;
import ontherock.contents.common.OntherockException;
import ontherock.contents.common.UserId;
import ontherock.contents.domain.*;
import ontherock.contents.dto.ExperienceResponse;
import ontherock.contents.dto.response.PostResponse;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Autowired
    private PostService postService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private PostHashtagService postHashtagService;

    @Autowired
    private PostSearchService postSearchService;

    @Autowired
    private HotClipsCache hotClipsCache;
    @Autowired
    private NotificationService notificationService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PostWithLikes>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping(value = "/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostResponse> getPostById(
            @PathVariable("postId") long postId,
            @UserId long userId
    ) {
        return ResponseEntity.ok(postService.getPostById(userId, postId));
    }

    @Operation(description = "프론트 사용 금지 X")
    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody Post post) {
        log.error("잘못된 포스트 호출");
        return ResponseEntity.ok(postService.createPost(post));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @UserId long userId,
            @PathVariable int postId
    ) {
        postService.deletePost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(
            value = "/media",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE },
            produces = "application/json; charset=UTF-8"
    )
    public ResponseEntity<PostResponse> createPostWithMedia(
            @RequestPart("post") String postJson,
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart("mediaTypes") String mediaTypesJson,
            @UserId long userId,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        log.error("게시글 미디어포함 포스트 작동");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // UTF-8 변환 작업
        postJson = new String(postJson.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        mediaTypesJson = new String(mediaTypesJson.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

        List<PostMedia.MediaType> mediaTypes = objectMapper.readValue(mediaTypesJson, new TypeReference<List<PostMedia.MediaType>>() {
        });

        long maxFileSize = 5 * 1024 * 1024; // 5MB
        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif", "mp4", "avi", "mov");

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            PostMedia.MediaType mediaType = mediaTypes.get(i);

            if (file.getSize() > maxFileSize) {
                throw new OntherockException(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기가 너무 큽니다.");
            }

            String originalFileName = file.getOriginalFilename();
            if (originalFileName != null && !originalFileName.isEmpty()) {
                String fileExtension = getFileExtension(originalFileName);
                if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
                    throw new OntherockException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 미디어 타입입니다.");
                }
            }

            // 미디어 타입 일치 확인
            String mimeType = file.getContentType();
            if (!isMediaTypeMatching(mimeType, mediaType)) {
                throw new OntherockException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 미디어 타입입니다.");
            }
        }

        Post post = objectMapper.readValue(postJson, Post.class);

        post.setCreatedAt(Timestamp.from(Instant.now()));
        Post savedPost = postRepository.save(post);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            PostMedia.MediaType mediaType = mediaTypes.get(i);

            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = generateUniqueFileName(originalFileName);

            Path tempFile = Files.createTempFile("upload-", uniqueFileName);
            Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            String url = s3Service.uploadFile(tempFile, uniqueFileName);
            Files.delete(tempFile);

            PostMedia postMedia = new PostMedia();
            postMedia.setPost(savedPost);
            postMedia.setMediaType(mediaType);
            postMedia.setMediaUrl(url);
            postMediaRepository.save(postMedia);
        }

        PostResponse savedPostResponse = postRepository.findByIdWithMedia(savedPost.getPostId())
                .map(PostResponse::from)
                .orElseThrow(() -> new OntherockException(HttpStatus.BAD_REQUEST, "게시글 업로드 실패"));
        notificationService.sendNotification(NotificationService.NotificationType.NEW_POST, userId, null);
        return ResponseEntity.ok(savedPostResponse);
    }

    private boolean isMediaTypeMatching(String mimeType, PostMedia.MediaType mediaType) {
        if (mediaType == PostMedia.MediaType.IMAGE && mimeType.startsWith("image/")) {
            return true;
        } else if (mediaType == PostMedia.MediaType.VIDEO && mimeType.startsWith("video/")) {
            return true;
        } else if (mediaType == PostMedia.MediaType.THUMBNAIL && mimeType.startsWith("image/")) {
            return true;
        } else if (mediaType == PostMedia.MediaType.ANALYSIS) {
            // 분석 파일의 경우 추가 로직 필요
            return true; // 예시로 일치시킴
        }
        return false;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    private String generateUniqueFileName(String originalFileName) {
        String fileExtension = getFileExtension(originalFileName);
        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        String uniqueName = baseName + "_" + UUID.randomUUID().toString();
        if (!fileExtension.isEmpty()) {
            uniqueName += "." + fileExtension;
        }
        return uniqueName;
    }

    @GetMapping("/{userId}/calendar")
    public ResponseEntity<List<PostResponse>> getPostsByUserIdAndMonth(
            @PathVariable long userId,
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @UserId long currentUserId) {
        // 현재 사용자 ID와 조회 대상 사용자 ID가 같은지 확인
        boolean includePrivate = userId == currentUserId;
        return ResponseEntity.ok(postService.getPostsByUserIdAndMonth(userId, year, month, includePrivate));
    }


    @GetMapping("/{userId}/feed")
    public ResponseEntity<List<PostResponse>> getPostsByUserId(
            @PathVariable long userId,
            @UserId long currentUserId) {
        // 현재 사용자 ID와 조회 대상 사용자 ID가 같은지 확인
        boolean includePrivate = userId == currentUserId;
        return ResponseEntity.ok(postService.getPostsByUserId(userId, includePrivate));
    }

    @PostMapping("/{postId}/hashtags")
    public ResponseEntity<Void> addHashtagsToPost(@PathVariable long postId, @RequestBody List<String> keywords) {
        postHashtagService.addHashtagsToPost(postId, keywords);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<PostResponse>> findPostsByHashtag(@RequestParam String keyword) {
        return ResponseEntity.ok(postSearchService.findPostsByHashtag(keyword));
    }

    @GetMapping("/hotclips")
    public ResponseEntity<List<PostWithLikes>> getHotClips() {
        List<PostWithLikes> hotClips = hotClipsCache.getHotClips();
        return ResponseEntity.ok(hotClips);
    }

    @GetMapping("/feed")
    public ResponseEntity<List<PostWithLikes>> getPostsByHashtagAndIndex(
            @RequestParam String hashtag,
            @RequestParam(required = false) Long lastIndex,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(postService.getDetails(hashtag, Objects.isNull(lastIndex) ? Long.MAX_VALUE : lastIndex, size));
    }

    @GetMapping("/experience")
    public ResponseEntity<ExperienceResponse> getExperience(@UserId long userId) {
        return ResponseEntity.ok(postService.getExperience(userId));
    }
}
