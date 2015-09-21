import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

class Gene {
	double[] w = new double[5];
	double f1score;

	Gene() {
		f1score = 0.0;
		w[0] = w[2] = w[3] = w[4] = w[5] = 1.0;
	}

	Gene(double _w1, double _w2, double _w3, double _w4, double _w5) {
		w[0] = _w1;
		w[1] = _w2;
		w[2] = _w3;
		w[3] = _w4;
		w[4] = _w5;
	}
}

class GeneComparator implements Comparator<Gene> {
	@Override
	public int compare(Gene o1, Gene o2) {
		if (o1.f1score == o2.f1score)
			return 0;
		else if (o1.f1score < o2.f1score)
			return 1;
		else
			return -1;
	}
}

public class GeneticAlgorithm {
	TreeMap<String, ImageFile> m_imageMap;
	TreeMap<String, ImageFile> m_imageTestMap;
	
	// For GA
	private int bestNGenes = 5;
	private double rangeMin = 0;
	private double rangeMax = 1000;
	private int maxGenerations = 1000000;
	private double mutationChance = 0.25;
	
	TreeMap<Pair, Double> m_colorHistScoreMap = new TreeMap<Pair, Double>();
	TreeMap<Pair, Double> m_semanticFeatureScoreMap = new TreeMap<Pair, Double>();
	TreeMap<Pair, Double> m_visualConceptScoreMap = new TreeMap<Pair, Double>();
	TreeMap<Pair, Double> m_siftScoreMap = new TreeMap<Pair, Double>();
	TreeMap<Pair, Double> m_textScoreMap = new TreeMap<Pair, Double>();
	
	
	GeneticAlgorithm(TreeMap<String, ImageFile> imageMap, TreeMap<String, ImageFile> imageTestMap) {
		m_imageMap = imageMap;
		m_imageTestMap = imageTestMap;
	}
	
	// Duplicated method from ImageSearch class, please be aware when changing this method
	public TreeSet<ImageFile> getRankFromScoreFile(ImageFile queryImage, double weightColorHist,
			double weightSemanticFeature, double weightVisualConcept, double weightSift, double weightText) {
		// Reset all scores
		for (Map.Entry<String, ImageFile> entry : m_imageMap.entrySet()) {
			ImageFile currImage = entry.getValue();
			currImage.resetScore();
		}

		TreeSet<ImageFile> result = new TreeSet<ImageFile>(new ImageFileScoreComparator());
		/* ranking the search results */

		for (Map.Entry<String, ImageFile> entry : m_imageMap.entrySet()) {
			ImageFile currImage = entry.getValue();
			Double colorHistScore = m_colorHistScoreMap.get(new Pair(queryImage.m_name, currImage.m_name));
			Double semanticFeatureScore = m_semanticFeatureScoreMap.get(new Pair(queryImage.m_name, currImage.m_name));
			Double visualConceptScore = m_visualConceptScoreMap.get(new Pair(queryImage.m_name, currImage.m_name));
			Double siftScore = m_siftScoreMap.get(new Pair(queryImage.m_name, currImage.m_name));
			Double textScore = m_textScoreMap.get(new Pair(queryImage.m_name, currImage.m_name));

			currImage.m_score = weightColorHist * colorHistScore
					+ weightSemanticFeature * semanticFeatureScore
					+ weightVisualConcept * visualConceptScore + weightSift * siftScore
					+ weightText * textScore;
			result.add(currImage);
			if (result.size() > ImageSearch.s_resultSize)
				result.pollLast();
		}
		return result;
	}
	
	private double runTestGA(Gene gene) {
		double totalF1 = 0.0;

		for (Map.Entry<String, ImageFile> entry : m_imageTestMap.entrySet()) {
			ImageFile currImageTest = entry.getValue();
			TreeSet<ImageFile> result;
			result = getRankFromScoreFile(currImageTest, gene.w[0], gene.w[1], gene.w[2], gene.w[3], gene.w[4]);

			int numRetrieved = result.size();
			int numRetrievedAndRelevant = 0;
			int numRelevant = 0;

			for (ImageFile currResult : result) {
				if (currResult.isRelevant(currImageTest)) {
					numRetrievedAndRelevant++;
				}
			}

			for (Map.Entry<String, ImageFile> innerEntry : m_imageMap.entrySet()) {
				ImageFile currImage = innerEntry.getValue();
				if (currImage.isRelevant(currImageTest)) {
					numRelevant++;
				}
			}

			double currPrecision = (double) numRetrievedAndRelevant / (double) numRetrieved;
			double currRecall = (double) numRetrievedAndRelevant / (double) numRelevant;
			totalF1 += GlobalHelper.getF1Score(currPrecision, currRecall);
		}

		double meanF1 = totalF1 / m_imageTestMap.size();

		gene.f1score = meanF1;

		return meanF1;
	}
	
