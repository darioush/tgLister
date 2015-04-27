package edu.washington.cs.tgs;

import java.util.Set;

public interface TgBuilder {
	public void build (String dir, Set<Tg> allTgs, Set<Tg> covered);
}
