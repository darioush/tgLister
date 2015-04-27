package edu.washington.cs.tgs;

import java.util.Arrays;
import java.util.Set;

public class TgPrinter {

	private Set<Tg> covered;
	private Set<Tg> all;

	public TgPrinter(Set<Tg> all, Set<Tg> covered) {
		this.all = all;
		this.covered = covered;
	}
	
	public void print() {
		Tg [] tgs = this.all.toArray(new Tg[this.all.size()]);
		Arrays.sort(tgs);
		for (Tg tg : tgs) {
			if (covered.contains(tg)) {
				System.out.println("+ " + tg);
			} else {
				System.out.println("- " + tg);
			}
		}
	}
}
