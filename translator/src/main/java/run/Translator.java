package run;

import grammar.RLexer;
import grammar.RParser;

import java.io.FileWriter;
import java.io.InputStreamReader;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;

public class Translator {

	public static final String TEMPLATE_FILE_NAME = "/java.stg";
    public static final String JAVA_LIBRARY_FILE_NAME = "/RLibrary.java";
	public static final String GENERATED_FILE_NAME = "Program.java";

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Error: no input file. Pass rel-lang source file as the argument");
			System.exit(1);
		} else if (args.length > 1) {
			System.out.println("Warn: too many arguments. Pass only rel-lang source file as the argument");
		}
		String sourceFilename = args[0];

        try {
			RLexer lexer = new RLexer(new ANTLRFileStream(args[0]));
			RParser parser = new RParser(new CommonTokenStream(lexer));

			parser.setTemplateLib(new StringTemplateGroup(new InputStreamReader(Translator.class.getResourceAsStream(TEMPLATE_FILE_NAME)), AngleBracketTemplateLexer.class));
			RuleReturnScope returnScope = parser.program();

			if (parser.getErrorsTable().isEmpty()) {
				System.out.println("Successful compiling!");
				try (FileWriter out = new FileWriter(GENERATED_FILE_NAME)) {
					String content = convertStreamToString(Translator.class.getResourceAsStream(JAVA_LIBRARY_FILE_NAME));
//                    System.out.println(content);
					out.write(content);
					out.write(returnScope.getTemplate().toString());
				}
			} else {
				System.out.println("Next errors were found: ");
				parser.getErrorsTable().printErrors(System.out);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
