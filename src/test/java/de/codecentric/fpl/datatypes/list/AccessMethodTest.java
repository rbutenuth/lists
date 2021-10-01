package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.linked.list.SingleLinkedList;

public class AccessMethodTest extends AbstractListTest {

	@Test
	public void createAndCheck() {
		SingleLinkedList list = create(0, 10);
		assertEquals(10, list.size());
		check(list, 0, 10);
	}

	@Test
	public void iterateTooMuchSmallList() {
		assertThrows(NoSuchElementException.class, () -> {
			Iterator<FplValue> iter = SingleLinkedList.fromValue(value(1)).iterator();
			assertTrue(iter.hasNext());
			assertEquals(value(1), iter.next());
			assertFalse(iter.hasNext());
			iter.next();
		});
	}

	@Test
	public void iterateTooMuchLargeList() {
		assertThrows(NoSuchElementException.class, () -> {
			Iterator<FplValue> iter = create(0, 100).iterator();
			for (int i = 0; i <= 99; i++) {
				assertTrue(iter.hasNext());
				assertEquals(value(i), iter.next());
			}
			assertFalse(iter.hasNext());
			iter.next();
		});
	}

	@Test
	public void listToStringNumbers() {
		assertEquals("(0 1 2)", create(0, 3).toString());
	}

	@Test
	public void firstSizeOne() {
		SingleLinkedList list = SingleLinkedList.fromValue(value(1));
		assertEquals(value(1), list.first());
	}

	@Test
	public void firstSmall() {
		SingleLinkedList list = create(3, 6);
		assertEquals(value(3), list.first());
	}

	@Test
	public void firstLarge() {
		SingleLinkedList list = create(3, 51);
		assertEquals(value(3), list.first());
	}

	@Test
	public void firstEmptyFails() {
		assertThrows(Exception.class, () -> {
			SingleLinkedList list = SingleLinkedList.fromValues(new FplValue[0]);
			list.first();
		});
	}

	@Test
	public void removeFirstEmptyFails() {
		assertThrows(Exception.class, () -> {
			SingleLinkedList list = SingleLinkedList.fromValues(new FplValue[0]);
			list.removeFirst();
		});
	}

	@Test
	public void removeFirstSmall() {
		SingleLinkedList list = create(0, 6).removeFirst();
		check(list, 1, 6);
	}

	@Test
	public void lastEmptyFails() {
		assertThrows(Exception.class, () -> {
			SingleLinkedList list = SingleLinkedList.fromValues(new FplValue[0]);
			list.removeFirst();
		});
	}

	@Test
	public void getFromEmptyList() {
		assertThrows(Exception.class, () -> {
			SingleLinkedList.EMPTY_LIST.get(0);
		});
	}

	@Test
	public void getSmallListIndexNegative() {
		assertThrows(Exception.class, () -> {
			create(0, 4).get(-1);
		});
	}

	@Test
	public void getSmallListIndexOutOfBounds() {
		assertThrows(Exception.class, () -> {
			create(0, 4).get(4);
		});
	}

	@Test
	public void getLargelListIndexOutOfBounds() {
		assertThrows(Exception.class, () -> {
			create(0, 101).get(101);
		});
	}
}
