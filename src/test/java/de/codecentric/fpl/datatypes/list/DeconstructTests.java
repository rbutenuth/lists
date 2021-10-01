package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.linked.list.SingleLinkedList;

public class DeconstructTests extends AbstractListTest {
	@Test
	public void removeFirstSizeOne() {
		SingleLinkedList list = SingleLinkedList.fromValues(new FplValue[1]);
		SingleLinkedList rest = list.removeFirst();
		assertEquals(0, rest.size());
	}

	@Test
	public void removeFirstSizeHundred() {
		SingleLinkedList list = create(0, 100);
		SingleLinkedList rest = list.removeFirst();
		check(rest, 1, 100);
	}
}
