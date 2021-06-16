package com.upgrade.campsite.domains.booking;

import java.time.LocalDate;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(value = "SELECT date "
            + " FROM booking_date "
            + " WHERE booking_date.date >= :startDate "
            + " OR booking_date.date <= :endDate ", nativeQuery = true)
    public Set<String> findScheduledDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @EntityGraph(value = "booking.default")
    public Booking findByBookingId(String bookingId);

}
