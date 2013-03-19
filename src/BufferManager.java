import java.util.ArrayList;


public class BufferManager {
	private ArrayList<String> Buffer1;
	private ArrayList<String> Buffer2;
	private ArrayList<String> Buffer3;
	private int[] flag = {0, 0, 0};
	
	public BufferManager () {
		Buffer1 = new ArrayList <String> ();
		Buffer2 = new ArrayList <String> ();
		Buffer3 = new ArrayList <String> ();
	}
	
	public int checkFinishedBuffer () {
		for (int i = 0; i < flag.length; i++) {
			if (flag[i] == 1) {
				return i;
			}
		}
		synchronized (flag) {
			try {
				flag.wait(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public synchronized boolean setFlag (int i, boolean action) {
		if (action) {
			flag[i] = 1;
		} else {
			flag[i] = 0;
		}
		synchronized (flag) {
			flag.notify();
		}
		return true;
	}
	
	public int getEmptyBuffer () {
		for (int i = 0; i < flag.length; i++) {
			if (flag[i] == 0) {
				return i;
			}
		}
		synchronized (flag) {
			try {
				flag.wait(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	public boolean putFullBuffer (int i, ArrayList<String> buffer) {
		switch (i) {
		case 0:
			Buffer1.addAll(buffer);
			break;
		case 1:
			Buffer2.addAll(buffer);
			break;
		case 2:
			Buffer3.addAll(buffer);
			break;
		default:
			System.out.println ("You are trying to write to a non-existing buffer");
			return false;
		}
		setFlag (i, true);
		return true;
	}
	
	public boolean putEmptyBuffer (int i) {
		switch (i) {
		case 0: 
			Buffer1.clear();	
			break;
		case 1:
			Buffer2.clear();
			break;
		case 3:
			Buffer3.clear();
			break;
		default:
			System.out.println ("You are trying to empty a non-existing buffer");
			return false;
		}
		setFlag (i, false);
		return true;
	}
	
	public ArrayList<String> getFullBuffer (int i) {
		ArrayList<String> tempBuffer = new ArrayList<String>();
		switch (i) {
		case 0:
			tempBuffer.addAll(Buffer1);
			break;
		case 1:
			tempBuffer.addAll(Buffer2);
			break;
		case 2:
			tempBuffer.addAll(Buffer3);
			break;
		default:
			System.out.println ("You are trying to access a non-existing buffer");
			break;
		}
		putEmptyBuffer(i);
		return tempBuffer;
	}
}
