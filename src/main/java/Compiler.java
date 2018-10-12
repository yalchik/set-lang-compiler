import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStreams;
//import org.antlr.stringtemplate.StringTemplateGroup;
//import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;

public class Compiler {

	public static final String TEMPLATE_FILE_NAME = "cpp.stg";
	public static final String GENERATED_FILE_NAME = "program.cpp";

	public static void main(String[] args) {
		try {
			HelloLexer hl = new HelloLexer(CharStreams.fromString("hello"));
//			HelloParser hp;
//			SetsLexer lexer = new SetsLexer(new ANTLRFileStream(args[0]));
//			SetsParser parser = new SetsParser(new CommonTokenStream(lexer));
//
//			parser.setTemplateLib(new StringTemplateGroup(new FileReader(
//					TEMPLATE_FILE_NAME), AngleBracketTemplateLexer.class));
//			RuleReturnScope returnScope = parser.program();
//
//			if (parser.getErrorsTable().isEmpty()) {
//				System.out.println("Successful compiling!");
//				try (FileWriter out = new FileWriter(GENERATED_FILE_NAME)) {
//					String content = new String(Files.readAllBytes(Paths
//							.get("lib\\library-code.cpp")));
//					// System.out.println(content);
//					out.write(content);
//					out.write(returnScope.getTemplate().toString());
//				}
//			} else {
//				System.out.println("Next errors were found: ");
//				parser.getErrorsTable().printErrors(System.out);
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
