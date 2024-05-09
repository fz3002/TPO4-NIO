package com.example.Models;

import com.google.gson.Gson;

public class News {

    private String topic;

    private String content;

    private Gson gson = new Gson();

    public News(String topic, String content) {
        this.topic = topic;
        this.content = content;
    }

    public String getTopic() {
        return topic;
    }

    public String getContent() {
        return content;
    }

    public String getParseMessage() {
        return gson.toJson(this);
    }
    
}
