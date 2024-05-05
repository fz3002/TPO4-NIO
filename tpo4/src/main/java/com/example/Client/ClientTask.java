package com.example.Client;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class ClientTask implements Runnable{

    private static Charset charset = Charset.forName("ISO-8859-2");
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    private SocketChannel channel;
    private ClientGUI gui;
    private String message;
    private StringBuffer result;

    public ClientTask(ClientGUI gui, SocketChannel channel, String messageString){
        this.gui = gui;
        this.channel = channel;
        this.message = messageString;
    }

    @Override
    public void run() {
        // TODO: client task communicating with server
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

}
