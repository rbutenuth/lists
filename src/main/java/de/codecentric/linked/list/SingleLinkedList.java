package de.codecentric.linked.list;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A persistent list implementation.
 */
public class SingleLinkedList<E> implements Iterable<E> {
	public static final SingleLinkedList<?> EMPTY_LIST = new SingleLinkedList<Object>();

	private static class Node<E> {
		private final E value;
		private Node<E> next;
		
		private Node(E value) {
			this.value = value;
		}
	}
	
	private final Node<E> first;
	
	// private because there is EMPTY_LIST
	private SingleLinkedList() {
		first = null;
	}

	private SingleLinkedList(Node<E> first) {
		this.first = first;
	}

	/**
	 * Create a list from one value
	 *
	 * @param value The value
	 */
	public static <E> SingleLinkedList<E> fromValue(E value) {
		Node<E> node = new Node<E>(value);
		return new SingleLinkedList<E>(node);
	}

	/**
	 * Create a list.
	 *
	 * @param values Array with values, the values will NOT be copied, so don't
	 *               modify the array after calling this method!
	 */
	@SuppressWarnings("unchecked")
	public static <E> SingleLinkedList<E> fromValues(E... values) {
		if (values.length == 0) {
			return (SingleLinkedList<E>) EMPTY_LIST;
		} else {
			Node<E> first = new Node<E>(values[0]);
			Node<E> last = first;
			for (int i = 1; i < values.length; i++) {
				E value = values[i];
				Node<E> node = new Node<E>(value);
				last.next = node;
				last = node;
			}
			return new SingleLinkedList<E>(first);
		}
	}

	@SuppressWarnings("unchecked")
	public static <E> SingleLinkedList<E> fromValues(List<E> list) {
		if (list.isEmpty()) {
			return (SingleLinkedList<E>) EMPTY_LIST;
		} else {
			Node<E> first = new Node<E>(list.get(0));
			Node<E> last = first;
			for (int i = 1; i < list.size(); i++) {
				E value = list.get(i);
				Node<E> node = new Node<E>(value);
				last.next = node;
				last = node;
			}
			return new SingleLinkedList<E>(first);
		}
	}

	@SuppressWarnings("unchecked")
	public static <E> SingleLinkedList<E> fromIterator(Iterator<E> iter) {
		if (iter.hasNext()) {
			Node<E> first = new Node<E>(iter.next());
			Node<E> last = first;
			while (iter.hasNext()) {
				E value = iter.next();
				Node<E> node = new Node<E>(value);
				last.next = node;
				last = node;
			}
			return new SingleLinkedList<E>(first);
		} else {
			return (SingleLinkedList<E>) EMPTY_LIST;
		}
	}

	/**
	 * @return First element of the list.
	 * @throws IllegalArgumentException If list is empty.
	 */
	public E first() {
		checkNotEmpty();
		return first.value;
	}

	/**
	 * @return Sublist without the first element.
	 * @throws IllegalArgumentException If list is empty.
	 */
	@SuppressWarnings("unchecked")
	public SingleLinkedList<E> removeFirst() {
		checkNotEmpty();
		if (first.next == null) {
			return (SingleLinkedList<E>) EMPTY_LIST;
		} else {
			return new SingleLinkedList<E>(first.next);
		}
	}

	/**
	 * @param position Position, starting with 0.
	 * @return Element at position.
	 * @throws IllegalArgumentException If list is empty or if <code>position</code> &lt;
	 *                             0 or &gt;= {@link #size()}.
	 */
	public E get(int position) {
		checkNotEmpty();
		if (position < 0) {
			throw new IllegalArgumentException("position < 0");
		}
		Node<E> node = first;
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
	public SingleLinkedList<E> addAtStart(E value) {
		Node<E> node = new Node<E>(value);
		node.next = first;
		return new SingleLinkedList<E>(node);
	}

	/**
	 * @return Number of elements in the list.
	 */
	public int size() {
		int count = 0;
		Node<E> node = first;
		while (node != null) {
			node = node.next;
			count++;
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	public <T> SingleLinkedList<T> map(java.util.function.Function<E, T> operator) {
		if (first == null) {
			return (SingleLinkedList<T>) EMPTY_LIST;
		} else {
			Node<E> current = first;
			Node<T> newFirst = null;
			Node<T> last = null;
			while (current != null) {
				Node<T> node = new Node<T>(operator.apply(current.value));
				if (newFirst == null) {
					newFirst = node;
				} else {
					last.next = node;
				}
				last = node;
				current = current.next;
			}
			return new SingleLinkedList<T>(newFirst);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> SingleLinkedList<T> flatMap(java.util.function.Function<E, SingleLinkedList<T>> operator) {
		if (first == null) {
			return (SingleLinkedList<T>) EMPTY_LIST;
		} else {
			Node<E> current = first;
			Node<T> newFirst = null;
			Node<T> last = null;
			while (current != null) {
				Node<T> subCurrent = operator.apply(current.value).first;
				while (subCurrent != null) {
					Node<T> node = new Node<T>(subCurrent.value);
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
			return new SingleLinkedList<T>(newFirst);
		}
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			Node<E> node = first;
			
			@Override
			public boolean hasNext() {
				return node != null;
			}

			@Override
			public E next() {
				if (node == null) {
					throw new NoSuchElementException();
				}
				E result = node.value;
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
		Iterator<E> iter = iterator();
		while (iter.hasNext()) {
			E v = iter.next();
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
