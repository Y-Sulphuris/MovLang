package com.ydo4ki.movlang.misc;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

// no idea how to name this class properly

/**
 * @author Sulphuris
 * @since 05.10.2024 18:54
 */
public abstract class OneOrMore<T> implements Iterable<T> {

	public static <T> OneOrMore<T> of(T value) {
		return new One<>(value);
	}

	public static <T> OneOrMore<T> of(OneOrMore<T> oom, T value) {
		ArrayList<T> list = new ArrayList<>();
		list.add(value);
		for (T t : oom) {
			list.add(t);
		}
		return of(list);
	}

	@SafeVarargs
	public static <T> OneOrMore<T> of(T... value) {
		return new MoreA<>(value);
	}

	public static <T> OneOrMore<T> of(Collection<T> value) {
		return new MoreC<>(value);
	}

	public abstract int count();

	static class One<T> extends OneOrMore<T> {
		private final T value;

		One(T value) {
			this.value = value;
		}

		@NotNull
		@Override
		public Iterator<T> iterator() {
			return new Iterator<T>() {
				boolean hasNext = true;

				@Override
				public boolean hasNext() {
					return hasNext;
				}

				@Override
				public T next() {
					hasNext = false;
					return value;
				}
			};
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		@Override
		public int count() {
			return 1;
		}
	}

	static class MoreA<T> extends OneOrMore<T> {
		final T[] values;

		MoreA(T[] values) {
			this.values = values;
		}

		@NotNull
		@Override
		public Iterator<T> iterator() {
			return new Iterator<T>() {
				int i = 0;

				@Override
				public boolean hasNext() {
					return i <= values.length;
				}

				@Override
				public T next() {
					return values[i++];
				}
			};
		}

		@Override
		public String toString() {
			return Arrays.toString(values);
		}

		@Override
		public int count() {
			return values.length;
		}
	}

	@SuppressWarnings("unchecked")
	static class MoreC<T> extends OneOrMore<T> {
		final Object[] values;

		MoreC(Collection<T> c) {
			this.values = c.toArray();
		}


		@NotNull
		@Override
		public Iterator<T> iterator() {
			return new Iterator<T>() {
				int i = 0;

				@Override
				public boolean hasNext() {
					return i <= values.length;
				}

				@Override
				public T next() {
					return (T) values[i++];
				}
			};
		}

		@Override
		public String toString() {
			return Arrays.toString(values);
		}

		@Override
		public int count() {
			return values.length;
		}
	}
}
