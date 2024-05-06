package com.example.Admin;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class AdminTask implements Runnable{

    private static Charset charset = Charset.forName("ISO-8859-2");
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    private SocketChannel channel;
    private AdminGUI gui;
    private String message;
    private StringBuffer result;

    public AdminTask(AdminGUI gui, SocketChannel channel, String messageString){
        this.gui = gui;
        this.channel = channel;
        this.message = messageString;
    }

    @Override
    public void run() {
        // TODO: admin client task
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

}
