package ontherock.contents.application;

import static org.junit.jupiter.api.Assertions.*;

import ontherock.contents.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class PostServiceTest {

    @MockBean
    private PostRepository postRepository;

    @MockBean
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostService postService;

    @Test
    void getDetails_success() {
        String hashTag = "example";
        long lastId = 100L;
        int size = 3;

        Post post1 = new Post(1L, 1L, 1L, "Title1", "Content1", Post.Visibility.PUBLIC, true, null, null, null);
        Post post2 = new Post(2L, 1L, 1L, "Title2", "Content2", Post.Visibility.PUBLIC, true, null, null, null);
        Post post3 = new Post(3L, 1L, 1L, "Title3", "Content3", Post.Visibility.PUBLIC, true, null, null, null);

        List<PostLike> likes1 = Arrays.asList(new PostLike(post1.getPostId(), 1L), new PostLike(post1.getPostId(), 2L));
        List<PostLike> likes2 = Arrays.asList(new PostLike(post2.getPostId(), 1L));
        List<PostLike> likes3 = Arrays.asList();

        when(postRepository.findByHashtagsKeywordAndPostIdLessThanOrderByPostIdDesc(eq(hashTag), eq(lastId), any())).thenReturn(Arrays.asList(post1, post2, post3));
        when(postLikeRepository.findByPostId(post1.getPostId())).thenReturn(likes1);
        when(postLikeRepository.findByPostId(post2.getPostId())).thenReturn(likes2);
        when(postLikeRepository.findByPostId(post3.getPostId())).thenReturn(likes3);

        List<PostWithLikes> result = postService.getDetails(hashTag, lastId, size);

        assertEquals(3, result.size());
        assertEquals(post1.getPostId(), result.get(0).getPost().postId());
        assertEquals(likes1.size(), result.get(0).getLikedUserIds().size());
        assertEquals(post2.getPostId(), result.get(1).getPost().postId());
        assertEquals(likes2.size(), result.get(1).getLikedUserIds().size());
        assertEquals(post3.getPostId(), result.get(2).getPost().postId());
        assertEquals(likes3.size(), result.get(2).getLikedUserIds().size());

        verify(postRepository).findByHashtagsKeywordAndPostIdLessThanOrderByPostIdDesc(eq(hashTag), eq(lastId), any());
        verify(postLikeRepository).findByPostId(post1.getPostId());
        verify(postLikeRepository).findByPostId(post2.getPostId());
        verify(postLikeRepository).findByPostId(post3.getPostId());
    }

    @Test
    void getDetails_noPosts() {
        String hashTag = "example";
        long lastId = 100L;
        int size = 3;

        when(postRepository.findByHashtagsKeywordAndPostIdLessThanOrderByPostIdDesc(eq(hashTag), eq(lastId), any())).thenReturn(Arrays.asList());

        List<PostWithLikes> result = postService.getDetails(hashTag, lastId, size);

        assertTrue(result.isEmpty());

        verify(postRepository).findByHashtagsKeywordAndPostIdLessThanOrderByPostIdDesc(eq(hashTag), eq(lastId), any());
        verify(postLikeRepository, never()).findByPostId(anyLong());
    }
}
