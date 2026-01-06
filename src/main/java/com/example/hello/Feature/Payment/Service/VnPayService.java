package com.example.hello.Feature.Payment.Service;

import com.example.hello.Enum.OrderStatus;
import com.example.hello.Feature.Payment.Model.PaymentResponse;
import com.example.hello.Feature.Payment.Model.VnPayProperties;
import com.example.hello.Infrastructure.Exception.ConflictException;
import com.example.hello.Infrastructure.Exception.EntityNotFoundException;
import com.example.hello.Middleware.Response;
import com.example.hello.Middleware.StringApplication;
import com.example.hello.Repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VnPayService {
    VnPayProperties vnPayProperties;
    OrderRepository orderRepository;

    @Transactional
    public Response<PaymentResponse> createPaymentUrl(UUID userId, UUID orderId, String ip){
        var order = orderRepository.findById(orderId).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.ORDER +
                        StringApplication.FIELD.NOT_EXIST)
        );
        if(!order.getUser().getUserId().equals(userId)){
            log.error("User not match with db");
            throw new ConflictException(StringApplication.FIELD.REQUEST +
                    StringApplication.FIELD.INVALID);
        }
        if(order.getOrderStatus() != OrderStatus.WAITING){
            log.error("Payment cant create because order status is {}", order.getOrderStatus());
            throw new ConflictException(StringApplication.FIELD.WAIT_AFTER +
                    StringApplication.FIELD.SOME +
                    StringApplication.FIELD.MINUTES);
        }
        var amount = order.getOrderItems().stream()
                .map(orderItem -> orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            log.error("Order amount is negative");
            throw new ConflictException(StringApplication.FIELD.INVALID);
        }
        long vnp_Amount = amount.multiply(new BigDecimal(100)).longValue();
        String vnp_TxnRef = String.valueOf(UUID.randomUUID());

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + orderId); // Theo mẫu: "Thanh toan don hang:" + vnp_TxnRef
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnPayProperties.getReturnUrl());
        vnp_Params.put("vnp_IpAddr", ip);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

        // 1. Sắp xếp tham số theo alphabet (Bắt buộc)
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        // 2. Build hashData và queryUrl đồng thời theo đúng file mẫu ajaxServlet
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // Build hash data (Lưu ý: Encode cả KEY và VALUE cho hashData)
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(VnPayUtils.vnpEncode(fieldValue));

                // Build query (Lưu ý: Encode cả KEY và VALUE cho query)
                query.append(VnPayUtils.vnpEncode(fieldName));
                query.append('=');
                query.append(VnPayUtils.vnpEncode(fieldValue));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        // 3. Tính toán SecureHash từ hashData đã build
        String vnp_SecureHash = VnPayUtils.hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());

        // 4. Nối SecureHash vào URL
        String paymentUrl = vnPayProperties.getPayUrl() + "?" + query + "&vnp_SecureHash=" + vnp_SecureHash;

        // 5. Lưu DB
        order.setPaymentId(vnp_TxnRef);
        order.setOrderStatus(OrderStatus.PAYING);
        log.info("Payment URL is {}", paymentUrl);
        return new Response<>(
                true,
                StringApplication.FIELD.SUCCESS,
                new PaymentResponse(paymentUrl)
        );
    }

    @Transactional
    public Boolean paymentReturn(HttpServletRequest request){
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach(
                (key, value) -> params.put(key, value[0])
        );
        String responseCode = params.get("vnp_ResponseCode");
        String vnp_TxnRef = params.get("vnp_TxnRef");
        var order = orderRepository.findByPaymentId(vnp_TxnRef).orElseThrow(
                ()-> new EntityNotFoundException(StringApplication.FIELD.ORDER +
                        StringApplication.FIELD.NOT_EXIST)
        );
        log.info("Response code is {}", responseCode);
        if ("00".equals(responseCode)) {
            order.setOrderStatus(OrderStatus.PENDING);
            order.setPaymentAt(Instant.now());
            return true;
        } else {
            order.setOrderStatus(OrderStatus.WAITING);
            return false;
        }
    }
}
