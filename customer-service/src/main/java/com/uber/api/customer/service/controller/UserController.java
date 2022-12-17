package com.uber.api.customer.service.controller;

import com.uber.api.common.api.dto.UserExtraRequest;
import com.uber.api.customer.service.entity.UserExtra;
import com.uber.api.customer.service.service.UserExtraService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/userextras")
public class UserController {

    private final UserExtraService userExtraService;

    @GetMapping(path = "/me",produces = "application/json")
    public UserExtra getUserExtra(Principal principal) {
        return userExtraService.validateAndGetUserExtra(principal.getName());
    }

    @PostMapping(path = "/me",produces = "application/json")
    public UserExtra saveUserExtra(@Valid @RequestBody UserExtraRequest updateUserExtraRequest, Principal principal) {
        Optional<UserExtra> userExtraOptional = userExtraService.getUserExtra(principal.getName());
        UserExtra userExtra = userExtraOptional.orElseGet(() -> new UserExtra(principal.getName()));
        userExtra.setAvatar(updateUserExtraRequest.getAvatar());
        return userExtraService.saveUserExtra(userExtra);
    }

}
