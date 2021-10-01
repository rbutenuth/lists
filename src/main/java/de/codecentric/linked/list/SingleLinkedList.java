package de.codecentric.linked.list;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.codecentric.fpl.datatypes.FplValue;

/**
 * A persistent list implementation.
 */
public class SingleLinkedList implements FplValue, Iterable<FplValue> {
	public static final SingleLinkedList EMPTY_LIST = new SingleLinkedList();

	private static class Node {
		private final FplValue value;
		private Node next;
		
		private Node(FplValue value) {
			this.value = value;
		}
	}
	
	private final Node first;
	
	// private because there is EMPTY_LIST
	private SingleLinkedList() {
		first = null;
	}

	private SingleLinkedList(Node first) {
		this.first = first;
	}

	/**
	 * Create a list from one value
	 *
	 * @param value The value
	 */
	public static SingleLinkedList fromValue(FplValue value) {
		Node node = new Node(value);
		return new SingleLinkedList(node);
	}

	/**
	 * Create a list.
	 *
	 * @param values Array with values, the values will NOT be copied, so don't
	 *               modify the array after calling this method!
	 */
	public static SingleLinkedList fromValues(FplValue... values) {
		if (values.length == 0) {
			return EMPTY_LIST;
		} else {
			Node first = new Node(values[0]);
			Node last = first;
			for (int i = 1; i < values.length; i++) {
				FplValue value = values[i];
				Node node = new Node(value);
				last.next = node;
				last = node;
			}
			return new SingleLinkedList(first);
		}
	}

	public static SingleLinkedList fromValues(List<? extends FplValue> list) {
		if (list.isEmpty()) {
			return EMPTY_LIST;
		} else {
			Node first = new Node(list.get(0));
			Node last = first;
			for (int i = 1; i < list.size(); i++) {
				FplValue value = list.get(i);
				Node node = new Node(value);
				last.next = node;
				last = node;
			}
			return new SingleLinkedList(first);
		}
	}

	public static SingleLinkedList fromIterator(Iterator<FplValue> iter) {
		if (iter.hasNext()) {
			Node first = new Node(iter.next());
			Node last = first;
			while (iter.hasNext()) {
				FplValue value = iter.next();
				Node node = new Node(value);
				last.next = node;
				last = node;
			}
			return new SingleLinkedList(first);
		} else {
			return EMPTY_LIST;
		}
	}

	/**
	 * @return First element of the list.
	 * @throws EvaluationException If list is empty.
	 */
	public FplValue first() {
		checkNotEmpty();
		return first.value;
	}

	/**
	 * @return Sublist without the first element.
	 * @throws EvaluationException If list is empty.
	 */
	public SingleLinkedList removeFirst() {
		checkNotEmpty();
		if (first.next == null) {
			return EMPTY_LIST;
		} else {
			return new SingleLinkedList(first.next);
		}
	}

	/**
	 * @param position Position, starting with 0.
	 * @return Element at position.
	 * @throws EvaluationException If list is empty or if <code>position</code> &lt;
	 *                             0 or &gt;= {@link #size()}.
	 */
	public FplValue get(int position) {
		checkNotEmpty();
		if (position < 0) {
			throw new IllegalArgumentException("position < 0");
		}
		Node node = first;
		for (int i = 0; i < position; i++) {
			node = node.next;
			if (node == null) {
				throw new IllegalArgumentException("position >= size");
			}
		}
		return node.value;
	}

	/**
	 * Add one value as new first element of the list. (The "cons" of Lisp)
	 *
	 * @param value Element to insert at front.
	 * @return New List: This list plus one new element at front.
	 */
	public SingleLinkedList addAtStart(FplValue value) {
		Node node = new Node(value);
		node.next = first;
		return new SingleLinkedList(node);
	}

	/**
	 * @return Number of elements in the list.
	 */
	public int size() {
		int count = 0;
		Node node = first;
		while (node != null) {
			node = node.next;
			count++;
		}
		return count;
	}

	public SingleLinkedList map(java.util.function.Function<FplValue, FplValue> operator) {
		if (first == null) {
			return EMPTY_LIST;
		} else {
			Node current = first;
			Node newFirst = null;
			Node last = null;
			while (current != null) {
				Node node = new Node(operator.apply(current.value));
				if (newFirst == null) {
					newFirst = node;
				} else {
					last.next = node;
				}
				last = node;
				current = current.next;
			}
			return new SingleLinkedList(newFirst);
		}
	}

	public SingleLinkedList flatMap(java.util.function.Function<FplValue, SingleLinkedList> operator) {
		if (first == null) {
			return EMPTY_LIST;
		} else {
			Node current = first;
			Node newFirst = null;
			Node last = null;
			while (current != null) {
				Node subCurrent = ((SingleLinkedList)operator.apply(current.value)).first;
				while (subCurrent != null) {
					Node node = new Node(subCurrent.value);
					if (newFirst == null) {
						newFirst = node;
					} else {
						last.next = node;
					}
					last = node;
					subCurrent = subCurrent.next;
				}
				current = current.next;
			}
			return new SingleLinkedList(newFirst);
		}
	}

	@Override
	public Iterator<FplValue> iterator() {
		return new Iterator<FplValue>() {
			Node node = first;
			
			@Override
			public boolean hasNext() {
				return node != null;
			}

			@Override
			public FplValue next() {
				if (node == null) {
					throw new NoSuchElementException();
				}
				FplValue result = node.value;
				node = node.next;
				return result;
			}
		};
	}

	public boolean isEmpty() {
		return first == null;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		Iterator<FplValue> iter = iterator();
		while (iter.hasNext()) {
			FplValue v = iter.next();
			sb.append(v == null ? "nil" : v.toString());
			if (iter.hasNext()) {
				sb.append(" ");
			}
		}
		sb.append(')');
		return sb.toString();
	}

	private void checkNotEmpty() {
		if (isEmpty()) {
			throw new IllegalStateException("List is empty");
		}
	}
}
