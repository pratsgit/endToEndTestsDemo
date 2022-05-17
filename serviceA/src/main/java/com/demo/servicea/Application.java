package com.demo.servicea;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

@SpringBootApplication
public class Application {
    static final String RESULTS_BINDING_NAME = "results-out-0";

    private StreamBridge streamBridge;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    public Application(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Bean(name = "receive")
    public Consumer<MessageEvent> receive() {
        return messageEvent -> {
            System.out.println("On Service A received event with message:" + messageEvent.getMessage());
            String payload = "{\"message\" : \"" + messageEvent.getMessage() + "\"}";
            this.streamBridge.send(RESULTS_BINDING_NAME, payload);
        };
    }
}
