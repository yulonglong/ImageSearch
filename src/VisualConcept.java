import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class VisualConcept {
	static int s_topNResult = 5;
	static double s_maxScore = 7.0;
	static int s_visualConceptVectorClass = 1000;
	
	public static double[] getVisualConceptVector(File semanticFeatureFile, double[] semanticFeatures) {
		double[] visualConceptVector = new double[1000];
		TreeMap<Double,Integer> semanticScore = new TreeMap<Double,Integer>(new DoubleDescendingComparator());
		int index = 0;
		try {
			Scanner cin = new Scanner(semanticFeatureFile);
			while (cin.hasNext()) {
				double currScore = cin.nextDouble();
				visualConceptVector[index] = currScore/s_maxScore;
				semanticScore.put(currScore, index);
				index++;
			}
			cin.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}
		
		int topN = s_topNResult;
		for (Map.Entry<Double, Integer> entry : semanticScore.entrySet()) {
			Double currScore = entry.getKey()/s_maxScore;
			Integer currIndex = entry.getValue();
			if (currScore < 0.0) break;
			if (topN == 0) break;
			
			ArrayList<Integer> categoryList = ImageSearch.s_semanticFeatureMap.get(currIndex);
			for(Integer categoryIndex: categoryList) {
				semanticFeatures[categoryIndex] += currScore;
				if (semanticFeatures[categoryIndex] > 1.0) semanticFeatures[categoryIndex] = 1.0;
			}
			topN--;
		}
		return visualConceptVector;
	}
	
	public static void search(TreeMap<String, ImageFile> images, ImageFile queryImage) {
		for (Map.Entry<String, ImageFile> entry : images.entrySet()) {
			ImageFile currImage = entry.getValue();
			for(int i=0;i<ImageSearch.s_totalCategoryNum;i++){
				if ((queryImage.m_semanticFeatures[i] > 0.0) && (currImage.m_category.contains(i))) {
					currImage.m_semanticFeatureScore += queryImage.m_semanticFeatures[i];
					if (currImage.m_semanticFeatureScore > 1.0) currImage.m_semanticFeatureScore = 1.0;
				}
			}
			for(int i=0;i<s_visualConceptVectorClass;i++){
				if ((currImage.m_visualConceptVector[i] > 0.0) && (queryImage.m_visualConceptVector[i] > 0.0)) {
					currImage.m_visualConceptVectorScore += Math.min(currImage.m_visualConceptVector[i], queryImage.m_visualConceptVector[i]);
					if (currImage.m_visualConceptVectorScore > 1.0) currImage.m_visualConceptVectorScore = 1.0;
				}
			}
		}
		
		return;
	}
}
