package edu.washington.cs.tgs.codecover;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.RootPaneContainer;

import org.codecover.metrics.coverage.QMOCoverage.QMOHint;
import org.codecover.model.MASTBuilder;
import org.codecover.model.TestCase;
import org.codecover.model.TestSessionContainer;
import org.codecover.model.extensions.PluginManager;
import org.codecover.model.mast.BasicBooleanTerm;
import org.codecover.model.mast.BasicStatement;
import org.codecover.model.mast.BooleanAssignment;
import org.codecover.model.mast.BooleanOperator;
import org.codecover.model.mast.BooleanResult;
import org.codecover.model.mast.BooleanTerm;
import org.codecover.model.mast.Branch;
import org.codecover.model.mast.ConditionalStatement;
import org.codecover.model.mast.CoverableItem;
import org.codecover.model.mast.HierarchyLevel;
import org.codecover.model.mast.Location;
import org.codecover.model.mast.LocationList;
import org.codecover.model.mast.LoopingStatement;
import org.codecover.model.mast.OperatorTerm;
import org.codecover.model.mast.QuestionMarkOperator;
import org.codecover.model.mast.RootTerm;
import org.codecover.model.mast.Statement;
import org.codecover.model.mast.StatementSequence;
import org.codecover.model.mast.SynchronizedStatement;
import org.codecover.model.mast.SourceFile;
import org.codecover.model.utils.LogLevel;
import org.codecover.model.utils.SimpleLogger;

import edu.washington.cs.tgs.LineIdxer;
import edu.washington.cs.tgs.Pair;
import edu.washington.cs.tgs.Tg;
import edu.washington.cs.tgs.TgBuilder;
import edu.washington.cs.tgs.Utils;
import net.sourceforge.cobertura.coveragedata.JumpData;
import net.sourceforge.cobertura.coveragedata.LineData;
import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.coveragedata.SwitchData;

public class CodecoverTgPrinter {

