package com.example.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

public class ClientGUI {

	public final static int PORT = 2137;
	private String server;
	private SocketChannel channel;

	//TODO: GUI Elements

	private ClientTask task;

	public ClientGUI(String server) {
		this.server = server;

		try {
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			connect();
		} catch (UnknownHostException e) {
			System.err.println("Unknow host: " + server);
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(2);
		}
	}

	//TODO: configure GUI elements


	private void connect() throws IOException {
		if(!channel.isOpen()) channel = SocketChannel.open();
		channel.connect(new InetSocketAddress(server, PORT));
		System.out.println("Connecting");
		while(!channel.finishConnect()){
			try {Thread.sleep(1000);} catch(Exception e) {return;}
			System.out.println(".");
		}
		System.out.println("\\nConnection Succesful");
	}

}
