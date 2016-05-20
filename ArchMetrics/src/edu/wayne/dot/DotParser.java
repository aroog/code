package edu.wayne.dot;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import net.claribole.zgrviewer.dot.DOTLexer;
import net.claribole.zgrviewer.dot.DOTParser;
import net.claribole.zgrviewer.dot.DOTTreeParser;
import net.claribole.zgrviewer.dot.DOTTreeTransformer;
import net.claribole.zgrviewer.dot.Graph;
import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;

public class DotParser {

	public static Graph parse(String f) {
		File file = new File(f);
		Graph g = null;
		
		try {
			// input stream containing the DOT file
			DataInputStream input = new DataInputStream(new FileInputStream(file));

			// DOT lexer and parser
			DOTLexer lexer = new DOTLexer(input);
			DOTParser parser = new DOTParser(lexer);
			parser.graph();

			// parsing produces an abstract syntax tree
			CommonAST ast = (CommonAST) parser.getAST();

			// this AST is transformed into a Graph data structure
			DOTTreeTransformer trans = new DOTTreeTransformer();
			trans.graph(ast);
			CommonAST astTrans = (CommonAST) trans.getAST();
			DOTTreeParser walker = new DOTTreeParser();
			g = walker.graph(astTrans);
		}
		catch (FileNotFoundException e) {
			System.err.println("File not found");
		}
		catch (TokenStreamException e) {
			e.printStackTrace();
		}
		catch (RecognitionException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return g;
	}
}
