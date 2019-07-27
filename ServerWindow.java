import java.util.ArrayList;

import javax.swing.*;

public class ServerWindow extends JFrame{

	private JLabel clientCounterLabel;
	private JPanel usersPanel;
	private JPanel boxPanel;
	private static int clientCounter;
	private static ArrayList<JLabel> clientLabels;
	
	public ServerWindow() {
		clientCounter = 0;
		clientLabels = new ArrayList<JLabel>();
		
		/*initialize frame*/
		this.setTitle("Swing Chat Server");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300, 400);
        
        /*initialize panel*/
        boxPanel = new JPanel();
        boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.PAGE_AXIS));
        boxPanel.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        
        /*initialize panel components*/
        //the 'Port' label
        JLabel portLabel = new JLabel("Listening on port " + ChatServer.PORT);
        portLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        //the 'Connected Clients' label
        clientCounterLabel = new JLabel("Connected Clients: 0");
        clientCounterLabel.setAlignmentX(CENTER_ALIGNMENT);
        
        //the panel that holds the user names
        usersPanel = new JPanel();
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.PAGE_AXIS));
        usersPanel.setAlignmentX(CENTER_ALIGNMENT);
        
        
        /*add components to the panel*/
        boxPanel.add(portLabel);
        boxPanel.add(clientCounterLabel);
        boxPanel.add(usersPanel);
        
        /*add panel to frame*/
        this.setContentPane(boxPanel);
        this.setLocationRelativeTo(null); //centers the window on the screen
        this.setVisible(true);
	}
	
	public void closeConnectionOfClient(String clientName) {
		JLabel tempLabel = null;
		/*update the GUI to remove the disconnected client*/
		clientCounter--;
		clientCounterLabel.setText("Connected Clients: " + clientCounter);
		for(JLabel label: clientLabels) {
			if(label.getText().equals(clientName)) {
				usersPanel.remove(label);
				usersPanel.revalidate();
				usersPanel.repaint();
				tempLabel = label; //the tempLabel is used in order to avoid ConcurrentModificationException
			}
		}
		if(tempLabel != null)
			clientLabels.remove(tempLabel);
	}
	
	public void addClient(String name) {
		clientCounter++;
		clientCounterLabel.setText("Connected Clients: " + clientCounter);
		
		clientLabels.add(new JLabel(name));
		clientLabels.get(clientLabels.size()-1).setAlignmentX(CENTER_ALIGNMENT); //center label in the panel
		usersPanel.add(clientLabels.get(clientLabels.size()-1)); //add label to the panel
		boxPanel.add(usersPanel); //add the users panel to the main panel
		boxPanel.revalidate();
		boxPanel.repaint();
		
		clientCounterLabel.setText("Connected Clients: " + clientCounter);
	}
		
	public boolean nameAlreadyExists(String name) {
		for(JLabel label: clientLabels) {
			if(label.getText().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	public ArrayList<String> getClientNames() {
		ArrayList<String> clientNames = new ArrayList<String>();
		for(JLabel clientLabel: clientLabels)
			clientNames.add(clientLabel.getText());
		return clientNames;
	}
	
	public JLabel getClientCounterLabel() {
		return clientCounterLabel;
	}
	
	public int getClientCounter() {
		return clientCounter;
	}
}
