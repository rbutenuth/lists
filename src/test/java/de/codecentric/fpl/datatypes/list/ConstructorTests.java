package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class ConstructorTests extends AbstractListTest {
	@Test
	public void elementConstructor() {
		FplList<Integer> list = FplList.fromValue(value(42));
		assertEquals(1, list.size());
		assertEquals(value(42), list.get(0));
		assertEquals(1, list.bucketSizes().length);
	}

	@Test
	public void twoElementConstructor() {
		FplList<Integer> list = FplList.fromValues(value(42), value(43));
		assertEquals(2, list.size());
		assertEquals(value(42), list.get(0));
		assertEquals(value(43), list.get(1));
		assertEquals(1, list.bucketSizes().length);
	}

	@Test
	public void emptyListConstructor() {
		FplList<Integer> list = FplList.fromValues(Collections.emptyList());
		assertEquals(0, list.size());
	}

	@Test
	public void bigListConstructor() {
		Integer[] values = new Integer[100];
		for (int i = 0; i < values.length; i++) {
			values[i] = value(i);
		}
		FplList<Integer> list = FplList.fromValues(Arrays.asList(values));
		check(list, 0, values.length);
		assertEquals(1, list.bucketSizes().length);
	}

	@Test
	public void badShape() {
		assertThrows(IllegalArgumentException.class, () -> {
			Integer[] values = new Integer[0];
			int[] bucketSizes = new int[1];
			bucketSizes[0] = 1;
			FplList.fromValuesWithShape(values, bucketSizes);
		});
	}

	@Test
	public void emptyFromIterator() throws Exception {
		FplList<Integer> list = FplList.fromIterator(new Iterator<Integer>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public Integer next() {
				return null; // never called
			}
		}, 0);
		assertTrue(list.isEmpty());
	}

	@Test
	public void elementsFromIterator() throws Exception {
		FplList<Integer> list = FplList.fromIterator(createIterator(0, 0));
		check(list, 0, 0);
	}

	@Test
	public void fromIteratorOneBucket() throws Exception {
		FplList<Integer> list = FplList.fromIterator(createIterator(0, 5));
		check(list, 0, 5);
		checkSizes(list, 5);
	}

	@Test
	public void fromIteratorTwoBuckets() throws Exception {
		FplList<Integer> list = FplList.fromIterator(createIterator(0, 10));
		check(list, 0, 10);
		checkSizes(list, 8, 2);
	}

	@Test
	public void fromIteratorThreeBuckets() throws Exception {
		int size = 8 + 12 + 17;
		FplList<Integer> list = FplList.fromIterator(createIterator(0, size));
		check(list, 0, size);
		checkSizes(list, 8, 12, 17);
	}

	@Test
	public void fromIteratorThreeBucketsWithLastFull() throws Exception {
		int size = 8 + 12 + 18;
		FplList<Integer> list = FplList.fromIterator(createIterator(0, size));
		check(list, 0, size);
		checkSizes(list, 8, 12, 18);
	}

	@Test
	public void fromIteratorWithBadSize() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> {
			FplList.fromIterator(createIterator(0, 2), 1);
		});
	}

	private Iterator<Integer> createIterator(int from, int to) {
		return new Iterator<Integer>() {
			int nextValue = from;

			@Override
			public Integer next() {
				if (hasNext()) {
					return Integer.valueOf(nextValue++);
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public boolean hasNext() {
				return nextValue < to;
			}
		};
	}
}
