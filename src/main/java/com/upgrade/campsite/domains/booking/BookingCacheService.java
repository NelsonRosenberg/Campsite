package com.upgrade.campsite.domains.booking;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookingCacheService {

    @Value("${cache.redis.key}")
    private String KEY;

    @Resource(name = "redisTemplate")
    private SetOperations<String, LocalDate> setOps;

    public Set<LocalDate> getAllFromCache() {
        try {
            return setOps.members(KEY);
        } catch (Exception ex) {
            log.error("Error when accessing cache.", ex);
        }

        return new HashSet<>();
    }

    @Async
    public void addToCache(Set<LocalDate> bookingDates) {
        try {
            if (isNotEmpty(bookingDates)) {
                setOps.add(KEY, bookingDates.toArray(LocalDate[]::new));
            }
        } catch (Exception ex) {
            log.error("Error when adding to cache.", ex);
        }
    }

    @Async
    public void updateCache(Set<LocalDate> newBookingDates, Set<LocalDate> oldBookingDates) {
        removeFromCache(oldBookingDates);
        addToCache(newBookingDates);
    }

    @Async
    public void removeFromCache(Set<LocalDate> bookingDates) {
        try {
            if (isNotEmpty(bookingDates)) {
                setOps.remove(KEY, (Object[]) bookingDates.toArray(Object[]::new));
            }
        } catch (Exception ex) {
            log.error("Error when removing from cache.", ex);
        }
    }

    public void clearCache() {
        try {
            removeFromCache(getAllFromCache());
        } catch (Exception ex) {
            log.error("Error when clearing cache.", ex);
        }
    }

    private boolean isNotEmpty(Collection obj) {
        return !(obj == null || obj.isEmpty());
    }
}
