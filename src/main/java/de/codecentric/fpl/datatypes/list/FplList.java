package de.codecentric.fpl.datatypes.list;

import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A persistent list implementation.
 */
public class FplList<E> implements Iterable<E> {
	public static final FplList<?> EMPTY_LIST = new FplList<Object>();

	private static final int BASE_SIZE = 8;
	private static final int FACTOR = 4;

	private final Object[][] shape;

	// private because there is EMPTY_LIST
	private FplList() {
		shape = new Object[0][];
	}

	private FplList(Object[][] data) {
		shape = data;
	}

	/**
	 * Create a list from one value
	 *
	 * @param value The value
	 */
	public static <E> FplList<E> fromValue(E value) {
		Object[][] data = new Object[1][];
		data[0] = bucket(value);
		return new FplList<E>(data);
	}

	/**
	 * Create a list.
	 *
	 * @param values Array with values, the values will be copied, so think about
	 *               using {@link #fromIterator(Iterator, int)} instead.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public static <E> FplList<E> fromValues(E... values) {
		if (values.length == 0) {
			return (FplList<E>) EMPTY_LIST;
		} else {
			Object[][] data = new Object[1][];
			data[0] = Arrays.copyOf(values, values.length);
			return new <E>FplList(data);
		}
	}

	@SuppressWarnings("unchecked")
	public static <E> FplList<E> fromValues(List<E> list) {
		if (list.isEmpty()) {
			return (FplList<E>) EMPTY_LIST;
		} else {
			Object[] values = list.toArray(new Object[list.size()]);
			Object[][] data = new Object[1][];
			data[0] = values;
			return new FplList<E>(data);
		}
	}

	public static <E> FplList<E> fromIterator(Iterator<E> iter, int size) {
		Object[][] shape = createEmptyShape(size);
		for (int bucketsIdx = 0; bucketsIdx < shape.length; bucketsIdx++) {
			Object[] bucket = shape[bucketsIdx];
			for (int inBucketIdx = 0; inBucketIdx < bucket.length; inBucketIdx++) {
				bucket[inBucketIdx] = iter.next();
			}
		}
		if (iter.hasNext()) {
			throw new IllegalArgumentException("Iterator conatins too much elements");
		}
		return new FplList<E>(shape);
	}

	public static <E> FplList<E> fromIterator(Iterator<E> iter) {
		Object[][] data = new Object[1][];
		int bucketSize = BASE_SIZE;
		Object[] currentBucket = new Object[bucketSize];
		data[0] = currentBucket;
		int currentBucketUsed = 0;

		while (iter.hasNext()) {
			E value = iter.next();

			if (currentBucketUsed < currentBucket.length) {
				// Room in last bucket, use it
				currentBucket[currentBucketUsed++] = value;
			} else {
				// Last bucket is full, create a new one
				Object[][] newData = new Object[data.length + 1][];
				arraycopy(data, 0, newData, 0, data.length);
				bucketSize += bucketSize / 2; // * 1.5
				data = newData;
				data[data.length - 1] = currentBucket = new Object[bucketSize];
				currentBucketUsed = 1;
				currentBucket[0] = value;
			}
		}

		if (currentBucketUsed == 0) {
			Object[][] dataNew = new Object[data.length - 1][];
			arraycopy(data, 0, dataNew, 0, dataNew.length);
			data = dataNew;
		} else if (currentBucket.length > currentBucketUsed) {
			Object[] shrinked = new Object[currentBucketUsed];
			arraycopy(currentBucket, 0, shrinked, 0, currentBucketUsed);
			data[data.length - 1] = shrinked;
		}

		return new FplList<E>(data);
	}

	/**
	 * Create a list.
	 *
	 * @param values      Array with values, the values wile be be copied into new
	 *                    arrays.
	 *
	 * @param bucketSizes The size of the used buckets. The of the sizes must match
	 *                    the length of <code>values</code>
	 */
	public static <E> FplList<E> fromValuesWithShape(E[] values, int[] bucketSizes) {
		int sum = 0;
		for (int l : bucketSizes) {
			sum += l;
		}
		if (values.length != sum) {
			throw new IllegalArgumentException(
					"values.length = " + values.length + ", but sum of bucketSizes = " + sum);
		}
		Object[][] data = new Object[bucketSizes.length][];
		int i = 0;
		for (int bucketIdx = 0; bucketIdx < bucketSizes.length; bucketIdx++) {
			int size = bucketSizes[bucketIdx];
			data[bucketIdx] = new Object[size];
			arraycopy(values, i, data[bucketIdx], 0, size);
			i += size;
		}
		return new FplList<E>(data);
	}

