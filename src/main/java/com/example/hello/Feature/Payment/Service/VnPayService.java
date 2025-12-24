package com.example.hello.Feature.Payment.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.example.hello.Feature.Payment.Service.VnPayUtils.buildQuery;
import static com.example.hello.Feature.Payment.Service.VnPayUtils.hmacSHA512;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VnPayService {
    @Value("${vnpay.tmnCode}")
    String tmnCode;

    @Value("${vnpay.hashSecret}")
    String hashSecret;

    @Value("${vnpay.payUrl}")
    String payUrl;

    @Value("${vnpay.returnUrl}")
    String returnUrl;

    public String createPaymentUrl(UUID orderId, long amount) throws Exception {

        Map<String, String> params = new HashMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay x100
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", String.valueOf(orderId));
        params.put("vnp_OrderInfo", "Thanh toan don hang" + orderId);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate",
                new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        String query = buildQuery(params);
        String secureHash = hmacSHA512(hashSecret, query);

        return payUrl + "?" + query + "&vnp_SecureHash=" + secureHash;
    }
}
