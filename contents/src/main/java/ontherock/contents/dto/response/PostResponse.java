package ontherock.contents.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import ontherock.contents.domain.Hashtag;
import ontherock.contents.domain.Post;
import ontherock.contents.domain.PostMedia;

import java.sql.Timestamp;
import java.util.List;

@Builder
public record PostResponse(
        long postId,
        String userId,
        String title,
        String content,
        boolean success,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        Timestamp createdAt,
        List<PostMedia> mediaList,
        List<Hashtag> hashtags
) {
    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .postId(post.getPostId())
                .userId(String.valueOf(post.getUserId()))
                .title(post.getTitle())
                .content(post.getContent())
                .success(post.isSuccess())
                .createdAt(post.getCreatedAt())
                .hashtags(post.getHashtags())
                .mediaList(post.getMediaList())
                .build();
    }
}
