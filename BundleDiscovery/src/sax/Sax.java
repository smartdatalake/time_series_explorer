package sax;

import tools.Functions;
import tools.GlobalConf;

import java.util.Arrays;

public class Sax {

	public Sax() {
	}

	public static short computeCard(int maskVal, int baseCard) {
		return (short) (Math.pow(2, maskVal) * baseCard);
	}

	public static short[] convertSAX(double[] ts, int wordLen, short card) throws Exception {
		double rem = Math.IEEEremainder(ts.length, wordLen);
		double[] PAA;
		if (!GlobalConf.breakpoints.containsKey(card))
			throw new RuntimeException("invalid alphabetsize");
		if (rem != 0) {
			int lcm = getLCM(ts, wordLen);
			double[] ts_dup;
			ts_dup = dupArray(ts, lcm / ts.length);
			PAA = getPAA(ts_dup, wordLen);
		} else {
			PAA = getPAA(ts, wordLen);
		}
		return getSymbol(PAA, wordLen, card);
	}

	public static double[] dupArray(double[] data, int dup) {
		int cur_index = 0;
		double[] dup_array = new double[data.length * dup];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < dup; j++) {
				dup_array[cur_index + j] = data[i];
			}
			cur_index += dup;
		}
		return dup_array;
	}

	private static int getGCD(double[] time_series, int num_seg) {
		int u = time_series.length;
		int v = num_seg;
		int div;
		int divisible_check;
		while (v > 0) {
			div = ((int) (Math.floor((double) u / (double) v)));
			divisible_check = u - v * div;
			u = v;
			v = divisible_check;
		}
		return u;
	}

	private static int getLCM(double[] time_series, int num_seg) {
		int gcd = getGCD(time_series, num_seg);
		int len = time_series.length;
		int n = num_seg;
		return (len * (n / gcd));
	}

	private static double[] getPAA(double[] data, int num_seg) throws Exception {
		if (Math.IEEEremainder(data.length, num_seg) != 0)
			throw new RuntimeException("Datalength not divisible by number of segments!");
		int segment_size = data.length / num_seg;
		int offset = 0;
		double[] PAA = new double[num_seg];
		if (num_seg == data.length) {
			PAA = Arrays.copyOf(data, data.length);
		}
		for (int i = 0; i < num_seg; i++) {
			PAA[i] = Functions.mean(data, offset, offset + segment_size - 1);
			offset = offset + segment_size;
		}
		return PAA;
	}

	private static short[] getSymbol(double[] PAA, int num_seg, short alphabet_size) {
		boolean FOUND = false;
		short[] symbols = new short[num_seg];
		for (int i = 0; i < num_seg; i++) {
			for (int j = 0; j < alphabet_size - 1; j++) {
				if (PAA[i] <= GlobalConf.breakpoints.get(alphabet_size)[j]) {
					symbols[i] = (short) j;
					FOUND = true;
					break;
				}

			}
			if (!FOUND) {
				symbols[i] = (short) (alphabet_size - 1);
			}
			FOUND = false;
		}
		return symbols;
	}
}