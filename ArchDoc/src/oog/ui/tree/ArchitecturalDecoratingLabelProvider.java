package oog.ui.tree;

import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.dialogs.FilteredTree;

public class ArchitecturalDecoratingLabelProvider extends DecoratingLabelProvider implements IColorProvider, IFontProvider {
	public ArchitecturalDecoratingLabelProvider(FilteredTree filterTree) {
		super(new LabelProviderObjectGraph(filterTree), new ArchitecturalLabelDecorator());
	}

	public void dispose() {
		super.dispose();

		ILabelDecorator labelDecorator = getLabelDecorator();
		if (labelDecorator != null) {
			labelDecorator.dispose();
		}

		ILabelProvider labelProvider = getLabelProvider();
		if (labelProvider != null) {
			labelProvider.dispose();
		}
	}

	public Color getForeground(Object element) {
		ILabelDecorator colorProvider = getLabelDecorator();
		if (colorProvider instanceof IColorProvider) {
			return ((IColorProvider) colorProvider).getForeground(element);
		}

		// Default
		return null;
	}

	public Color getBackground(Object element) {
		ILabelDecorator colorProvider = getLabelDecorator();
		if (colorProvider instanceof IColorProvider) {
			return ((IColorProvider) colorProvider).getBackground(element);
		}

		// Default
		return null;
	}

	public Font getFont(Object element) {
		ILabelDecorator fontProvider = getLabelDecorator();
		if (fontProvider instanceof IFontProvider) {
			return ((IFontProvider) fontProvider).getFont(element);
		}

		// Default
		return null;
	}

}
