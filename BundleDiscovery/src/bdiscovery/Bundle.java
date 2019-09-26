package bdiscovery;

import java.util.HashMap;

public class Bundle implements Cloneable {

	private int[] fromToIndex;
	private HashMap<String, double[]> members;

	public Bundle() {
	}

	public int[] getFromToIndex() {
		return fromToIndex;
	}

	public void setFromToIndex(int[] fromToIndex) {
		this.fromToIndex = fromToIndex;
	}

	public HashMap<String, double[]> getMembers() {
		return members;
	}

	public void setMembers(HashMap<String, double[]> members) {
		this.members = members;
	}

	public Bundle clone() throws CloneNotSupportedException {
		return (Bundle) super.clone();
	}

}