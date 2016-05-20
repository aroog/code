package edu.wayne.metrics.qual;

import org.eclipse.jdt.core.dom.ITypeBinding;
import ast.Type;

public abstract class Classify_Base extends Q_Base {

	// All methods that extracts the classifications- With and Without annotations

	// Methods that classify outliers of metrics on systems with annotations
	public abstract void Which_A(Type objType);
	public abstract void SO(Type ObjType);
	public abstract void PTEP(ITypeBinding fieldType);
	public abstract void DFEP(Type fieldType);
	public abstract void TMO(Type ObjType, Type superType);
	public abstract void Which_A_In_Which_B(Type firstObjType, Type secObjType);
	public abstract void Which_A_In_B(Type firstObjType, Type secObjType);
	public abstract void HMO(Type objType);

	// Method to classify fields/variables/receivers/parameters on systems with no annotations
	public abstract void noAnnotateVisitor(ITypeBinding type);

}
