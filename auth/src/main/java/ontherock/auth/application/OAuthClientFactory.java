package ontherock.auth.application;

import ontherock.auth.client.OAuthClient;
import ontherock.auth.domain.OauthType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuthClientFactory {
    private final Map<OauthType, OAuthClient> clients;

    public OAuthClientFactory(Set<OAuthClient> clientSet) {
        this.clients = clientSet.stream()
                .collect(Collectors.toMap(OAuthClient::getType, Function.identity()));
    }

    public OAuthClient getClient(OauthType type) {
        return clients.get(type);
    }
}

