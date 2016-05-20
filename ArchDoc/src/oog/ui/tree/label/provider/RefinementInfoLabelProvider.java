package oog.ui.tree.label.provider;

import oog.re.Heuristic;
import oog.re.IHeuristic;
import oog.re.IOtherRefinement;
import oog.re.Refinement;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

// TODO: Rename: LabelProviderRefinement
public class RefinementInfoLabelProvider extends LabelProvider implements ITableLabelProvider {

	private static final int OP_STATE = /*6;*/ 5;
	private static final int DST_DOM = /*5;*/ 4;
	private static final int DST_TYPE = /*4;*/ 3;
	// private static final int DST_INSTANCE = 3;
	private static final int SRC_TYPE = /*2;*/ 2;
	// private static final int SRC_INSTANCE = 1;
	private static final int OP_TYPE = 1;
	private static final int OP_NUM = 0;

	@Override
	public String getText(Object element) {
		return element.toString();
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		
		String text = "";
		if(element instanceof Refinement)
		{
			Refinement refinement = (Refinement) element;
			switch(columnIndex){
			case OP_NUM:
				text = refinement.getRefID();
				break;
			case OP_TYPE:
				text = refinement.getClass().getSimpleName();
				break;
			case DST_DOM:
				text = refinement.getDomainName();
				break;
			/*case DST_INSTANCE:
				text = ""; // TODO: Expose me? refinement.getDstIObject().getInstanceDisplayName();
				break;*/
			case DST_TYPE:
				text = refinement.getDstObject();
				break;
			/*case SRC_INSTANCE:
				text = ""; // TODO: Expose me? refinement.getSrcIObject().getInstanceDisplayName();
				break;*/
			case SRC_TYPE:
				text = refinement.getSrcObject();
				break;
			case OP_STATE:
				text = refinement.getState().toString();
			}
			
		}
		else if(element instanceof IOtherRefinement)
		{
			IOtherRefinement refinement = (IOtherRefinement) element;
			switch(columnIndex){
			case OP_TYPE:
				text = refinement.getClass().getSimpleName();
				break;
			case DST_DOM:
				text = refinement.getDstDomain();
				break;
			/*case DST_INSTANCE:
				text = refinement.getSrcObject();
				break;*/
			default:
				break;
			}
		}
		else if (element instanceof IHeuristic ) {
			IHeuristic heuristic = (IHeuristic) element;
			switch(columnIndex){
			case OP_NUM:
				text = "";
				if (heuristic instanceof Heuristic)
					text = ((Heuristic) heuristic).getRefID();
				break;
			case OP_TYPE:
				text = heuristic.getClass().getSimpleName();
				break;
			case DST_DOM:
				text = heuristic.getDomainName();
				break;
			/*case DST_INSTANCE:
				text = ""; // TODO: Expose me?
				break;*/
			case DST_TYPE:
				text = heuristic.getDstObject();
				break;
			/*case SRC_INSTANCE:
				text = ""; // TODO: Expose me?
				break;*/
			case SRC_TYPE:
				text = heuristic.getSrcObject();
				break;
			case OP_STATE:
				text = heuristic.getState().toString();
				break;
			default:
				text = heuristic.toDisplayName();
				break;
			}
		}
		return text;
	}

}
