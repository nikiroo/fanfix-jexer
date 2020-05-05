package be.nikiroo.jexer;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class TTableLine implements List<String> {
	//TODO: in TTable: default to header of size 1
	private List<String> list;

	public TTableLine(List<String> list) {
		this.list = list;
	}

	// TODO: override this and the rest shall follow
	protected List<String> getList() {
		return list;
	}

	@Override
	public int size() {
		return getList().size();
	}

	@Override
	public boolean isEmpty() {
		return getList().isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return getList().contains(o);
	}

	@Override
	public Iterator<String> iterator() {
		return getList().iterator();
	}

	@Override
	public Object[] toArray() {
		return getList().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return getList().toArray(a);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return getList().containsAll(c);
	}

	@Override
	public String get(int index) {
		return getList().get(index);
	}

	@Override
	public int indexOf(Object o) {
		return getList().indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return getList().lastIndexOf(o);
	}

	@Override
	public List<String> subList(int fromIndex, int toIndex) {
		return getList().subList(fromIndex, toIndex);
	}

	@Override
	public ListIterator<String> listIterator() {
		return getList().listIterator();
	}

	@Override
	public ListIterator<String> listIterator(int index) {
		return getList().listIterator(index);
	}

	@Override
	public boolean add(String e) {
		throw new UnsupportedOperationException("Read-only collection");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Read-only collection");
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
		throw new UnsupportedOperationException("Read-only collection");
	}

	@Override
	public boolean addAll(int index, Collection<? extends String> c) {
		throw new UnsupportedOperationException("Read-only collection");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Read-only collection");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Read-only collection");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Read-only collection");
	}

	@Override
	public String set(int index, String element) {
		throw new UnsupportedOperationException("Read-only collection");
	}

	@Override
	public void add(int index, String element) {
		throw new UnsupportedOperationException("Read-only collection");
	}

	@Override
	public String remove(int index) {
		throw new UnsupportedOperationException("Read-only collection");
	}
}
