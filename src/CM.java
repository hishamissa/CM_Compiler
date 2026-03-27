import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import absyn.Absyn;
import absyn.ShowTreeVisitor;
import absyn.SemanticAnalyzer;
import absyn.CodeGenerator;

/**
 * Main driver for the C- compiler.
 * Handles command-line arguments and coordinates parsing,
 * AST generation, and semantic analysis.
 */
public class CM {
    public static boolean SHOW_TREE = false;
    public static boolean SHOW_TABLE = false;
    public static boolean GENERATE_CODE = false;
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java CM [-a] [-s] [-c] <filename>");
            System.exit(1);
        }
        
        String filename = null;
        
        // Parse command-line flags
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-a")) {
                SHOW_TREE = true;
            } else if (args[i].equals("-s")) {
                SHOW_TABLE = true;
            } else if (args[i].equals("-c")) {
                GENERATE_CODE = true;
            } else {
                filename = args[i];
            }
        }
        
        if (filename == null) {
            System.err.println("Error: No input file specified");
            System.exit(1);
        }
        
        try {
            // Reset parser error flag
            parser.hasParseErrors = false;
            
            // Parse input file and build AST
            parser p = new parser(new Lexer(new FileReader(filename)));
            Object parseResult = p.parse().value;
            
            // Check if parsing succeeded and returned an AST
            if (parser.hasParseErrors || !(parseResult instanceof Absyn)) {
                System.err.println("\nParsing failed. Semantic analysis skipped.");
                return;
            }
            
            Absyn result = (Absyn) parseResult;
            
            // Display AST if -a flag specified
            if (SHOW_TREE && result != null) {
                String absFilename = filename.substring(0, filename.lastIndexOf('.')) + ".abs";
                PrintWriter absOut = new PrintWriter(new FileWriter(absFilename));
                
                absOut.println("Abstract syntax tree:");
                ShowTreeVisitor treeVisitor = new ShowTreeVisitor(absOut);
                result.accept(treeVisitor, 0);
                
                absOut.close();
                System.out.println("Abstract syntax tree saved to " + absFilename);
            }
            
            // Perform semantic analysis if -s flag specified
            if (SHOW_TABLE && result != null) {
                String symFilename = filename.substring(0, filename.lastIndexOf('.')) + ".sym";
                PrintWriter symOut = new PrintWriter(new FileWriter(symFilename));
                
                SemanticAnalyzer analyzer = new SemanticAnalyzer(symOut);
                result.accept(analyzer, 0);
                
                symOut.close();
                
                if (SemanticAnalyzer.hasSemanticErrors) {
                    System.err.println("\nSemantic analysis completed with errors.");
                }
                System.out.println("Symbol table saved to " + symFilename);
            }

            // Generate assembly code if -c flag specified
            if (GENERATE_CODE && result != null) {
                // Run semantic analysis if not already done
                boolean ranSemanticAnalysis = SHOW_TABLE;
                boolean hasErrors = false;
                
                if (!ranSemanticAnalysis) {
                    // Run semantic analysis silently (no output file)
                    SemanticAnalyzer analyzer = new SemanticAnalyzer(new PrintWriter(System.out));
                    result.accept(analyzer, 0);
                    hasErrors = SemanticAnalyzer.hasSemanticErrors;
                } else {
                    hasErrors = SemanticAnalyzer.hasSemanticErrors;
                }
                
                // Only generate code if no semantic errors
                if (!hasErrors) {
                    String tmFilename = filename.substring(0, filename.lastIndexOf('.')) + ".tm";
                    PrintWriter tmOut = new PrintWriter(new FileWriter(tmFilename));
                    
                    CodeGenerator codegen = new CodeGenerator(tmOut);
                    codegen.generate(result);
                    
                    tmOut.close();
                    System.out.println("Assembly code saved to " + tmFilename);
                } else {
                    System.err.println("\nCode generation skipped due to semantic errors.");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}