
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;

public class ImageSearch extends JFrame implements ActionListener {
	static final long serialVersionUID = 42L;
	final static int s_totalCategoryNum = 25;

	final static boolean s_enableColorHistCache = false;
	
	JFileChooser m_fc;
	JPanel m_contentPane;

	ImageFile m_queryImageFile = null;
	BufferedImage m_queryImage = null;
	File m_queryFile;
	
	int m_windowWidth = 1600;
	int m_windowHeight = 900;
	int m_resultSize = 20; // size of the searching result

	static String s_mainDatapath = "D:\\GitHub\\ImageSearchFull\\MainDirectory\\";
	static String s_siftPath = s_mainDatapath + "FeatureExtractor\\siftDemoV4\\";
	
	String m_semanticFeaturePath = s_mainDatapath + "FeatureExtractor\\semanticFeature\\";
	String m_semanticFeatureExecutableName = "image_classification.exe";
	String m_semanticFeatureClass = s_mainDatapath + "FeatureExtractor\\semanticFeature\\1000d.csv";

	String m_imageDataPgmPath = s_mainDatapath + "ImageData\\train\\data_complete_pgm\\";
	String m_imageDataPath = s_mainDatapath + "ImageData\\train\\data_complete\\";
	String m_imageSemanticFeaturePath = s_mainDatapath + "ImageData\\train\\semanticFeature_complete\\";
	String m_imageListPath = s_mainDatapath + "ImageList\\train\\TrainImagelist.txt";
	String m_imageDescriptionPath = s_mainDatapath + "ImageData\\train\\train_tags.txt";
	String m_imageCategoryPath = s_mainDatapath + "ImageData\\category_names.txt";
	String m_groundTruthPath = s_mainDatapath + "Groundtruth\\train\\";

	String m_imageTestDataPgmPath = s_mainDatapath + "ImageData\\test\\data_complete_pgm\\";
	String m_imageTestDataPath = s_mainDatapath + "ImageData\\test\\data_complete\\";
	String m_imageTestSemanticFeaturePath = s_mainDatapath + "ImageData\\test\\semanticFeature_complete\\";
	String m_imageTestListPath = s_mainDatapath + "ImageList\\test\\TestImagelist.txt";
	String m_imageTestDescriptionPath = s_mainDatapath + "ImageData\\test\\test_tags.txt";
	String m_testGroundTruthPath = s_mainDatapath + "Groundtruth\\test\\";

	JButton m_openButton, m_searchButton, m_testButton, m_multipleTestButton;

	JLabel[] m_imageLabels = new JLabel[m_resultSize];
	JLabel m_queryImageLabel = new JLabel();
	JTextField m_queryDescription = new JTextField(20);

	JCheckBox m_colorHistogramCheckBox = new JCheckBox("Color Histogram");
	JCheckBox m_visualConceptCheckBox = new JCheckBox("Visual Concept");
	JCheckBox m_visualKeywordCheckBox = new JCheckBox("Visual Keywords");
	JCheckBox m_textCheckBox = new JCheckBox("Text");

	JProgressBar m_progressBar = new JProgressBar();

	TreeMap<String, Integer> m_categoryMap = new TreeMap<String, Integer>();
	TreeMap<Integer, String> m_invCategoryMap = new TreeMap<Integer, String>();
	TreeMap<String, ImageFile> m_imageMap = new TreeMap<String, ImageFile>();
	TreeMap<String, ImageFile> m_imageTestMap = new TreeMap<String, ImageFile>();
	static TreeMap<Integer, ArrayList<Integer> > s_semanticFeatureMap = new TreeMap<Integer, ArrayList<Integer> >();

