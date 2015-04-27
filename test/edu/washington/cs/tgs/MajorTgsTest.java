package edu.washington.cs.tgs;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import edu.washington.cs.tgs.jmockit.JmockitTgPrinter;
import edu.washington.cs.tgs.major.MajorTgPrinter;

public class MajorTgsTest {

	
	@Test
	public void test() {
		URL in = this.getClass().getResource("mutants.log");
		File f = new File(in.getFile());
		
		URL in2 = this.getClass().getResource("kill.csv");
		File f2 = new File(in2.getFile());
		
		MajorTgPrinter mtp = new MajorTgPrinter(f2, f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		mtp.printLines(all, covered);
		new TgPrinter(all, covered).print();
	}

	
	@Test
	public void testMutants() {
		URL in = this.getClass().getResource("mutants.log");
		File f = new File(in.getFile());
		
		URL in2 = this.getClass().getResource("kill.csv");
		File f2 = new File(in2.getFile());
		
		MajorTgPrinter mtp = new MajorTgPrinter(f2, f);
		HashSet<Tg> all, covered;
		all = new HashSet<Tg>();
		covered = new HashSet<Tg>();
		mtp.printMutants(all, covered);
		new TgPrinter(all, covered).print();
	}
}
