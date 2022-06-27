package uebung;

import java.io.*;

public class Input {
	private InputStream in;
	public boolean end = false;
	
	public Input(File file_in) {
		if (!file_in.exists()) {
			System.out.println("File does not exist!");
			System.exit(1);
		}
		try {
			in = new FileInputStream(file_in.toString());
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}
	
	public char next() throws IOException {
		int c = in.read();
		if (c == -1) { end = true; }
		return (char) c;
	}
	
	public void read_file() throws IOException {
		while(!this.end) {
			System.out.println(this.next());
		}
	}
	
	public void close() throws IOException {
		this.in.close();
	}
}