	/**
	 * @return First element of the list.
	 * @throws IllegalArgumentException If list is empty.
	 */
	@SuppressWarnings("unchecked")
	public E first() {
		checkNotEmpty();
		return (E) shape[0][0];
	}

	/**
	 * @return Last element of the list.
	 * @throws IllegalArgumentException If list is empty.
	 */
	@SuppressWarnings("unchecked")
	public E last() {
		checkNotEmpty();
		Object[] lastBucket = shape[shape.length - 1];
		return (E) lastBucket[lastBucket.length - 1];
	}

	/**
	 * @return Sublist without the first element.
	 * @throws IllegalArgumentException If list is empty.
	 */
	public FplList<E> removeFirst() {
		checkNotEmpty();
		if (shape[0].length == 1) {
			Object[][] bucketsDst = new Object[shape.length - 1][];
			arraycopy(shape, 1, bucketsDst, 0, bucketsDst.length);
			return new FplList<E>(bucketsDst);
		}
		if (shape[0].length <= BASE_SIZE + 1) {
			Object[][] bucketsDst = new Object[shape.length][];
			arraycopy(shape, 1, bucketsDst, 1, bucketsDst.length - 1);
			bucketsDst[0] = new Object[shape[0].length - 1];
			arraycopy(shape[0], 1, bucketsDst[0], 0, bucketsDst[0].length);
			return new FplList<E>(bucketsDst);
		}
		// First bucket is too large, split it according "ideal" shape
		int count = shape[0].length - 1;
		int additionalBuckets = -1;
		int bucketFillSize = BASE_SIZE / 2;
		while (count > 0) {
			additionalBuckets++;
			count -= bucketFillSize;
			bucketFillSize *= FACTOR;
		}

		Object[][] bucketsDst = new Object[shape.length + additionalBuckets][];
		bucketFillSize = BASE_SIZE / 2;
		bucketsDst[0] = new Object[bucketFillSize];
		arraycopy(shape[0], 1, bucketsDst[0], 0, bucketFillSize);

		int dstIdx = 1;
		count = shape[0].length - 1 - bucketFillSize;
		int inBucketIdx = bucketFillSize + 1;
		while (count > 0) {
			bucketFillSize *= FACTOR;
			if (bucketFillSize > count) {
				bucketFillSize = count;
			}
			bucketsDst[dstIdx] = new Object[bucketFillSize];
			arraycopy(shape[0], inBucketIdx, bucketsDst[dstIdx], 0, bucketFillSize);
			dstIdx++;
			inBucketIdx += bucketFillSize;
			count -= bucketFillSize;
		}
		int srcIdx = 1;
		while (dstIdx < bucketsDst.length) {
			bucketsDst[dstIdx++] = shape[srcIdx++];
		}
		return new FplList<E>(bucketsDst);
	}

