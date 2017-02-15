package net.sourceforge.ondex.tools.functions;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.convertConceptClasses;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.convertRelationTypes;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createCC;
import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createRT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.subgraph.Subgraph;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.map.LazyMap;

/**
 * Useful reusable functions
 * 
 * @author lysenkoa
 */
public class StandardFunctions {


	public static String getAccession(ONDEXConcept c, DataSource cv) {
		for (ConceptAccession ac : c.getConceptAccessions()) {
			if (ac.getElementOf().equals(cv)) {
				return ac.getAccession();
			}
		}
		return null;
	}

	public static Set<String> getAccessions(ONDEXConcept c, DataSource cv) {
		Set<String> set = new HashSet<String>();
		for (ConceptAccession ac : c.getConceptAccessions()) {
			if (ac.getElementOf().equals(cv)) {
				set.add(ac.getAccession());
			}
		}
		return set;
	}

	public static Set<ONDEXRelation> getAllConnectingEdges(ONDEXGraph graph, ONDEXConcept a, ONDEXConcept b) {
		Set<ONDEXRelation> rs = new HashSet<ONDEXRelation>();
		for (ONDEXRelation z : graph.getRelationsOfConcept(a)) {
			if (b.equals(getOtherNode(a, z))) {
				rs.add(z);
			}
		}
		return rs;
	}

