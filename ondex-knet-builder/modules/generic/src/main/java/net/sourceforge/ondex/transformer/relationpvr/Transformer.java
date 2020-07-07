package net.sourceforge.ondex.transformer.relationpvr;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.annotations.Status;
import net.sourceforge.ondex.annotations.StatusType;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.tools.functions.GraphElementManipulation;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * For modifying certain relations
 * 
 * @author taubertj
 * 
 */
@Custodians(custodians = { "Jochen Weile" }, emails = { "jweile at users.sourceforge.net" })
@Status(status = StatusType.STABLE, description = "Tested December 2013 (Jacek Grzebyta)")
public class Transformer extends ONDEXTransformer implements ArgumentNames {

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		return new ArgumentDefinition[] {
				new BooleanArgumentDefinition(REMOVE_DUPS_ARG,
						REMOVE_DUPS_DESC, false, false),
				new BooleanArgumentDefinition(COMBINE_ALL_ARG,
						COMBINE_ALL_DESC, false, false) };
	}

	@Override
	public String getName() {
		return "Relation Duplications Remover";
	}

	@Override
	public String getVersion() {
		return "v1.0";
	}

	@Override
	public String getId() {
		return "relationpvr";
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return new String[] {};
	}

	@Override
	public void start() throws Exception {
		RelationType rt = graph.getMetaData().getRelationType("r");
		EvidenceType et = graph.getMetaData().getEvidenceType("IMPD");
		Object o = args.getUniqueValue(REMOVE_DUPS_ARG);
		boolean undup = false, merge = false;
		if (o != null)
			undup = (Boolean) o;
		o = args.getUniqueValue(COMBINE_ALL_ARG);
		if (o != null)
			merge = (Boolean) o;
		if (!undup && !merge)
			return;
		if (undup) {

			BitSet set_remove = new BitSet();
			for (ONDEXRelation r1 : graph.getRelations()) {
				if (set_remove.get(r1.getId()))
					continue;
				ONDEXRelation r2 = graph.getRelation(r1.getToConcept(),
						r1.getFromConcept(), r1.getOfType());
				if (r2 != null && r1 != r2) {
					GraphElementManipulation.copyRelationAttributes(r2, r1);
					set_remove.set(r2.getId());
				}
			}
			for (int i = set_remove.nextSetBit(0); i >= 0; i = set_remove
					.nextSetBit(i + 1)) {
				graph.deleteRelation(i);
			}
			System.err.println("Removed duplicates: "
					+ set_remove.cardinality());
		}
		if (merge) {
			Map<ONDEXConcept, Set<ONDEXRelation>> lookup = new HashMap<ONDEXConcept, Set<ONDEXRelation>>();
			for (ONDEXRelation r : graph.getRelations()) {
				Set<ONDEXRelation> set = lookup.get(r.getFromConcept());
				if (set == null) {
					set = new HashSet<ONDEXRelation>();
					lookup.put(r.getFromConcept(), set);
				}
				set.add(r);
				set = lookup.get(r.getToConcept());
				if (set == null) {
					set = new HashSet<ONDEXRelation>();
					lookup.put(r.getToConcept(), set);
				}
				set.add(r);
			}
			BitSet remove = new BitSet();
			BitSet keep = new BitSet();
			for (ONDEXRelation r : graph.getRelations()) {
				if (remove.get(r.getId()) || keep.get(r.getId()))
					continue;
				ONDEXRelation one = graph.getRelation(r.getFromConcept(),
						r.getToConcept(), rt);
				if (one == null) {
					one = graph.getRelation(r.getToConcept(),
							r.getFromConcept(), rt);
				}
				if (one == null) {
					one = graph.getFactory().createRelation(r.getFromConcept(),
							r.getToConcept(), rt, et);
				}
				keep.set(one.getId());
				Set<ONDEXRelation> all = new HashSet<ONDEXRelation>();
				if (r.getFromConcept().equals(r.getToConcept())) {
					for (ONDEXRelation temp : graph.getRelationsOfConcept(r
							.getToConcept())) {
						if (temp.getFromConcept().equals(temp.getToConcept())) {
							all.add(temp);
						}
					}
				} else {
					all = getIntersection(lookup.get(r.getFromConcept()),
							lookup.get(r.getToConcept()));
				}
				for (ONDEXRelation r1 : all) {
					if (!r1.equals(one)) {
						GraphElementManipulation
								.copyRelationAttributes(r1, one);
						remove.set(r1.getId());
					}
				}
			}
			for (int i = remove.nextSetBit(0); i >= 0; i = remove
					.nextSetBit(i + 1))
				graph.deleteRelation(i);
			System.err.println("Relations merged: " + remove.cardinality());
		}
	}

	public static Set<ONDEXRelation> getIntersection(Set<ONDEXRelation> v1,
			Set<ONDEXRelation> v2) {
		Set<ONDEXRelation> result = new HashSet<ONDEXRelation>();
		for (ONDEXRelation ra : v1) {
			for (ONDEXRelation rb : v2) {
				if (ra.equals(rb)) {
					result.add(ra);
					continue;
				}
			}
		}
		return result;
	}
}
