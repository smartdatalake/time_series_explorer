package tools;

import java.io.Serializable;

public class TimeSeries implements Serializable {

	private static final long serialVersionUID = 1L;
	private String id;
	private double[] ts;

	public TimeSeries(String id, double[] ts) {
		this.id = id;
		this.ts = ts;
	}

	public String getId() {
		return id;
	}

	public double[] getTimeSeries() {
		return ts;
	}

	public void setTimeSeries(double[] ts) {
		this.ts = ts;
	}

}