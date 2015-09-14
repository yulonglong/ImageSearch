
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;

/*path of the dataset, and the size of search result could be changed here*/



class ImageFile implements Comparable<ImageFile>{
	BufferedImage bufferedImage;
	File file;
	String name;
	String description;
	double score = 0.0;
	ImageFile (File _file) {
		file = _file;
		try {
			bufferedImage = ImageIO.read(file);
			name = file.getName();
		}
		catch (Exception e ) {
			System.out.println("Image File exception : " + e);
		}
	}
	Double getScore() {
		return score;
	}
	public int compareTo(ImageFile thatImage) {
        return this.name.compareTo(thatImage.name);
    }
}

class TreeSetScoreComparator implements Comparator<ImageFile> {
    @Override
    public int compare(ImageFile e1, ImageFile e2) {
        return e2.getScore().compareTo(e1.getScore());
    }
}

public class ImageSearch extends JFrame implements ActionListener {
	static final long serialVersionUID = 42L;
	
    JFileChooser fc;
	JPanel contentPane;

	int resultsize = 9;    //size of the searching result
	String datasetpath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageData\\train\\data_complete\\"; //the path of image dataset
	String imageListPath = "D:\\GitHub\\ImageSearchFull\\Assignment1\\ImageList\\train\\TrainImagelist.txt";
    ColorHist colorhist = new ColorHist();
    JButton openButton, searchButton;
	BufferedImage bufferedimage;
    
	JLabel [] imageLabels = new JLabel [ resultsize ];
	
	JCheckBox colorHistogramCheckBox = new JCheckBox("Color Histogram");
	JCheckBox visualConceptCheckBox = new JCheckBox("Visual Concept");
	JCheckBox visualKeywordCheckBox = new JCheckBox("Visual Keywords");
	JCheckBox textCheckbox = new JCheckBox("Text");
	
	JProgressBar progressBar = new JProgressBar();
	
	File file = null;
	TreeSet<ImageFile> images = new TreeSet<ImageFile>();


    public ImageSearch() {
    	// Begin Reading image file names
    	TreeSet<String> imageName = new TreeSet<String>();
    	try {
        	Scanner cin = new Scanner(new File(imageListPath));
        	while (cin.hasNext()) {
        		imageName.add(cin.nextLine());
        	}
        	cin.close();
    	}
    	catch (Exception e) {
    		System.out.println("Error! Failed to read ImageList : " + e);
    	}
    	// End Reading image file names
    	
    	
		File dir = new File(datasetpath);  //path of the dataset
		File [] files = dir.listFiles();
		
		// Initialize ProgressBar
		contentPane = (JPanel)this.getContentPane();
		setSize(800,900);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	progressBar.setMinimum(0);
    	progressBar.setMaximum(files.length);
    	progressBar.setStringPainted(true);
    	contentPane.add(progressBar);
    	contentPane.setVisible(true);
    	setVisible(true);
		
    	// Load Image Files
		for (int i=0; i < files.length;i++){
			progressBar.setValue(i);
			double currPercentage = (double)i/(double)files.length * 100.0;
			progressBar.setString(String.format("%.2f", currPercentage) + "%");
			ImageFile currImage = new ImageFile(files[i]);
			images.add(currImage);
			imageName.remove(currImage.name); // Remove the image filename from the list
		}
		
		// If it is not empty, there are missing images
		if (!imageName.isEmpty()) {
			System.err.println("Error! Not all training images are read!");
			for (String temp: imageName) {
				System.out.println(temp);
			}
		}
		
		contentPane.setVisible(false);
		contentPane.remove(progressBar);
		// End of ProgressBar
        
		
		
		// Main UI
        openButton = new JButton("Select an image...",
                createImageIcon("images/Open16.gif"));
        openButton.addActionListener(this);
        
        searchButton = new JButton("Search");
        searchButton.addActionListener(this);

        //For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); //use FlowLayout
        buttonPanel.add(openButton);
        buttonPanel.add(searchButton);
        
        JPanel algorithmPanel = new JPanel();
        algorithmPanel.add(colorHistogramCheckBox);
        algorithmPanel.add(visualConceptCheckBox);
        algorithmPanel.add(visualKeywordCheckBox);
        algorithmPanel.add(textCheckbox);
        
    	JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new GridLayout(0,3));
        
        for (int i = 0; i<imageLabels.length;i++){
        	imageLabels[i] = new JLabel();
        	imageLabels[i].setHorizontalTextPosition(JLabel.CENTER);
        	imageLabels[i].setVerticalTextPosition(JLabel.BOTTOM);
        	imagePanel.add(imageLabels[i]);
        }

		contentPane = (JPanel)this.getContentPane();
		setSize(800,900);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        contentPane.add(buttonPanel, BorderLayout.PAGE_START);
        contentPane.add(algorithmPanel, BorderLayout.PAGE_END);
        contentPane.add(imagePanel, BorderLayout.CENTER);
        
        contentPane.setVisible(true);
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
        if (e.getSource() == openButton) {
        if (fc == null) {
            fc = new JFileChooser();

	    //Add a custom file filter and disable the default
	    //(Accept All) file filter.
            fc.addChoosableFileFilter(new ImageFilter());
            fc.setAcceptAllFileFilterUsed(false);

	    //Add custom icons for file types.
            fc.setFileView(new ImageFileView());

	    //Add the preview pane.
            fc.setAccessory(new ImagePreview(fc));
        } 
        

        //Show it.
        int returnVal = fc.showDialog(ImageSearch.this,
                                      "Select an image..");

        //Process the results.
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();

        }

        fc.setSelectedFile(null);
        }else if (e.getSource() == searchButton) {
        	
        	try {
				bufferedimage = ImageIO.read(file);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	TreeSet<ImageFile> result = null;
			try {
				result = colorhist.search (images, bufferedimage, resultsize);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
			int imageLabelsIndex = 0;
			for(ImageFile currResult : result) {
				imageLabels[imageLabelsIndex].setIcon(new ImageIcon(currResult.bufferedImage));
				imageLabels[imageLabelsIndex].setText(currResult.name);
				imageLabelsIndex++;
			}
        }
    }

	public static void main(String[] args) {
	    @SuppressWarnings("unused")
		ImageSearch example = new ImageSearch();
    }
}
