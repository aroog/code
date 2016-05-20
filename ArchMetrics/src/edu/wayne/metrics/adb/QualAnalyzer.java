package edu.wayne.metrics.adb;


import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

import edu.wayne.metrics.qual.C_AllMetrics;
import edu.wayne.metrics.qual.C_VisitInstantiatedType;

import org.eclipse.jdt.core.dom.IVariableBinding;;


//TODO: TOSUM: Create objects of all C_Metrics here. Traverse the collected stuff here.
public class QualAnalyzer {

	CompilationUnit unit;
	private Writer writer;
	private String filePath;	
	private String noInstancefilePath;	
	private ArrayList<IVariableBinding> var = new ArrayList<IVariableBinding>();
	private HashMap<ITypeBinding, String> newExpr = new HashMap<ITypeBinding,String>();
	
	// Constructor - Get the paths and ITypebindings
	public QualAnalyzer(String filePaths,String noInstancefilePath, ArrayList<IVariableBinding> var, HashMap<ITypeBinding, String> newExpr)  
			 throws IOException {		
		this.filePath = filePaths;	
		this.var = var;
		this.newExpr = newExpr;
		this.noInstancefilePath = noInstancefilePath;
		visitNoAnnotat();	
		visitNewExpr();
	}

	// Visit all the No Annotat Metrics Object
	private void visitNoAnnotat() throws IOException
	{
		writer = new CustomWriter(filePath);
		C_AllMetrics noAnnotat = new C_AllMetrics(writer,var);
		noAnnotat.visit();
		noAnnotat.display();		
	    writer.flush();
		writer.close();
	}
	
	// Visit all the No Annotat Metrics Object
	private void visitNewExpr() throws IOException
	{
		writer = new CustomWriter(noInstancefilePath);
		C_VisitInstantiatedType noAnnotat = new C_VisitInstantiatedType(writer,newExpr);
		noAnnotat.visit();
		noAnnotat.display();		
	    writer.flush();
		writer.close();
	}
}
