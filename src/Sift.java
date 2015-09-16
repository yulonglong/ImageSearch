import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

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
		
		for (Map.Entry<String, ImageFile> entry : images.entrySet()) {
			ImageFile currImage = entry.getValue();
			
			String currImageName = currImage.m_name;
			int inPos = currImageName.lastIndexOf(".");
			if (inPos > 0) {
				currImageName = currImageName.substring(0, inPos);
			}
			String currImagePgmFilePath = ImageSearch.s_siftPath + currImageName + ".pgm";
			File currImagePgmFile = new File(currImagePgmFilePath);
			String currImageKeyFilePath = ImageSearch.s_siftPath + currImageName + ".key";
			File currImageKeyFile = new File(currImageKeyFilePath);
			try {
				BufferedImage convertedImage = GlobalHelper.getGrayScale(currImage.m_bufferedImage);
				ImageIO.write(convertedImage, "pnm", currImagePgmFile);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			String[] commandCurr = {"cmd", "/c" , "siftWin32.exe", "<"+currImageName+".pgm", ">"+currImageName+".key"};
			int numCurrFeatures = GlobalHelper.runExecutable(commandCurr, new File(ImageSearch.s_siftPath));
			
			String[] commandMatch = {"cmd", "/c" , "match.exe", "-im1", currImageName+".pgm", "-k1", currImageName+".key", "-im2", queryImageName+".pgm", "-k2", queryImageName+".key", ">out.txt"};
			int numMatchingFeatures = GlobalHelper.runExecutable(commandMatch, new File(ImageSearch.s_siftPath));
			
			System.out.println(numQueryFeatures + " " + numCurrFeatures + " " + numMatchingFeatures);
			
			currImage.m_siftScore += (double)numMatchingFeatures / (double)Math.min(numQueryFeatures, numCurrFeatures);
			
			currImagePgmFile.delete();
			currImageKeyFile.delete();
		}
		
		queryImagePgmFile.delete();
		queryImageKeyFile.delete();
		
		return;
	}
}
