package mainapp;

import bdiscovery.Bundle;
import bdiscovery.BundleDiscCheckpoint;
import bdiscovery.BundleDiscSweepLine;
import tools.Functions;
import tools.GlobalConf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MainApp {

	public static void main(String[] args) throws Exception {

		if (!GlobalConf.runExperiment) {

			BufferedReader reader = null;
			if (args.length == 1)
				reader = new BufferedReader(new FileReader(args[0]));
			else if (args.length == 0)
				reader = new BufferedReader(new FileReader("properties.conf"));
			else {
				System.out.println("Wrong arguments! Exiting...");
				System.exit(0);
			}

			GlobalConf.stockDataPath = reader.readLine().split("=")[1];
			GlobalConf.dataFile = reader.readLine().split("=")[1];
			GlobalConf.resultFile = reader.readLine().split("=")[1];
			GlobalConf.heuristic = Integer.parseInt(reader.readLine().split("=")[1]);
			GlobalConf.tsLength = Integer.parseInt(reader.readLine().split("=")[1]);
			GlobalConf.readCount = Integer.parseInt(reader.readLine().split("=")[1]);
			GlobalConf.year = Integer.parseInt(reader.readLine().split("=")[1]);
			GlobalConf.zNormalize = Integer.parseInt(reader.readLine().split("=")[1]);
			GlobalConf.delta = Integer.parseInt(reader.readLine().split("=")[1]);
			GlobalConf.minNoTS = Integer.parseInt(reader.readLine().split("=")[1]);
			GlobalConf.epsilon = Double.parseDouble(reader.readLine().split("=")[1]);
			reader.close();

			if (GlobalConf.readCount == -1)
				GlobalConf.readCount = Integer.MAX_VALUE;

			long startExecuteTime;
			long totalElapsedExecuteTime = 0;
			double[] metrics1 = new double[0];

			//Functions.readStockData(GlobalConf.stockDataPath, GlobalConf.year);
			//Functions.readEEGData(GlobalConf.eegDataPath);
			if (GlobalConf.zNormalize == 1) {
				//Functions.zNormalize(GlobalConf.dataFile);
				GlobalConf.dataFile = GlobalConf.dataFile + "zNorm.csv";
			}
			GlobalConf.minMax = Functions.getMinMax(GlobalConf.dataFile);
			Functions.generateBreakpoints();

			ArrayList<Bundle> results = new ArrayList<>();
			GlobalConf.maxCard = (short) GlobalConf.breakpoints.size();

			// ###### SWEEP LINE ###### //
			if (GlobalConf.heuristic == 0) {
				Functions.fillBins(GlobalConf.dataFile);
				startExecuteTime = System.currentTimeMillis();
				metrics1 = BundleDiscSweepLine.discoverFlocks(results);
				totalElapsedExecuteTime = System.currentTimeMillis() - startExecuteTime;
			}

			// ###### SLABS HEURISTIC 1 ###### //
			else if (GlobalConf.heuristic == 1) {
				Functions.fillBins(GlobalConf.dataFile);
				startExecuteTime = System.currentTimeMillis();
				metrics1 = BundleDiscCheckpoint.discoverFlocks(results);
				totalElapsedExecuteTime = System.currentTimeMillis() - startExecuteTime;
			}

			Functions.writeResults(results, GlobalConf.resultFile);
			double[] metrics2 = Functions.countResultMetrics(results);

			int ExecuteMillis = (int) totalElapsedExecuteTime % 1000;
			int ExecuteSeconds = (int) (totalElapsedExecuteTime / 1000) % 60;
			int ExecuteMinutes = (int) ((totalElapsedExecuteTime / (1000 * 60)) % 60);
			int ExecuteHours = (int) ((totalElapsedExecuteTime / (1000 * 60 * 60)) % 24);
			//System.out.println("\nTotal time: " + ExecuteHours + "h " + ExecuteMinutes
			//		+ "m " + ExecuteSeconds + "sec " + ExecuteMillis + "mil");

			//System.out.print("Total candidate bundles checked: ");
			//System.out.printf("%.1f", metrics1[0]);
			//System.out.println();
			//System.out.print("Total time instances verified: ");
			//System.out.printf("%.1f", +metrics1[1]);
			//System.out.println();
			System.out.print("Number of results: ");
			System.out.print(metrics2[0] + "\n");
			//System.out.println();
			System.out.print("Maximum bundle length: ");
			System.out.print(metrics2[1] + "\n");
			//System.out.println();
			System.out.print("Maximum bundle size: ");
			System.out.print(metrics2[2]);
		} else {
			GlobalConf.heuristic = Integer.parseInt(args[0]);
			GlobalConf.tsLength = Integer.parseInt(args[1]);
			GlobalConf.readCount = Integer.parseInt(args[2]);
			GlobalConf.delta = Integer.parseInt(args[3]);
			GlobalConf.minNoTS = Double.parseDouble(args[4]);
			GlobalConf.epsilon = Double.parseDouble(args[5]);

			long startExecuteTime;
			long totalElapsedExecuteTime = 0;
			double[] metrics1 = new double[0];
			GlobalConf.minMax = Functions.getMinMax(GlobalConf.dataFile);
			Functions.generateBreakpoints();

			ArrayList<Bundle> results = new ArrayList<>();
			GlobalConf.maxCard = (short) GlobalConf.breakpoints.size();
			if (GlobalConf.heuristic == 0) {
				Functions.fillBins(GlobalConf.dataFile);
				startExecuteTime = System.currentTimeMillis();
				metrics1 = BundleDiscSweepLine.discoverFlocks(results);
				totalElapsedExecuteTime = System.currentTimeMillis() - startExecuteTime;
			} else if (GlobalConf.heuristic == 1) {
				Functions.fillBins(GlobalConf.dataFile);
				startExecuteTime = System.currentTimeMillis();
				metrics1 = BundleDiscCheckpoint.discoverFlocks(results);
				totalElapsedExecuteTime = System.currentTimeMillis() - startExecuteTime;
			}
			double[] metrics2 = Functions.countResultMetrics(results);

			int ExecuteMillis = (int) totalElapsedExecuteTime % 1000;
			int ExecuteSeconds = (int) (totalElapsedExecuteTime / 1000) % 60;
			int ExecuteMinutes = (int) ((totalElapsedExecuteTime / (1000 * 60)) % 60);
			int ExecuteHours = (int) ((totalElapsedExecuteTime / (1000 * 60 * 60)) % 24);


			FileWriter fileWriter = new FileWriter(GlobalConf.resultPath + "results_" + GlobalConf.tsLength + "_" + GlobalConf.readCount + "_" + GlobalConf.delta +
					"_" + GlobalConf.minNoTS + "_" + GlobalConf.epsilon + "_" + GlobalConf.heuristic + ".txt");
			PrintWriter printWriter = new PrintWriter(fileWriter);

			printWriter.print("\nTotal time: " + ExecuteHours + "h " + ExecuteMinutes
					+ "m " + ExecuteSeconds + "sec " + ExecuteMillis + "mil");

			printWriter.println();
			printWriter.print("Total candidate flocks checked: ");
			printWriter.printf("%.1f", metrics1[0]);
			printWriter.println();
			printWriter.print("Total time instances verified: ");
			printWriter.printf("%.1f", +metrics1[1]);
			printWriter.println();
			printWriter.print("Number of results: ");
			printWriter.printf("%.1f", +metrics2[0]);
			printWriter.println();
			printWriter.print("Maximum flock length: ");
			printWriter.printf("%.1f", +metrics2[1]);
			printWriter.println();
			printWriter.print("Maximum flock size: ");
			printWriter.printf("%.1f", +metrics2[2]);

			printWriter.close();
		}
	}
}