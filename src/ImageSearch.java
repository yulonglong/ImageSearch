
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;

/*path of the dataset, and the size of search result could be changed here*/



class ImageFile implements Comparable<ImageFile>{
	final static int totalCategoryNum = 25;
	BufferedImage m_bufferedImage;
	String m_name;
	String m_description;
	double m_score = 0.0;
	double[] m_colorHistogram;
	TreeSet<Integer> m_category = new TreeSet<Integer>();
	ImageFile (File _file) {
		try {
			m_bufferedImage = ImageIO.read(_file);
			m_name = _file.getName();
			m_colorHistogram = ColorHist.getHist(m_bufferedImage);
		}
		catch (Exception e ) {
			System.out.println("Image File exception : " + e);
		}
	}
	
	Double getScore() {
		return m_score;
	}
	public int compareTo(ImageFile thatImage) {
        return this.m_name.compareTo(thatImage.m_name);
    }
	// matrix sequence tp,tn,fp,fn
	public void getConfusionMatrix(ImageFile queryFile, int[] matrix) {
		int tp = 0;
		for(Integer thisCat: m_category) {
			 if (queryFile.m_category.contains(thisCat)) {
				 tp++;
			 }
		}
		int fn = queryFile.m_category.size() - tp;
		int fp = m_category.size() - tp;
		int tn = (totalCategoryNum-tp-fp)-fn;
		matrix[0] = tp; matrix[1] = tn; matrix[2] = fp; matrix[3] = fn;
	}
	
	public static double getPrecision(int[] matrix) {
		int tp = matrix[0];
		int fp = matrix[2];
		double ans = (double)tp/((double)tp+(double)fp);
		return ans;
	}
	public static double getRecall(int[] matrix) {
		int tp = matrix[0];
		int fn = matrix[3];
		double ans = (double)tp/((double)tp+(double)fn);
		return ans;
	}
	public static double getF1Score(int[] matrix) {
		int tp = matrix[0];
		int fp = matrix[2];
		int fn = matrix[3];
		double ans = 2.0*(double)tp/ (2.0*(double)tp + (double)fp + (double)fn);
		return ans;
	}
}

class ImageFileScoreComparator implements Comparator<ImageFile> {
    @Override
    public int compare(ImageFile e1, ImageFile e2) {
        return e2.getScore().compareTo(e1.getScore());
    }
}

public class ImageSearch extends JFrame implements ActionListener {
	static final long serialVersionUID = 42L;
	
    JFileChooser m_fc;
	JPanel m_contentPane;


	ImageFile m_queryImageFile = null;
    BufferedImage m_queryImage = null;
	File m_queryFile;
	int m_windowWidth = 1600;
	int m_windowHeight = 1280;
	int m_resultSize = 20;    //size of the searching result
	String m_imageDataPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageData\\train\\data_complete\\"; //the path of image dataset
	String m_imageListPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageList\\train\\TrainImagelist.txt";
	String m_imageDescriptionPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageData\\train\\train_tags.txt";
	String m_imageCategoryPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageData\\category_names.txt";
	String m_groundTruthPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\Groundtruth\\train\\";
	
	String m_imageTestDataPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageData\\test\\data_complete\\"; //the path of image dataset
	String m_imageTestListPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageList\\test\\TestImagelist.txt";
	String m_imageTestDescriptionPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageData\\test\\test_tags.txt";
	String m_testGroundTruthPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\Groundtruth\\test\\";
	
    JButton m_openButton, m_searchButton, m_testButton;
    
	JLabel [] m_imageLabels = new JLabel [ m_resultSize ];
	JLabel m_queryImageLabel = new JLabel();
	
	JCheckBox m_colorHistogramCheckBox = new JCheckBox("Color Histogram");
	JCheckBox m_visualConceptCheckBox = new JCheckBox("Visual Concept");
	JCheckBox m_visualKeywordCheckBox = new JCheckBox("Visual Keywords");
	JCheckBox m_textCheckbox = new JCheckBox("Text");
	
	JProgressBar m_progressBar = new JProgressBar();
	
