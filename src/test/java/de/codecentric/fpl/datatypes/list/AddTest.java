package de.codecentric.fpl.datatypes.list;

import org.junit.jupiter.api.Test;

public class AddTest extends AbstractListTest {
	
	@Test
	public void addAtStartOnEmptyList() {
		FplList<Integer> list = FplList.emptyList();
		list = list.addAtStart(value(0));
		check(list, 0, 1);
		checkSizes(list, 1);
	}

	@Test
	public void addAtEndOnEmptyList() {
		FplList<Integer> list = FplList.emptyList();
		list = list.addAtEnd(value(0));
		check(list, 0, 1);
		checkSizes(list, 1);
	}

	@Test
	public void addAtStartOnListWithSpaceInFirstBucket() {
		FplList<Integer> list = create(1, 7);
		list = list.addAtStart(value(0));
		check(list, 0, 7);
		checkSizes(list,  7);
	}

	@Test
	public void addAtEndOnListWithSpaceInLastBucket() {
		FplList<Integer> list = create(0, 6);
		list = list.addAtEnd(value(6));
		check(list, 0, 7);
		checkSizes(list,  7);
	}

	@Test
	public void addAtStartOnListWithNearlyFullFirstBucket() {
		FplList<Integer> list = create(1, 8);
		list = list.addAtStart(value(0));
		check(list, 0, 8);
		checkSizes(list, 8);
	}

	@Test
	public void addAtEndOnListWithNearlyFullLastBucket() {
		FplList<Integer> list = create(0, 7);
		list = list.addAtEnd(value(7));
		check(list, 0, 8);
		checkSizes(list, 8);
	}

	@Test
	public void addAtStartOnListWithFullFirstBucket() {
		FplList<Integer> list = create(1, 9);
		list = list.addAtStart(value(0));
		check(list, 0, 9);
		checkSizes(list, 1, 8);
	}

	@Test
	public void addAtEndOnListWithFullLastBucket() {
		FplList<Integer> list = create(0, 8);
		list = list.addAtEnd(value(8));
		check(list, 0, 9);
		checkSizes(list, 8, 1);
	}

	@Test
	public void addAtStartOnList_7_8_16() {
		FplList<Integer> list = create(1, 32, 7, 8, 16);
		list = list.addAtStart(value(0));
		check(list, 0, 32);
		checkSizes(list, 16, 16);
	}

	@Test
	public void addAtEndOnList_16_8_7() {
		FplList<Integer> list = create(0, 31, 16, 8, 7);
		list = list.addAtEnd(value(31));
		check(list, 0, 32);
		checkSizes(list, 16, 16);
	}

	@Test
	public void addAtStartOnListWithBigFirstBucket() {
		int sizes[] = new int[1];
		sizes[0] = 99;
		FplList<Integer> list = create(1, 100, sizes);
		list = list.addAtStart(value(0));
		check(list, 0, 100);
		checkSizes(list, 1, 99);
	}

	@Test
	public void addAtEndOnListWithBigLastBucket() {
		int sizes[] = new int[1];
		sizes[0] = 99;
		FplList<Integer> list = create(0, 99, sizes);
		list = list.addAtEnd(value(99));
		check(list, 0, 100);
		checkSizes(list, 99, 1);
	}

	@Test
	public void addAtStartOnListWithSmallerBucketsAtEnd() {
		FplList<Integer> list = create(1, 40, 7, 24, 8);
		list = list.addAtStart(value(0));
		check(list, 0, 40);
		checkSizes(list, 32, 8);
	}

	@Test
	public void addAtEndOnListWithSmallerBucketsAtStart() {
		FplList<Integer> list = create(0, 39, 8, 24, 7);
		list = list.addAtEnd(value(39));
		check(list, 0, 40);
		checkSizes(list, 8, 32);
	}

	@Test
	public void addAtStartWithBigCarry() {
		FplList<Integer> list = create(1, 101).append(create(101, 201));
		list = list.addAtStart(value(0));
		check(list, 0, 201);
	}

	@Test
	public void addAtEndWithBigCarry() {
		FplList<Integer> list = create(0, 100).append(create(100, 200));
		list = list.addAtEnd(value(200));
		check(list, 0, 201);
	}

	@Test
	public void addAtStartOnLongArray() {
		FplList<Integer> list = create(1, 101);
		list = list.addAtStart(value(0));
		check(list, 0, 101);
	}

	@Test
	public void addAtEndOnLongArray() {
		FplList<Integer> list = create(0, 100);
		list = list.addAtEnd(value(100));
		check(list, 0, 101);
	}

	@Test
	public void addAtStartWithStartOverflowInBigBucket() {
		FplList<Integer> list = create(1, 9).append(create(9, 109));
		list = list.addAtStart(value(0));
		check(list, 0, 109);
	}
	
	@Test
	public void addAtEndWithStartOverflowInBigBucket() {
		FplList<Integer> list = create(0, 100).append(create(100, 109));
		list = list.addAtEnd(value(109));
		check(list, 0, 110);
	}
	
	@Test
	public void addAtStartWithStartOverflowInSmallBucket() {
		FplList<Integer> list = create(1, 9).append(create(9, 17));
		list = list.addAtStart(value(0));
		check(list, 0, 17);
	}

	@Test
	public void addAtEndWithStartOverflowInSmallBucket() {
		FplList<Integer> list = create(0, 8).append(create(8, 16));
		list = list.addAtEnd(value(16));
		check(list, 0, 17);
	}

}
