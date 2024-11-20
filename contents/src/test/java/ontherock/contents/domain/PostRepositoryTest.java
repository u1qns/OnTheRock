package ontherock.contents.domain;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @BeforeEach
    void setUp() {
        Hashtag hashtag1 = new Hashtag(null, "hashtag");
        Hashtag hashtag2 = new Hashtag(null, "another");

        hashtag1 = hashtagRepository.save(hashtag1);
        hashtag2 = hashtagRepository.save(hashtag2);

        Post post1 = new Post(0L, 1L, 1L, "First post with #hashtag", "Content1", Post.Visibility.PUBLIC, true, null, null, List.of(hashtag1));
        Post post2 = new Post(0L, 1L, 1L, "Second post with #hashtag", "Content2", Post.Visibility.PUBLIC, true, null, null, List.of(hashtag1));
        Post post3 = new Post(0L, 1L, 1L, "Third post with #hashtag", "Content3", Post.Visibility.PUBLIC, true, null, null, List.of(hashtag1));
        Post post4 = new Post(0L, 1L, 1L, "Fourth post with #another", "Content4", Post.Visibility.PUBLIC, true, null, null, List.of(hashtag2));

        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);
        postRepository.save(post4);
    }

//    @Test
    void TagPaging_success1() {
        String hashtag = "hashtag";
        Long lastIndex = 2L;
        Pageable pageable = PageRequest.ofSize(2);

        List<Post> posts = postRepository.findByHashtagsKeywordAndPostIdLessThanOrderByPostIdDesc(hashtag, lastIndex, pageable);
        System.out.println(posts);
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getPostId()).isEqualTo(1L);
    }

//    @Test
    void TagPaging_success2() {
        String hashtag = "another";
        Long lastIndex = Long.MAX_VALUE;
        Pageable pageable = PageRequest.ofSize(2);

        List<Post> posts = postRepository.findByHashtagsKeywordAndPostIdLessThanOrderByPostIdDesc(hashtag, lastIndex, pageable);
        System.out.println(posts);

        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getPostId()).isEqualTo(4L);
    }
}
