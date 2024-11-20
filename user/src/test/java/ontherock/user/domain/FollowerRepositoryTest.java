package ontherock.user.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FollowerRepositoryTest {

    @Autowired
    private FollowerRepository followerRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private User follower;

    @BeforeEach
    void setUp() {
        followerRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder().authId("user-12345").nickname("User").build());
        follower = userRepository.save(User.builder().authId("follower-12345").nickname("Follower").build());
    }

    @Test
    void saveFollower_success() {
        Follower follow = Follower.builder()
                .user(user)
                .follower(follower)
                .build();

        Follower savedFollow = followerRepository.save(follow);

        assertThat(savedFollow).isNotNull();
        assertThat(savedFollow.getFollowId()).isNotNull();
        assertThat(savedFollow.getUser().getUserId()).isEqualTo(user.getUserId());
        assertThat(savedFollow.getFollower().getUserId()).isEqualTo(follower.getUserId());
    }

    @Test
    void findByUser_success() {
        Follower follow = Follower.builder()
                .user(user)
                .follower(follower)
                .build();
        followerRepository.save(follow);

        List<Follower> followers = followerRepository.findByUser(user);

        assertThat(followers).isNotEmpty();
        assertThat(followers.get(0).getUser().getUserId()).isEqualTo(user.getUserId());
        assertThat(followers.get(0).getFollower().getUserId()).isEqualTo(follower.getUserId());
    }

    @Test
    void deleteFollower_success() {
        Follower follow = Follower.builder()
                .user(user)
                .follower(follower)
                .build();
        Follower savedFollow = followerRepository.save(follow);

        followerRepository.delete(savedFollow);
        Optional<Follower> deletedFollow = followerRepository.findById(savedFollow.getFollowId());

        assertThat(deletedFollow).isNotPresent();
    }
}