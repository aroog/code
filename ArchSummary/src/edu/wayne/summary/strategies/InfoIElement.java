package edu.wayne.summary.strategies;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import oog.itf.IElement;

public class InfoIElement implements Info<IElement> {

	private Set<IElement> values = new HashSet<IElement>();
	private String key;
	private InfoType type;

	public InfoIElement(String key, InfoType type){
		this.key = key;
		this.type = type;
	}

	public InfoIElement(String key) {
		this.key = key;
		this.type = InfoType.UNK;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public double getNumber() {

		return values.size();
	}

	@Override
	public Set<IElement> getValues() {
		return values;

	}

	@Override
	public MarkStatus getMark() {
		return MarkManager.getInstance().getMark(this.key);
	}
	
	@Override
	public void setMark(MarkStatus m) {
		MarkManager.getInstance().setMark(this.key, m);
	}
	/**
	 * Note: this class has a natural ordering that is inconsistent with equals.
	 * Ordering is greatest to least based on {@link Info#getNumber()}
	 */
	@Override
	public int compareTo(Info<IElement> o) {
		if(this.equals(o)){
			return 0;
		}
		if (this.getNumber() < o.getNumber()) {
			return 1;
		}else if(this.getNumber() == o.getNumber()){
			return this.key.compareTo(o.getKey());
		}
		return -1;

	}

	@Override
	public boolean add(IElement item) {
		return values.add(item);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InfoIElement other = (InfoIElement) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return "InfoIElement [values=" + values + ", key=" + key + "]";
	}

	@Override
	public boolean addAll(Collection<? extends IElement> c) {
		return values.addAll(c);

	}

	@Override
	public InfoType getType() {
		return type;
	}
}
