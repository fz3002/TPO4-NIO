package com.example.Client;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class ClientSenderTask implements Runnable {
    private static Charset charset = Charset.forName("ISO-8859-2");
    private SocketChannel channel;
    private ClientGUI gui;

    public ClientSenderTask(ClientGUI gui, SocketChannel channel) {
        this.gui = gui;
        this.channel = channel;
    }

    @Override
    public void run() {
        while (true) {
            if (gui.newSubscription) {
                sendTopicSubscriptionChange("SUBSCRIBE");
                gui.newSubscription = false;
            } else if (gui.newUnsubscription) {
                sendTopicSubscriptionChange("UNSUBSCRIBE");
                gui.newUnsubscription = false;
            }
        }
    }

    private void sendTopicSubscriptionChange(String command) {
        try {
            CharBuffer cbuf = CharBuffer.wrap(command + "\n" + gui.getChangedTopic());
            ByteBuffer outBuffer = charset.encode(cbuf);
            channel.write(outBuffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
