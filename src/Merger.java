/**
 * @author: Nhat Ho
 * The Merger which will continuously check the multiple buffer and write results to files
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;


public class Merger extends Thread {
	private BufferManager manager;
	private WordList questions;
	private AtomicBoolean shutDown;
	private String path;
	/**
	 * Constructor
	 * @param manager			Object of BufferManager
	 * @param questions			Object of WordList
	 * @param shutDown			Object of AtomicBoolean
	 */
	public Merger (BufferManager manager, WordList questions, AtomicBoolean shutDown) {
		this.manager = manager;
		this.questions = questions;
		this.shutDown = shutDown;
		getPath();
	}
	/**
	 * Get absolute path for each machine from config.txt file
	 */
	public void getPath () {
		File file = new File("config.txt");
		Scanner input = null;
		try {
			input = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.println ("Cannot get info from config.txt file");
		}

		while(input.hasNext()) {
		    path = input.nextLine();
		}
		input.close();
	}
	/**
	 * Write the answer from buffers to File
	 * @param buffer				The buffer contains information
	 * @throws IOException			
	 */
	public void writeFile (ArrayList<String> buffer) throws IOException {
		File file = new File(path + "/storage/" + buffer.get(0) + ".txt");
		FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 1; i < buffer.size(); i++) {
			bw.write(buffer.get(i));
			bw.newLine();
		}
		bw.close();
	}
	/**
	 * Create files if the required files don't exist on system
	 * @throws IOException
	 */
	public void createFiles () throws IOException {
		for (int i = 0; i < questions.getSize(); i++) {
			File file = new File(path + "/storage/" + questions.getFile(i) + ".txt");
			if (!file.exists()) {
				file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(questions.getQuestion(i));
				bw.newLine();
				bw.close();
			}
		}
	}
	/**
	 * Run this thread continuously check for new information and update txt files
	 */
	public void run() {
		try {
			createFiles();
		} catch (IOException e) {
			System.out.println ("Failed to create files");
		}
		int fullBuffer = -1;
		ArrayList<String> dataBuffer = new ArrayList<String>();
		while (!shutDown.get()) {
			do {
				fullBuffer = manager.checkFinishedBuffer();
			} while (fullBuffer == -1);
				dataBuffer.addAll(manager.getFullBuffer(fullBuffer));
			try {
				writeFile (dataBuffer);
				dataBuffer.clear();
			} catch (IOException e) {
				System.out.println ("Failed to write files");
			}
		}
		System.out.println ("Merger is closed");
	}
}
