package oog.ui.tree;

import java.util.ArrayList;

import oog.re.RefinementModel;
import oog.ui.content.wrappers.OGraphInfoWrapper.TopLevel;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ContentProviderRefinements implements IStructuredContentProvider {
	
	private static final Object[] EMPTY_ARRAY = new Object[0];
	
	private RefinementModel refinementModel;
	
	public ContentProviderRefinements() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof RefinementModel) {
			this.refinementModel = (RefinementModel) newInput;
		}		
	}

	@Override
    public Object[] getElements(Object inputElement) {
		Object[] array = ContentProviderRefinements.EMPTY_ARRAY;

		if ( refinementModel != null ) {
			ArrayList<Object> o = new ArrayList<Object>();
			o.addAll(refinementModel.getRefinements());
			o.addAll(refinementModel.getOtherRefinements());
			o.addAll(refinementModel.getHeuristics());
			array = o.toArray();
		}
	    return array;
    }
}
