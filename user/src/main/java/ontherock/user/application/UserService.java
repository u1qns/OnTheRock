package ontherock.user.application;

import lombok.RequiredArgsConstructor;
import ontherock.user.client.StreamingServiceClient;
import ontherock.user.common.OntherockException;
import ontherock.user.domain.Follower;
import ontherock.user.domain.FollowerRepository;
import ontherock.user.domain.User;
import ontherock.user.domain.UserRepository;
import ontherock.user.dto.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ontherock.user.application.NotificationService.NotificationType.FOLLOW;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final FollowerRepository followerRepository;
    private final NotificationService notificationService;
    private final StreamingServiceClient streamingServiceClient;

    public RegisterResponse regist(RegisterRequest registerRequest) {
        User user = User.builder()
                .authId(registerRequest.authId())
                .nickname(Objects.isNull(registerRequest.name()) ? "" : registerRequest.name())
                .profilePicture("https://picsum.photos/seed" + registerRequest.authId() + "/100/100")
                .build();
        
        User savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getUserId().toString());
    }

    public List<UserResponse> search(String keyword) {
        return userRepository.findByNicknameContaining(keyword)
                .stream()
                .map(user -> new UserResponse(user.getUserId().toString(), user.getNickname(), user.getProfilePicture()))
                .toList();
    }

    public Void follow(long userId, long followerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OntherockException(HttpStatus.BAD_REQUEST, "User not found: " + userId));
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new OntherockException(HttpStatus.BAD_REQUEST, "Follower not found: " + followerId));

        if (userId == followerId) {
            throw new OntherockException(HttpStatus.NOT_ACCEPTABLE, "자신을 팔로우할 수 없습니다");
        }

        Follower follow = Follower.builder()
                .user(user)
                .follower(follower)
                .build();

        if (followerRepository.existsByUserAndFollower(user, follower)) {
            throw new OntherockException(HttpStatus.BAD_REQUEST, "Already following this user: " + userId);
        }

        followerRepository.save(follow);

        notificationService.sendNotification(FOLLOW, followerId, userId);

        return null;
    }

    @Transactional
    public Void unfollow(long userId, long followerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OntherockException(HttpStatus.BAD_REQUEST, "User not found: " + userId));
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new OntherockException(HttpStatus.BAD_REQUEST, "Follower not found: " + followerId));


        if (!followerRepository.existsByUserAndFollower(user, follower)) {
            throw new OntherockException(HttpStatus.BAD_REQUEST, "Following not found: " + followerId);
        }

        followerRepository.deleteByUserAndFollower(user, follower);

        return null;
    }

    public List<UserResponse> getFollowing(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OntherockException(HttpStatus.BAD_REQUEST, "User not found: " + userId));

        return followerRepository.findByFollower(user)
                .stream()
                .map(f -> UserResponse.of(f.getUser()))
                .toList();
    }

    public UserResponse getProfile(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OntherockException(HttpStatus.BAD_REQUEST, "User not found: " + userId));
        return UserResponse.of(user);
    }

    public UserResponse updateUser(Long userId, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OntherockException(HttpStatus.BAD_REQUEST, "User not found: " + userId));
        user.setNickname(updateUserRequest.nickname());
        userRepository.save(user);
        return UserResponse.of(user);
    }

    public List<FollowingResponse> getFollowingStatus(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OntherockException(HttpStatus.BAD_REQUEST, "User not found: " + userId));

        Map<String, StreamingListResponse> userToList = streamingServiceClient.streamingList();

        return followerRepository.findByFollower(user)
                .stream()
                .map(follower -> {
                    User followee = follower.getUser();
                    return FollowingResponse.of(userToList.get(followee.getUserId().toString()), followee);
                })
                .toList();
    }

    @Transactional
    public void removeDuplicateFollowers() {
        List<Follower> allFollowers = followerRepository.findAll();

        Set<String> uniqueFollowerPairs = new HashSet<>();
        List<Follower> duplicatesToRemove = new ArrayList<>();

        for (Follower follower : allFollowers) {
            String pairKey = follower.getUser() + "-" + follower.getFollower();
            if (!uniqueFollowerPairs.add(pairKey)) {
                duplicatesToRemove.add(follower);
            }
        }

        if (!duplicatesToRemove.isEmpty()) {
            followerRepository.deleteAll(duplicatesToRemove);
        }
    }
}
