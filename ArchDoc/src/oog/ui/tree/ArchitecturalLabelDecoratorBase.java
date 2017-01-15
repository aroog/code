package oog.ui.tree;

import oog.itf.IDomain;
import oog.itf.IObject;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

// TODO: All these fonts/colors gotta be disposed. 
public class ArchitecturalLabelDecoratorBase implements IColorProvider, IFontProvider {

	private static FontRegistry fontCache = new FontRegistry();

	private static ColorRegistry colorCache = new ColorRegistry();

	private static FontData[] baseData;

	static {
		colorCache.put("PublicDomainColor", Display.getCurrent().getSystemColor(SWT.COLOR_BLUE).getRGB());
		colorCache.put("PrivateDomainColor", Display.getCurrent().getSystemColor(SWT.COLOR_RED).getRGB());
		colorCache.put("ParamDomainColor", Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN).getRGB());

		// XXX. Remove this.
		colorCache.put("HiddenObjectColor", Display.getCurrent().getSystemColor(SWT.COLOR_GRAY).getRGB());

		ArchitecturalLabelDecoratorBase.baseData = ArchitecturalLabelDecoratorBase.fontCache.getFontData("Default");

		ArchitecturalLabelDecoratorBase.fontCache.put("Italic", ArchitecturalLabelDecoratorBase.getModifiedFontData(SWT.ITALIC));
		ArchitecturalLabelDecoratorBase.fontCache.put("Bold", ArchitecturalLabelDecoratorBase.getModifiedFontData(SWT.BOLD));
		ArchitecturalLabelDecoratorBase.fontCache.put("BoldItalic", ArchitecturalLabelDecoratorBase.getModifiedFontData(SWT.BOLD | SWT.ITALIC));
	}

	public ArchitecturalLabelDecoratorBase() {
		// Populate the FontRegistry with values for Italic, Bold, etc...
		// FontData[] fds = Display.getCurrent().getFontList("Tahoma", true);
		// if ( fds != null && fds.length > 0 )
		// {
		// FontData fd = fds[0];
		// fd.setStyle(SWT.ITALIC);
		// fd.setHeight( 8);
		//
		// fontCache.put("CrossLinkFont", fds);
		// }
		//      
		// fontCache.put("Italic", getModifiedFontData(SWT.ITALIC) );
		// fontCache.put("Bold", getModifiedFontData(SWT.BOLD) );
		// fontCache.put("BoldItalic", getModifiedFontData(SWT.BOLD | SWT.ITALIC) );

		// FontData[] uData = getModifiedFontData(0);
		// underlineFontData(uData);
		// fontCache.put("Underline", uData );
	}

	protected Image createImage(Image baseImage, Object syncModelElement) {
		if (baseImage == null) {
			return baseImage;
		}
		ImageDescriptor[] lowerLeft = getLowerLeftOverlays(syncModelElement);
		ImageDescriptor[] upperRight = getUpperRightOverlays(syncModelElement);
		ImageDescriptor[] lowerRight = getLowerRightOverlays(syncModelElement);
		ImageDescriptor[] upperLeft = getUpperLeftOverlays(syncModelElement);
		ImageOverlayIcon compDesc = new ImageOverlayIcon(baseImage, new ImageDescriptor[][] { upperRight, lowerRight, lowerLeft, upperLeft });
		return compDesc.createImage();
	}

	protected ImageDescriptor[] getLowerLeftOverlays(Object syncModelElement) {
		return null;
	}

	protected ImageDescriptor[] getUpperRightOverlays(Object syncModelElement) {

		return null;
	}

	protected ImageDescriptor[] getLowerRightOverlays(Object syncModelElement) {
		return null;
	}

	protected ImageDescriptor[] getUpperLeftOverlays(Object syncModelElement) {
		return null;
	}

	protected String decorateLabel(Object syncModelElement) {
		StringBuffer builder = new StringBuffer("");

		return builder.toString();
	}

	private static FontData[] getModifiedFontData(int style) {
		FontData[] styleData = new FontData[ArchitecturalLabelDecoratorBase.baseData.length];
		for (int i = 0; i < styleData.length; i++) {
			FontData base = ArchitecturalLabelDecoratorBase.baseData[i];
			styleData[i] = new FontData(base.getName(), base.getHeight(), base.getStyle() | style);
		}
		return styleData;
	}

	protected void underlineFontData(FontData[] styleData) {
		for (int i = 0; i < styleData.length; i++) {
			styleData[i].data.lfUnderline = Byte.MAX_VALUE;
		}
	}

	public Image decorateImage(Image baseImage, Object element) {
		Image image = null;
		if (baseImage != null) {
		}
		return image;
	}

	protected String getKey(Object modelElement) {
		StringBuffer builder = new StringBuffer();

		return builder.toString();
	}

	public String decorateText(String text, Object element) {
		// Currently, not decorating text
		return null;
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	// TODO: Don't see the point of coloring domains...
	public Color getForeground(Object element) {
		Color color = null;

		if (element instanceof IDomain) {
			IDomain displayDomain = (IDomain) element;
			if (!displayDomain.isPublic()) { // This is the more common case
				color = colorCache.get("PrivateDomainColor");
			}
			// XXX. Extract constants
			else if (displayDomain.getD().equals("owner")) {
				color = colorCache.get("ParamDomainColor");
			}
			else
				color = colorCache.get("PublicDomainColor");
		}
		else if (element instanceof IObject) {
			IObject displayObject = (IObject) element;
			
			
	/*		if (!displayObject.isVisible()) {
				color = colorCache.get("HiddenObjectColor");
			}*/
		}

		// Always use default.
		return color;
	}

	public Color getBackground(Object element) {
		// Always use default.
		return null;
	}

	public Font getFont(Object element) {
		Font font = null;
		if (element instanceof IObject) {
			IObject displayObject = (IObject) element;
			if(displayObject.isMainObject()){
				font = fontCache.get("Bold");
			}
		}

		return font;
	}
}
