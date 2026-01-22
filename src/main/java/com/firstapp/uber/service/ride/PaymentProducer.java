package com.firstapp.uber.service.ride;


import com.firstapp.uber.dto.ride.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import model.PaymentStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate;

    private static final String TOPIC = "payment-events";

    public void publishPaymentSuccess(Integer rideId, String method) {
        PaymentSuccessEvent event = new PaymentSuccessEvent(
                rideId,
                method,
                PaymentStatus.COMPLETED,
                System.currentTimeMillis()
        );

        kafkaTemplate.send(TOPIC, rideId.toString(), event);
    }
}

