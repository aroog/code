package edu.wayne.metrics.qual;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import ast.TypeInfo;
import org.eclipse.jdt.core.dom.ITypeBinding;
import edu.wayne.metrics.utils.CSVConst;
import edu.wayne.metrics.utils.CSVOutputUtils;

public class C_VisitInstantiatedType extends Q_Base {

	private Writer writer;

	TypeInfo typeInfo = TypeInfo.getInstance();

	private int numOfInstantiated =0; 

	

	//private ArrayList<ITypeBinding> newExpr = new ArrayList<ITypeBinding>();

	private HashMap<ITypeBinding, String> newExpr = new HashMap<ITypeBinding, String>();
	
	public C_VisitInstantiatedType(Writer writer, HashMap<ITypeBinding, String> newExpr) {
		this.writer = writer;
		this.newExpr = newExpr;
	}


	@Override
	public void visit() {
		QualUtils utils = QualUtils.getInstance();
		Set<Map.Entry<ITypeBinding, String>> entries = newExpr.entrySet();
		 for(Map.Entry<ITypeBinding, String> entry : entries) {
			 ITypeBinding key = entry.getKey();
	         String value = entry.getValue();

	         if(!utils.isAbstractType(key.getQualifiedName()) && !utils.isInterfaceType(key.getQualifiedName())
	        		 && !utils.isFrameworkType(key.getQualifiedName()) && value == "true")
	         {
	          	System.out.println("key" +key.getQualifiedName());	     
	          	numOfInstantiated++;
	        	
	         }	        
	        }
		
	}

	@Override
	public void display() throws IOException {

		QualUtils utils = QualUtils.getInstance();		
		
		writer.append(CSVConst.NEWLINE);
		writer.append("Results of Quantitative analysis");
		writer.append(CSVConst.NEWLINE);

		// Header of the CSV
		writer.append(CSVConst.NEWLINE);
		writer.append("Type");
		writer.append(CSVConst.COMMA);			
		
		Set<Map.Entry<ITypeBinding, String>> entries = newExpr.entrySet();
		 for(Map.Entry<ITypeBinding, String> entry : entries) {
			 ITypeBinding key = entry.getKey();
	         String value = entry.getValue();

	         if(!utils.isAbstractType(key.getQualifiedName()) && !utils.isInterfaceType(key.getQualifiedName())
	        		 && !utils.isFrameworkType(key.getQualifiedName()) && value == "false")
	         {
	        	writer.append(CSVOutputUtils.sanitize((key.getQualifiedName())));
				writer.append(CSVConst.COMMA);							
				writer.append(CSVConst.NEWLINE);        
	        	
	         }
		 }
	
	}

}
