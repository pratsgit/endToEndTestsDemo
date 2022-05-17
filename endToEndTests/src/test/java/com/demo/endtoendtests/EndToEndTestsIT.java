package com.demo.endtoendtests;

import com.demo.serviceb.MessageEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1,
        bootstrapServersProperty = "spring.kafka.bootstrap-servers",
        topics = { "topic1", "topic2" })
@DirtiesContext
@TestPropertySource(properties = "test.topic=topic2")
public class EndToEndTestsIT {
    private static ConfigurableApplicationContext serviceAContext = null;
    private static ConfigurableApplicationContext serviceBContext = null;

    @BeforeEach
    public void startServices() throws InterruptedException {
        Executors.newSingleThreadExecutor().execute(() -> {
            SpringApplication application = new SpringApplication(com.demo.servicea.Application.class);
            application.setAdditionalProfiles("serviceAProfile");
            serviceAContext = application.run();
        });

        Executors.newSingleThreadExecutor().execute(() -> {
            SpringApplication application = new SpringApplication(com.demo.serviceb.Application.class);
            application.setAdditionalProfiles("serviceBProfile");
            serviceBContext = application.run();
        });

        while (serviceAContext == null || !serviceAContext.isRunning()) {
            System.out.println("Waiting for service A to start, already waited for seconds:" );
            Thread.sleep(1000);
        }

        while (serviceBContext == null || !serviceBContext.isRunning()) {
            System.out.println("Waiting for service B to start, already waited for seconds:" );
            Thread.sleep(1000);
        }
    }

    @AfterEach
    public void cleanTestState() {
        serviceAContext.close();
        serviceBContext.close();
    }

    @Autowired
    private KafkaConsumer consumer;

    @Autowired
    private KafkaProducer producer;

    @Test
    public void messageSentOnTopic1CapturedOnTopic2() throws InterruptedException, IOException {
        String payload = "{\"message\" : \"message on topic 1\"}";
        this.producer.send("topic1", payload);

        consumer.getLatch().await(5, TimeUnit.MINUTES);
        assertThat(consumer.getLatch().getCount(), equalTo(0L));

        byte[] recordBytes = consumer.getRecordValue();
        assertNotNull(recordBytes);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        MessageEvent messageEvent = mapper.readValue(recordBytes, MessageEvent.class);
        assertNotNull(messageEvent);
        assertEquals("message on topic 1", messageEvent.getMessage());
    }
}
