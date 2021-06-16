package com.upgrade.campsite.domains.booking;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookingCacheService {

    @Value("${cache.redis.key}")
    private String KEY;

    @Resource(name = "redisTemplate")
    private SetOperations<String, String> setOps;

    public Set<String> getAllFromCache() {
        try {
            return setOps.members(KEY);
        } catch (Exception ex) {
            log.error("Error when accessing cache.", ex);
        }

        return new HashSet<>();
    }

    public void addToCache(Set<LocalDate> bookingDates) {
        try {
            if (isNotEmpty(bookingDates)) {
                setOps.add(KEY, convertToArray(bookingDates));
            }
        } catch (Exception ex) {
            log.error("Error when adding to cache.", ex);
        }
    }

    public void addAllToCache(Set<String> bookingDates) {
        try {
            if (isNotEmpty(bookingDates)) {
                setOps.add(KEY, bookingDates.toArray(String[]::new));
            }
        } catch (Exception ex) {
            log.error("Error when adding all to cache.", ex);
        }
    }

    public void updateCache(Set<LocalDate> newBookingDates, Set<LocalDate> oldBookingDates) {
        removeFromCache(oldBookingDates);
        addToCache(newBookingDates);
    }

    public void removeFromCache(Set<LocalDate> bookingDates) {
        try {
            if (isNotEmpty(bookingDates)) {
                setOps.remove(KEY, (Object[]) convertToArray(bookingDates));
            }
        } catch (Exception ex) {
            log.error("Error when removing from cache.", ex);
        }
    }

    public void clearCache() {
        try {
            Set<String> cache = getAllFromCache();
            if (isNotEmpty(cache)) {
                setOps.remove(KEY, (Object[]) cache.toArray(String[]::new));
            }
        } catch (Exception ex) {
            log.error("Error when clearing cache.", ex);
        }
    }

    private String[] convertToArray(Set<LocalDate> bookingDates) {
        return bookingDates
                .stream()
                .map(d -> d.toString())
                .toArray(String[]::new);
    }

    private boolean isNotEmpty(Collection obj) {
        return !(obj == null || obj.isEmpty());
    }
}
