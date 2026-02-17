package com.example.hello.Infrastructure.Security;

import com.example.hello.Enum.BrowserName;
import com.example.hello.Enum.DeviceName;
import com.example.hello.Enum.DeviceType;
import com.example.hello.Feature.User.dto.Address;
import com.example.hello.Middleware.ParamName;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeviceTypeFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        //Device type
        DeviceType deviceType = getDeviceType(request);
        request.setAttribute(ParamName.DEVICE_TYPE_ATTRIBUTE, deviceType);
        log.info("deviceType is {}", deviceType);
        //Device name
        request.setAttribute(ParamName.DEVICE_NAME_ATTRIBUTE, getDeviceName(request, deviceType));
        //Ip address
        request.setAttribute(ParamName.IP_ADDRESS_ATTRIBUTE, getIpAddress(request));
        //Address
        request.setAttribute(ParamName.ADDRESS_ATTRIBUTE, getAddress(request));

        filterChain.doFilter(request, response);
    }

    private Address getAddress(HttpServletRequest request) {
        return Address.builder()
                .city(decodeCloudflareHeader(request.getHeader("CF-IPCity")))
                .country(decodeCloudflareHeader(request.getHeader("CF-IPCountry")))
                .region(decodeCloudflareHeader(request.getHeader("CF-Region")))
                .timezone(decodeCloudflareHeader(request.getHeader("CF-Timezone")))
                .build();
    }

    private String decodeCloudflareHeader(String value){
        if(value==null){
            return null;
        }

        return new String(value.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
    }

    private String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("CF-CONNECTING-IP");
            if (ipAddress == null || ipAddress.isEmpty()) {
                log.info("IP not pass cloudflared");
                ipAddress = request.getRemoteAddr();
            }
        } catch (Exception e) {
            ipAddress = "unknow";
        }
        log.info("IP address: {}", ipAddress);
        return ipAddress;
    }


    private DeviceType getDeviceType(HttpServletRequest request) {
        DeviceType deviceType;
        if("22fe0072-d200-4ec4-a90a-d6e56d782f08".equals(request.getHeader("X-client-x"))){
            deviceType = DeviceType.SWING;
        }
        else{
            deviceType = DeviceType.WEB;
        }
        return deviceType;
    }

    private String getDeviceName(HttpServletRequest request, DeviceType deviceType) {
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        if (userAgent == null) {
            userAgent = "";
        }
        userAgent = userAgent.toLowerCase();

        BrowserName browser = BrowserName.Other;
        DeviceName device = DeviceName.Other;

        if (deviceType == DeviceType.WEB) {

            // --- Detect Browser ---
            if ((userAgent.contains("chrome") || userAgent.contains("crios"))
                    && !userAgent.contains("edge") && !userAgent.contains("opr")) {
                browser = BrowserName.Chrome;

            } else if (userAgent.contains("firefox")) {
                browser = BrowserName.Firefox;

            } else if (userAgent.contains("safari") && !userAgent.contains("chrome") && !userAgent.contains("crios")) {
                browser = BrowserName.Safari;

            } else if (userAgent.contains("edge")) {
                browser = BrowserName.Edge;

            } else if (userAgent.contains("opr") || userAgent.contains("opera")) {
                browser = BrowserName.Opera;
            }

            // --- Detect Device ---
            if (userAgent.contains("android")) {
                device = DeviceName.Android;

            } else if (userAgent.contains("iphone")) {
                device = DeviceName.iPhone;

            } else if (userAgent.contains("ipad")) {
                device = DeviceName.iPad;

            } else if (userAgent.contains("windows")) {
                device = DeviceName.Windows;

            } else if (userAgent.contains("macintosh")) {
                device = DeviceName.Macintosh;

            } else if (userAgent.contains("linux")) {
                device = DeviceName.Linux;
            }
        }

        else if(deviceType == DeviceType.SWING){
            return "SWING";
        }

        return browser.name() + " " + device.name();
    }
}
