package com.example.Admin;

import java.awt.BorderLayout;
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
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

public class AdminGUI extends JFrame implements ActionListener{

    public final static int PORT = 2137;
	private String server;
	private SocketChannel channel;

    String[] test = {"test", "test2"};
    private DefaultListModel<String> model = new DefaultListModel<String>();
	private JList<String> listOfTopics = new JList<>(model);
    private JPanel newsPanel = new JPanel(new BorderLayout());
	private JPanel optionsPanel = new JPanel(new BorderLayout());
    private Container contentPane = getContentPane();
    private JTextArea textArea = new JTextArea(20, 20);
    private JButton sendNewsButton = new JButton("Send news");
    private JButton deleteTopicButton = new JButton("Delete Topic");
    private JButton addTopicButton = new JButton("Add Topic");
    private JTabbedPane tabbedPane = new JTabbedPane();
    
    private AdminTask task;

    public AdminGUI(String server) {
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
		for (String e : test){
			model.addElement(e);
		}
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane.setLayout(new FlowLayout());
        newsPanel.setBorder(BorderFactory.createTitledBorder("News"));
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
        JPanel buttonPanel = new JPanel();
		buttonPanel.add(deleteTopicButton);
		buttonPanel.add(addTopicButton);

		optionsPanel.add(new JScrollPane(listOfTopics), BorderLayout.CENTER);
		optionsPanel.add(buttonPanel, BorderLayout.SOUTH);
        newsPanel.add(new JScrollPane(textArea), BorderLayout.NORTH);
		deleteTopicButton.addActionListener(this);
		addTopicButton.addActionListener(this);
		sendNewsButton.addActionListener(this);
        newsPanel.add(sendNewsButton, BorderLayout.SOUTH);
		tabbedPane.add("News", newsPanel);
		tabbedPane.add("Menage Topics", optionsPanel);
        contentPane.add(tabbedPane);
        pack();
		setVisible(true);

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
        if (event.getSource() == deleteTopicButton){
            model.remove(listOfTopics.getSelectedIndex());
        }else if (event.getSource() == addTopicButton){
			String newTopic = JOptionPane.showInputDialog(this, "Add Topic", null);
			model.addElement(newTopic);
		}else if (event.getSource() == sendNewsButton){
			//TODO: sending news to server
		}
    }

}
