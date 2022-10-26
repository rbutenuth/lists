package de.codecentric.fpl.datatypes.linkedlist;

import org.junit.jupiter.api.Test;

import de.codecentric.linked.list.SingleLinkedList;

public class AddTest extends AbstractListTest {
	
	@Test
	public void addAtStartOnEmptyList() {
		@SuppressWarnings("unchecked")
		SingleLinkedList<Integer> list = (SingleLinkedList<Integer>) SingleLinkedList.EMPTY_LIST;
		list = list.addAtStart(0);
		check(list, 0, 1);
	}

	@Test
	public void addAtStartOnListWithSpaceInFirstBucket() {
		SingleLinkedList<Integer> list = create(1, 7);
		list = list.addAtStart(0);
		check(list, 0, 7);
	}

	@Test
	public void addAtStartOnListWithNearlyFullFirstBucket() {
		SingleLinkedList<Integer> list = create(1, 8);
		list = list.addAtStart(0);
		check(list, 0, 8);
	}

	@Test
	public void addAtStartOnListWithFullFirstBucket() {
		SingleLinkedList<Integer> list = create(1, 9);
		list = list.addAtStart(0);
		check(list, 0, 9);
	}

	@Test
	public void addAtStartOnListWithBigFirstBucket() {
		SingleLinkedList<Integer> list = create(1, 100);
		list = list.addAtStart(0);
		check(list, 0, 100);
	}

	@Test
	public void addAtStartOnLongArray() {
		SingleLinkedList<Integer> list = create(1, 101);
		list = list.addAtStart(0);
		check(list, 0, 101);
	}
}