	private void loadTrainingData() {
		// Begin Reading Image Category
		try {
			Scanner cin = new Scanner(new File(m_imageCategoryPath));
			while (cin.hasNext()) {
				String categoryName = cin.next();
				int index = m_categoryMap.size();
				m_categoryMap.put(categoryName, index);
				m_invCategoryMap.put(index, categoryName);
			}
			cin.close();
		} catch (Exception e) {
			System.out.println("Error! Failed to read CategoryName : " + e);
		}
		// End Reading Image Category
		

		// Begin Reading semantic features map
		try {
			int index = 0;
			Scanner cin = new Scanner(new File(m_semanticFeatureClass));
			while (cin.hasNext()) {
				String line = cin.nextLine();
				String[] splitStr = line.split(",");
				s_semanticFeatureMap.put(index, new ArrayList<Integer>());
				for(int i=0;i<3;i++){
					Integer indexMapping = m_categoryMap.get(splitStr[i]);
					if (m_categoryMap.get(splitStr[i]) != null) {
						s_semanticFeatureMap.get(index).add(indexMapping);
					}
				}
				index++;
			}
			cin.close();
		} catch (Exception e) {
			System.out.println("Error! Failed to read Semantic Features Category : " + e);
		}
		// End Reading semantic features map
		
		// Begin Reading image file names
		TreeSet<String> imageName = new TreeSet<String>();
		try {
			Scanner cin = new Scanner(new File(m_imageListPath));
			while (cin.hasNext()) {
				imageName.add(cin.nextLine());
			}
			cin.close();
		} catch (Exception e) {
			System.out.println("Error! Failed to read ImageList : " + e);
		}
		// End Reading image file names

		File dir = new File(m_imageDataPath); // path of the dataset
		File[] files = dir.listFiles();

		// Initialize ProgressBar

		m_progressBar.setMinimum(0);
		m_progressBar.setMaximum(files.length);
		m_progressBar.setStringPainted(true);
		m_contentPane.add(m_progressBar);
		m_contentPane.setVisible(true);
		setVisible(true);

		// Load Image Files
		for (int i = 0; i < files.length; i++) {
			m_progressBar.setValue(i);
			double currPercentage = (double) i / (double) files.length * 100.0;
			m_progressBar.setString(String.format("%.2f", currPercentage) + "%");

			String semanticFilename = files[i].getName();
			int pos = semanticFilename.lastIndexOf(".");
			if (pos > 0) {
				semanticFilename = semanticFilename.substring(0, pos);
			}
			File semanticFile = new File(m_imageSemanticFeaturePath + semanticFilename + ".txt");

			ImageFile currImage = new ImageFile(files[i], semanticFile, m_imageDataPgmPath);
			m_imageMap.put(currImage.m_name, currImage);
			imageName.remove(currImage.m_name); // Remove the image filem_name
												// from the list
		}

		// If it is not empty, there are missing m_imageMap
		if (!imageName.isEmpty()) {
			System.err.println("Error! Not all training images are read!");
			for (String temp : imageName) {
				System.out.println(temp);
			}
		}

		// Begin Reading image description texts
		try {
			Scanner cin = new Scanner(new File(m_imageDescriptionPath));
			while (cin.hasNext()) {
				String imageFileName = cin.next();
				String fulltext = cin.nextLine();
				ImageFile currImage = m_imageMap.get(imageFileName);
				Scanner innercin = new Scanner(fulltext);
				while (innercin.hasNext()) {
					currImage.m_description.add(innercin.next().trim().toLowerCase());
				}
				innercin.close();
			}
			cin.close();
		} catch (Exception e) {
			System.out.println("Error! Failed to read ImageDescription : " + e);
			e.printStackTrace();
		}
		// End Reading image description texts
		
		try {
			for (Map.Entry<String, Integer> entryCategory : m_categoryMap.entrySet()) {
				String categoryName = entryCategory.getKey();
				int index = entryCategory.getValue();
		
				Scanner groundTruthCin = new Scanner(new File(m_groundTruthPath + "Labels_" + categoryName + ".txt"));
				for (Map.Entry<String, ImageFile> entry : m_imageMap.entrySet()) {
					if (!groundTruthCin.hasNext())
						break;
					int valid = groundTruthCin.nextInt();
					if (valid == 1) {
						ImageFile currImage = entry.getValue();
						currImage.m_category.add(index);
					}
				}
				groundTruthCin.close();
			}
		}
		catch (Exception e) {
			System.out.println("Error! Failed to read training ground truth : " + e);
		}
		
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
		} catch (Exception e) {
			System.out.println("Error! Failed to read Test ImageList : " + e);
		}
		// End Reading image file names

		File dir = new File(m_imageTestDataPath); // path of the dataset
		File[] files = dir.listFiles();

