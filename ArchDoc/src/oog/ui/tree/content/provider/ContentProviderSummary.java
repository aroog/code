package oog.ui.tree.content.provider;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;


public class ContentProviderSummary implements IStructuredContentProvider {
	private static Object[] EMPTY_ARRAY = new Object[0];


	public void dispose() {
	}


	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public Object[] getElements(Object inputElement) {
		Object[] ret = EMPTY_ARRAY;
		if(inputElement instanceof Set<?>){
			ret = ((Set<?>) inputElement).toArray();
		}else if (inputElement instanceof Collection) {
            ret = ((Collection) inputElement).toArray();
        }

		return ret;
	}



}
