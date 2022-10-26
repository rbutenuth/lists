package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class DeconstructTests extends AbstractListTest {
	@Test
	public void removeFirstSizeOne() {
		FplList<Integer> list = FplList.fromValue(1);
		FplList<Integer> rest = list.removeFirst();
		assertEquals(0, rest.size());
	}

	@Test
	public void removeFirstSizeTwo() {
		FplList<Integer> list = FplList.fromValue(value(1)).addAtEnd(value(2));
		FplList<Integer> rest = list.removeFirst();
		assertEquals(1, rest.size());
		assertEquals(value(2), rest.get(0));
	}

	@Test
	public void removeFirstSizeHundred() {
		FplList<Integer> list = create(0, 100);
		FplList<Integer> rest = list.removeFirst();
		check(rest, 1, 100);
	}

	@Test
	public void removeFirstSizeFiftyAppendFifty() {
		FplList<Integer> list = create(0, 50).append(create(50, 100));
		FplList<Integer> rest = list.removeFirst();
		check(rest, 1, 100);
	}

	@Test
	public void removeFirstSizeTwoFromAppend() {
		FplList<Integer> list = create(1, 2).append(create(2, 3));
		FplList<Integer> rest = list.removeFirst();
		assertEquals(1, rest.size());
		assertEquals(value(2), rest.get(0));
	}

	@Test
	public void removeLastSizeOne() {
		FplList<Integer> list = FplList.fromValue(1);
		FplList<Integer> rest = list.removeLast();
		assertEquals(0, rest.size());
	}

	@Test
	public void removeLastSizeTwo() {
		FplList<Integer> list = FplList.fromValue(value(1)).addAtEnd(value(2));
		FplList<Integer> rest = list.removeLast();
		assertEquals(1, rest.size());
		assertEquals(value(1), rest.get(0));
	}

	@Test
	public void removeLastSizeHundred() {
		FplList<Integer> list = create(0, 100);
		FplList<Integer> rest = list.removeLast();
		check(rest, 0, 99);
	}

	@Test
	public void removeLastSizeFiftyAppendFifty() {
		FplList<Integer> list = create(0, 50).append(create(50, 100));
		FplList<Integer> rest = list.removeLast();
		check(rest, 0, 99);
	}

	@Test
	public void removeLastSizeTwoFromAppend() {
		FplList<Integer> list = create(1, 2).append(create(2, 3));
		FplList<Integer> rest = list.removeLast();
		assertEquals(1, rest.size());
		assertEquals(value(1), rest.get(0));
	}

	@Test
	public void lowerHalfFromEmptyList() {
		FplList<Integer> emptyList = FplList.emptyList();
		FplList<Integer> list = emptyList.lowerHalf();
		assertTrue(list.isEmpty());
	}

	@Test
	public void lowerHalfFromListWithSizeOne() {
		FplList<Integer> list = create(0, 1).lowerHalf();
		assertTrue(list.isEmpty());
	}

	@Test
	public void lowerHalfFromSmallList() {
		FplList<Integer> list = create(0, 16);
		FplList<Integer> lower = list.lowerHalf();
		check(lower, 0, 8);
		checkSizes(lower, 8);
	}

	@Test
	public void lowerHalfFromMediumList() {
		FplList<Integer> list = create(0, 100, 30, 40, 30);
		FplList<Integer> lower = list.lowerHalf();
		check(lower, 0, 50);
		checkSizes(lower, 6, 6, 6, 7, 6, 6, 6, 7);
	}

	@Test
	public void lowerHalfFromBigList() {
		FplList<Integer> list = create(0, 1000000);
		FplList<Integer> lower = list.lowerHalf();
		check(lower, 0, 500000);
		assertEquals(32, lower.bucketSizes().length);
		lower = lower.lowerHalf();
		check(lower, 0, 250000);
		assertEquals(16, lower.bucketSizes().length);
		while (lower.bucketSizes().length > 1) {
			lower = lower.lowerHalf();
		}
		lower = lower.lowerHalf();
		assertEquals(16, lower.bucketSizes().length);
	}

	@Test
	public void upperHalfFromEmptyList() {
		FplList<Integer> emptyList = FplList.emptyList();
		FplList<Integer> list = emptyList.upperHalf();
		assertTrue(list.isEmpty());
	}

	@Test
	public void upperHalfFromListWithSizeOne() {
		FplList<Integer> list = create(0, 1).upperHalf();
		check(list, 0, 1);
	}

	@Test
	public void upperHalfFromSmallList() {
		FplList<Integer> list = create(0, 16);
		FplList<Integer> upper = list.upperHalf();
		check(upper, 8, 16);
		checkSizes(upper, 8);
	}

	@Test
	public void upperHalfFromMediumList() {
		FplList<Integer> list = create(0, 100, 30, 40, 30);
		FplList<Integer> upper = list.upperHalf();
		check(upper, 50, 100);
		checkSizes(upper, 6, 6, 6, 7, 6, 6, 6, 7);
	}

	@Test
	public void upperHalfFromBigList() {
		FplList<Integer> list = create(0, 1000000);
		FplList<Integer> upper = list.upperHalf();
		check(upper, 500000, 1000000);
		assertEquals(32, upper.bucketSizes().length);
		upper = upper.upperHalf();
		check(upper, 750000, 1000000);
		assertEquals(16, upper.bucketSizes().length);
	}
	
	@Test
	public void iteratorFromEmptyList() {
		FplList<Integer> emptyList = FplList.emptyList();
		Iterator<Integer> iterator = emptyList.iterator();
		assertFalse(iterator.hasNext());
		assertThrows(NoSuchElementException.class, () -> { iterator.next(); });
	}
}
