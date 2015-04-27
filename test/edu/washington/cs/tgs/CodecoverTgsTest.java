package edu.washington.cs.tgs;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import org.junit.Test;

import edu.washington.cs.tgs.cobertura.CoberturaTgPrinter;
import edu.washington.cs.tgs.codecover.CodecoverTgPrinter;
import edu.washington.cs.tgs.jmockit.JmockitTgPrinter;

public class CodecoverTgsTest {

	//@Test
	public void test() {
		URL in = this.getClass().getResource("codecover.xml");
		File f = new File(in.getFile());
		CodecoverTgPrinter ctp = new CodecoverTgPrinter(f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		ctp.printLines(all, covered);
		new TgPrinter(all, covered).print();
	}
	
	//@Test
	public void testStatement() {
		URL in = this.getClass().getResource("codecover.xml");
		File f = new File(in.getFile());
		CodecoverTgPrinter ctp = new CodecoverTgPrinter(f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		ctp.printStatements(all, covered);
		new TgPrinter(all, covered).print();
	}
	
	//@Test
	public void testBranches() {
		URL in = this.getClass().getResource("codecover.xml");
		File f = new File(in.getFile());
		CodecoverTgPrinter ctp = new CodecoverTgPrinter(f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		ctp.printBranches(all, covered);
		new TgPrinter(all, covered).print();
	}
	
	//@Test
	public void testLoops() {
		URL in = this.getClass().getResource("codecover.xml");
		File f = new File(in.getFile());
		CodecoverTgPrinter ctp = new CodecoverTgPrinter(f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		ctp.printLoops(all, covered);
		new TgPrinter(all, covered).print();
	}
	
	@Test
	public void testTerms() {
		URL in = this.getClass().getResource("codecover.xml");
		File f = new File(in.getFile());
		CodecoverTgPrinter ctp = new CodecoverTgPrinter(f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		ctp.printTerms(all, covered);
		new TgPrinter(all, covered).print();
	}
}
