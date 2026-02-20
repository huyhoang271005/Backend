package com.example.hello.Feature.Payment.Controller;

import com.example.hello.Feature.Payment.Service.VnPayService;
import com.example.hello.Infrastructure.Security.AppProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("payment-return")
public class PaymentReturn {
    VnPayService vnPayService;
    AppProperties appProperties;
    @GetMapping("vn-pay")
    public ResponseEntity<?> handleReturn(HttpServletRequest request) {
        var payment = vnPayService.paymentReturn(request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION,
                        appProperties.getFrontendUrl() + "/payment?success=" + payment)
                .build();
    }
}
