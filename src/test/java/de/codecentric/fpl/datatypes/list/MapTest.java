package de.codecentric.fpl.datatypes.list;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class MapTest extends AbstractListTest {

	@Test
	public void map() {
		FplList<Integer> list = create(0, 3);
		list = list.map(new Function<Integer, Integer>() {

			@Override
			public Integer apply(Integer v) {
				return Integer.valueOf(v.intValue() + 1);
			}
		});
		check(list, 1, 4);
	}

	@Test
	public void flatMapEmptyList() {
		FplList<Integer> list = FplList.emptyList();
		list = list.flatMap(new Function<Integer, FplList<Integer>>() {

			@Override
			public FplList<Integer> apply(Integer t) {
				return FplList.fromValue(t);
			}
		});
		assertTrue(list.isEmpty());
	}
	
	@Test
	public void flatMapOneEmptyListInTheMiddle() {
		FplList<FplList<Integer>> input = FplList.fromValues(create(0, 3), FplList.emptyList(), create(3, 7));
		FplList<Integer> list = input.flatMap(new Function<FplList<Integer>, FplList<Integer>>() {

			@Override
			public FplList<Integer> apply(FplList<Integer> t) {
				return t;
			}

		});
		check(list, 0, 7);
	}
	
	@Test
	public void flatMapOneEmptyListAtTheBeginning() {
		FplList<FplList<Integer>> input = FplList.fromValues(FplList.emptyList(), create(0, 3), create(3, 7));
		FplList<Integer> list = input.flatMap(new Function<FplList<Integer>, FplList<Integer>>() {

			@Override
			public FplList<Integer> apply(FplList<Integer> t) {
				return t;
			}

		});
		check(list, 0, 7);
	}
	
	@Test
	public void flatMapOneEmptyListAtTheEnd() {
		FplList<FplList<Integer>> input = FplList.fromValues(create(0, 3), create(3, 7), FplList.emptyList());
		FplList<Integer> list = input.flatMap(new Function<FplList<Integer>, FplList<Integer>>() {

			@Override
			public FplList<Integer> apply(FplList<Integer> t) {
				return t;
			}

		});
		check(list, 0, 7);
	}
	


}
