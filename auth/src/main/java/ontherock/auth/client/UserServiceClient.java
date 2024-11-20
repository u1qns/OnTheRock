package ontherock.auth.client;

import ontherock.auth.dto.UserRegisterRequest;
import ontherock.auth.dto.UserRegisterResponse;
import ontherock.auth.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user")
public interface UserServiceClient {
    @PostMapping("user/register")
    UserRegisterResponse register(@RequestBody UserRegisterRequest userRegisterRequest);

    @GetMapping("user/profile/{userId}")
    UserResponse test(@PathVariable Long userId);
}
