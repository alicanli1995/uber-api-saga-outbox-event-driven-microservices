package com.uber.api.customer.service.service;


import com.uber.api.customer.service.entity.UserExtra;

import java.util.Optional;

public interface UserExtraService {

    UserExtra validateAndGetUserExtra(String username);

    Optional<UserExtra> getUserExtra(String username);

    UserExtra saveUserExtra(UserExtra userExtra);


}
