package ontherock.auth.dto;

public record TestUserResponse (
        String id,
        String name,
        String accessToken
){
}
