package com.example.hello.Feature.Payment.Controller;

import com.example.hello.Feature.Payment.Service.VnPayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("payment")
@RequiredArgsConstructor
public class PaymentController {

    VnPayService vnPayService;

    @GetMapping("/vn-pay/{orderId}")
    public ResponseEntity<Void> pay(@AuthenticationPrincipal UUID userId, @PathVariable UUID orderId) {
        String url = vnPayService.createPaymentUrl(userId, orderId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url)
                .build();
    }
}

