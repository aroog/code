package edu.cmu.cs.viewer.ui;

public interface ITreeElement {

	String getLabel();

	Object[] getChildren();

	Object getParent();

	boolean hasChildren();

	boolean isSelectable();

	boolean isVisible();
}
