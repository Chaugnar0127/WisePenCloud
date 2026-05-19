package com.oriole.wisepen.user.api.feign;

import com.oriole.wisepen.common.core.domain.PageResult;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.user.api.domain.dto.req.CurrencyExchangeRequest;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointChangeRequest;
import com.oriole.wisepen.user.api.domain.dto.res.InfoPointTransactionRecordResponse;
import com.oriole.wisepen.user.api.enums.InfoPointChangeType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "内部钱包服务", description = "提供给其他微服务的钱包接口")
@FeignClient(contextId = "remoteWalletService", value = "wisepen-user-service")
public interface RemoteWalletService {

    @Operation(summary = "内部信息点变更")
    @PostMapping("/internal/wallet/infopoint/changeBalance")
    R<Void> changeInfoPointBalance(@RequestBody InfoPointChangeRequest req);

    @Operation(summary = "内部信息点交易结算")
    @PostMapping("/internal/wallet/infopoint/settleTrade")
    R<Void> settleInfoPointTrade(
            @RequestParam("buyerId") Long buyerId,
            @RequestParam("sellerId") Long sellerId,
            @RequestParam("price") Integer price,
            @RequestParam("relatedId") Long relatedId
    );

    @Operation(summary = "内部换汇")
    @PostMapping("/internal/wallet/infopoint/exchange")
    R<Void> exchangeCurrency(@RequestBody CurrencyExchangeRequest req);

    @Operation(summary = "内部查询信息点余额")
    @GetMapping("/internal/wallet/infopoint/balance")
    R<Integer> getInfoPointBalance(@RequestParam("userId") Long userId);

    @Operation(summary = "内部查询信息点流水")
    @GetMapping("/internal/wallet/infopoint/records")
    R<PageResult<InfoPointTransactionRecordResponse>> getInfoPointRecords(
            @RequestParam("userId") Long userId,
            @RequestParam(value = "changeType", required = false) InfoPointChangeType changeType,
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size
    );
}
