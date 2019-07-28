import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ClientWindow extends JFrame{

	private JTextArea textArea;
	private DefaultListModel<String> clientsListModel;
	
	public ClientWindow(Socket dataSocket, String name) {
		/*initialize frame*/
		this.setTitle(name);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500, 300);
        
        /*initialize panel*/
        JPanel borderPanel = new JPanel();
        borderPanel.setLayout(new BorderLayout());
        borderPanel.setBorder(BorderFactory.createEmptyBorder(10,10,2,10));
        
        /*initialize panel components*/
        //the 'Chat Area:' label
        JLabel connectionLabel = new JLabel("Chat Area:");
        connectionLabel.setAlignmentX(JFrame.CENTER_ALIGNMENT);
        
        //the text area of the chat
        textArea = new JTextArea();
        textArea.append("Connected to main server.\nChoose a client from the list and type your message:\n");
        textArea.setEditable(false);
        textArea.setFocusable(false);
        
        //the scroll pane (for the text area to be scrollable)
        JScrollPane textAreaScrollPane = new JScrollPane(textArea);
        
        //the list of the connected clients
        clientsListModel = new DefaultListModel<String>();
        clientsListModel.addElement("<ALL>");
        JList<String> clientsList = new JList<String>(clientsListModel); 
        clientsList.setSelectedIndex(0);
        
        //the scroll pane (for the list to be scrollable)
        JScrollPane clientsScrollPane = new JScrollPane(clientsList);
        clientsScrollPane.setPreferredSize(new Dimension(100, 100));
        
        //the panel containing the combobox and textfield
        JPanel messagePanel = new JPanel(new BorderLayout());
        
        //the combobox that allows the client to choose the receiver
        JTextArea sendToTxtArea = new JTextArea();
        sendToTxtArea.setText("Send to <ALL>: ");
        sendToTxtArea.setEditable(false);
        sendToTxtArea.setFocusable(false);
        
        //the text field that allows the user to type his messages
        JTextField textField = new JTextField();
        
        //adding the combobox and textfield to the message panel
        messagePanel.add(sendToTxtArea, BorderLayout.LINE_START);
        messagePanel.add(textField, BorderLayout.CENTER);
        
        
        /*add components to panel*/
        borderPanel.add(connectionLabel, BorderLayout.PAGE_START);
        borderPanel.add(textAreaScrollPane, BorderLayout.CENTER);
        borderPanel.add(clientsScrollPane, BorderLayout.AFTER_LINE_ENDS);
        borderPanel.add(messagePanel, BorderLayout.PAGE_END);
        
        /*add panel to frame*/
        this.setContentPane(borderPanel);
        this.setVisible(true);
        
        
        /*add listeners*/
        //listens for the change of the list option by the user
        clientsList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent i) {
				if(clientsList.getSelectedValue() == null)
					clientsList.setSelectedIndex(0);
				sendToTxtArea.setText("Send to " + clientsList.getSelectedValue() + ": ");
				textField.requestFocus();
			}
        });
        
        //listens for the enter button on the text field
        textField.addKeyListener(new KeyAdapter() { 
			public void keyReleased(KeyEvent e) {
		        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		        	if(dataSocket.isConnected()) {
						try {
							OutputStream os = dataSocket.getOutputStream();
							PrintWriter out = new PrintWriter(os,true);
							if(clientsList.getSelectedIndex() == 0) { //send to ALL
								out.println("!ALL!~" + textField.getText());
								textArea.append("To ALL>> "+ textField.getText()+"\n");
							}
							else {
								out.println(clientsList.getSelectedValue() + "!~" + textField.getText());
								textArea.append("To ["+ clientsList.getSelectedValue() +"]>> "+ textField.getText()+"\n");
							}
							textField.setText("");
						} catch (IOException e1) {
							JOptionPane.showMessageDialog(ClientWindow.this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
							//e1.printStackTrace();
						}
					}
		        }
		      }
		});
        
        //listens for the closing of the client window
        this.addWindowListener(new WindowAdapter() {  
            @Override
            public void windowClosing(WindowEvent e) {
            	try {
            		OutputStream os = dataSocket.getOutputStream();
					PrintWriter out = new PrintWriter(os,true);
					out.println("!END"); //escape sequence
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(ClientWindow.this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
                e.getWindow().dispose();
            }
        });
	}
	
	public JTextArea getTextArea() {
		return textArea;
	}
	
	public DefaultListModel<String> getClientsListModel() {
		return clientsListModel;
	}
}
