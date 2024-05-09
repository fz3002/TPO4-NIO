package com.example.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.example.Models.Client;
import com.example.Models.News;
import com.google.gson.Gson;

public class Server {

	private static final String ENDCODE = "\nEND";
	private static final String[] LISTOFDEFAULTTOPICS = {"Politics", "Sport", "Show Business"};
	private ServerSocketChannel serverChannel;
	private Selector selector;
	private Set<String> topics = new HashSet<String>(Arrays.asList(LISTOFDEFAULTTOPICS));
	private Set<Client> clients = new HashSet<Client>();
	private Queue<News> newsBackLog = new ConcurrentLinkedQueue<>();
	private Queue<String> topicsToAdd = new ConcurrentLinkedQueue<>();
	private Queue<String> topicsToRemove = new ConcurrentLinkedQueue<>();
	private Gson gson = new Gson();

	public static void main(String[] args) throws IOException, InterruptedException {
		new Server();
	}

	Server() throws IOException {

		String host = "localhost";
		int port = 2137;
		serverChannel = ServerSocketChannel.open();
		serverChannel.socket().bind(new InetSocketAddress(host, port));

		serverChannel.configureBlocking(false);

		selector = Selector.open();

		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		System.out.println("Server waiting ... ");

		serviceConnections();

	}

	private void serviceConnections()
			throws IOException, ClosedChannelException {
		while (true) {

			selector.select();

			Set<SelectionKey> keys = selector.selectedKeys();

			Iterator<SelectionKey> iter = keys.iterator();

			while (iter.hasNext()) {

				SelectionKey key = iter.next();

				iter.remove();

				if (key.isAcceptable()) {

					System.out.println("New connection ..., accepting ... ");
					SocketChannel cc = serverChannel.accept();

					cc.configureBlocking(false);

					cc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

					clients.add(new Client(cc));

					continue;
				}

				if (key.isReadable()) {

					SocketChannel cc = (SocketChannel) key.channel();

					serviceRequest(cc);

					continue;
				}
				if (key.isWritable()) {
					SocketChannel cc = (SocketChannel) key.channel();
					
					sendData(cc);

					continue;
				}

			}
		}
	}

	private static Charset charset = Charset.forName("ISO-8859-2");
	private static final int BSIZE = 1024;

	private ByteBuffer bbuf = ByteBuffer.allocate(BSIZE);

	private StringBuffer reqString = new StringBuffer();

	private void serviceRequest(SocketChannel sc) {
		if (!sc.isOpen())
			return; 
		System.out.print("Reading Client Request ... ");
		reqString.setLength(0);
		bbuf.clear();

		try {
			CharBuffer cbuf;
			readLoop: while (true) {
				bbuf.clear();
                int readBytes = sc.read(bbuf);
                if (readBytes == 0) {
                    continue;
                } else if (readBytes == -1) {
                    System.out.println("SERVER: Channel closed");
                } else {
					System.out.println("reading");
                    bbuf.flip();
                    cbuf = charset.decode(bbuf);
					reqString.append(cbuf);
                    if (cbuf.toString().endsWith("END"))
                        break readLoop;
                }
			}

			String request = reqString.toString().substring(0, reqString.toString().length() - 3);
			System.out.println(request);

			if (request.startsWith("SUBSCRIBE")) {

				Client client = getClient(sc);
				client.addSubscribedTopic(request.split("\n")[1]);

			} else if (request.startsWith("UNSUBSCRIBE")) {

				Client client = getClient(sc);
				client.removeSubscribedTopic(request.split("\n")[1]);

			} else if (request.startsWith("ADD")) {

				topics.add(request.split("\n")[1]);
				topicsToAdd.add(request.split("\n")[1]);

			} else if (request.startsWith("REMOVE")) {

				topics.remove(request.split("\n")[1]);
				topicsToRemove.add(request.split("\n")[1]);

			} else if (request.startsWith("CLIENT")) {

				System.out.println("List req received");

				Client client = getClient(sc);
				client.setAdmin(false);
				String topicsString = gson.toJson(topics, HashSet.class);
				System.out.println(topicsString);
				try {
					cbuf = CharBuffer.wrap(topicsString + ENDCODE);
					ByteBuffer outBuffer = charset.encode(cbuf);
					sc.write(outBuffer);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (request.startsWith("ADMIN")) {

				Client client = getClient(sc);
				client.setAdmin(true);
				String topicsString = gson.toJson(topics, HashSet.class);
				try {
					cbuf = CharBuffer.wrap(topicsString + ENDCODE);
					ByteBuffer outBuffer = charset.encode(cbuf);
					sc.write(outBuffer);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (request.startsWith("SEND")) {
				String body = request.substring(request.lastIndexOf("SEND"));
				News news = gson.fromJson(body, News.class);
				newsBackLog.add(news);
			}
		} catch (Exception exc) {
			// przerwane polączenie?
			exc.printStackTrace();
			try {
				sc.close();
				sc.socket().close();
			} catch (Exception e) {
			}
		}

	}

	private void sendData(SocketChannel sc) {
		if (getClient(sc).isAdmin()) {
			if (newsBackLog.size() > 0) {
				for (News news : newsBackLog) {
					if (getClient(sc).getSubscribedTopics().contains(news.getTopic())) {
						try {
							CharBuffer cbuf = CharBuffer.wrap(news.getParseMessage() + ENDCODE);
							ByteBuffer outBuffer = charset.encode(cbuf);
							sc.write(outBuffer);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} else if (topicsToAdd.size() > 0) {

				try {
					CharBuffer cbuf = CharBuffer.wrap("ADD\n" + topicsToAdd.poll() + ENDCODE);
					ByteBuffer outBuffer = charset.encode(cbuf);
					sc.write(outBuffer);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (topicsToRemove.size() > 0) {
				try {
					CharBuffer cbuf = CharBuffer.wrap("REMOVE\n" + topicsToRemove.poll() + ENDCODE);
					ByteBuffer outBuffer = charset.encode(cbuf);
					sc.write(outBuffer);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Client getClient(SocketChannel clientChannel) {
		for (Client client : clients) {
			if (client.getID() == clientChannel) {
				return client;
			}
		}
		return null;
	}

}