	/**
	 * @return Sublist without the last element.
	 * @throws IllegalArgumentException If list is empty.
	 */
	public FplList<E> removeLast() {
		checkNotEmpty();
		int lastIdx = shape.length - 1;
		if (shape[lastIdx].length == 1) {
			Object[][] bucketsDst = new Object[shape.length - 1][];
			arraycopy(shape, 0, bucketsDst, 0, bucketsDst.length);
			return new FplList<E>(bucketsDst);
		}
		if (shape[lastIdx].length <= BASE_SIZE + 1) {
			Object[][] bucketsDst = new Object[shape.length][];
			arraycopy(shape, 0, bucketsDst, 0, bucketsDst.length - 1);
			bucketsDst[lastIdx] = new Object[shape[lastIdx].length - 1];
			arraycopy(shape[lastIdx], 0, bucketsDst[lastIdx], 0, bucketsDst[lastIdx].length);
			return new FplList<E>(bucketsDst);
		}
		int count = shape[lastIdx].length - 1;
		int additionalBuckets = -1;
		int bucketFillSize = BASE_SIZE / 2;
		while (count > 0) {
			additionalBuckets++;
			count -= bucketFillSize;
			bucketFillSize *= FACTOR;
		}

		Object[][] bucketsDst = new Object[shape.length + additionalBuckets][];
		bucketFillSize = BASE_SIZE / 2;
		int dstIdx = shape.length + additionalBuckets - 1;
		int inBucketIdx = shape[lastIdx].length - bucketFillSize - 1;
		bucketsDst[dstIdx] = new Object[bucketFillSize];
		arraycopy(shape[lastIdx], inBucketIdx, bucketsDst[dstIdx], 0, bucketFillSize);

		dstIdx--;
		count = shape[lastIdx].length - 1 - bucketFillSize;
		while (count > 0) {
			bucketFillSize *= FACTOR;
			if (bucketFillSize > count) {
				bucketFillSize = count;
			}
			inBucketIdx -= bucketFillSize;
			bucketsDst[dstIdx] = new Object[bucketFillSize];
			arraycopy(shape[lastIdx], inBucketIdx, bucketsDst[dstIdx], 0, bucketFillSize);
			dstIdx--;
			count -= bucketFillSize;
		}
		lastIdx--;
		while (dstIdx >= 0) {
			bucketsDst[dstIdx--] = shape[lastIdx--];
		}
		return new FplList<E>(bucketsDst);
	}

	/**
	 * @param position Position, starting with 0.
	 * @return Element at position.
	 * @throws IllegalArgumentException If list is empty or if <code>position</code> &lt;
	 *                             0 or &gt;= {@link #size()}.
	 */
	public Object get(int position) {
		checkNotEmpty();
		if (position < 0) {
			throw new IllegalArgumentException("position < 0");
		}
		int bucketIdx = 0;
		int count = 0;
		while (count + shape[bucketIdx].length <= position) {
			count += shape[bucketIdx].length;
			bucketIdx++;
			if (bucketIdx >= shape.length) {
				throw new IllegalArgumentException("position >= size");
			}
		}
		return shape[bucketIdx][position - count];
	}

	/**
	 * Add one value as new first element of the list. (The "cons" of Lisp)
	 *
	 * @param value Element to insert at front.
	 * @return New List: This list plus one new element at front.
	 */
	public FplList<E> addAtStart(E value) {
		int bucketIdx = 0;
		int carrySize = 1;
		int maxSize = BASE_SIZE;
		int lastSize = 0;
		while (bucketIdx < shape.length) {
			int bucketSize = shape[bucketIdx].length;

			if (bucketSize < lastSize) {
				// Buckets are getting smaller, insert carry before
				break;
			}
			if (carrySize + bucketSize < maxSize) {
				// There is enough space in the current bucket,
				// use it by pointing bucketIdx just behind it.
				bucketIdx++;
				carrySize += bucketSize;
				break;
			}
			if (bucketSize >= maxSize) {
				// The current bucket is too big, insert carry before
				break;
			}

			lastSize = bucketSize;
			bucketIdx++;
			carrySize += bucketSize;
			maxSize *= FACTOR;
		}
		// buckedIdx points to the first bucket which is NOT part of the carry
		Object[][] bucketsDst = new Object[shape.length - bucketIdx + 1][];

		// Collect carry
		Object[] carry = new Object[carrySize];
		bucketsDst[0] = carry;
		carry[0] = value;
		for (int i = 0, dst = 1; i < bucketIdx; i++) {
			arraycopy(shape[i], 0, carry, dst, shape[i].length);
			dst += shape[i].length;
		}
		// Copy buckets (behind carry)
		arraycopy(shape, bucketIdx, bucketsDst, 1, bucketsDst.length - 1);

		return new FplList<E>(bucketsDst);
	}

