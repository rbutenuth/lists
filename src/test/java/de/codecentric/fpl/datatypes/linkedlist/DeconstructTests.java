package de.codecentric.fpl.datatypes.linkedlist;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.codecentric.linked.list.SingleLinkedList;

public class DeconstructTests extends AbstractListTest {
	@Test
	public void removeFirstSizeOne() {
		SingleLinkedList<Integer> list = SingleLinkedList.fromValues(new Integer[1]);
		SingleLinkedList<Integer> rest = list.removeFirst();
		assertEquals(0, rest.size());
	}

	@Test
	public void removeFirstSizeHundred() {
		SingleLinkedList<Integer> list = create(0, 100);
		SingleLinkedList<Integer> rest = list.removeFirst();
		check(rest, 1, 100);
	}
}
