package namestable;

import java.util.HashMap;
import java.util.Map;

public class VariableNamesTable {

	private final Map<String, Variable> variables = new HashMap<>();

	private final VariableNamesTable parentTable;

	public VariableNamesTable() {
		this(null);
	}

	public VariableNamesTable(VariableNamesTable table) {
		parentTable = table;
	}

	public boolean isDeclarated(String varName) {
		boolean declarated = variables.containsKey(varName);
		// если не нашли, то смотрим в родительских таблицах
		if (!declarated && parentTable != null) {
			declarated = parentTable.isDeclarated(varName);
		}
		return declarated;
	}

	public void addVariable(Variable var) {
		variables.put(var.getName(), var);
	}

	public Variable getVariable(String varName) {
		Variable var = variables.get(varName);
		// если не нашли, то смотрим в родительских таблицах
		if (var == null && parentTable != null) {
			var = parentTable.getVariable(varName);
		}
		return var;
	}

	public VariableNamesTable getParentTable() {
		return parentTable;
	}
}
