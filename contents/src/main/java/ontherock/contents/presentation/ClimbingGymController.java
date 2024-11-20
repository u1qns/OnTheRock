package ontherock.contents.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import ontherock.contents.application.ClimbingGymService;
import ontherock.contents.dto.response.GymSearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/contents/gym")
public class ClimbingGymController {

    @Autowired
    private ClimbingGymService climbingGymService;

    @GetMapping("/search")
    @Operation(summary = "클라이밍장 조회", description = "주어진 키워드로 클라이밍장을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "클라이밍장 목록을 성공적으로 조회하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다. 키워드가 필요합니다."),
            @ApiResponse(responseCode = "204", description = "해당 키워드에 대한 클라이밍장이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생하였습니다.")
    })
    public ResponseEntity<List<GymSearchResponse>> searchGyms(@RequestParam("gym") String keyword) {
        List<GymSearchResponse> climbingGyms = climbingGymService.findGymsByKeyword(keyword);
        return ResponseEntity.ok(climbingGyms);
    }

    @GetMapping("/search/v2")
    @Operation(summary = "[테스트용]클라이밍장 조회", description = "주어진 키워드로 클라이밍장을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "클라이밍장 목록을 성공적으로 조회하였습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청입니다. 키워드가 필요합니다."),
            @ApiResponse(responseCode = "204", description = "해당 키워드에 대한 클라이밍장이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버 오류가 발생하였습니다.")
    })
    public ResponseEntity<List<GymSearchResponse>> searchGyms2(@RequestParam("gym") String keyword) {
        List<GymSearchResponse> climbingGyms = climbingGymService.searchGyms(keyword);
        return ResponseEntity.ok(climbingGyms);
    }

}
