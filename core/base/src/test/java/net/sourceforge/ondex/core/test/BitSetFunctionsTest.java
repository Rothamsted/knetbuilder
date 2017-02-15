package net.sourceforge.ondex.core.test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EntityFactory;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.NullValueException;

import org.junit.Test;

/**
 * Created by IntelliJ IDEA. User: nmrp3 Date: 12-Jul-2010 Time: 17:09:21 To
 * change this template use File | Settings | File Templates.
 */
public class BitSetFunctionsTest {
	// regression test for OVTK-209
	@Test
	public void testAdd() {
		Set<ONDEXConcept> cSet = new HashSet<ONDEXConcept>(Arrays.asList(
				new DummyConcept(1, "rod"), new DummyConcept(2, "jane"),
				new DummyConcept(3, "freddy")));

		ONDEXGraph g = new DummyONDEXGraph(cSet);

		Set<ONDEXConcept> cs = BitSetFunctions.create(g, ONDEXConcept.class,
				new BitSet());

		for (ONDEXConcept c : g.getConcepts()) {
			cs.add(c);
		}
	}

	private static class DummyConcept implements ONDEXConcept {
		private int id;
		private String name;

		private DummyConcept(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public String getPID() {
			return name;
		}

		@Override
		public void setPID(String pid) {

		}

		@Override
		public ConceptAccession createConceptAccession(String accession,
				DataSource elementOf, boolean ambiguous) {
			return null;

		}

		@Override
		public ConceptName createConceptName(String name, boolean isPreferred) {
			return null;

		}

		@Override
		public boolean deleteConceptAccession(String accession,
				DataSource elementOf) {
			return false;

		}

		@Override
		public boolean deleteConceptName(String name) {
			return false;

		}

		@Override
		public String getAnnotation() {
			return null;

		}

		@Override
		public ConceptAccession getConceptAccession(String accession,
				DataSource elementOf) {
			return null;

		}

		@Override
		public Set<ConceptAccession> getConceptAccessions() {
			return null;

		}

		@Override
		public ConceptName getConceptName() {
			return null;

		}

		@Override
		public ConceptName getConceptName(String name) {
			return null;

		}

		@Override
		public Set<ConceptName> getConceptNames() {
			return null;

		}

		@Override
		public String getDescription() {
			return null;

		}

		@Override
		public DataSource getElementOf() {
			return null;

		}

		@Override
		public void setAnnotation(String annotation) {

		}

		@Override
		public void setDescription(String description) {

		}

		@Override
		public boolean inheritedFrom(ConceptClass conceptClass) {
			return false;

		}

		@Override
		public ConceptClass getOfType() {
			return null;

		}

		@Override
		public long getSID() {
			return 0;

		}

		@Override
		public void addTag(ONDEXConcept ac) throws AccessDeniedException,
				NullValueException {

		}

		@Override
		public void addEvidenceType(EvidenceType evidencetype) {

		}

		@Override
		public Set<ONDEXConcept> getTags() {
			return null;

		}

		@Override
		public Set<EvidenceType> getEvidence() {
			return null;

		}

		@Override
		public boolean removeTag(ONDEXConcept ac) {
			return false;

		}

		@Override
		public boolean removeEvidenceType(EvidenceType evidencetype) {
			return false;

		}

		@Override
		public Attribute createAttribute(AttributeName attrname, Object value,
				boolean doIndex) throws AccessDeniedException,
				NullValueException {
			return null;

		}

		@Override
		public boolean deleteAttribute(AttributeName attrname) {
			return false;

		}

		@Override
		public Attribute getAttribute(AttributeName attrname) {
			return null;

		}

		@Override
		public Set<Attribute> getAttributes() {
			return null;

		}
	}

	private static class DummyONDEXGraph implements ONDEXGraph {
		private Set<ONDEXConcept> cSet;

		public DummyONDEXGraph(Set<ONDEXConcept> cSet) {
			this.cSet = cSet;
		}

		@Override
		public ONDEXConcept createConcept(String pid, String annotation,
				String description, DataSource elementOf, ConceptClass ofType,
				Collection<EvidenceType> evidence) {
			return null;

		}

		@Override
		public ONDEXRelation createRelation(ONDEXConcept fromConcept,
				ONDEXConcept toConcept, RelationType ofType,
				Collection<EvidenceType> evidence) {
			return null;

		}

		@Override
		public boolean deleteConcept(int id) {
			return false;

		}

		@Override
		public boolean deleteRelation(int id) {
			return false;

		}

		@Override
		public boolean deleteRelation(ONDEXConcept fromConcept,
				ONDEXConcept toConcept, RelationType ofType) {
			return false;

		}

		@Override
		public ONDEXConcept getConcept(int id) {
			return null;

		}

		@Override
		public Set<ONDEXConcept> getConcepts() {
			return cSet;
		}

		@Override
		public Set<ONDEXConcept> getConceptsOfAttributeName(AttributeName an) {
			return null;

		}

		@Override
		public Set<ONDEXConcept> getConceptsOfConceptClass(ConceptClass cc) {
			return null;

		}

		@Override
		public Set<ONDEXConcept> getConceptsOfTag(ONDEXConcept ac) {
			return null;

		}

		@Override
		public Set<ONDEXConcept> getConceptsOfDataSource(DataSource dataSource) {
			return null;

		}

		@Override
		public Set<ONDEXConcept> getConceptsOfEvidenceType(EvidenceType et) {
			return null;

		}

		@Override
		public Set<ONDEXConcept> getAllTags() {
			return null;

		}

		@Override
		public EntityFactory getFactory() {
			return null;

		}

		@Override
		public ONDEXGraphMetaData getMetaData() {
			return null;

		}

		@Override
		public String getName() {
			return null;

		}

		@Override
		public ONDEXRelation getRelation(int id) {
			return null;

		}

		@Override
		public ONDEXRelation getRelation(ONDEXConcept fromConcept,
				ONDEXConcept toConcept, RelationType ofType) {
			return null;

		}

		@Override
		public Set<ONDEXRelation> getRelations() {
			return null;

		}

		@Override
		public Set<ONDEXRelation> getRelationsOfAttributeName(AttributeName an) {
			return null;

		}

		@Override
		public Set<ONDEXRelation> getRelationsOfConcept(ONDEXConcept concept) {
			return null;

		}

		@Override
		public Set<ONDEXRelation> getRelationsOfConceptClass(ConceptClass cc) {
			return null;

		}

		@Override
		public Set<ONDEXRelation> getRelationsOfTag(ONDEXConcept ac) {
			return null;

		}

		@Override
		public Set<ONDEXRelation> getRelationsOfDataSource(DataSource dataSource) {
			return null;

		}

		@Override
		public Set<ONDEXRelation> getRelationsOfEvidenceType(EvidenceType et) {
			return null;

		}

		@Override
		public Set<ONDEXRelation> getRelationsOfRelationType(RelationType rt) {
			return null;

		}

		@Override
		public boolean isReadOnly() {
			return false;

		}

		@Override
		public long getSID() {
			return 0;

		}
	}
}
