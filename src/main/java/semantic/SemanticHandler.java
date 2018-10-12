package semantic;

import java.util.List;

import namestable.Function;
import namestable.FunctionNamesTable;
import namestable.Variable;
import namestable.VariableNamesTable;
import errors.ErrorsTable;

public class SemanticHandler {
	// таблица имЄн дл¤ функций
	private final FunctionNamesTable functionNamesTable = new FunctionNamesTable();
	// таблица имЄн дл¤ глобальных переменных
	// private final VariableNamesTable globalNamesTable = new
	// VariableNamesTable();
	// таблица семантических ошибок
	private final ErrorsTable errorsTable = new ErrorsTable();
	// таблица имЄн дл¤ локальных переменных
	private VariableNamesTable currentNamesTable = new VariableNamesTable();// globalNamesTable;

	public ErrorsTable getErrorsTable() {
		return errorsTable;
	}

	public void declareVariable(String id, int line) {
		// если переменна¤ ещЄ не объ¤влена, то добавл¤ем еЄ в таблицу имЄн,
		// иначе генерируем ошибку
		if (!currentNamesTable.isDeclarated(id)) {
			currentNamesTable.addVariable(new Variable(id, line));
			// System.out.println($ID.line + " " + varName);
		} else {
			errorsTable.addError("variable \"" + id
					+ "\" has already been declared", line);
		}
	}

	public void declareFunction(String id, List<Variable> parameters, int line,
			List<String> templates) {
		// если функци¤ ещЄ не объ¤вл¤лась, то добавл¤ем еЄ в таблицу имЄн,
		// иначе регистрируем ошибку
		if (!functionNamesTable.isDeclarated(id)) {
			functionNamesTable.addFunction(new Function(id, parameters, line,
					templates));
		} else {
			errorsTable.addError("function \"" + id
					+ "\" has already been declared", line);
		}
	}

	public void defineFunction(String id, List<Variable> parameters, int line,
			List<String> templates) {
		// если функци¤ уже объ¤влена, то свер¤ем количество параметров в
		// объ¤влении и определении функции
		// количество параметров должно совпадать
		// если функци¤ ещЄ не объ¤влена, то объ¤вл¤ем еЄ
		Function currentFunction = functionNamesTable.getFunction(id);
		if (currentFunction != null) {
			List<Variable> declaredArgs = currentFunction.getArgs();
			List<Variable> defArgs = parameters;
			if ((defArgs == null && declaredArgs.size() > 0)
					|| (defArgs != null && defArgs.size() != declaredArgs
							.size())) {
				errorsTable.addError("function \"" + id + "\" must have "
						+ declaredArgs.size() + " parameter(s). See line "
						+ currentFunction.getLine(), line);
			}
		} else {
			currentFunction = new Function(id, parameters, line, templates);
			functionNamesTable.addFunction(currentFunction);
		}

		// устанавливаем флаг определени¤ функции, если он не установлен
		// добавл¤ем аргументы функции в локальную таблицу имЄн
		// если флаг определени¤ функции уже установлен, то регистрируем ошибку
		// повторного определени¤ функции
		if (currentFunction.implement()) {
			if (parameters != null) {
				for (Variable var : parameters) {
					currentNamesTable.addVariable(var);
				}
			}
		} else {
			errorsTable.addError("function \"" + id
					+ "\" has already been implemented", line);
		}
	}

	public void enterLocalNamesTable() {
		// уходим в дочернюю таблицу имЄн
		currentNamesTable = new VariableNamesTable(currentNamesTable);
	}

	public void enterParentNamesTable() {
		// возвращаемс¤ в родительскую таблицу имЄн
		currentNamesTable = currentNamesTable.getParentTable();
	}

	public void checkVariableDeclaration(String id, int line) {
		// провер¤ем, объ¤влена ли переменна¤
		// если нет, то регистрируем ошибку
		if (!currentNamesTable.isDeclarated(id)) {
			errorsTable.addError("variable \"" + id + "\" is not declared",
					line);
		}
	}

	public void checkFunctionDeclaration(String id, int line) {
		// провер¤ем, объ¤влена ли функци¤
		// если нет, то регистрируем ошибку
		if (!functionNamesTable.isDeclarated(id)) {
			errorsTable.addError("function \"" + id + "\" is not declared",
					line);
		}
	}

	public void checkMainFunction() {
		// провер¤ем, определена ли функци¤ с именем main
		// если нет, то генерируем ошибку
		if (!functionNamesTable.isDeclarated("main")) {
			errorsTable.addError("function \"" + "main" + "\" must be defined",
					0);
		}
	}
}
