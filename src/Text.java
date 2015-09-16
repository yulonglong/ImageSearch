import java.util.Map;
import java.util.TreeMap;

public class Text {
	public static void search(TreeMap<String, ImageFile> images, ImageFile queryImage) {
		if (queryImage.m_description == null) return; // Do nothing if no description exists
		int queryImageDescSize = queryImage.m_description.size();
		if (queryImageDescSize == 0) return; // Do nothing if no description exists
		for (Map.Entry<String, ImageFile> entry : images.entrySet()) {
			ImageFile currImage = entry.getValue();
			if (currImage.m_description == null) continue; // Skip if no description exists
			int currImageDescSize = currImage.m_description.size();
			if (currImageDescSize == 0) continue; // Skip if no description exists
			
			int count = 0;
			for(int i=0;i<currImageDescSize;i++){
				for(int j=0;j<queryImageDescSize;j++){
					if ((currImage.m_description.get(i).contains(queryImage.m_description.get(j))) ||
					(queryImage.m_description.get(j).contains(currImage.m_description.get(i)))) {
						count++;
						break;
					}
				}
			}
			double currScore = (double)count / (double)Math.max(queryImageDescSize,currImageDescSize);
			currImage.m_textScore += currScore;
		}
		return;
	}
}
