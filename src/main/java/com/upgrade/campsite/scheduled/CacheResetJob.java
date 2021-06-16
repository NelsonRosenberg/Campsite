package com.upgrade.campsite.scheduled;

import com.upgrade.campsite.domains.booking.BookingService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class CacheResetJob implements Job {

    @Autowired
    private BookingService bookingService;

    @Override
    public void execute(JobExecutionContext context) {
        log.info("Reseting cache.");
        bookingService.resetCache();
    }
}
