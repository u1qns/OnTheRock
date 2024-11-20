package ontherock.contents.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClimbingGymRepository extends JpaRepository<ClimbingGym, Long> {
    List<ClimbingGym> findByNameContainingOrAddressContaining(String keyword, String keyword2);
}

