package net.sourceforge.ondex.core.util;

import java.util.AbstractSet;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;

import org.apache.commons.collections15.set.UnmodifiableSet;

public class BitSetFunctions {

	/**
	 * Provides a iterator type view based on BitSets on all Objects which
	 * implement Viewable (AbstractConcept and AbstractRelation).
	 * 
	 * @author taubertj
	 * @param <AnyType>
	 */
	private abstract static class ONDEXSet<AnyType extends ONDEXEntity> extends
			AbstractSet<AnyType> implements Set<AnyType>, Cloneable {

		/**
		 * Represents view of participating AbstractConcept or AbstractRelation
		 * IDs.
		 */
		private BitSet set;

		/**
		 * Constructors initialises all internal variables for a given class and
		 * BitSet.
		 * 
		 * @param set
		 *            SparseBitSet
		 */
		private ONDEXSet(BitSet set) {
			this.set = set;
			if (set == null)
				throw new NullPointerException(
						"Null set used in ONDEXSet constructor");
		}

		@Override
		public boolean add(AnyType c) {
			boolean isSet = set.get(c.getId());
			set.set(c.getId());
			return isSet;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean addAll(Collection<? extends AnyType> c) {
			if (c instanceof ONDEXSet) {
				int size = set.cardinality();
				set.or(((ONDEXSet) c).set);
				return size != set.cardinality();
			} else {
				return super.addAll(c);
			}
		}

		/**
		 * Returns a deep clone of the set but clones only the reference to
		 * graph and Class (obviously)
		 */
		@SuppressWarnings({ "unchecked" })
		@Override
		public ONDEXSet<AnyType> clone() {
			try {
				ONDEXSet<AnyType> newImpl = (ONDEXSet<AnyType>) super.clone();

				newImpl.set = (BitSet) set.clone();

				return newImpl;
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());
			}
		}

		/**
		 * returns false if the element is not contained in the view or if the
		 * user has no permission to view the element.
		 * 
		 * @throws net.sourceforge.ondex.exception.type.AccessDeniedException
		 *             if the object parameter does not provide read permission.
		 */
		@Override
		public boolean contains(Object o) {
			if (o != null && getDataType().isAssignableFrom(o.getClass())) {
				if (o instanceof ONDEXEntity) {
					ONDEXEntity e = (ONDEXEntity) o;
					return set.get(e.getId());
				}
			}
			return false;
		}

		/**
		 * Checks for all objects of a given Collection to be contained in this
		 * iterator.
		 * 
		 * @param c
		 *            Collection<?>
		 * @return boolean
		 * @throws net.sourceforge.ondex.exception.type.AccessDeniedException
		 */
		@Override
		public boolean containsAll(Collection<?> c) {
			if (size() == 0)
				return false;
			for (Object aC : c) {
				if (!contains(aC)) {
					return false;
				}
			}
			return true;
		}

		/**
		 * Returns true if this collection contains no elements.
		 * 
		 * @return true if this collection contains no elements
		 */
		@Override
		public boolean isEmpty() {
			return (size() == 0);
		}

		@Override
		public Iterator<AnyType> iterator() {
			return new Iterator<AnyType>() {

				private int currentIndex = -1;
				private int nextIndex = set.nextSetBit(0);

				@Override
				public boolean hasNext() {
					return nextIndex > -1;
				}

				@Override
				public AnyType next() {
					currentIndex = nextIndex;
					nextIndex = set.nextSetBit(currentIndex + 1);
					return reallyGetEntity(currentIndex);
				}

				@Override
				public void remove() {
					if (currentIndex > -1)
						set.clear(currentIndex);
					else
						throw new IllegalStateException();
				}
			};
		}

		@Override
		public boolean remove(Object obj) {
			if (obj instanceof ONDEXEntity) {
				boolean exists = set.get(((ONDEXEntity) obj).getId());
				set.clear(((ONDEXEntity) obj).getId());
				return exists;
			}
			return false;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean removeAll(Collection<?> c) {
			if (c instanceof ONDEXSet) {
				int size = set.cardinality();
				set.andNot(((ONDEXSet) c).set);
				return size != set.cardinality();
			} else {
				return super.removeAll(c);
			}
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean retainAll(Collection<?> c) {
			if (c instanceof ONDEXSet) {
				int size = set.cardinality();
				set.and(((ONDEXSet) c).set);
				return size != set.cardinality();
			} else {
				return super.retainAll(c);
			}
		}

		@Override
		public int size() {
			return set.cardinality();
		}

		protected abstract Class<AnyType> getDataType();

		protected abstract AnyType reallyGetEntity(int i);
	}

	/**
	 * Return "and" of two given Set.
	 * 
	 * @param view1
	 *            Set<T>
	 * @param view2
	 *            Set<T>
	 * @return Set<T>
	 */
	public static <T extends ONDEXEntity> Set<T> and(Set<T> view1, Set<T> view2) {
		Set<T> newset = copy(view1);
		newset.retainAll(view2);
		return newset;
	}

	/**
	 * Return new set containing "andNot" of two given Set.
	 * 
	 * @param view1
	 *            Set<T>
	 * @param view2
	 *            Set<T>
	 * @return Set<T>
	 */
	public static <T extends ONDEXEntity> Set<T> andNot(Set<T> view1,
			Set<T> view2) {
		Set<T> newset = copy(view1);
		newset.removeAll(view2);
		return newset;
	}

	/**
	 * Copy given set.
	 * 
	 * @param <E>
	 * @param set
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <E> Set<E> copy(Set<E> set) {
		if (set instanceof ONDEXSet) {
			ONDEXSet sat = (ONDEXSet) set;
			return sat.clone();
		} else {
			return new HashSet<E>(set);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <AnyType extends ONDEXEntity> Set<AnyType> create(
			final ONDEXGraph aog, Class<AnyType> c, BitSet set) {
		if (ONDEXConcept.class.equals(c)) {
			return (Set<AnyType>) new ONDEXSet<ONDEXConcept>(set) {
				@Override
				public Class<ONDEXConcept> getDataType() {
					return ONDEXConcept.class;
				}

				@Override
				protected ONDEXConcept reallyGetEntity(int i) {
					return aog.getConcept(i);
				}
			};
		} else if (ONDEXRelation.class.equals(c)) {
			return (Set<AnyType>) new ONDEXSet<ONDEXRelation>(set) {
				@Override
				public Class<ONDEXRelation> getDataType() {
					return ONDEXRelation.class;
				}

				@Override
				protected ONDEXRelation reallyGetEntity(int i) {
					return aog.getRelation(i);
				}
			};
		} else {
			throw new ClassCastException("Can't instantiate ONDEXSet for: " + c);
		}
	}

	@SuppressWarnings("unchecked")
	public static <AnyType extends ONDEXEntity> Set<AnyType> create(
			final ONDEXGraph aog, Class<AnyType> c, Set<Integer> set) {
		
		// copy content of set to BitSet, will be optimised later
		BitSet bs = new BitSet();
		for (Integer i : set)
			bs.set(i);
		
		if (ONDEXConcept.class.equals(c)) {
			return (Set<AnyType>) new ONDEXSet<ONDEXConcept>(bs) {
				@Override
				public Class<ONDEXConcept> getDataType() {
					return ONDEXConcept.class;
				}

				@Override
				protected ONDEXConcept reallyGetEntity(int i) {
					return aog.getConcept(i);
				}
			};
		} else if (ONDEXRelation.class.equals(c)) {
			return (Set<AnyType>) new ONDEXSet<ONDEXRelation>(bs) {
				@Override
				public Class<ONDEXRelation> getDataType() {
					return ONDEXRelation.class;
				}

				@Override
				protected ONDEXRelation reallyGetEntity(int i) {
					return aog.getRelation(i);
				}
			};
		} else {
			throw new ClassCastException("Can't instantiate ONDEXSet for: " + c);
		}
	}

	/**
	 * Return "or" of two given Set.
	 * 
	 * @param view1
	 *            Set<T>
	 * @param view2
	 *            Set<T>
	 * @return Set<T>
	 */
	public static <T extends ONDEXEntity> Set<T> or(Set<T> view1, Set<T> view2) {
		Set<T> newset = copy(view1);
		newset.addAll(view2);
		return newset;
	}

	/**
	 * Returns an unmodifiable set for the given set. If the given set is
	 * already instance of UnmodifiableSet, simply return it.
	 * 
	 * @param <E>
	 *            generic type
	 * @param set
	 *            Set to wrap in UnmodifiableSet
	 * @return UnmodifiableSet
	 */
	public static <E> Set<E> unmodifiableSet(Set<E> set) {
		if (set == null)
			return UnmodifiableSet.decorate(Collections.<E> emptySet());
		if (set instanceof UnmodifiableSet)
			return set;
		return UnmodifiableSet.decorate(set);
	}
}
