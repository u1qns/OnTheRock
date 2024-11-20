package ontherock.contents.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByPostId(Long postId);
    long countByPostId(Long postId);
    Optional<Report> findByPostIdAndReporterId(Long postId, Long reporterId);
}