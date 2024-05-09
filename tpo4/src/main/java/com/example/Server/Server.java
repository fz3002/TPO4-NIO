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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.example.Models.Client;
import com.example.Models.News;
import com.google.gson.Gson;

public class Server {

	private ServerSocketChannel serverChannel;
	private Selector selector;
	private Set<String> topics = new HashSet<String>();
	private Set<Client> clients = new HashSet<Client>();
	private Queue<News> newsBackLog = new ConcurrentLinkedQueue<>();
	private Queue<String> topicsToAdd = new ConcurrentLinkedQueue<>();
	private Queue<String> topicsToRemove = new ConcurrentLinkedQueue<>();
	private Gson gson = new Gson();

	public static void main(String[] args) throws IOException, InterruptedException {
		new Server();
	}

	Server() throws IOException {

		// Utworzenie kanału gniazda serwera
		// i związanie go z konkretnym adresem (host+port)
		String host = "localhost";
		int port = 2137;
		serverChannel = ServerSocketChannel.open();
		serverChannel.socket().bind(new InetSocketAddress(host, port));

		// Ustalenie trybu nieblokującego
		// dla kanału serwera gniazda
		serverChannel.configureBlocking(false);

		// Utworzenie selektora
		selector = Selector.open();

		// Rejestracja kanału gniazda serwera u selektora
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		System.out.println("Server waiting ... ");

		serviceConnections();

	}

	private void serviceConnections()
			throws IOException, ClosedChannelException {
		// Selekcja gotowych operacji do wykonania i ich obsługa
		// w pętli dzialania serwera
		while (true) {

			// Selekcja gotowej operacji
			// To wywolanie jest blokujące
			// Czeka aż selektor powiadomi o gotowości jakiejś operacji na jakimś kanale
			selector.select();

			// Teraz jakieś operacje są gotowe do wykonania
			// Zbiór kluczy opisuje te operacje (i kanały)
			Set<SelectionKey> keys = selector.selectedKeys();

			// Przeglądamy "gotowe" klucze
			Iterator<SelectionKey> iter = keys.iterator();

			while (iter.hasNext()) {

				SelectionKey key = iter.next();

				iter.remove();

				if (key.isAcceptable()) {

					System.out.println("New connection ..., accepting ... ");
					SocketChannel cc = serverChannel.accept();

					cc.configureBlocking(false);

					cc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

					clients.add(new Client(key.hashCode()));

					continue;
				}

				if (key.isReadable()) {

					SocketChannel cc = (SocketChannel) key.channel();

					serviceRequest(cc, key.hashCode());

					continue;
				}
				if (key.isWritable()) {
					SocketChannel cc = (SocketChannel) key.channel();

					sendData(cc, key.hashCode());

					continue;
				}

			}
		}
	}

	private static Charset charset = Charset.forName("ISO-8859-2");
	private static final int BSIZE = 1024;

	private ByteBuffer bbuf = ByteBuffer.allocate(BSIZE);

	private StringBuffer reqString = new StringBuffer();

	private void serviceRequest(SocketChannel sc, int ClientID) {
		if (!sc.isOpen())
			return; 
		System.out.print("Reading Client Request ... ");
		reqString.setLength(0);
		bbuf.clear();

		try {
			readLoop: while (true) { //TODO: Change to readLoop to End reading after encountering END code
				int n = sc.read(bbuf);
				if (n > 0) {
					bbuf.flip();
					CharBuffer cbuf = charset.decode(bbuf);
					while (cbuf.hasRemaining()) {
						char c = cbuf.get();
						// System.out.println(c);
						if (c == '\r' || c == '\n')
							break readLoop;
						else {
							// System.out.println(c);
							reqString.append(c);
						}
					}
				}
			}

			String request = reqString.toString();
			System.out.println(reqString);

			if (request.startsWith("SUBSCRIBE")) {

				Client client = getClient(ClientID);
				client.addSubscribedTopic(request.split("\n")[1]);

			} else if (request.startsWith("UNSUBSCRIBE")) {

				Client client = getClient(ClientID);
				client.removeSubscribedTopic(request.split("\n")[1]);

			} else if (request.startsWith("ADD")) {

				topics.add(request.split("\n")[1]);
				topicsToAdd.add(request.split("\n")[1]);

			} else if (request.startsWith("REMOVE")) {

				topics.remove(request.split("\n")[1]);
				topicsToRemove.add(request.split("\n")[1]);

			} else if (request.equals("CLIENT")) {

				Client client = getClient(ClientID);
				client.setAdmin(false);
				String topicsString = gson.toJson(topics, HashSet.class);
				try {
					CharBuffer cbuf = CharBuffer.wrap(topicsString + "\nEND");
					ByteBuffer outBuffer = charset.encode(cbuf);
					sc.write(outBuffer);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (request.equals("ADMIN")) {

				Client client = getClient(ClientID);
				client.setAdmin(true);
				String topicsString = gson.toJson(topics, HashSet.class);
				try {
					CharBuffer cbuf = CharBuffer.wrap(topicsString + "\nEND");
					ByteBuffer outBuffer = charset.encode(cbuf);
					sc.write(outBuffer);
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (request.startsWith("SEND")) {

				News news = new News(request.split("\n")[1], request.split("\n")[2]);
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

	private void sendData(SocketChannel sc, int ClientID) {
		if (getClient(ClientID).isAdmin()) {
			if (newsBackLog.size() > 0) {
				for (News news : newsBackLog) {
					if (getClient(ClientID).getSubscribedTopics().contains(news.getTopic())) {
						try {
							CharBuffer cbuf = CharBuffer.wrap(news.getParseMessage() + "\nEND");
							ByteBuffer outBuffer = charset.encode(cbuf);
							sc.write(outBuffer);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} else if (topicsToAdd.size() > 0) {

				try {
					CharBuffer cbuf = CharBuffer.wrap("ADD\n" + topicsToAdd.poll() + "\nEND");
					ByteBuffer outBuffer = charset.encode(cbuf);
					sc.write(outBuffer);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (topicsToRemove.size() > 0) {
				try {
					CharBuffer cbuf = CharBuffer.wrap("REMOVE\n" + topicsToRemove.poll() + "\nEND");
					ByteBuffer outBuffer = charset.encode(cbuf);
					sc.write(outBuffer);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Client getClient(int ClientID) {
		for (Client client : clients) {
			if (client.getID() == ClientID) {
				return client;
			}
		}
		return null;
	}

}
