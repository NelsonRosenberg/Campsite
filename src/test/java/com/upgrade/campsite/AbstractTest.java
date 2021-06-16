package com.upgrade.campsite;

import com.upgrade.campsite.domains.booking.BookingRepository;
import com.upgrade.campsite.domains.booking.BookingService;
import com.upgrade.campsite.services.CachingService;
import javax.transaction.Transactional;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class})
public abstract class AbstractTest {

    @Autowired
    public BookingService bookingService;

    @Autowired
    public CachingService cachingService;

    @Autowired
    public BookingRepository bookingRepository;

    @Transactional
    public void deleteAll() {
        bookingRepository.deleteAll();
        cachingService.clearCache();
    }

}
