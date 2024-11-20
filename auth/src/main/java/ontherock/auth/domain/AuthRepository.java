package ontherock.auth.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Auth, String> {
    Optional<Auth> findByAuthIdAndOauthType(String authId, OauthType oauthType);
}
