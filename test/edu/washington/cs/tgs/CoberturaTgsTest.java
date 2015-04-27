package edu.washington.cs.tgs;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import org.junit.Test;

import edu.washington.cs.tgs.cobertura.CoberturaTgPrinter;
import edu.washington.cs.tgs.jmockit.JmockitTgPrinter;

public class CoberturaTgsTest {

	//@Test
	public void test() {
		URL in = this.getClass().getResource("cobertura.ser");
		File f = new File(in.getFile());
		CoberturaTgPrinter ctp = new CoberturaTgPrinter(f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		ctp.printLines(all, covered);
		new TgPrinter(all, covered).print();
	}
	
	@Test
	public void testBranches() {
		URL in = this.getClass().getResource("cobertura.ser");
		File f = new File(in.getFile());
		CoberturaTgPrinter ctp = new CoberturaTgPrinter(f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		ctp.printBranches(all, covered);
		new TgPrinter(all, covered).print();
	}
	
	@Test
	public void testTerms() {
		URL in = this.getClass().getResource("cobertura.ser");
		File f = new File(in.getFile());
		CoberturaTgPrinter ctp = new CoberturaTgPrinter(f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		ctp.printTerms(all, covered);
		new TgPrinter(all, covered).print();
	}
	
}
