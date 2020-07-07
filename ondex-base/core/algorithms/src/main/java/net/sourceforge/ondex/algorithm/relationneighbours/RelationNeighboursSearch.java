package net.sourceforge.ondex.algorithm.relationneighbours;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;

/**
 * @author hindlem, Matthew Pocock
 */
public class RelationNeighboursSearch {

	/**
	 * Excution service
	 */
	private static ExecutorService EXECUTOR;

	/**
	 * Use all available threads
	 */
	private final static int maxJobs = Runtime.getRuntime()
			.availableProcessors();

	/**
	 * Parent ONDEX graph
	 */
	private final ONDEXGraph og;

	/**
	 * At what depth has concept been found
	 */
	private final Map<ONDEXConcept, Integer> foundConcepts;

	/**
	 * At what depth has relation been found
	 */
	private final Map<ONDEXRelation, Integer> foundRelations;

	private LogicalRelationValidator validator = null;

	private static final ReentrantLock lock = new ReentrantLock();

	/**
	 * @param og
	 *            the ONDEX graph
	 */
	public RelationNeighboursSearch(ONDEXGraph og) {

		if (EXECUTOR == null) {
			EXECUTOR = Executors.newFixedThreadPool(maxJobs);
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					if (EXECUTOR != null)
						EXECUTOR.shutdownNow();
				}
			});
		}

		this.og = og;

		// it is used in threads, so make it synchronise
		foundConcepts = Collections
				.synchronizedMap(new HashMap<ONDEXConcept, Integer>());
		foundRelations = Collections
				.synchronizedMap(new HashMap<ONDEXRelation, Integer>());
	}

	/**
	 * Explicitly shutdown the executor.
	 */
	public void shutdown() {
		EXECUTOR.shutdownNow();
		EXECUTOR = null;
	}

	/**
	 * @param validator
	 *            the validator to be used for all subsequent searches
	 */
	public void setValidator(LogicalRelationValidator validator) {
		this.validator = validator;
	}

	/**
	 * Depth is set at Integer.MAX_VALUE
	 * 
	 * @param concept
	 *            the seed concept
	 */
	public void search(ONDEXConcept concept) {
		search(concept, Integer.MAX_VALUE);
	}

	/**
	 * @param concept
	 *            the seed concept to begin depth at
	 * @param depth
	 *            the depth to search at (represents distance in relations)
	 */
	public void search(ONDEXConcept concept, int depth) {
		Set<ONDEXConcept> seeds = new HashSet<ONDEXConcept>();
		seeds.add(concept);
		search(seeds, depth);
	}

	/**
	 * Represents a faster search than individualy searching the graph as
	 * previously traversed segments are remembered. Seed is included in
	 * returned results. Depth is unlimited
	 * 
	 * @param seeds
	 *            the seeds to do a depth search on
	 */
	public void search(Set<ONDEXConcept> seeds) {
		search(seeds, Integer.MAX_VALUE);
	}

	/**
	 * Represents a faster search than individualy searching the graph as
	 * previously traversed segments are remembered.
	 * 
	 * @param seeds
	 *            the seeds to do a maxDepth search on
	 * @param maxDepth
	 *            the maxDepth to search at (represents distance in relations)
	 */
	public void search(Set<ONDEXConcept> seeds, int maxDepth) {
		foundConcepts.clear();
		foundRelations.clear();

		final BlockingQueue<State> states = new LinkedBlockingQueue<State>();
		for (ONDEXConcept c : seeds) {
			states.add(new State(0, c));
		}

		AtomicInteger counter = new AtomicInteger(0);
		List<Future<?>> futures = new ArrayList<Future<?>>();
		for (int i = 0; i < maxJobs; i++) {
			futures.add(EXECUTOR.submit(new RecursionStep(states, counter,
					maxDepth)));
		}

		for (Future<?> f : futures) {
			try {
				f.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				throw new Error(e);
			}
		}

		if (!states.isEmpty() || counter.get() > 0) {
			throw new RuntimeException(
					"Concurrent Error threads finished before work");
		}
	}

	/**
	 * Relations found in the last search (independent copy)
	 * 
	 * @return an Set of the relations
	 */
	public Set<ONDEXRelation> getFoundRelations() {
		return BitSetFunctions.unmodifiableSet(foundRelations.keySet());
	}

	/**
	 * Concepts found in the last search (independent copy)
	 * 
	 * @return an Set of the concepts
	 */
	public Set<ONDEXConcept> getFoundConcepts() {
		return BitSetFunctions.unmodifiableSet(foundConcepts.keySet());
	}

	private static class State {

		private final int depth;
		private final ONDEXConcept concept;

		private State(int depth, ONDEXConcept concept) {
			this.depth = depth;
			if (concept == null)
				throw new NullPointerException(
						"Null concepts not allowed here.");
			this.concept = concept;
		}
	}

	private class RecursionStep implements Runnable {
		private final BlockingQueue<State> states;
		private final int maxDepth;
		private final AtomicInteger counter;

		public RecursionStep(BlockingQueue<State> states,
				AtomicInteger counter, int maxDepth) {
			this.states = states;
			this.maxDepth = maxDepth;
			this.counter = counter;
		}

		@Override
		public void run() {
			try {
				while (counter.get() > 0 || !states.isEmpty()) {
					State s = states.poll(25, TimeUnit.MILLISECONDS);
					if (s != null) { // timeout
						counter.incrementAndGet();
						try {
							recurseState(s);
						} finally {
							counter.decrementAndGet();
						}
					}
				}
			} catch (InterruptedException e) {
				throw new Error(e);
			}
		}

		private void recurseState(State s) throws InterruptedException {
			int nextDepth = s.depth + 1;
			for (ONDEXRelation r : og.getRelationsOfConcept(s.concept)) {
				if ((foundRelations.get(r) == null || nextDepth < foundRelations
						.get(r))
						&& validateRelationAtDepth(r, nextDepth, s.concept)) {
					foundRelations.put(r, nextDepth);
					addAndRecurseConcept(r.getFromConcept(), nextDepth);
					addAndRecurseConcept(r.getToConcept(), nextDepth);
				}
			}
		}

		private void addAndRecurseConcept(ONDEXConcept concept, int nextDepth) {
			if (foundConcepts.get(concept) == null
					|| nextDepth < foundConcepts.get(concept)) {
				if (nextDepth < maxDepth) {
					states.add(new State(nextDepth, concept));
				}
				foundConcepts.put(concept, nextDepth);
			}
		}

		public boolean validateRelationAtDepth(ONDEXRelation relation,
				int currentPosition, ONDEXConcept conceptAtHead) {
			if (validator == null)
				return true;
			lock.lock();
			try {
				return validator.isValidRelationAtDepth(relation,
						currentPosition, conceptAtHead);
			} finally {
				lock.unlock();
			}
		}
	}
}
