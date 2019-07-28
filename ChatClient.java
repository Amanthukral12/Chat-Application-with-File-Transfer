import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class ChatClient{
	private String name;
	
	public static void main(String[] args) {
		new ChatClient();
	}
	
	public ChatClient() {
		//prompt the user to enter the server's IP and PORT
		String input = JOptionPane.showInputDialog(new JFrame(), "Enter the server's IP and PORT to connect\n(example 192.168.56.1 1234): ", "192.168.56.1 1234");
		if(input == null)
			System.exit(0);
		else {
			Socket dataSocket;
			String[] splitter;
			splitter = input.split(" ");
			try {
				if(splitter.length != 2) {
					JOptionPane.showMessageDialog(new JFrame(), "Wrong input format", "Error", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
				else {
					dataSocket = new Socket(splitter[0], Integer.parseInt(splitter[1]));
					
					//prompt the user to enter his username
					name = JOptionPane.showInputDialog(new JFrame(), "Connection established.\nType your username:", "Username prompt", JOptionPane.INFORMATION_MESSAGE);
					OutputStream os = dataSocket.getOutputStream();
					PrintWriter out = new PrintWriter(os,true);
					out.println(name); //send the name to the server
					
					//the window acts as the SendThread
					ClientWindow clientWindow = new ClientWindow(dataSocket, name); 
					
					//a new thread dedicated to receiving messages
					ReceiveThread receive = new ReceiveThread(dataSocket, clientWindow.getTextArea(), clientWindow.getClientsListModel(), name);
					Thread thread = new Thread(receive);
					thread.start();
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
				//e.printStackTrace();
			}
		}
	}
	
	public String getName() {
		return name;
	}

}

class ReceiveThread implements Runnable{

	private Socket dataSocket;
	private JTextArea textArea;
	private DefaultListModel<String> clientsListModel;
	private String clientName;
	
	public ReceiveThread(Socket soc, JTextArea textArea, DefaultListModel<String> clientsListModel, String clientName){
		dataSocket = soc;
		this.textArea = textArea;
		this.clientsListModel = clientsListModel;
		this.clientName = clientName;
	}
	
	public void run() {
		String[] splitter; //this is used for splitting the incoming message to sections
		try{
			InputStream is = dataSocket.getInputStream(); //get the incoming stream
			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			
			/*reads all the current connected clients (the server writes them in the stream. Part of the communication protocol)*/
			String msg = in.readLine();
			while(!msg.equals("!END_CLIENT_INIT")) {
				clientsListModel.addElement(msg);
				msg = in.readLine();
			}
			
			/*keep reading messages from the incoming stream as long as the client is connected*/
			msg = in.readLine(); //read the first message
			while(msg != null) {
				if(msg.contains("!ADD_USER")) { //special message from the server: a new user has connected
					splitter = msg.split("!~"); //now splitter[1] holds the name of the new client
					if(!splitter[1].equals(clientName)) {
						clientsListModel.addElement(splitter[1]);
						textArea.append("Client [" + splitter[1] + "] has connected\n");
					}
				}
				else if(msg.contains("!REMOVE_USER")) {
					splitter = msg.split("!~"); //now splitter[1] holds the name of the disconnected client
					if(!splitter[1].equals(clientName)) {
						clientsListModel.removeElement(splitter[1]);
						textArea.append("Client [" + splitter[1] + "] has disconnected\n");
					}
				}
				else { //a simple message
					textArea.append(msg+"\n"); //display the message on the client's chat area
				}
				msg = in.readLine();
			} 
			dataSocket.close();
		}catch (IOException e){
			if(e.getMessage().contains("Connection reset")) {
				textArea.append("Server is closed. You are now offline\n");
			}
			else
				JOptionPane.showMessageDialog(new JFrame(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			//e.printStackTrace();
		}
	}
}