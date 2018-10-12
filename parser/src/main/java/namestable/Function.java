package namestable;

import java.util.ArrayList;
import java.util.List;

public class Function {

	private final int line;
	private final String name;
	private List<Variable> args = new ArrayList<>();
	private boolean isImplemented = false;
	public boolean returns = false;

	public Function(String name, List<Variable> args, int line) {
		if (args != null) {
			this.args = args;
		}
		this.name = name;
		this.line = line;
	}

	public String getName() {
		return name;
	}

	public int getLine() {
		return line;
	}

	public List<Variable> getArgs() {
		return args;
	}

	public boolean implement() {
		if (!isImplemented) {
			return isImplemented = true;
		}
		return false;
	}	
	
	public void setReturns() {
	    returns = true;
	}
	
	public boolean isReturns() {
	    return returns;
	}

	@Override
	public String toString() {
		return "line " + line + " - " + "function: " + name + ", args : " + args.toString();
	}
}