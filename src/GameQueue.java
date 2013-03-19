import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class GameQueue {
	private static final int MAXJOINER = 100;
	private static final int MAXCREATOR = 20;
	protected BlockingQueue<String> joinGameQueue = new ArrayBlockingQueue<String>(100);
	protected BlockingQueue<Integer> createGameQueue = new ArrayBlockingQueue<Integer>(20);
	
	public GameQueue () {}
	/**
	 * Add the player who request to join an exisiting game into the waiting queue
	 * @param request					The requester port and gameroomID (in form of String)
	 * @throws InterruptedException		If timed out before successfully adding
	 */
	public boolean addJoiner (String request) throws InterruptedException {
		if (joinGameQueue.size() < MAXJOINER) {
			joinGameQueue.offer(request, 300, TimeUnit.MICROSECONDS);
			synchronized (joinGameQueue) {
				joinGameQueue.notify();
			}
			return true;
		}
		return false;
	}
	/**
	 * Add the player who requests to create new game into the waiting queue
	 * @param request					The requester
	 * @throws InterruptedException		If timed out before adding
	 */
	public boolean addCreator (int request) throws InterruptedException {
		if (createGameQueue.size() < MAXCREATOR) {
			if(createGameQueue.offer(new Integer(request), 300, TimeUnit.MICROSECONDS)) {
				synchronized (createGameQueue) {
					createGameQueue.notify();
				}
				return true;
			}
		} 
		return false;
	}
	/**
	 * Get and remove the first element in the queue to join specific game
	 * @return							The first element in this queue
	 * @throws InterruptedException		Throw exception if timed out
	 */
	public String getJoiner (int flag, int gameId) throws InterruptedException {
		synchronized (joinGameQueue) {
			while (flag == 1 && joinGameQueue.size() == 0) {
				joinGameQueue.wait(5000);
			}
		}	
		if (joinGameQueue.size() > 0 && examineJoiner (gameId)) {
			String[] temp = joinGameQueue.poll(300, TimeUnit.MICROSECONDS).split(" ");
			return temp[0].trim();
		}
		return "";
	}
	/**
	 * Get and remove the first element in the queue to create game
	 * @return					The first element in queue
	 * @throws InterruptedException		Throw exception if timed out
	 */
	public int getCreator () throws InterruptedException {
		synchronized (createGameQueue) {
			while (createGameQueue.size() == 0) {
				createGameQueue.wait();
			}
		}
		if (createGameQueue.size() > 0) {
			return createGameQueue.poll(300, TimeUnit.MICROSECONDS);
		}
		return -1;
	}
	/**
	 * Check to see which game room that user wants to join
	 * @param gameId			The game ID of the game room
	 * @return					true if the player wants to join the game room that is checking
	 */
	public boolean examineJoiner (int gameId) {
		if (joinGameQueue.size() > 0) {
			String joiner = joinGameQueue.peek();
			if (joiner.length() > 0) {
				String[] elements = joiner.split(" ");
					if (Integer.parseInt(elements[1]) == gameId) {
						return true;
					}
			}
			// If the head element doesn't belong to this gameroom, notify so next gameroom can check
			synchronized (joinGameQueue) {
				joinGameQueue.notify();
			}
		}
		return false;
	}
}
