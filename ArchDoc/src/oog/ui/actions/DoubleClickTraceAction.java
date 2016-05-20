package oog.ui.actions;

import oog.ui.content.wrappers.TraceabilityNode;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;

import util.TraceabilityEntry;
import util.TraceabilityList;
import ast.BaseTraceability;
import edu.wayne.tracing.actions.TraceToCodeUIAction;

public final class DoubleClickTraceAction extends Action {
	
	private TreeViewer viewer;
	private TraceToCodeUIAction traceToCode;
	public DoubleClickTraceAction(TreeViewer viewer) {
		this.viewer = viewer;
		traceToCode = new TraceToCodeUIAction();
		TraceToCodeUIAction.setHighlightCode(true);
	}
	public void run() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection)
				.getFirstElement();
		BaseTraceability traceItem = null;
		if(obj instanceof TraceabilityEntry){
			BaseTraceability trace = ((TraceabilityEntry) obj).getSecond();
			traceItem = trace;
		}else if(obj instanceof BaseTraceability){
			traceItem = (BaseTraceability) obj;
			viewer.expandAll();
		}else if(obj instanceof TraceabilityNode){
			traceItem = ((TraceabilityNode) obj).getData().getSecond();
		} 
		
		if(obj instanceof TraceabilityList){
			if(viewer.getExpandedState(obj)){
				viewer.collapseToLevel(obj, TreeViewer.ALL_LEVELS);
			}else{
				viewer.expandToLevel(obj, TreeViewer.ALL_LEVELS);
			}
		}
		if(traceItem!=null){
			traceToCode.setTraceability(traceItem);
			traceToCode.run(null);
		}

	}
}