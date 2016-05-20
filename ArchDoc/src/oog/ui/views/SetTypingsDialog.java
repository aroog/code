package oog.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import oog.re.RankedTypings;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * XXX. Gotta implement proper Cancel behavior; right now, modifying the real object
 * XXX. Need to add buttons to re-order typings:
 * - Move up
 * - Move to top
 * - Move down
 * - Move to bottom
 * XXX. Maybe do not allow removing just re-ordering...
 * XXX. Add trace to code... 
 */
public class SetTypingsDialog extends TitleAreaDialog   {
	
    private Button buttonRemove;
    
	private ListViewer lvTypings;

	private ListViewer lvAUs;

	private Map<String, ArrayList<String>> rankedTyping;

	
    public SetTypingsDialog (Shell shell) {
        super (shell);
        setHelpAvailable(false);
    }

	@Override
    public int getShellStyle() {
		return (SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE | SWT.MAX);
	}
	
	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("OOGRE: More Information Needed");
	}

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
     */
    protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		SashForm sashForm2 = new SashForm(composite, SWT.HORIZONTAL);
		sashForm2.setLayout(new GridLayout());
		sashForm2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Right-composite
		GridLayout glRight = new GridLayout();
		glRight.numColumns = 1;
		
		Composite compositeRight =  new Composite(sashForm2, SWT.NULL);
		compositeRight.setSize(new org.eclipse.swt.graphics.Point(479,300));
		compositeRight.setLayout(glRight);
		
		GridData gdLabelAUs = new org.eclipse.swt.layout.GridData();
		gdLabelAUs.horizontalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
		gdLabelAUs.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdLabelAUs.horizontalSpan = 2;

		Label labelAUs= new Label(compositeRight, SWT.NONE);
		labelAUs.setText("AUs:");
		labelAUs.setLayoutData(gdLabelAUs);
		
		GridData gdListAUs = new org.eclipse.swt.layout.GridData();
		gdListAUs.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdListAUs.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdListAUs.grabExcessHorizontalSpace = true;
		gdListAUs.grabExcessVerticalSpace = true;
		gdListAUs.minimumHeight = 200;

		lvAUs = new ListViewer(compositeRight);
		lvAUs.getControl().setLayoutData(gdListAUs);
		lvAUs.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				ArrayList v = (ArrayList) inputElement;
				return v.toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				lvAUs.refresh(true);
			}
		});

		lvAUs.setLabelProvider(new LabelProvider() {
			public org.eclipse.swt.graphics.Image getImage(Object element) {
				return null;
			}

			public String getText(Object element) {
				return (String) element;
			}
		});

		lvAUs.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
		        // Populate the list
		        lvTypings.setInput(getTypings(getSelectedAU()));

				updateButtons();
			}
		});

		lvAUs.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return true;
			}
		});

		lvAUs.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((String) e1).compareTo(((String) e2));
			}

		});		

		GridData gdLeft = new GridData(SWT.FILL, SWT.FILL, true, true);
		gdLeft.widthHint = 479;
		
		Composite compositeLeft = new Composite(sashForm2, SWT.NULL);
		compositeLeft.setLayoutData(gdLeft);

		// create the desired layout for this wizard page
		GridLayout glLeft = new GridLayout();
		glLeft.numColumns = 1;
		compositeLeft.setLayout(glLeft);

		GridData gdLabelLayout = new org.eclipse.swt.layout.GridData();
		gdLabelLayout.horizontalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
		gdLabelLayout.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdLabelLayout.horizontalSpan = 2;

		Label labelLayout= new Label(compositeLeft, SWT.NONE);
		labelLayout.setText("Ranked Typings:");
		labelLayout.setLayoutData(gdLabelLayout);
		
		GridData gdListTypings = new org.eclipse.swt.layout.GridData();
		gdListTypings.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdListTypings.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdListTypings.grabExcessHorizontalSpace = true;
		gdListTypings.grabExcessVerticalSpace = true;
		gdListTypings.minimumHeight = 200;

		lvTypings = new ListViewer(compositeLeft);
		lvTypings.getControl().setLayoutData(gdListTypings);
		lvTypings.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				ArrayList v = (ArrayList) inputElement;
				return v.toArray();
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				lvTypings.refresh(true);
			}
		});

		lvTypings.setLabelProvider(new LabelProvider() {
			public org.eclipse.swt.graphics.Image getImage(Object element) {
				return null;
			}

			public String getText(Object element) {
				return (String) element;
			}
		});

		lvTypings.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});

		lvTypings.addFilter(new ViewerFilter() {
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				return true;
			}
		});

		lvTypings.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				return ((String) e1).compareTo(((String) e2));
			}

		});

		GridData gdButton = new org.eclipse.swt.layout.GridData();
		gdButton.horizontalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
		gdButton.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;

        buttonRemove = new Button (compositeLeft, SWT.PUSH);
        buttonRemove.setText ("Remove");
        buttonRemove.setLayoutData(gdButton);
        buttonRemove.addSelectionListener (new SelectionAdapter () {
            @Override
			public void widgetSelected(SelectionEvent e) {

				String au = getSelectedAU();
				if (au != null) {
					ArrayList<String> typings = rankedTyping.get(au);
					// XXX. Here, we're modifying the real object.
					IStructuredSelection selection = (IStructuredSelection) lvTypings.getSelection();
					final Iterator it = selection.iterator();
					while (it.hasNext()) {
						typings.remove(it.next());
					}
					lvTypings.refresh(true);
				}
			}

        });
        
		sashForm2.setWeights(new int[] { 20, 80 });
		
        setTitle("Set the preferred typings");
		setMessage("For the selected AU, re-order the preferring typings, or remove unwanted ones.") ;

		// Update the display
		updateDisplay();
		
        updateButtons();
		
        return parent;
	}
    
    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        // Note: Call the superclass version after you do the processin, to avoid "Widget Disposed" exceptions!
		super.okPressed();
    }

    @Override
    protected void cancelPressed() {
    	// Do nothing
        super.cancelPressed ();
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		Control c = super.createButtonBar(parent);
		return c;
	}

	private void updateButtons() {
	    buttonRemove.setEnabled(!lvTypings.getSelection().isEmpty());
    }
	
	public void populate(RankedTypings rankedTypings) {
		rankedTyping = rankedTypings.getRankedTypings();
	}
	
	private void updateDisplay() {
		String first = "";
		
		int ii = 0;
		for(Entry<String, ArrayList<String>> entry : rankedTyping.entrySet() ) {
			if (ii == 0 ) {
				first = entry.getKey();
			}
			lvAUs.add(entry.getKey());
			
		}
		
		 IStructuredSelection structuredSelection = new StructuredSelection(first);
		 lvTypings.setSelection(structuredSelection);

	}

	private String getSelectedAU() {
    	String selectedAU = null;
    	IStructuredSelection selection = (IStructuredSelection) lvAUs.getSelection();
    	final Iterator it = selection.iterator();
    	while(it.hasNext()) {
    		selectedAU = (String) it.next();
    	}
    	return selectedAU;
    }

    private ArrayList<String> getTypings(String selectedAU) {
    	ArrayList<String> list = null;
    	if (selectedAU != null ) {
    		list = rankedTyping.get(selectedAU);
    	}
    	return list;
    }
}
