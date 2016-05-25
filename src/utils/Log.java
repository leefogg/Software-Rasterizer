package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Log {
	private static FileWriter outfile;
	
	public static void init() throws IOException {
		String localdir =  System.getProperty("user.dir").replaceAll("\\\\", "/");
		outfile = new FileWriter(new File(localdir + "/log.log"));
	}

	
	public static void write(String line) {
		try {
			outfile.write(line);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void writeLine(String line) {
		try {
			outfile.write(line + "\n");
			System.out.println(line);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void dispose() {
		try {
			outfile.flush();
			outfile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
