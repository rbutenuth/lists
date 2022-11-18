package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class SetTests extends AbstractListTest {
	private static Integer number = Integer.valueOf(-1);
	
	@Test
	public void setOnEmptyThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> { FplList.emptyList().set(0, number); });
	}
	
	@Test
	public void setWithNegativeIndexThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> { create(0, 10).set(-1, number); });
	}
	
	@Test
	public void setWithIndexOutOfBoundsOneBucketThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> { create(0, 5).set(5, number); });
	}
	
	@Test
	public void setWithIndexOutOfBoundsThrowsException() {
		assertThrows(IllegalArgumentException.class, () -> { create(0, 10, 5, 5).set(10, number); });
	}
	
	@Test
	public void updateFirstOneSmallBucket() {
		FplList<Integer> list = create(0, 8);
		list = list.set(0, number);
		checkUpdated(0, 8, list);
	}

	@Test
	public void updateFirstTwoBuckets() {
		FplList<Integer> list = create(0, 20, 10, 10);
		list = list.set(0, number);
		checkUpdated(0, 20, list);
	}

	@Test
	public void updateSecondTwoBuckets() {
		FplList<Integer> list = create(0, 20, 10, 10);
		list = list.set(1, number);
		checkUpdated(1, 20, list);
	}

	@Test
	public void updateFirstWithReshape() {
		FplList<Integer> list = create(0, 100, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
		list = list.set(0, number);
		checkUpdated(0, 100, list);
	}

	@Test
	public void updateFirstWithReshape2() {
		FplList<Integer> list = create(0, 100, 4, 15, 11, 10, 10, 10, 10, 10, 10, 10);
		list = list.set(10, number);
		checkUpdated(10, 100, list);
	}

	@Test
	public void updateLastOneSmallBucket() {
		FplList<Integer> list = create(0, 8);
		list = list.set(7, number);
		checkUpdated(7, 8, list);
	}

	@Test
	public void updateLastTwoBuckets() {
		FplList<Integer> list = create(0, 20, 10, 10);
		list = list.set(19, number);
		checkUpdated(19, 20, list);
	}

	@Test
	public void updateSecondLastTwoBuckets() {
		FplList<Integer> list = create(0, 20, 10, 10);
		list = list.set(18, number);
		checkUpdated(18, 20, list);
	}

	@Test
	public void updateLastWithReshape() {
		FplList<Integer> list = create(0, 100, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10);
		list = list.set(99, number);
		checkUpdated(99, 100, list);
	}

	private void checkUpdated(int updatedIndex, int expectedSize, FplList<Integer> list) {
		assertEquals(expectedSize, list.size());
		for (int i = 0; i < expectedSize; i++) {
			if (i == updatedIndex) {
				assertEquals(number, list.get(i));
			} else {
				assertEquals(Integer.valueOf(i), list.get(i));
			}
		}
	}
}
