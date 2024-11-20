package ontherock.contents.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "post_likes")
public class PostLike {

    @Id
    private String id;

    @Indexed
    private long postId;
    @Indexed
    private long userId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Instant createdAt;

    public PostLike(long postId, long userId) {
        this.postId = postId;
        this.userId = userId;
        this.createdAt = Instant.now();
    }
}