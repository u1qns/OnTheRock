package ontherock.streaming.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ontherock.streaming.application.StreamingService;
import ontherock.streaming.dto.request.StreamingRequest;
import ontherock.streaming.dto.response.StreamingListResponse;
import ontherock.streaming.dto.response.StreamingResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/streaming")
public class StreamingController {

    private final StreamingService streamingService;

    public StreamingController(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @PostMapping("/start")
    @Operation(summary = "스트리밍 시작", description = "사용자를 위한 새로운 스트리밍 세션을 시작합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스트리밍이 성공적으로 시작되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다.")
    })
    public ResponseEntity<StreamingResponse> startStreaming(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody StreamingRequest request) {

        log.info("Starting streaming for user: {}", userId);
        request.setUserId(userId);
        return ResponseEntity.ok(streamingService.startStreaming(request));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = "스트리밍 중지", description = "사용자의 스트리밍 세션을 중지합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스트리밍이 성공적으로 중지되었습니다."),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없습니다.")
    })
    public ResponseEntity<StreamingResponse> stopStreaming(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String sessionId,
            @RequestBody StreamingRequest streamingRequest) {

        log.info("Stopping streaming for user: {}", userId);
        streamingService.stopStreaming(userId);
        return ResponseEntity.ok(new StreamingResponse("Streaming stopped successfully."));
    }


    @PostMapping("/{sessionId}")
    @Operation(summary = "스트리밍 참여", description = "사용자가 기존의 스트리밍 세션에 참여할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "세션에 성공적으로 참여하였습니다."),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없습니다.")
    })
    public ResponseEntity<StreamingResponse> joinStreaming(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String sessionId,
            @RequestBody StreamingRequest request) {
        log.info("User {} is joining session: {}", userId, sessionId);
        String token = streamingService.joinStreaming(sessionId, request);
        return ResponseEntity.ok(new StreamingResponse("Successfully joined the session.", sessionId, token));

    }

    @SneakyThrows
    @GetMapping("/list")
    @Operation(summary = "활성 스트리밍 목록", description = "활성 스트리밍 세션의 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활성 스트리밍 목록을 성공적으로 조회하였습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생했습니다.")
    })
    public ResponseEntity< ConcurrentHashMap<String, StreamingListResponse>> listPublisherSession() {
        ConcurrentHashMap<String, StreamingListResponse> activeStreams = streamingService.getSessionsWithPublishers();
        return ResponseEntity.ok(activeStreams);
    }
}
