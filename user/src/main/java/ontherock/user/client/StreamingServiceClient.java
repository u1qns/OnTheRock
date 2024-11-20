package ontherock.user.client;

import ontherock.user.dto.StreamingListResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "streaming")
public interface StreamingServiceClient {
    @GetMapping("streaming/list")
    Map<String, StreamingListResponse> streamingList();
}
