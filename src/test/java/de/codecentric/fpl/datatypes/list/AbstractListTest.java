package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;

import de.codecentric.linked.list.SingleLinkedList;

public class AbstractListTest {

	/**
	 * @param list  List to check, must contain elements from <code>start</code> and
	 *              <code>end</code>
	 * @param from First element (including)
	 * @param to   Last element in list (excluding)
	 */
	public static void check(SingleLinkedList<Integer> list, int from, int to) {
		assertEquals(to - from, list.size(), "List size");
		Iterator<Integer> iter = list.iterator();
		int value = from;
		while (iter.hasNext()) {
			Integer next = (Integer) iter.next();
			assertEquals(value, next);
			value++;
		}
		assertEquals(to, value);
	}

	/**
	 * @param from first element of generated list (including)
	 * @param to   last element of generated list (excluding)
	 * @return List of {@link FplInteger}, including <code>start</code> and
	 *         <code>end</code>
	 */
	public static SingleLinkedList<Integer> create(int from, int to) {
		return SingleLinkedList.fromValues(createValues(from, to));
	}

	public static  Integer[] createValues(int from, int to) {
		Integer[] values = new Integer[to - from];
		for (int i = from, j = 0; i < to; i++, j++) {
			values[j] = i;
		}
		return values;
	}

}
