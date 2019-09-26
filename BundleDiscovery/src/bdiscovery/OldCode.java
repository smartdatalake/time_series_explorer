package bdiscovery;

public class OldCode {

	/* ##################################### CHECKPOINT BASED ########################################
	private static void expandCandidateFlocks(ArrayList<Bundle> results, ArrayList<Bundle> candidates, int glIndex, int slabIndex) throws CloneNotSupportedException {

		for (int i = 0; i < candidates.size(); i++) {

			Bundle cand = candidates.get(i);
			int initSize = cand.getMembers().size();
			ArrayList<Bundle> possibleCands = new ArrayList<>();
			possibleCands.add(cand.clone());
			if (cand.getFromToIndex()[0] > 0) {
				for (int j = cand.getFromToIndex()[0] - 1; j >= 0; j--) {
					count2++;

					List<HashMap.Entry<String, double[]>> memblist = new LinkedList<>(cand.getMembers().entrySet());
					int fj = j;
					Collections.sort(memblist, Comparator.comparingDouble(o -> o.getValue()[fj]));

					Bundle candClone = cand.clone();
					boolean found = false;
					int largestGroupFound = 0;
					for (int k = 0; k < memblist.size(); k++) {
						HashMap<String, double[]> possibleMembs = new HashMap<>();
						possibleMembs.put(memblist.get(k).getKey(), memblist.get(k).getValue());
						int l;
						for (l = k + 1; l < memblist.size(); l++) {
							if (Math.abs(memblist.get(k).getValue()[j] - memblist.get(l).getValue()[j]) <= GlobalConf.epsilon) {
								possibleMembs.put(memblist.get(l).getKey(), memblist.get(l).getValue());
							} else {
								break;
							}
						}

						if (largestGroupFound >= memblist.size() - k)
							break;

						if (possibleMembs.size() >= GlobalConf.minNoTS) {
							if (possibleMembs.size() > largestGroupFound) {
								cand.setMembers(possibleMembs);
								cand.setFromToIndex(new int[]{j, cand.getFromToIndex()[1]});
								largestGroupFound = possibleMembs.size();
								found = true;
							}
						}
					}

					if (cand.getMembers().size() < initSize) {
						possibleCands.add(candClone);
						initSize = cand.getMembers().size();
					}

					if (!found)
						break;
				}
			}
			possibleCands.add(cand.clone());

			int prevSize = Integer.MAX_VALUE;
			for (int p = possibleCands.size() - 1; p >= 0; p--) {
				cand = possibleCands.get(p);
				if (cand.getMembers().size() < prevSize)
					prevSize = cand.getMembers().size();
				else
					continue;

				if (slabIndex < indices.size() - 1) {
					int nextSlab = indices.get(slabIndex + 1);
					int initvalidCount = glIndex - cand.getFromToIndex()[0];
					int validCount = glIndex - cand.getFromToIndex()[0];

					for (int m = nextSlab - initvalidCount; m > glIndex; m--) {
						count2++;

						List<HashMap.Entry<String, double[]>> memblist = new LinkedList<>(cand.getMembers().entrySet());
						int fj = m;
						Collections.sort(memblist, Comparator.comparingDouble(o -> o.getValue()[fj]));

						boolean found = false;
						int largestGroupFound = 0;
						for (int k = 0; k < memblist.size(); k++) {
							HashMap<String, double[]> possibleMembs = new HashMap<>();
							possibleMembs.put(memblist.get(k).getKey(), memblist.get(k).getValue());
							int l;
							for (l = k + 1; l < memblist.size(); l++) {
								if (Math.abs(memblist.get(k).getValue()[m] - memblist.get(l).getValue()[m]) <= GlobalConf.epsilon) {
									possibleMembs.put(memblist.get(l).getKey(), memblist.get(l).getValue());
								} else {
									break;
								}
							}

							if (largestGroupFound >= memblist.size() - k)
								break;

							if (possibleMembs.size() >= GlobalConf.minNoTS) {
								if (possibleMembs.size() > largestGroupFound) {
									cand.setMembers(possibleMembs);
									largestGroupFound = possibleMembs.size();
									found = true;
								}
							}
						}

						if (!found)
							break;
						validCount++;
					}

					if (validCount < GlobalConf.delta) {
						continue;
					} else {
						initSize = cand.getMembers().size();
						cand.setFromToIndex(new int[]{cand.getFromToIndex()[0], nextSlab - initvalidCount});
						for (int j = cand.getFromToIndex()[1] + 1; j < GlobalConf.tsLength; j++) {
							count2++;

							List<HashMap.Entry<String, double[]>> memblist = new LinkedList<>(cand.getMembers().entrySet());
							int fj = j;
							Collections.sort(memblist, Comparator.comparingDouble(o -> o.getValue()[fj]));

							Bundle candClone = cand.clone();
							boolean found = false;
							int largestGroupFound = 0;
							for (int k = 0; k < memblist.size(); k++) {
								HashMap<String, double[]> possibleMembs = new HashMap<>();
								possibleMembs.put(memblist.get(k).getKey(), memblist.get(k).getValue());
								int l;
								for (l = k + 1; l < memblist.size(); l++) {
									if (Math.abs(memblist.get(k).getValue()[j] - memblist.get(l).getValue()[j]) <= GlobalConf.epsilon) {
										possibleMembs.put(memblist.get(l).getKey(), memblist.get(l).getValue());
									} else {
										break;
									}
								}

								if (largestGroupFound >= memblist.size() - k)
									break;

								if (possibleMembs.size() >= GlobalConf.minNoTS) {
									if (possibleMembs.size() > largestGroupFound) {
										cand.setMembers(possibleMembs);
										cand.setFromToIndex(new int[]{cand.getFromToIndex()[0], j});
										largestGroupFound = possibleMembs.size();
										found = true;
									}
								}
							}

							if (cand.getMembers().size() < initSize) {
								if (cand.getFromToIndex()[1] - cand.getFromToIndex()[0] >= GlobalConf.delta)
									addCandidateRes(candClone, results);
								initSize = cand.getMembers().size();
							}

							if (!found)
								break;
						}

						addCandidateRes(cand, results);
					}
				} else {
					initSize = cand.getMembers().size();
					for (int j = glIndex; j < GlobalConf.tsLength; j++) {
						count2++;

						List<HashMap.Entry<String, double[]>> memblist = new LinkedList<>(cand.getMembers().entrySet());
						int fj = j;
						Collections.sort(memblist, Comparator.comparingDouble(o -> o.getValue()[fj]));

						Bundle candClone = cand.clone();
						boolean found = false;
						int largestGroupFound = 0;
						for (int k = 0; k < memblist.size(); k++) {
							HashMap<String, double[]> possibleMembs = new HashMap<>();
							possibleMembs.put(memblist.get(k).getKey(), memblist.get(k).getValue());
							int l;
							for (l = k + 1; l < memblist.size(); l++) {
								if (Math.abs(memblist.get(k).getValue()[j] - memblist.get(l).getValue()[j]) <= GlobalConf.epsilon) {
									possibleMembs.put(memblist.get(l).getKey(), memblist.get(l).getValue());
								} else {
									break;
								}
							}

							if (largestGroupFound >= memblist.size() - k)
								break;

							if (possibleMembs.size() >= GlobalConf.minNoTS) {
								if (possibleMembs.size() > largestGroupFound) {
									cand.setMembers(possibleMembs);
									cand.setFromToIndex(new int[]{cand.getFromToIndex()[0], j});
									largestGroupFound = possibleMembs.size();
									found = true;
								}
							}
						}

						if (cand.getMembers().size() < initSize) {
							if (cand.getFromToIndex()[1] - cand.getFromToIndex()[0] >= GlobalConf.delta)
								addCandidateRes(candClone, results);
							initSize = cand.getMembers().size();
						}

						if (!found)
							break;
					}

					addCandidateRes(cand, results);
				}
			}
		}
	}

	/* ##################################### SWEEP LINE BASED ########################################
		/*
	private static void expandCandidateFlocks(ArrayList<Bundle> results, ArrayList<Bundle> candidates) throws CloneNotSupportedException {

		for (int i = 0; i < candidates.size(); i++) {

			Bundle cand = candidates.get(i);
			int initSize = cand.getMembers().size();
			ArrayList<Bundle> possibleCands = new ArrayList<>();
			possibleCands.add(cand.clone());
			if (cand.getFromToIndex()[0] > 0) {
				for (int j = cand.getFromToIndex()[0] - 1; j >= 0; j--) {
					count2++;

					List<HashMap.Entry<String, double[]>> memblist = new LinkedList<>(cand.getMembers().entrySet());
					int fj = j;
					Collections.sort(memblist, Comparator.comparingDouble(o -> o.getValue()[fj]));

					boolean found = false;
					Bundle toAdd = cand.clone();
					int largestGroupFound = 0;
					for (int k = 0; k < memblist.size(); k++) {
						if (largestGroupFound >= memblist.size() - k)
							break;

						HashMap<String, double[]> possibleMembs = new HashMap<>();
						possibleMembs.put(memblist.get(k).getKey(), memblist.get(k).getValue());
						int l;
						for (l = k + 1; l < memblist.size(); l++) {
							if (Math.abs(memblist.get(k).getValue()[j] - memblist.get(l).getValue()[j]) <= GlobalConf.epsilon) {
								possibleMembs.put(memblist.get(l).getKey(), memblist.get(l).getValue());
							} else {
								break;
							}
						}

						if (possibleMembs.size() >= GlobalConf.minNoTS) {
							if (possibleMembs.size() > largestGroupFound) {
								cand.setMembers(possibleMembs);
								cand.setFromToIndex(new int[]{j, cand.getFromToIndex()[1]});
								largestGroupFound = possibleMembs.size();
								found = true;
							}
						}
					}

					if (cand.getMembers().size() < initSize) {
						possibleCands.add(toAdd);
						initSize = cand.getMembers().size();
					}

					if (!found)
						break;
				}
			}
			possibleCands.add(cand);

			for (int p = possibleCands.size() - 1; p >= 0; p--) {

				initSize = cand.getMembers().size();
				for (int j = cand.getFromToIndex()[0] + 1; j < GlobalConf.tsLength; j++) {
					count2++;

					List<HashMap.Entry<String, double[]>> memblist = new LinkedList<>(cand.getMembers().entrySet());
					int fj = j;
					Collections.sort(memblist, Comparator.comparingDouble(o -> o.getValue()[fj]));

					boolean found = false;
					Bundle toAdd = cand.clone();
					int largestGroupFound = 0;
					for (int k = 0; k < memblist.size(); k++) {
						if (largestGroupFound >= memblist.size() - k)
							break;

						HashMap<String, double[]> possibleMembs = new HashMap<>();
						possibleMembs.put(memblist.get(k).getKey(), memblist.get(k).getValue());
						int l;
						for (l = k + 1; l < memblist.size(); l++) {
							if (Math.abs(memblist.get(k).getValue()[j] - memblist.get(l).getValue()[j]) <= GlobalConf.epsilon) {
								possibleMembs.put(memblist.get(l).getKey(), memblist.get(l).getValue());
							} else {
								break;
							}
						}

						if (possibleMembs.size() >= GlobalConf.minNoTS) {
							if (possibleMembs.size() > largestGroupFound) {
								cand.setMembers(possibleMembs);
								cand.setFromToIndex(new int[]{cand.getFromToIndex()[0], j});
								largestGroupFound = possibleMembs.size();
								found = true;
							}
						}
					}

					if (cand.getMembers().size() < initSize) {
						if (toAdd.getFromToIndex()[1] - toAdd.getFromToIndex()[0] >= GlobalConf.delta)
							addCandidateRes(toAdd, results);
						initSize = cand.getMembers().size();
					}

					if (!found)
						break;
				}
				if (cand.getFromToIndex()[1] - cand.getFromToIndex()[0] >= GlobalConf.delta)
					addCandidateRes(cand, results);
			}
		}
	}*/

}