/**
 * @Author: Nhat Ho
 * The player class, instantiate a player and interact with user
 */
import java.io.*;
import java.net.*;

public class Player {

	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;
	private int mainServerPort = -1;
	private int controllerPort = -1;
	/**
	 * Constructor
	 * Binds player to any available host and communicate with server
	 */
	public Player()
	{
		try {
			// Bind a datagram socket to any available port on the local host machine. 
			sendReceiveSocket = new DatagramSocket();
			sendReceiveSocket.setSoTimeout(120000); // Set timeout for the socket (2 minutes)
		} catch (SocketException se) {   // Can't create the socket.
			System.out.println ("CANNOT find any available socket");
			se.printStackTrace();
			System.exit(1);
		}
	}
	/**
	 * De-structor 
	 * Close the player socket
	 */
	public void finalize () {
		sendReceiveSocket.close();
	}
	/**
	 * Create packet and send it to server or game controller
	 * @param s					The message or request to send
	 * @param serverPort		The port to send to
	 */
	public void send(String s, int serverPort)
	{
		// Send a DatagramPacket to port 5000 on the same host.
		try {
			// Java stores characters as 16-bit Unicode values, but 
			// DatagramPackets store their messages as byte arrays.
			byte msg[] = s.getBytes();
			sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getByName("127.0.0.1"), serverPort);
			/*System.out.println("Client: Sending to " + sendPacket.getAddress() + ":" +
							sendPacket.getPort() + " - " + new String(sendPacket.getData()) + 
							"(" + sendPacket.getLength()+ ")");*/
			sendReceiveSocket.send(sendPacket);
			// System.out.println("Client: Packet sent.");         
		}
		catch (UnknownHostException e1)  { e1.printStackTrace(); System.exit(1);}
		catch (IOException e2) { e2.printStackTrace(); System.exit(0);}
	}
	/**
	 * Listen to any incoming message or packet
	 * @return message			message or question from server
	 */
	public String receiveData () {
		String message;
		byte data[] = new byte[1000];
		receivePacket = new DatagramPacket(data, data.length);
	    try {
	    	// Block until a datagram is received via sendReceiveSocket.  
	        sendReceiveSocket.receive(receivePacket);
	    } catch (SocketTimeoutException e) {
	    	System.out.println ("TIMED OUT");
	    } catch(IOException e) { e.printStackTrace(); System.exit(1);}
	    message = new String(receivePacket.getData(),0,receivePacket.getLength());
	    return message;
	}
	/**
	 * Get user option, and act upon request
	 * @return option		The choice that user made
	 */
	public int getUserOption () {
		int option = -1;
		UserInterface userChoice = new UserInterface ();
		while (option != 0 && option != 1 && option != 3) {
			option = userChoice.getUserOption();
			switch (option) {
			case 0: break;
			case 1:
				send("1", mainServerPort);
				controllerPort = Integer.parseInt(receiveData());
				System.out.println (receiveData()); // get the greetings from GameController
				//receiveData (); // Filter out the GAME PAUSED message 
				break;
			case 2:
				send("2", mainServerPort);
				String gameList = receiveData();
				if (gameList.length() != 0) {
					System.out.println ("GAME LIST: \n" + gameList);
				} else {
					System.out.println ("NO AVAILABLE GAME ROOM AT THE MOMENT");
				}
				break;
			case 3:
				send("2", mainServerPort); // check the list of available game room
				String tempGameList = receiveData();
				if (tempGameList.length() != 0) { // prevent user try to connect to not available game room
					controllerPort = userChoice.getUserGameId();
					establishGameConnection();
				} else {
					System.out.println ("NO AVAILABLE GAME ROOM AT THE MOMENT");
					option = -1; // Reset the loop
				}
				break;
			default: 
				option = -1; 
				break;
			}
			
		}
		return option;
	}
	/**
	 * Send request to join a game room with game room Id "controllerPort"
	 */
	public void establishGameConnection () {
		send ("3 " + sendReceiveSocket.getLocalPort() + " " + controllerPort, mainServerPort);
	}
	/**
	 * Instantiate the connection to the main GameServer
	 * @return			True if successfully connected, false otherwise
	 */
	public boolean instantiateConnection () {
		String isServer = "connect";
		send (isServer, mainServerPort);
		if (receiveData().equals("allowed")) {
			System.out.println ("You are connected to the GAME SERVER");
			return true;
		} else {
			return false;
		}
	}
	/**
	 * Create a player and getting user request. Let user join game and process the answer
	 * Pause the player, resume the game and invoke all functionalities that Player can use
	 * @param args
	 */
	public static void main(String args[])
	{
		int option = -1;
		Player c = new Player();
		UserInterface getUserChoice = new UserInterface();
		c.mainServerPort = getUserChoice.getPort();
		if (!c.instantiateConnection()) {
			System.out.println ("The Server that you are trying to connect DOES NOT exist, PLEASE TRY AGAIN");
			System.exit(0);
		}
		while (option != 0) {
			String userAnswer = "";
			option = c.getUserOption();
			if (option == 3 || option == 1) {
				while (!userAnswer.toLowerCase().equals("quit")) {
					String message = c.receiveData(); // Round Start and Question
					System.out.println (message);
					if (message.matches("(\\W)* GAME OVER (\\W)*")) {
						break;
					}
					if (message.matches("wrong")) {
						System.out.println ("The Game ID you used was INVALID, CHECK AGAIN!!!");
						break;
					}
					if (message.equals ("GAME PAUSED")) {
						System.out.println ("PLEASE WAIT ...");
						continue;
					} else if (message.equals("wait")) {
						System.out.println ("Please wait till ROUND is over");
					} else {
						userAnswer = getUserChoice.getUserAnswer();
						c.send(userAnswer, c.controllerPort);
					}
				}
				String message = c.receiveData(); // Get Score from the game
				System.out.println (message);
			}
		}
		c.finalize();
		System.out.println ("Player exited");
	}
}