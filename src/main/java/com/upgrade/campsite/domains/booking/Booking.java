package com.upgrade.campsite.domains.booking;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedEntityGraph;
import javax.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
        name = "booking.default",
        includeAllAttributes = true)
public class Booking {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BookingSeq")
    @SequenceGenerator(name = "BookingSeq", sequenceName = "booking_seq")
    private Long id;

    @Column
    private String email;

    @Column
    private String name;

    @Column
    private String bookingId;

    @Column(unique = true)
    @Builder.Default
    @ElementCollection(fetch = FetchType.LAZY)
    private Set<LocalDate> date = new HashSet<>();

}
