package com.example.Models;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class Client {
    private SocketChannel channel;
    private List<String> subscribedTopics = new ArrayList<String>();
    private Boolean admin = false;

    public Client(SocketChannel channel) {
        this.channel = channel;
    }

    public SocketChannel getID() {
        return this.channel;
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
