package ontherock.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowerRepository extends JpaRepository<Follower, Long> {
    List<Follower> findByUser(User user);
    List<Follower> findByFollower(User follower);
    boolean existsByUserAndFollower(User user, User follower);
    void deleteByUserAndFollower(User user, User follower);
}
