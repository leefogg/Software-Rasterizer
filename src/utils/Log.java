package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Log {
	private static final String newline = System.getProperty("line.separator");
	
	private static FileWriter outfile;
	
	public static void init() throws IOException {
		String localdir =  System.getProperty("user.dir").replaceAll("\\\\", "/");
		outfile = new FileWriter(new File(localdir + "/log.log"));
	}

	
	public static void write(String line) {
		try {
			outfile.write(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeLine(String line) {
		try {
			outfile.write(line + newline);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void dispose() {
		try {
			outfile.flush();
			outfile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
