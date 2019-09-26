package bdiscovery;

import sax.SaxData;
import tools.Functions;
import tools.GlobalConf;
import tools.TimeSeries;

import java.util.*;

public class BundleDiscCheckpoint {

	static int count1 = 0;
	static int count2 = 0;
	static ArrayList<Integer> indices = new ArrayList<>();

	public static double[] discoverFlocks(ArrayList<Bundle> results) throws CloneNotSupportedException {
		double minTotalRatio = Double.MAX_VALUE;
		for (int i = 0; i < GlobalConf.delta; i++) {

			ArrayList<Integer> tmpIndices = new ArrayList<>();
			tmpIndices.add(i);
			int ind = 0;
			double totalRatio = 0;
			while (true) {
				int toAdd = tmpIndices.get(ind++) + GlobalConf.delta;
				if (toAdd >= GlobalConf.tsLength)
					break;

				double maxFilledRatio = 0;
				for (ArrayList<SaxData> list : GlobalConf.bins.get(toAdd).values()) {
					double tmp = list.size();
					if (tmp > maxFilledRatio)
						maxFilledRatio = tmp;
				}

				totalRatio += maxFilledRatio;
				tmpIndices.add(toAdd);
			}

			if (totalRatio < minTotalRatio) {
				minTotalRatio = totalRatio;
				indices = tmpIndices;
			}
		}

		//System.out.print("Checkpoints: ");
		for (int i = 0; i < indices.size(); i++) {
			//System.out.print((i+1) + "...");
			System.out.println("Checking checkpoint " + (i+1) + "/" + indices.size());
			HashMap<Integer, ArrayList<SaxData>> candidateGroups = new HashMap<>();
			Functions.groupBins(indices.get(i), candidateGroups);
			discoverFlocks(results, candidateGroups, i);
		}
		System.out.println("Finished!");

		return new double[]{count1, count2};
	}


	public static void discoverFlocks(ArrayList<Bundle> results, HashMap<Integer, ArrayList<SaxData>> candidateGroups, int slabIndex) throws CloneNotSupportedException {
		int j = indices.get(slabIndex);

		ArrayList<Bundle> candidates = new ArrayList<>();
		for (Map.Entry<Integer, ArrayList<SaxData>> groupEntry : candidateGroups.entrySet()) {
			ArrayList<SaxData> group = groupEntry.getValue();

			for (int n = 0; n < group.size(); n++) {
				TimeSeries ts = group.get(n).ts;
				double p1 = ts.getTimeSeries()[j];
				double[] interval = new double[]{p1, p1 + GlobalConf.epsilon};
				HashMap<String, double[]> adjacent = getAdjacent(interval, group, j);

				count1++;
				if (adjacent.size() >= GlobalConf.minNoTS) {
					Set<String> adjacentIDs = adjacent.keySet();

					boolean foundExact = false;
					for (int m = 0; m < candidates.size(); m++) {

						Bundle candidate = candidates.get(m);
						String[] candMembers = candidate.getMembers().keySet().toArray(new String[candidate.getMembers().keySet().size()]);
						Set<String> candMemberSet = new HashSet<>(Arrays.asList(candMembers));

						Set<Object> intersection = intersectionSetTS(adjacentIDs, candMemberSet);
						if (intersection.size() == adjacentIDs.size()) {
							foundExact = true;
							break;
						} else if (intersection.size() == candMemberSet.size() && adjacent.size() > candMemberSet.size()) {
							candidates.remove(m);
						}
					}
					if (!foundExact)
						addNewCandFlock(j, candidates, adjacent);
				}
			}
		}
		expandCandidateFlocks(results, candidates);
	}

	private static void addNewCandFlock(int j, ArrayList<Bundle> tmpResults, HashMap<String, double[]> members) {
		Bundle bundle = new Bundle();
		bundle.setFromToIndex(new int[]{j, j});
		bundle.setMembers(members);
		tmpResults.add(bundle);
	}

	private static void expandCandFlock(int i, int j, ArrayList<Bundle> tmpResults, HashMap<String, double[]> members) {
		Bundle bundle = new Bundle();
		bundle.setFromToIndex(new int[]{i, j});
		bundle.setMembers(members);
		tmpResults.add(bundle);
	}

