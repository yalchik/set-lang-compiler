package run;

import grammar.SetsLexer;
import grammar.SetsParser;

import java.io.FileWriter;
import java.io.InputStreamReader;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;

public class Translator {

	public static final String TEMPLATE_FILE_NAME = "/cpp.stg";
    public static final String CPP_LIBRARY_FILE_NAME = "/library-code.cpp";
	public static final String GENERATED_FILE_NAME = "program.cpp";

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Error: no input file. Pass set-lang source file as the argument");
			System.exit(1);
		} else if (args.length > 1) {
			System.out.println("Warn: too many arguments. Pass only set-lang source file as the argument");
		}
		String sourceFilename = args[0];

        try {
			SetsLexer lexer = new SetsLexer(new ANTLRFileStream(sourceFilename));
			SetsParser parser = new SetsParser(new CommonTokenStream(lexer));

			parser.setTemplateLib(new StringTemplateGroup(new InputStreamReader(Translator.class.getResourceAsStream(TEMPLATE_FILE_NAME)), AngleBracketTemplateLexer.class));
			RuleReturnScope returnScope = parser.program();

			if (parser.getErrorsTable().isEmpty()) {
				System.out.println("Successful compiling!");
				try (FileWriter out = new FileWriter(GENERATED_FILE_NAME)) {
					String content = convertStreamToString(Translator.class.getResourceAsStream(CPP_LIBRARY_FILE_NAME));
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
