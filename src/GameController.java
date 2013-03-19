/**
 * @author Nhat Ho
 * This class is the game controller which controls the questions and user answers
 */
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameController extends Thread {

	private DatagramPacket sendPacket, receivePacket;
	DatagramSocket sendSocket, receiveSocket;
	private ArrayList <Integer> playerList = new ArrayList<Integer>();
	private List<Integer> waitList = Collections.synchronizedList(new ArrayList<Integer>());
	private GameDB games;
	private WordList questionList;
	private GameQueue requestQueue;
	private AtomicBoolean shutDown;
	private BufferManager manager;
	private HashMap<Integer, Integer> scoreBoard = new HashMap<Integer, Integer>();
	private int gameId; // game ID of this game room
	/**
	 * Constructor
	 * @param firstPlayer		The player who created this game room
	 */
	public GameController(GameDB games, WordList questionList, GameQueue requestQueue, AtomicBoolean shutDown, BufferManager manager) {
		try {
			// Bind a datagram socket to any available port on the local host machine
			// This socket will be used to // send UDP Datagram packets.
			sendSocket = new DatagramSocket();
			
			// Bind a datagram socket to any available host on local machine
			// This socket will be used to receive UDP Datagram packets.
			receiveSocket = new DatagramSocket();
			gameId = receiveSocket.getLocalPort();
			receiveSocket.setSoTimeout(30000);
			this.games = games;
			this.questionList = questionList;
			this.requestQueue = requestQueue;
			this.shutDown = shutDown;
			this.manager = manager;
		} catch (SocketException se) {
			System.out.println ("CANNOT find any available socket");
			se.printStackTrace();
			System.exit(1);
		}
	}
	public GameController(int port, GameDB games, WordList questionList, GameQueue requestQueue, AtomicBoolean shutDown, BufferManager manager) {
		try {
			// Bind a datagram socket to any available port on the local host machine
			// This socket will be used to // send UDP Datagram packets.
			sendSocket = new DatagramSocket();
			
			// Bind a datagram socket to any available host on local machine
			// This socket will be used to receive UDP Datagram packets.
			receiveSocket = new DatagramSocket(port);
			gameId = receiveSocket.getLocalPort();
			receiveSocket.setSoTimeout(120000);
			this.games = games;
			this.questionList = questionList;
			this.requestQueue = requestQueue;
			this.shutDown = shutDown;
			this.manager = manager;
		} catch (SocketException se) {
			System.out.println ("CANNOT find any available socket");
			se.printStackTrace();
			System.exit(1);
		}
	}
	/**
	 * De-structor, when garbage collector comes in, or manually invoke
	 */
	public void finalize () {
		sendSocket.close();
		receiveSocket.close();
		playerList.clear();
	}
	/**
	 * Update scoreboard in a simple manner, the first one answer will get the full point (whichever the size of playerList is)
	 * Then the score will decrease as more answer comes in
	 * @param id			The user port, as playerID
	 * @param turn			The turn of player in term of arriving answer
	 */
	private void keepScore(int id, int turn) {
		if (!scoreBoard.containsKey(id)) {
			scoreBoard.put(id, playerList.size() - turn);
		} else {
			scoreBoard.put(id, scoreBoard.get(id) + playerList.size() - turn);
			System.out.println ("Player: " + id +", turn: " + turn + ", score: " + (scoreBoard.get(id) + playerList.size() - turn));
		}
	}
	private void displayScore(int player) {
		String result = "++++++++++++++++++++++++\n Your score is: " + scoreBoard.get(player) + "\n++++++++++++++++++++++++\n";
		for (Integer key : scoreBoard.keySet()) {
			result += key + " -----> "  + scoreBoard.get(key) + "\n";
		}
		result += "++++++++++++++++++++++++\n";
		send (result, player);
	}
	/**
	 * Send a message to greet the first player
	 * @param playerPort
	 */
	private void greetingPlayer (int playerPort) {
		send (Integer.toString(gameId), playerPort);
		String greetings = "Welcome to game room: " + gameId + "\nPlease wait for another player ...\n";
		send (greetings, playerPort);
	}
	/**
	 * Get the first player port from the request
	 */
	public void receiveFirstPlayer () { 
		int firstPlayerPort;
	    try {
			firstPlayerPort = requestQueue.getCreator();
			playerList.add(firstPlayerPort);
		    greetingPlayer (firstPlayerPort);
		    updatePlayer ();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Listening method of game controller
	 * Listen to all incoming packets from user and act upon it accordingly
	 * @param questionIndex				The index of question that controller is collecting answer
	 */
	private void receive(int questionIndex) {
		// Construct a DatagramPacket for receiving packets up 
		// to 100 bytes long (the length of the byte array).
		String response = "";
		int emptyBuffer;
		int numOfResponse = 0;
		int answerTurn = 0; // this is different from numOfResponse because numOfResponse contains the "quit" statement
		byte data[] = new byte[100];
		receivePacket = new DatagramPacket(data, data.length);
		ArrayList <String> answer = new ArrayList <String>();
		answer.add(questionList.getFile(questionIndex));
		while (!shutDown.get()) {
			// Block until a datagram packet is received from receiveSocket.
			int players = getNumberOfPlayer();
			try {         
				receiveSocket.receive(receivePacket);
			} catch (SocketTimeoutException e) {
				break; // catch timeout of controller
			} catch (IOException e) {  e.printStackTrace(); System.exit(1); }
			// Process the received datagram.
			// For debug and keeping track purposes
			System.out.println("Server: Received from "+  receivePacket.getAddress() + ":" +
								receivePacket.getPort() + " - " + 
								new String(receivePacket.getData(),0,receivePacket.getLength()) + 
								"(" + receivePacket.getLength() + ")" );
			response = new String(receivePacket.getData(),0,receivePacket.getLength());
			// If user choose to quit, remove him/her from the list and proceed
			if (response.toLowerCase().equals("quit")) {
				int quitter = receivePacket.getPort();
				displayScore(quitter);
				playerList.remove(new Integer(quitter));
				updatePlayer();
			// If everything is going well, store the answer and count the number of answer
			} else {
				answer.add(response.toLowerCase());
				keepScore(receivePacket.getPort(), answerTurn);
				answerTurn ++;
			}
			numOfResponse ++;
			if (numOfResponse >= players) {
				break;
			}	
		}
		do {
			emptyBuffer = manager.getEmptyBuffer();
			if (emptyBuffer != -1) {
				manager.putFullBuffer(emptyBuffer, answer);
			}
		} while (emptyBuffer == -1);
		
	}
	public String getSecondPlayer () {
		String secondPlayer = "";
		while (secondPlayer.length() == 0) {
			try {
				secondPlayer = requestQueue.getJoiner(1, gameId);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		playerList.add(Integer.parseInt(secondPlayer));
		updatePlayer();
		return secondPlayer;
	}
	/**
	 * Send any string to user according to playerPort
	 * @param s				The String to be sent
	 * @param playerPort	The Receiver port
	 */
	private void send (String s, int playerPort) {
		try {
			// Java stores characters as 16-bit Unicode values, but 
			// DatagramPackets store their messages as byte arrays.
			byte msg[] = s.getBytes();
			sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getByName("127.0.0.1"), playerPort);
			/*System.out.println("Client: Sending to " + sendPacket.getAddress() + ":" +
								sendPacket.getPort() + " - " + new String(sendPacket.getData()) + 
								"(" + sendPacket.getLength()+ ")");*/
			sendSocket.send(sendPacket);
			// System.out.println("Server: Packet sent.");         
		}
		catch (UnknownHostException e1)  { e1.printStackTrace(); System.exit(1);}
		catch (IOException e2) { e2.printStackTrace(); System.exit(0);}
	}
	public void updatePlayerList () {
		if (waitList.size() > 0) {
			playerList.addAll(waitList);
			waitList.clear();
			updatePlayer();
		}
	}
	/**
	 * Start the game by continuously going through the question list
	 * If any player drop out, and there is only 1 player left, pause the game until 2nd player join in
	 */
	private void startGame () {
		int firstQuestion = 0;
		for (int i = 0; i < questionList.getSize(); i++) {
			updatePlayerList();
			System.out.println ("Starting next Round");
			int players = getNumberOfPlayer(); // continuously check the number of players
			if (players == 0) {
				break;
			} else if (players == 1) {
				send ("GAME PAUSED", playerList.get(0));
				getSecondPlayer();
			}
			for (int j = 0; j < players; j++) {
				if (firstQuestion == 0) {
					send ("+++++ ROUND STARTS +++++\n" + "Question: " + questionList.getQuestion(i), playerList.get(j));
					firstQuestion ++;
				} else {
					send ("----- ROUND ENDED -----\n" + "+++++ NEW ROUND STARTS +++++\n" + "Question: " + questionList.getQuestion(i), playerList.get(j));
				}
				players = getNumberOfPlayer();
			}
			receive(i);
		}
	}
	/**
	 * Get number of player in current list
	 * @return 			Number of players
	 */
	public int getNumberOfPlayer () {
		try {
			return games.getNoOfPlayers(gameId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * Add new player into the list, and update the total amount of players in GameDB
	 */
	public void updatePlayer () {
		try {
			games.updateNoOfPlayers(gameId, playerList.size());
		} catch (Exception e) {
			System.out.println (e.getMessage());
		}
	}
	
	/**
	 * Run method of thread
	 */
 	public void run () {
 		Observer newObserver = new Observer (waitList, shutDown, gameId, requestQueue);
 		newObserver.start();
 		while (!shutDown.get()) {
 			while (getNumberOfPlayer() == 0) {
 	 			receiveFirstPlayer();
 	 		}
 	 		startGame ();
 	 		for (int i = 0; i < getNumberOfPlayer(); i++) {
 	 			send ("##### GAME OVER #####", playerList.get(i));
 	 			displayScore(playerList.get(i));
 	 		}
 	 		for (int i = 0; i < waitList.size(); i++){
 	 			send ("##### GAME OVER #####", waitList.get(i));
 	 		}
 	 		playerList.clear();
 	 		System.out.println (playerList.toString());
 	 		updatePlayer();
 		}
 		try {
			newObserver.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
 		System.out.println ("GameRoom: " + gameId + " has been closed");
 	}
}

class Observer extends Thread {
	private AtomicBoolean shutDown;
	private List<Integer> waitList;
	private int gameId;
	private GameQueue requestQueue;
	public Observer (List<Integer> waitList, AtomicBoolean shutDown, int gameId, GameQueue requestQueue) {
		this.waitList = waitList;
		this.shutDown = shutDown;
		this.gameId = gameId;
		this.requestQueue = requestQueue;
	}
	
	public void run() {
		String newPlayer = "";
		while (!shutDown.get()) {
			if (requestQueue.examineJoiner(gameId)) {
				try {
					newPlayer = requestQueue.getJoiner(0, gameId);
					waitList.add(Integer.parseInt(newPlayer));
					break;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
