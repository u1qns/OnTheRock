package ontherock.user.dto;

import ontherock.user.domain.User;

public record UserResponse(
        String id,
        String nickname,
        String profilePicture
) {
    public static UserResponse of(User user) {
        return new UserResponse(user.getUserId().toString(), user.getNickname(), user.getProfilePicture());
    }
}
