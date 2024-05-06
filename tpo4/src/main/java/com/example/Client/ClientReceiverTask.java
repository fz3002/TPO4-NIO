package com.example.Client;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class ClientReceiverTask implements Runnable{

    private static Charset charset = Charset.forName("ISO-8859-2");
    private static ByteBuffer inBuffer = ByteBuffer.allocateDirect(1024);
    private SocketChannel channel;
    private ClientGUI gui;
    private String message;
    private StringBuffer result;
    private Gson g = new Gson();

    public ClientReceiverTask(ClientGUI gui, SocketChannel channel){
        this.gui = gui;
        this.channel = channel;
    }

    @Override
    public void run() {
        getTopics();
        while(true) {
            listen();
        }
    }

    private void getTopics() {
        result = new StringBuffer();
        int count = 0, rcount = 0;
        try {
            CharBuffer cbuf = CharBuffer.wrap("GET /listOfTopics");
            ByteBuffer outBuffer = charset.encode(cbuf);
            channel.write(outBuffer);
            while(true) {
                inBuffer.clear();
                int readBytes = channel.read(inBuffer);
                if (readBytes == 0) {
                    System.out.println("Waiting " + ++count);
                    continue;
                }else if (readBytes == -1) {
                    System.out.println("Channel closed");
                }else{
                    inBuffer.flip();
                    cbuf = charset.decode(inBuffer);
                    if(cbuf.toString().equals("END")) break;
                    result.append(cbuf);
                    System.out.println("Receiving..." + rcount);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        List<String> receivedTopics = g.fromJson(result.toString(), ArrayList.class);
        gui.setModel(receivedTopics);
    }

    private void listen() {
        result = new StringBuffer();
        int count = 0, rcount = 0;
        try {
            CharBuffer cbuf;
            while(true) {
                inBuffer.clear();
                int readBytes = channel.read(inBuffer);
                if (readBytes == 0) {
                    System.out.println("Waiting " + ++count);
                    continue;
                }else if (readBytes == -1) {
                    System.out.println("Channel closed");
                }else{
                    inBuffer.flip();
                    cbuf = charset.decode(inBuffer);
                    if(cbuf.toString().equals("END")) break;
                    result.append(cbuf);
                    System.out.println("Receiving..." + rcount);
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (result.toString().startsWith("REMOVE")){
            gui.deleteTopic(result.toString().split("\n")[1]);
        }else if(result.toString().startsWith("ADD")){
            gui.addTopic(result.toString().split("\n")[1]);
        }
    }

}
