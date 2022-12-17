package com.uber.api.keycloak.api.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "keycloak-admin")
public class KeycloakAdminInfoConfig {
    private String realm;
    private String url;
    private String username;
    private String password;
    private String clientId;
}