package edu.cmu.cs.viewer.objectgraphs.views;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class OpenFile {
    /**
     * This is used to open a file in the eclipse editor.
     * @param filename
     * @param lineNumber
     * @return
     */
    public IEditorPart openFile(Shell parent, String filename, Integer lineNumber )
    {
    	IEditorDescriptor edDesc = null;
    	IEditorInput input = null;
    	IEditorPart part = null;

    	filename = filename.replace('\\', '/');
    	try
		{
			edDesc = org.eclipse.ui.ide.IDE.getEditorDescriptor(filename);
		} catch (PartInitException pie) {
			edDesc = null;
		}

		File file = new File(filename);
		if (file.exists())
		{

			if (edDesc == null)
			{
				MessageDialog.openConfirm(parent,"LogViewer", "No Description of this file");
				return null; // the user didn't want to open the file, no sense in retrieving it
			}
			if (!edDesc.isInternal() &&
				!MessageDialog.openConfirm(parent,"LogViewer", "There is no default editor installed that can be used to open this file, do you want to open it as a simple text file instead?"))
			{
				return null; // the user didn't want to open the file, no sense in retrieving it
			}

			IFile workspaceFile= getWorkspaceFile(file);
			input = createEditorInput(file);

			if (input != null)
			{
				try
				{
					IMarker marker = null;
					if( workspaceFile != null )
					{
						marker = workspaceFile.createMarker(IMarker.LINE_NUMBER);
						if( lineNumber != null )
						{
							marker.setAttribute(IMarker.LINE_NUMBER, lineNumber.intValue());
						}
					}
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

					if (edDesc.isInternal())
					{
						//part = page.openEditor(input, edDesc.getId());
						part = org.eclipse.ui.ide.IDE.openEditor(page, input, edDesc.getId());

					} else {
						part = org.eclipse.ui.ide.IDE.openEditor(page, input, "org.eclipse.ui.DefaultTextEditor");
					}

					if( part != null && marker != null )
					{
						org.eclipse.ui.ide.IDE.gotoMarker(part, marker);
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return part;
    }

    private IEditorInput createEditorInput(File file)
    {
    	IFile workspaceFile= getWorkspaceFile(file);
		if (workspaceFile != null)
		{
			return new FileEditorInput(workspaceFile);
		}
		// return new JavaFileEditorInput(file);
		return null;
	}

    private IFile getWorkspaceFile(File file)
    {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IPath location= Path.fromOSString(file.getAbsolutePath());
		IFile[] files= workspace.getRoot().findFilesForLocation(location);

		if (files == null || files.length == 0)
			return null;
		if (files.length == 1)
		{
			return files[0];
		}
		return null;
	}

}
