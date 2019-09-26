package tools;

import bdiscovery.Bundle;
import sax.Sax;
import sax.SaxData;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Functions {

	public static short[] UnsignedShortArray(int len, short j) {
		short[] array = new short[len];
		for (int i = 0; i < array.length; i++)
			array[i] = j;
		return array;
	}

	public static double mean(double[] data, int index1, int index2) throws Exception {
		if (index1 < 0 || index2 < 0 || index1 >= data.length || index2 >= data.length) {
			throw new Exception("Invalid index!");
		}

		if (index1 > index2) {
			int temp = index2;
			index2 = index1;
			index1 = temp;
		}

		double sum = 0;
		for (int i = index1; i <= index2; i++) {
			sum += data[i];
		}

		return sum / (index2 - index1 + 1);
	}

	public static int log(short x, int base) {
		return (int) (Math.log(x) / Math.log(base));
	}

	public static int countLeadZs(int x) {
		int displayMask = 1 << 31;
		int cnt = 0;
		for (int c = 1; c <= 32; c++) {
			if ((x & displayMask) == 0) {
				cnt++;
			} else {
				return cnt;
			}
			x <<= 1;
		}
		return cnt;
	}

	private static int cardinalityBitDelta(int c0, int c1) {
		int c0_bits = numberBitsInCardinality(c0);
		int c1_bits = numberBitsInCardinality(c1);
		return Math.abs(c1_bits - c0_bits);
	}

	public static String promote(int sax, int targetSax, int card, int targetCard) throws Exception {
		int iNewBits = cardinalityBitDelta(card, targetCard);
		int iTargetSAXNumber_Prefix = (targetSax >> iNewBits) << iNewBits;
		int iLocalSAXNumber_Prefix = (sax << iNewBits);
		int resultSax = 0;
		if (iTargetSAXNumber_Prefix == iLocalSAXNumber_Prefix) {
			resultSax = targetSax;
		} else if (iTargetSAXNumber_Prefix > iLocalSAXNumber_Prefix) {
			resultSax = iLocalSAXNumber_Prefix;
			int iMask = 1;

			for (int x = 0; x < iNewBits; x++) {
				resultSax = resultSax ^ iMask;
				iMask = iMask << 1;
			}
		} else if (iTargetSAXNumber_Prefix < iLocalSAXNumber_Prefix) {
			resultSax = iLocalSAXNumber_Prefix;
		} else {
			throw new Exception("Case not meant to happen?");
		}
		return Integer.toString(resultSax);
	}

	public static int numberBitsInCardinality(int card) {
		return 32 - countLeadZs(card);
	}

	public static double[] getMinMax(String dataFile) throws Exception {

		final InputStream in = new BufferedInputStream(new FileInputStream(dataFile));
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String line;
		int count = 0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		while ((line = br.readLine()) != null && count < GlobalConf.readCount) {
			String[] parts = line.split(",");
			double[] ts = parseTS(parts);

			for (int i = 0; i < ts.length; i++) {
				double measurement = ts[i];
				if (measurement > max)
					max = measurement;
				if (measurement < min)
					min = measurement;
			}
			//System.out.println(++count);
		}

		double minMax[] = {min, max};
		return minMax;
	}

	static double[] parseTS(String[] parts) {
		double[] ts = new double[parts.length-3];
		for (int i = 3; i < parts.length; i++) {
			ts[i - 3] = Double.parseDouble(parts[i]);
		}
		return ts;
	}

	public static void writeResults(ArrayList<Bundle> results, String resultFile) throws IOException {

		File file = new File(resultFile);
		if (file.exists())
			file.delete();

		FileWriter fw = new FileWriter(resultFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 0; i < results.size(); i++) {
			Bundle bundle = results.get(i);
			//String toWrite = "Bundle_" + i + " [" + bundle.getFromToIndex()[0] + "-" + bundle.getFromToIndex()[1] + "] " + bundle.getMembers().size() + " Members:";
			String toWrite = "Bundle_" + i + ";";
			for (String member : bundle.getMembers().keySet()) {
				toWrite += member + ",";
			}
			toWrite = toWrite.substring(0, toWrite.length() - 1);
			toWrite += ";[" + bundle.getFromToIndex()[0] + "-" + bundle.getFromToIndex()[1] + "]\n";
			bw.write(toWrite);
		}

		bw.close();
	}

	public static void generateBreakpoints() {
		GlobalConf.breakpoints = new HashMap<>();
		double times = ((GlobalConf.minMax[1] - GlobalConf.minMax[0]) / GlobalConf.epsilon) + 1;

		double epsilon;
		while (times > 1) {
			times--;
			epsilon = (GlobalConf.minMax[1] - GlobalConf.minMax[0]) / times;
			double mod = (GlobalConf.minMax[1] - GlobalConf.minMax[0]) % epsilon;
			double low = GlobalConf.minMax[0] + (mod / 2);
			double high = GlobalConf.minMax[1] + (mod / 2);

			LinkedList<Double> tmpBreakpoints = new LinkedList<>();
			tmpBreakpoints.add(low);
			double tmp = low;
			for (int i = 0; i < times; i++) {
				tmp = tmp + epsilon;
				if (tmp >= high)
					break;
				tmpBreakpoints.add(tmp);
			}
			tmpBreakpoints.add(high);

			Double[] brpointArray = tmpBreakpoints.toArray(new Double[tmpBreakpoints.size()]);
			brpointArray = Arrays.copyOf(brpointArray, brpointArray.length - 1);
			GlobalConf.breakpoints.put((short) times, brpointArray);
		}
	}

	public static void fillBins(String dataFile) throws Exception {
		int processed = 0;
		boolean allRead = false;
		GlobalConf.bins = new ArrayList<>();
		final InputStream in = new BufferedInputStream(new FileInputStream(dataFile));
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		while (!(allRead)) {
			String line;
			while ((line = br.readLine()) != null && processed < GlobalConf.readCount) {

				String[] parts = line.split(",");
				String id = parts[0];
				double[] ts = Functions.parseTS(parts);
				TimeSeries timeSeries = new TimeSeries(id, ts);

				short[] maxCardSAX = Sax.convertSAX(ts, GlobalConf.tsLength, (short) (GlobalConf.maxCard - 1));
				SaxData sd = new SaxData(timeSeries, maxCardSAX, id);

				for (int i = 0; i < GlobalConf.tsLength; i++) {
					if (processed == 0) {
						ArrayList<SaxData> tmp1 = new ArrayList();
						tmp1.add(sd);
						HashMap<Short, ArrayList<SaxData>> tmp2 = new HashMap<>();
						tmp2.put(maxCardSAX[i], tmp1);
						GlobalConf.bins.add(tmp2);
					} else if (GlobalConf.bins.get(i).containsKey(maxCardSAX[i])) {
						GlobalConf.bins.get(i).get(maxCardSAX[i]).add(sd);
					} else {
						ArrayList<SaxData> tmp1 = new ArrayList();
						tmp1.add(sd);
						HashMap<Short, ArrayList<SaxData>> tmp2 = new HashMap<>();
						tmp2.put(maxCardSAX[i], tmp1);
						GlobalConf.bins.get(i).put(maxCardSAX[i], tmp1);
					}
				}

				processed++;

				if (processed % 10000 == 0) {
					System.out.println(processed + "time series inserted.");
				}
			}

			if (line == null)
				allRead = true;
		}
		in.close();
		br.close();

		GlobalConf.datasetSize = processed;
		//System.out.println("Total: " + processed);
		//System.out.println("Complete. " + processed + " inserted.");
		System.out.println("Insert complete!");
		//System.out.println();
	}

	public static double[] zNormalization(double[] timeSeries) throws Exception {

		double mean = mean(timeSeries, 0, timeSeries.length - 1);
		double std = stdDev(timeSeries);

		double[] normalized = new double[timeSeries.length];

		if (std == 0)
			std = 1;

		for (int i = 0; i < timeSeries.length; i++) {
			normalized[i] = (timeSeries[i] - mean) / std;
		}

		return normalized;
	}

	public static double stdDev(double[] timeSeries) throws Exception {
		double mean = mean(timeSeries, 0, timeSeries.length - 1);
		double var = 0.0;

		for (int i = 0; i < timeSeries.length; i++) {
			var += (timeSeries[i] - mean) * (timeSeries[i] - mean);
		}
		var /= (timeSeries.length - 1);

		return Math.sqrt(var);
	}

	public static void zNormalize(String dataFile) throws Exception {

		final InputStream in = new BufferedInputStream(new FileInputStream(dataFile));
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile + "zNorm.csv"));

		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(",");
			String id = parts[0];

			int countZero = 0;
			double[] ts = new double[GlobalConf.tsLength];
			int sameAsPreviousCount = 0;
			double prev = 0;
			for (int i = 3; i < GlobalConf.tsLength + 3; i++) {
				double curr = Double.parseDouble(parts[i]);
				ts[i - 3] = curr;
				if (ts[i - 3] <= 0.1)
					countZero++;
				if (prev==curr)
					sameAsPreviousCount++;
				prev = curr;
			}
			if (countZero > 500)
				continue;
			if (sameAsPreviousCount > 500)
				continue;
			ts = zNormalization(ts);

			writer.write(id + ", X, Y");
			for (int i = 0; i < ts.length; i++) {
				writer.write(", " + ts[i]);
			}
			writer.write("\n");
		}

		br.close();
		writer.close();
	}

	public static double[] countResultMetrics(ArrayList<Bundle> results) {

		double[] metrics = new double[3];
		metrics[0] = results.size();

		double maxLength = 0.0;
		double minLength = Double.MAX_VALUE;
		int maxLengthIndex = 0;
		int minLengthIndex = 0;
		double maxSize = 0.0;
		for (int i = 0; i < results.size(); i++) {

			Bundle bundle = results.get(i);
			double length = bundle.getFromToIndex()[1] - bundle.getFromToIndex()[0];
			if (length > maxLength) {
				maxLength = length;
				maxLengthIndex = i;
			}

			if (length < minLength) {
				minLength = length;
				minLengthIndex = i;
			}

			double size = bundle.getMembers().size();
			if (size > maxSize) {
				maxSize = size;

			}
		}

		metrics[1] = maxLength;
		metrics[2] = maxSize;

		//System.out.println("Max length flock:" + maxLengthIndex + " size: " + maxLength);
		//System.out.println("Min length flock:" + minLengthIndex + " size: " + minLength);
		return metrics;
	}

	public static void groupBins(int j, HashMap<Integer, ArrayList<SaxData>> candidateGroups) {

		HashMap<Short, ArrayList<SaxData>> currentSegment = GlobalConf.bins.get(j);
		ArrayList<Short> sortedKeys = new ArrayList(GlobalConf.bins.get(j).keySet());
		Collections.sort(sortedKeys);

		int count = 0;
		for (short key : sortedKeys) {
			ArrayList<SaxData> bin = currentSegment.get((short) (key + 1));
			if (bin != null) {
				ArrayList<SaxData> tmp = new ArrayList<>();
				tmp.addAll(bin);
				tmp.addAll(currentSegment.get(key));
				if (tmp.size() >= GlobalConf.minNoTS)
					candidateGroups.put(count++, tmp);
			} else {
				if (currentSegment.get(key).size() >= GlobalConf.minNoTS)
					candidateGroups.put(count++, currentSegment.get(key));
			}
		}
	}

	public static void readStockData(String stockDataPath, int year) throws IOException {
		File folder = new File(stockDataPath);
		File[] listOfFiles = folder.listFiles();

		File stocks = new File("stockTS.csv");
		File stocksZNorm = new File("stockTS.csvzNorm.csv");
		Files.deleteIfExists(stocks.toPath());
		Files.deleteIfExists(stocksZNorm.toPath());
		FileWriter fileWriter = new FileWriter(stocks);
		PrintWriter printWriter = new PrintWriter(fileWriter);

		for (File file : listOfFiles) {
			if (file.isFile()) {
				if (yearExists(file, year)) {
					String parts1[] = String.valueOf(file.toPath()).split("/");
					String filename = (parts1[parts1.length-1]).split("\\.")[0];
					printWriter.print(filename + ", X, " + "Y");
					BufferedReader reader = new BufferedReader(new FileReader(file));
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.equals("")) continue;
						String[] parts2 = line.split(",");
						int readYear = Integer.parseInt(parts2[0].split("/")[2]);
						if (year == readYear) {
							printWriter.write(", " + parts2[5]);
						}
					}
					printWriter.write("\n");
					reader.close();
				}
			}
		}

		printWriter.close();
	}

	private static boolean yearExists(File file, int year) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		int containsAll = 0;
		while ((line = reader.readLine()) != null) {
			if (line.equals("")) continue;
			String[] parts = line.split(",");
			int readYear = 0;
			try {
				readYear = Integer.parseInt(parts[0].split("/")[2]);
			} catch (Exception e) {
				System.currentTimeMillis();
			}
			if (year == readYear)
				containsAll++;
		}
		reader.close();

		if (containsAll == 261)
			return true;
		return false;
	}

	public static void readEEGData(String eegDataPath) throws IOException {

		File eegOut = new File(eegDataPath + "eegTS.csv");
		Files.deleteIfExists(eegOut.toPath());
		FileWriter fileWriter = new FileWriter(eegOut);
		PrintWriter printWriter = new PrintWriter(fileWriter);

		File eegIn = new File(GlobalConf.eegFile);
		BufferedReader reader = new BufferedReader(new FileReader(eegIn));

		HashMap<String, double[]> eegData = new HashMap<>();
		String line = reader.readLine();
		String[] columnNames = line.split("\t");
		for (int i = 1; i < columnNames.length; i++) {
			eegData.put(columnNames[i], new double[7500]);
		}

		int t = 0;
		while ((line = reader.readLine()) != null) {
			String[] columns = line.split("\t");
			for (int i = 1; i < eegData.size(); i++) {
				eegData.get(columnNames[i])[t] = Double.parseDouble(columns[i]);
			}
			t++;
		}

		for (String k : eegData.keySet()) {
			printWriter.write(k + ",X,Y");
			for (int i=0; i<7500; i++) {
				printWriter.write("," + eegData.get(k)[i]);
			}
			printWriter.write("\n");
		}
		reader.close();
		printWriter.close();
	}
}