	private static void expandCandidateFlocks(ArrayList<Bundle> results, ArrayList<Bundle> candidates) throws CloneNotSupportedException {

		for (int i = 0; i < candidates.size(); i++) {
			Bundle cand = candidates.get(i);
			ArrayList<Bundle> possibleCands = new ArrayList<>();
			ArrayList<Bundle> tmpPossibleCands = new ArrayList<>();
			possibleCands.add(cand.clone());
			if (cand.getFromToIndex()[0] > 0) {

				for (int j = cand.getFromToIndex()[0] - 1; j >= 0; j--) {
					removeFinishedBack(possibleCands, tmpPossibleCands, results, j);

					ArrayList<Bundle> newCands = new ArrayList<>();
					for (Bundle newCand : possibleCands) {

						count2++;
						HashMap<String, double[]> membList = newCand.getMembers();
						ArrayList<Bundle> currCands = new ArrayList<>();
						for (String key : membList.keySet()) {
							double p1 = membList.get(key)[j];
							double[] interval = new double[]{p1, p1 + GlobalConf.epsilon};
							HashMap<String, double[]> adjacent = getAdjacent(interval, membList, j);

							if (adjacent.size() >= GlobalConf.minNoTS) {
								Set<String> adjacentIDs = adjacent.keySet();

								boolean foundExact = false;
								for (int m = 0; m < currCands.size(); m++) {

									Bundle candidate = currCands.get(m);
									String[] candMembers = candidate.getMembers().keySet().toArray(new String[candidate.getMembers().keySet().size()]);
									Set<String> candMemberSet = new HashSet<>(Arrays.asList(candMembers));

									Set<Object> intersection = intersectionSetTS(adjacentIDs, candMemberSet);
									if (intersection.size() == adjacentIDs.size()) {
										foundExact = true;
										break;
									} else if (intersection.size() == candMemberSet.size() && adjacent.size() > candMemberSet.size()) {
										currCands.remove(m);
									}
								}
								if (!foundExact)
									expandCandFlock(j, cand.getFromToIndex()[0], currCands, adjacent);
							}
						}

						if (currCands.size() > 0) {
							newCands.addAll(currCands);
						}
					}

					if (newCands.size() > 0) {
						for (Bundle tmpCand : newCands) {
							addCandidate(tmpCand, possibleCands);
						}
					} else
						break;
				}

			}

			possibleCands.addAll(tmpPossibleCands);
			for (int j = cand.getFromToIndex()[0] + 1; j < GlobalConf.tsLength; j++) {
				removeFinished(possibleCands, results, j);

				ArrayList<Bundle> newCands = new ArrayList<>();
				for (Bundle newCand : possibleCands) {

					count2++;
					HashMap<String, double[]> membList = newCand.getMembers();
					ArrayList<Bundle> currCands = new ArrayList<>();
					for (String key : membList.keySet()) {
						double p1 = membList.get(key)[j];
						double[] interval = new double[]{p1, p1 + GlobalConf.epsilon};
						HashMap<String, double[]> adjacent = getAdjacent(interval, membList, j);

						if (adjacent.size() >= GlobalConf.minNoTS) {
							Set<String> adjacentIDs = adjacent.keySet();

							boolean foundExact = false;
							for (int m = 0; m < currCands.size(); m++) {

								Bundle candidate = currCands.get(m);
								String[] candMembers = candidate.getMembers().keySet().toArray(new String[candidate.getMembers().keySet().size()]);
								Set<String> candMemberSet = new HashSet<>(Arrays.asList(candMembers));

								Set<Object> intersection = intersectionSetTS(adjacentIDs, candMemberSet);
								if (intersection.size() == adjacentIDs.size()) {
									foundExact = true;
									break;
								} else if (intersection.size() == candMemberSet.size() && adjacent.size() > candMemberSet.size()) {
									currCands.remove(m);
								}
							}
							if (!foundExact)
								expandCandFlock(newCand.getFromToIndex()[0], j, currCands, adjacent);
						}
					}

					if (currCands.size() > 0) {
						newCands.addAll(currCands);
					}
				}

				if (newCands.size() > 0) {
					for (Bundle tmpCand : newCands) {
						addCandidate(tmpCand, possibleCands);
					}
				} else
					break;

			}

			for (Bundle toAddCand : possibleCands)
				addCandidateRes(toAddCand, results);
		}
	}

	private static void removeFinishedBack(ArrayList<Bundle> possibleCands, ArrayList<Bundle> tmpPossibleCands, ArrayList<Bundle> results, int j) {
		ArrayList<Bundle> toRemove = new ArrayList<>();
		for (Bundle cand : possibleCands) {
			if (cand.getFromToIndex()[0] > (j + 1))
				toRemove.add(cand);
		}

		for (Bundle toAddCand : toRemove)
			addCandidateRes(toAddCand, results);

		possibleCands.removeAll(toRemove);
		tmpPossibleCands.addAll(toRemove);
	}

