package edu.wayne.summary.strategies;
import java.util.Collection;
import java.util.Set;



public class InfoUnranked implements Info<String> {
	private String key;
	private InfoType type;
	public InfoUnranked(String key) {
		this.key = key;
		this.type = InfoType.UNK;
	}
	@Override
	public int compareTo(Info<String> o) {
		return this.key.compareTo(o.getKey());
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
		InfoUnranked other = (InfoUnranked) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}
	@Override
	public String getKey() {
		return key;
	}

	@Override
	public double getNumber() {
		return 0;
	}

	@Override
	public Set<String> getValues() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(String item) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
		throw new UnsupportedOperationException();
		
	}
	@Override
	public InfoType getType() {
		return type;
	}
	@Override
	public MarkStatus getMark() {
		return MarkManager.getInstance().getMark(this.key);
	}
	@Override
	public void setMark(MarkStatus m) {
		MarkManager.getInstance().setMark(this.key, m);
	}

}
