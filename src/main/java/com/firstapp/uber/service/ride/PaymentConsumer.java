package com.firstapp.uber.service.ride;

import com.firstapp.uber.dto.ride.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "payment-events", groupId = "payment-group")
    public void consume(PaymentSuccessEvent event) {
        paymentService.handlePaymentSuccess(
                event.rideId(),
                event.method()
        );
    }
}

