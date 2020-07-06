package net.sourceforge.ondex.ovtk2.ui.console.functions;

import java.util.List;
import java.util.Set;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Type-safe Supplier constructors for Guava collections
 * 
 * @author lysenkoa
 * 
 */

public class DefaultSuppliers {

	private DefaultSuppliers() {

	};

	public static <T> Supplier<List<T>> list(Class<T> type) {
		return new Supplier<List<T>>() {
			public List<T> get() {
				return Lists.newArrayList();
			}
		};
	}

	public static <T> Supplier<Set<T>> set(Class<T> type) {
		return new Supplier<Set<T>>() {
			public Set<T> get() {
				return Sets.newHashSet();
			}
		};
	}
}
