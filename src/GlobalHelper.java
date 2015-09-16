import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.swing.ImageIcon;

public class GlobalHelper {
	public static void runExecutable(String[] command, File runDirectory) {
		try {
			// String[] command = {"CMD", "/C", "dir"};
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.directory(runDirectory);
			Process process = pb.start();
			// Read out dir output
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			System.out.printf("Output of running %s is:\n", Arrays.toString(command));
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

			// Wait to get exit value
			try {
				int exitValue = process.waitFor();
				System.out.println("\n\nExit Value is " + exitValue);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		} catch (IOException e1) {
			System.out.println("Error, exception while running executable : " + e1);
		}
	}
	
	public static double getPrecision(int[] matrix) {
		int tp = matrix[0];
		int fp = matrix[2];
		double ans = (double) tp / ((double) tp + (double) fp);
		return ans;
	}

	public static double getRecall(int[] matrix) {
		int tp = matrix[0];
		int fn = matrix[3];
		double ans = (double) tp / ((double) tp + (double) fn);
		return ans;
	}

	public static double getF1Score(int[] matrix) {
		int tp = matrix[0];
		int fp = matrix[2];
		int fn = matrix[3];
		double ans = 2.0 * (double) tp / (2.0 * (double) tp + (double) fp + (double) fn);
		return ans;
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	public static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = ImageSearch.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
}
