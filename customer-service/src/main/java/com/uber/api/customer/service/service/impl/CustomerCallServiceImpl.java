package com.uber.api.customer.service.service.impl;

import com.uber.api.customer.service.dto.CallDriverCommand;
import com.uber.api.customer.service.dto.CallStatusDTO;
import com.uber.api.customer.service.dto.CustomerStatusDTO;
import com.uber.api.customer.service.service.CustomerCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Slf4j
@Component
@Validated
@RequiredArgsConstructor
public class CustomerCallServiceImpl implements CustomerCallService {

    private final CustomerCallCommandHandler customerCallCommandHandler;
    private final CustomerCallQueryHandler customerCallQueryHandler;

    @Override
    public CallStatusDTO callDriver(CallDriverCommand callDriverCommand) {
        try {
            return customerCallCommandHandler.callDriver(callDriverCommand);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CustomerStatusDTO getCustomerStatus(String mail, String name, String ip) {
        return customerCallQueryHandler.getCustomerStatus(mail,name,ip);
    }

}
