package com.oriole.wisepen.market.controller;

import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.market.api.domain.dto.req.CurrencyExchangeRequest;
import com.oriole.wisepen.market.api.domain.dto.res.InfoPointTransactionRecordResponse;
import com.oriole.wisepen.market.api.enums.InfoPointChangeType;
import com.oriole.wisepen.market.service.IInfoPointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
@Validated
public class InfoPointController {

    private final IInfoPointService infoPointService;

    @PostMapping("/wallet/exchangeCurrency")
    public R<Void> exchangeCurrency(@RequestParam @Valid CurrencyExchangeRequest dto) {
        infoPointService.exchangeCurrency(dto);
        return R.ok();
    }

    @GetMapping("/wallet/getBalance")
    public R<Integer> getBalance(@RequestParam @Valid Long userId) {
        return R.ok(infoPointService.getBalance(userId));
    }

    @GetMapping("/wallet/listRecords")
    public R<PageResult<InfoPointTransactionRecordResponse>> getRecordlist(Long userId, InfoPointChangeType changeType, Integer page, Integer size) {
        return R.ok(infoPointService.getRecordlist(userId, changeType, page, size));
    }

}
