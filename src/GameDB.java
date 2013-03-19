import java.util.HashMap;
import java.util.Collections;
import java.util.Map;


public class GameDB {

	private Map<Integer, Integer> games;
	//private volatile int estpolls;

	/**
	* Creates a new PollCollection.
	*/
	public GameDB(){
		games = Collections.synchronizedMap(new HashMap<Integer, Integer>());
	}
	
	/**
	 * Get number of players in a specified game room
	 * @param id			Id of the game room
	 * @return				The number of players in that game room
	 * @throws Exception	If the gameID is not valid, return an error
	 * Most likely the game ID will be correct since they are usually called from GameController not user input
	 */
	public int getNoOfPlayers (int id) throws Exception {
		if (games.containsKey(id)) {
			return games.get(id);
		} else {
			throw new Exception ("Game Id is not Valid!!!");
		}
	}
	/**
	 * Update number of player in a specific game room
	 * @param id			Id of the game room
	 * @param numOfPlayers	The number of players in that game room
	 * @return				true if successfully update the data
	 * @throws Exception	If the game ID couldn't be found, usually this won't happen (just for safety check)
	 */
	public boolean updateNoOfPlayers (int id, int numOfPlayers) throws Exception {
		if (games.containsKey(id)) {
			games.put(id, numOfPlayers);
			return true;
		} else {
			throw new Exception ("Game Id is not Valid!!!");
		}
	}
	/**
	 * Add new game into Game Collection
	 * @param id		The id of new game room (the port of the game)
	 * @return			True if successfully add, false if the game room already exists.
	 */
	public boolean addNewGame (int id) {
		games.put(id, 0);
		if (games.containsKey(id)) {
			return true;
		} else {
			return false;
		}
	}
}
