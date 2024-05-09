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
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import com.example.Models.News;

public class AdminGUI extends JFrame implements ActionListener {

	public final static int PORT = 2137;
	private String server;
	private SocketChannel channel;
	private volatile String changedTopic;

	private DefaultListModel<String> model = new DefaultListModel<String>();
	private JList<String> listOfTopics = new JList<>(model);
	private JPanel newsPanel = new JPanel(new BorderLayout());
	private JPanel newsPage = new JPanel(new BorderLayout());
	private JPanel optionsPanel = new JPanel(new BorderLayout());
	private JPanel topicPanel = new JPanel(new BorderLayout());
	private Container contentPane = getContentPane();
	private JTextArea textArea = new JTextArea(20, 20);
	private JComboBox<String> topicComboBox = new JComboBox<>();
	private JButton sendNewsButton = new JButton("Send news");
	private JButton deleteTopicButton = new JButton("Delete Topic");
	private JButton addTopicButton = new JButton("Add Topic");
	private JTabbedPane tabbedPane = new JTabbedPane();

	private AdminTask task;
	public volatile boolean  topicAdded;
	public volatile boolean topicRemoved;
	public volatile boolean newsToSend;
	private News news;
	private Thread taskThread;

	public AdminGUI(String server) {
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

		task = new AdminTask(this, channel);
		taskThread = new Thread(task);
		taskThread.start();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane.setLayout(new FlowLayout());
		newsPanel.setBorder(BorderFactory.createTitledBorder("News"));
		topicPanel.setBorder(BorderFactory.createTitledBorder("Topic"));
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(deleteTopicButton);
		buttonPanel.add(addTopicButton);

		optionsPanel.add(new JScrollPane(listOfTopics), BorderLayout.CENTER);
		optionsPanel.add(buttonPanel, BorderLayout.SOUTH);
		newsPanel.add(new JScrollPane(textArea), BorderLayout.NORTH);
		topicPanel.add(topicComboBox, BorderLayout.CENTER);
		deleteTopicButton.addActionListener(this);
		addTopicButton.addActionListener(this);
		sendNewsButton.addActionListener(this);
		newsPanel.add(sendNewsButton, BorderLayout.SOUTH);
		newsPage.add(topicPanel, BorderLayout.NORTH);
		newsPage.add(newsPanel, BorderLayout.SOUTH);
		tabbedPane.add("News", newsPage);
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
				} catch (Exception ex) {
					System.exit(0);
				}
			}
		});
	}

	
	private void connect() throws IOException {
		if (!channel.isOpen())
			channel = SocketChannel.open();
		channel.connect(new InetSocketAddress(server, PORT));
		System.out.println("Connecting");
		while (!channel.finishConnect()) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				return;
			}
			System.out.println(".");
		}
		System.out.println("\\nConnection Succesful");
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == deleteTopicButton) {
			String selectedTopic = listOfTopics.getSelectedValue();
			boolean successfullyRemoved = model.removeElement(selectedTopic);
			if (successfullyRemoved) {
				topicComboBox.removeItem(selectedTopic);
				changedTopic = selectedTopic;
				topicRemoved = true;
			} else {
				JOptionPane.showMessageDialog(null, "Cannot remove topic");
			}

		} else if (event.getSource() == addTopicButton) {
			String newTopic = JOptionPane.showInputDialog(this, "Add Topic", null);

			if (model.contains(newTopic)) {
				JOptionPane.showMessageDialog(null, "Topic Already Exists");
			} else {
				model.addElement(newTopic);
				topicComboBox.addItem(newTopic);
				changedTopic = newTopic;
				topicAdded = true;
			}
		} else if (event.getSource() == sendNewsButton) {
			if (textArea.getText().length() > 0) {
				news = new News((String)topicComboBox.getSelectedItem(), textArea.getText());
				newsToSend = true;
				textArea.setText("");
			}

		}
	}

	public void setModel(Set<String> listOfTopics) {
		for (String topic : listOfTopics) {
			model.addElement(topic);
		}
	}

	public String getChangedTopic() {
		return changedTopic;
	}

	public News getNews() {
		return news;
	}

	public void comboBoxUpdate() {
		for (int i = 0; i < model.size(); i++) {
			topicComboBox.addItem(model.getElementAt(i));
		}
	}

}
