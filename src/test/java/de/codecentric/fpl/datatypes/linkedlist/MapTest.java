package de.codecentric.fpl.datatypes.linkedlist;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

import de.codecentric.linked.list.SingleLinkedList;

public class MapTest extends AbstractListTest {

	@Test
	public void map() {
		SingleLinkedList<Integer> list = create(0, 3);
		list = list.map(new Function<Integer, Integer>() {

			@Override
			public Integer apply(Integer v) {
				Integer i = v;
				return Integer.valueOf(i + 1);
			}
		});
		check(list, 1, 4);
	}

	@Test
	public void flatMap() {
		@SuppressWarnings("unchecked")
		SingleLinkedList<SingleLinkedList<Integer>> list = SingleLinkedList.fromValues(create(0, 3), create(3, 7));
		SingleLinkedList<Integer> result = list.flatMap(new Function<SingleLinkedList<Integer>, SingleLinkedList<Integer>>() {

			@Override
			public SingleLinkedList<Integer> apply(SingleLinkedList<Integer> t) {
				return t;
			}
		});
		check(result, 0, 7);
	}
}
