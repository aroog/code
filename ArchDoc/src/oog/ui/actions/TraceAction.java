package oog.ui.actions;

import org.eclipse.jface.action.Action;

import edu.wayne.tracing.actions.TraceToCodeUIAction;

import ast.AstNode;
import ast.BaseTraceability;

public class TraceAction extends Action {

	private BaseTraceability trace;
	TraceToCodeUIAction action = new TraceToCodeUIAction();
	public TraceAction(BaseTraceability trace) {
		this.trace = trace;
		
		AstNode expression = this.trace.getExpression();
		if(expression!=null){
			this.setText(expression.toString());
		}else{
			this.setText(trace.toString());
		}
		
		action.setTraceability(trace);
		TraceToCodeUIAction.setHighlightCode(true);
	}
	@Override
	public void run() {
		super.run();
		action.run(null);
	}
	
}
