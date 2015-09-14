
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;

/*path of the dataset, and the size of search result could be changed here*/



class ImageFile implements Comparable<ImageFile>{
	BufferedImage m_bufferedImage;
	String m_name;
	String m_description;
	double m_score = 0.0;
	TreeSet<Integer> m_category = new TreeSet<Integer>();
	ImageFile (File _file) {
		try {
			m_bufferedImage = ImageIO.read(_file);
			m_name = _file.getName();
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

	File m_targetFile;
	int m_resultSize = 9;    //size of the searching result
	String m_imageDataPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageData\\train\\data_complete\\"; //the path of image dataset
	String m_imageListPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageList\\train\\TrainImagelist.txt";
	String m_imageDescriptionPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageData\\train\\train_tags.txt";
	String m_imageCategoryPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageData\\category_names.txt";
	String m_groundTruthPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\Groundtruth\\train\\";
	
    ColorHist m_colorHist = new ColorHist();
    JButton m_openButton, m_searchButton;
    
	JLabel [] m_imageLabels = new JLabel [ m_resultSize ];
	
	JCheckBox m_colorHistogramCheckBox = new JCheckBox("Color Histogram");
	JCheckBox m_visualConceptCheckBox = new JCheckBox("Visual Concept");
	JCheckBox m_visualKeywordCheckBox = new JCheckBox("Visual Keywords");
	JCheckBox m_textCheckbox = new JCheckBox("Text");
	
	JProgressBar m_progressBar = new JProgressBar();
	
	TreeMap<String, Integer> m_categoryMap = new TreeMap<String, Integer>();
	TreeMap<Integer, String> m_invCategoryMap = new TreeMap<Integer, String>();
	TreeMap<String, ImageFile> m_imageMap = new TreeMap<String, ImageFile>();

    public ImageSearch() {
    	
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
		m_contentPane = (JPanel)this.getContentPane();
		setSize(800,900);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		
		// Begin Reading image m_description texts
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
    	// End Reading image m_description texts
    	
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
        
		
		// Main UI
        m_openButton = new JButton("Select an image...",
                createImageIcon("images/Open16.gif"));
        m_openButton.addActionListener(this);
        
        m_searchButton = new JButton("Search");
        m_searchButton.addActionListener(this);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(m_openButton);
        buttonPanel.add(m_searchButton);
        
        JPanel algorithmPanel = new JPanel();
        algorithmPanel.add(m_colorHistogramCheckBox);
        algorithmPanel.add(m_visualConceptCheckBox);
        algorithmPanel.add(m_visualKeywordCheckBox);
        algorithmPanel.add(m_textCheckbox);
        
    	JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new GridLayout(0,3));
        
        for (int i = 0; i<m_imageLabels.length;i++){
        	m_imageLabels[i] = new JLabel();
        	m_imageLabels[i].setHorizontalTextPosition(JLabel.CENTER);
        	m_imageLabels[i].setVerticalTextPosition(JLabel.BOTTOM);
        	imagePanel.add(m_imageLabels[i]);
        }

		m_contentPane = (JPanel)this.getContentPane();
		setSize(800,900);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        m_contentPane.add(buttonPanel, BorderLayout.PAGE_START);
        m_contentPane.add(algorithmPanel, BorderLayout.PAGE_END);
        m_contentPane.add(imagePanel, BorderLayout.CENTER);
        
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
	            m_targetFile = m_fc.getSelectedFile();
	        }
	
	        m_fc.setSelectedFile(null);
        }
        else if (e.getSource() == m_searchButton) {
        	BufferedImage targetImage = null;
        	try {
        		targetImage = ImageIO.read(m_targetFile);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	TreeSet<ImageFile> result = null;
			try {
				result = m_colorHist.search (m_imageMap, targetImage, m_resultSize);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
			int m_imageLabelsIndex = 0;
			for(ImageFile currResult : result) {
				m_imageLabels[m_imageLabelsIndex].setIcon(new ImageIcon(currResult.m_bufferedImage));
				m_imageLabels[m_imageLabelsIndex].setText(m_invCategoryMap.get(currResult.m_category.first()) + " - " + currResult.m_name + " - " + currResult.m_description);
				m_imageLabelsIndex++;
			}
        }
    }

	public static void main(String[] args) {
	    @SuppressWarnings("unused")
		ImageSearch example = new ImageSearch();
    }
}
