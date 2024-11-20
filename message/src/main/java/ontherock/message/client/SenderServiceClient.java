package ontherock.message.client;

import ontherock.message.dto.SendRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "sender")
public interface SenderServiceClient {
    @PostMapping("sender/notification")
    ResponseEntity<Void> send(@RequestBody SendRequest sendRequest);
}