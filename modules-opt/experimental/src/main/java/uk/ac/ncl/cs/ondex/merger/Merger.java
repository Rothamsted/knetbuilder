/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.ncl.cs.ondex.merger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.transformer.ONDEXTransformer;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.exception.type.MetaDataMissingException;
import org.apache.log4j.Logger;
import uk.ac.ncl.cs.ondex.merger.ConfigReader.Configuration;
import uk.ac.ncl.cs.ondex.tools.ConsoleProgressBar;
import static uk.ac.ncl.cs.ondex.merger.MergerTools.*;

/**
 * 
 * @author jweile
 */
public class Merger extends ONDEXTransformer {

	public static final String CONFIG_ARG = "config";
	public static final String CONFIG_ARG_DESC = "Configuration Syntax: methodName(argument=value,argument=value). "
			+ "Available methods and their arguments: "
			+ ConfigReader.LinkByAccession.HELP
			+ ConfigReader.MergeByAccession.HELP
			+ ConfigReader.MergeByNeighbourhood.HELP;

	/*
	 * ##################### ##### ARGUMENTS ##### #####################
	 */

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition<?>[] { new StringArgumentDefinition(
				CONFIG_ARG, CONFIG_ARG_DESC, true, null, true) };
	}

	/*
	 * #################### ##### METADATA ##### ####################
	 */

	private RelationType rtPartOf;
	private AttributeName anSources;
	private EvidenceType etTrans, etAcc;
	private DataSource dataSourceUnknown;

	private void initMetadata() throws MetaDataMissingException {

		rtPartOf = requireRelationType("is_part_of");
		anSources = requireAttributeName("DataSources");
		dataSourceUnknown = requireDataSource("unknown");
		etTrans = requireEvidenceType("InferredByTransformation");
		etAcc = requireEvidenceType("ACC");

	}

	/*
	 * ################### ##### MERGERS ##### ###################
	 */
	private DataMerger<String> pidMerger, annoMerger, descMerger, sourceMerger;
	private SetMerger<EvidenceType> etMerger;
	private SetMerger<ConceptAccession> accMerger;
	private SetMerger<ConceptName> nameMerger;
	// private SetMerger<Attribute> gdsMerger;
	private SetMerger<ONDEXConcept> contextMerger;

	private void initMergers() {
		pidMerger = new DataMerger<String>(graph) {
			@Override
			public String extractData(ONDEXConcept c) {
				return c.getPID();
			}
		};
		annoMerger = new DataMerger<String>(graph) {
			@Override
			public String extractData(ONDEXConcept c) {
				return c.getAnnotation();
			}
		};
		descMerger = new DataMerger<String>(graph) {
			@Override
			public String extractData(ONDEXConcept c) {
				return c.getDescription();
			}
		};
		sourceMerger = new DataMerger<String>(graph) {
			@Override
			public String extractData(ONDEXConcept c) {
				return c.getElementOf().getFullname();
			}
		};
		etMerger = new SetMerger<EvidenceType>(graph) {
			@Override
			public Set<EvidenceType> extractSet(ONDEXConcept c) {
				return c.getEvidence();
			}
		};
		accMerger = new SetMerger<ConceptAccession>(graph) {
			@Override
			public Set<ConceptAccession> extractSet(ONDEXConcept c) {
				return c.getConceptAccessions();
			}
		};
		nameMerger = new SetMerger<ConceptName>(graph) {
			@Override
			public Set<ConceptName> extractSet(ONDEXConcept c) {
				return c.getConceptNames();
			}
		};
		// gdsMerger = new SetMerger<Attribute>(graph) {
		// @Override
		// public Set<Attribute> extractSet(ONDEXConcept c) {
		// return c.getGDSs();
		// }
		// };
		contextMerger = new SetMerger<ONDEXConcept>(graph) {
			@Override
			public Set<ONDEXConcept> extractSet(ONDEXConcept c) {
				return c.getTags();
			}
		};
	}

	private Logger logger = Logger.getLogger(this.getClass());

	/*
	 * ########################## ##### MAIN PROCEDURE #####
	 * ##########################
	 */

	@Override
	public void start() throws Exception {

		initMergers();
		initMetadata();

		ConfigReader cr = new ConfigReader(graph);

		for (String configString : getArguments().getObjectValueList(
				CONFIG_ARG, String.class)) {

			Configuration config = cr.readConfiguration(configString);

			config.print(logger);

			if (config instanceof ConfigReader.LinkByAccession) {

				ConfigReader.LinkByAccession lba = (ConfigReader.LinkByAccession) config;
				linkByAccession(lba.getFromCC(), lba.getToCC(), lba.getRt(),
						lba.getNs());

			} else if (config instanceof ConfigReader.MergeByAccession) {

				ConfigReader.MergeByAccession mba = (ConfigReader.MergeByAccession) config;
				mergeByAccession(mba.getTargetCC(), mba.getNs(),
						mba.isSetBasedMatching(), mba.isAllowMultiples(),
						mba.isAllowMissing());

			} else if (config instanceof ConfigReader.MergeByNeighbourhood) {

				ConfigReader.MergeByNeighbourhood mbn = (ConfigReader.MergeByNeighbourhood) config;
				mergeByContext(mbn.getTargetCC(), mbn.getNeighbourCC());

			}

		}

	}

	/**
	 * Links concepts of <code>ConceptClass fromClass</code> to concepts of
	 * <code>ConceptClass toClass</code> with relations of
	 * <code>RelationType rt</code> if they share accessions in the namespace
	 * <code>DataSource accNameSpace</code>. Depending on the relation type, the
	 * complete set of accessions needs to match ("equ" mode), or the only
	 * accession in the from-concept needs to match at least one of the
	 * accessions in the to-concept ("is_part_of" mode).
	 */
	private void linkByAccession(ConceptClass fromClass, ConceptClass toClass,
			RelationType rt, DataSource accNameSpace) {

		boolean eqMode = !rtPartOf.isAssignableFrom(rt);

		AccessionIndex fromIndex = new AccessionIndex(graph);
		fromIndex.indexByAccession(fromClass, accNameSpace, eqMode, eqMode,
				false);
		AccessionIndex toIndex = new AccessionIndex(graph);
		toIndex.indexByAccession(toClass, accNameSpace, eqMode, true, false);

		log("Linking " + fromClass.getFullname() + " to "
				+ toClass.getFullname() + "...");

		int count = 0;

		for (String acc : fromIndex.keySet()) {
			MergeList fromCs = fromIndex.get(acc);
			check(eqMode || fromCs.size() == 1,
					"More than one " + fromClass.getFullname()
							+ " for accession " + acc + " after merging!");

			MergeList toCs = toIndex.get(acc);
			if (toCs != null) {
				for (ONDEXConcept fromC : fromCs) {
					for (ONDEXConcept toC : toCs) {
						ONDEXRelation r = graph.getRelation(fromC, toC, rt);
						if (r == null) {
							graph.getFactory().createRelation(fromC, toC, rt,
									etAcc);
							count++;
						}
					}
				}
			}
		}

		log(count + " new relations have been created!");
	}

	/**
	 * Merges the concepts of
	 * <code>ConceptClass cc</cc> if they have the same set of
	 * accessions from the namespace <code>cv</code>.
	 * 
	 * @param cc
	 *            the <code>ConceptClass</code> of which concepts shall be
	 *            merged.
	 * @param dataSource
	 *            the namespace of the accession, according to which concepts
	 *            are merged.
	 * @param multipleAccAllowed
	 *            whether a concept is allowed to have more than one accession
	 *            in the given namespace.
	 * @param completeMatch
	 *            whether <i>all</i> of the accessions of that type have to
	 *            match between the concepts in order to consider them equal.
	 * @param missingAccAllowed
	 *            whether missing accessions of type <code>cv</code> will be
	 *            tolerated or not.
	 */
	private void mergeByAccession(ConceptClass cc, DataSource dataSource,
			boolean completeMatch, boolean multipleAccAllowed,
			boolean missingAccAllowed) {

		AccessionIndex index = new AccessionIndex(graph);
		index.indexByAccession(cc, dataSource, completeMatch,
				multipleAccAllowed, missingAccAllowed);
		// HashMap<String, MergeList> index = index(cc, cv, completeMatch,
		// multipleAccAllowed, missingAccAllowed);

		log("Merging " + cc.getFullname() + "...");
		ConsoleProgressBar pb = new ConsoleProgressBar(index.keySet().size());
		StringBuilder inconsistencies = new StringBuilder();
		int count = 0;
		for (String accKey : index.keySet()) {
			MergeList list = index.get(accKey);
			if (list.size() > 1 && !list.hasBeenMerged()) {
				try {
					merge(list, accKey);
					list.markAsMerged();
					count++;
				} catch (InconsistencyException e) {
					inconsistencies.append(e.getMessage()).append("\n");
				}
			}
			pb.inc(1);
		}
		pb.complete();

		log(count + " sets of " + cc.getFullname() + " have been merged.");

		if (inconsistencies.length() > 0) {
			complain(inconsistencies.toString());
		}
	}

	/**
	 * Merges the concepts of
	 * <code>ConceptClass cc</cc> if they have the same set of
	 * accessions from the namespace <code>cv</code>.
	 * 
	 * @param targetCC
	 *            The <code>ConceptClass</code> of which concepts shall be
	 *            merged.
	 * @param contextCC
	 *            The <code>ConceptClass</code> that determines the target's
	 *            context.
	 */
	private void mergeByContext(ConceptClass targetCC, ConceptClass contextCC) {

		ContextIndex index = new ContextIndex(graph);
		index.indexByNeighbourhood(targetCC, contextCC);
		// HashMap<String, MergeList> index = index(cc, cv, completeMatch,
		// multipleAccAllowed, missingAccAllowed);

		log("Merging " + targetCC.getFullname() + "...");
		ConsoleProgressBar pb = new ConsoleProgressBar(index.keySet().size());
		StringBuilder inconsistencies = new StringBuilder();
		int count = 0;
		for (String accKey : index.keySet()) {
			Set<MergeList> listSet = index.get(accKey);
			for (MergeList list : listSet) {
				if (list.size() > 1 && !list.hasBeenMerged()) {
					try {
						merge(list, accKey);
						list.markAsMerged();
						count++;
					} catch (InconsistencyException e) {
						inconsistencies.append(e.getMessage()).append("\n");
					}
				}
			}
			pb.inc(1);
		}
		pb.complete();

		log(count + " sets of " + targetCC.getFullname() + " have been merged.");

		if (inconsistencies.length() > 0) {
			complain(inconsistencies.toString());
		}
	}

	private void merge(MergeList list, String groupID)
			throws InconsistencyException {

		ConceptClass cc = determineCC(list, groupID, graph);

		// merge PIDs
		String pid = pidMerger.mergeStrings(list);

		// create merge concept
		ONDEXConcept merged = graph.getFactory().createConcept(pid,
				dataSourceUnknown, cc, etTrans);

		// merge annotations
		String anno = annoMerger.mergeStrings(list);
		if (anno != null) {
			merged.setAnnotation(anno);
		}

		// merge descriptions
		String desc = descMerger.mergeStrings(list);
		if (desc != null) {
			merged.setDescription(desc);
		}

		// merge CVs
		Collection<String> cvs = sourceMerger.mergeData(list);
		merged.createAttribute(anSources, new ArrayList<String>(cvs), false);

		// merge evidences
		for (EvidenceType et : etMerger.mergeData(list)) {
			merged.addEvidenceType(et);
		}

		// merge accessions
		for (ConceptAccession acc : accMerger.mergeData(list)) {
			merged.createConceptAccession(acc.getAccession(),
					acc.getElementOf(), acc.isAmbiguous());
		}

		// merge names
		for (ConceptName name : nameMerger.mergeData(list)) {
			merged.createConceptName(name.getName(), name.isPreferred());
		}

		// merge contexts
		for (ONDEXConcept co : contextMerger.mergeData(list)) {
			merged.addTag(co);
		}

		InconsistencyException error = null;

		// merge gds
		Map<AttributeName, Attribute> map = new HashMap<AttributeName, Attribute>();
		for (ONDEXConcept c : list) {
			for (Attribute attribute : c.getAttributes()) {
				Attribute peer = map.get(attribute.getOfType());
				if (peer == null) {
					map.put(attribute.getOfType(), attribute);
				} else {
					Class<?> type = peer.getOfType().getDataType();
					if (Collection.class.isAssignableFrom(type)) {
						try {
							Collection peerVal = (Collection) peer.getValue();
							Collection val = (Collection) attribute.getValue();
							peerVal.addAll(val);
						} catch (Exception e) {
							error = new InconsistencyException(
									"Tried to merge attributes with incompatible "
											+ "parameterisations of type Collection. Data lost!"
											+ "\nAttribute name: "
											+ attribute.getOfType()
													.getFullname() + " ("
											+ attribute.getOfType().getId()
											+ ")");
						}
					} else if (Map.class.isAssignableFrom(type)) {
						try {
							Map peerVal = (Map) peer.getValue();
							Map val = (Map) attribute.getValue();
							peerVal.putAll(val);
						} catch (Exception e) {
							error = new InconsistencyException(
									"Tried to merge attributes with incompatible "
											+ "paremeterisations of type Map. Data lost!"
											+ "\nAttribute name: "
											+ attribute.getOfType()
													.getFullname() + " ("
											+ attribute.getOfType().getId()
											+ ")");
						}
					} else {
						if (!peer.getValue().equals(attribute.getValue())) {
							error = new InconsistencyException(
									"Merge group contains attributes with "
											+ "same name but different value. Data lost!"
											+ "\nAttribute name: "
											+ attribute.getOfType()
													.getFullname() + " ("
											+ attribute.getOfType().getId()
											+ ")" + "\nValues: "
											+ attribute.getValue() + " vs. "
											+ peer.getValue());
						}
					}
				}
			}
		}
		for (Entry<AttributeName, Attribute> pair : map.entrySet()) {
			merged.createAttribute(pair.getKey(), pair.getValue().getValue(),
					false);
		}

		// index relations
		HashMap<RelationWrapper, HashSet<Attribute>> rel2attribute = new HashMap<RelationWrapper, HashSet<Attribute>>();
		HashMap<RelationWrapper, HashSet<EvidenceType>> rel2ets = new HashMap<RelationWrapper, HashSet<EvidenceType>>();
		for (ONDEXConcept c : list) {
			for (ONDEXRelation r : graph.getRelationsOfConcept(c)) {
				RelationWrapper w = new RelationWrapper();
				w.type = r.getOfType().getId();

				if (r.getFromConcept().equals(c)) {
					w.target = r.getToConcept().getId();
					w.outward = true;
				} else {
					w.target = r.getFromConcept().getId();
					w.outward = false;
				}

				// register gdss
				HashSet<Attribute> gdsset = rel2attribute.get(w);
				if (gdsset == null) {
					gdsset = new HashSet<Attribute>();
					rel2attribute.put(w, gdsset);
				}
				for (Attribute attribute : r.getAttributes()) {
					gdsset.add(attribute);
				}

				// register evidences
				HashSet<EvidenceType> etset = rel2ets.get(w);
				if (etset == null) {
					etset = new HashSet<EvidenceType>();
					rel2ets.put(w, etset);
				}
				for (EvidenceType et : r.getEvidence()) {
					etset.add(et);
				}
			}
		}

		// merge relations
		for (RelationWrapper w : rel2attribute.keySet()) {
			ONDEXConcept from = w.outward ? merged : graph.getConcept(w.target);
			ONDEXConcept to = w.outward ? graph.getConcept(w.target) : merged;
			RelationType rt = graph.getMetaData().getRelationType(w.type);
			ONDEXRelation r;
			r = graph.getRelation(from, to, rt);
			if (r == null) {
				r = graph.getFactory().createRelation(from, to, rt, etTrans);
			}

			HashSet<EvidenceType> ets = rel2ets.get(w);
			if (ets != null) {
				for (EvidenceType et : ets) {
					r.addEvidenceType(et);
				}
			}
			HashSet<Attribute> gdss = rel2attribute.get(w);
			if (gdss != null) {
				for (Attribute attribute : gdss) {
					r.createAttribute(attribute.getOfType(),
							attribute.getValue(), false);
				}
			}
		}

		// delete originals
		for (ONDEXConcept c : list) {
			graph.deleteConcept(c.getId());
		}

		if (error != null) {
			throw error;
		}

	}

	/*
	 * ########################## ##### PLUGIN METHODS #####
	 * ##########################
	 */

	@Override
	public String getId() {
		return "nclmerger";
	}

	@Override
	public String getName() {
		return "NCL Ondex merger";
	}

	@Override
	public String getVersion() {
		return "0.0.1";
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[0];
	}

	/**
	 * A little helper class that wraps an <code>OndexRelation</code>, thus
	 * allowing relations to be compared.
	 */
	private class RelationWrapper {
		public String type;
		public int target;
		public int qualifier;
		public boolean outward;

		@Override
		public boolean equals(Object o) {
			if (o instanceof RelationWrapper) {
				RelationWrapper w = (RelationWrapper) o;
				if (w.type.equals(type) && w.target == target
						&& w.qualifier == qualifier && w.outward == outward) {
					return true;
				}
			}
			return false;
		}
	}

}
