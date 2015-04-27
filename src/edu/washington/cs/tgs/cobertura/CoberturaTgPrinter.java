package edu.washington.cs.tgs.cobertura;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

public class CoberturaTgPrinter {

	public static HashMap<String, TgBuilder> map = new HashMap<String, TgBuilder>();
	static {
		map.put("line", new TgBuilder() {
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new CoberturaTgPrinter(new File(dir + "/cobertura.ser")).printLines(allTgs, covered);
			}
		});
		map.put("branch", new TgBuilder() {
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new CoberturaTgPrinter(new File(dir + "/cobertura.ser")).printBranches(allTgs, covered);
			}
		});
		map.put("term", new TgBuilder() {
			@Override
			public void build(String dir, Set<Tg> allTgs, Set<Tg> covered) {
				new CoberturaTgPrinter(new File(dir + "/cobertura.ser")).printTerms(allTgs, covered);
			}
		});
	}
	
	private File inF;
	private ProjectData data;

	public CoberturaTgPrinter(File inF) {
		this.inF = inF;
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(this.inF));
			this.data = (ProjectData) ois.readObject();
			ois.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public void printLines(Set<Tg> allTgs, Set<Tg> covered) {
		for (SourceFileData sfd: (TreeSet<SourceFileData>)(data.getSourceFiles())) {
			for (ClassData cd : (TreeSet<ClassData>)sfd.getClasses()) {
				for (CoverageData line: cd.getLines()) {
					LineData lcd = (LineData) line;
					Tg tg = new Tg(sfd.getName(), lcd.getLineNumber(), 0, 0, "line", "cobertura");
					allTgs.add(tg);
					if (lcd.getHits() > 0) {
						covered.add(tg);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void printBranches(Set<Tg> allTgs, Set<Tg> covered) {
		for (SourceFileData sfd: (TreeSet<SourceFileData>)(data.getSourceFiles())) {
			LineIdxer idxer = new LineIdxer();

			for (ClassData cd : (TreeSet<ClassData>)sfd.getClasses()) {
				for (CoverageData line: cd.getLines()) {
					LineData lcd = (LineData) line;
					if (!lcd.hasBranch()) {
						continue;
					}
					List<JumpData> jumps = Utils.getPrivate(lcd, "jumps");
					List<SwitchData> switches = Utils.getPrivate(lcd, "switches");
					
					if (jumps != null) {
						long totalHits = lcd.getHits();
						long jumpedHits = 0;
						for (JumpData j: jumps) {
							jumpedHits += j.getTrueHits();
						}
						Tg jumped = new Tg(sfd.getName(), 
								lcd.getLineNumber(), 0,
								idxer.next(new Pair<Integer, Integer>(lcd.getLineNumber(), 0)), 
								"branch", "cobertura");
						Tg notJumped = new Tg(sfd.getName(), 
								lcd.getLineNumber(), 0,
								idxer.next(new Pair<Integer, Integer>(lcd.getLineNumber(), 0)), 
								"branch", "cobertura");
						allTgs.add(jumped);
						allTgs.add(notJumped);
						if (jumpedHits > 0) {
							covered.add(jumped);
						}
						if (jumpedHits < totalHits) {
							covered.add(notJumped);
						}
					}
					if (switches != null) {
						for (SwitchData s : switches) {
							for (int i = 0; i < s.getNumberOfValidBranches() - 1; ++i) {
								Tg theCase = new Tg(sfd.getName(), 
										lcd.getLineNumber(), 0,
										idxer.next(new Pair<Integer, Integer>(lcd.getLineNumber(), 0)), 
										"branch", "cobertura");
								allTgs.add(theCase);
								if (s.getHits(i) > 0) {
									covered.add(theCase);
								}
								if (s.getHits(i) == -1) {
									throw new RuntimeException();
								}
							}
							Tg defaultCase = new Tg(sfd.getName(), 
									lcd.getLineNumber(), 0,
									idxer.next(new Pair<Integer, Integer>(lcd.getLineNumber(), 0)), 
									"branch", "cobertura");
							allTgs.add(defaultCase);
							if (s.getDefaultHits() > 0) {
								covered.add(defaultCase);
							}
						}
					 }
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void printTerms(Set<Tg> allTgs, Set<Tg> covered) {
		for (SourceFileData sfd: (TreeSet<SourceFileData>)(data.getSourceFiles())) {
			LineIdxer idxer = new LineIdxer();

			for (ClassData cd : (TreeSet<ClassData>)sfd.getClasses()) {
				for (CoverageData line: cd.getLines()) {
					LineData lcd = (LineData) line;
					if (!lcd.hasBranch()) {
						continue;
					}
					List<JumpData> jumps = Utils.getPrivate(lcd, "jumps");
					
					if (jumps != null) {
						for (JumpData j: jumps) {
							Tg jumped = new Tg(sfd.getName(), 
									lcd.getLineNumber(), j.getConditionNumber(),
									idxer.next(new Pair<Integer, Integer>(lcd.getLineNumber(), j.getConditionNumber())), 
									"term", "cobertura");
							Tg notJumped = new Tg(sfd.getName(), 
									lcd.getLineNumber(), j.getConditionNumber(),
									idxer.next(new Pair<Integer, Integer>(lcd.getLineNumber(), j.getConditionNumber())), 
									"term", "cobertura");
							allTgs.add(jumped);
							allTgs.add(notJumped);
							
							if (j.getTrueHits() > 0) {
								covered.add(jumped);
							}
							
							if (j.getFalseHits() > 0) {
								covered.add(notJumped);
							}
						}						
					}
				}
			}
		}
	}
}
