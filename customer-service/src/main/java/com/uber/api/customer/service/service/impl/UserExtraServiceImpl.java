package com.uber.api.customer.service.service.impl;

import com.uber.api.customer.service.entity.UserExtra;
import com.uber.api.customer.service.exception.UserExtraNotFoundException;
import com.uber.api.customer.service.repository.UserExtraRepository;
import com.uber.api.customer.service.service.UserExtraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserExtraServiceImpl implements UserExtraService {

    private final UserExtraRepository userExtraRepository;

    @Override
    public UserExtra validateAndGetUserExtra(String username) {
        return getUserExtra(username).orElseThrow(() -> new UserExtraNotFoundException(username));
    }

    @Override
    public Optional<UserExtra> getUserExtra(String username) {
        return userExtraRepository.findById(username);
    }

    @Override
    public UserExtra saveUserExtra(UserExtra userExtra) {
        return userExtraRepository.save(userExtra);
    }

}
