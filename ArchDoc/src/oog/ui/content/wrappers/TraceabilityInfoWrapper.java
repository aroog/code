package oog.ui.content.wrappers;

import java.util.ArrayList;
import java.util.List;

import ast.AstNode;
import ast.BaseTraceability;
import ast.BodyDeclaration;
import ast.FieldDeclaration;
import ast.MethodDeclaration;
import ast.MiniAstUtils;
import ast.Type;
import ast.TypeDeclaration;
import ast.VariableDeclaration;

public class TraceabilityInfoWrapper implements InfoWrapper {
 
	private List<Info> traceInfoList = new ArrayList<Info>();

	public TraceabilityInfoWrapper(BaseTraceability inputElement) {
		AstNode expression = inputElement.getExpression();
		TypeDeclaration enclosingTypeDeclaration = MiniAstUtils.getEnclosingTypeDeclaration(expression);
		traceInfoList.add(new EnclosingDeclarationInfo(expression.enclosingDeclaration));
		traceInfoList.add(new EnclosingTypeInfo(enclosingTypeDeclaration));
		traceInfoList.add(new ExpressionInfo(expression));
		//traceInfoList.add( new ExpressionTypeInfo(inputElement.expressionType));
	}

	public class ExpressionTypeInfo implements Info{

		private Type type;

		public ExpressionTypeInfo(Type expressionType) {
			this.type = expressionType;
		}

		@Override
		public String getLabel() {
			return type!=null?type.getFullyQualifiedName():"";
		}

		@Override
		public String getTitle() {
			return "Expression Type";
		}
		
	}
	public class ExpressionInfo implements Info{

		private AstNode expression;

		public ExpressionInfo(AstNode expression) {
			this.expression = expression;
		}

		@Override
		public String getLabel() {
			
			return expression.toString();
		}

		@Override
		public String getTitle() {
			return "Expression";
		}
		
	}
	public class EnclosingTypeInfo implements Info{

		private TypeDeclaration element;

		public EnclosingTypeInfo(TypeDeclaration element) {
			this.element = element;
		}
		@Override
		public String getLabel() {
			String label = "";
			if (element instanceof TypeDeclaration) {
				label = ((TypeDeclaration) element).getFullyQualifiedName();
			}
			return label;
		}

		@Override
		public String getTitle() {
			return "Enclosing Type";
		}
		
	}
	public class EnclosingDeclarationInfo implements Info{

		private BodyDeclaration bodyDeclaration;

		public EnclosingDeclarationInfo(BodyDeclaration bodyDeclaration){
			this.bodyDeclaration = bodyDeclaration;
			
		}
		@Override
		public String getLabel() {
			String label = "";
			if (bodyDeclaration instanceof BodyDeclaration) {
				if (bodyDeclaration instanceof FieldDeclaration) {
					label = ((FieldDeclaration) bodyDeclaration).fieldName;
				} else if (bodyDeclaration instanceof MethodDeclaration) {
					label = ((MethodDeclaration) bodyDeclaration).methodName;
				} else if (bodyDeclaration instanceof VariableDeclaration) {
					label = ((VariableDeclaration) bodyDeclaration).varName;
				}
			}
			return label;
		}

		@Override
		public String getTitle() {
			return "Enclosing Declaration";
		}
		
	}
	@Override
	public Object[] toArray() {
		return traceInfoList.toArray();
	}

}
