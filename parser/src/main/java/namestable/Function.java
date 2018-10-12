package namestable;

import java.util.ArrayList;
import java.util.List;

public class Function {

	private final int line;
	private final String name;
	private List<Variable> args = new ArrayList<>();
	private List<String> templates = new ArrayList<>();
	private boolean isImplemented = false;
	private boolean templated = false;
	public boolean returns = false;

	public Function(String name, List<Variable> args, int line,
			List<String> templates) {
		if (args != null) {
			this.args = args;
		}
		this.name = name;
		this.line = line;
		if (templates != null) {
			this.templates = templates;
			templated = true;
		}
	}

	public Function(String name, List<Variable> args, int line) {
		this(name, args, line, null);
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

	public List<String> getTemplates() {
		return templates;
	}

	public boolean isTemplated() {
		return templated;
	}

	public boolean implement() {
		if (!isImplemented) {
			return isImplemented = true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "line " + line + " - " + "procedure : " + name + ", args : "
				+ args.toString();
	}
}