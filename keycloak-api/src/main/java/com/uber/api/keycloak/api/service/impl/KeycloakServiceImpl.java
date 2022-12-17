package com.uber.api.keycloak.api.service.impl;

import com.uber.api.keycloak.api.dto.UserAddRequestDTO;
import com.uber.api.keycloak.api.runner.KeycloakInitializerRunner;
import com.uber.api.keycloak.api.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private final KeycloakInitializerRunner keycloakInitializerRunner;

    @Override
    public Response addUser(UserAddRequestDTO userDTO) {
        return keycloakInitializerRunner.addUser(userDTO);
    }

    @Override
    public void assignRoleToUser(String replaceAll, String uberDriver) {
        keycloakInitializerRunner.assignRoleToUser(replaceAll, uberDriver);
    }

}
