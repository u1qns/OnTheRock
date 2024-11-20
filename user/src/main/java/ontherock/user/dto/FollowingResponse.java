package ontherock.user.dto;

import lombok.Builder;
import ontherock.user.domain.User;

import java.util.Objects;

@Builder
public record FollowingResponse(
        String id,
        String streamingSessionId,
        String name,
        String profilePicture
) {
    public static FollowingResponse of(StreamingListResponse listResponse, User user) {
        return FollowingResponse.builder()
                .id(user.getUserId().toString())
                .streamingSessionId(Objects.isNull(listResponse) ? null : listResponse.sessionId())
                .name(user.getNickname())
                .profilePicture(user.getProfilePicture())
                .build();
    }
}
