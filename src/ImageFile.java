import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

import javax.imageio.ImageIO;

public class ImageFile implements Comparable<ImageFile> {
	BufferedImage m_bufferedImage;
	String m_name;
	String m_fullImagePath;
	String m_fullPgmPath;
	String m_fullKeyPath;
	ArrayList<String> m_description = new ArrayList<String>();
	
	double[] m_colorHistogram;
	double[] m_semanticFeatures = new double[25];
	double[] m_visualConceptVector;
	TreeSet<Integer> m_category = new TreeSet<Integer>();

	double m_score = 0.0;
	double m_colorHistScore = 0.0;
	double m_semanticFeatureScore = 0.0;
	double m_visualConceptVectorScore = 0.0;
	double m_siftScore = 0.0;
	double m_textScore = 0.0;

	ImageFile(File _file) {
		try {
			m_bufferedImage = ImageIO.read(_file);
			m_fullImagePath = _file.toString();
			m_name = _file.getName();
			if (ImageSearch.s_enableColorHistCache)
				m_colorHistogram = ColorHist.getHist(m_bufferedImage);
		} catch (Exception e) {
			System.out.println("Image File exception : " + e);
		}
	}

	ImageFile(File _file, File semanticFeatureFile) {
		try {
			m_bufferedImage = ImageIO.read(_file);
			m_fullImagePath = _file.toString();
			m_name = _file.getName();
			if (ImageSearch.s_enableColorHistCache)
				m_colorHistogram = ColorHist.getHist(m_bufferedImage);
			m_visualConceptVector = VisualConcept.getVisualConceptVector(semanticFeatureFile, m_semanticFeatures);
		} catch (Exception e) {
			System.out.println("Image File exception : " + e);
		}
	}
	
	ImageFile(File _file, File semanticFeatureFile, String pgmDataPath) {
		try {
			m_bufferedImage = ImageIO.read(_file);
			m_name = _file.getName();
			m_fullImagePath = _file.toString();
			if (ImageSearch.s_enableColorHistCache)
				m_colorHistogram = ColorHist.getHist(m_bufferedImage);
			m_visualConceptVector = VisualConcept.getVisualConceptVector(semanticFeatureFile, m_semanticFeatures);
			m_fullPgmPath = pgmDataPath + GlobalHelper.changeExtension(m_name, ".pgm");
			m_fullKeyPath = pgmDataPath + GlobalHelper.changeExtension(m_name, ".key");
		} catch (Exception e) {
			System.out.println("Image File exception : " + e);
		}
	}
	
	public void resetScore() {
		m_score = 0.0;
		m_colorHistScore = 0.0;
		m_semanticFeatureScore = 0.0;
		m_visualConceptVectorScore = 0.0;
		m_textScore = 0.0;
		m_siftScore = 0.0;
	}

	Double getScore() {
		return m_score;
	}
	
	public void updateDescription(String description) {
		m_description.clear();
		Scanner innercin = new Scanner(description);
		while (innercin.hasNext()) {
			m_description.add(innercin.next().trim().toLowerCase());
		}
		innercin.close();
	}

	public int compareTo(ImageFile thatImage) {
		return this.m_name.compareTo(thatImage.m_name);
	}
	
	public boolean isRelevant(ImageFile queryFile) {
		for (Integer thisCat : m_category) {
			if (queryFile.m_category.contains(thisCat)) {
				return true;
			}
		}
		return false;
	}

	// matrix sequence tp,tn,fp,fn
	public void getConfusionMatrix(ImageFile queryFile, int[] matrix) {
		int tp = 0;
		for (Integer thisCat : m_category) {
			if (queryFile.m_category.contains(thisCat)) {
				tp++;
			}
		}
		int fn = queryFile.m_category.size() - tp;
		int fp = m_category.size() - tp;
		int tn = (ImageSearch.s_totalCategoryNum - tp - fp) - fn;
		matrix[0] = tp;
		matrix[1] = tn;
		matrix[2] = fp;
		matrix[3] = fn;
	}
}

class DoubleDescendingComparator implements Comparator<Double> {
	@Override
	public int compare(Double e1, Double e2) {
		return e2.compareTo(e1);
	}
}

class ImageFileScoreComparator implements Comparator<ImageFile> {
	@Override
	public int compare(ImageFile e1, ImageFile e2) {
		int currCompare = e2.getScore().compareTo(e1.getScore());
		if (currCompare == 0) {
			return e1.m_name.compareTo(e2.m_name);
		}
		return currCompare;
	}
}