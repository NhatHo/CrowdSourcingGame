
public class WordList {
	private String question[] = {"What is your favourite pet?", "What is your favourite food?", "What is your favourite car?", "Which is your favourite country?", "Where do you want to travel to?"};
	private String fileName[] = {"Pet", "Food", "Car", "Country", "Travel"};
	public WordList () {}
	
	public int getSize() {
		return question.length;
	}
	/**
	 * Return the question from the question list
	 * @param i			
	 * @return
	 */
	public String getQuestion (int i) {
		return question[i];
	}
	
	public String getFile (int i) {
		return fileName[i];
	}
}
