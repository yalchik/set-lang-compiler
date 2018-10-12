package semantic;

import java.util.List;

import namestable.Function;
import namestable.FunctionNamesTable;
import namestable.Variable;
import namestable.VariableNamesTable;
import errors.ErrorsTable;

public class SemanticHandler {
    // таблица имЄн дл€ функций
    private final FunctionNamesTable functionNamesTable = new FunctionNamesTable();
    // таблица семантических ошибок
    private final ErrorsTable errorsTable = new ErrorsTable();
    // таблица имЄн дл€ локальных переменных
    private VariableNamesTable currentNamesTable = new VariableNamesTable();

    private Function currentFunction;

    public ErrorsTable getErrorsTable() {
	return errorsTable;
    }

    public void declareVariable(String id, int line) {
	// если переменна€ ещЄ не объ€влена, то добавл€ем еЄ в таблицу имЄн,
	// иначе генерируем ошибку
	if (!currentNamesTable.isDeclarated(id)) {
	    currentNamesTable.addVariable(new Variable(id, line));
	} else {
	    errorsTable.addError(String.format(
		    "variable '%s' has already been declared", id), line);
	}
    }

    public void declareFunction(String id, List<Variable> parameters, int line) {
	// если функци€ ещЄ не объ€вл€лась, то добавл€ем еЄ в таблицу имЄн,
	// иначе регистрируем ошибку
	if (!functionNamesTable.isDeclarated(id)) {
	    functionNamesTable.addFunction(new Function(id, parameters, line));
	} else {
	    errorsTable.addError(String.format(
		    "function '%s' has already been declared", id), line);
	}
    }

    public void defineFunction(String id, List<Variable> parameters, int line) {
	// если функци€ уже объ€влена, то свер€ем количество параметров в
	// объ€влении и определении функции
	// количество параметров должно совпадать
	// если функци€ ещЄ не объ€влена, то объ€вл€ем еЄ
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

	// устанавливаем флаг определени€ функции, если он не установлен
	// добавл€ем аргументы функции в локальную таблицу имЄн
	// если флаг определени€ функции уже установлен, то регистрируем ошибку
	// повторного определени€ функции
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
	// уходим в дочернюю таблицу имЄн
	currentNamesTable = new VariableNamesTable(currentNamesTable);
    }

    public void enterParentNamesTable() {
	// возвращаемс€ в родительскую таблицу имЄн
	currentNamesTable = currentNamesTable.getParentTable();
    }

    public boolean checkVariableDeclaration(String id, int line) {
	// провер€ем, объ€влена ли переменна€
	// если нет, то объ€вл€ем еЄ
	if (!currentNamesTable.isDeclarated(id)) {
	    declareVariable(id, line);
	    return false;
	}
	return true;
    }
    
    public void checkVariableInitialization(String id, int line) {
	// провер€ем, инициализирована ли переменна€
	// если нет, то регистрируем ошибку
	// (на самом деле провер€ем, была ли она объ€влена,
	// так как это будет указывать на то, что еЄ как-то проинициализировали)
	if (!currentNamesTable.isDeclarated(id)) {
		errorsTable.addError("variable \"" + id + "\" is not initializated",
				line);
	}
}

    public void checkFunctionDeclaration(String id, int line) {
	// провер€ем, объ€влена ли функци€
	// если нет, то регистрируем ошибку
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
