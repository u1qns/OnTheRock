package ontherock.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import ontherock.user.application.UserService;
import ontherock.user.common.UserId;
import ontherock.user.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody RegisterRequest registerRequest
    ) {
        return ResponseEntity.ok(userService.regist(registerRequest));
    }

    @PostMapping("/search")
    public ResponseEntity<List<UserResponse>> search(@RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(userService.search(searchRequest.keyword()));
    }

    @PostMapping("/follow/{followeeId}")
    public ResponseEntity<Void> follow(
            @UserId long followerId,
            @PathVariable long followeeId
    ) {
        userService.follow(followeeId, followerId);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/unfollow/{followeeId}")
    public ResponseEntity<Void> unfollow(
            @UserId long followerId,
            @PathVariable long followeeId
    ) {
        return ResponseEntity.ok(userService.unfollow(followeeId, followerId));
    }

    @GetMapping("/following")
    public ResponseEntity<List<UserResponse>> followers(@UserId long myId) {
        return ResponseEntity.ok(userService.getFollowing(myId));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserResponse> getProfile(@PathVariable long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@UserId long myId) {
        return ResponseEntity.ok(userService.getProfile(myId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateUser(
            @UserId long userId,
            @RequestBody UpdateUserRequest updateUserRequest
    ) {
        return ResponseEntity.ok(userService.updateUser(userId, updateUserRequest));
    }

    @GetMapping("/following/status")
    public ResponseEntity<List<FollowingResponse>> getFollowing(@UserId long userId) {
        return ResponseEntity.ok(userService.getFollowingStatus(userId));
    }

    @Operation(description = "테스트용 중복 모든 팔로워 제거 API")
    @GetMapping("/test1")
    public ResponseEntity<Void> test1() {
        userService.removeDuplicateFollowers();
        return null;
    }
}