	/**
	 * Append one value at the end of the list.
	 *
	 * @param value Element to be appended
	 * @return New List: This list plus the new element at the end.
	 */
	public FplList<E> addAtEnd(E value) {
		int bucketIdx = shape.length - 1;
		int carrySize = 1;
		int maxSize = BASE_SIZE;
		int lastSize = 0;
		while (bucketIdx >= 0) {
			int bucketSize = shape[bucketIdx].length;

			if (bucketSize < lastSize) {
				// Buckets are getting smaller, insert carry behind
				break;
			}
			if (carrySize + bucketSize < maxSize) {
				// There is enough space in the current bucket,
				// use it by pointing bucketIdx just before it.
				bucketIdx--;
				carrySize += bucketSize;
				break;
			}
			if (bucketSize >= maxSize) {
				// The current bucket is too big, insert carry before
				break;
			}

			lastSize = bucketSize;
			bucketIdx--;
			carrySize += bucketSize;
			maxSize *= FACTOR;
		}
		// buckedIdx points to the first bucket which is NOT part of the carry
		Object[][] bucketsDst = new Object[bucketIdx + 2][];

		// Collect carry
		Object[] carry = new Object[carrySize];
		bucketsDst[bucketsDst.length - 1] = carry;
		carry[carry.length - 1] = value;
		for (int i = bucketIdx + 1, dst = 0; i < shape.length; i++) {
			arraycopy(shape[i], 0, carry, dst, shape[i].length);
			dst += shape[i].length;
		}
		// Copy buckets (before carry)
		arraycopy(shape, 0, bucketsDst, 0, bucketsDst.length - 1);

		return new FplList<E>(bucketsDst);
	}

	/**
	 * Append a second list to this list.
	 *
	 * @param list List to append, <code>null</code> is the same as an empty list.
	 * @return This list with appended list.
	 */
	public FplList<E> append(FplList<E> list) {
		if (list == null || list.isEmpty()) {
			return this;
		}
		if (isEmpty()) {
			return list;
		}
		int totalSize = size() + list.size();
		int totalBuckets = shape.length + list.shape.length;

		Object[] lastBucket = shape[shape.length - 1];
		Object[] listFirstBucket = list.shape[0];

		if (lastBucket.length + listFirstBucket.length <= BASE_SIZE) {
			if (needsReshaping(totalBuckets - 1, totalSize)) {
				return new FplList<E>(mergedShape(shape, list.shape, totalSize));
			} else {
				Object[][] buckets = new Object[shape.length + list.shape.length - 1][];
				arraycopy(shape, 0, buckets, 0, shape.length - 1);
				Object[] bucket = new Object[lastBucket.length + listFirstBucket.length];
				arraycopy(lastBucket, 0, bucket, 0, lastBucket.length);
				arraycopy(listFirstBucket, 0, bucket, lastBucket.length, listFirstBucket.length);
				buckets[shape.length - 1] = bucket;
				arraycopy(list.shape, 1, buckets, shape.length, list.shape.length - 1);
				return new FplList<E>(buckets);
			}
		} else {
			if (needsReshaping(totalBuckets, totalSize)) {
				return new FplList<E>(mergedShape(shape, list.shape, totalSize));
			} else {
				Object[][] buckets = copyOf(shape, shape.length + list.shape.length);
				arraycopy(list.shape, 0, buckets, shape.length, list.shape.length);
				return new FplList<E>(buckets);
			}
		}
	}

	private boolean needsReshaping(int numberOfBuckets, int size) {
		return (1 << numberOfBuckets) > size;
	}

