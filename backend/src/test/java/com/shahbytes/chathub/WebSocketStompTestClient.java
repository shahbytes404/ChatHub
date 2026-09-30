package com.shahbytes.chathub;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class WebSocketStompTestClient {
    public static void main(String[] args) throws InterruptedException {
        String token = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiI1M2Y2NDY0ZC1hMmVmLTQ1YzQtOTU1My04MjBiNDQxNjBlZWIiLCJlbWFpbCI6InVzZXIxQHVzZXIxLmNvbSIsImlhdCI6MTc5MDc3MTA3MiwiZXhwIjoxNzkxMzc1ODcyfQ.lj4bbsNK4lkFrwqr3wAdL7pR0EotLX9ZRgD90LylY8V8hzagwZhSs0VyGv-29t9W";

        var webSocketClient = new StandardWebSocketClient();

        var stompClient = new WebSocketStompClient(webSocketClient);

        stompClient.setMessageConverter(
                new MessageConverter() {
                    @Override
                    public @Nullable Object fromMessage(Message<?> message, Class<?> targetClass) {
                        return message.getPayload();
                    }

                    @Override
                    public @Nullable Message<?> toMessage(Object payload, @Nullable MessageHeaders headers) {
                        return null;
                    }
                }
        );

        var connected = new CountDownLatch(1);

        StompHeaders connectHeaders = new StompHeaders();

        connectHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        stompClient.connectAsync(
                "ws://localhost:8080/ws",
                new WebSocketHttpHeaders(),
                connectHeaders,
                new StompSessionHandlerAdapter() {
                    @Override
                    public void afterConnected(
                            StompSession session,
                            StompHeaders connectedHeaders
                    ) {
                        System.out.println("=============================");
                        System.out.println("CONNECTED!");

                        System.out.println(
                                "Session ID: " + session.getSessionId()
                        );

                        session.subscribe(
                                "/user/queue/events",
                                new StompFrameHandler() {
                                    @Override
                                    public Type getPayloadType(StompHeaders headers) {
                                        return Object.class;
                                    }

                                    @Override
                                    public void handleFrame(
                                            StompHeaders headers,
                                            @Nullable Object payload) {
                                        System.out.println("=============================");
                                        System.out.println("MESSAGE RECEIVED!");
                                        System.out.println("=============================");

                                        System.out.println("Headers:");
                                        System.out.println(headers);

                                        System.out.println("Payload type:");
                                        System.out.println(
                                                payload == null
                                                        ? "null"
                                                        : payload.getClass().getName()
                                        );

                                        System.out.println("Payload:");

                                        if (payload instanceof byte[] bytes) {
                                            System.out.println(
                                                    new String(
                                                            bytes,
                                                            StandardCharsets.UTF_8
                                                    )
                                            );
                                        } else {
                                            System.out.println(payload);
                                        }

                                        System.out.println("=============================");
                                    }
                                }
                        );

                        System.out.println("SUBSCRIBED: /user/queue/events");

                        connected.countDown();
                    }

                    @Override
                    public void handleException(
                            StompSession session,
                            StompCommand command,
                            StompHeaders headers,
                            byte[] payload,
                            Throwable exception
                    ) {
                        System.err.println("=============================");
                        System.err.println("STOMP ERROR");
                        System.err.println("=============================");

                        exception.printStackTrace();

                        connected.countDown();
                    }
                }
        );

        connected.await();

        Thread.sleep(60_000);
    }
}
