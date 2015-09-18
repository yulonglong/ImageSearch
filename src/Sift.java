import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

import javax.imageio.ImageIO;

class SiftThread implements Runnable {
	ImageFile currImage;
	String queryImageName;
	int numQueryFeatures;
	int index;
	
	public SiftThread(ImageFile _currImage, String _queryImageName, int _numQueryFeatures, int _index) {
		currImage = _currImage;
		queryImageName = _queryImageName;
		numQueryFeatures = _numQueryFeatures;
		index = _index;
	}

	@Override
	public void run() {
		File fromPgm = new File(ImageSearch.s_siftPath + queryImageName + ".pgm");
		File toPgm = new File(ImageSearch.s_siftPath + queryImageName + "_" + index + ".pgm");
		File fromKey = new File(ImageSearch.s_siftPath + queryImageName + ".key");
		File toKey = new File(ImageSearch.s_siftPath + queryImageName + "_" + index + ".key");
		try {
			toPgm.delete();
			toKey.delete();
			Files.copy( fromPgm.toPath(), toPgm.toPath() );
			Files.copy( fromKey.toPath(), toKey.toPath() );
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String outFilename = "out_"+index+".txt";
		File outFile = new File(ImageSearch.s_siftPath+outFilename);
		
		String[] commandMatch = {"cmd", "/c" , "match.exe", "-im1", currImage.m_fullPgmPath, "-k1", currImage.m_fullKeyPath, "-im2", queryImageName+"_"+index+".pgm", "-k2", queryImageName+"_"+index+".key", ">"+outFilename};
		int numMatchingFeatures = GlobalHelper.runExecutable(commandMatch, new File(ImageSearch.s_siftPath));
		
		// System.out.println(currImage.m_name + " " + numMatchingFeatures + " " + numQueryFeatures);
		currImage.m_siftScore += (double)numMatchingFeatures / (double) numQueryFeatures;
		if (currImage.m_siftScore > 1.0) currImage.m_siftScore = 1.0;
		
		toPgm.delete();
		toKey.delete();
		outFile.delete();
	}
}

public class Sift {
	public static void search(TreeMap<String, ImageFile> images, ImageFile queryImage) {
		String queryImageName = queryImage.m_name;
		int pos = queryImageName.lastIndexOf(".");
		if (pos > 0) {
			queryImageName = queryImageName.substring(0, pos);
		}
		String queryImagePgmFilePath = ImageSearch.s_siftPath + queryImageName + ".pgm";
		File queryImagePgmFile = new File(queryImagePgmFilePath);
		String queryImageKeyFilePath = ImageSearch.s_siftPath + queryImageName + ".key";
		File queryImageKeyFile = new File(queryImageKeyFilePath);
		try {
			BufferedImage convertedImage = GlobalHelper.getGrayScale(queryImage.m_bufferedImage);
			ImageIO.write(convertedImage, "pnm", queryImagePgmFile);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String[] commandQuery = {"cmd", "/c" , "siftWin32.exe", "<"+queryImageName+".pgm", ">"+queryImageName+".key"};
		int numQueryFeatures = GlobalHelper.runExecutable(commandQuery, new File(ImageSearch.s_siftPath));
		
		ArrayList<Thread> threadsList = new ArrayList<Thread>();
		int threadIndex = 0;
		for (Map.Entry<String, ImageFile> entry : images.entrySet()) {
			ImageFile currImage = entry.getValue();
			
			Runnable r = new SiftThread(currImage, queryImageName, numQueryFeatures, threadIndex++);
			Thread t = new Thread(r);
			threadsList.add(t);
			t.start();
		}
		
		try {
			for(Thread t: threadsList) {
				t.join();
			}
		}
		catch (Exception e) {
			System.out.println("Failed to join threads");
			e.printStackTrace();
		}
		
		queryImagePgmFile.delete();
		queryImageKeyFile.delete();
		
		return;
	}
}
