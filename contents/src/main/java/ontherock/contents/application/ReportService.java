package ontherock.contents.application;

import ontherock.contents.common.OntherockException;
import ontherock.contents.domain.Post;
import ontherock.contents.domain.PostRepository;
import ontherock.contents.domain.Report;
import ontherock.contents.domain.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PostRepository postRepository;

    @Value("${app.report.threshold}")
    private int reportThreshold;  // 신고 임계값 설정

    public void reportPost(Long postId, Long reporterId) {
        // 중복 신고 방지: 동일 사용자가 동일 게시글을 여러 번 신고하지 못하도록 체크
        if (reportRepository.findByPostIdAndReporterId(postId, reporterId).isPresent()) {
            throw new OntherockException(HttpStatus.BAD_REQUEST, "You have already reported this post.");
        }

        Report report = new Report();
        report.setPostId(postId);
        report.setReporterId(reporterId);
        reportRepository.save(report);

        // 신고 횟수 체크
        long reportCount = reportRepository.countByPostId(postId);
        if (reportCount >= reportThreshold) {
            handleReportedPost(postId);
        }
    }

    private void handleReportedPost(Long postId) {
        Optional<Post> postOptional = postRepository.findById(postId);
        if (postOptional.isPresent()) {
            Post post = postOptional.get();
            post.setVisibility(Post.Visibility.PRIVATE);  // 게시글 비공개 처리
            postRepository.save(post);
        }
    }
}