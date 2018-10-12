package semantic;

import java.util.List;

import namestable.Function;
import namestable.FunctionNamesTable;
import namestable.Variable;
import namestable.VariableNamesTable;
import errors.ErrorsTable;

public class SemanticHandler {
    // ������� ��� ��� �������
    private final FunctionNamesTable functionNamesTable = new FunctionNamesTable();
    // ������� ������������� ������
    private final ErrorsTable errorsTable = new ErrorsTable();
    // ������� ��� ��� ��������� ����������
    private VariableNamesTable currentNamesTable = new VariableNamesTable();

    private Function currentFunction;

    public ErrorsTable getErrorsTable() {
	return errorsTable;
    }

    public void declareVariable(String id, int line) {
	// ���� ���������� ��� �� ���������, �� ��������� � � ������� ���,
	// ����� ���������� ������
	if (!currentNamesTable.isDeclarated(id)) {
	    currentNamesTable.addVariable(new Variable(id, line));
	} else {
	    errorsTable.addError(String.format(
		    "variable '%s' has already been declared", id), line);
	}
    }

    public void declareFunction(String id, List<Variable> parameters, int line) {
	// ���� ������� ��� �� �����������, �� ��������� � � ������� ���,
	// ����� ������������ ������
	if (!functionNamesTable.isDeclarated(id)) {
	    functionNamesTable.addFunction(new Function(id, parameters, line));
	} else {
	    errorsTable.addError(String.format(
		    "function '%s' has already been declared", id), line);
	}
    }

    public void defineFunction(String id, List<Variable> parameters, int line) {
	// ���� ������� ��� ���������, �� ������� ���������� ���������� �
	// ���������� � ����������� �������
	// ���������� ���������� ������ ���������
	// ���� ������� ��� �� ���������, �� ��������� �
	currentFunction = functionNamesTable.getFunction(id);
	if (currentFunction != null) {
	    List<Variable> declaredArgs = currentFunction.getArgs();
	    List<Variable> defArgs = parameters;
	    if ((defArgs == null && declaredArgs.size() > 0)
		    || (defArgs != null && defArgs.size() != declaredArgs
			    .size())) {
		errorsTable.addError(String.format(
			"function '%s' must have %d parameter(s). See line %d",
			id, declaredArgs.size(), currentFunction.getLine()),
			line);
	    }
	} else {
	    currentFunction = new Function(id, parameters, line);
	    functionNamesTable.addFunction(currentFunction);
	}

	// ������������� ���� ����������� �������, ���� �� �� ����������
	// ��������� ��������� ������� � ��������� ������� ���
	// ���� ���� ����������� ������� ��� ����������, �� ������������ ������
	// ���������� ����������� �������
	if (currentFunction.implement()) {
	    if (parameters != null) {
		for (Variable var : parameters) {
		    currentNamesTable.addVariable(var);
		}
	    }
	} else {
	    errorsTable.addError(String.format(
		    "function '%s' has already been implemented", id), line);
	}
    }

    public void enterLocalNamesTable() {
	// ������ � �������� ������� ���
	currentNamesTable = new VariableNamesTable(currentNamesTable);
    }

    public void enterParentNamesTable() {
	// ������������ � ������������ ������� ���
	currentNamesTable = currentNamesTable.getParentTable();
    }

    public boolean checkVariableDeclaration(String id, int line) {
	// ���������, ��������� �� ����������
	// ���� ���, �� ��������� �
	if (!currentNamesTable.isDeclarated(id)) {
	    declareVariable(id, line);
	    return false;
	}
	return true;
    }
    
    public void checkVariableInitialization(String id, int line) {
	// ���������, ���������������� �� ����������
	// ���� ���, �� ������������ ������
	// (�� ����� ���� ���������, ���� �� ��� ���������,
	// ��� ��� ��� ����� ��������� �� ��, ��� � ���-�� �������������������)
	if (!currentNamesTable.isDeclarated(id)) {
		errorsTable.addError("variable \"" + id + "\" is not initializated",
				line);
	}
}

    public void checkFunctionDeclaration(String id, int line) {
	// ���������, ��������� �� �������
	// ���� ���, �� ������������ ������
	if (!functionNamesTable.isDeclarated(id)) {
	    errorsTable.addError(
		    String.format("function '%s' is not declared", id), line);
	}
    }

    public void setCurrentFunctionReturn() {
	if (currentFunction != null) {
	    currentFunction.setReturns();
	}
    }

    public void checkFunctionReturn() {
	if (!currentFunction.isReturns()) {
	    errorsTable.addError(String.format(
		    "function '%s' has no return operator",
		    currentFunction.getName()), currentFunction.getLine());
	}
    }

    public boolean isGlobalCurrentNamesTable() {
	return currentNamesTable.getParentTable() == null ? true : false;
    }

    @SuppressWarnings("rawtypes")
    public void checkCallFunction(String id, List args, int line) {
	Function callingFunction = functionNamesTable.getFunction(id);
	if (callingFunction != null) {
	    if (args.size() != callingFunction.getArgs().size()) {
		errorsTable.addError(String.format(
			"function '%s' must have %d argument(s). See line %d",
			id, callingFunction.getArgs().size(),
			callingFunction.getLine()), line);
	    }
	}
    }

}
