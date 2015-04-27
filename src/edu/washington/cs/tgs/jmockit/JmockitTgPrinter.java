package edu.washington.cs.tgs.jmockit;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

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

public class JmockitTgPrinter {

	public static HashMap<String, TgBuilder> map = new HashMap<String, TgBuilder>();
	static {
		map.put("line", new TgBuilder() {	
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new JmockitTgPrinter(new File(dir + "/coverage.ser")).printLines(allTgs, covered);
			}
		});
		map.put("branch", new TgBuilder() {	
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new JmockitTgPrinter(new File(dir + "/coverage.ser")).printBranches(allTgs, covered);
			}
		});
		map.put("term", new TgBuilder() {	
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new JmockitTgPrinter(new File(dir + "/coverage.ser")).printTerms(allTgs, covered);
			}
		});
		map.put("data", new TgBuilder() {	
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new JmockitTgPrinter(new File(dir + "/coverage.ser")).printData(allTgs, covered);
			}
		});
	}
	
	private File inF;
	private CoverageData data;

	public JmockitTgPrinter(File inF) {
		this.inF = inF;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					this.inF));
			this.data = (CoverageData) ois.readObject();
			ois.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void printLines(Set<Tg> allTgs, Set<Tg> covered) {
		for (Entry<String, FileCoverageData> e : data.getFileToFileDataMap()
				.entrySet()) {
			String fn = e.getKey();
			PerFileLineCoverage lc = e.getValue().lineCoverageInfo;
			int numLines = lc.getLineCount();
			int execLines = lc.getExecutableLineCount();
			for (int lineNumber = 1; lineNumber <= numLines; ++lineNumber) {
				if (lc.hasLineData(lineNumber)) {
					--execLines;
					Tg tg = new Tg(fn, lineNumber, 0, 0, "line", "jmockit");
					allTgs.add(tg);
					if (lc.getExecutionCount(lineNumber) > 0) {
						covered.add(tg);
					}
				}
			}
			if (execLines != 0) {
				throw new RuntimeException("Bad counting");
			}
		}
	}

	public void printBranches(Set<Tg> allTgs, Set<Tg> covered) {
		for (Entry<String, FileCoverageData> e : data.getFileToFileDataMap()
				.entrySet()) {
			String fn = e.getKey();
			Collection<MethodCoverageData> methods = e.getValue().getMethods();
			LineIdxer idxer = new LineIdxer();
			for (MethodCoverageData method : methods) {
				ArrayList<Node> nodes = Utils.getPrivate(method, "nodes");

				LinkedHashMap<Node, ArrayList<Node>> joins = new LinkedHashMap<Node, ArrayList<Node>>();

				// join -> { x, y, z, t }
				// x -> join
				// y -> join
				// z -> join
				// t -> fallthrough

				// join -> { x }
				// x -> join
				// x -> fallthroughw

				for (Node n : nodes) {
					for (Node br : getBranches(n)) {
						ArrayList<Node> joinSrc = joins.get(br);
						if (joinSrc == null) {
							joinSrc = new ArrayList<Node>();
							joins.put(br, joinSrc);
						}
						joinSrc.add(n);
					}
				}

				for (Entry<Node, ArrayList<Node>> entry : joins.entrySet()) {
					Node jmpDst = entry.getKey();
					List<Node> srcs = entry.getValue();

					LinkedHashMap<List<Node>, Tg> pathsToTgs = new LinkedHashMap<List<Node>, Tg>();
					Node first = srcs.get(0);
					Tg jumped = new Tg(fn, first.line, first.getSegment(),
					// below is in for MultiForks
							idxer.next(new Pair<Integer, Integer>(first.line,
									first.getSegment())), "branch", "jmockit");
					// below does not apply to MultiForks
					Tg notJumped = new Tg(fn, first.line, first.getSegment(),
							1, "branch", "jmockit");

					// 'jumped' tg happens if 'any' of the jumps are taken:
					for (Node src : srcs) {
						pathsToTgs.put(Arrays.asList(src, jmpDst), jumped);
					}
					for (List<Node> notJumpedPath : getAdditionalPathList(srcs)) {
						pathsToTgs.put(notJumpedPath, notJumped);
					}
					for (List<Node> path : pathsToTgs.keySet()) {
						Tg goal = pathsToTgs.get(path);
						allTgs.add(goal);
						List<Path> paths = method.getPaths();
						if (isInCoveredPaths(path, paths)) {
							covered.add(goal);
						}
					}
				}

			}
		}
	}

	public void printTerms(Set<Tg> allTgs, Set<Tg> covered) {
		for (Entry<String, FileCoverageData> e : data.getFileToFileDataMap()
				.entrySet()) {
			String fn = e.getKey();
			Collection<MethodCoverageData> methods = e.getValue().getMethods();
			LineIdxer idxer = new LineIdxer();
			for (MethodCoverageData method : methods) {
				ArrayList<Node> nodes = Utils.getPrivate(method, "nodes");
				
				LinkedHashMap<Node, ArrayList<Node>> joins = new LinkedHashMap<Node, ArrayList<Node>>();
				for (Node n : nodes) {
					for (Node br : getBranches(n)) {
						ArrayList<Node> joinSrc = joins.get(br);
						if (joinSrc == null) {
							joinSrc = new ArrayList<Node>();
							joins.put(br, joinSrc);
						}
						joinSrc.add(n);
					}
				}

				for (Entry<Node, ArrayList<Node>> entry : joins.entrySet()) {
					Node jmpDst = entry.getKey();
					List<Node> srcs = entry.getValue();
					// don't need cases
					if (isMultiFork(srcs.get(0))) {
						continue;
					}

					LinkedHashMap<List<Node>, Tg> pathsToTgs = new LinkedHashMap<List<Node>, Tg>();
					// each term gets two goals:
					int srcCounter = 0;
					for (Node src : srcs) {
						Tg jumped = new Tg(fn, 
								src.line, src.getSegment(),
								idxer.next(new Pair<Integer, Integer>(src.line, src.getSegment())), 
								"term", "jmockit");
						
						pathsToTgs.put(Arrays.asList(src, jmpDst), jumped);
						for (List<Node> notJumpedPath : 
								getAdditionalPathList(srcs.subList(0, srcCounter + 1))) {
							Tg notJumped = new Tg(fn,
									src.line, src.getSegment(),
									idxer.next(new Pair<Integer, Integer>(src.line, src.getSegment()))
									, "term", "jmockit");
							pathsToTgs.put(notJumpedPath, notJumped);
						}
						
						++srcCounter;
					}
					
					for (List<Node> path : pathsToTgs.keySet()) {
						Tg goal = pathsToTgs.get(path);
						allTgs.add(goal);
						List<Path> paths = method.getPaths();
						if (isInCoveredPaths(path, paths)) {
							covered.add(goal);
						}
					}
				}

			}
		}
	}

	public void printData(Set<Tg> allTgs, Set<Tg> covered) {
		for (Entry<String, FileCoverageData> e : data.getFileToFileDataMap()
				.entrySet()) {
			String fn = e.getKey();
			PerFileDataCoverage dataCvg = e.getValue().dataCoverageInfo;
			int i = 0;
			for (String fieldName: dataCvg.allFields) {
				if (!dataCvg.isFieldWithCoverageData(fieldName)) {
					continue;
				}
				FieldData fd = dataCvg.getInstanceFieldData(fieldName);
				if (fd == null) {
					fd = dataCvg.getStaticFieldData(fieldName);
				}
				Tg tg = new Tg(fn, 0, i, 0, "data", "jmockit");
				allTgs.add(tg);
				if (fd.isCovered()) {
					covered.add(tg);
				}
				++i;
			}
		}
	}
	
	public List<Node> getBranches(Node n) {
		if (isMultiFork(n)) {
			ArrayList<Node> cases = Utils.getPrivate(n, "caseNodes");
			return cases;
		}
		if (isSimpleFork(n)) {
			// Node n1 = getPrivate(n, "nextConsecutiveNode");
			Node n2 = Utils.getPrivate(n, "nextNodeAfterJump");
			return Arrays.asList(n2);
		}
		return new ArrayList<Node>();
	}

	private boolean isSimpleFork(Node n) {
		return n.getClass().getName()
				.equals("mockit.coverage.paths.Node$SimpleFork");
	}

	private boolean isMultiFork(Node n) {
		return n.getClass().getName()
				.equals("mockit.coverage.paths.Node$MultiFork");
	}

	private boolean isBasicBlock(Node n) {
		return n.getClass().getName()
				.equals("mockit.coverage.paths.Node$BasicBlock");
	}

	private boolean isInCoveredPaths(List<Node> path, List<Path> paths) {
		for (Path p : paths) {
			// unexecuted paths don't matter
			if (p.getExecutionCount() == 0) {
				continue;
			}
			// System.out.println(Arrays.toString(path.toArray()));
			// System.out.println(Arrays.toString(p.getNodes().toArray()))
			if (Collections.indexOfSubList(p.getNodes(), path) != -1) {
				return true;
			}
		}
		return false;
	}

	private List<List<Node>> getAdditionalPathList(List<Node> srcs) {
		boolean allSimpleFork = true;
		for (int i = 0; i < srcs.size(); ++i) {
			Node n = srcs.get(i);
			if (!isSimpleFork(n)) {
				allSimpleFork = false;
				break;
			}
			if (i < srcs.size() - 1) {
				Node next = srcs.get(i + 1);
				Node expectedNext = getNextNode(Utils.<Node> getPrivate(n,
						"nextConsecutiveNode"));
				if (next != expectedNext) {
					throw new RuntimeException();
				}
			}
		}

		if (allSimpleFork) {
			Node last = srcs.get(srcs.size() - 1);
			Node next = Utils.getPrivate(last, "nextConsecutiveNode");
			return Arrays.asList(Arrays.asList(last, next));
		} else {
			if (!(srcs.size() == 1 && isMultiFork(srcs.get(0)))) {
				throw new RuntimeException();
			}
			return Collections.emptyList();
		}
	}

	private Node getNextNode(Node n) {
		if (isBasicBlock(n)) {
			Node nc = Utils.getPrivate(n, "nextConsecutiveNode");
			Node nj = Utils.getPrivate(n, "nextNodeAfterGoto");
			return nj != null ? nj : nc;
		}
		throw new RuntimeException();
	}
}