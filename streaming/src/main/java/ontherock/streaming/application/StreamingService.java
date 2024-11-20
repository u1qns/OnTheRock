package ontherock.streaming.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openvidu.java.client.*;
import lombok.extern.slf4j.Slf4j;
import ontherock.streaming.common.OntherockException;
import ontherock.streaming.dto.request.StreamingRequest;
import ontherock.streaming.dto.response.StreamingListResponse;
import ontherock.streaming.dto.response.StreamingResponse;
import ontherock.streaming.exception.StreamingException;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ontherock.streaming.application.NotificationService.NotificationType.STREAMING;

@Service
@Slf4j
public class StreamingService {

    private final OpenVidu openVidu;
    private final NotificationService notificationService;

    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>(); // userId

    @Autowired
    public StreamingService(OpenVidu openVidu, NotificationService notificationService) {
        this.openVidu = openVidu;
        this.notificationService = notificationService;
    }

    /**
     * 스트리밍을 시작합니다.
     *
     * @param request 스트리밍 요청 정보 (userId 포함)
     * @return StreamingResponse 스트리밍 시작 결과 및 연결 토큰
     * @throws OntherockException 세션 생성 또는 연결 생성 중 오류 발생 시
     */
    public StreamingResponse startStreaming(StreamingRequest request) {
        String userId = request.getUserId();
        if (userId == null) {
            log.error("userId is missing in the request.");
            throw new OntherockException(HttpStatus.BAD_REQUEST, "UserId is required.");
        }

        Session session = sessions.get(userId);

        try {
            openVidu.fetch();

            // 새로운 세션 생성 또는 유효하지 않은 경우
            if (session == null || openVidu.getActiveSession(session.getSessionId()) == null) {
                sessions.remove(userId); // 유효하지 않은 세션 삭제
                session = openVidu.createSession();
                log.info("New session created for user: {}", userId);
            } else {
                log.info("Using existing session for user: {}", userId);
            }

            Connection connection = createConnection(session, userId);
            sessions.put(userId, session); // 커넥션까지 잘 되었을 때 세션 저장

            log.info("Streaming started for user: {}. Session ID: {}, Token: {}",
                    userId, session.getSessionId(), connection.getToken());

            notificationService.sendNotification(STREAMING, Long.parseLong(userId), -1L);

            return new StreamingResponse("Streaming started successfully.", session.getSessionId(), connection.getToken());

        } catch (JsonProcessingException e) {
            log.error("JSON processing error for user: {}", userId, e);
            throw new OntherockException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (OpenViduException e) {
            log.error("Error during streaming process for user: {}", userId, e);
            throw new OntherockException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    /**
     * 세션에 연결을 생성합니다.
     *
     * @param session 스트리밍 세션
     * @param userId 사용자 ID
     * @return 생성된 연결 객체
     * @throws OpenViduException 연결 생성 중 오류 발생 시
     */
    private Connection createConnection(Session session, String userId)
            throws OpenViduException, JsonProcessingException {

        Map<String, String> connectionData = new HashMap<>();
        connectionData.put("userId", userId);
        connectionData.put("createdAt", String.valueOf(System.currentTimeMillis()));
        String jsonData = new ObjectMapper().writeValueAsString(connectionData);

        return session.createConnection(new ConnectionProperties.Builder()
                .role(OpenViduRole.PUBLISHER)
                .data(jsonData)
                .build());
    }

    /**
     * 스트리밍을 중지합니다.
     *
     * @param userId 사용자 ID
     * @throws RuntimeException 스트리밍 중지 중 오류 발생 시
     */
    public void stopStreaming(String userId) {

        if (userId == null) {
            throw new OntherockException(HttpStatus.BAD_REQUEST, "UserId is required.");
        }

        Session session = sessions.get(userId);
        if (session == null) {
            throw new OntherockException(HttpStatus.NOT_FOUND, "Streaming session not found for user: " + userId);
        }

        try {
            session.close();
            log.info("Streaming stopped for user: {}", userId);
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            log.error("Error closing session for userId {}: {}", userId, e);
            throw new StreamingException("Error closing session for user: " + userId);
        } finally {
            sessions.remove(userId);
        }
    }



    /**
     * 스트리밍 세션에 참여합니다.
     *
     * @param sessionId 스트리밍 세션 ID
     * @param request 스트리밍 요청 정보 (userId 포함)
     * @return String 생성된 연결 토큰
     * @throws OntherockException 세션이 존재하지 않을 경우 및 연결 생성 중 오류 발생 시
     */
    public String joinStreaming(String sessionId, StreamingRequest request) {

        if (sessionId == null) {
            throw new OntherockException(HttpStatus.BAD_REQUEST, "UserId is required.");
        }

        Session activeSession = openVidu.getActiveSession(sessionId);
        if (activeSession == null) {
            throw new OntherockException(HttpStatus.NOT_FOUND, "Session not found for sessionId: " + sessionId);
        }

        try {
            Map<String, String> connectionData = new HashMap<>();
            connectionData.put("userId", request.getUserId()); // userId를 String으로 변환하여 추가

            String jsonData = new ObjectMapper().writeValueAsString(connectionData); // Map을 JSON 문자열로 변환

            Connection connection = activeSession.createConnection(new ConnectionProperties.Builder()
                    .role(OpenViduRole.SUBSCRIBER)
                    .data(jsonData)
                    .build());

            return connection.getToken();
        } catch (JsonProcessingException e) {
            throw new OntherockException(HttpStatus.BAD_REQUEST, "Failed to create JSON for connection data: " + e.getMessage());
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            throw new OntherockException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create connection: " + e.getMessage());
        }
    }

    /**
     * 현재 활성화된 퍼블리셔의 스트리밍 목록을 가져옵니다.
     *
     * @return ConcurrentHashMap<String, StreamingListResponse> 활성화된 퍼블리셔의 스트리밍 정보 (키: 사용자 ID, 값: 스트리밍 정보)
     */
    public ConcurrentHashMap<String, StreamingListResponse> getSessionsWithPublishers() {
        ConcurrentHashMap<String, StreamingListResponse> responseMap = new ConcurrentHashMap<>();

        try {
            openVidu.fetch();
            List<Session> activeSessions = openVidu.getActiveSessions();
            log.info("Active Session Size: {}", activeSessions.size());

            for (Session session : activeSessions) {
                for (Connection connection : session.getConnections()) {
                    if (connection.getRole() == OpenViduRole.PUBLISHER) {
                        String data = connection.getServerData();

                        try {
                            Map<String, String> connectionData = new ObjectMapper().readValue(data, new TypeReference<Map<String, String>>() {});
                            String userId = Optional.ofNullable(connectionData.get("userId"))
                                    .orElseThrow(()
                                            -> new OntherockException(HttpStatus.BAD_REQUEST, "User ID is missing"));

                            long timestamp = Long.parseLong(connectionData.get("createdAt"));
                            LocalDateTime createdAt = Instant.ofEpochMilli(timestamp)
                                    .atZone(ZoneId.systemDefault()).toLocalDateTime();

                            responseMap.putIfAbsent(userId, StreamingListResponse.builder()
                                    .userId(userId)
                                    .sessionId(session.getSessionId())
                                    .createdAt(createdAt)
                                    .build());

                        } catch (JsonProcessingException e) {
                            log.error("Error parsing connection data for connection {}: {}", connection.getConnectionId(), e.getMessage());
                            throw new OntherockException(HttpStatus.BAD_REQUEST, "Error parsing connection data for connection: " + connection.getConnectionId());
                        }
                    }
                }
            }
        } catch (OpenViduJavaClientException | OpenViduHttpException e) {
            log.error("Error fetching sessions: {}", e.getMessage());
            throw new OntherockException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }

        return responseMap;
    }
}
