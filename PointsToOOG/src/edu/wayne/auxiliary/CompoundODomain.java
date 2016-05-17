package edu.wayne.auxiliary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.wayne.ograph.internal.ODomain;

public class CompoundODomain implements Iterator<List<ODomain>>, Iterable<List<ODomain>> {

	private Set<List<ODomain>> transpose;

	public CompoundODomain() {
		this.transpose = new LinkedHashSet<List<ODomain>>();
	}

	public void add(Set<ODomain> lookupSet) {
		if (lookupSet.size() > 1) {
			Set<List<ODomain>> newTranspose = new LinkedHashSet<List<ODomain>>();
			for (ODomain d : lookupSet) {
				ArrayList<ODomain> newArrayList = new ArrayList<ODomain>();
				if (transpose.isEmpty()) {
					newArrayList.add(d);
					newTranspose.add(newArrayList);
				} else {
					for (List<ODomain> listD : transpose) {
						ArrayList<ODomain> arrayList = newArrayList;
						arrayList.addAll(listD);
						arrayList.add(d);
						newTranspose.add(arrayList);
					}
				}
			}
			transpose = newTranspose;
		} else { // add one element to each list
			for (ODomain d : lookupSet) {
				ArrayList<ODomain> newArrayList = new ArrayList<ODomain>();
				if (transpose.isEmpty()) {
					newArrayList.add(d);
					transpose.add(newArrayList);
				} else {
					for (List<ODomain> listD : transpose) {
						listD.add(d);
					}
				}
			}
		}
	}

	@Override
	public Iterator<List<ODomain>> iterator() {
		return transpose.iterator();
	}

	@Override
	public boolean hasNext() {
		return transpose.iterator().hasNext();
	}

	@Override
	public List<ODomain> next() {
		return transpose.iterator().next();
	}

	@Override
	public void remove() {
	}

}
