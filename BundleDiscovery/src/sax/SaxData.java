package sax;

import tools.TimeSeries;

import java.io.Serializable;

public class SaxData implements Serializable {

	private static final long serialVersionUID = 1L;
	public String id;
	public TimeSeries ts;
	public short[] maxCardSAX;

	public SaxData(TimeSeries ts, short[] maxCardSAX, String id) {
		this.ts = ts;
		this.maxCardSAX = maxCardSAX.clone();
		this.id = id;
	}
}