		// Initialize ProgressBar

		m_progressBar.setMinimum(0);
		m_progressBar.setMaximum(files.length);
		m_progressBar.setStringPainted(true);
		m_contentPane.add(m_progressBar);
		m_contentPane.setVisible(true);
		setVisible(true);

		// Load Image Files
		for (int i = 0; i < files.length; i++) {
			m_progressBar.setValue(i);
			double currPercentage = (double) i / (double) files.length * 100.0;
			m_progressBar.setString(String.format("%.2f", currPercentage) + "%");

			String semanticFilename = files[i].getName();
			int pos = semanticFilename.lastIndexOf(".");
			if (pos > 0) {
				semanticFilename = semanticFilename.substring(0, pos);
			}
			File semanticFile = new File(m_imageTestSemanticFeaturePath + semanticFilename + ".txt");

			ImageFile currImage = new ImageFile(files[i], semanticFile, m_imageTestDataPgmPath);
			m_imageTestMap.put(currImage.m_name, currImage);
			imageName.remove(currImage.m_name); // Remove the image filem_name
												// from the list
		}

		// If it is not empty, there are missing m_imageMap
		if (!imageName.isEmpty()) {
			System.err.println("Error! Not all test images are read!");
			for (String temp : imageName) {
				System.out.println(temp);
			}
		}

		// Begin Reading image description texts
		try {
			Scanner cin = new Scanner(new File(m_imageTestDescriptionPath));
			while (cin.hasNext()) {
				String imageFileName = cin.next();
				String fulltext = cin.nextLine();
				ImageFile currImage = m_imageTestMap.get(imageFileName);
				Scanner innercin = new Scanner(fulltext);
				while (innercin.hasNext()) {
					currImage.m_description.add(innercin.next().trim().toLowerCase());
				}
				innercin.close();
			}
			cin.close();
		} catch (Exception e) {
			System.out.println("Error! Failed to read ImageTestDescription : " + e);
			e.printStackTrace();
		}
		// End Reading image description texts

		// Begin Reading Image Category
		try {
			Scanner cin = new Scanner(new File(m_imageCategoryPath));
			for (Map.Entry<String, Integer> entryCategory : m_categoryMap.entrySet()) {
				String categoryName = entryCategory.getKey();
				int index = entryCategory.getValue();

				Scanner groundTruthCin = new Scanner(new File(m_testGroundTruthPath + "Labels_" + categoryName + ".txt"));
				for (Map.Entry<String, ImageFile> entry : m_imageTestMap.entrySet()) {
					if (!groundTruthCin.hasNext())
						break;
					int valid = groundTruthCin.nextInt();
					if (valid == 1) {
						ImageFile currImage = entry.getValue();
						currImage.m_category.add(index);
					}
				}
				groundTruthCin.close();
			}
			cin.close();
		} catch (Exception e) {
			System.out.println("Error! Failed to read Test CategoryName : " + e);
		}
		// End Reading Image Category

