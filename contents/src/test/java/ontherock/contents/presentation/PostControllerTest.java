package ontherock.contents.presentation;

import ontherock.contents.domain.*;
import ontherock.contents.application.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private HashtagRepository hashtagRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostService postService;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();
        postLikeRepository.deleteAll();
        hashtagRepository.deleteAll();

        Hashtag hashtag1 = new Hashtag(null, "hashtag");
        Hashtag hashtag2 = new Hashtag(null, "another");

        hashtag1 = hashtagRepository.save(hashtag1);
        hashtag2 = hashtagRepository.save(hashtag2);

        Post post1 = new Post(1L, 1L, 1L, "First post with #hashtag", "Content1", Post.Visibility.PUBLIC, true, null, null, List.of(hashtag1));
        Post post2 = new Post(2L, 1L, 1L, "Second post with #hashtag", "Content2", Post.Visibility.PUBLIC, true, null, null, List.of(hashtag1));
        Post post3 = new Post(3L, 1L, 1L, "Third post with #hashtag", "Content3", Post.Visibility.PUBLIC, true, null, null, List.of(hashtag1));
        Post post4 = new Post(4L, 1L, 1L, "Fourth post with #another", "Content4", Post.Visibility.PUBLIC, true, null, null, List.of(hashtag2));
        postRepository.saveAll(List.of(post1, post2, post3, post4));

        postLikeRepository.saveAll(List.of(
                new PostLike(post1.getPostId(), 2L),
                new PostLike(post2.getPostId(), 3L),
                new PostLike(post2.getPostId(), 4L)
        ));
    }

//    @Test
    void shouldGetExperience() throws Exception {
        long userId = 1L;

        mockMvc.perform(get("/contents/experience")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.postCount").value(4))
                .andExpect(MockMvcResultMatchers.jsonPath("$.likeCount").value(3));
    }

//    @Test
    void shouldGetPostsByHashtagAndIndex() throws Exception {
        String hashtag = "hashtag";
        Long lastIndex = 123L;
        int size = 2;

        MvcResult result = mockMvc.perform(get("/contents/feed")
                        .param("hashtag", hashtag)
                        .param("lastIndex", lastIndex.toString())
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        System.out.println("JSON Response: " + jsonResponse);

        mockMvc.perform(get("/contents/feed")
                        .param("hashtag", hashtag)
                        .param("lastIndex", lastIndex.toString())
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1)); // Check the response length
    }
}
