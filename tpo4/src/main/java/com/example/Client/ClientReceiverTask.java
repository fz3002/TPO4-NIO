package com.example.Client;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.example.Models.News;
import com.google.gson.Gson;

public class ClientReceiverTask implements Runnable {

    private static final String ENDCODE = "\nEND";
    private static final String ROLE = "CLIENT";
    private static Charset charset = Charset.forName("ISO-8859-2");
    private static ByteBuffer inBuffer = ByteBuffer.allocateDirect(1024);
    private Queue<String> newsBackLog = new ConcurrentLinkedQueue<>();
    private SocketChannel channel;
    private ClientGUI gui;
    private StringBuffer result;
    private Gson g = new Gson();
    private boolean listening = true;

    public ClientReceiverTask(ClientGUI gui, SocketChannel channel) {
        this.gui = gui;
        this.channel = channel;
    }

    @Override
    public void run() {
        getTopics();
        while (listening) {
            listen();
        }
    }

    private void getTopics() {
        result = new StringBuffer();
        int count = 0, rcount = 0;
        try {
            CharBuffer cbuf = CharBuffer.wrap(ROLE + ENDCODE);
            ByteBuffer outBuffer = charset.encode(cbuf);
            channel.write(outBuffer);
            while (true) {
                inBuffer.clear();
                int readBytes = channel.read(inBuffer);
                if (readBytes == 0) {
                    System.out.println("CLIENT: Waiting " + ++count);
                    continue;
                } else if (readBytes == -1) {
                    System.out.println("CLIENT: Channel closed");
                    listening = false;
                    break;
                } else {
                    inBuffer.flip();
                    cbuf = charset.decode(inBuffer);
                    result.append(cbuf);
                    if (cbuf.toString().endsWith("END"))
                        break;
                    System.out.println("CLIENT: Receiving..." + rcount);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("CLIENT: list of topics: " + result);
        String substringResult = result.toString().substring(0, result.toString().length() - 3);
        HashSet<String> receivedTopics = g.fromJson(substringResult, HashSet.class);
        gui.setModel(receivedTopics);
    }

    private void listen() {
        result = new StringBuffer();
        int count = 0, rcount = 0;
        System.out.println("CLIENT: Listening...");
        try {
            CharBuffer cbuf;
            while (true) {
                inBuffer.clear();
                int readBytes = channel.read(inBuffer);
                if (readBytes == 0) {
                    System.out.println("Client: Waiting " + ++count);
                    continue;
                } else if (readBytes == -1) {
                    System.out.println("CLIENT: Receiver: Channel closed");
                    listening = false;
                    break;
                } else {
                    System.out.println("CLIENT: Reading");
                    inBuffer.flip();
                    cbuf = charset.decode(inBuffer);
                    result.append(cbuf);
                    if (cbuf.toString().endsWith("END"))
                        break;
                    System.out.println("CLIENT: Receiving..." + rcount);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result.toString());
        if (result.toString().startsWith("REMOVE")) {
            gui.deleteTopic(result.toString().split("\n")[1]);
        } else if (result.toString().startsWith("ADD")) {
            gui.addTopic(result.toString().split("\n")[1]);
        } else if (result.toString().startsWith("NEWS")) {
            System.out.println("CLIENT: News Received");
            News news = g.fromJson(result.toString().split("\n")[1], News.class);
            newsBackLog.add(news.getContent());
            gui.setBacklogStatus(newsBackLog.size());
        }
    }

    public String getNews() {
        String newsToShow = newsBackLog.poll();
        gui.setBacklogStatus(newsBackLog.size());
        return newsToShow;

    }

}