	TreeMap<String, Integer> m_categoryMap = new TreeMap<String, Integer>();
	TreeMap<Integer, String> m_invCategoryMap = new TreeMap<Integer, String>();
	TreeMap<String, ImageFile> m_imageMap = new TreeMap<String, ImageFile>();
	TreeMap<String, ImageFile> m_imageTestMap = new TreeMap<String, ImageFile>();
	
	private void loadTrainingData() {
    	// Begin Reading image file names
    	TreeSet<String> imageName = new TreeSet<String>();
    	try {
        	Scanner cin = new Scanner(new File(m_imageListPath));
        	while (cin.hasNext()) {
        		imageName.add(cin.nextLine());
        	}
        	cin.close();
    	}
    	catch (Exception e) {
    		System.out.println("Error! Failed to read ImageList : " + e);
    	}
    	// End Reading image file names
    	
    	
		File dir = new File(m_imageDataPath);  //path of the dataset
		File [] files = dir.listFiles();
		
		// Initialize ProgressBar
		
    	m_progressBar.setMinimum(0);
    	m_progressBar.setMaximum(files.length);
    	m_progressBar.setStringPainted(true);
    	m_contentPane.add(m_progressBar);
    	m_contentPane.setVisible(true);
    	setVisible(true);
		
    	// Load Image Files
		for (int i=0; i < files.length;i++){
			m_progressBar.setValue(i);
			double currPercentage = (double)i/(double)files.length * 100.0;
			m_progressBar.setString(String.format("%.2f", currPercentage) + "%");
			ImageFile currImage = new ImageFile(files[i]);
			m_imageMap.put(currImage.m_name,currImage);
			imageName.remove(currImage.m_name); // Remove the image filem_name from the list
		}
		
		// If it is not empty, there are missing m_imageMap
		if (!imageName.isEmpty()) {
			System.err.println("Error! Not all training images are read!");
			for (String temp: imageName) {
				System.out.println(temp);
			}
		}
		
		// Begin Reading image description texts
    	try {
        	Scanner cin = new Scanner(new File(m_imageDescriptionPath));
        	while (cin.hasNext()) {
        		String imageFileName = cin.next();
        		String text = cin.nextLine();
        		ImageFile currImage = m_imageMap.get(imageFileName);
        		currImage.m_description = text.trim();
        	}
        	cin.close();
    	}
    	catch (Exception e) {
    		System.out.println("Error! Failed to read ImageDescription : " + e);
    	}
    	// End Reading image description texts
    	
    	// Begin Reading Image Category
    	try {
        	Scanner cin = new Scanner(new File(m_imageCategoryPath));
        	while (cin.hasNext()) {
        		String categoryName = cin.next();
        		int index = m_categoryMap.size();
        		m_categoryMap.put(categoryName, index);
        		m_invCategoryMap.put(index, categoryName);
        		
        		Scanner groundTruthCin = new Scanner (new File(m_groundTruthPath + "Labels_" + categoryName + ".txt"));
        		for (Map.Entry<String, ImageFile> entry : m_imageMap.entrySet()) {
        			if (!groundTruthCin.hasNext()) break;
        			int valid = groundTruthCin.nextInt();
        			if (valid == 1) {
            			ImageFile currImage = entry.getValue();
            			currImage.m_category.add(index);
        			}
        		}
        		groundTruthCin.close();
        	}
        	cin.close();
    	}
    	catch (Exception e) {
    		System.out.println("Error! Failed to read CategoryName : " + e);
    	}
    	// End Reading Image Category
		
		m_contentPane.setVisible(false);
		m_contentPane.remove(m_progressBar);
		// End of ProgressBar
	}
	
