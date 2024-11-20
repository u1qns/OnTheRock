package ontherock.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import ontherock.user.application.UserService;
import ontherock.user.common.OntherockException;
import ontherock.user.domain.FollowerRepository;
import ontherock.user.domain.UserRepository;
import ontherock.user.dto.RegisterRequest;
import ontherock.user.dto.RegisterResponse;
import ontherock.user.dto.SearchRequest;
import ontherock.user.dto.UserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    @SpyBean
    UserRepository userRepository;

    @SpyBean
    FollowerRepository followerRepository;

    @AfterEach
    void tearDown() {
        reset(userService);
        reset(userRepository);
        reset(followerRepository);
    }

    @Test
    void register_success() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("naver-12345", "조승기");
        String requestJson = objectMapper.writeValueAsString(registerRequest);

        RegisterResponse registerResponse = new RegisterResponse("1");
        given(userService.regist(any(RegisterRequest.class))).willReturn(registerResponse);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    void register_withoutName_success() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("naver-12345", null);
        String requestJson = objectMapper.writeValueAsString(registerRequest);

        RegisterResponse registerResponse = new RegisterResponse("1");
        given(userService.regist(any(RegisterRequest.class))).willReturn(registerResponse);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    void search_success() throws Exception {
        List<UserResponse> searchResults = Arrays.asList(
                new UserResponse("1", "조승기", "/profile/1.png"),
                new UserResponse("2", "조용기", "/profile/2.png")
        );

        given(userService.search(anyString())).willReturn(searchResults);

        String keyword = "조";
        SearchRequest searchRequest = new SearchRequest(keyword);
        String requestJson = objectMapper.writeValueAsString(searchRequest);

        mockMvc.perform(post("/user/search").content(requestJson).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].nickname").value("조승기"))
                .andExpect(jsonPath("$[0].profilePicture").value("/profile/1.png"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].nickname").value("조용기"))
                .andExpect(jsonPath("$[1].profilePicture").value("/profile/2.png"));
    }

    @Test
    void follow_success() throws Exception {
        String userId = "1";
        String followerId = "2";

        doNothing().when(userService).follow(anyLong(), anyLong());

        mockMvc.perform(post("/user/follow/{userId}", followerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId))
                .andExpect(status().isOk());
    }

    @Test
    void getFollowing_success() throws Exception {
        List<UserResponse> followingUsers = Arrays.asList(
                new UserResponse("1", "조승기", "/profile/1.png"),
                new UserResponse("2", "조용기", "/profile/2.png")
        );

        when(userService.getFollowing(anyLong())).thenReturn(followingUsers);

        mockMvc.perform(get("/user/following")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].nickname").value("조승기"))
                .andExpect(jsonPath("$[0].profilePicture").value("/profile/1.png"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].nickname").value("조용기"))
                .andExpect(jsonPath("$[1].profilePicture").value("/profile/2.png"));
    }

    @Test
    void getProfile_success() throws Exception {
        UserResponse userResponse = new UserResponse("1", "조승기", "/profile/1.png");

        when(userService.getProfile(anyLong())).thenReturn(userResponse);

        mockMvc.perform(get("/user/profile/{userId}", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nickname").value("조승기"))
                .andExpect(jsonPath("$.profilePicture").value("/profile/1.png"));
    }
}
