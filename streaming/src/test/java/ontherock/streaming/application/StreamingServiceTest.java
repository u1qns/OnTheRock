package ontherock.streaming.application;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class StreamingServiceTest {

    @Autowired
    private StreamingService streamingService;

    @Test
    public void testStreamingService() {
        // streamingService가 잘 주입되었는지 확인
        assertNotNull(streamingService);
    }
}