	private void loadTestData() {
		// Begin Reading image file names
    	TreeSet<String> imageName = new TreeSet<String>();
    	try {
        	Scanner cin = new Scanner(new File(m_imageTestListPath));
        	while (cin.hasNext()) {
        		imageName.add(cin.nextLine());
        	}
        	cin.close();
    	}
    	catch (Exception e) {
    		System.out.println("Error! Failed to read Test ImageList : " + e);
    	}
    	// End Reading image file names
    	
    	
		File dir = new File(m_imageTestDataPath);  //path of the dataset
		File [] files = dir.listFiles();
		
		// Initialize ProgressBar
		
    	m_progressBar.setMinimum(0);
    	m_progressBar.setMaximum(files.length);
    	m_progressBar.setStringPainted(true);
    	m_contentPane.add(m_progressBar);
    	m_contentPane.setVisible(true);
    	setVisible(true);
		
    	// Load Image Files
		for (int i=0; i < files.length;i++){
			m_progressBar.setValue(i);
			double currPercentage = (double)i/(double)files.length * 100.0;
			m_progressBar.setString(String.format("%.2f", currPercentage) + "%");
			ImageFile currImage = new ImageFile(files[i]);
			m_imageTestMap.put(currImage.m_name,currImage);
			imageName.remove(currImage.m_name); // Remove the image filem_name from the list
		}
		
		// If it is not empty, there are missing m_imageMap
		if (!imageName.isEmpty()) {
			System.err.println("Error! Not all test images are read!");
			for (String temp: imageName) {
				System.out.println(temp);
			}
		}
		
		// Begin Reading image m_description texts
    	try {
        	Scanner cin = new Scanner(new File(m_imageTestDescriptionPath));
        	while (cin.hasNext()) {
        		String imageFileName = cin.next();
        		String text = cin.nextLine();
        		ImageFile currImage = m_imageTestMap.get(imageFileName);
        		currImage.m_description = text.trim();
        	}
        	cin.close();
    	}
    	catch (Exception e) {
    		System.out.println("Error! Failed to read Test ImageDescription : " + e);
    	}
    	// End Reading image m_description texts
    	
    	// Begin Reading Image Category
    	try {
        	Scanner cin = new Scanner(new File(m_imageCategoryPath));
        	for (Map.Entry<String, Integer> catEntry : m_categoryMap.entrySet()) {
        		String categoryName = catEntry.getKey();
        		int index = catEntry.getValue();
        		
        		Scanner groundTruthCin = new Scanner (new File(m_testGroundTruthPath + "Labels_" + categoryName + ".txt"));
        		for (Map.Entry<String, ImageFile> entry : m_imageTestMap.entrySet()) {
        			if (!groundTruthCin.hasNext()) break;
        			int valid = groundTruthCin.nextInt();
        			if (valid == 1) {
            			ImageFile currImage = entry.getValue();
            			currImage.m_category.add(index);
        			}
        		}
        		groundTruthCin.close();
        	}
        	cin.close();
    	}
    	catch (Exception e) {
    		System.out.println("Error! Failed to read Test CategoryName : " + e);
    	}
    	// End Reading Image Category
		
		m_contentPane.setVisible(false);
		m_contentPane.remove(m_progressBar);
		// End of ProgressBar
	}

    public ImageSearch() {
    	m_contentPane = (JPanel)this.getContentPane();
		setSize(m_windowWidth,m_windowHeight);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
    	loadTrainingData();
    	loadTestData();
		
		// Main UI
        m_openButton = new JButton("Select an image...",
                createImageIcon("images/Open16.gif"));
        m_openButton.addActionListener(this);
        
        m_searchButton = new JButton("Search");
        m_searchButton.addActionListener(this);
        
        m_testButton = new JButton("Run Test");
        m_testButton.addActionListener(this);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(m_openButton);
        buttonPanel.add(m_searchButton);
        buttonPanel.add(m_testButton);
        
        buttonPanel.add(m_colorHistogramCheckBox);
        buttonPanel.add(m_visualConceptCheckBox);
        buttonPanel.add(m_visualKeywordCheckBox);
        buttonPanel.add(m_textCheckbox);
        
        JPanel queryImagePanel = new JPanel();
        queryImagePanel.add(m_queryImageLabel);
        
    	JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new GridLayout(4,5));
        
        for (int i = 0; i<m_imageLabels.length;i++){
        	m_imageLabels[i] = new JLabel();
        	m_imageLabels[i].setHorizontalTextPosition(JLabel.CENTER);
        	m_imageLabels[i].setVerticalTextPosition(JLabel.BOTTOM);
        	imagePanel.add(m_imageLabels[i]);
        }

