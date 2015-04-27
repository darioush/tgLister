package edu.washington.cs.tgs;


public class Tg implements Comparable<Tg> {

	public String file;
	public int line;
	public int toolIdx;
	public int tgIdx;
	public String type;
	public String tool;
	
	public Tg(String fn, int lineNumber, int toolIdx, int tgIdx, String type, String tool) {
		this.file = fn;
		this.line = lineNumber;
		this.toolIdx = toolIdx;
		this.tgIdx = tgIdx;
		this.type = type;
		this.tool = tool;
	}

	@Override
	public int hashCode() {
		return file.hashCode() + line + toolIdx + tgIdx + type.hashCode() + type.hashCode() + tool.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Tg)) {
			return false;
		}
		Tg other = (Tg) obj;
		return this.file.equals(other.file) &&
				this.line == other.line &&
				this.toolIdx == other.toolIdx &&
				this.tgIdx == other.tgIdx && 
				this.type.equals(other.type) &&
				this.tool.equals(other.tool);
	}
	
	
	@Override
	public String toString() {
		return String.format("%s:%s:%s:%d:%d:%d", type, tool, file, line, toolIdx, tgIdx);
	}

	@Override
	public int compareTo(Tg o) {
		if (this.equals(o)) {
			return 0;
		}
		
		if (this.type.compareTo(o.type) != 0) {
			return this.type.compareTo(o.type);
		}
		if (this.tool.compareTo(o.tool) != 0) {
			return this.tool.compareTo(o.tool);
		}
		if (this.line < o.line) {
			return -1;
		} else if (this.line > o.line) {
			return 1;
		}
		if (this.toolIdx < o.toolIdx) {
			return -1;
		} else if (this.toolIdx > o.toolIdx) {
			return 1;
		}
		if (this.tgIdx < o.tgIdx) {
			return -1;
		} else if (this.tgIdx > o.tgIdx) {
			return 1;
		}
		throw new RuntimeException("Not Possible");
	}
}
