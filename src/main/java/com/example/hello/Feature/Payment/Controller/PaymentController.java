package com.example.hello.Feature.Payment.Controller;

import com.example.hello.Feature.Payment.Service.VnPayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth/payment")
@RequiredArgsConstructor
public class PaymentController {

    VnPayService vnPayService;
    UUID id = UUID.fromString("22fe0072-d200-4ec4-a90a-d6e56d782f08");

    @GetMapping("/vnpay")
    public ResponseEntity<Void> pay() throws Exception {
        String url = vnPayService.createPaymentUrl(id, 100000);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }
}

