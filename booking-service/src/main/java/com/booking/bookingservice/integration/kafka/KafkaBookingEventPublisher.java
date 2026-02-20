package com.booking.bookingservice.integration.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "justlife.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaBookingEventPublisher implements BookingEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(KafkaBookingEventPublisher.class);

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final String topic;

  public KafkaBookingEventPublisher(
      KafkaTemplate<String, String> kafkaTemplate,
      ObjectMapper objectMapper,
      org.springframework.core.env.Environment env
  ) {
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = objectMapper;
    this.topic = env.getProperty("justlife.kafka.topics.booking-events", "booking.events");
  }

  @Override
  public void publish(BookingEventMessage event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(topic, event.bookingId().toString(), payload);
    } catch (JsonProcessingException e) {
      log.warn("Failed to serialize booking event, skipping publish. event={}", event, e);
    } catch (Exception ex) {
      log.warn("Failed to publish booking event to Kafka, skipping. event={}", event, ex);
    }
  }
}
