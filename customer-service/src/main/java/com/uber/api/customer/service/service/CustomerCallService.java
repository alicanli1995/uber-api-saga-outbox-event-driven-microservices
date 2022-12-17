package com.uber.api.customer.service.service;

import com.uber.api.customer.service.dto.CallDriverCommand;
import com.uber.api.customer.service.dto.CallStatusDTO;
import com.uber.api.customer.service.dto.CustomerStatusDTO;

import javax.validation.Valid;

public interface CustomerCallService {
    CallStatusDTO callDriver(@Valid CallDriverCommand callDriverCommand);

    CustomerStatusDTO getCustomerStatus(String mail, String name, String ip);
}
