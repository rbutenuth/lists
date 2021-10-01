package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.linked.list.SingleLinkedList;

public class ConstructorTests extends AbstractListTest {
	@Test
	public void empty() {
		SingleLinkedList list = SingleLinkedList.fromValues(new FplValue[0]);
		assertEquals(0, list.size());
		assertFalse(list.iterator().hasNext());
	}

	@Test
	public void elementConstructor() {
		SingleLinkedList list = SingleLinkedList.fromValue(value(42));
		assertEquals(1, list.size());
		assertEquals(value(42), list.get(0));
	}

	@Test
	public void emptyListConstructor() {
		SingleLinkedList list = SingleLinkedList.fromValues(Collections.emptyList());
		assertEquals(0, list.size());
	}

	@Test
	public void bigArrayConstructor() {
		FplValue[] values = new FplValue[100];
		for (int i = 0; i < values.length; i++) {
			values[i] = value(i);
		}
		SingleLinkedList list = SingleLinkedList.fromValues(values);
		check(list, 0, values.length);
	}

	@Test
	public void bigListConstructor() {
		FplValue[] values = new FplValue[100];
		for (int i = 0; i < values.length; i++) {
			values[i] = value(i);
		}
		SingleLinkedList list = SingleLinkedList.fromValues(Arrays.asList(values));
		check(list, 0, values.length);
	}

	@Test
	public void emptyFromIterator() throws Exception {
		SingleLinkedList list = SingleLinkedList.fromIterator(createIterator(0, 0));
		check(list, 0, 0);
	}

	@Test
	public void fromIteratorOneBucket() throws Exception {
		SingleLinkedList list = SingleLinkedList.fromIterator(createIterator(0, 5));
		check(list, 0, 5);
	}

	@Test
	public void fromIteratorTwoBuckets() throws Exception {
		SingleLinkedList list = SingleLinkedList.fromIterator(createIterator(0, 10));
		check(list, 0, 10);
	}

	@Test
	public void fromIteratorThreeBuckets() throws Exception {
		SingleLinkedList list = SingleLinkedList.fromIterator(createIterator(0, 163));
		check(list, 0, 163);
	}

	@Test
	public void fromIteratorThreeBucketsWithLastFull() throws Exception {
		SingleLinkedList list = SingleLinkedList.fromIterator(createIterator(0, 167));
		check(list, 0, 167);
	}

	private Iterator<FplValue> createIterator(int from, int to) {
		return new Iterator<FplValue>() {
			int nextValue = from;

			@Override
			public FplValue next() {
				if (hasNext()) {
					return FplInteger.valueOf(nextValue++);
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
