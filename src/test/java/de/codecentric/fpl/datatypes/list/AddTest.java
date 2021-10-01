package de.codecentric.fpl.datatypes.list;

import org.junit.jupiter.api.Test;

import de.codecentric.linked.list.SingleLinkedList;

public class AddTest extends AbstractListTest {
	
	@Test
	public void addAtStartOnEmptyList() {
		SingleLinkedList list = SingleLinkedList.EMPTY_LIST;
		list = list.addAtStart(value(0));
		check(list, 0, 1);
	}

	@Test
	public void addAtStartOnListWithSpaceInFirstBucket() {
		SingleLinkedList list = create(1, 7);
		list = list.addAtStart(value(0));
		check(list, 0, 7);
	}

	@Test
	public void addAtStartOnListWithNearlyFullFirstBucket() {
		SingleLinkedList list = create(1, 8);
		list = list.addAtStart(value(0));
		check(list, 0, 8);
	}

	@Test
	public void addAtStartOnListWithFullFirstBucket() {
		SingleLinkedList list = create(1, 9);
		list = list.addAtStart(value(0));
		check(list, 0, 9);
	}

	@Test
	public void addAtStartOnListWithBigFirstBucket() {
		SingleLinkedList list = create(1, 100);
		list = list.addAtStart(value(0));
		check(list, 0, 100);
	}

	@Test
	public void addAtStartOnLongArray() {
		SingleLinkedList list = create(1, 101);
		list = list.addAtStart(value(0));
		check(list, 0, 101);
	}
}
