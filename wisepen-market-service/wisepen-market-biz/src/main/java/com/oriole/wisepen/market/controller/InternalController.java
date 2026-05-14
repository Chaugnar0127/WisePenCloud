package com.oriole.wisepen.market.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.market.api.domain.dto.req.InfoPointChangeRequest;
import com.oriole.wisepen.market.service.IInfoPointService;
import com.oriole.wisepen.market.service.IMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final IInfoPointService infoPointService;
    private final IMarketService marketService;

    @PostMapping("/infoPoint/changeBalance")
    public R<Void> changeBalance(@RequestBody InfoPointChangeRequest req){
        infoPointService.changeBalance(req);
        return R.ok();
    }

    @PostMapping("/infoPoint/deleteProduct")
    public R<Void> deleteProduct(@RequestParam Long productId){
        marketService.deleteProduct(productId);
        return R.ok();
    }
}
