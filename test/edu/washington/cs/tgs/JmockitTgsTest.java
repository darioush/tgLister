package edu.washington.cs.tgs;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import org.junit.Test;

import edu.washington.cs.tgs.jmockit.JmockitTgPrinter;

public class JmockitTgsTest {

	@Test
	public void test() {
		URL in = this.getClass().getResource("coverage.ser");
		File f = new File(in.getFile());
		JmockitTgPrinter jtp = new JmockitTgPrinter(f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		jtp.printLines(all, covered);
		new TgPrinter(all, covered).print();
	}

	
	//@Test
	public void testBranch() {
		URL in = this.getClass().getResource("coverage.ser");
		File f = new File(in.getFile());
		JmockitTgPrinter jtp = new JmockitTgPrinter(f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		jtp.printBranches(all, covered);
		new TgPrinter(all, covered).print();
	}

//	@Test
	public void testTerm() {
		URL in = this.getClass().getResource("coverage.ser");
		File f = new File(in.getFile());
		JmockitTgPrinter jtp = new JmockitTgPrinter(f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		jtp.printTerms(all, covered);
		new TgPrinter(all, covered).print();
	}
	
	//@Test
	public void testData() {
		URL in = this.getClass().getResource("coverage.ser");
		File f = new File(in.getFile());
		JmockitTgPrinter jtp = new JmockitTgPrinter(f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		jtp.printData(all, covered);
		new TgPrinter(all, covered).print();
	}
}
