package com.example.Admin;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashSet;

import com.google.gson.Gson;

public class AdminTask implements Runnable {

    private static final String ROLE = "ADMIN";
    private static Charset charset = Charset.forName("ISO-8859-2");
    private static ByteBuffer inBuffer = ByteBuffer.allocateDirect(1024);
    private SocketChannel channel;
    private AdminGUI gui;
    private StringBuffer result;

    private Gson g = new Gson();

    public AdminTask(AdminGUI gui, SocketChannel channel) {
        this.gui = gui;
        this.channel = channel;
    }

    @Override
    public void run() {
        getTopics();
        while (true) {
            if (gui.topicAdded) {
                sendTopicListChange("ADD");
                gui.topicAdded = false;
            } else if (gui.topicRemoved) {
                sendTopicListChange("REMOVE");
                gui.topicRemoved = false;
            } else if (gui.newsToSend) {
                try {
                    CharBuffer cbuf = CharBuffer.wrap("SEND" + "\n" + gui.getNews());
                    ByteBuffer outBuffer = charset.encode(cbuf);
                    channel.write(outBuffer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getTopics() {
        result = new StringBuffer();
        int count = 0, rcount = 0;
        try {
            CharBuffer cbuf = CharBuffer.wrap(ROLE);
            ByteBuffer outBuffer = charset.encode(cbuf);
            channel.write(outBuffer);
            while (true) {
                inBuffer.clear();
                int readBytes = channel.read(inBuffer);
                if (readBytes == 0) {
                    System.out.println("Waiting " + ++count);
                    continue;
                } else if (readBytes == -1) {
                    System.out.println("Channel closed");
                } else {
                    inBuffer.flip();
                    cbuf = charset.decode(inBuffer);
                    if (cbuf.toString().equals("END"))
                        break;
                    result.append(cbuf);
                    System.out.println("Receiving..." + rcount);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        HashSet<String> receivedTopics = g.fromJson(result.toString(), HashSet.class);
        gui.setModel(receivedTopics);
    }

    private void sendTopicListChange(String command) {
        try {
            CharBuffer cbuf = CharBuffer.wrap(command + "\n" + gui.getChangedTopic());
            ByteBuffer outBuffer = charset.encode(cbuf);
            channel.write(outBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
