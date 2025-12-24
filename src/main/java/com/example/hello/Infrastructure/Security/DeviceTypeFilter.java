package com.example.hello.Infrastructure.Security;

import com.example.hello.Enum.BrowserName;
import com.example.hello.Enum.DeviceName;
import com.example.hello.Enum.DeviceType;
import com.example.hello.Middleware.ParamName;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DeviceTypeFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        DeviceType deviceType;
        if("22fe0072-d200-4ec4-a90a-d6e56d782f08".equals(request.getHeader("Device-type"))){
            deviceType = DeviceType.SWING;
        }
        else{
            deviceType = DeviceType.WEB;
        }
        request.setAttribute(ParamName.DEVICE_TYPE_ATTRIBUTE, deviceType);
        String deviceName = getDeviceName(userAgent, deviceType);
        // --- lưu vào attribute ---
        request.setAttribute(ParamName.DEVICE_NAME_ATTRIBUTE, deviceName);

        // Detect IP
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        request.setAttribute(ParamName.IP_ADDRESS_ATTRIBUTE, ip);

        filterChain.doFilter(request, response);
    }

    private String getDeviceName(String userAgent, DeviceType deviceType) {
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

        return browser.name() + " " + device.name();
    }
}