	private Object[][] mergedShape(Object[][] left, Object[][] right, int totalSize) {
		Object[][] buckets = createEmptyShape(totalSize);

		int idx = 0, inBucketIdx = 0, dstIdx = 0, inBucketDstIdx = 0;
		// Copy entries from "left"
		while (idx < left.length) {
			int length = Math.min(left[idx].length - inBucketIdx, buckets[dstIdx].length - inBucketDstIdx);
			arraycopy(left[idx], inBucketIdx, buckets[dstIdx], inBucketDstIdx, length);
			inBucketIdx += length;
			if (inBucketIdx == left[idx].length) {
				inBucketIdx = 0;
				idx++;
			}
			inBucketDstIdx += length;
			if (inBucketDstIdx == buckets[dstIdx].length) {
				inBucketDstIdx = 0;
				dstIdx++;
			}
		}
		// Copy entries from "right"
		idx = 0;
		inBucketIdx = 0;
		while (idx < right.length) {
			int length = Math.min(right[idx].length - inBucketIdx, buckets[dstIdx].length - inBucketDstIdx);
			arraycopy(right[idx], inBucketIdx, buckets[dstIdx], inBucketDstIdx, length);
			inBucketIdx += length;
			if (inBucketIdx == right[idx].length) {
				inBucketIdx = 0;
				idx++;
			}
			inBucketDstIdx += length;
			if (inBucketDstIdx == buckets[dstIdx].length) {
				inBucketDstIdx = 0;
				dstIdx++;
			}
		}

		return buckets;
	}

	/**
	 * Create an empty shape starting at both ends with size 3/4 * BASE_SIZE and
	 * increasing by FACTOR to the middle.
	 *
	 * @param size It place for this number of values.
	 * @return Array of arrays, all values <code>null</code>
	 */
	private static Object[][] createEmptyShape(int size) {
		int numBuckets = 2;
		int bucketSize = 3 * BASE_SIZE / 4;
		int sizeInBuckets = 2 * bucketSize;
		while (sizeInBuckets < size) {
			bucketSize *= FACTOR;
			sizeInBuckets += 2 * bucketSize;
			numBuckets += 2;
		}
		numBuckets--;
		Object[][] emptyShape = new Object[numBuckets][];
		bucketSize = BASE_SIZE;
		int rest = size;
		int i = 0, j = numBuckets - 1;
		while (i < j) {
			emptyShape[i] = new Object[bucketSize / 2];
			emptyShape[j] = new Object[bucketSize / 2];
			rest -= emptyShape[i].length + emptyShape[j].length;
			bucketSize *= FACTOR;
			i++;
			j--;
		}
		emptyShape[i] = new Object[rest];

		return emptyShape;
	}

	/**
	 * Returns a portion of this list between the specified {@code fromIndex},
	 * inclusive, and {@code toIndex}, exclusive. (If {@code fromIndex} and
	 * {@code toIndex} are equal, the returned list is empty.)
	 */
	@SuppressWarnings("unchecked")
	public FplList<E> subList(int fromIndex, int toIndex) {
		if (fromIndex < 0) {
			throw new IllegalArgumentException("fromIndex < 0");
		}
		if (fromIndex > toIndex) {
			throw new IllegalArgumentException("fromIndex > toIndex");
		}
		if (fromIndex == toIndex) {
			return (FplList<E>) EMPTY_LIST;
		}
		int bucketFromIdx = 0;
		int index = 0;

		// Determine index of first bucket and index within that bucket
		while (index + shape[bucketFromIdx].length <= fromIndex) {
			index += shape[bucketFromIdx].length;
			bucketFromIdx++;
			if (bucketFromIdx >= shape.length) {
				throw new IllegalArgumentException("fromIndex >= size");
			}
		}
		int inBucketFromIdx = fromIndex - index;
		int bucketToIdx = bucketFromIdx;

		// Determine index of last bucket and index within that bucket
		while (index + shape[bucketToIdx].length <= toIndex - 1) {
			index += shape[bucketToIdx].length;
			bucketToIdx++;
			if (bucketToIdx >= shape.length) {
				throw new IllegalArgumentException("toIndex > size + 1");
			}
		}
		int inBucketToIdx = toIndex - index;

		// Optimization: Return origin list when subList of complete list is requested
		if (fromIndex == 0 && bucketToIdx == shape.length - 1 && inBucketToIdx == shape[bucketToIdx].length) {
			return this;
		}

		if (bucketFromIdx == bucketToIdx) {
			return subListFromOneLargeArray(shape[bucketFromIdx], inBucketFromIdx, inBucketToIdx);
		} else {
			int numBucketsLeft = computeNumberOfBucketsLeft(shape[bucketFromIdx], inBucketFromIdx);
			int numBucketsRight = computeNumberOfBucketsRight(shape[bucketToIdx], inBucketToIdx);
			int numBucketsCenter = bucketToIdx - bucketFromIdx - 1;

			Object[][] bucketsDst = new Object[numBucketsLeft + numBucketsCenter + numBucketsRight][];

			createAndFillShapeFromLeft(shape[bucketFromIdx], inBucketFromIdx, bucketsDst);
			arraycopy(shape, bucketFromIdx + 1, bucketsDst, numBucketsLeft, numBucketsCenter);
			createAndFillShapeFromRight(shape[bucketToIdx], inBucketToIdx, bucketsDst);

			return new FplList<E>(bucketsDst);
		}
	}

