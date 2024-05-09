package com.example.Admin;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashSet;

import com.google.gson.Gson;

public class AdminTask implements Runnable {

    private static final String ENDCODE = "\nEND";
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
                System.out.println("remove topic");
                gui.topicRemoved = false;
            } else if (gui.newsToSend) {
                try {
                    CharBuffer cbuf = CharBuffer.wrap("SEND" + "\n" + gui.getNews().getParseMessage() + ENDCODE);
                    ByteBuffer outBuffer = charset.encode(cbuf);
                    channel.write(outBuffer);
                    gui.newsToSend = false;
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
            CharBuffer cbuf = CharBuffer.wrap(ROLE + ENDCODE);
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
                    result.append(cbuf);
                    if (cbuf.toString().endsWith("END"))
                        break;
                    System.out.println("Receiving..." + rcount);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String resultString = result.toString().substring(0, result.length() - 3);
        HashSet<String> receivedTopics = g.fromJson(resultString, HashSet.class);
        gui.setModel(receivedTopics);
        gui.comboBoxUpdate();
    }

    private void sendTopicListChange(String command) {
        try {
            CharBuffer cbuf = CharBuffer.wrap(command + "\n" + gui.getChangedTopic() + ENDCODE);
            ByteBuffer outBuffer = charset.encode(cbuf);
            channel.write(outBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
