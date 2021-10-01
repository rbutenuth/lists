package de.codecentric.fpl.datatypes.list;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import de.codecentric.fpl.datatypes.FplInteger;
import de.codecentric.fpl.datatypes.FplValue;
import de.codecentric.linked.list.SingleLinkedList;

public class MapTest extends AbstractListTest {

	@Test
	public void map() {
		SingleLinkedList list = create(0, 3);
		list = list.map(new Function<FplValue, FplValue>() {

			@Override
			public FplValue apply(FplValue v) {
				FplInteger i = (FplInteger) v;
				return FplInteger.valueOf(i.getValue() + 1);
			}
		});
		check(list, 1, 4);
	}

	@Test
	public void flatMap() {
		SingleLinkedList list = SingleLinkedList.fromValues(create(0, 3), create(3, 7));
		list = list.flatMap(new Function<FplValue, SingleLinkedList>() {

			@Override
			public SingleLinkedList apply(FplValue t) {
				return (SingleLinkedList)t;
			}
		});
		check(list, 0, 7);
	}
}
