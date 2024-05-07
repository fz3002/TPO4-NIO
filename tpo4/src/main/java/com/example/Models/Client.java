package com.example.Models;

import java.util.ArrayList;
import java.util.List;

public class Client {
    private int ID;
    private List<String> subscribedTopics = new ArrayList<String>();
    private Boolean admin = false;

    public Client(int id) {
        this.ID = id;
    }

    public int getID() {
        return this.ID;
    }

    public List<String> getSubscribedTopics() {
        return this.subscribedTopics;
    }

    public void addSubscribedTopic(String topic){
        subscribedTopics.add(topic);
    }

    public void removeSubscribedTopic(String topic){
        subscribedTopics.remove(topic);
    }

    public void setAdmin(boolean admin){
        this.admin = admin;
    }

    public boolean isAdmin(){
        return admin;
    }
}
