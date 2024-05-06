package com.example.Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class ClientGUI extends JFrame implements ActionListener {

	public final static int PORT = 2137;
	private String server;
	private SocketChannel channel;

	//TODO: GUI Elements
	String[] test = {"test", "test2"};
	private JList<String> listOfTopics = new JList<>(test);
	private HashSet<String> subsribed = new HashSet<>();
	private JTextArea textArea = new JTextArea( 20, 20);
	private JPanel newsPanel = new JPanel();
	private JPanel optionsPanel = new JPanel(new BorderLayout());
	private Container contentPane = getContentPane();
	private JButton getNextNewsButton = new JButton("Get Next News");
	private JButton subscribeButton = new JButton("Subscribe");
	private JButton unsubscribeButton = new JButton("Unsubscribe");
	private JTabbedPane tabbedPane = new JTabbedPane();

	private ClientTask task;

	public ClientGUI(String server) {
		this.server = server;
		subsribed.add("test");
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
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane.setLayout(new FlowLayout());
		textArea.setText("Test Text for News");
		newsPanel.setBorder(BorderFactory.createTitledBorder("News"));
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(subscribeButton);
		buttonPanel.add(unsubscribeButton);

		optionsPanel.add(new JScrollPane(listOfTopics), BorderLayout.CENTER);
		optionsPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		newsPanel.add(new JScrollPane(textArea));
		subscribeButton.addActionListener(this);
		unsubscribeButton.addActionListener(this);
		getNextNewsButton.addActionListener(this);
		newsPanel.add(getNextNewsButton);
		tabbedPane.add("News", newsPanel);
		tabbedPane.add("Menage Subscriptions", optionsPanel);
		contentPane.add(tabbedPane);
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

		listOfTopics.setCellRenderer(new DefaultListCellRenderer(){
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof String) {
					String topic = (String) value;
					if (subsribed.contains(topic)) {
						setBackground(Color.GREEN);
					}
					if (isSelected) {
						setBackground(getBackground().darker());
					}
				}
				return c;
			}
		});
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
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == subscribeButton){
			//TODO: send info to server about change in subscription
			boolean successfullyAdded = subsribed.add(listOfTopics.getSelectedValue());
			if(!successfullyAdded){
				JOptionPane.showMessageDialog(null, "Already subscribed");
			}
		}else if(event.getSource() == unsubscribeButton){
			//TODO: send info to server about change in subscription
			boolean successfullyRemoved = subsribed.remove(listOfTopics.getSelectedValue());
			if(!successfullyRemoved) {
				JOptionPane.showMessageDialog(null, "Topic Not subscribed");
			}
		}else if(event.getSource() == getNextNewsButton){
			//TODO: getting news from the backlog
		}
	}
}