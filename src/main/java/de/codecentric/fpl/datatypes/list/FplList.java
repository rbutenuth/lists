package de.codecentric.fpl.datatypes.list;

import static java.lang.Math.min;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * A persistent list implementation.
 */
public class FplList<E> implements Iterable<E> {
	public static final FplList<?> EMPTY_LIST = new FplList<Object>();
	private static final Iterator<?> EMPTY_ITERATOR = new Iterator<Object>() {
		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Object next() {
			throw new NoSuchElementException("empty list");
		}
	};

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

	@SuppressWarnings("unchecked")
	public static <E> FplList<E> emptyList() {
		return (FplList<E>) EMPTY_LIST;
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
	 * @param a first element
	 * @param b second element
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <E> FplList<E> fromValues(E a, E b) {
		Object[][] data = new Object[1][];
		Object[] ab = new Object[2];
		ab[0] = a;
		ab[1] = b;
		data[0] = ab;
		return new FplList(data);
	}

	/**
	 * Create a list.
	 *
	 * @param a first element
	 * @param b second element
	 * @param c third element
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <E> FplList<E> fromValues(E a, E b, E c) {
		Object[][] data = new Object[1][];
		Object[] abc = new Object[3];
		abc[0] = a;
		abc[1] = b;
		abc[2] = c;
		data[0] = abc;
		return new FplList(data);
	}

	@SuppressWarnings("unchecked")
	public static <E> FplList<E> fromValues(List<? extends E> list) {
		if (list.isEmpty()) {
			return (FplList<E>) EMPTY_LIST;
		} else {
			Object[] values = list.toArray(new Object[list.size()]);
			Object[][] data = new Object[1][];
			data[0] = values;
			return new FplList<E>(data);
		}
	}

	@SuppressWarnings("unchecked")
	public static <E> FplList<E> fromIterator(Iterator<E> iter, int size) {
		if (iter.hasNext()) {
			int[] bucketSizes = computeBucketSizes(size);
			Object[][] shape = new Object[bucketSizes.length][];
			for (int bucketIdx = 0; bucketIdx < shape.length; bucketIdx++) {
				Object[] bucket = new Object[bucketSizes[bucketIdx]];
				shape[bucketIdx] = bucket;
				for (int inBucketIdx = 0; inBucketIdx < bucket.length; inBucketIdx++) {
					bucket[inBucketIdx] = iter.next();
				}
			}
			if (iter.hasNext()) {
				throw new IllegalArgumentException("Iterator conatins too much elements");
			}
			return new FplList<E>(shape);
		} else {
			return (FplList<E>) EMPTY_LIST;
		}
	}

	@SuppressWarnings("unchecked")
	public static <E> FplList<E> fromIterator(Iterator<E> iter) {
		if (!iter.hasNext()) {
			return (FplList<E>) EMPTY_LIST;
		} else {
			Object[][] data = new Object[1][];
			int bucketSize = BASE_SIZE;
			Object[] currentBucket = new Object[bucketSize];
			data[0] = currentBucket;
			int currentBucketUsed = 0;

			do {
				E value = iter.next();

				if (currentBucketUsed < currentBucket.length) {
					// Room in last bucket, use it
					currentBucket[currentBucketUsed++] = value;
				} else {
					// Last bucket is full, create a new one
					Object[][] newData = copyOf(data, data.length + 1);
					bucketSize += bucketSize / 2; // * 1.5
					data = newData;
					data[data.length - 1] = currentBucket = new Object[bucketSize];
					currentBucketUsed = 1;
					currentBucket[0] = value;
				}
			} while (iter.hasNext());

			if (currentBucket.length > currentBucketUsed) {
				data[data.length - 1] = copyOf(currentBucket, currentBucketUsed);
			}

			return new FplList<E>(data);
		}
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
			data[bucketIdx] = copyOfRange(values, i, i + size);
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
			return new FplList<E>(copyOfRange(shape, 1, shape.length));
		}
		if (shape[0].length <= BASE_SIZE + 1) {
			Object[][] bucketsDst = copyOf(shape, shape.length);
			bucketsDst[0] = copyOfRange(shape[0], 1, shape[0].length);
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
		bucketsDst[0] = copyOfRange(shape[0], 1, 1 + bucketFillSize);

		int dstIdx = 1;
		count = shape[0].length - 1 - bucketFillSize;
		int inBucketIdx = bucketFillSize + 1;
		while (count > 0) {
			bucketFillSize *= FACTOR;
			if (bucketFillSize > count) {
				bucketFillSize = count;
			}
			bucketsDst[dstIdx] = copyOfRange(shape[0], inBucketIdx, inBucketIdx + bucketFillSize);
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
			return new FplList<E>(copyOf(shape, shape.length - 1));
		}
		if (shape[lastIdx].length <= BASE_SIZE + 1) {
			Object[][] bucketsDst = copyOf(shape, shape.length);
			bucketsDst[lastIdx] = copyOf(shape[lastIdx], shape[lastIdx].length - 1);
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
		bucketsDst[dstIdx] = copyOfRange(shape[lastIdx], inBucketIdx, inBucketIdx + bucketFillSize);

		dstIdx--;
		count = shape[lastIdx].length - 1 - bucketFillSize;
		while (count > 0) {
			bucketFillSize *= FACTOR;
			if (bucketFillSize > count) {
				bucketFillSize = count;
			}
			inBucketIdx -= bucketFillSize;
			bucketsDst[dstIdx] = copyOfRange(shape[lastIdx], inBucketIdx, inBucketIdx + bucketFillSize);
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
	 * @throws IllegalArgumentException If list is empty or if <code>position</code>
	 *                                  &lt; 0 or &gt;= {@link #size()}.
	 */
	@SuppressWarnings("unchecked")
	public E get(int position) {
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
		return (E) shape[bucketIdx][position - count];
	}

	/**
	 * @param position Position, starting with 0.
	 * @param element  New value at position.
	 * @return Updated list.
	 * @throws IllegalArgumentException If list is empty or if <code>position</code> &lt;
	 *                             0 or &gt;= {@link #size()}.
	 */
	public FplList<E> set(int position, E element){
		// General strategy is to copy all elements of a bucket, then - conditionally - overwrite
		// overwrite the one "element" to replace. Costs one write operation, gains simplicity.
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
		Object[][] newShape;
		int bucketSize = shape[bucketIdx].length;
		if (bucketSize <= BASE_SIZE) {
			// simple case: We hit a small bucket
			newShape = shape.clone();
			Object[] newBucket = shape[bucketIdx].clone();
			newBucket[position - count] = element;
			newShape[bucketIdx] = newBucket;
		} else {
			// we hit a large bucket, which has to be split
			int s = size();
			if (needsReshaping(shape.length + 1, s)) {
				int[] bucketSizes = computeBucketSizes(s);
				newShape = new Object[bucketSizes.length][];
				
				bucketIdx = 0;
				int inBucketIdx = 0;
				int dstBucketIdx = 0;
				int inBucketDstIdx = 0;
				count = 0;
				boolean overwritten = false;
				while (bucketIdx < shape.length) {
					int length = min(shape[bucketIdx].length - inBucketIdx, bucketSizes[dstBucketIdx] - inBucketDstIdx);
					if (inBucketDstIdx == 0) {
						newShape[dstBucketIdx] = copyOfRange(shape[bucketIdx], inBucketIdx, inBucketIdx + bucketSizes[dstBucketIdx]);
					} else {
						arraycopy(shape[bucketIdx], inBucketIdx, newShape[dstBucketIdx], inBucketDstIdx, length);
					}
					if (!overwritten && position - count < length) {
						newShape[dstBucketIdx][inBucketDstIdx + position - count] = element;
						overwritten = true;
					}
					inBucketIdx += length;
					if (inBucketIdx == shape[bucketIdx].length) {
						inBucketIdx = 0;
						bucketIdx++;
					}
					inBucketDstIdx += length;
					if (inBucketDstIdx == newShape[dstBucketIdx].length) {
						inBucketDstIdx = 0;
						dstBucketIdx++;
					}
					count += length;
				}
			} else {
				newShape = new Object[shape.length + 1][];
				// copy buckets before split bucket
				arraycopy(shape, 0, newShape, 0, bucketIdx);
				
				// split bucket (which has a length > BASE_SIZE)
				int leftSize = bucketSize / 2;
				int rightSize = bucketSize - leftSize;
				newShape[bucketIdx] = copyOf(shape[bucketIdx], leftSize);
				newShape[bucketIdx + 1] = new Object[rightSize];
				arraycopy(shape[bucketIdx], leftSize, newShape[bucketIdx+1], 0, rightSize);
				int inBucketIdx = position - count;
				if (inBucketIdx < leftSize) {
					newShape[bucketIdx][inBucketIdx] = element; 
				} else {
					newShape[bucketIdx + 1][inBucketIdx - leftSize] = element;
				}
				
				// copy buckets behind split bucket
				arraycopy(shape, bucketIdx + 1, newShape, bucketIdx + 2, shape.length - bucketIdx - 1);
			}
		}
		return new FplList<E>(newShape);
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
		// Copy buckets (before carry)
		Object[][] bucketsDst = copyOf(shape, bucketIdx + 2);

		// Collect carry
		Object[] carry = new Object[carrySize];
		bucketsDst[bucketsDst.length - 1] = carry;
		carry[carry.length - 1] = value;
		for (int i = bucketIdx + 1, dst = 0; i < shape.length; i++) {
			arraycopy(shape[i], 0, carry, dst, shape[i].length);
			dst += shape[i].length;
		}
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
				Object[][] buckets = copyOf(shape, shape.length + list.shape.length - 1);
				Object[] bucket = copyOf(lastBucket, lastBucket.length + listFirstBucket.length);
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
		int[] bucketSizes = computeBucketSizes(totalSize);
		Object[][] buckets = new Object[bucketSizes.length][];

		int bucketIdx = 0, inBucketIdx = 0, dstBucketIdx = 0, inBucketDstIdx = 0;
		// Copy entries from "left"
		while (bucketIdx < left.length) {
			int length = min(left[bucketIdx].length - inBucketIdx, bucketSizes[dstBucketIdx] - inBucketDstIdx);
			if (inBucketDstIdx == 0) {
				buckets[dstBucketIdx] = copyOfRange(left[bucketIdx], inBucketIdx, inBucketIdx + bucketSizes[dstBucketIdx]);
			} else {
				arraycopy(left[bucketIdx], inBucketIdx, buckets[dstBucketIdx], inBucketDstIdx, length);
			}
			inBucketIdx += length;
			if (inBucketIdx == left[bucketIdx].length) {
				inBucketIdx = 0;
				bucketIdx++;
			}
			inBucketDstIdx += length;
			if (inBucketDstIdx == buckets[dstBucketIdx].length) {
				inBucketDstIdx = 0;
				dstBucketIdx++;
			}
		}
		// Copy entries from "right"
		bucketIdx = 0;
		inBucketIdx = 0;
		while (bucketIdx < right.length) {
			int length = min(right[bucketIdx].length - inBucketIdx, bucketSizes[dstBucketIdx] - inBucketDstIdx);
			if (inBucketDstIdx == 0) {
				buckets[dstBucketIdx] = copyOfRange(right[bucketIdx], inBucketIdx, inBucketIdx + bucketSizes[dstBucketIdx]);
			} else {
				arraycopy(right[bucketIdx], inBucketIdx, buckets[dstBucketIdx], inBucketDstIdx, length);
			}
			inBucketIdx += length;
			if (inBucketIdx == right[bucketIdx].length) {
				inBucketIdx = 0;
				bucketIdx++;
			}
			inBucketDstIdx += length;
			if (inBucketDstIdx == buckets[dstBucketIdx].length) {
				inBucketDstIdx = 0;
				dstBucketIdx++;
			}
		}

		return buckets;
	}

	/**
	 * Create an array with bucket sizes for a list of "size". 
	 * Starting at both ends with size 3/4 * BASE_SIZE and
	 * increasing by FACTOR to the middle.
	 *
	 * @param size Size of the complete list.
	 * @return Array with bucket sizes.
	 */
	private static int[] computeBucketSizes(int size) {
		int numBuckets = 2;
		int bucketSize = 3 * BASE_SIZE / 4;
		int sizeInBuckets = 2 * bucketSize;
		while (sizeInBuckets < size) {
			bucketSize *= FACTOR;
			sizeInBuckets += 2 * bucketSize;
			numBuckets += 2;
		}
		numBuckets--;
		int[] bucketSizes = new int[numBuckets];
		bucketSize = BASE_SIZE;
		int rest = size;
		int i = 0, j = numBuckets - 1;
		while (i < j) {
			bucketSizes[i] = bucketSize / 2;
			bucketSizes[j] = bucketSize / 2;
			rest -= bucketSizes[i] + bucketSizes[j];
			bucketSize *= FACTOR;
			i++;
			j--;
		}
		bucketSizes[i] = rest;

		return bucketSizes;
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
				int size = min(bucketSize / 2, rest);
				bucketsDst[bucketDstIndex] = copyOfRange(bucket, inBucketFromIdx, inBucketFromIdx + size);
				bucketDstIndex++;
				inBucketFromIdx += size;
				rest -= size;
				bucketSize *= FACTOR;
			}
			bucketsDst[bucketDstIndex] =  copyOfRange(bucket, inBucketFromIdx, inBucketFromIdx + rest);
		}
	}

	private void createAndFillShapeFromRight(Object[] bucket, int inBucketToIdx, Object[][] bucketsDst) {
		if (inBucketToIdx == bucket.length) {
			bucketsDst[bucketsDst.length - 1] = bucket;
		} else {
			int bucketSize = 3 * BASE_SIZE / 4;
			int bucketDstIndex = bucketsDst.length - 1;
			int rest = inBucketToIdx;
			while (rest > bucketSize) {
				int size = min(bucketSize / 2, rest);
				bucketsDst[bucketDstIndex] = copyOfRange(bucket, inBucketToIdx - size, inBucketToIdx);
				bucketDstIndex--;
				inBucketToIdx -= size;
				rest -= size;
				bucketSize *= FACTOR;
			}
			bucketsDst[bucketDstIndex] = copyOf(bucket, rest);
		}
	}

	private int computeNumberOfBucketsLeft(Object[] Objects, int inBucketIdx) {
		if (inBucketIdx == 0) {
			return 1; // It is possible to copy the complete bucket by reference
		}
		return numBucketsForCount(Objects.length - inBucketIdx);
	}

	private int computeNumberOfBucketsRight(Object[] Objects, int inBucketIdx) {
		if (inBucketIdx == Objects.length) {
			return 1; // It is possible to copy the complete bucket by reference
		}
		return numBucketsForCount(inBucketIdx);
	}

	private int numBucketsForCount(int count) {
		if (count < 3 * BASE_SIZE / 4) {
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

	private FplList<E> subListFromOneLargeArray(Object[] objects, int first, int behindLast) {
		int size = behindLast - first;
		if (size <= BASE_SIZE) {
			Object[] b = copyOfRange(objects, first, first + size);
			Object[][] data = new Object[1][];
			data[0] = b;
			return new FplList<E>(data);
		} else {
			int[] bucketSizes = computeBucketSizes(size);
			Object[][] bucketsDst = new Object[bucketSizes.length][];
			for (int i = 0, bucketIdx = 0; bucketIdx < bucketsDst.length; bucketIdx++) {
				bucketsDst[bucketIdx] = copyOfRange(objects, first + i, first + i + bucketSizes[bucketIdx]);
				i += bucketSizes[bucketIdx];
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
			data = copyOf(shape,  fromBucketIdx);
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
					// so here no "if" necessary as for toBucketIdx
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
			data = copyOfRange(shape, fromBucketIdx, shape.length);  // new Object[shape.length - fromBucketIdx][];
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

	public <T> FplList<T> flatMap(Function<E, FplList<T>> operator) {
		
		return FplList.fromIterator(new Iterator<T>() {
			Iterator<E> listIter = iterator();
			Iterator<T> subListIter = null;

			@Override
			public boolean hasNext() {
				while (subListIter == null|| !subListIter.hasNext()) {
					if (listIter.hasNext()) {
						subListIter = operator.apply(listIter.next()).iterator();
						if (subListIter.hasNext()) {
							return true;
						} else {
							// Don't return, try listIter in next loop again.
							subListIter = null;
						}
					} else {
						return false;
					}
				}
				return subListIter.hasNext();
			}

			@Override
			public T next() {
				// We don't check if we have an element, fromIterator() is correct.
				return subListIter.next();
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> iterator() {
		if (isEmpty()) {
			return (Iterator<E>) EMPTY_ITERATOR;
		} else {
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
			sb.append(v == null ? "null" : v.toString());
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
