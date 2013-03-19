/**
 * @Author: Nhat Ho
 * THis class includes all interaction with user, common class among other classes
 */
import java.util.*;


public class UserInterface {	
	private Scanner scan;
	/**
	 * Constructor, declare the scanner
	 */
	public UserInterface () {
		scan = new Scanner (System.in);
	}
	/**
	 * De-structor, close the scanner
	 */
	public void finalize () {
		scan.close();
	}
	/**
	 * Get the port of the server to communicate
	 * @return				Server Port
	 */
	public int getPort () {
		int port;
		System.out.println(String.format("%80s", " ").replaceAll(" ", "_"));
		System.out.println(String.format("%80s", " ").replaceAll(" ", "_"));
		System.out.println ("WELCOME TO MIND-BOGGLING GAME");
		System.out.print ("Please, Enter the Server Port ----> ");
		port = getInteger ();
		System.out.println(String.format("%80s", " ").replaceAll(" ", "+"));
		return port;
	}
	/**
	 * Display the menu from which the user can choose
	 */
	public void displayMenu () {
		String [] menu = {"Create New Game", "View Existing Game(s)", "Join An Existing Game"}; 
		// method used to display menu options
		System.out.println(String.format("%80s", " ").replaceAll(" ", "="));
		System.out.println ("M I N D - B O G G L I N G    G A M E");
		System.out.println(String.format("%80s", " ").replaceAll(" ", "-"));
		for (int i = 0; i < menu.length; i++) {
		    System.out.println("[" + (i + 1) + "] " + menu[i]);
		}
		System.out.println("[0] Exit");
		System.out.println(String.format("%80s", " ").replaceAll(" ", "-"));
	}
	/**
	 * Get user choice from option list
	 * @return				User Option
	 */
	public int getUserOption() {
		int choice = -1;
		boolean goodChoice = false;
		// get user choice and return it
	    while (!goodChoice) {
	    	displayMenu();
	    	System.out.print ("Your Choice ---> ");
	    	choice = getInteger();
	    	if (choice > 3 || choice < 0) {
	 	    	System.out.println ("The option you chose is NOT VALID!!!");
	    	} else {
	    		goodChoice = true;
	    	}
	    }
	    return choice;
	}
	/**
	 * Scan for any integer, continue the loop until user input an integer
	 * @return			user input value
	 */
	public int getInteger () {
		int input = -1;
	    boolean valid = false;
	    String prompt;
	    while (!valid) {
	    	try {
	    		prompt = scan.nextLine();
	    		input = Integer.parseInt(prompt);
	        	valid = true;
	    	} catch (NumberFormatException e) {
	    		System.out.println("*** INTEGER ONLY PLEASE ***");
	    	}
	    }
	    return input;
	}	
	/**
	 * Get the gameroom which user wants to join in
	 * @return game room ID
	 */
	public int getUserGameId () {
		int gameId;
		System.out.println(String.format("%80s", " ").replaceAll(" ", "_"));
		System.out.print ("Game room to join ---> ");
		gameId = getInteger();
		return gameId;
	}
	/**
	 * Get user answers for a specific question
	 * @return the answer
	 */
	public String getUserAnswer () {
		String answer;
		System.out.print("Your Answer ---> ");
		answer = scan.next();
		return answer;
	}
}
