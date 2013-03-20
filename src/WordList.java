/**
 * @author Nhat Ho
 * @StudentId: 100815111
 * The wordlist class to keep the questions and the files name for short
 */
public class WordList {
	private String question[] = {"What is your favourite pet?", "What is your favourite food?", "What is your favourite car?", "Which is your favourite country?", "Where do you want to travel to?"};
	private String fileName[] = {"Pet", "Food", "Car", "Country", "Travel"};
	public WordList () {}
	/**
	 * Get size of questionList
	 * @return		The size of questionList
	 */
	public int getSize() {
		return question.length;
	}
	/**
	 * Return the question from the question list
	 * @param i			The index of question
	 * @return			The question
	 */
	public String getQuestion (int i) {
		return question[i];
	}
	/**
	 * Return the name of question used as file names
	 * @param i			The index of file
	 * @return			The name of file
	 */
	public String getFile (int i) {
		return fileName[i];
	}
}
