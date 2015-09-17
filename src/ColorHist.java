import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.*;

class ColorHistThread implements Runnable {
	ImageFile currImage;
	double[] targetHist;
	
	public ColorHistThread(ImageFile _currImage, double[] _targetHist) {
		currImage = _currImage;
		targetHist = _targetHist;
	}

	@Override
	public void run() {
		double[] currHist = currImage.m_colorHistogram;
		if (currHist == null) 
			currHist = ColorHist.getHist(currImage.m_bufferedImage);
		currImage.m_colorHistScore += ColorHist.computeSimilarity(targetHist, currHist);
	}
}


public class ColorHist {
	private static int dim = 64;

	public static void search(TreeMap<String, ImageFile> images, ImageFile queryImage) {
		double[] targetHist = queryImage.m_colorHistogram;
		if (targetHist == null)
			targetHist = getHist(queryImage.m_bufferedImage);
		/* ranking the search results */
		ArrayList<Thread> threadsList = new ArrayList<Thread>();
		for (Map.Entry<String, ImageFile> entry : images.entrySet()) {
			ImageFile currImage = entry.getValue();
			
			Runnable r = new ColorHistThread(currImage, targetHist);
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
		return;
	}

	public static double computeSimilarity(double[] hist1, double[] hist2) {

		double distance = calculateDistance(hist1, hist2);
		return 1 - distance;
	}

	public static double[] getHist(BufferedImage image) {
		int imHeight = image.getHeight();
		int imWidth = image.getWidth();
		double[] bins = new double[dim * dim * dim];
		int step = 256 / dim;
		Raster raster = image.getRaster();
		for (int i = 0; i < imWidth; i++) {
			for (int j = 0; j < imHeight; j++) {
				// rgb->ycrcb
				int r = raster.getSample(i, j, 0);
				int g = raster.getSample(i, j, 1);
				int b = raster.getSample(i, j, 2);

				// Changed Codes.
				int y = (int) (0 + 0.299 * r + 0.587 * g + 0.114 * b);
				int cb = (int) (128 - 0.16874 * r - 0.33126 * g + 0.50000 * b);
				int cr = (int) (128 + 0.50000 * r - 0.41869 * g - 0.08131 * b);

				int ybin = y / step;
				int cbbin = cb / step;
				int crbin = cr / step;

				// Changed Codes.
				bins[ybin * dim * dim + cbbin * dim + crbin]++;
			}
		}

		// Changed Codes.
		for (int i = 0; i < dim * dim * dim; i++) {
			bins[i] = bins[i] / (imHeight * imWidth);
		}

		return bins;
	}

	public static double calculateDistance(double[] array1, double[] array2) {
		// Euclidean distance
		/*
		 * double Sum = 0.0; for(int i = 0; i < array1.length; i++) { Sum = Sum
		 * + Math.pow((array1[i]-array2[i]),2.0); } return Math.sqrt(Sum);
		 */

		// Bhattacharyya distance
		double h1 = 0.0;
		double h2 = 0.0;
		int N = array1.length;
		for (int i = 0; i < N; i++) {
			h1 = h1 + array1[i];
			h2 = h2 + array2[i];
		}

		double Sum = 0.0;
		for (int i = 0; i < N; i++) {
			Sum = Sum + Math.sqrt(array1[i] * array2[i]);
		}
		double dist = Math.sqrt(1 - Sum / Math.sqrt(h1 * h2));
		return dist;
	}
}
