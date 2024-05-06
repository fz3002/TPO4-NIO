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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.example.Models.Client;


public class Server {

	private ServerSocketChannel serverChannel;
	private Selector selector;
	private List<String> topics = new ArrayList<String>();
	private List<Client> clients = new ArrayList<Client>();


	public static void main(String[] args) throws IOException, InterruptedException {	
		new Server();
	}
	
	Server () throws IOException {
	
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
			  
			 while(iter.hasNext()) {  
			    
				 SelectionKey key = iter.next();
			    
				 iter.remove();                                                  
			    
				 if (key.isAcceptable()) {
			      
					 System.out.println("New connection ..., accepting ... ");
					 SocketChannel cc = serverChannel.accept();
			      
					 cc.configureBlocking(false);
			    		
					 cc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
			      
					 continue;
				  }
			    
				  if (key.isReadable()) {
			      
					  SocketChannel cc = (SocketChannel) key.channel();
					  
					  //TODO: Find a way to bind Client to key, to update client subscriptions
					  //Probably using haschcode of key
					  serviceRequest(cc); 
			
					  continue;
				  }
				  if (key.isWritable()) {
			    	
					  continue;
				  } 
				 			  
			 }
		}
	}
	
	
		// Strona kodowa do kodowania/dekodowania buforów
	private static Charset charset  = Charset.forName("ISO-8859-2");
	private static final int BSIZE = 1024;

	  	// Bufor bajtowy - do niego są wczytywane dane z kanału
	private ByteBuffer bbuf = ByteBuffer.allocate(BSIZE);

	  	// Tu będzie zlecenie do pezetworzenia
	private StringBuffer reqString = new StringBuffer();
	
	//TODO: Change service request to implement given bussiness logic
	private void serviceRequest(SocketChannel sc) {
		if (!sc.isOpen()) return; // jeżeli kanał zamknięty
	 
		System.out.print("Reading Client Request ... ");
			// Odczytanie zlecenia
		reqString.setLength(0);
	    bbuf.clear();
	    
	    try {
	    	readLoop:                    // Czytanie jest nieblokujące
	    	while (true) {               // kontynujemy je dopóki
	    		int n = sc.read(bbuf);   // nie natrafimy na koniec wiersza
	    		if (n > 0) {
	    			bbuf.flip();
	    			CharBuffer cbuf = charset.decode(bbuf);
	    			while(cbuf.hasRemaining()) {
	    				char c = cbuf.get();
	    				//System.out.println(c);
	    				if (c == '\r' || c == '\n') break readLoop;
	    				else {
	    					//System.out.println(c);
	    				    reqString.append(c);
	    				}
	    			}
	    		}
	      	}
	    		
		    String request = reqString.toString();
		    System.out.println(reqString);
		    
		    if (request.startsWith("SUBSCRIBE")) {
		    	//TODO: add subscription to client
		    }else if(request.startsWith("UNSUBSCRIBE")){
				//TODO: remove subscription from client
			}else if(request.startsWith("ADD")){
				//TODO: add new topic
			}else if(request.startsWith("REMOVE")){
				//TODO: remove topic
			}else if(request.equals("GET /listOfTopics")){
				//TODO: send list of possible topics to client in JSON format
			}else if(request.startsWith("SEND")){
				//TODO: send news to subscribers
			}
	    } catch (Exception exc) { // przerwane polączenie?
	    	exc.printStackTrace();
	        try { sc.close();
	              sc.socket().close();
	        } catch (Exception e) {}
	    }
	    
	}

}

