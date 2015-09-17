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
			
			String[] commandMatch = {"cmd", "/c" , "match.exe", "-im1", currImage.m_fullPgmPath, "-k1", currImage.m_fullKeyPath, "-im2", queryImageName+".pgm", "-k2", queryImageName+".key", ">out.txt"};
			int numMatchingFeatures = GlobalHelper.runExecutable(commandMatch, new File(ImageSearch.s_siftPath));
			
			System.out.println(numQueryFeatures + " " + numMatchingFeatures);
			
			currImage.m_siftScore += (double)numMatchingFeatures / (double) numQueryFeatures;
			if (currImage.m_siftScore > 1.0) currImage.m_siftScore = 1.0;
		}
		
		queryImagePgmFile.delete();
		queryImageKeyFile.delete();
		
		return;
	}
}
