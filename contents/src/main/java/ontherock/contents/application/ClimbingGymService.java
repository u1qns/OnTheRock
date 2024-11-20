package ontherock.contents.application;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import ontherock.contents.domain.ClimbingGym;
import ontherock.contents.domain.ClimbingGymRepository;
import ontherock.contents.dto.response.GymSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ClimbingGymService {

    private final RedisTemplate<String, LocalDateTime> redisTemplate;
    private final ClimbingGymRepository climbingGymRepository;

    @Autowired
    public ClimbingGymService(RedisTemplate<String, LocalDateTime> redisTemplate,
                              ClimbingGymRepository climbingGymRepository) {
        this.redisTemplate = redisTemplate;
        this.climbingGymRepository = climbingGymRepository;
    }

    @PostConstruct
    public void init() {
        String csvFile = "crawler/climbing_gyms.csv"; // CSV 파일 경로
        log.info("CSV 파일 로딩 시작: {}", csvFile);
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 3) {
                    log.warn("유효하지 않은 CSV 행: {}", Arrays.toString(line));
                    continue;
                }

                try {
                    String placeIdString = line[0].replaceAll("[^\\d]", "").trim(); // 숫자만 남기기

                    ClimbingGym gym = new ClimbingGym();
                    gym.setPlaceId(Long.valueOf(placeIdString));
                    gym.setName(line[1].trim());
                    gym.setAddress(line[2].trim());

                    climbingGymRepository.save(gym);

                } catch (NumberFormatException e) {
                    log.error("Place ID 변환 오류: {}", line[0], e);
                }
            }
        } catch (IOException | CsvValidationException e) {
            log.error("CSV 파일 로딩 중 오류 발생: {}", e.getMessage(), e);
        }

        log.info("CSV 파일 로딩 완료.");
    }

    /**
     * 주어진 키워드로 클라이밍장을 DB에서 검색합니다.
     *
     * @param keyword 검색할 키워드
     * @return 클라이밍장 리스트
     */
    public List<GymSearchResponse> findGymsByKeyword(String keyword) {
        List<ClimbingGym> gyms = climbingGymRepository.findByNameContainingOrAddressContaining(keyword, keyword);
        if (gyms.isEmpty()) {
            log.warn("검색 결과가 없습니다: keyword={}", keyword);
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "해당 키워드에 대한 클라이밍장이 없습니다.");
        }

        log.info("검색 결과 반환: {}개의 클라이밍장", gyms.size());
        return gyms.stream()
                .map(gym -> GymSearchResponse.builder()
                        .placeId(gym.getPlaceId())
                        .name(gym.getName())
                        .address(gym.getAddress())
                        .build())
                .toList();
    }

    /**
     * 클라이밍장을 검색합니다.
     *
     * @param keyword 검색할 키워드
     * @return 검색 결과 리스트
     * @throws ResponseStatusException BAD_REQUEST(400) - 키워드가 null 또는 빈 문자열인 경우
     *                                  NO_CONTENT(204) - 검색 결과가 없을 경우
     */
    public List<GymSearchResponse> searchGyms(String keyword) {
        log.info("searchGyms 호출: keyword={}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("검색 키워드가 null 또는 빈 문자열입니다.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "키워드가 필요합니다.");
        }

        // 업데이트 필요 여부 확인
        if (isUpdateRequired(keyword)) {
            executePythonScript(keyword); // 스크립트 실행, 실패 시 예외 발생
            redisTemplate.opsForValue().set(keyword, LocalDateTime.now());
            log.info("Redis에 updated_at 값 저장: keyword={}", keyword);
        }

        return findGymsByKeyword(keyword);
    }

    /**
     * Python 스크립트를 실행하여 클라이밍장 정보를 업데이트합니다.
     *
     * @param keyword 검색할 키워드
     * @throws ResponseStatusException INTERNAL_SERVER_ERROR(500) - 스크립트 실행 중 오류 발생 시
     */
    public void executePythonScript(String keyword) {
        log.info("executePythonScript 호출: keyword={}", keyword);
        String scriptPath = "crawler/crawl_script.py"; // 컨텐츠 루트 기준
        ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath, keyword);
        processBuilder.redirectErrorStream(true);

        try {
            log.info("Python 스크립트 실행: {}", processBuilder.command());
            Process process = processBuilder.start();

            // 출력 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            log.info("Python 스크립트 출력:");
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }

            int exitCode = process.waitFor();
            log.info("Python 스크립트 종료 코드: {}", exitCode);
            if (exitCode != 0) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "스크립트 실행 중 오류가 발생했습니다.");
            }
        } catch (Exception e) {
            log.error("스크립트 실행 중 오류 발생: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "스크립트 실행 중 오류가 발생했습니다.");
        }
    }

    /**
     * 주어진 키워드에 대해 업데이트가 필요한지 확인합니다.
     *
     * @param keyword 검색할 키워드
     * @return true - 업데이트가 필요할 경우, false - 필요하지 않을 경우
     */
    private boolean isUpdateRequired(String keyword) {
        LocalDateTime existingUpdatedAt = redisTemplate.opsForValue().get(keyword);
        if (existingUpdatedAt == null) {
            log.info("기존 업데이트 시간이 없습니다. 업데이트 필요: keyword={}", keyword);
            return true;
        }

        long daysSinceUpdate = ChronoUnit.DAYS.between(existingUpdatedAt, LocalDateTime.now());
        if (daysSinceUpdate >= 7) {
            log.info("업데이트가 필요합니다. 마지막 업데이트: {}일 전, keyword={}", daysSinceUpdate, keyword);
            return true; // 업데이트 필요
        } else {
            log.info("업데이트 필요하지 않음. 마지막 업데이트: {}일 전, keyword={}", daysSinceUpdate, keyword);
            return false; // 업데이트 필요 없음
        }
    }
}
