package oog.ui.perspective;

import oog.ui.utils.ASTUtils;
import oog.ui.views.AbstractObjectTreeView;
import oog.ui.views.OOGREView;
import oog.ui.views.ObjectTreeView;
import oog.ui.views.SummaryView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class RuntimePerspective implements IPerspectiveFactory {

	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(IPageLayout layout) {
		IWorkbenchPage activePage = ASTUtils.getActivePage();
		String editorArea = layout.getEditorArea();
		addFastViews(layout);
		addViewShortcuts(layout);
		addPerspectiveShortcuts(layout);
		try {

			{
				IFolderLayout folderLayout = layout.createFolder("folder",
						IPageLayout.LEFT, 0.2f, IPageLayout.ID_EDITOR_AREA);
//				folderLayout.addView(AbstractObjectTreeView.ID);
				folderLayout.addView(ObjectTreeView.ID);
				folderLayout.addView("org.eclipse.jdt.ui.PackageExplorer");

				
				// XXX. Add a check here before doing a cast.
				if(activePage!=null){
					AbstractObjectTreeView showView = (AbstractObjectTreeView)activePage.showView(AbstractObjectTreeView.ID);
					if (showView != null)
						showView.refresh();
				}



			}
			{

				IFolderLayout folderLayout = layout.createFolder("folder_1",
						IPageLayout.BOTTOM, 0.7f, IPageLayout.ID_EDITOR_AREA);

				folderLayout.addView("oog.ui.views.AbstractStackView");
				layout.addView(SummaryView.ID, IPageLayout.RIGHT,
						IPageLayout.DEFAULT_VIEW_RATIO, "folder_1");
				if(activePage!=null){
					SummaryView showView = (SummaryView) activePage.showView(SummaryView.ID);
					if (showView != null)
						showView.refresh();
				}
			}
			{
				IFolderLayout folderLayout = layout.createFolder("folder_2",
						IPageLayout.RIGHT, 0.64f, IPageLayout.ID_EDITOR_AREA);
				//folderLayout.addView("oog.ui.views.PartialOOG");
				folderLayout.addView(OOGREView.ID);
				folderLayout.addView("oog.ui.views.RelatedObjectsEdges");
			}
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add fast views to the perspective.
	 */
	private void addFastViews(IPageLayout layout) {
	}

	/**
	 * Add view shortcuts to the perspective.
	 */
	private void addViewShortcuts(IPageLayout layout) {
	}

	/**
	 * Add perspective shortcuts to the perspective.
	 */
	private void addPerspectiveShortcuts(IPageLayout layout) {
	}

}
