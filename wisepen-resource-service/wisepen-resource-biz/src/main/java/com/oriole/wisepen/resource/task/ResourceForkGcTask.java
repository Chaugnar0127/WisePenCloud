package com.oriole.wisepen.resource.task;

import com.oriole.wisepen.resource.service.IResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceForkGcTask {

    private final IResourceService resourceService;

    @Scheduled(cron = "${wisepen.resource.fork-timeout-gc-cron:0 */5 * * * ?}")
    public void markTimedOutForks() {
        log.debug("resourceForkTimeoutGc started");
        resourceService.markForkTimedOut();
        log.debug("resourceForkTimeoutGc finished");
    }
}