	private void createAndFillShapeFromLeft(Object[] bucket, int inBucketFromIdx, Object[][] bucketsDst) {
		if (inBucketFromIdx == 0) {
			bucketsDst[0] = bucket;
		} else {
			int bucketSize = 3 * BASE_SIZE / 4;
			int bucketDstIndex = 0;
			int rest = bucket.length - inBucketFromIdx;
			while (rest > bucketSize) {
				int size = Math.min(bucketSize / 2, rest);
				bucketsDst[bucketDstIndex] = new Object[size];
				arraycopy(bucket, inBucketFromIdx, bucketsDst[bucketDstIndex], 0, size);
				bucketDstIndex++;
				inBucketFromIdx += size;
				rest -= size;
				bucketSize *= FACTOR;
			}
			bucketsDst[bucketDstIndex] = new Object[rest];
			arraycopy(bucket, inBucketFromIdx, bucketsDst[bucketDstIndex], 0, rest);
		}
	}

	private void createAndFillShapeFromRight(Object[] bucket, int inBucketToIdx, Object[][] bucketsDst) {
		if (inBucketToIdx == bucket.length) {
			bucketsDst[bucketsDst.length - 1] = bucket;
		} else {
			int bucketSize = 3 * BASE_SIZE / 4;
			int bucketDstIndex = bucketsDst.length - 1;
			int rest = inBucketToIdx + 1;
			while (rest > bucketSize) {
				int size = Math.min(bucketSize / 2, rest);
				bucketsDst[bucketDstIndex] = new Object[size];
				arraycopy(bucket, inBucketToIdx - size + 1, bucketsDst[bucketDstIndex], 0, size);
				bucketDstIndex--;
				inBucketToIdx += size;
				rest -= size;
				bucketSize *= FACTOR;
			}
			bucketsDst[bucketDstIndex] = new Object[rest];
			arraycopy(bucket, 0, bucketsDst[bucketDstIndex], 0, rest);
		}
	}

	private int computeNumberOfBucketsLeft(Object[] fplValues, int inBucketIdx) {
		if (inBucketIdx == 0) {
			return 1; // It is possible to copy the complete bucket by reference
		}
		return numBucketsForCount(fplValues.length - inBucketIdx);
	}

	private int computeNumberOfBucketsRight(Object[] fplValues, int inBucketIdx) {
		if (inBucketIdx == fplValues.length) {
			return 1; // It is possible to copy the complete bucket by reference
		}
		return numBucketsForCount(inBucketIdx);
	}

	private int numBucketsForCount(int count) {
		if (count < BASE_SIZE) {
			return 1;
		}
		int rest = count;
		int bucketSize = 3 * BASE_SIZE / 4;
		int buckets = 1;
		while (rest > bucketSize) {
			rest -= bucketSize / 2; // fill to half
			bucketSize *= FACTOR;
			buckets++;
		}
		return buckets;
	}

