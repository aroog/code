package oog.ui.views;


import oog.ui.utils.IconUtils;
import oog.ui.utils.LabelUtil;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import edu.wayne.summary.strategies.Info;
import edu.wayne.summary.strategies.MarkStatus;

//TOMAR: clean this up.
//- handle extended attributes on Info
//- extract some classes
//- add sorting
public class RankedTableViewer extends TableViewer {

	static int rank = 1;
	
	private Composite parent;

	public RankedTableViewer(Composite parent) {
		super(parent);
		this.parent = parent;
		createColumns();
	}
	
	public RankedTableViewer(Composite parent, int style) {
		super(parent, style);
		this.parent = parent;
		createColumns();
	}
	private void createColumns() {
		TableViewerColumn column = new TableViewerColumn(this, SWT.NONE);
		TableColumn columnRank = column.getColumn();

		columnRank.setText("#");
		columnRank.setWidth(10);
		column.setLabelProvider(new ColumnLabelProvider(){

			@Override
			public String getText(Object element) {
				return Integer.toString(rank++);
			}
			
		});
		
		TableViewerColumn column2 = new TableViewerColumn(this, SWT.NONE);

		TableColumn columnText = column2.getColumn();
		
		columnText.setWidth(100);
		column2.setLabelProvider(new ColumnLabelProvider(){

			@Override
			public String getText(Object element) {
				return LabelUtil.getSummaryViewLabel(element);
			}

			@Override
			public Image getImage(Object element) {
				Image infoIcon = IconUtils.getInfoIcon(element);
				if(infoIcon!=null){
					return infoIcon;
				}
				return super.getImage(element);

			}
			
		});
		TableViewerColumn column3 = new TableViewerColumn(this, SWT.NONE);
		
		TableColumn columnWeight = column3.getColumn();
		columnWeight.setText("Weight");
		column3.setLabelProvider(new ColumnLabelProvider(){

			@Override
			public String getText(Object element) {
				String label ="";
				if(element instanceof Info<?>){
					label = String.valueOf(((Info) element).getNumber());
				}
				return label;
			}
			
		});
		
		/*
		 * new column for marks. Currently all marks are empty after initialization
		 * of MICs because ASummary doesn't save analysis result, but they may changed
		 * by user's actions.
		 */
		TableViewerColumn column4 = new TableViewerColumn(this, SWT.NONE);
		TableColumn columnMark = column4.getColumn();
		columnMark.setText("Mark");
		column4.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				String label = "";
				if(element instanceof Info<?>){
					MarkStatus mark = ((Info) element).getMark();
					//TOYIB: Convert enum to String
					if(MarkStatus.Visited == mark)
						label = "Visited";
					else if (mark == MarkStatus.Unchanged)
						label = "Unchanged";
					else if (MarkStatus.Impacted == mark)
						label = "Impacted";
				}
				return label;
			}

		});
		
		TableColumnLayout layout = new TableColumnLayout();
	

		layout.setColumnData(columnRank, new ColumnWeightData(10));
		layout.setColumnData(columnText, new ColumnWeightData(80));
		layout.setColumnData(columnWeight, new ColumnWeightData(10));
		layout.setColumnData(columnMark, new ColumnWeightData(20));
		
		parent.setLayout(layout);
	}
	
	public static void resetRank(){
		rank = 1;
	}
	
	@Override
	public void refresh(){
		resetRank();
		// XXX. Avoid calling this. If the widget has been disposed!
		super.refresh();
	}
}
