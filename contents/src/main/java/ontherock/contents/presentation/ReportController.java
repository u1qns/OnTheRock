package ontherock.contents.presentation;

import ontherock.contents.application.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/contents")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/report")
    public ResponseEntity<Void> reportPost(
            @RequestParam Long postId,
            @RequestParam Long reporterId
    ) {
        reportService.reportPost(postId, reporterId);
        return ResponseEntity.ok().build();
    }
}