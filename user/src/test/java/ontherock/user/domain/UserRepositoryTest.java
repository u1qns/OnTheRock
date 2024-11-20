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
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void saveUser_success() {
        User user = User.builder()
                .authId("naver-12345")
                .nickname("조승기")
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getAuthId()).isEqualTo(user.getAuthId());
        assertThat(savedUser.getNickname()).isEqualTo(user.getNickname());

        // TSID 검증
        long timestamp = (savedUser.getUserId() >> 20);
        assertThat(timestamp).isGreaterThan(0);
    }

    @Test
    void findUserById_success() {
        User user = User.builder()
                .authId("naver-12345")
                .nickname("조승기")
                .build();
        User savedUser = userRepository.save(user);

        Optional<User> foundUser = userRepository.findById(savedUser.getUserId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUserId()).isEqualTo(savedUser.getUserId());
        assertThat(foundUser.get().getAuthId()).isEqualTo(savedUser.getAuthId());
        assertThat(foundUser.get().getNickname()).isEqualTo(savedUser.getNickname());
    }

    @Test
    void deleteUser_success() {
        User user = User.builder()
                .authId("naver-12345")
                .nickname("조승기")
                .build();
        User savedUser = userRepository.save(user);

        userRepository.delete(savedUser);
        Optional<User> deletedUser = userRepository.findById(savedUser.getUserId());

        assertThat(deletedUser).isNotPresent();
    }

    @Test
    void findByNicknameContaining_success() {
        User user1 = User.builder().authId("naver-12345").nickname("조승기").build();
        User user2 = User.builder().authId("naver-45678").nickname("조용기").build();

        userRepository.save(user1);
        userRepository.save(user2);

        List<User> searchedUsers = userRepository.findByNicknameContaining("조");
        assertThat(searchedUsers.size()).isEqualTo(2);
        assertThat(searchedUsers).extracting(User::getNickname).containsExactlyInAnyOrder("조승기", "조용기");
    }
}
