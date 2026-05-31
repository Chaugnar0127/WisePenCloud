package com.oriole.wisepen.user.api.feign;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointTradeReverseRequest;
import com.oriole.wisepen.user.api.domain.dto.req.InfoPointTradeSettleRequest;
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

    @Operation(summary = "内部信息点交易结算（幂等）")
    @PostMapping("/internal/wallet/infopoint/settleTrade")
    R<Void> settleInfoPointTrade(@RequestBody InfoPointTradeSettleRequest req);

    @Operation(summary = "内部冲正信息点集市交易")
    @PostMapping("/internal/wallet/infopoint/reverseTrade")
    R<Void> reverseInfoPointTrade(@RequestBody InfoPointTradeReverseRequest req);

    @Operation(summary = "内部查询信息点余额")
    @GetMapping("/internal/wallet/infopoint/getBalance")
    R<Integer> getInfoPointBalance(@RequestParam("userId") Long userId);
}
