package errors;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class ErrorsTable {

	private final List<Error> errorsList = new ArrayList<>();

	public void addError(String errorMessage, int errorLine) {
		addError(new Error(errorMessage, errorLine));
	}

	private void addError(Error error) {
		errorsList.add(error);
	}

	public void printErrors(PrintStream out) {
		for (Error error : errorsList) {
			out.println(error.toString());
		}
	}

	public boolean isEmpty() {
		return errorsList.isEmpty();
	}
}
