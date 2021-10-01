package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;

import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.linked.list.SingleLinkedList;

public class AbstractListTest {

	/**
	 * @param list  List to check, must contain elements from <code>start</code> and
	 *              <code>end</code>
	 * @param from First element (including)
	 * @param to   Last element in list (excluding)
	 */
	public static void check(SingleLinkedList list, int from, int to) {
		assertEquals(to - from, list.size(), "List size");
		Iterator<FplValue> iter = list.iterator();
		int value = from;
		while (iter.hasNext()) {
			FplInteger next = (FplInteger) iter.next();
			assertEquals(value, next.getValue());
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
	public static SingleLinkedList create(int from, int to) {
		return SingleLinkedList.fromValues(createValues(from, to));
	}

	public static  FplValue[] createValues(int from, int to) {
		FplValue[] values = new FplValue[to - from];
		for (int i = from, j = 0; i < to; i++, j++) {
			values[j] = value(i);
		}
		return values;
	}

	public static FplInteger value(int i) {
		return FplInteger.valueOf(i);
	}
}
