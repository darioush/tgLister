package edu.washington.cs.tgs.major;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import mockit.coverage.data.CoverageData;
import mockit.coverage.data.FileCoverageData;
import mockit.coverage.dataItems.FieldData;
import mockit.coverage.dataItems.PerFileDataCoverage;
import mockit.coverage.lines.PerFileLineCoverage;
import mockit.coverage.paths.MethodCoverageData;
import mockit.coverage.paths.Node;
import mockit.coverage.paths.Path;
import edu.washington.cs.tgs.LineIdxer;
import edu.washington.cs.tgs.Pair;
import edu.washington.cs.tgs.Tg;
import edu.washington.cs.tgs.TgBuilder;
import edu.washington.cs.tgs.Utils;

public class MajorTgPrinter {

	public static HashMap<String, TgBuilder> map = new HashMap<String, TgBuilder>();
	static {
		map.put("line", new TgBuilder() {
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new MajorTgPrinter(new File(dir + "/kill.csv"), new File(dir + "/mutants.log")).printLines(allTgs, covered);
			}
		});
		map.put("mutant", new TgBuilder() {
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new MajorTgPrinter(new File(dir + "/kill.csv"), new File(dir + "/mutants.log")).printMutants(allTgs, covered);
			}
		});
	}
	
	
	
	private File killFile;
	private File mutantLog;
	private TreeMap<Integer, Boolean> killMap;
	private TreeMap<Integer, Boolean> coverMap;
	private TreeMap<Integer, Pair<String, Integer>> mutantMap;
	

	public MajorTgPrinter(File killFile, File mutantLog) {
		this.killFile = killFile;
		this.mutantLog = mutantLog;
		try {
			readMutants();
			readKill();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private void readKill() throws FileNotFoundException {
		killMap = new TreeMap<Integer, Boolean>();
		coverMap = new TreeMap<Integer, Boolean>();

		Scanner s = new Scanner(this.killFile);
		s.nextLine(); // skip
		while(s.hasNextLine()) {
			String line = s.nextLine();
			String [] split = line.split(",");
			int mutno = Integer.parseInt(split[0]);
			killMap.put(mutno, Arrays.asList("FAIL", "TIME", "EXC").contains(split[1]));
			coverMap.put(mutno, Arrays.asList("FAIL", "TIME", "EXC", "LIVE").contains(split[1]));
		}
		s.close();
	}

	private void readMutants() throws FileNotFoundException {
		mutantMap = new TreeMap<Integer, Pair<String,Integer>>();
		Scanner s = new Scanner(this.mutantLog);
		while(s.hasNextLine()) {
			String line = s.nextLine();
			String [] split = line.split(":", 7);
			// idx, class, from, to, pos, line, code
			int idx = Integer.parseInt(split[0]);
			String fn = split[4].split("@")[0].replace('.', '/') + ".java";
			int lineNo = Integer.parseInt(split[5]);
			mutantMap.put(idx, new Pair<String, Integer>(fn, lineNo));
		}
		s.close();
	}

	
	public void printLines(Set<Tg> allTgs, Set<Tg> covered) {
		for (Entry<Integer, Pair<String, Integer>> e : mutantMap.entrySet()) {
			int mutNo = e.getKey();
			Tg tg = new Tg(e.getValue().first, e.getValue().second, 0, 0, "line", "major");
			allTgs.add(tg);
			if (coverMap.get(mutNo) != null && coverMap.get(mutNo) == true) {
				covered.add(tg);
			}
		}
	}

	public void printMutants(Set<Tg> allTgs, Set<Tg> covered) {
		for (Entry<Integer, Pair<String, Integer>> e : mutantMap.entrySet()) {
			int mutNo = e.getKey();
			Tg tg = new Tg(e.getValue().first, e.getValue().second, mutNo, 0, "mutant", "major");
			allTgs.add(tg);
			if (killMap.get(mutNo) != null && killMap.get(mutNo) == true) {
				covered.add(tg);
			}
		}
	}
}