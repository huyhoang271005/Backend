package com.example.hello.Feature.Payment.Controller;

import com.example.hello.Feature.Payment.Service.VnPayService;
import com.example.hello.Middleware.ParamName;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
    public ResponseEntity<?> pay(HttpServletRequest request,
                                        @AuthenticationPrincipal UUID userId, @PathVariable UUID orderId) {
        String ip = (String) request.getAttribute(ParamName.IP_ADDRESS_ATTRIBUTE);
        return ResponseEntity.ok().body(vnPayService.createPaymentUrl(userId, orderId, ip));
    }
}

