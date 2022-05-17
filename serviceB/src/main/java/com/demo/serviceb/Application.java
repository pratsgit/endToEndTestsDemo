package com.demo.serviceb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.function.Consumer;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean(name = "receive")
    public Consumer<MessageEvent> receive() {
        return messageEvent -> {
            System.out.println("I am here");
            System.out.println("On Service B received event with message:" + messageEvent.getMessage());
        };
    }
}
