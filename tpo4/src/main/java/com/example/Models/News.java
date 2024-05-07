package com.example.Models;

public class News {

    private String topic;

    private String content;

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
        //TODO: Parsing news to message
        return null;
    }
    
}
