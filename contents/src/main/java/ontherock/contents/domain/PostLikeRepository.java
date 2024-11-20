package ontherock.contents.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends MongoRepository<PostLike, String> {
    Optional<PostLike> findByPostIdAndUserId(long postId, long userId);

    @Query(value = "{ 'postId': ?0 }", count = true)
    long countByPostId(long postId);

    List<PostLike> findByPostId(long postId);

    @Query(value = "{ 'postId': { $in: ?0 } }", count = true)
    long countByPostIdIn(List<Long> postIds);
}