	public static Set<ONDEXConcept> getOtherNodesIncoming(ONDEXGraph graph, ONDEXConcept c, RelationType... types) {
		Set<RelationType> restriction = new HashSet<RelationType>();
		boolean performCheck = false;
		if (types != null && types.length > 0) {
			performCheck = true;
			restriction.addAll(Arrays.asList(types));
		}

		Set<ONDEXConcept> result = new HashSet<ONDEXConcept>();
		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			if (!r.getToConcept().equals(c)) {
				continue;
			}
			if (!performCheck || restriction.contains(r.getOfType())) {
				result.add(getOtherNode(c, r));
			}
		}
		return result;
	}

	// TODO

	public static void changeRelationDirection(ONDEXGraph graph, String originaRT, String newRT) {
		ONDEXGraphMetaData meta = graph.getMetaData();
		RelationType original = meta.getRelationType(originaRT);
		String invName = original.getInverseName();
		if (invName == null) {
			invName = newRT;
		} else {
			System.err.println("Inverse type found: " + invName);
		}
		RelationType inverse = createRT(graph, invName);

	}

	/**
	 * Converts a collection of concepts to an array of their ids
	 * 
	 * @param concepts
	 * @return an array of integer ids
	 */
	public static Integer[] entitesToIds(Collection<ONDEXConcept> concepts) {
		Integer[] result = new Integer[concepts.size()];
		int i = 0;
		for (ONDEXEntity c : concepts) {
			result[i] = c.getId();
			i++;
		}
		return result;
	}

	/**
	 * Searches in a graph for concepts with accessions matching a regex
	 * 
	 * @param regex
	 *            a valid Java regex (a pattern.matcher($accession).matches() is
	 *            done)
	 * @param graph
	 *            the graph to search concepts in
	 * @param exclusive
	 *            if true then return all concepts (from the whole graph) that
	 *            don't have matching accessions, else return all matching
	 *            concepts
	 * @param cc
	 *            the concept class of concepts to search in (can be null)
	 * @param concept_dataSource
	 *            the cv of concepts to search in (can be null)
	 * @param accession_dataSource
	 *            the cv of accessions to seach in (can be null)
	 * @author hindlem
	 */
	public static final Set<ONDEXConcept> filterConceptsOnAcessionRegex(String regex, ONDEXGraph graph, boolean exclusive, ConceptClass cc, DataSource concept_dataSource, DataSource accession_dataSource) {

		BitSet conceptsFound = new BitSet();

		Pattern pattern = Pattern.compile(regex);

		Set<ONDEXConcept> concepts = graph.getConcepts();

		if (cc != null) {
			BitSetFunctions.and(concepts, graph.getConceptsOfConceptClass(cc));
		}

		if (concept_dataSource != null) {
			BitSetFunctions.and(concepts, graph.getConceptsOfDataSource(concept_dataSource));
		}

		for (ONDEXConcept concept : concepts) {
			for (ConceptAccession accession : concept.getConceptAccessions()) {
				if (accession_dataSource != null && !accession.getElementOf().equals(accession_dataSource)) {
					continue;
				}
				if (pattern.matcher(accession.getAccession()).matches()) {
					conceptsFound.set(concept.getId());
					break;
				}
			}
		}

		Set<ONDEXConcept> matchedConcepts = BitSetFunctions.create(graph, ONDEXConcept.class, conceptsFound);

		if (exclusive)
			return BitSetFunctions.andNot(graph.getConcepts(), matchedConcepts);
		else
			return matchedConcepts;
	}

	public static Set<ONDEXConcept> filteroutUnconnected(ONDEXGraph graph, Set<ONDEXConcept> setOfConcepts, Set<ONDEXRelation> validRelations) {
		BitSet resultSet = new BitSet();
		for (ONDEXConcept c : setOfConcepts) {
			if (BitSetFunctions.and(graph.getRelationsOfConcept(c), validRelations).size() > 0)
				resultSet.set(c.getId());
		}
		Set<ONDEXConcept> result = BitSetFunctions.create(graph, ONDEXConcept.class, resultSet);
		return result;
	}

	/**
	 * Counts the gds ranking for a given graph
	 * 
	 * @param graph
	 * @param concepts
	 * @param attributeName
	 * @return
	 */
	public static final SortedMap<Integer, Object> gdsRanking(ONDEXGraph graph, Set<ONDEXConcept> concepts, String attributeName) {
		AttributeName att = graph.getMetaData().getAttributeName(attributeName);
		if (att == null)
			return new TreeMap<Integer, Object>();
		Map<Object, Integer> counts = new HashMap<Object, Integer>();
		for (ONDEXConcept c : concepts) {
			Attribute attribute = c.getAttribute(att);
			if (attribute != null) {
				Integer count = counts.get(attribute.getValue());
				if (count == null)
					count = 0;
				counts.put(attribute.getValue(), ++count);
			}
		}
		SortedMap<Integer, Object> sorted = new TreeMap<Integer, Object>(new Comparator<Integer>() {
			public int compare(Integer o1, Integer o2) {
				if (o1 > o2)
					return 1;
				if (o1 < o2)
					return -1;
				return 0;
			}
		});
		for (Entry<Object, Integer> ent : counts.entrySet()) {
			sorted.put(ent.getValue(), ent.getKey());
		}
		counts = null;
		return sorted;
	}

	/**
	 * Gets the list of all accessions of a particular cv from a concpet
	 * 
	 * @param c
	 *            - concept with accessions
	 * @param type
	 *            - type of accesions to extract
	 * @return
	 */
	public static final List<String> getAccessionsOfType(ONDEXConcept c, DataSource type) {
		List<String> accs = new LinkedList<String>();
		for (ConceptAccession a : c.getConceptAccessions()) {
			if (a.getElementOf().equals(type)) {
				accs.add(a.getAccession());
			}
		}
		return accs;
	}

	/**
	 * Gets all connected concepts and relations that are linked to the seed
	 * concept via valid relations
	 * 
	 * @param seed
	 * @param graph
	 * @param setOfTraversableRelations
	 * @return
	 */
	public static BitSet[] getAllConnected(ONDEXConcept seed, ONDEXGraph graph, Set<ONDEXRelation> setOfTraversableRelations) {
		BitSet[] result = new BitSet[2];
		result[0] = new BitSet(); // concepts
		result[0].set(seed.getId());
		result[1] = new BitSet(); // relations
		BitSet toProcess = new BitSet();
		toProcess.set(seed.getId());
		while (toProcess.cardinality() > 0) {
			BitSet toProcessNext = new BitSet();
			for (int i = toProcess.nextSetBit(0); i >= 0; i = toProcess.nextSetBit(i + 1)) {
				BitSet[] temp = getNeighbours(i, graph, setOfTraversableRelations);
				temp[0].andNot(result[0]);
				temp[1].andNot(result[1]);
				result[0].or(temp[0]);
				result[1].or(temp[1]);
				toProcessNext.or(temp[0]);
			}
			toProcess = toProcessNext;
		}
		return result;
	}

	/**
	 * @return Object2DoubleOpenHashMap<ONDEXConcept>
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 * @author taubertj Calculates betweenness centrality on the ONDEX graph.
	 *         Result returned as a map of concept to its betweennes centrality
	 *         value.
	 */
	public static final Map<ONDEXConcept, Double> getBetweenessCentrality(ONDEXGraph aog) throws NullValueException, AccessDeniedException {
		return getBetweenessCentrality(aog.getConcepts(), aog.getRelations(), aog);
	}

	/**
	 * @return Object2DoubleOpenHashMap<ONDEXConcept>
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 * @author taubertj Calculates betweenness centrality on the ONDEX graph.
	 *         Result returned as a map of concept to its betweennes centrality
	 *         value.
	 */
	public static final Map<ONDEXConcept, Double> getBetweenessCentrality(Set<ONDEXConcept> itc, Set<ONDEXRelation> incomingr, ONDEXGraph aog) throws NullValueException, AccessDeniedException {
		boolean filter = incomingr.size() != aog.getRelations().size();

		// CB[v] <- 0, v e V
		Map<ONDEXConcept, Double> CB = LazyMap.decorate(new HashMap<ONDEXConcept, Double>(), new Factory<Double>() {
			@Override
			public Double create() {
				return Double.valueOf(0.0);
			}
		});

		// iterate over all concepts
		for (ONDEXConcept concept : itc) {

			// S <- empty stack
			Stack<ONDEXConcept> S = new Stack<ONDEXConcept>();

			// P[w] <- empty list, w e V
			Map<ONDEXConcept, List<ONDEXConcept>> P = new Hashtable<ONDEXConcept, List<ONDEXConcept>>();

			// rho[t] <- 0, t e V
			Map<ONDEXConcept, Integer> rho = LazyMap.decorate(new HashMap<ONDEXConcept, Integer>(), new Factory<Integer>() {
				@Override
				public Integer create() {
					return Integer.valueOf(0);
				}
			});

			// rho[s] <- 1
			rho.put(concept, 1);

			// d[t] <- -1, t e V
			Map<ONDEXConcept, Integer> d = LazyMap.decorate(new HashMap<ONDEXConcept, Integer>(), new Factory<Integer>() {
				@Override
				public Integer create() {
					return Integer.valueOf(-1);
				}
			});

			// d[s] <- 0
			d.put(concept, 0);

			// Q <- empty queue
			LinkedBlockingQueue<ONDEXConcept> Q = new LinkedBlockingQueue<ONDEXConcept>();

			// enqueue s -> Q
			Q.offer(concept);

			// while Q not empty do
			while (!Q.isEmpty()) {

				// dequeue v <- Q
				ONDEXConcept v = Q.poll();

				// push v -> S
				S.push(v);

				Set<ONDEXRelation> itr = aog.getRelationsOfConcept(v);
				if (filter) {
					itr = BitSetFunctions.copy(itr);
					itr.retainAll(incomingr);
				}
				// foreach neighbor w of v do
				for (ONDEXRelation r : itr) {
					ONDEXConcept from = r.getFromConcept();
					ONDEXConcept to = r.getToConcept();
					ONDEXConcept w = null;
					if (!from.equals(v))
						w = from;
					else if (!to.equals(v))
						w = to;
					else
						continue; // or w = v;
					// w found for the first time?
					// if d[w] < 0 then
					if (d.get(w) < 0) {

						// enqueue w -> Q
						Q.offer(w);

						// d[w] <- d[v] + 1
						d.put(w, d.get(v) + 1);
					}
					// shortest path to w via v?
					// if d[w] = d[v] + 1
					if (d.get(w) == d.get(v) + 1) {

						// rho[w] <- rho[w] + rho[v]
						rho.put(w, rho.get(w) + rho.get(v));

						// append v -> P[w]
						if (!P.containsKey(w))
							P.put(w, new ArrayList<ONDEXConcept>());
						P.get(w).add(v);
					}
				}
			}

			// delta[v] <- 0, v e V
			Map<ONDEXConcept, Double> delta = LazyMap.decorate(new HashMap<ONDEXConcept, Double>(), new Factory<Double>() {
				@Override
				public Double create() {
					return Double.valueOf(0.0);
				}
			});

			// S returns vertices in order of non-increasing distance from s
			// while S not empty do
			while (!S.isEmpty()) {

				// pop w <- S
				ONDEXConcept w = S.pop();

				// for v e P[w] do
				if (P.containsKey(w)) {
					for (ONDEXConcept v : P.get(w)) {

						// delta[v] <- delta[v] + rho[v] / rho[w] * (1 +
						// delta[w])
						delta.put(v, delta.get(v) + ((double) rho.get(v) / (double) rho.get(w)) * (1.0 + delta.get(w)));
					}
				}

				// if w != s
				if (!w.equals(concept)) {

					// CB[w] <- CB[w] + delta[w]
					CB.put(w, CB.get(w) + delta.get(w));
				}
			}
		}

		// normalise results to [0,1]
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		ONDEXConcept[] keys = CB.keySet().toArray(new ONDEXConcept[0]);
		for (int i = 0; i < keys.length; i++) {
			double value = CB.get(keys[i]);
			if (value < min)
				min = value;
			if (value > max)
				max = value;
		}

		double diff = max - min;
		for (int i = 0; i < keys.length; i++) {
			CB.put(keys[i], (CB.get(keys[i]) - min) / diff);
		}

		return CB;
	}

	/**
	 * Construct a view of the concepts that have concept class with on of the
	 * supplied ids.
	 * 
	 * @param ids
	 *            - string concept class ids
	 * @return ondex view of concepts
	 */
	public static Set<ONDEXConcept> getConceptsByClassId(ONDEXGraph graph, String... ids) {
		Set<ONDEXConcept> result = BitSetFunctions.create(graph, ONDEXConcept.class, new BitSet());
		if (ids == null)
			return result;
		for (int i = 0; i < ids.length; i++) {
			result.addAll(graph.getConceptsOfConceptClass(createCC(graph, ids[i])));
		}
		return result;
	}

	/**
	 * This method will return all concept classes in a given OndexView. This
	 * method is non-destructive and will operate on a copy of a view, so it can
	 * be reused afterwards, but also must be closed, if no longer needed.
	 * 
	 * @return - a set of concept classes.
	 */
	public static final Set<ConceptClass> getContainedConceptClasses(Set<ONDEXConcept> concepts) {
		Set<ConceptClass> result = new HashSet<ConceptClass>();
		for (ONDEXConcept c : concepts) {
			result.add(c.getOfType());
		}
		return result;
	}

	/**
	 * Calculates degree centrality on the ONDEX graph. Result returned as a map
	 * of concept to its degree centrality value.
	 * 
	 * @return Object2DoubleOpenHashMap<ONDEXConcept>
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 */
	public static final Map<ONDEXConcept, Double> getDegreeCentrality(ONDEXGraph aog) throws NullValueException, AccessDeniedException {
		return getDegreeCentrality(aog.getConcepts(), aog.getRelations(), aog);
	}

	/**
	 * Calculates degree centrality on the ONDEX graph. Result returned as a map
	 * of concept to its degree centrality value.
	 * 
	 * @return Object2DoubleOpenHashMap<ONDEXConcept>
	 * @throws AccessDeniedException
	 * @throws NullValueException
	 */
	public static final Map<ONDEXConcept, Double> getDegreeCentrality(Set<ONDEXConcept> itc, Set<ONDEXRelation> itr, ONDEXGraph aog) throws NullValueException, AccessDeniedException {
		Map<ONDEXConcept, Double> conceptToDegree = new HashMap<ONDEXConcept, Double>();
		double normalisationFactor = itc.size() - 1;
		for (ONDEXConcept c : itc) {
			if (itr.size() == aog.getRelations().size())
				conceptToDegree.put(c, aog.getRelationsOfConcept(c).size() / normalisationFactor);
			else
				conceptToDegree.put(c, BitSetFunctions.and(aog.getRelationsOfConcept(c), itr).size() / normalisationFactor);
		}
		return conceptToDegree;
	}

	/**
	 * Returns all incoming relations of concept
	 * 
	 * @param graph
	 *            - graph
	 * @param c
	 *            - concept
	 * @param exclude
	 *            - ignore retalions of these types
	 * @return - set of relations
	 */

	public static final Set<ONDEXRelation> getIncomingRelations(ONDEXGraph graph, ONDEXConcept c, RelationType... exclude) {
		Set<ONDEXRelation> it = graph.getRelationsOfConcept(c);
		Set<RelationType> toExclude = new HashSet<RelationType>(Arrays.asList(exclude));
		Set<ONDEXRelation> result = new HashSet<ONDEXRelation>();
		for (ONDEXRelation r : it) {
			if (r.getToConcept().equals(c) && !toExclude.contains(r.getOfType()))
				result.add(r);
		}
		return result;
	}

	/**
	 * Returns all incoming relations of conceptc that connect it to one of the
	 * concepts of selected concept class(es)
	 * 
	 * @param graph
	 *            - graph
	 * @param c
	 *            - concept
	 * @return - set of relations
	 */
	public static final Set<ONDEXRelation> getIncomingRelationsToConceptClass(ONDEXGraph graph, ONDEXConcept c, ConceptClass... ccs) {
		Set<ONDEXRelation> it = graph.getRelationsOfConcept(c);
		Set<ONDEXRelation> result = new HashSet<ONDEXRelation>();
		Set<ConceptClass> test = new HashSet<ConceptClass>(Arrays.asList(ccs));
		for (ONDEXRelation r : it) {
			if (r.getToConcept().equals(c) && (test.size() == 0 || test.contains(r.getToConcept().getOfType())))
				result.add(r);
		}
		return result;
	}

	/**
	 * Reports intersections of the two sets: result[0] - unique entries in set
	 * one result[1] - intersection of one and two result[2] - unique entries in
	 * set two
	 * 
	 * @param one
	 * @param two
	 * @return
	 */
	public static final BitSet[] getIntersection(BitSet one, BitSet two) {
		BitSet[] result = new BitSet[3];
		result[0] = new BitSet();
		result[0].or(one);
		result[0].andNot(two);
		result[1] = new BitSet();
		result[1].or(one);
		result[1].and(two);
		result[2] = new BitSet();
		result[2].or(two);
		result[2].andNot(one);
		return result;
	}

	/**
	 * Returns a neighbourhood of the node. Relations are included in the
	 * subgraph if if they are of the matching relation type. Concepts are
	 * included if they are of matching concept class AND are connected via a
	 * relation of a matching relation type.
	 * <p/>
	 * If the relation types or concept classes supplied are null, all concept
	 * classes and relation types are matched.
	 * <p/>
	 * If either array is empty, the match will be set to match nothing; this
	 * option could be used to get just the relations, but not concepts.
	 * <p/>
	 * The seed concept is not included in the match.
	 * 
	 * @param graph
	 *            - graph to do the analysis in
	 * @param c
	 *            - seed concept
	 * @param rts
	 *            - relation types to match
	 * @param ccs
	 *            - concept classes to match
	 * @return - matching first level neighbourhood as a subgraph
	 */
	public static Subgraph getNeighbourhood(ONDEXGraph graph, ONDEXConcept c, RelationType[] rts, ConceptClass[] ccs) {
		Subgraph result = new Subgraph(graph);

		Set<RelationType> restrictionRT = new HashSet<RelationType>();
		boolean performRTCheck = false;
		if (rts != null) {
			performRTCheck = true;
			restrictionRT.addAll(Arrays.asList(rts));
		}

		Set<ConceptClass> restrictionCC = new HashSet<ConceptClass>();
		boolean performCCCheck = false;
		if (ccs != null) {
			performCCCheck = true;
			restrictionCC.addAll(Arrays.asList(ccs));
		}

		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			if (!performRTCheck || restrictionRT.contains(r.getOfType())) {
				result.addRelation(r);
				ONDEXConcept other = getOtherNode(c, r);
				if (!performCCCheck || restrictionCC.contains(other.getOfType())) {
					result.addConcept(other);
				}
			}
		}
		return result;
	}

	/**
	 * Returns a bitset of neighbouring concepts at position 0 and all relations
	 * at position 1
	 * 
	 * @param conceptID
	 * @param graph
	 * @return
	 */
	public static BitSet[] getNeighbours(int conceptID, ONDEXGraph graph) {
		return getNeighbours(graph.getConcept(conceptID), graph);
	}

	public static BitSet[] getNeighbours(int conceptID, ONDEXGraph graph, Set<ONDEXRelation> validRelations) {
		return getNeighbours(graph.getConcept(conceptID), graph, validRelations);
	}

	/**
	 * Returns an array of two bitset of neighbouring concepts at array position
	 * 0 and all relations at array position 1
	 * 
	 * @param seed
	 * @param graph
	 * @return
	 */
	public static BitSet[] getNeighbours(ONDEXConcept seed, ONDEXGraph graph) {
		BitSet[] result = new BitSet[2];
		result[0] = new BitSet(); // concepts
		result[1] = new BitSet(); // relations
		for (ONDEXRelation r : graph.getRelationsOfConcept(seed)) {
			result[0].set(StandardFunctions.getOtherNode(seed, r).getId());
			result[1].set(r.getId());
		}
		return result;
	}

	/**
	 * Return all neighbours for a seed concept
	 * 
	 * @param seed
	 * @param graph
	 * @param validRelations
	 * @return
	 */
	public static BitSet[] getNeighbours(ONDEXConcept seed, ONDEXGraph graph, Set<ONDEXRelation> validRelations) {
		BitSet[] result = new BitSet[2];
		result[0] = new BitSet(); // concepts
		result[1] = new BitSet(); // relations
		for (ONDEXRelation r : graph.getRelationsOfConcept(seed)) {
			if (!validRelations.contains(r)) {
				continue;
			}
			result[0].set(StandardFunctions.getOtherNode(seed, r).getId());
			result[1].set(r.getId());
		}
		return result;
	}

	/**
	 * Returns a bitset of neighbouring concepts at position 0 and all relations
	 * at position 1
	 * 
	 * @param seed
	 * @param graph
	 * @return
	 */
	public static BitSet[] getNeighboursAtLevel(ONDEXConcept seed, ONDEXGraph graph, int level) {
		BitSet[] result = new BitSet[2];
		result[0] = new BitSet(); // concepts
		result[1] = new BitSet(); // relations
		BitSet toProcess = new BitSet();
		toProcess.set(seed.getId());
		while (level > 0) {
			BitSet toProcessNext = new BitSet();
			for (int i = toProcess.nextSetBit(0); i >= 0; i = toProcess.nextSetBit(i + 1)) {
				BitSet[] temp = getNeighbours(i, graph);
				temp[0].andNot(result[0]);
				temp[1].andNot(result[1]);
				result[0].or(temp[0]);
				result[1].or(temp[1]);
				toProcessNext.or(temp[0]);
				if (toProcessNext.cardinality() == 0)
					;
				break;
			}
			toProcess = toProcessNext;
			level--;
		}

		return result;
	}

	/**
	 * Returns a bitset of neighbouring concepts at position 0 and all relations
	 * at position 1
	 * 
	 * @param seed
	 * @param graph
	 * @return
	 */
	public static BitSet[] getNeighboursAtLevel(ONDEXConcept seed, ONDEXGraph graph, Set<ONDEXRelation> setOfTraversableRelations, int level) {
		BitSet[] result = new BitSet[2];
		result[0] = new BitSet(); // concepts
		result[1] = new BitSet(); // relations
		BitSet toProcess = new BitSet();
		toProcess.set(seed.getId());
		while (level > 0) {
			BitSet toProcessNext = new BitSet();
			for (int i = toProcess.nextSetBit(0); i >= 0; i = toProcess.nextSetBit(i + 1)) {
				BitSet[] temp = getNeighbours(i, graph, setOfTraversableRelations);
				result[0].or(temp[0]);
				result[1].or(temp[1]);
				toProcessNext.or(temp[0]);
			}
			if (toProcessNext.cardinality() == 0)
				break;
			toProcess = toProcessNext;
			level--;
		}

		return result;
	}

	/**
	 * Returns one or more concepts of specified concept class(s), that are
	 * associated with this relations
	 * 
	 * @param r
	 *            - relation
	 * @param ccs
	 *            - concept classes
	 * @return - an array of concepts that satisfy this condition
	 */
	public static ONDEXConcept[] getNodesByConceptClass(ONDEXRelation r, Collection<ConceptClass> ccs) {
		Set<ONDEXConcept> set = new HashSet<ONDEXConcept>();
		ONDEXConcept c = r.getFromConcept();
		if (ccs.contains(c.getOfType()))
			set.add(c);
		c = r.getToConcept();
		if (ccs.contains(c.getOfType()))
			set.add(c);
		return set.toArray(new ONDEXConcept[set.size()]);
	}

	/**
	 * Returns a concept on the opposite end of the relation to the one
	 * specified.
	 * 
	 * @param c
	 *            - concept on the opposite side of the relation to the one
	 *            needed
	 * @param r
	 *            - relation connecting them
	 * @return concept on the other end of the specified relation
	 */
	public static ONDEXConcept getOtherNode(ONDEXConcept c, ONDEXRelation r) {
		ONDEXConcept to = r.getToConcept();
		if (c.equals(to))
			return r.getFromConcept();
		return to;
	}

	/**
	 * Returns all concepts that are directly connected to the one specified
	 * Optional: provide one or more relation types as a restriction. Only the
	 * nodes that are connected by the relations of the following types will be
	 * returned.
	 * 
	 * @param graph
	 *            - graph
	 * @param c
	 *            - seed concept
	 * @return - set of concepts directly connected to the seed concept
	 */
	public static Set<ONDEXConcept> getOtherNodes(ONDEXGraph graph, ONDEXConcept c, RelationType... types) {
		Set<RelationType> restriction = new HashSet<RelationType>();
		boolean performCheck = false;
		if (types != null && types.length > 0) {
			performCheck = true;
			restriction.addAll(Arrays.asList(types));
		}
		Set<ONDEXConcept> result = new HashSet<ONDEXConcept>();
		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			if (!performCheck || restriction.contains(r.getOfType())) {
				result.add(getOtherNode(c, r));
			}
		}
		return result;
	}

	/**
	 * Returns all outgoing relations of concept
	 * 
	 * @param graph
	 *            - graph
	 * @param c
	 *            - concept
	 * @param exclude
	 *            - ignore retalions of these types
	 * @return - set of relations
	 */
	public static final Set<ONDEXRelation> getOutgoingRelations(ONDEXGraph graph, ONDEXConcept c, RelationType... exclude) {
		Set<RelationType> toExclude = new HashSet<RelationType>(Arrays.asList(exclude));
		Set<ONDEXRelation> result = new HashSet<ONDEXRelation>();
		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			if (r.getFromConcept().equals(c) && !toExclude.contains(r.getOfType()))
				result.add(r);
		}
		return result;
	}

	/**
	 * Returns all outgoing relations of concept c that connect it to one of the
	 * concepts of selected concept class(es)
	 * 
	 * @param graph
	 *            - graph
	 * @param c
	 *            - concept
	 * @return - set of relations
	 */
	public static final Set<ONDEXRelation> getOutgoingRelationsToConceptClass(ONDEXGraph graph, ONDEXConcept c, ConceptClass... ccs) {
		Set<ONDEXRelation> result = new HashSet<ONDEXRelation>();
		Set<ConceptClass> test = new HashSet<ConceptClass>(Arrays.asList(ccs));
		for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
			if (r.getFromConcept().equals(c) && (test.size() == 0 || test.contains(r.getToConcept().getOfType())))
				result.add(r);
		}
		return result;
	}

	/**
	 * Constructs the view containing all of the relations of particular types,
	 * specified by their string ids.
	 * 
	 * @param graph
	 *            - graph to operate on
	 * @param types
	 *            - string ids of the relation types that will be used to
	 *            determine which relation to include in the view.
	 * @return - resulting view
	 */
	public static Set<ONDEXRelation> getRelationsByTypes(ONDEXGraph graph, List<String> types) {
		Set<ONDEXRelation> result = BitSetFunctions.create(graph, ONDEXRelation.class, new BitSet());
		ONDEXGraphMetaData meta = graph.getMetaData();
		for (String type : types) {
			RelationType rt = meta.getRelationType(type);
			if (rt != null) {
				result.addAll(graph.getRelationsOfRelationType(rt));
			}
		}
		return result;
	}

	/**
	 * Returns a list of subgraphs that match the pattern of concept
	 * classes/relation types supplied as arguments. To work correctly the set
	 * of arguments must always start with an array of string ids for concept
	 * classes. Every concept that matches the concept classes in the first
	 * array will become a seed for a subgraph returned in the result. Graph
	 * entities may be members of multiple subgraphs, if they are part of more
	 * than one path.
	 * 
	 * @return - a list of subgraphs that answer the query.
	 */
	public static List<Subgraph> getSubgraphMatch(ONDEXGraph graph, String[]... ccsRts) {
		List<Subgraph> result = new ArrayList<Subgraph>();
		if (ccsRts == null || ccsRts.length == 0)
			return result;
		for (ONDEXConcept seed : getConceptsByClassId(graph, ccsRts[0])) {
			Subgraph sg = new Subgraph(graph);
			result.add(sg);
			sg.addConcept(seed);
			Set<ONDEXConcept> candidates = new HashSet<ONDEXConcept>();
			candidates.add(seed);
			for (int i = 1; i < ccsRts.length; i = i + 2) {
				String[] relationTypes = ccsRts[i];
				String[] conceptClasses = null;
				if (i + 1 < ccsRts.length) {
					conceptClasses = ccsRts[i + 1];
				}
				Set<ONDEXConcept> newCandidates = new HashSet<ONDEXConcept>();
				for (ONDEXConcept newSeed : candidates) {
					Subgraph temp = getNeighbourhood(graph, newSeed, convertRelationTypes(graph, relationTypes), convertConceptClasses(graph, conceptClasses));
					newCandidates.addAll(temp.getConcepts());
					sg.add(temp);
				}
				candidates = newCandidates;
			}

		}

		return result;
	}

	/**
	 * Checks the one of the collections for a presence of any of the elements
	 * from another collection
	 * 
	 * @param toMatch
	 *            - reference collection
	 * @param toCheck
	 *            - collection that will be checked for the presence of matches
	 * @return true if an element was found , false otherwise
	 */
	public static final boolean hasMatch(Collection<?> toMatch, Collection<?> toCheck) {
		for (Object o : toCheck) {
			if (toMatch.contains(o))
				return true;
		}
		return false;
	}

	/**
	 * StandardFunctions.nullChecker A convenience method to check groups of
	 * arguments to intercept null values in order to avoid annoying NullValue
	 * exceptions
	 * 
	 * @param objs
	 *            - values to check
	 * @return true if null found, false otherwise
	 */

	public static boolean nullChecker(Object... objs) {
		for (Object o : objs) {
			if (o == null)
				return true;
		}
		return false;
	}

	/**
	 * Return all source concepts for a given set of relations. If the concept
	 * is bout a source an a target it will be omitted
	 * 
	 * @param rs
	 *            - set of relations
	 * @return - set of source concepts
	 */
	public static final Set<ONDEXConcept> relationsToSources(Set<ONDEXRelation> rs) {
		Set<ONDEXConcept> result = new HashSet<ONDEXConcept>();
		for (ONDEXRelation r : rs) {
			ONDEXConcept from = r.getFromConcept();
			ONDEXConcept to = r.getToConcept();
			if (to != from)
				result.add(from);
		}

		return result;
	}

	/**
	 * Return all target concepts for a given set of relations. If the concept
	 * is bout a source an a target it will be omitted
	 * 
	 * @param rs
	 *            - set of relations
	 * @return - set of target concepts
	 */
	public static final Set<ONDEXConcept> relationsToTargets(Set<ONDEXRelation> rs) {
		Set<ONDEXConcept> result = new HashSet<ONDEXConcept>();
		for (ONDEXRelation r : rs) {
			ONDEXConcept from = r.getFromConcept();
			ONDEXConcept to = r.getToConcept();
			if (to != from)
				result.add(to);
		}

		return result;
	}

	/**
	 * Gets the sparse bit set from the view. The view is used up and closed
	 * when this method is called.
	 * 
	 * @param view
	 *            ondex view
	 * @return sparse bit set of ids
	 */
	public static BitSet viewToBitSet(Set<ONDEXEntity> view) {
		BitSet set = new BitSet();
		for (ONDEXEntity oe : view) {
			set.set(oe.getId());
		}
		return set;
	}

	private StandardFunctions() {
	}

}
