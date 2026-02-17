package com.example.hello.SseEmitter;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SseService {
    Map<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private void removeEmitter(UUID userId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(userId);
        if (list == null) return;

        list.remove(emitter);
        if (list.isEmpty()) {
            log.info("Removed emitter user {}", userId);
            emitters.remove(userId);
        }
        log.info("remove emitter {} for user {}", emitter, userId);
    }

    public SseEmitter createSseEmitter(UUID key) {
        SseEmitter emitter = new SseEmitter(60L * 60 * 1000);
        emitters.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>());
        emitters.get(key).add(emitter);
        log.info("Emitter {} has been created:  {}", key, emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name(SseTopicName.connected.name())
                    .data("Sse connected"));
        }
        catch (IOException e) {
            log.error("sse connect failed", e);
        }
        emitter.onCompletion(() -> removeEmitter(key, emitter));
        emitter.onTimeout(() -> removeEmitter(key, emitter));
        emitter.onError((e) -> removeEmitter(key, emitter));

        return emitter;
    }


    public <T> void sendSse(String topicName, T data, List<UUID> keys) {
        keys.forEach(key -> {
            if(emitters.get(key) != null){
                emitters.get(key).forEach(emitter -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .id(UUID.randomUUID().toString())
                                .name(topicName)
                                .data(data));
                        log.info("Emitter {} has been sent:  {} width body {}", emitter, key, data);
                    }
                    catch (IOException e) {
                        removeEmitter(key, emitter);
                    }
                });
            }
        });
    }
}
