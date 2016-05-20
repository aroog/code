package oog.ui.tree.content.provider;

import oog.itf.IEdge;
import oog.ui.content.wrappers.EdgeInfoWrapper;
import oog.ui.content.wrappers.InfoWrapper;
import oog.ui.content.wrappers.TraceabilityInfoWrapper;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import ast.BaseTraceability;

public class ContentProviderTraceInfo implements IStructuredContentProvider {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

	@Override
	public Object[] getElements(Object inputElement) {
		
		if (inputElement instanceof BaseTraceability) {
			InfoWrapper info = new TraceabilityInfoWrapper((BaseTraceability)inputElement);
			return info.toArray();
		}else if(inputElement instanceof IEdge){
			InfoWrapper info = new EdgeInfoWrapper((IEdge) inputElement);
			return info.toArray();
			
		}
		return new Object[]{};
	}

}
