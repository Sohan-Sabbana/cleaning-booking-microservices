package com.booking.bookingservice.model.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "booking_cleaner",
    uniqueConstraints = @UniqueConstraint(name = "uk_booking_cleaner", columnNames = {"booking_id", "cleaner_id"})
)
public class BookingCleanerEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "ID")
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "booking_id", nullable = false)
  private BookingEntity booking;

  @Column(name = "cleaner_id", nullable = false)
  private String cleanerId;

  protected BookingCleanerEntity() {}

  public BookingCleanerEntity(BookingEntity booking, String cleanerId) {
    this.booking = booking;
    this.cleanerId = cleanerId;
  }

  public String getId() { return id; }

  public BookingEntity getBooking() { return booking; }

  public String getBookingId() {
    return booking == null ? null : booking.getId();
  }

  public String getCleanerId() { return cleanerId; }
}