	private static void removeFinished(ArrayList<Bundle> possibleCands, ArrayList<Bundle> results, int j) {
		ArrayList<Bundle> toRemove = new ArrayList<>();
		for (Bundle cand : possibleCands) {
			if (cand.getFromToIndex()[1] < (j - 1))
				toRemove.add(cand);
		}

		for (Bundle toAddCand : toRemove)
			addCandidateRes(toAddCand, results);

		possibleCands.removeAll(toRemove);
	}

	public static void addCandidate(Bundle cand, ArrayList<Bundle> candidates) {
		ArrayList<Bundle> toRemove = new ArrayList<>();
		for (int n = 0; n < candidates.size(); n++) {
			Bundle bundle = candidates.get(n);
			if (bundle.getFromToIndex()[1] <= cand.getFromToIndex()[1] && bundle.getFromToIndex()[0] >= cand.getFromToIndex()[0]) {
				if (intersection(bundle.getMembers().keySet(), cand.getMembers().keySet()).length == bundle.getMembers().size())
					toRemove.add(bundle);
			}
		}
		candidates.removeAll(toRemove);
		candidates.add(cand);
	}

	public static void addCandidateRes(Bundle cand, ArrayList<Bundle> results) {
		// CHECK IF EXISTS, REMOVE AND ADD
		if (cand.getMembers().size() >= GlobalConf.minNoTS) {
			if (cand.getFromToIndex()[1] - cand.getFromToIndex()[0] >= GlobalConf.delta) {

				ArrayList<Bundle> toRemove = new ArrayList<>();
				for (int n = 0; n < results.size(); n++) {
					Bundle bundle = results.get(n);
					if (bundle.getFromToIndex()[1] <= cand.getFromToIndex()[1] && bundle.getFromToIndex()[0] >= cand.getFromToIndex()[0]) {
						if (intersection(bundle.getMembers().keySet(), cand.getMembers().keySet()).length == bundle.getMembers().size())
							toRemove.add(bundle);
					}
				}
				results.removeAll(toRemove);
				if (!checkSubset(results, cand))
					results.add(cand);
			}
		}
	}

	private static boolean checkSubset(ArrayList<Bundle> results, Bundle cand) {
		for (int i = 0; i < results.size(); i++) {
			Bundle bundle = results.get(i);
			if (bundle.getFromToIndex()[1] >= cand.getFromToIndex()[1] && bundle.getFromToIndex()[0] <= cand.getFromToIndex()[0]) {
				if (intersection(bundle.getMembers().keySet(), cand.getMembers().keySet()).length == cand.getMembers().size())
					return true;
			}
		}
		return false;
	}

	private static HashMap<String, double[]> getAdjacent(double[] interval, HashMap<String, double[]> tsList, int j) {
		HashMap<String, double[]> adjacent = new HashMap<>();
		for (String key : tsList.keySet()) {
			double p2 = tsList.get(key)[j];
			if (p2 >= interval[0] && p2 <= interval[1]) {
				adjacent.put(key, tsList.get(key));
			}
		}
		return adjacent;
	}

	private static HashMap<String, double[]> getAdjacent(double[] interval, ArrayList<SaxData> tsArray, int j) {
		HashMap<String, double[]> adjacent = new HashMap<>();
		for (int n = 0; n < tsArray.size(); n++) {
			TimeSeries ts2 = tsArray.get(n).ts;
			double p2 = ts2.getTimeSeries()[j];
			if (p2 >= interval[0] && p2 <= interval[1]) {
				adjacent.put(ts2.getId(), ts2.getTimeSeries());
			}
		}
		return adjacent;
	}

	public static Object[] intersection(Set<String> a, Set<String> b) {
		if (a.size() > b.size()) {
			return intersection(b, a);
		}
		Set<String> results = new HashSet<>();
		for (Object element : a) {
			String str = (String) element;
			if (b.contains(str)) {
				results.add(str);
			}
		}
		return results.toArray();
	}

	public static Set<Object> intersectionSetTS(Set<String> set1, Set<String> set2) {
		Set<Object> intersection = new HashSet<>(set1);
		intersection.retainAll(set2);
		return intersection;
	}
}