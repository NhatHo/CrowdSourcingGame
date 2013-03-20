/**
 * @Author: Nhat Ho
 * The player class, instantiate a player and interact with user
 */
import java.io.*;
import java.net.*;

public class PlayerAutomated {

	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;
	private int mainServerPort = -1;
	private int controllerPort = -1;
	private int index = 0;
	/**
	 * Constructor
	 * Binds player to any available host and communicate with server
	 */
	public PlayerAutomated(String[] inputs)
	{
		try {
			// Bind a datagram socket to any available port on the local host machine. 
			sendReceiveSocket = new DatagramSocket();
			sendReceiveSocket.setSoTimeout(30000); // Set timeout for the socket (2 minutes)
			mainServerPort = 5000;
			instantiateConnection();
			createAction (inputs); 
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
	 * Automatically act upon the descriptive request string in constructor
	 * @param action				The action array to act on	
	 */
	public void createAction (String[] action) {
		UserInterface userChoice = new UserInterface ();
		boolean displayFlag = false;
		for (index = 0; index < action.length; index++) {
			if (!displayFlag){
				userChoice.displayMenu();
				System.out.print ("Your Choice ---> ");
			}
			displayFlag = false;
			switch (action[index]) {
			case "0":
				System.out.println ("0");
				break;
			case "1":
				System.out.println ("1");
				send("1", mainServerPort);
				controllerPort = Integer.parseInt(receiveData());
				System.out.println (receiveData()); // get the greetings from GameController
				reply (action);
				break;
			case "2":
				System.out.println ("2");
				send("2", mainServerPort);
				String gameList = receiveData();
				if (gameList.length() != 0) {
					System.out.println ("GAME LIST: \n" + gameList);
				} else {
					System.out.println ("NO AVAILABLE GAME ROOM AT THE MOMENT");
				}
				break;
			case "3":
				System.out.println ("3");
				send("2", mainServerPort); // check the list of available game room
				String tempGameList = receiveData();
				if (tempGameList.length() != 0) { // prevent user try to connect to not available game room
					index++;
					controllerPort = Integer.parseInt(action[index]);
					System.out.println(String.format("%80s", " ").replaceAll(" ", "_"));
					System.out.println ("Game room to join ---> " + controllerPort);
					establishGameConnection();
					reply (action);
				} else {
					System.out.println ("NO AVAILABLE GAME ROOM AT THE MOMENT");
				}
				break;
			case "pause":
				index++;
				try {
					Thread.sleep(Integer.parseInt(action[index]));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				displayFlag = true;
				break;
			default: 
				break;
			}
		}
	}
	/**
	 * Replay to the question sent by server
	 * @param action			The action string contains answers
	 */
	public void reply (String[] action) {
		String userAnswer = "";
		while (!userAnswer.toLowerCase().equals("quit")) {
			index++;
			String message = receiveData(); // Round Start and Question
			System.out.println (message);
			if (message.matches("(\\W)* GAME OVER (\\W)*")) {
				index--;
				break;
			}
			if (message.matches("wrong")) {
				System.out.println ("The Game ID you used was INVALID, CHECK AGAIN!!!");
			}
			if (message.equals ("GAME PAUSED")) {
				System.out.println ("PLEASE WAIT ...");
				index--;
				continue;
			} else if (message.equals("wait")) {
				System.out.println ("Please wait till ROUND is over");
				index--;
				continue;
			} else {
				userAnswer = action[index];
				if (action[index].equals("pause")) {
					index++;
					try {
						Thread.sleep(Integer.parseInt(action[index]));
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					index++;
					userAnswer = action[index];
				}
				send(userAnswer, controllerPort);
				System.out.println ("Your Answer ---> " + userAnswer);
			}			
		}
		String message = receiveData(); // Get Score from the game
		System.out.println (message);
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
			sendReceiveSocket.send(sendPacket);
			// System.out.println("Client: Packet sent.");         
		}
		catch (UnknownHostException e1)  { e1.printStackTrace(); System.exit(1);}
		catch (IOException e2) { e2.printStackTrace(); System.exit(0);}
	}
	/**
	 * Listen to any incomfullBuffering message or packet
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
	 * Send request to join a game room with game room Id "controllerPort"
	 */
	public void establishGameConnection () {
		send ("3 " + sendReceiveSocket.getLocalPort() + " " + controllerPort, mainServerPort);
	}
	/**			System.out.println ("The value of shutDown in Merger: " + shutDown.get());
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
		String[] inputs = new String[args.length];
		for (int i = 0; i < inputs.length; i++) {
			inputs[i] = args[i];
		}
		PlayerAutomated c = new PlayerAutomated(inputs);
		c.finalize();
		System.out.println ("Player exited");
	}
}