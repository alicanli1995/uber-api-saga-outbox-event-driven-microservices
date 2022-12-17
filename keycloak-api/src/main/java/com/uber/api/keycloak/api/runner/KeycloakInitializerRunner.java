package com.uber.api.keycloak.api.runner;

import com.uber.api.common.api.dto.UserDTO;
import com.uber.api.keycloak.api.dto.UserAddRequestDTO;
import com.uber.api.keycloak.api.security.Credentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakInitializerRunner implements CommandLineRunner {

    private final Keycloak keycloakAdmin;
    private static final String KEYCLOAK_SERVER_URL = "http://localhost:8080/";
    private static final String COMPANY_SERVICE_REALM_NAME = "uber-services";
    private static final String UBER_APP_CLIENT_ID = "uber-app";
    private static final String UBER_APP_REDIRECT_URL = "http://localhost:3000/*";
    private static final List<UserPass> UBER_APP_USERS = List.of(
            new UserPass("user", "user"),
            new UserPass("admin", "password"));

    @Override
    public void run(String... args) {
        log.info("Initializing '{}' realm in Keycloak ...", COMPANY_SERVICE_REALM_NAME);

        Optional<RealmRepresentation> representationOptional = keycloakAdmin.realms()
                .findAll()
                .stream()
                .filter(r -> r.getRealm().equals(COMPANY_SERVICE_REALM_NAME))
                .findAny();
        if (representationOptional.isPresent()) {
            log.info("Realm '{}' already exists, skipping initialization", COMPANY_SERVICE_REALM_NAME);
            return;
        }

        // Realm
        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(COMPANY_SERVICE_REALM_NAME);
        realmRepresentation.setEnabled(true);
        realmRepresentation.setRegistrationAllowed(true);

        // Client
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(UBER_APP_CLIENT_ID);
        clientRepresentation.setDirectAccessGrantsEnabled(true);
        clientRepresentation.setPublicClient(true);
        clientRepresentation.setRedirectUris(Collections.singletonList(UBER_APP_REDIRECT_URL));
        clientRepresentation.setDefaultRoles(new String[]{"CUSTOMER"});
        realmRepresentation.setClients(Collections.singletonList(clientRepresentation));

        // Users
        List<UserRepresentation> userRepresentations = UBER_APP_USERS.stream()
                .map(userPass -> {
                    // User Credentials
                    CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
                    credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
                    credentialRepresentation.setValue(userPass.password());

                    // User
                    UserRepresentation userRepresentation = new UserRepresentation();
                    userRepresentation.setUsername(userPass.username());
                    userRepresentation.setEnabled(true);
                    userRepresentation.setCredentials(Collections.singletonList(credentialRepresentation));
                    userRepresentation.setClientRoles(getClientRoles(userPass));

                    return userRepresentation;
                }).toList();
        realmRepresentation.setUsers(userRepresentations);

        // Create Realm
        keycloakAdmin.realms().create(realmRepresentation);

        // Testing
        UserPass admin = UBER_APP_USERS.get(0);
        log.info("Testing getting token for '{}' ...", admin.username());

        Keycloak keycloak = KeycloakBuilder.builder().serverUrl(KEYCLOAK_SERVER_URL)
                .realm(COMPANY_SERVICE_REALM_NAME).username(admin.username()).password(admin.password())
                .clientId(UBER_APP_CLIENT_ID).build();

        log.info("'{}' token: {}", admin.username(), keycloak.tokenManager().grantToken().getToken());
        log.info("'{}' initialization completed successfully!", COMPANY_SERVICE_REALM_NAME);
    }

    private Map<String, List<String>> getClientRoles(UserPass userPass) {
        List<String> roles = new ArrayList<>();
        roles.add("CUSTOMER");
        if ("admin".equals(userPass.username())) {
            roles.add("DRIVER");
        }
        return Map.of(UBER_APP_CLIENT_ID, roles);
    }


    public void assignRoleToUser(String userId, String role) {
        Keycloak keycloak = keycloakAdmin;
        UsersResource usersResource = keycloak.realm(COMPANY_SERVICE_REALM_NAME).users();
        UserResource userResource = usersResource.get(userId);
        ClientRepresentation clientRepresentation = keycloak.realm(COMPANY_SERVICE_REALM_NAME)
                .clients()
                .findAll()
                .stream()
                .filter(client -> client.getClientId().equals(UBER_APP_CLIENT_ID))
                .toList()
                .get(0);
        ClientResource clientResource = keycloak.realm(COMPANY_SERVICE_REALM_NAME).clients().get(clientRepresentation.getId());
        RoleRepresentation roleRepresentation = clientResource.roles().list().stream()
                .filter(element -> element.getName().equals(role)).toList()
                .get(0);
        userResource.roles().clientLevel(clientRepresentation.getId()).add(Collections.singletonList(roleRepresentation));
    }

    public Response addUser(UserAddRequestDTO userDTO){
        try{
            CredentialRepresentation credential = Credentials
                    .createPasswordCredentials(userDTO.password());
            UserRepresentation user = new UserRepresentation();
            user.setUsername(userDTO.username());
            user.setFirstName(userDTO.firstName());
            user.setLastName(userDTO.lastName());
            user.setEmail(userDTO.email());
            user.setCredentials(Collections.singletonList(credential));
            user.setEnabled(true);
            UsersResource instance = getInstance();
            return instance.create(user);
        }
        catch (Exception e){
            log.error("Error while adding user",e);
            return null;
        }

    }

    public UsersResource getInstance(){
        return keycloakAdmin.realm(COMPANY_SERVICE_REALM_NAME).users();
    }

    private record UserPass(String username, String password) {
    }

}