		m_contentPane = (JPanel)this.getContentPane();
		setSize(m_windowWidth,m_windowHeight);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        m_contentPane.add(buttonPanel, BorderLayout.PAGE_START);
        m_contentPane.add(imagePanel, BorderLayout.CENTER);
        m_contentPane.add(queryImagePanel, BorderLayout.PAGE_END);
        
        m_contentPane.setVisible(true);
		setVisible(true);
//      add(logScrollPane, BorderLayout.CENTER);
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ImageSearch.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        //Set up the file chooser.
        if (e.getSource() == m_openButton) {
	        if (m_fc == null) {
	            m_fc = new JFileChooser();
	
	            //Add a custom file filter and disable the default
	            //(Accept All) file filter.
	            m_fc.addChoosableFileFilter(new ImageFilter());
	            m_fc.setAcceptAllFileFilterUsed(false);
	
	            //Add custom icons for file types.
	            m_fc.setFileView(new ImageFileView());
	
	            //Add the preview pane.
	            m_fc.setAccessory(new ImagePreview(m_fc));
	        } 
	        
	        //Show it.
	        int returnVal = m_fc.showDialog(ImageSearch.this, "Select an image..");
	
	        //Process the results.
	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            m_queryFile = m_fc.getSelectedFile();
	        }
	        
	        if (m_queryFile != null) {
	        	try {
	        		m_queryImageFile = new ImageFile(m_queryFile);
	        		m_queryImage = ImageIO.read(m_queryFile);
	        	
	            	m_queryImageLabel.setIcon(new ImageIcon(m_queryImage));
	            	m_queryImageLabel.setText(m_queryFile.getName());
				} catch (IOException e1) {}
	        }
	
	        m_fc.setSelectedFile(null);
        }
        else if (e.getSource() == m_searchButton) {
        	TreeSet<ImageFile> result = null;
        	result = getRank(m_queryImageFile);
	
        	
			int m_imageLabelsIndex = 0;
			for(ImageFile currResult : result) {
				m_imageLabels[m_imageLabelsIndex].setIcon(new ImageIcon(currResult.m_bufferedImage));
				m_imageLabels[m_imageLabelsIndex].setText(m_invCategoryMap.get(currResult.m_category.first())+ " - " + m_invCategoryMap.get(currResult.m_category.last()) + currResult.m_name);
				m_imageLabelsIndex++;
			}
        }
        else if (e.getSource() == m_testButton) {
			int[] globalMatrix = new int[4];
        	for (Map.Entry<String, ImageFile> entry : m_imageTestMap.entrySet()) {
    			ImageFile currImageTest = entry.getValue();
    			TreeSet<ImageFile> result = null;
    			result = getRank(currImageTest);
    			
    			
    			for(ImageFile currResult: result) {
    				int[] matrix = new int[4];
    				currResult.getConfusionMatrix(currImageTest, matrix);
    				globalMatrix[0] += matrix[0];
    				globalMatrix[1] += matrix[1];
    				globalMatrix[2] += matrix[2];
    				globalMatrix[3] += matrix[3];
    			}	
        	}
			System.out.println(ImageFile.getRecall(globalMatrix) + " - " + ImageFile.getPrecision(globalMatrix) + " - " + ImageFile.getF1Score(globalMatrix));
			System.out.println();
			
        }
    }
    
    public TreeSet<ImageFile> getRank(ImageFile queryImage) {
	    ColorHist.search(m_imageMap, queryImage);

    	TreeSet<ImageFile> result = new TreeSet<ImageFile>(new ImageFileScoreComparator());
    	/*ranking the search results*/
    	for (Map.Entry<String, ImageFile> entry : m_imageMap.entrySet()) {
            ImageFile currImage = entry.getValue();
			result.add(currImage);
			if (result.size() > m_resultSize) result.pollLast();
		}
    	return result;
    }

	public static void main(String[] args) {
	    @SuppressWarnings("unused")
		ImageSearch example = new ImageSearch();
    }
}
