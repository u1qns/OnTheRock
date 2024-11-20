package ontherock.contents.domain;

import lombok.*;
import ontherock.contents.dto.response.PostResponse;

import java.util.Collection;
import java.util.List;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostWithLikes {
    private PostResponse post;
    private long likeCount;
    private List<Long> likedUserIds;

    public static PostWithLikes of(Post post, List<PostLike> postLikes) {
        List<Long> likedUsers = postLikes.stream().map(PostLike::getUserId).toList();
        return PostWithLikes.builder()
                .post(PostResponse.from(post))
                .likedUserIds(likedUsers)
                .likeCount(likedUsers.size())
                .build();
    }
}
