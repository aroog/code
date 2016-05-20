package oog.ui.tree.displaygraph;

import oog.itf.IEdge;
import oog.itf.IObject;

import oog.ui.content.wrappers.DisplayGraphInfoWrapper.TopLevel;
import oog.ui.utils.IconUtils;
import oog.ui.utils.LabelUtil;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.dialogs.FilteredTree;

import edu.cmu.cs.viewer.objectgraphs.EdgeEnum;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayDomain;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayEdge;
import edu.cmu.cs.viewer.objectgraphs.displaygraph.DisplayObject;
import edu.wayne.ograph.ODomain;



// TODO: Consolidate all the image registries!!!
public class LabelProviderObjectGraph extends PreferenceBoldLabelProvider {
	// ImageRegistry takes care of disposing any image objects
	private static ImageRegistry imageCache = new ImageRegistry();

	public LabelProviderObjectGraph(FilteredTree filterTree) {
		super(filterTree);
	}

	public void dispose() {
		super.dispose();
	}

	/*
	 * @see ILabelProvider#getImage(Object)
	 */
	public Image getImage(Object element) {
		String key = element.getClass().getSimpleName();

		// obtain the cached image corresponding to the descriptor
		Image image = LabelProviderObjectGraph.imageCache.get(key);
		if (image == null) {
			ImageDescriptor imageDescriptor = IconUtils.getImageDescriptor("icons/" + key+".gif");
			

		    if(imageDescriptor == null) return null;
		    else{
				image = imageDescriptor.createImage();
				LabelProviderObjectGraph.imageCache.put(key, image);
			}
		}
		return image;
		

	}

	public String getText(Object element) {
		String label = "";

		if(element instanceof DisplayDomain){
			DisplayDomain treeElement = (DisplayDomain)element;
			String d = treeElement.getLabel();
			int lastIndexOf = d.lastIndexOf("::");
			if(lastIndexOf > -1){
				label = d.substring(lastIndexOf+2);
			}else{
				label= d;
			}
		}else if (element instanceof DisplayObject) {
			label = LabelUtil.getDisplayObjectLabel((DisplayObject)element);

		}else if(element instanceof DisplayEdge){
			StringBuilder builder = new StringBuilder();
			DisplayEdge edge = (DisplayEdge)element;
			EdgeEnum edgeType = edge.getEdgeType();
//			switch (edgeType) {
//			case CF:
//				break;
//			case PT:
//				break;
//			case DF:
//				break;
//			case UNKNOWN:
//				break;
//			}
			builder.append(edgeType.toString());
			builder.append(": ");
			
			builder.append(LabelUtil.getDisplayObjectLabel(edge.getFromObject()));
			builder.append(" -> ");
			builder.append(LabelUtil.getDisplayObjectLabel(edge.getToObject()));

			// XXX. TODO: Fix Edge.getLabel();
//			switch (edgeType) {
//			case CF:
//				break;
//			case PT:
//				builder.append("[");
//				builder.append(edge);
//				builder.append("]");
//				break;
//			case DF:
//				builder.append(" [");
//				builder.append(edge.getLabel());
//				builder.append("]");
//				break;
//			case UNKNOWN:
//				break;
//			}
			
			label = builder.toString();
		}else if(element instanceof TopLevel){
			label = ((TopLevel) element).getTitle();
		}
		

		return label;
	}

	protected RuntimeException unknownElement(Object element) {
		return new RuntimeException("Unknown type of element in tree of type " + element.getClass().getName());
	}
}
