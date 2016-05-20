package oog.ui.tree.content.provider;

import java.util.HashSet;
import java.util.Set;

import oog.itf.IElement;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ast.BaseTraceability;

import edu.wayne.summary.traceability.ReverseTraceabilityMap;

public class ContentProviderRelatedElements  implements ITreeContentProvider{

	private String expression;

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput instanceof ASTNode){
			expression = newInput.toString();
		}
		
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof ASTNode){
			ReverseTraceabilityMap instance = ReverseTraceabilityMap.getInstance();
			Set<IElement> elements = instance.getElements((ASTNode)parentElement);
			if(elements!=null)
				return elements.toArray();
			else
			{
				ASTNode parent = ((ASTNode) parentElement).getParent();
				while(parent!=null){
					Set<IElement> elements2 = instance.getElements(parent);
					if(elements2!=null){
						return elements2.toArray();
					}
					parent = parent.getParent();
				}
			}
		}else if(parentElement instanceof IElement){
			Set<BaseTraceability> traceability = ((IElement) parentElement).getTraceability();
			
			//traceability = removeCurrentExpression(traceability);
			return traceability.toArray();
		}
		return new Object[0];
	}

	// XXX. Candidate for elimination
	private Set<BaseTraceability>  removeCurrentExpression(Set<BaseTraceability> traceability) {
		//HACKISH, shouldn't be comparing toString representation, could remove expressions that aren't actually the same
		Set<BaseTraceability> retSet = new HashSet<BaseTraceability>();
		for(BaseTraceability trace: traceability){
			if(trace.getExpression()!=null){
				if(expression.compareTo(trace.getExpression().toString()) != 0){
					retSet.add(trace);
				}
			}
		}
		return retSet;
		
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		// TODO Auto-generated method stub
		return getChildren(element).length>0;
	}

}
