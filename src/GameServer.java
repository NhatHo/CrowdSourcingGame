/**
 * @Author: Nhat Ho
 * The main server which listens to all player requests
 * Always run until the process is killed by user
 */
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameServer {
	private DatagramPacket receivePacket, sendPacket;
	private DatagramSocket receiveSocket, sendSocket;
	private GameDB gameList;
	private WordList questionList;
	private ArrayList<Integer> gameIdList = new ArrayList<Integer>();
	private GameQueue requestQueue;
	private AtomicBoolean shutDown = new AtomicBoolean(false);
	private BufferManager manager;
	private Merger merge;
	/**
	 * Constructor of game server, set up a server with given port
	 * @param port		Port to set up server, taken from user input
	 */
	public GameServer (int port)
	{
		try {
			// Bind a datagram socket to given on the local host machine. 
			// This socket will be used to receive UDP Datagram packets.
			receiveSocket = new DatagramSocket(port);
			receiveSocket.setSoTimeout(1000);
			sendSocket = new DatagramSocket();
			gameList = new GameDB();
			questionList = new WordList();
			requestQueue = new GameQueue();
			manager = new BufferManager ();
			merge = new Merger (manager, questionList, shutDown);
			merge.start();
			spawnControllers();
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	
	public GameServer (int port, int controller1, int controller2) {
		try {
			// Bind a datagram socket to given on the local host machine. 
			// This socket will be used to receive UDP Datagram packets.
			receiveSocket = new DatagramSocket(port);
			receiveSocket.setSoTimeout(1000);
			sendSocket = new DatagramSocket();
			gameList = new GameDB();
			questionList = new WordList();
			requestQueue = new GameQueue();
			manager = new BufferManager ();
			merge = new Merger (manager, questionList, shutDown);
			merge.start();
			spawnControllersAutomated (controller1, controller2);
		} catch (SocketException se) {
			se.printStackTrace();
			System.exit(1);
		}
	}
	/**
	 * Destructor to clear the server
	 */
	public void finalize () {
		receiveSocket.close();
		sendSocket.close();
	}
	/**
	 * Distribute the work to functions according to user request
	 * @param request		The user request string
	 */
	public void processRequest (String request) {
		if (request.matches("3 (\\d+) (\\d+)")) {
			String[] elements = request.split(" ");
			int requestRoom = Integer.parseInt(elements[2].trim());
			try {
				if (gameIdList.contains(requestRoom) && gameList.getNoOfPlayers(requestRoom) != 0) {
					send ("wait", Integer.parseInt(elements[1].trim()));
					joinGame (elements[1].trim() + " " + elements[2].trim());
				} else {
					send ("wrong", Integer.parseInt(elements[1].trim()));
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			int userRequest = Integer.parseInt(request);
			switch (userRequest) {
			case 1: 
				createNewGame();
				break;
			case 2: 
				returnGameList();
				break;
			default:
				break;			
			}
		}
	}
	
	/**
	 * Spawn 2 controllers thread (pre-threaded) when the server gets start up
	 */
	private void spawnControllers () {
		GameController newGameController1 = new GameController(gameList, questionList, requestQueue, shutDown, manager);
		int newGameControllerSocket1 = newGameController1.receiveSocket.getLocalPort();
		if (gameList.addNewGame(newGameControllerSocket1)) {
			gameIdList.add(newGameControllerSocket1);
			newGameController1.start();
		}
		GameController newGameController2 = new GameController(gameList, questionList, requestQueue, shutDown, manager);
		int newGameControllerSocket2 = newGameController2.receiveSocket.getLocalPort();
		if (gameList.addNewGame(newGameControllerSocket2)) {
			gameIdList.add(newGameControllerSocket2);
			newGameController2.start();
		}
	}
	/**
	 * Automatically spawn 2 Controllers with known ports ---> Used for automation
	 * @param controller1			Port for COntroller 1
	 * @param controller2			Port for controller 2
	 */
	private void spawnControllersAutomated (int controller1, int controller2) {
		GameController newGameController1 = new GameController(controller1, gameList, questionList, requestQueue, shutDown, manager);
		if (gameList.addNewGame(controller1)) {
			gameIdList.add(controller1);
			newGameController1.start();
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		GameController newGameController2 = new GameController(controller2, gameList, questionList, requestQueue, shutDown, manager);
		if (gameList.addNewGame(controller2)) {
			gameIdList.add(controller2);
			newGameController2.start();
		}
	}
	/**
	 * Listen to the 2nd request from user, the 2nd packet will contain the information of player port
	 * And gameID
	 */
	public void joinGame(String userRequest) {
		try {
			requestQueue.addJoiner(userRequest);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Create new game by adding the requester into creator queue
	 */
	public void createNewGame () {
		try {
			requestQueue.addCreator(receivePacket.getPort());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Listener of server, keep on listening non-stop to any request
	 */
	public void listener()
	{
		// Construct a DatagramPacket for receiving packets up 
		// to 100 bytes long (the length of the byte array).
		
		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		
		new Thread(){
			public void run(){
				System.out.println("Enter 'quit' to quit");
				@SuppressWarnings("resource")
				Scanner scanner = new Scanner(System.in);
				String message;
				while (!shutDown.get()){
					message = scanner.nextLine();
					if (message.trim().toLowerCase().equals("quit")){
						shutDown.set(true);
					}
				}

			}
		}.start();
		
		while (!shutDown.get()) {
			// Block until a datagram packet is received from receiveSocket.
			try {         
				receiveSocket.receive(receivePacket);
			} catch (SocketTimeoutException e) {
				continue;
			} catch (IOException e) {  e.printStackTrace(); System.exit(1); }
			
			String userRequest = new String(receivePacket.getData(),0,receivePacket.getLength());
			if (userRequest.equals("connect")) {
				send ("allowed", receivePacket.getPort());
			} else {
				processRequest(userRequest);
			}
		}
		System.out.println ("Server is closed");
	}
	/**
	 * Send messages to player
	 * @param s				The message to be sent to player 
	 * @param playerPort	The socket which the player is using to send and receive
	 */
	public void send(String s, int playerPort)
	{
		try {
			// Java stores characters as 16-bit Unicode values, but 
			// DatagramPackets store their messages as byte arrays.
			byte msg[] = s.getBytes();
			sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getByName("127.0.0.1"), playerPort);
			sendSocket.send(sendPacket);      
		}
		catch (UnknownHostException e1)  { e1.printStackTrace(); System.exit(1);}
		catch (IOException e2) { e2.printStackTrace(); System.exit(0);}
	}
	/**
	 * Combine and send a list of available game rooms to player
	 */
	public void returnGameList () {
		String existingGame = "";
		for (int i = 0; i < gameIdList.size(); i++) {
			try {
				if (gameList.getNoOfPlayers(gameIdList.get(i)) != 0) {
					existingGame += "Game Room: " + gameIdList.get(i) + "\n";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		send(existingGame, receivePacket.getPort());
	}
	
	
	/**
	 * Main program of server. 
	 * Create object of UserInterface, and set up the server accordingly.
	 * Execute player requests
	 * @param args
	 */
	public static void main(String[] args){
		UserInterface setUpPort = new UserInterface();
		GameServer server;
		if (args.length != 0) {
			server = new GameServer (Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		} else {
			int port = setUpPort.getPort();
			server = new GameServer (port);
		}
		server.listener();
		server.finalize();
	}

}
