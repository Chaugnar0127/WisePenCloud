package com.oriole.wisepen.system.service;

import com.oriole.wisepen.system.api.domain.dto.req.BusinessConfigApplyRequest;
import com.oriole.wisepen.system.api.domain.dto.res.BusinessConfigApplyResponse;
import com.oriole.wisepen.system.api.domain.dto.res.BusinessConfigResponse;
import com.oriole.wisepen.system.api.domain.dto.res.BusinessConfigSummaryResponse;

import java.util.List;

public interface BusinessConfigService {

    List<BusinessConfigSummaryResponse> listConfigs();

    BusinessConfigResponse getConfig(String code);

    BusinessConfigApplyResponse applyConfig(BusinessConfigApplyRequest request);
}
