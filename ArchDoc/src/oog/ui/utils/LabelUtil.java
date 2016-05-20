package oog.ui.utils;

import org.eclipse.jdt.core.Signature;

import ast.AstNode;
import ast.BaseTraceability;
import ast.BodyDeclaration;
import ast.FieldDeclaration;
import ast.MethodDeclaration;
import ast.MiniAstUtils;
import ast.TypeDeclaration;
import ast.VariableDeclaration;

import oog.itf.IEdge;
import oog.itf.IObject;
import oog.ui.content.wrappers.TraceabilityNode;
import util.TraceabilityEntry;
import util.TraceabilityList;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayDomain;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayEdge;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;
import edu.wayne.ograph.ODFEdge;
import edu.wayne.ograph.OPTEdge;
import edu.wayne.summary.strategies.Info;

public class LabelUtil {
	public static String getExtraLabel(IEdge iEdge) {
		String label = "";
		if (iEdge instanceof ODFEdge) {
			label = ((ODFEdge) iEdge).getFlow()
					.getInstanceDisplayName();
		} else if (iEdge instanceof OPTEdge) {
			label = ((OPTEdge) iEdge).getFieldName();
		}
		return label;
	}
	public static String getEdgeLabel(IEdge element) {
		String label= "";
		if(element!=null){
		IEdge iEdge = (IEdge) element;
		label = iEdge.getClass().getSimpleName() + " "
				+ iEdge.getOsrc().getInstanceDisplayName() + "->"
				+ iEdge.getOdst().getInstanceDisplayName() + " ["
				+ LabelUtil.getExtraLabel(iEdge) + "]";
		}
		return label;
	}
	public static String getDisplayObjectLabel(DisplayObject element) {
		return element.getInstanceDisplayName() + " : "+ Signature.getSimpleName(element.getTypeDisplayName());
	}
	
	
	public static String getIObjectLabel(IObject object){
		return object.getInstanceDisplayName() + " : "+ Signature.getSimpleName(object.getTypeDisplayName());
	}
	public static String getAbstractStackLabel(Object element) {
		String label = "";
		if (element != null) {
			label = element.toString();
			if (element instanceof TraceabilityList) {
				BaseTraceability last = ((TraceabilityList) element)
						.getLast();
				if (last != null) {
					label = last.toString();
	
					AstNode expression = last.getExpression();
					if (expression != null) {
						label = expression.toString();
					}
				}
			} else if (element instanceof TraceabilityNode) {
	
				// XXX. Check for nulls here... BAD
				TraceabilityEntry entry = ((TraceabilityNode) element).getData();
				BaseTraceability second = entry.getSecond();
				TypeDeclaration enclosingType = MiniAstUtils.getEnclosingTypeDeclaration(second.getExpression());
				label = Signature.getSimpleName(enclosingType.getFullyQualifiedName())
						+ ".";
				BodyDeclaration enclosingDeclaration = second.getExpression().enclosingDeclaration;
				if (enclosingDeclaration instanceof MethodDeclaration) {
					MethodDeclaration methodDeclaration = (MethodDeclaration) enclosingDeclaration;
					label += methodDeclaration.methodName;
				} else if (enclosingDeclaration instanceof FieldDeclaration) {
					label += ((FieldDeclaration) enclosingDeclaration).fieldName;
				} else if (enclosingDeclaration instanceof TypeDeclaration) {
					label += ((TypeDeclaration) enclosingDeclaration)
							.getFullyQualifiedName();
				} else if (enclosingDeclaration instanceof VariableDeclaration) {
					label += ((VariableDeclaration) enclosingDeclaration).varName;
				}
	
				if (second != null) {
					AstNode expression = second.getExpression();
					if (expression != null) {
						label += "." + expression.toString();
					}
				}
	
			}
	
		}
		return label;
	}
	public static String getDisplayElementLabel(Object element) {
		String label = "";
	
		if (element instanceof DisplayDomain) {
			DisplayDomain treeElement = (DisplayDomain) element;
			label = treeElement.getId();
		}else if(element instanceof DisplayObject){
			DisplayObject treeElement = (DisplayObject)element;
			label = treeElement.getInstanceDisplayName();
		}else if(element instanceof DisplayEdge){
			label = element.toString();
		}else{
			label = element!=null? element.toString(): label;
		}
	
		return label;
	}
	public static String getRelatedObjectsTreeLabel(Object element) {
		String label = "";
		if (element instanceof IObject) {
			label = ((IObject) element).getInstanceDisplayName();
		} else if (element instanceof BaseTraceability) {
	
			AstNode expression = ((BaseTraceability) element).getExpression();
			if (expression != null)
				label = expression.toString();
		} else if (element instanceof IEdge) {
			label = getEdgeLabel((IEdge) element);
		} else {
			label = element.toString();
		}
		return label;
	}
	public static String getSummaryViewLabel(Object element) {
		String label ="";
		if(element instanceof Info<?>){
			switch(((Info) element).getType()){
			case CLASS:
				label =  Signature.getSimpleName(((Info) element).getKey());
				break;
			case METHOD:
				label =  ((Info) element).getKey();
				break;
				
			}
		}else{
			label = element!=null? element.toString(): label;
		}
		return label;
	}
}
