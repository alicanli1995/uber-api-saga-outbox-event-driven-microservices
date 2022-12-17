package com.uber.api.keycloak.api.runner;

import com.uber.api.keycloak.api.config.KeycloakAdminInfoConfig;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakAdminConfig {

    private final KeycloakAdminInfoConfig keycloakAdminInfoConfig;
    @Bean
    public Keycloak keycloakAdmin() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakAdminInfoConfig.getUrl())
                .realm(keycloakAdminInfoConfig.getRealm())
                .username(keycloakAdminInfoConfig.getUsername())
                .password(keycloakAdminInfoConfig.getPassword())
                .clientId(keycloakAdminInfoConfig.getClientId())
                .build();
    }
}
