package com.upgrade.campsite;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppEvents {

    @EventListener(ApplicationReadyEvent.class)
    public void startSuccess() {
        log.info("Started Application");
    }

}
