package com.uber.api.keycloak.api.service;

import com.uber.api.keycloak.api.dto.UserAddRequestDTO;

import javax.ws.rs.core.Response;

public interface KeycloakService {
    Response addUser(UserAddRequestDTO user);

    void assignRoleToUser(String replaceAll, String uberDriver);

}
