package edu.washington.cs.tgs;

import java.util.HashMap;

public class LineIdxer {

	HashMap<Pair<Integer, Integer>, Integer> myMap = new HashMap<Pair<Integer, Integer>, Integer>();
	
	public int next(Pair<Integer, Integer> line) {
		Integer myInt = myMap.get(line);
		if (myInt == null) {
			myInt = 0;
		}
		myMap.put(line, myInt+1);
		return myInt;
	}
}