	@SuppressWarnings("unchecked")
	private FplList<E> subListFromOneLargeArray(Object[] fplValues, int first, int behindLast) {
		int size = behindLast - first;
		if (size <= BASE_SIZE) {
			Object[] b = new Object[size];
			arraycopy(fplValues, first, b, 0, size);
			return (FplList<E>) FplList.fromValues(b);
		} else {
			Object[][] bucketsDst = createEmptyShape(size);
			for (int i = 0, bucketIdx = 0; bucketIdx < bucketsDst.length; bucketIdx++) {
				Object[] bucketDst = bucketsDst[bucketIdx];
				arraycopy(fplValues, first + i, bucketDst, 0, bucketDst.length);
				i += bucketDst.length;
			}
			return new FplList<E>(bucketsDst);
		}
	}

	/**
	 * @return The lower / first half of a list. Get the other half with
	 *         {@link #upperHalf()}. When the size of the list is not an even
	 *         number, the lower half will be one element smaller than the upper
	 *         half.
	 */
	@SuppressWarnings("unchecked")
	public FplList<E> lowerHalf() {
		// from is 0, so no variable
		int to = size() / 2; // exclusive
		// size = to
		if (to == 0) {
			return (FplList<E>) EMPTY_LIST;
		}
		Object[][] data;

		int count = 0;
		int fromBucketIdx = 0;
		while (count < to) {
			count += shape[fromBucketIdx++].length;
		}
		if (fromBucketIdx < shape.length && count == to) {
			data = new Object[fromBucketIdx][];
			arraycopy(shape, 0, data, 0, data.length);
		} else {
			data = createShapeForSplitting(to);
			fromBucketIdx = 0;
			Object[] fromBucket = shape[fromBucketIdx];
			int inFromBucketIdx = 0;
			int toBucketIdx = 0;
			Object[] toBucket = data[toBucketIdx];
			int inToBucketIdx = 0;
			for (int i = 0; i < to; i++) {
				toBucket[inToBucketIdx++] = fromBucket[inFromBucketIdx++];
				if (inFromBucketIdx == fromBucket.length) {
					// We can never hit the last value in the last bucket,
					// so here no if necessary as for toBucketIdx
					fromBucket = shape[++fromBucketIdx];
					inFromBucketIdx = 0;
				}
				if (inToBucketIdx == toBucket.length) {
					if (++toBucketIdx < data.length) {
						toBucket = data[toBucketIdx];
					}
					inToBucketIdx = 0;
				}
			}
		}
		return new FplList<E>(data);
	}

	/**
	 * @return The upper / second half of a list. Get the other half with
	 *         {@link #lowerHalf()}. When the size of the list is not an even
	 *         number, the lower half will be one element smaller than the upper
	 *         half.
	 */
	@SuppressWarnings("unchecked")
	public FplList<E> upperHalf() {
		int to = size(); // exclusive
		int from = to / 2; // inclusive
		int size = to - from;
		if (size == 0) {
			return (FplList<E>) EMPTY_LIST;
		}
		Object[][] data;

		int count = 0;
		int fromBucketIdx = shape.length - 1;
		while (count < size) {
			count += shape[fromBucketIdx--].length;
		}
		fromBucketIdx++;
		if (count == size) {
			data = new Object[shape.length - fromBucketIdx][];
			arraycopy(shape, fromBucketIdx, data, 0, data.length);
		} else {
			data = createShapeForSplitting(size);

			fromBucketIdx = shape.length - 1;
			Object[] fromBucket = shape[fromBucketIdx];
			int inFromBucketIdx = fromBucket.length - 1;
			int toBucketIdx = data.length - 1;
			Object[] toBucket = data[toBucketIdx];
			int inToBucketIdx = toBucket.length - 1;
			for (int i = 0; i < size; i++) {
				toBucket[inToBucketIdx--] = fromBucket[inFromBucketIdx--];
				if (inFromBucketIdx < 0) {
					fromBucket = shape[--fromBucketIdx];
					inFromBucketIdx = fromBucket.length - 1;
				}
				if (inToBucketIdx < 0) {
					if (--toBucketIdx >= 0) {
						toBucket = data[toBucketIdx];
					}
					inToBucketIdx = toBucket.length - 1;
				}
			}
		}

		return new FplList<E>(data);
	}

