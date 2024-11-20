package ontherock.user.application;

import ontherock.user.common.OntherockException;
import ontherock.user.domain.Follower;
import ontherock.user.domain.FollowerRepository;
import ontherock.user.domain.User;
import ontherock.user.domain.UserRepository;
import ontherock.user.dto.RegisterRequest;
import ontherock.user.dto.RegisterResponse;
import ontherock.user.dto.UpdateUserRequest;
import ontherock.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private FollowerRepository followerRepository;
    @Autowired
    private UserService userService;
    @MockBean
    private NotificationService notificationService;

    @Test
    void register_success() {
        RegisterRequest registerRequest = new RegisterRequest("naver-12345", "조승기");
        User user = new User(1L, registerRequest.authId(), registerRequest.name(), null, 0, "beginner", LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(user);

        RegisterResponse response = userService.regist(registerRequest);

        assertEquals("1", response.userId());
    }

    @Test
    void register_withoutName_success() {
        RegisterRequest registerRequest = new RegisterRequest("naver-12345", null);
        User user = new User(1L, registerRequest.authId(), null, null, 0, "beginner", LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.save(any(User.class))).thenReturn(user);

        RegisterResponse response = userService.regist(registerRequest);

        assertEquals("1", response.userId());
    }

    @Test
    void search_success() {
        String keyword = "조";
        List<User> userList = Arrays.asList(
                User.builder().userId(1L).nickname("조용기").profilePicture("/profile/1.png").build(),
                User.builder().userId(2L).nickname("조승기").profilePicture("/profile/2.png").build()
        );

        when(userRepository.findByNicknameContaining(anyString())).thenReturn(userList);
        List<UserResponse> response = userService.search(keyword);

        assertEquals(2, response.size());
        assertEquals(1, response.stream().filter(r -> "조승기".equals(r.nickname())).count());
        assertEquals(1, response.stream().filter(r -> r.id().equals("2")).count());
    }

    @Test
    void follow_success() {
        long userId = 1L;
        long followerId = 2L;

        User user = User.builder().userId(userId).build();
        User follower = User.builder().userId(followerId).build();
        Follower follow = Follower.builder().user(user).follower(follower).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
        when(followerRepository.save(any(Follower.class))).thenReturn(follow);
        doNothing().when(notificationService).sendNotification(any(), anyLong(), anyLong());

        userService.follow(userId, followerId);

        verify(userRepository).findById(userId);
        verify(userRepository).findById(followerId);
        verify(followerRepository).save(argThat(argument ->
                argument.getUser().getUserId() == userId &&
                        argument.getFollower().getUserId() == followerId
        ));
    }

    @Test
    void follow_userNotFound() {
        long userId = 1L;
        long followerId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(OntherockException.class, () -> {
            userService.follow(userId, followerId);
        });

        assertEquals("User not found: " + userId, exception.getMessage());
    }

    @Test
    void follow_followerNotFound() {
        long userId = 1L;
        long followerId = 2L;

        User user = User.builder().userId(userId).build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findById(followerId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(OntherockException.class, () -> {
            userService.follow(userId, followerId);
        });

        assertEquals("Follower not found: " + followerId, exception.getMessage());
    }

//    @Test
    void getFollowing_success() {
        long userId = 1L;

        User user = User.builder().userId(userId).build();
        List<User> followerUsers = Stream.of(2L, 3L, 4L).map(f -> User.builder().userId(f).build()).toList();
        List<Follower> followers = followerUsers.stream().map(u -> Follower.builder().follower(u).build()).toList();

        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        when(followerRepository.findByUser(user)).thenReturn(followers);

        List<UserResponse> result = userService.getFollowing(userId);

        assertEquals(followerUsers.size(), result.size());
        verify(followerRepository).findByUser(user);
    }

    @Test
    void getFollowing_userNotFound() {
        long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(OntherockException.class, () -> {
            userService.getFollowing(userId);
        });

        assertEquals("User not found: " + userId, exception.getMessage());
    }

    @Test
    void getProfile_success() {
        long userId = 1L;
        User user = User.builder().userId(userId).nickname("nickname").profilePicture("profile.jpg").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse result = userService.getProfile(userId);

        assertEquals(String.valueOf(userId), result.id());
        assertEquals("nickname", result.nickname());
        assertEquals("profile.jpg", result.profilePicture());
        verify(userRepository).findById(userId);
    }

    @Test
    void getProfile_userNotFound() {
        long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(OntherockException.class, () -> {
            userService.getProfile(userId);
        });

        assertEquals("User not found: " + userId, exception.getMessage());
    }

    @Test
    void updateUser_success() {
        long userId = 1L;
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("newNickname");

        User existingUser = User.builder()
                .userId(userId)
                .nickname("oldNickname")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserResponse result = userService.updateUser(userId, updateUserRequest);

        assertEquals("newNickname", result.nickname());
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_userNotFound() {
        long userId = 1L;
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("newNickname");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(OntherockException.class, () -> {
            userService.updateUser(userId, updateUserRequest);
        });

        assertEquals("User not found: " + userId, exception.getMessage());
        verify(userRepository).findById(userId);
    }
}