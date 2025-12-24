package com.example.hello.Infrastructure.WebClient;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IpService {
    WebClient webClient;

    public Map<?,?> getIp(String ip) {
        return webClient.get()
                .uri("https://ipinfo.io/" + ip + "/json")
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    public IpResponse getAddress(String ip) {
        Map<?,?> result = getIp(ip);
        return IpResponse.builder()
                .city((String) result.get("city"))
                .country((String) result.get("country"))
                .region((String) result.get("region"))
                .timezone((String) result.get("timezone"))
                .build();
    }
}
