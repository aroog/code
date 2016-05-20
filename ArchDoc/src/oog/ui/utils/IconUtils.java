package oog.ui.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import edu.wayne.summary.Crystal;
import edu.wayne.summary.strategies.Info;

public class IconUtils {
	public static final String IMG_PRIVATE_CO = "icons/private_co.gif";

	public static final String IMG_STATIC_CO = "icons/static_co.gif";

	public static final String IMG_ABSTRACT_CO = "icons/abstract_co.gif";
	
	public static final String IMG_PUBLIC_CO = "icons/public_co.gif";

	public static final String IMG_PROTECTED_CO = "icons/protected_co.gif";
	public static final String IMG_REFRESH = "icons/refresh_tab.gif";
	public static final String IMG_EXPANDALL = "icons/expandall.gif";
	public static final String IMG_EDGE_BASE = "icons/edge-base.gif";
	public static final String IMG_OBJECT_BASE = "icons/object-base.gif";
	
	//TODO: need a suitable picture for mark button.
	public static final String IMG_MARK = "icons/sample.gif";
	
	public static ImageDescriptor getImageDescriptor(String path){
		Bundle bundle = FrameworkUtil.getBundle(IconUtils.class);
	    URL url = FileLocator.find(bundle, new Path(path), null);
	    if(url!=null){
	    	return ImageDescriptor.createFromURL(url);
	    }
	    return null;
	}

	public static ImageDescriptor getPlatformImageDescriptor(String symbolicName){
		return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(symbolicName);
	}

	public static Image getInfoIcon(Object element) {
		if(element instanceof Info<?>){
			switch(((Info) element).getType()){
			case CLASS:
				ITypeBinding typeBinding = Crystal.getInstance().getTypeBindingFromName((((Info) element).getKey()));
				if(typeBinding!=null){
					List<ImageDescriptor> overlayIcons = new ArrayList<ImageDescriptor>();
					int typeModifiers = typeBinding.getModifiers();
					if(Modifier.isAbstract(typeModifiers)){
						overlayIcons.add(getImageDescriptor(IMG_ABSTRACT_CO));
						
					}else if(Modifier.isStatic(typeModifiers)){
						overlayIcons.add(getImageDescriptor(IMG_STATIC_CO));
					}else if(Modifier.isPrivate(typeModifiers)){
						overlayIcons.add(getImageDescriptor(IMG_PRIVATE_CO));
					}
					Image baseImage = JavaPluginImages.get(ISharedImages.IMG_OBJS_CLASS);
					
					DecorationOverlayIcon image = new DecorationOverlayIcon(baseImage, overlayIcons.toArray(new ImageDescriptor[0]));
					
					return image.createImage();
				}
				break;
			case METHOD:
				MethodDeclaration methodDeclaration = ASTUtils.getMethodDeclaration(((Info) element).getKey());
				if(methodDeclaration!=null){
					String icon= IMG_PUBLIC_CO;
					if(methodDeclaration!=null){
						int modifiers = methodDeclaration.getModifiers();
						if(Modifier.isPrivate(modifiers)){
							icon = IMG_PRIVATE_CO;
						}else if(Modifier.isProtected(modifiers)){
							icon = IMG_PROTECTED_CO;
						}else{
							
						}
						
					}
					
					ImageDescriptor imageDescriptor = getImageDescriptor(icon);
						
					
					return imageDescriptor.createImage();
				}
				break;
				
			}
		}
		return null;
	}



}
