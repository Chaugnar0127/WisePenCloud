package com.oriole.wisepen.market.controller;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.market.service.IMarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final IMarketService marketService;

    @PostMapping("/infoPoint/deleteProduct")
    public R<Void> deleteProduct(@RequestParam Long productId){
        marketService.deleteProduct(productId);
        return R.ok();
    }
}
