import java.awt.Graphics;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.*;

import javax.swing.ImageIcon;

class StreamGobbler extends Thread {
	static Pattern s_pattern1 = Pattern.compile("([0-9]+) keypoints found.");
	static Pattern s_pattern2 = Pattern.compile("Found ([0-9]+) matches.");
	int answer = 0;
	
	InputStream is;
	String type;
	boolean printConsole = false;

	public StreamGobbler(InputStream is, String type, boolean _printConsole) {
		this.is = is;
		this.type = type;
		printConsole = _printConsole;
	}

	@Override
	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (printConsole)
					System.out.println(type + "> " + line);
				
				Matcher matcher1 = s_pattern1.matcher(line);
				if (matcher1.find()) {
					answer = Integer.parseInt(matcher1.group(1));
				}
				Matcher matcher2 = s_pattern2.matcher(line);
				if (matcher2.find()) {
					answer = Integer.parseInt(matcher2.group(1));
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public int getResult() { return answer; }
}

public class GlobalHelper {
	public static int runExecutable(String[] command, File runDirectory) {
		try {
			// String[] command = {"CMD", "/C", "dir"};
			ProcessBuilder pb = new ProcessBuilder(command);
			pb.directory(runDirectory);
			Process process = pb.start();
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR", false);
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT", true);
			outputGobbler.start();
			errorGobbler.start();
			// Wait to get exit value
			try {
				process.waitFor();
				// int exitValue = process.waitFor();
				// System.out.println("\n\nExit Value is " + exitValue);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			return errorGobbler.getResult();
		} catch (IOException e1) {
			System.out.println("Error, exception while running executable : " + e1);
			
		}
		return 0;
	}

	public static BufferedImage getGrayScale(BufferedImage inputImage){
	    BufferedImage img = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	    Graphics g = img.getGraphics();
	    g.drawImage(inputImage, 0, 0, null);
	    g.dispose();
	    return img;
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
