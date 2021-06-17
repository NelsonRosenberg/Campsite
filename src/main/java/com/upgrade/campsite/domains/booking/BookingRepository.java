package com.upgrade.campsite.domains.booking;

import java.time.LocalDate;
import java.util.Set;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(value = "SELECT dates "
            + " FROM Booking b "
            + " JOIN b.date as dates"
            + " WHERE dates >= :startDate "
            + " AND dates <= :endDate ")
    @Transactional(readOnly = true)
    public Set<LocalDate> findScheduledDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @EntityGraph(value = "booking.default")
    @Transactional(readOnly = true)
    public Booking findByBookingId(String bookingId);

}
