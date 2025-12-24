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
            emitters.remove(userId);
        }
    }

    public SseEmitter createSseEmitter(UUID userId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
        emitters.get(userId).add(emitter);
        log.info("Emitter {} has been created:  {}", userId, emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        return emitter;
    }

    public <T> void sendNotification (String topicName, T data, List<UUID> userIds) {
        userIds.forEach(userId -> {
            if(emitters.get(userId) != null){
                emitters.get(userId).forEach(emitter -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name(topicName)
                                .data(data));
                    }
                    catch (IOException e) {
                        removeEmitter(userId, emitter);
                    }
                });
            }
        });
    }
}
