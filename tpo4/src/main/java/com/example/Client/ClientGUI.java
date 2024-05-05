package com.example.Client;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ClientGUI extends JFrame implements ActionListener {

	public final static int PORT = 2137;
	private String server;
	private SocketChannel channel;

	//TODO: GUI Elements
	private JList<String> listOfTopics = new JList<>();
	private JList<String> listOfExistingTopics = new JList<>();
	private JScrollPane scrollPane = new JScrollPane();
	private JTextArea textArea = new JTextArea( 20, 20);
	private JPanel newsPanel = new JPanel();
	private JPanel optionsPanel = new JPanel();
	private Container contentPane = getContentPane();
	private JButton subMenagmentButton = new JButton("Menage Subscriptions");
	private JButton getNextNewsButton = new JButton("Get Next News");

	private ClientTask task;

	public ClientGUI(String server) {
		this.server = server;
		/*
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
		*/
		contentPane.setLayout(new FlowLayout());
		textArea.setText("Test Text for News");
		newsPanel.setBorder(BorderFactory.createTitledBorder("News"));
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		newsPanel.add(new JScrollPane(textArea));
		optionsPanel.add(subMenagmentButton);
		optionsPanel.add(getNextNewsButton);
		contentPane.add(newsPanel);
		contentPane.add(optionsPanel);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				dispose();
				try {
					channel.close();
					channel.socket().close();
				}catch(Exception ex) {
					System.exit(0);
				}
			}
		});

		pack();
		setVisible(true);
	}

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

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'actionPerformed'");
	}
}