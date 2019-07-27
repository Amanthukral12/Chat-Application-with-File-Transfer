import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.JOptionPane;

public class ChatServer implements Runnable{

	public static final int PORT = 1234;
	private ServerWindow mainWindow;
	private static HashMap<String, Socket> dataSocketsHMap;
	
	public static void main(String[] args) {
		new ChatServer();
	}
	
	public ChatServer() {
		dataSocketsHMap = new HashMap<String, Socket>();
		
		//initialize and display the main window (server window)
		mainWindow = new ServerWindow();
		
		//start a new thread dedicated to receiving new connection requests
		Thread thread = new Thread(this);
		thread.start(); //calls the run() method on the new thread
	}

	@Override
	public void run() {
		ServerSocket connectionSocket;
		try {
			connectionSocket = new ServerSocket(PORT);
			//as long as the program is running the server is waiting for new connection requests
			while(true) {
				Socket dataSocket = connectionSocket.accept(); //accept new connection
				
				//send the connected clients to the new client
				sendConnClientsToNewClient(dataSocket);
				
				//a new thread dedicated to receiving messages
				ReceiveFromClientThread receive = new ReceiveFromClientThread(dataSocket, mainWindow, dataSocketsHMap);
				Thread thread = new Thread(receive);
				thread.start();
			}
		} catch (IOException e) {
			JOptionPane.showMessageDialog(mainWindow, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			//e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void sendConnClientsToNewClient(Socket dataSocket) throws IOException {
		OutputStream os = dataSocket.getOutputStream();
		PrintWriter out = new PrintWriter(os,true);
		
		for(String name: mainWindow.getClientNames()) {	
			out.println(name); //send message to client with PrintWriter
		}
		out.println("!END_CLIENT_INIT");
	}
}

class ReceiveFromClientThread implements Runnable{
	
	private Socket dataSocket;
	private ServerWindow mainWindow;
	private HashMap<String, Socket> dataSocketsHMap;
	
	public ReceiveFromClientThread(Socket soc, ServerWindow mainWindow, HashMap<String, Socket> dataSocketsHMap){
		this.mainWindow = mainWindow;
		dataSocket = soc;
		this.dataSocketsHMap = dataSocketsHMap;
	}

	public void run() {
		String[] splitter; //this is used for splitting the incoming message to sections
		try{
			InputStream is = dataSocket.getInputStream(); //get the incoming stream
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			
			OutputStream os = dataSocket.getOutputStream(); //get the outgoing stream
			PrintWriter out = new PrintWriter(os,true);
			
			String clientName = in.readLine(); //read the client name first (part of the communication protocol)
			
			dataSocketsHMap.put(clientName, dataSocket); //store the name of the client and the corresponding dataSocket in the dataSocketsHMap
			mainWindow.addClient(clientName);
			sendNewUserToConnClients(clientName);
			
			String msg = in.readLine(); //read first message
			while(!msg.equals("!END")) { //read until connection is closed
				splitter = msg.split("!~"); //now splitter[0] holds the receiver's name and splitter[1] holds the message
				if(msg.contains("!ALL")) {
					for(Socket socket: dataSocketsHMap.values()) {
						if(socket != dataSocket) {
					    	if(!socket.isClosed()) {
						    	os = socket.getOutputStream();
								out = new PrintWriter(os,true);
								out.println("From ["+ clientName +"] to ALL<< " + splitter[1]);
					    	}
						}
				    }
				}
				else {
					os = dataSocketsHMap.get(splitter[0]).getOutputStream(); //get the receiver's outgoing stream
					out = new PrintWriter(os,true);
					out.println("From ["+ clientName +"]<< " + splitter[1]); //write on the receiver's outgoing stream the name
																		  	 //of the sender and the message
				}
				msg = in.readLine(); //read next message
			}
			
			removeUserFromConnClients(clientName);
			dataSocketsHMap.remove(clientName);
			mainWindow.closeConnectionOfClient(clientName); //update the GUI to remove the disconnected client
			dataSocket.close();
		}catch(IOException e){
			JOptionPane.showMessageDialog(mainWindow, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			//e.printStackTrace();
		}
	}
	
	private void sendNewUserToConnClients(String name) throws IOException {
		//send the new client's name to all connected clients
		OutputStream os;
		PrintWriter out;
	    for(Socket socket: dataSocketsHMap.values()) {
	    	if(!socket.isClosed()) {
		    	os = socket.getOutputStream();
				out = new PrintWriter(os,true);
				out.println("!ADD_USER!~" + name);
	    	}
	    }
	}
	
	private void removeUserFromConnClients(String name) throws IOException {
		//send the client's name to all connected clients
		OutputStream os;
		PrintWriter out;
	    for(Socket socket: dataSocketsHMap.values()) {
	    	if(!socket.isClosed()) {
		    	os = socket.getOutputStream();
				out = new PrintWriter(os,true);
				out.println("!REMOVE_USER!~" + name);
	    	}
	    }
	}
		
}
