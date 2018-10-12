package namestable;

import java.util.HashMap;
import java.util.Map;

public class FunctionNamesTable {

	private final Map<String, Function> functions = new HashMap<>();

	public boolean isDeclarated(String functionName) {
		return functions.containsKey(functionName);
	}

	public void addFunction(Function function) {
		functions.put(function.getName(), function);
	}

	public Function getFunction(String functionName) {
		return functions.get(functionName);
	}
}