	public void generateRandomGenes(ArrayList<Gene> geneList) {
		Random random = new Random();
		for (int j = 0; j < bestNGenes; j++) {
			double[] value = new double[5];
			for (int i = 0; i < 5; i++) {
				value[i] = rangeMin + (rangeMax - rangeMin) * random.nextDouble();
			}
			Gene gene = new Gene(value[0], value[1], value[2], value[3], value[4]);
			geneList.add(gene);
		}
	}

	private Gene generateOffspring(Gene gene1, Gene gene2) {
		Random randomCrossover = new Random();
		Random randomMutation = new Random();
		Random randomPositiveMutation = new Random();
		Random randomValue = new Random();
		double[] value = new double[5];
		for (int i = 0; i < 5; i++) {

			// Crossover
			if (randomCrossover.nextDouble() < 0.5) {
				value[i] = gene1.w[i];
			} else {
				value[i] = gene2.w[i];
			}

			// Mutation
			double randMutation = randomMutation.nextDouble();
			double randPositive = randomPositiveMutation.nextDouble();
			double randValue = randomValue.nextDouble();
			if (randMutation < mutationChance / 4.0) {
				if (randPositive < 0.5) {
					value[i] = value[i] + (randValue * 100) + 1;
				} else {
					value[i] = value[i] - (randValue * 100) + 1;
				}
			} else if (randMutation < mutationChance / 2.0) {
				if (randPositive < 0.5) {
					value[i] = value[i] + (randValue * 10) + 1;
				} else {
					value[i] = value[i] - (randValue * 10) + 1;
				}
			} else if (randMutation < mutationChance) {
				if (randPositive < 0.5) {
					value[i] = value[i] + (randValue * 2);
				} else {
					value[i] = value[i] - (randValue * 2);
				}
			}
			// Make sure attributes have proper signs
			if (((i < 6) || (i == 11)) && value[i] < 0) {
				value[i] = -1 * value[i];
			} else if (((i >= 6) && (i <= 10)) && value[i] > 0) {
				value[i] = -1 * value[i];
			}
		}
		Gene offspring = new Gene(value[0], value[1], value[2], value[3], value[4]);
		return offspring;
	}

	public void generateNewGenes(ArrayList<Gene> geneList) {
		ArrayList<Gene> tempGeneList = new ArrayList<Gene>();
		for (Gene gene : geneList) {
			tempGeneList.add(gene);
		}
		geneList.clear();

		for (int i = 0; i < bestNGenes - 1; i++) {
			for (int j = i + 1; j < bestNGenes; j++) {
				Gene offSpring = generateOffspring(tempGeneList.get(i), tempGeneList.get(j));
				geneList.add(offSpring);
			}
		}
		for (int i = 0; i < bestNGenes; i++) {
			Gene fittestGene = tempGeneList.get(i);
			geneList.add(fittestGene);
		}
		tempGeneList.clear();
	}
	
	void readScore(TreeMap<Pair, Double> scoreMap, String scoreFilePath) {
		try {
			scoreMap.clear();
			Scanner cin = new Scanner(new File(scoreFilePath));
			while (cin.hasNext()) {
				String from = cin.next();
				String to = cin.next();
				Double score = cin.nextDouble();
				Pair newPair = new Pair(from, to);
				scoreMap.put(newPair, score);
			}
			cin.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void runGA() {
		readScore(m_colorHistScoreMap, ImageSearch.s_colorHistScorePath);
		readScore(m_semanticFeatureScoreMap, ImageSearch.s_semanticFeatureScorePath);
		readScore(m_visualConceptScoreMap, ImageSearch.s_visualConceptScorePath);
		readScore(m_siftScoreMap, ImageSearch.s_siftScorePath);
		readScore(m_textScoreMap, ImageSearch.s_textScorePath);

		ArrayList<Gene> geneList = new ArrayList<Gene>();

		generateRandomGenes(geneList);
		for (int i = 0; i < maxGenerations; i++) {
			System.out.println("--------- Generation " + i + " ----------");
			System.err.println("--------- Generation " + i + " ----------");
			generateNewGenes(geneList);
			generateRandomGenes(geneList);
			for (Gene currGene : geneList) {
				currGene.f1score = runTestGA(currGene);
				System.out.println(currGene.w[0] + "--" + currGene.w[1] + "--" + currGene.w[2] + "--" + currGene.w[3]
						+ "--" + currGene.w[4]);
				System.out.println("Mean f1-Score : " + currGene.f1score);

			}
			geneList.sort(new GeneComparator());
			for (int z = 0; z < bestNGenes; z++) {
				Gene currGene = geneList.get(z);
				System.err.println(currGene.w[0] + "--" + currGene.w[1] + "--" + currGene.w[2] + "--" + currGene.w[3]
						+ "--" + currGene.w[4]);
				System.err.println("Mean f1-Score : " + currGene.f1score);
			}
		}
	}
}