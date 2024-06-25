package redcompiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import lexer.Lexer;
import parser.Parser;

/**
 *
 * @autor mark
 */
public class RedCompiler {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            System.setIn(new FileInputStream(new File(args[0])));
            Lexer lexer = new Lexer();
            Parser parser = new Parser(lexer);
            parser.start();
        } else {
            System.out.println("No input file specified. Please provide the input file as a command-line argument.");
        }
    }
    
}