		m_contentPane.setVisible(false);
		m_contentPane.remove(m_progressBar);
		// End of ProgressBar
	}

	public ImageSearch() {
		m_contentPane = (JPanel) this.getContentPane();
		setSize(m_windowWidth, m_windowHeight);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		loadTrainingData();
		loadTestData();

		// Main UI
		m_openButton = new JButton("Select an image...", GlobalHelper.createImageIcon("images/Open16.gif"));
		m_openButton.addActionListener(this);

		m_searchButton = new JButton("Search");
		m_searchButton.addActionListener(this);

		m_testButton = new JButton("Run Test");
		m_testButton.addActionListener(this);
		
		m_multipleTestButton = new JButton("Run Multiple Test");
		m_multipleTestButton.addActionListener(this);

		// For layout purposes, put the buttons in a separate panel
		JPanel buttonPanel = new JPanel(); // use FlowLayout
		buttonPanel.add(m_openButton);
		buttonPanel.add(m_searchButton);
		buttonPanel.add(m_testButton);
		buttonPanel.add(m_multipleTestButton);

		buttonPanel.add(m_colorHistogramCheckBox);
		buttonPanel.add(m_visualConceptCheckBox);
		buttonPanel.add(m_visualKeywordCheckBox);
		buttonPanel.add(m_textCheckBox);

		JPanel queryImagePanel = new JPanel();
		queryImagePanel.add(m_queryImageLabel);
		queryImagePanel.add(m_queryDescription);

		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(new GridLayout(4, 5));

		for (int i = 0; i < m_imageLabels.length; i++) {
			m_imageLabels[i] = new JLabel();
			m_imageLabels[i].setHorizontalTextPosition(JLabel.CENTER);
			m_imageLabels[i].setVerticalTextPosition(JLabel.BOTTOM);
			imagePanel.add(m_imageLabels[i]);
		}

		m_contentPane = (JPanel) this.getContentPane();
		setSize(m_windowWidth, m_windowHeight);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		m_contentPane.add(buttonPanel, BorderLayout.PAGE_START);
		m_contentPane.add(imagePanel, BorderLayout.CENTER);
		m_contentPane.add(queryImagePanel, BorderLayout.PAGE_END);

		m_contentPane.setVisible(true);
		setVisible(true);
		// add(logScrollPane, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		// Set up the file chooser.
		if (e.getSource() == m_openButton) {
			if (m_fc == null) {
				m_fc = new JFileChooser();

				// Add a custom file filter and disable the default
				// (Accept All) file filter.
				m_fc.addChoosableFileFilter(new ImageFilter());
				m_fc.setAcceptAllFileFilterUsed(false);

				// Add custom icons for file types.
				m_fc.setFileView(new ImageFileView());

				// Add the preview pane.
				m_fc.setAccessory(new ImagePreview(m_fc));
			}

			// Show it.
			int returnVal = m_fc.showDialog(ImageSearch.this, "Select an image..");

			// Process the results.
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				m_queryFile = m_fc.getSelectedFile();
			}

			if (m_queryFile != null) {
				try {
					String queryFileName = m_queryFile.toString();
					queryFileName = queryFileName.replaceAll("\\\\", "\\\\\\\\");
					File semanticArgsFile = new File(m_semanticFeaturePath + "args.txt");
					PrintWriter writer = new PrintWriter(semanticArgsFile, "UTF-8");
					writer.println(queryFileName);
					writer.close();

					String[] command = { "CMD", "/C", m_semanticFeatureExecutableName, semanticArgsFile.toString() };
					GlobalHelper.runExecutable(command, new File(m_semanticFeaturePath));

					String querySemanticFilename = m_queryFile.toString();
					int pos = querySemanticFilename.lastIndexOf(".");
					if (pos > 0) {
						querySemanticFilename = querySemanticFilename.substring(0, pos) + ".txt";
					}
					File querySemanticFile = new File(querySemanticFilename);

					m_queryImageFile = new ImageFile(m_queryFile, querySemanticFile);
					m_queryImage = ImageIO.read(m_queryFile);

					m_queryImageLabel.setIcon(new ImageIcon(m_queryImage));
					m_queryImageLabel.setText(m_queryFile.getName());

					semanticArgsFile.delete();
					querySemanticFile.delete();
				} catch (Exception e1) {
				}
			}

			m_fc.setSelectedFile(null);
		} else if (e.getSource() == m_searchButton) {
			m_queryImageFile.updateDescription(m_queryDescription.getText());
			TreeSet<ImageFile> result = getRank(m_queryImageFile);

			int m_imageLabelsIndex = 0;
			for (ImageFile currResult : result) {
				m_imageLabels[m_imageLabelsIndex].setIcon(new ImageIcon(currResult.m_bufferedImage));
				m_imageLabels[m_imageLabelsIndex].setText(m_invCategoryMap.get(currResult.m_category.first()) + " - "
						+ m_invCategoryMap.get(currResult.m_category.last()) + currResult.m_name);
				m_imageLabelsIndex++;
			}
		} else if (e.getSource() == m_testButton) {
			runTestIR(false);
		}
		else if (e.getSource() == m_multipleTestButton) {
			runMultipleTests();
		}
	}
	
	public TreeSet<ImageFile> getRank(ImageFile queryImage) {
		// Reset all scores
		for (Map.Entry<String, ImageFile> entry : m_imageMap.entrySet()) {
			ImageFile currImage = entry.getValue();
			currImage.resetScore();
		}

		if (m_colorHistogramCheckBox.isSelected())
			ColorHist.search(m_imageMap, queryImage);
		
		if (m_visualConceptCheckBox.isSelected())
			VisualConcept.search(m_imageMap, queryImage);
		
		if (m_visualKeywordCheckBox.isSelected())
			Sift.search(m_imageMap, queryImage);
		
		if (m_textCheckBox.isSelected())
			Text.search(m_imageMap, queryImage);

		TreeSet<ImageFile> result = new TreeSet<ImageFile>(new ImageFileScoreComparator());
		/* ranking the search results */
		
		for (Map.Entry<String, ImageFile> entry : m_imageMap.entrySet()) {
			ImageFile currImage = entry.getValue();
			currImage.m_score = (double)weightColorHist*currImage.m_colorHistScore + 
					(double)weightSemanticFeature*currImage.m_semanticFeatureScore + 
					(double)weightVisualConcept*currImage.m_visualConceptVectorScore + 
					(double)weightSift*currImage.m_siftScore + 
					(double)weightText*currImage.m_textScore;
			result.add(currImage);
			if (result.size() > m_resultSize)
				result.pollLast();
		}
		return result;
		
	}
	
	private int weightColorHist = 1;
	private int weightSemanticFeature = 1;
	private int weightVisualConcept = 1;
	private int weightSift = 1;
	private int weightText = 1;
	private double bestMAP = 0;
	private int bestWeightColorHist = 1;
	private int bestWeightSemanticFeature = 1;
	private int bestWeightVisualConcept = 1;
	private int bestWeightSift = 1;
	private int bestWeightText = 1;
	private int maxWeight = 10;
	
	TreeMap<Pair,Double> m_colorHistScoreMap = new TreeMap<Pair,Double>();
	TreeMap<Pair,Double> m_semanticFeatureScoreMap = new TreeMap<Pair,Double>();
	TreeMap<Pair,Double> m_visualConceptScoreMap = new TreeMap<Pair,Double>();
	TreeMap<Pair,Double> m_siftScoreMap = new TreeMap<Pair,Double>();
	TreeMap<Pair,Double> m_textScoreMap = new TreeMap<Pair,Double>();
	
	static String s_colorHistScorePath = s_mainDatapath + "ImageData\\SimilarityTable\\ColorHist.txt";
	static String s_semanticFeatureScorePath = s_mainDatapath + "ImageData\\SimilarityTable\\SemanticFeature.txt";
	static String s_visualConceptScorePath = s_mainDatapath + "ImageData\\SimilarityTable\\VisualConcept.txt";
	static String s_siftScorePath = s_mainDatapath + "ImageData\\SimilarityTable\\Sift.txt";
	static String s_textScorePath = s_mainDatapath + "ImageData\\SimilarityTable\\Text.txt";

	public TreeSet<ImageFile> getRankWithOutput(ImageFile queryImage) {
		// Reset all scores
		for (Map.Entry<String, ImageFile> entry : m_imageMap.entrySet()) {
			ImageFile currImage = entry.getValue();
			currImage.resetScore();
		}

		if (m_colorHistogramCheckBox.isSelected())
			ColorHist.search(m_imageMap, queryImage);
		
		if (m_visualConceptCheckBox.isSelected())
			VisualConcept.search(m_imageMap, queryImage);
		
		if (m_visualKeywordCheckBox.isSelected())
			Sift.search(m_imageMap, queryImage);
		
		if (m_textCheckBox.isSelected())
			Text.search(m_imageMap, queryImage);

		TreeSet<ImageFile> result = new TreeSet<ImageFile>(new ImageFileScoreComparator());
		/* ranking the search results */
		
		try {
			PrintWriter pw1 = new PrintWriter(new BufferedWriter(new FileWriter(s_colorHistScorePath, true)));
			PrintWriter pw2 = new PrintWriter(new BufferedWriter(new FileWriter(s_semanticFeatureScorePath, true)));
			PrintWriter pw3 = new PrintWriter(new BufferedWriter(new FileWriter(s_visualConceptScorePath, true)));
			PrintWriter pw4 = new PrintWriter(new BufferedWriter(new FileWriter(s_siftScorePath, true)));
			PrintWriter pw5 = new PrintWriter(new BufferedWriter(new FileWriter(s_textScorePath, true)));

			for (Map.Entry<String, ImageFile> entry : m_imageMap.entrySet()) {
				ImageFile currImage = entry.getValue();
				currImage.m_score = (double)weightColorHist*currImage.m_colorHistScore + 
						(double)weightSemanticFeature*currImage.m_semanticFeatureScore + 
						(double)weightVisualConcept*currImage.m_visualConceptVectorScore + 
						(double)weightSift*currImage.m_siftScore + 
						(double)weightText*currImage.m_textScore;
				result.add(currImage);
				if (result.size() > m_resultSize)
					result.pollLast();
				
				pw1.println(queryImage.m_name + " " + currImage.m_name + " " + currImage.m_colorHistScore);
				pw2.println(queryImage.m_name + " " + currImage.m_name + " " + currImage.m_semanticFeatureScore);
				pw3.println(queryImage.m_name + " " + currImage.m_name + " " + currImage.m_visualConceptVectorScore);
				pw4.println(queryImage.m_name + " " + currImage.m_name + " " + currImage.m_siftScore);
				pw5.println(queryImage.m_name + " " + currImage.m_name + " " + currImage.m_textScore);
				pw1.flush();
				pw2.flush();
				pw3.flush();
				pw4.flush();
				pw5.flush();
			}
			
			pw1.close();
			pw2.close();
			pw3.close();
			pw4.close();
			pw5.close();
		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
		
	}
	
	public TreeSet<ImageFile> getRankFromScoreFile(ImageFile queryImage) {
		// Reset all scores
		for (Map.Entry<String, ImageFile> entry : m_imageMap.entrySet()) {
			ImageFile currImage = entry.getValue();
			currImage.resetScore();
		}

		TreeSet<ImageFile> result = new TreeSet<ImageFile>(new ImageFileScoreComparator());
		/* ranking the search results */
		
		for (Map.Entry<String, ImageFile> entry : m_imageMap.entrySet()) {
			ImageFile currImage = entry.getValue();
			Double colorHistScore = m_colorHistScoreMap.get(new Pair(queryImage.m_name,currImage.m_name));
			Double semanticFeatureScore = m_semanticFeatureScoreMap.get(new Pair(queryImage.m_name,currImage.m_name));
			Double visualConceptScore = m_visualConceptScoreMap.get(new Pair(queryImage.m_name,currImage.m_name));
			Double siftScore = m_siftScoreMap.get(new Pair(queryImage.m_name,currImage.m_name));
			Double textScore = m_textScoreMap.get(new Pair(queryImage.m_name,currImage.m_name));
			
			currImage.m_score = (double)weightColorHist*colorHistScore + 
					(double)weightSemanticFeature*semanticFeatureScore + 
					(double)weightVisualConcept*visualConceptScore + 
					(double)weightSift*siftScore + 
					(double)weightText*textScore;
			result.add(currImage);
			if (result.size() > m_resultSize)
				result.pollLast();
		}
		return result;
	}
	
	private double runTestConfusionMatrix(boolean isScoreFromFile) {
		int[] globalMatrix = new int[4];
		for (Map.Entry<String, ImageFile> entry : m_imageTestMap.entrySet()) {
			ImageFile currImageTest = entry.getValue();
			TreeSet<ImageFile> result;
			if (isScoreFromFile)
				result = getRankFromScoreFile(currImageTest);
			else 
				result = getRank(currImageTest);

			int[] localMatrix = new int[4];
			for (ImageFile currResult : result) {
				int[] matrix = new int[4];
				currResult.getConfusionMatrix(currImageTest, matrix);
				localMatrix[0] += matrix[0];
				localMatrix[1] += matrix[1];
				localMatrix[2] += matrix[2];
				localMatrix[3] += matrix[3];
			}
			globalMatrix[0] += localMatrix[0];
			globalMatrix[1] += localMatrix[1];
			globalMatrix[2] += localMatrix[2];
			globalMatrix[3] += localMatrix[3];
		}
		System.out.println("Final Result");
		System.out.println("TP = " + globalMatrix[0] + " -- TN = " + globalMatrix[1] + " -- FP = " + globalMatrix[2]
				+ " -- FN = " + globalMatrix[3]);
		System.out.println("Recall    : " + GlobalHelper.getRecall(globalMatrix));
		System.out.println("Precision : " + GlobalHelper.getPrecision(globalMatrix));
		System.out.println("F1-Score  : " + GlobalHelper.getF1Score(globalMatrix));
		System.out.println();
		System.out.println();
		
		return GlobalHelper.getF1Score(globalMatrix);
	}
	
	private double runTestIR(boolean isScoreFromFile) {
		
		double totalPrecision = 0.0;
		
		for (Map.Entry<String, ImageFile> entry : m_imageTestMap.entrySet()) {
			ImageFile currImageTest = entry.getValue();
			TreeSet<ImageFile> result;
			if (isScoreFromFile)
				result = getRankFromScoreFile(currImageTest);
			else 
				result = getRank(currImageTest);
			
			int numRetrieved = result.size();
			int numRetrievedAndRelevant = 0;

			for (ImageFile currResult : result) {
				if (currResult.isRelevant(currImageTest)) {
					numRetrievedAndRelevant++;
				}
			}
			
			double currPrecision = (double)numRetrievedAndRelevant / (double)numRetrieved;
			totalPrecision += currPrecision;
		}
		
		double meanAveragePrecision = totalPrecision/(double)m_imageTestMap.size();
		
		System.out.println("MAP  : " + meanAveragePrecision);
		System.out.println();
		
		return meanAveragePrecision;
	}
	
	class Pair implements Comparable<Pair> {
		String first;
		String second;
		Pair(String _first, String _second) {
			first = _first;
			second = _second;
		}
		
		@Override
		public int compareTo(Pair other) {
			if (first.equals(other.first)){
				return second.compareTo(other.second);
			}
			return first.compareTo(other.first);
		}
		
		public boolean equals(Pair other) {
			return (first.equals(other.first) && second.equals(other.second));
		}
	}
	

	void readScore(TreeMap<Pair,Double> scoreMap, String scoreFilePath) {
		try {
			scoreMap.clear();
			Scanner cin = new Scanner(new File(scoreFilePath));
			while (cin.hasNext()) {
				String from = cin.next();
				String to = cin.next();
				Double score = cin.nextDouble();
				Pair newPair = new Pair(from,to);
				scoreMap.put(newPair, score);
			}
			cin.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void runMultipleTests() {
		readScore(m_colorHistScoreMap, s_colorHistScorePath);
		readScore(m_semanticFeatureScoreMap, s_semanticFeatureScorePath);
		readScore(m_visualConceptScoreMap, s_visualConceptScorePath);
		readScore(m_siftScoreMap, s_siftScorePath);
		readScore(m_textScoreMap, s_textScorePath);
		
		for(weightColorHist=0;weightColorHist<=maxWeight;weightColorHist++) {
			for(weightSemanticFeature=0;weightSemanticFeature<=maxWeight;weightSemanticFeature++) {
				for(weightVisualConcept=0;weightVisualConcept<=maxWeight;weightVisualConcept++) {
					for(weightSift=0;weightSift<=maxWeight;weightSift++) {
						for(weightText=0;weightText<=maxWeight;weightText++) {
							System.out.println(weightColorHist+"--"+weightSemanticFeature+"--"+weightVisualConcept+"--"+weightSift+"--"+weightText);
							double currMAP = runTestIR(true);
							if (currMAP > bestMAP) {
								bestMAP = currMAP;
								bestWeightColorHist = weightColorHist;
								bestWeightSemanticFeature = weightSemanticFeature;
								bestWeightVisualConcept = weightVisualConcept;
								bestWeightSift = weightSift;
								bestWeightText = weightText;
								System.out.println("Current Best MAP : " + bestMAP);
								System.out.println(bestWeightColorHist+"--"+bestWeightSemanticFeature+"--"+bestWeightVisualConcept+"--"+bestWeightSift+"--"+bestWeightText);
								System.out.println();
							}
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		ImageSearch example = new ImageSearch();
	}
}
