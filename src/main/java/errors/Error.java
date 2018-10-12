package errors;

public class Error {

	private final String errorMessage;
	private final int errorLine;

	public Error(String errorMessage, int errorLine) {
		this.errorLine = errorLine;
		this.errorMessage = errorMessage;
	}

	public int getErrorLine() {
		return errorLine;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public String toString() {
		return "line " + Integer.toString(errorLine) + ":  " + errorMessage;
	}
}
