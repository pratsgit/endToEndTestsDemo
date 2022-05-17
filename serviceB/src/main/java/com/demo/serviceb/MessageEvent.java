package com.demo.serviceb;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageEvent implements Serializable {
    private String message;

    public MessageEvent(String message) {
        this.message = message;
    }

    public MessageEvent(){
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }
}