	public static HashMap<String, TgBuilder> map = new HashMap<String, TgBuilder>();
	static {
		map.put("line", new TgBuilder() {
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new CodecoverTgPrinter(new File(dir + "/codecover.xml")).printLines(allTgs, covered);
			}	
		});
		map.put("statement", new TgBuilder() {
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new CodecoverTgPrinter(new File(dir + "/codecover.xml")).printStatements(allTgs, covered);
			}	
		});
		map.put("branch", new TgBuilder() {
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new CodecoverTgPrinter(new File(dir + "/codecover.xml")).printBranches(allTgs, covered);
			}	
		});
		map.put("loop", new TgBuilder() {
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new CodecoverTgPrinter(new File(dir + "/codecover.xml")).printLoops(allTgs, covered);
			}	
		});
		map.put("term", new TgBuilder() {
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new CodecoverTgPrinter(new File(dir + "/codecover.xml")).printTerms(allTgs, covered);
			}	
		});
		
	}
	
	private File inF;
	private TestSessionContainer data;
	private TestCase testCase;
	private LinkedHashMap<SourceFile, Pos2Line> posmap = new LinkedHashMap<SourceFile, Pos2Line>(); 

	public CodecoverTgPrinter(File inF) {
		this.inF = inF;
		try {
			PluginManager pm = PluginManager.create();
			MASTBuilder mb = new MASTBuilder(null);
			SimpleLogger logger = new SimpleLogger(System.out, LogLevel.INFO);
			this.data = TestSessionContainer.load(pm, logger, mb, inF);
			for (TestCase tc : this.data.getTestSessions().get(0).getTestCases()) {
				if (!"EMPTY_TEST_CASE".equals(tc.getName())) {
					this.testCase = tc;
					break;
				}
			}
			for (SourceFile sf: this.data.getFiles()) {
				posmap.put(sf, new Pos2Line(sf.getContent()));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	
	class PackageNameAdder implements HierarchyLevel.Visitor {
		private LinkedList<String> pkg;
		
		public PackageNameAdder(LinkedList<String> pkg) {
			this.pkg = pkg;
		}

		@Override
		public void visit(HierarchyLevel hierarchyLevel) {
			if ("package".equals(hierarchyLevel.getType().getInternalName())) {
				this.pkg.addLast(hierarchyLevel.getName());
			}
		}
	}
	
	class PackageNameRemover implements HierarchyLevel.Visitor {
		private LinkedList<String> pkg;
		
		public PackageNameRemover(LinkedList<String> pkg) {
			this.pkg = pkg;
		}

		@Override
		public void visit(HierarchyLevel hierarchyLevel) {
			if ("package".equals(hierarchyLevel.getType().getInternalName())) {
				this.pkg.removeLast();
			}
		}
	}
	
	public abstract class MyVisitor implements Statement.Visitor, QuestionMarkOperator.Visitor, RootTerm.Visitor {
		private List<String> pkg;
		private Set<Tg> all;
		private Set<Tg> covered;

		public MyVisitor(List<String> pkg, Set<Tg> all, Set<Tg> covered) {
			this.pkg = pkg;
			this.all = all;
			this.covered = covered;
		}

		public void handleLocation(Location location, CoverableItem item) { 
			handleLocation(location, item, 0);
		}

		public void handleLocation(Location location, CoverableItem item, int myIdx) {
			Pos2Line pos2line = CodecoverTgPrinter.this.posmap.get(location.getFile());
			int column = pos2line.pos2column(location.getStartOffset());
			int line = pos2line.pos2line(location.getStartOffset());
			String fn = getFn(location);
			Tg tg = this.buildTg(fn, line, column, myIdx);
			all.add(tg);
			if (isCovered(item)) {
				covered.add(tg);
			}
		}

		
		protected String getFn(Location location) {
			String fn = Utils.join("/", pkg) + (pkg.size() > 0 ? "/" : "") + 
					location.getFile().getFileName();
			return fn;
		}
		
		public abstract Tg buildTg (String fn, int line, int column, int myIdx);

		@Override
		public void visit(BasicStatement statement) {}
		@Override
		public void visit(ConditionalStatement statement) {}
		@Override
		public void visit(Branch branch) {}
		@Override
		public void visit(LoopingStatement statement) {}
		@Override
		public void visit(StatementSequence sequence) {};
		@Override
		public void visit(SynchronizedStatement statement) {}
		
		@Override
		public void visit(QuestionMarkOperator qmo) {};
		
		@Override
		public void visit(RootTerm term) {}
		
	}

	public void printLines(final Set<Tg> allTgs, final Set<Tg> covered) {
		printLines(allTgs, covered, true);
	}
	public void printStatements(final Set<Tg> all, final Set<Tg> covered) {
		printLines(all, covered, false);
	}

	
	private void printLines(final Set<Tg> all, final Set<Tg> covered, final boolean isLine) {
		final LinkedList<String> pkg = new LinkedList<String>();
		this.data.getCode().accept(new PackageNameAdder(pkg), new PackageNameRemover(pkg), 
		new MyVisitor(pkg, all, covered) {
				
			@Override
			public void visit(Branch branch) {
				if (branch.getDecision().getLocations().size() == 0) {
					return; 
				}
				this.handleLocation(fetchSingle(branch.getDecision()), branch.getCoverableItem());
			}
		
			@Override
			public void visit(LoopingStatement statement) {
				this.handleLocation(fetchSingle(statement.getLocation()), statement.getCoverableItem());
			}
			
			
			@Override
			public void visit(ConditionalStatement statement) {
				this.handleLocation(fetchSingle(statement.getLocation()), statement.getCoverableItem());
			}
			
			@Override
			public void visit(BasicStatement statement) {
				this.handleLocation(fetchSingle(statement.getLocation()), statement.getCoverableItem());
			}

			@Override
			public Tg buildTg(String fn, int line, int column, int myIdx) {
				return new Tg(fn, line, isLine ? 0 : column, 0, isLine ? "line" : "statement", "codecover");
			}
		}, null, null, null, null, null, null);		
		//statementPre, statementPost, rootTermPre, rootTermPost, termPre, termPost, qmaVisitor);
	}

	public void printBranches(final Set<Tg> allTgs, final Set<Tg> covered) {
		final LinkedList<String> pkg = new LinkedList<String>();
		this.data.getCode().accept(new PackageNameAdder(pkg), new PackageNameRemover(pkg), 
		new MyVisitor(pkg, allTgs, covered) {
			@Override
			public void visit(ConditionalStatement statement) {
				int branchIdx = 0;
				List<Branch> branches = statement.getBranches();
				if ("if".equals(statement.getKeyword().getContent())) {
					LinkedList<Branch> tmp = new LinkedList<Branch>(branches); 
					branches = tmp;
					Collections.reverse(branches);
				}
				for (Branch branch: branches) {
					this.handleLocation(fetchSingle(statement.getLocation()), branch.getCoverableItem(), branchIdx++);
				}
			}
			
			@Override
			public void visit(LoopingStatement statement) {
				// in case of a loop, the back-edge may not be covered:
				if (statement.isOptionalBodyExecution()) {
					this.handleLocation(fetchSingle(statement.getLocation()), statement.getOnceExecutedItem(), 0);
					this.handleLocation(fetchSingle(statement.getLocation()), statement.getMultipleExecutedItem(), 0);
				} else {
					this.handleLocation(fetchSingle(statement.getLocation()), statement.getMultipleExecutedItem(), 0);
				}
			}
			
			@Override
			public Tg buildTg(String fn, int line, int column, int myIdx) {
				return new Tg(fn, line, column, myIdx, "branch", "codecover");
			}
		},	null, null, null, null, null, new MyVisitor(pkg, allTgs, covered) {
			@Override
			public Tg buildTg(String fn, int line, int column, int myIdx) {
				return new Tg(fn, line, column, myIdx, "branch", "codecover");
			}
	
			@Override
			public void visit(QuestionMarkOperator qmo) {
				int branchIdx = 0;
				this.handleLocation(fetchSingle(qmo.getLocation()), qmo.getQuestionMarkOperatorExpression2().getCoverableItem(), branchIdx++);
				this.handleLocation(fetchSingle(qmo.getLocation()), qmo.getQuestionMarkOperatorExpression1().getCoverableItem(), branchIdx++);
			}
		});
		//statementPre, statementPost, rootTermPre, rootTermPost, termPre, termPost, qmaVisitor
	}
	
	public void printLoops(final Set<Tg> allTgs, final Set<Tg> covered) {
		final LinkedList<String> pkg = new LinkedList<String>();
		this.data.getCode().accept(new PackageNameAdder(pkg), new PackageNameRemover(pkg), 
		new MyVisitor(pkg, allTgs, covered) {

			@Override
			public void visit(LoopingStatement statement) {
				// in case of a loop, the back-edge may not be covered:
				if (statement.isOptionalBodyExecution()) {
					this.handleLocation(fetchSingle(statement.getLocation()), statement.getNeverExecutedItem(), 0);
				}
				this.handleLocation(fetchSingle(statement.getLocation()), statement.getOnceExecutedItem(), 1);
				this.handleLocation(fetchSingle(statement.getLocation()), statement.getMultipleExecutedItem(), 2);
			}

			@Override
			public Tg buildTg(String fn, int line, int column, int myIdx) {
				return new Tg(fn, line, column, myIdx, "loop", "codecover");
			}
		}, null, null, null, null, null, null);
	}
			
	public void printTerms(final Set<Tg> allTgs, final Set<Tg> covered) {
		final LinkedList<String> pkg = new LinkedList<String>();
		this.data.getCode().accept(new PackageNameAdder(pkg), new PackageNameRemover(pkg), null, null,
			new MyVisitor(pkg, allTgs, covered) {
			
			public List<Map<BasicBooleanTerm, Boolean>> makeGoalsFor(BooleanTerm bt, boolean makeItBe) {
				if (bt instanceof BasicBooleanTerm) {
					BasicBooleanTerm basicT = (BasicBooleanTerm) bt;
					return Collections.singletonList(Collections.singletonMap(basicT, makeItBe));
				}
				
				if (bt instanceof OperatorTerm) {
					LinkedList<Map<BasicBooleanTerm, Boolean>> retval = new LinkedList<Map<BasicBooleanTerm, Boolean>>();
					OperatorTerm bOp = (OperatorTerm) bt;
					for (Entry<BooleanAssignment, Boolean> e : bOp.getOperator().getPossibleAssignments().entrySet()) {
						if (e.getValue() == makeItBe) {
							BooleanAssignment ba = e.getKey();
							List<List<Map<BasicBooleanTerm, Boolean>>> opGoals = new LinkedList<List<Map<BasicBooleanTerm,Boolean>>>();
							for (int i = 0; i < ba.getLength(); ++i) {
								BooleanResult desired = ba.getResults().get(i);
								BooleanTerm operand = bOp.getOperands().get(i);
								if (desired == BooleanResult.FALSE) {
									opGoals.add(makeGoalsFor(operand, false));
								} else if (desired == BooleanResult.TRUE) {
									opGoals.add(makeGoalsFor(operand, true));
								}
							}
							retval.addAll(merge(opGoals));
						}
					}
					return retval;
				}
				
				throw new RuntimeException();
			}
			
			private <K, V> List<Map<K, V>> merge(List<List<Map<K, V>>> opGoals) {
				if (opGoals.size() == 1) {
					return opGoals.get(0);
				}
				if (opGoals.size() == 0) {
					return Collections.emptyList();
				}
				List<Map<K, V>> retVal = new LinkedList<Map<K, V>>();
				List<Map<K, V>> first = opGoals.get(0);
				for (Map<K, V> mm : first) {
					for (Map<K, V> merged: merge(opGoals.subList(1, opGoals.size()))) {
						Map<K, V> mb = new HashMap<K, V>(mm);
						mb.putAll(merged);
						retVal.add(mb);
					}
				}
				return retVal;
			}


			@SuppressWarnings("unused")
			private void printAssgmntMap(Map<BasicBooleanTerm, Boolean> toExtend) {
				for (Entry<BasicBooleanTerm, Boolean> e: toExtend.entrySet()) {
					System.out.println(e.getKey().getLocation().getLocations().get(0).getContent() + " - " + e.getValue());
				}
				System.out.println("---");
			}

			@Override
			public void visit(RootTerm term) {
				Location location = fetchSingle(term.getTerm().getLocation());
				Pos2Line pos2line = CodecoverTgPrinter.this.posmap.get(location.getFile());
				int column = pos2line.pos2column(location.getStartOffset());
				int line = pos2line.pos2line(location.getStartOffset());
				String fn = getFn(location);

				int myIdx = 0;
				for (boolean b : new boolean[]{true, false}) {
					for (Map<BasicBooleanTerm, Boolean> map : makeGoalsFor(term.getTerm(), b)) {
						Tg tg = new Tg(fn, line, column, myIdx++, "term", "codecover");
						allTgs.add(tg);
						if (isCovered(map, term)) {
							covered.add(tg);
						}
						//printAssgmntMap(map);
					}
				}
			}
			
			private boolean isCovered(Map<BasicBooleanTerm, Boolean> map, RootTerm term) {
				Map<BooleanAssignment, Boolean> assignments = testCase.getAssignments(term);
				for (BooleanAssignment ba: assignments.keySet()) {
					if (matches(ba, map, term)) {
						return true;
					}
				}
				return false;
			}

			private boolean matches(BooleanAssignment ba,
					Map<BasicBooleanTerm, Boolean> map, RootTerm term) {
				for (Entry<BasicBooleanTerm, Boolean> e : map.entrySet()) {
					int pos = term.getPositionOfTerm(e.getKey());
					if (e.getValue() == true && ba.getResults().get(pos) != BooleanResult.TRUE) {
						return false;
					}
					if (e.getValue() == false && ba.getResults().get(pos) != BooleanResult.FALSE) {
						return false;
					}
				}
				return true;
			}

			@Override
			public Tg buildTg(String fn, int line, int column, int myIdx) {
				throw new RuntimeException();
			}
			
		}, null, null, null, null);
	}
	
	private static Location fetchSingle(LocationList location) {
		return fetchSingle(location.getLocations());
	}

	private static <T> T fetchSingle(List<T> l) {
		if (l.size() != 1) {
			throw new RuntimeException();
		}
		return l.get(0);
	}
	
	private boolean isCovered(CoverableItem coverableItem) {
		return this.testCase.getCoverageCount(coverableItem) > 0;
	}


}