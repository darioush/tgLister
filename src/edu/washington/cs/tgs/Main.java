package edu.washington.cs.tgs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import edu.washington.cs.tgs.cobertura.CoberturaTgPrinter;
import edu.washington.cs.tgs.codecover.CodecoverTgPrinter;
import edu.washington.cs.tgs.jmockit.JmockitTgPrinter;
import edu.washington.cs.tgs.major.MajorTgPrinter;

public class Main {

	
	static HashMap<String, Map<String, TgBuilder>> map = new HashMap<String, Map<String,TgBuilder>>();
	static {
		map.put("major", MajorTgPrinter.map);
		map.put("codecover", CodecoverTgPrinter.map);
		map.put("cobertura", CoberturaTgPrinter.map);
		map.put("jmockit", JmockitTgPrinter.map);
	}
	
	public static void main(String[] args) {
		String dirName = args[0];
		HashSet<Tg> all = new HashSet<Tg>();
		HashSet<Tg> covered = new HashSet<Tg>();
		for (int i = 1; i < args.length; ++i) {
			String [] split = args[i].split(":");
			String tool = split[1];
			String goal = split[0];
			
			map.get(tool).get(goal).build(dirName, all, covered);
		}
		new TgPrinter(all, covered).print();
	}
}
