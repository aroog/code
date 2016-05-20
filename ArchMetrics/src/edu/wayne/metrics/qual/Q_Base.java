package edu.wayne.metrics.qual;

import java.io.IOException;


public abstract class Q_Base{

	public abstract void visit();

	public abstract void display() throws IOException;
	
	// TODO:DONE Constants -> ALLCAPS (Eclipse naming conventions)
	public static final String EXCEPTIONS = "java.lang.Exception";
	
}
