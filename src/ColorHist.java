import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.util.*;

public class ColorHist {
	int dim = 64;
	
	public TreeSet<ImageFile> search(TreeSet<ImageFile> images, BufferedImage bufferedimage, int resultsize) throws IOException{
    	double[] targetHist = getHist(bufferedimage);
    	TreeSet<ImageFile> result = new TreeSet<ImageFile>(new TreeSetScoreComparator());
		
		/*ranking the search results*/
		for (ImageFile currImage: images) {
			double[] currHist = getHist(currImage.bufferedImage);
			currImage.score = computeSimilarity(targetHist, currHist);
			result.add(currImage);
			if (result.size() > resultsize) result.pollLast();
		}
    	return result;
    }
    
    public double computeSimilarity(double [] hist1, double [] hist2) {
		
		double distance = calculateDistance(hist1, hist2);
		return 1-distance;
	}
	
	public double[] getHist(BufferedImage image) {
		int imHeight = image.getHeight();
        int imWidth = image.getWidth();
        double[] bins = new double[dim*dim*dim];
        int step = 256 / dim;
        Raster raster = image.getRaster();
        for(int i = 0; i < imWidth; i++)
        {
            for(int j = 0; j < imHeight; j++)
            {
            	// rgb->ycrcb
            	int r = raster.getSample(i,j,0);
            	int g = raster.getSample(i,j,1);
            	int b = raster.getSample(i,j,2);
            	
            	//Changed Codes. 
            	int y  = (int)( 0 + 0.299   * r + 0.587   * g + 0.114   * b);
        		int cb = (int)(128 -0.16874 * r - 0.33126 * g + 0.50000 * b);
        		int cr = (int)(128 + 0.50000 * r - 0.41869 * g - 0.08131 * b);
        		
        		int ybin = y / step;
        		int cbbin = cb / step;
        		int crbin = cr / step;

        		//Changed Codes. 
                bins[ybin*dim*dim+cbbin*dim+crbin] ++;
            }
        }
        
        //Changed Codes. 
        for(int i = 0; i < dim*dim*dim; i++) {
        	bins[i] = bins[i]/(imHeight*imWidth);
        }
        
        return bins;
	}
	
	public double calculateDistance(double[] array1, double[] array2)
    {
		// Euclidean distance
        /*double Sum = 0.0;
        for(int i = 0; i < array1.length; i++) {
           Sum = Sum + Math.pow((array1[i]-array2[i]),2.0);
        }
        return Math.sqrt(Sum);
        */
        
        // Bhattacharyya distance
		double h1 = 0.0;
		double h2 = 0.0;
		int N = array1.length;
        for(int i = 0; i < N; i++) {
        	h1 = h1 + array1[i];
        	h2 = h2 + array2[i];
        }

        double Sum = 0.0;
        for(int i = 0; i < N; i++) {
           Sum = Sum + Math.sqrt(array1[i]*array2[i]);
        }
        double dist = Math.sqrt( 1 - Sum / Math.sqrt(h1*h2));
        return dist;
    }
}
