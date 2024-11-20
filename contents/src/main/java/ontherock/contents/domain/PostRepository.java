package ontherock.contents.domain;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.mediaList WHERE p.postId = :postId")
    Optional<Post> findByIdWithMedia(@Param("postId") long postId);

    @Query("SELECT p FROM Post p WHERE p.userId = :userId " +
            "AND FUNCTION('MONTH', p.createdAt) = :month " +
            "AND FUNCTION('YEAR', p.createdAt) = :year")
    List<Post> findByUserIdAndMonth(@Param("userId") long userId,
                                    @Param("month") int month,
                                    @Param("year") int year);

    List<Post> findByUserId(long userId);

    @Query("SELECT p FROM Post p " +
            "WHERE p.createdAt > :startDate " +
            "ORDER BY p.createdAt DESC")
    List<Post> findPostsFromLastTwoWeeks(@Param("startDate") Timestamp startDate);


    List<Post> findByHashtagsKeywordAndPostIdLessThanOrderByPostIdDesc(String hashtag, Long lastIndex, Pageable pageable);

    List<Post> findByPostIdLessThanOrderByPostIdDesc(Long lastId, Pageable pageable);
}
