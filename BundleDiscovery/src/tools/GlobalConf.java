package tools;

import sax.SaxData;

import java.util.ArrayList;
import java.util.HashMap;

public class GlobalConf {
	public static boolean runExperiment = false;

	public static int heuristic = 1;
	public static int tsLength = 261;
	public static int readCount = 100;
	public static int year = 2018;
	public static int delta = 50;
	public static double minNoTS = 20;
	public static double epsilon = 2;
	public static int zNormalize = 0;

	// ###### OTHER PARAMETERS
	public static double minMax[];
	public static HashMap<Short, Double[]> breakpoints;
	public static ArrayList<HashMap<Short, ArrayList<SaxData>>> bins;
	public static short maxCard = 512;
	public static String stockDataPath = "/Users/gchatzi/Recovered Files – 28 Aug 2019, 9:58:27 PM/Documents/Data/StockData.nosync/China/";
	public static String eegDataPath = "/Users/gchatzi/Documents/Data/EEGData/";
	public static String eegFile = eegDataPath + "trial0.txt";
	public static String dataFile = "stockTS.csv";
	//public static String dataFile = eegDataPath + "eegTS.csvzNorm.csv";
	public static String resultFile = "/Users/gchatzi/Documents/Data/BundleDiscovery/resultFlocks.txt.nosync";
	public static String resultPath = "/Users/gchatzi/Documents/Data/BundleDiscovery/";
	public static double datasetSize;

}