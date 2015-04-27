package edu.washington.cs.tgs;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.Test;

public class MapBuilderTest {

	@Test
	public void test() {
		URL in = this.getClass().getResource("ParseMe.java.txt");
		File f = new File(in.getFile());
		MapBuilder mb = new MapBuilder(f);
		mb.buildMap();
	}
	
	public void recurse(Class<?> klass) {
		System.out.println(klass.getCanonicalName());
		for (Class<?> k: klass.getDeclaredClasses()) {
			recurse(k);
		}
	}
	
	@Test
	public void test2() throws Exception {
		System.out.println("--------");
		Class<?> klass = Class.forName("edu.washington.cs.tgs.TT");
		recurse(klass);
	}

}
