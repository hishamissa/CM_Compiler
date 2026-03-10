import java.io.FileReader;
import absyn.Absyn;
import absyn.ShowTreeVisitor;
import absyn.SemanticAnalyzer;

public class CM {
    public static boolean SHOW_TREE = false;
    public static boolean SHOW_TABLE = false;  // NEW
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java CM [-a] [-s] <filename>");
            System.exit(1);
        }
        
        String filename = null;
        
        // Parse command line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-a")) {
                SHOW_TREE = true;
            } else if (args[i].equals("-s")) {
                SHOW_TABLE = true;  // NEW
            } else {
                filename = args[i];
            }
        }
        
        if (filename == null) {
            System.err.println("Error: No input file specified");
            System.exit(1);
        }
        
        try {
            parser p = new parser(new Lexer(new FileReader(filename)));
            Absyn result = (Absyn)(p.parse().value);
            
            if (SHOW_TREE && result != null) {
                System.out.println("Abstract syntax tree:");
                ShowTreeVisitor visitor = new ShowTreeVisitor();
                result.accept(visitor, 0);
            }
            
            // NEW: Semantic analysis (we'll implement this fully in later phases)
            if (SHOW_TABLE && result != null) {
                System.out.println("\nBuilding symbol table...");
                SemanticAnalyzer analyzer = new SemanticAnalyzer();
                result.accept(analyzer, 0);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
