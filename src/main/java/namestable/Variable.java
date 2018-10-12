package namestable;

public class Variable {

	private final int line;
	private final String name;

	public Variable(String name, int line) {
		this.name = name;
		this.line = line;
	}

	@Override
	public String toString() {
		return "line " + line + ": " + name;
	}

	public String getName() {
		return name;
	}

	public int getLine() {
		return line;
	}
}