	private Object[][] createShapeForSplitting(int size) {
		int numberOfBuckets = 1;
		int bucketSize = size;
		while (bucketSize > BASE_SIZE && (2L << numberOfBuckets) < size) {
			numberOfBuckets *= 2;
			bucketSize = size / numberOfBuckets;
		}
		Object[][] data = new Object[numberOfBuckets][];
		createBuckets(data, 0, numberOfBuckets, size);

		return data;
	}

	private void createBuckets(Object[][] data, int offset, int numberOfBuckets, int size) {
		if (numberOfBuckets == 1) {
			data[offset] = new Object[size];
		} else {
			int nextNumberOfBuckets = numberOfBuckets / 2;
			int lower = size / 2;
			int upper = size - lower;
			createBuckets(data, offset, nextNumberOfBuckets, lower);
			createBuckets(data, offset + nextNumberOfBuckets, nextNumberOfBuckets, upper);
		}
	}

	/**
	 * @return Number of elements in the list.
	 */
	public int size() {
		int count = 0;
		for (Object[] bucket : shape) {
			count += bucket.length;
		}
		return count;
	}

	public <T> FplList<T> map(java.util.function.Function<E, T> operator) {
		return FplList.fromIterator(new Iterator<T>() {
			Iterator<E> iter = iterator();

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public T next() {
				return operator.apply(iter.next());
			}
		}, size());
	}

	public <T> FplList<T> flatMapWithIntermediateArrayList(java.util.function.Function<E, FplList<T>> operator) {
		List<T> values = new ArrayList<>(size());
		Iterator<E> iter = iterator();
		while (iter.hasNext()) {
			FplList<T> subList = operator.apply(iter.next());
			for (T subValue : subList) {
				values.add(subValue);
			}
		}
		return FplList.fromValues(values);
	}

	@SuppressWarnings("unchecked")
	public <T> FplList<T> flatMapWithIterator(java.util.function.Function<E, FplList<T>> operator) {
		Iterator<E> listIter = iterator();
		if (listIter.hasNext()) {

			return FplList.fromIterator(new Iterator<T>() {
				Iterator<T> subListIter = operator.apply(listIter.next()).iterator();

				@Override
				public boolean hasNext() {
					return subListIter.hasNext();
				}

				@Override
				public T next() {
					T result = subListIter.next();
					while (!subListIter.hasNext()) {
						if (listIter.hasNext()) {
							subListIter = operator.apply(listIter.next()).iterator();
						} else {
							break;
						}
					}
					return result;
				}
			});
		} else {
			return (FplList<T>) EMPTY_LIST;
		}
	}
	
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			private int bucketsIdx = 0;
			private int inBucketIdx = 0;
			private boolean atEnd = isEmpty();

			@Override
			public boolean hasNext() {
				return !atEnd;
			}

			@Override
			public E next() {
				if (atEnd) {
					throw new NoSuchElementException();
				}
				@SuppressWarnings("unchecked")
				E result = (E) shape[bucketsIdx][inBucketIdx];
				inBucketIdx++;
				if (inBucketIdx == shape[bucketsIdx].length) {
					bucketsIdx++;
					inBucketIdx = 0;
					atEnd = bucketsIdx == shape.length;
				}
				return result;
			}
		};
	}

	public boolean isEmpty() {
		return shape.length == 0;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		Iterator<E> iter = iterator();
		while (iter.hasNext()) {
			E v = iter.next();
			sb.append(v == null ? "nil" : v.toString());
			if (iter.hasNext()) {
				sb.append(" ");
			}
		}
		sb.append(')');
		return sb.toString();
	}

	// only for testing
	int[] bucketSizes() {
		int[] sizes = new int[shape.length];
		for (int i = 0; i < shape.length; i++) {
			sizes[i] = shape[i].length;
		}
		return sizes;
	}

	private void checkNotEmpty() throws IllegalArgumentException {
		if (isEmpty()) {
			throw new IllegalArgumentException("List is empty");
		}
	}

	private static Object[] bucket(Object element) {
		Object[] result = new Object[1];
		result[0] = element;
		return result;
	}
}
