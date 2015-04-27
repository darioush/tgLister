package edu.washington.cs.tgs;

public class Pair<T, K> {
	public T first;
	public K second;

	public Pair(T first, K second) {
		this.first = first; this.second = second;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (null == obj || !(obj instanceof Pair<?,?>)) {
			return false;
		}
		@SuppressWarnings("unchecked")
		Pair<T, K> p = (Pair<T, K>) obj;
		return this.first.equals(p.first) && this.second.equals(p.second);
	}
	
	@Override
	public int hashCode() {
		return this.first.hashCode() * 7 + this.second.hashCode() * 13;
	}
}
