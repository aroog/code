package oog.ui;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.cmu.cs.viewer.objectgraphs.VisualReportOptions;

public class ArchDocPreferencePage
	extends PreferencePage
	implements IWorkbenchPreferencePage {

	private Button buttonTopLevelObject;
	
	private Button btnShowLinks;
	
	private Button btnShowPrivateDomains;
	
	private Button buttonBiDirectionalArrows;
	
	private Button buttonDOT;

	private Button buttonFDP;

	private Button buttonLR;

	private Button buttonTB;	
	
	private VisualReportOptions options = VisualReportOptions.getInstance();

	private Button buttonCFEdge;

	private Button buttonCREdge;

	private Button buttonDFEdge;

	private Button buttonPTEdge;
	
	/*
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {

		Composite compositeLeft = new Composite(parent, SWT.NULL);

		//Create a data that takes up the extra space in the dialog .
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		compositeLeft.setLayoutData(data);

		GridLayout layout = new GridLayout();
		compositeLeft.setLayout(layout);			
				
		GridData gdButtonTopLevelObject = new org.eclipse.swt.layout.GridData();
		gdButtonTopLevelObject.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdButtonTopLevelObject.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdButtonTopLevelObject.horizontalSpan = 1;

		buttonTopLevelObject = new Button(compositeLeft, SWT.CHECK);
		buttonTopLevelObject.setSelection(options.isShowTopLevelObject());
		buttonTopLevelObject.setText("Top-Level Object");
		buttonTopLevelObject.setLayoutData(gdButtonTopLevelObject);
		buttonTopLevelObject.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		
		GridData gdLabelShowDomainLinks = new org.eclipse.swt.layout.GridData();
		gdLabelShowDomainLinks.horizontalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
		gdLabelShowDomainLinks.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdLabelShowDomainLinks.horizontalSpan = 3;

		btnShowLinks = new Button(compositeLeft, SWT.CHECK);
		btnShowLinks.setText("Domain Links");
		btnShowLinks.setSelection(options.isShowDomainLinks());
		btnShowLinks.setLayoutData(gdLabelShowDomainLinks);
		btnShowLinks.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});		
	
		GridData gdLabelShowPrivateDomains = new org.eclipse.swt.layout.GridData();
		gdLabelShowPrivateDomains.horizontalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
		gdLabelShowPrivateDomains.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdLabelShowPrivateDomains.horizontalSpan = 3;

		btnShowPrivateDomains = new Button(compositeLeft, SWT.CHECK);
		btnShowPrivateDomains.setText("Private Domains");
		btnShowPrivateDomains.setSelection(options.isShowPrivateDomains());
		btnShowPrivateDomains.setLayoutData(gdLabelShowPrivateDomains);
		btnShowPrivateDomains.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		

		GridData gdButtonBiDirectionalArrows = new org.eclipse.swt.layout.GridData();
		gdButtonBiDirectionalArrows.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdButtonBiDirectionalArrows.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdButtonBiDirectionalArrows.horizontalSpan = 1;

		buttonBiDirectionalArrows = new Button(compositeLeft, SWT.CHECK);
		buttonBiDirectionalArrows.setSelection(options.isUseBiDirectionalArrows());
		buttonBiDirectionalArrows.setText("Bi-Directional Edges");
		buttonBiDirectionalArrows.setLayoutData(gdButtonBiDirectionalArrows);
		buttonBiDirectionalArrows.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		
		GridData gdLabelLayout = new org.eclipse.swt.layout.GridData();
		gdLabelLayout.horizontalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
		gdLabelLayout.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdLabelLayout.horizontalSpan = 1;

		Label labelLayout = new Label(compositeLeft, SWT.NONE);
		labelLayout.setText("Graph Layout:");
		labelLayout.setLayoutData(gdLabelLayout);

		GridData gdLabelRadio = new org.eclipse.swt.layout.GridData();
		gdLabelRadio.horizontalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
		gdLabelRadio.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdLabelRadio.horizontalSpan = 2;

		Composite compositeRadio = new Composite(compositeLeft, SWT.NULL);
		compositeRadio.setLayoutData(gdLabelRadio);
		compositeRadio.setLayout(new RowLayout());

		buttonDOT = new Button(compositeRadio, SWT.RADIO);
		buttonDOT.setText("DOT");
		buttonDOT.setSelection(!options.isUseFDP());
		buttonDOT.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		buttonFDP = new Button(compositeRadio, SWT.RADIO);
		buttonFDP.setText("FDP");
		buttonFDP.setSelection(options.isUseFDP());
		buttonFDP.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});

		GridData gdLabelLayoutOption = new org.eclipse.swt.layout.GridData();
		gdLabelLayoutOption.horizontalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
		gdLabelLayoutOption.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdLabelLayoutOption.horizontalSpan = 1;

		Label labelLayoutOption = new Label(compositeLeft, SWT.NONE);
		labelLayoutOption.setText("Layout Option:");
		labelLayoutOption.setLayoutData(gdLabelLayoutOption);

		Composite compositeRadio2 = new Composite(compositeLeft, SWT.NULL);
		compositeRadio2.setLayout(new RowLayout());

		buttonTB = new Button(compositeRadio2, SWT.RADIO);
		buttonTB.setText("TB");
		buttonTB.setSelection(!options.isLayoutLR());
		buttonTB.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		buttonLR = new Button(compositeRadio2, SWT.RADIO);
		buttonLR.setText("LR");
		buttonLR.setSelection(options.isLayoutLR());
		buttonLR.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});		
		
		
		GridData gdLabelGraphView = new org.eclipse.swt.layout.GridData();
		gdLabelLayoutOption.horizontalAlignment = org.eclipse.swt.layout.GridData.BEGINNING;
		gdLabelLayoutOption.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gdLabelLayoutOption.horizontalSpan = 1;

		Label labelGraphView = new Label(compositeLeft, SWT.NONE);
		labelGraphView.setText("Graph View");
		labelGraphView.setLayoutData(gdLabelGraphView);
		
		
		Composite compositeRadioEdgeType = new Composite(compositeLeft, SWT.NULL);
		compositeRadioEdgeType.setLayout(new RowLayout());

		buttonPTEdge = new Button(compositeRadioEdgeType, SWT.CHECK);
		buttonPTEdge.setText("Points-to");
		buttonPTEdge.setSelection(options.isShowReferenceEdges());
		buttonPTEdge.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		buttonDFEdge = new Button(compositeRadioEdgeType, SWT.CHECK);
		buttonDFEdge.setText("Dataflow");
		buttonDFEdge.setSelection(options.isShowUsageEdges());
		buttonDFEdge.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});		

		buttonCREdge = new Button(compositeRadioEdgeType, SWT.CHECK);
		buttonCREdge.setText("Creation");
		buttonCREdge.setSelection(options.isShowCreationEdges());
		buttonCREdge.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});		
		buttonCFEdge = new Button(compositeRadioEdgeType, SWT.CHECK);
		buttonCFEdge.setText("Control Flow");
		//buttonCFEdge.setSelection(options.isShowControlFlowEdges());
		buttonCFEdge.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});		
		
		
		return compositeLeft;
	}

	/*
	 * @see IWorkbenchPreferencePage#init(IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		//Initialize the preference store we wish to use
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	/**
	 * Performs special processing when this page's Restore Defaults button has been pressed.
	 * Sets the contents of the nameEntry field to
	 * be the default 
	 */
	protected void performDefaults() {
	}
	/** 
	 * Method declared on IPreferencePage. Save the
	 * author name to the preference store.
	 */
	public boolean performOk() {
		options.setShowTopLevelObject(buttonTopLevelObject.getSelection());
		options.setShowDomainLinks(btnShowLinks.getSelection());
		options.setShowPrivateDomains(btnShowPrivateDomains.getSelection());		
		options.setUseFDP(buttonFDP.getSelection());
		options.setLayoutLR(buttonLR.getSelection());
		options.setUseBiDirectionalArrows(buttonBiDirectionalArrows.getSelection());
		options.setShowReferenceEdges(buttonPTEdge.getSelection());
		options.setShowCreationEdges(buttonCREdge.getSelection());
		options.setShowUsageEdges(buttonDFEdge.getSelection());
		
		return super.performOk();
		
	}

	

}