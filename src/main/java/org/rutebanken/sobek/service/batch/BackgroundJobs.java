package org.rutebanken.sobek.service.batch;

import jakarta.annotation.PostConstruct;
import org.rutebanken.sobek.netex.id.GaplessIdGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Jobs that run periodically in the background
 */
@Service
public class BackgroundJobs {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundJobs.class);

    private static final AtomicLong threadNumber = new AtomicLong();

    private final ScheduledExecutorService backgroundJobExecutor =
            Executors.newScheduledThreadPool(3, (runnable) -> new Thread(runnable, "background-job-"+threadNumber.incrementAndGet()));

    private final GaplessIdGeneratorService gaplessIdGeneratorService;

    public BackgroundJobs(GaplessIdGeneratorService gaplessIdGeneratorService) {
        this.gaplessIdGeneratorService = gaplessIdGeneratorService;
    }

    @PostConstruct
    public void scheduleBackgroundJobs() {
        logger.info("Scheduling background job for gaplessIdGeneratorService");
        backgroundJobExecutor.scheduleAtFixedRate(gaplessIdGeneratorService::persistClaimedIds, 15, 15, TimeUnit.SECONDS);
    }
}
