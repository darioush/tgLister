package edu.washington.cs.tgs.codecover;

import java.util.Map.Entry;
import java.util.TreeMap;

public class Pos2Line {

	String code;
	private TreeMap<Integer, Integer> ts;
	
	
	public Pos2Line(String code) {
		this.code = code;
		
		int from = 0, pos = -1, line = 0;
		ts = new TreeMap<Integer, Integer>();
		while ( (pos = this.code.indexOf('\n', from)) != -1) {
			ts.put(pos, ++line);
			from = pos + 1;
		};
	}
	
	public int pos2line(int pos) {
		Entry<Integer, Integer> floor = ts.floorEntry(pos);
		if (floor == null) {
			return 1; // first line
		}
		return floor.getValue() + 1;
	}
	
	public int pos2column(int pos) {
		Entry<Integer, Integer> floor = ts.floorEntry(pos);
		if (floor == null) {
			return pos; // first line
		}
		return pos - floor.getKey();
	}
	
	
}
