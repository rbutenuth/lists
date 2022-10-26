package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;

public class AbstractListTest {

	/**
	 * @param list  List to check, must contain elements from <code>start</code> and
	 *              <code>end</code>
	 * @param from First element (including)
	 * @param to   Last element in list (excluding)
	 */
	public static void check(FplList<Integer> list, int from, int to) {
		assertEquals(to - from, list.size(), "List size");
		Iterator<Integer> iter = list.iterator();
		int value = from;
		while (iter.hasNext()) {
			Integer next = iter.next();
			assertEquals(value, next.intValue());
			value++;
		}
		assertEquals(to, value);
	}

	public static void checkSizes(FplList<Integer> list, int... sizes) {
		int[] listSizes = list.bucketSizes();
		assertEquals(sizes.length, listSizes.length, "Wrong number of buckets");
		for (int i = 0; i < listSizes.length; i++) {
			assertEquals(sizes[i], listSizes[i], "Size of bucket " + i);
		}
	}
	
	public static FplList<Integer> create(int from, int to, int... bucketSizes) {
		return FplList.fromValuesWithShape(createValues(from, to), bucketSizes);
	}

	/**
	 * @param from first element of generated list (including)
	 * @param to   last element of generated list (excluding)
	 * @return List of {@link FplInteger}, including <code>start</code> and excluding
	 *         <code>end</code>
	 */
	public static FplList<Integer> create(int from, int to) {
		int bucketSizes[] = new int[1];
		bucketSizes[0] = to - from;
		return FplList.fromValuesWithShape(createValues(from, to), bucketSizes);
	}

	public static  Integer[] createValues(int from, int to) {
		Integer[] values = new Integer[to - from];
		for (int i = from, j = 0; i < to; i++, j++) {
			values[j] = value(i);
		}
		return values;
	}

	public static Integer value(int i) {
		return Integer.valueOf(i);
	}
}
