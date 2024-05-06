package com.example.Models;

import java.util.ArrayList;
import java.util.List;

public class Client {
    private int ID;
    private List<String> subscribedTopics = new ArrayList<String>();

    public int getID() {
        return this.ID;
    }

    public List<String> getSubscribedTopics() {
        return this.subscribedTopics;
    }
     public void addSubscribedTopic(String topic){
        subscribedTopics.add(topic);
     }

}
