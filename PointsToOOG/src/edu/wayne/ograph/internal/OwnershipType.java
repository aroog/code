package edu.wayne.ograph.internal;

import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * This is C<\ob{p}> - but is used only for fields. TODO: LOW: maybe remove
 * */
public class OwnershipType implements Entry<ITypeBinding, List<DomainP>> {

	private ITypeBinding C;
	private List<DomainP> listP;

	public OwnershipType(ITypeBinding c, List<DomainP> listP) {
		C = c;
		this.listP = listP;
	}

	@Override
	public ITypeBinding getKey() {
		return C;
	}

	@Override
	public List<DomainP> getValue() {
		return listP;
	}

	@Override
	public List<DomainP> setValue(List<DomainP> arg0) {
		listP.clear();
		listP.addAll(arg0);
		return listP;
	}

}
