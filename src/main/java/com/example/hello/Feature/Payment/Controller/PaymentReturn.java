package com.example.hello.Feature.Payment.Controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth/payment-return")
public class PaymentReturn {

    @GetMapping("vnpay")
    public String handleReturn(HttpServletRequest request) {

        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach(
                (key, value) -> params.put(key, value[0])
        );

        String responseCode = params.get("vnp_ResponseCode");

        if ("00".equals(responseCode)) {
            return "Thanh toán thành công";
        } else {
            return "Thanh toán thất bại";
        }
    }
}
