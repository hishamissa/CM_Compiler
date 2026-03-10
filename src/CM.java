import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import absyn.Absyn;
import absyn.ShowTreeVisitor;
import absyn.SemanticAnalyzer;

/**
 * Main driver for the C- compiler.
 * Handles command-line arguments and coordinates parsing,
 * AST generation, and semantic analysis.
 */
public class CM {
    public static boolean SHOW_TREE = false;
    public static boolean SHOW_TABLE = false;
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java CM [-a] [-s] <filename>");
            System.exit(1);
        }
        
        String filename = null;
        
        // Parse command-line flags
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-a")) {
                SHOW_TREE = true;
            } else if (args[i].equals("-s")) {
                SHOW_TABLE = true;
            } else {
                filename = args[i];
            }
        }
        
        if (filename == null) {
            System.err.println("Error: No input file specified");
            System.exit(1);
        }
        
        try {
            // Parse input file and build AST
            parser p = new parser(new Lexer(new FileReader(filename)));
            Absyn result = (Absyn)(p.parse().value);
            
            // Display AST if -a flag specified
            if (SHOW_TREE && result != null) {
                System.out.println("Abstract syntax tree:");
                ShowTreeVisitor visitor = new ShowTreeVisitor();
                result.accept(visitor, 0);
            }
            
            // Perform semantic analysis if -s flag specified
            if (SHOW_TABLE && result != null) {
                // Create output file: replace .cm extension with .sym
                String symFilename = filename.substring(0, filename.lastIndexOf('.')) + ".sym";
                PrintWriter symOut = new PrintWriter(new FileWriter(symFilename));
                
                SemanticAnalyzer analyzer = new SemanticAnalyzer(symOut);
                result.accept(analyzer, 0);
                
                symOut.close();
                System.out.println("Symbol table saved to " + symFilename);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
