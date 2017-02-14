package net.sourceforge.ondex.transformer.attributeregex;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.args.StringMappingPairArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.exception.type.AttributeNameMissingException;
import net.sourceforge.ondex.exception.type.InconsistencyException;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This transformer applies a given regular expression to a String value of a
 * Attribute and replaces the Attribute value with the matching String.
 * 
 * @author taubertj
 * 
 */
public class Transformer extends ONDEXTransformer
{

	private static final String anConceptArg = "ConceptAttributeNameRegex";

	private static final String anConceptDesc = "A comma separated pair of attribute name id and JAVA RegEx to be applied to Attribute value Strings on Concepts.";

	private static final String squareRootConceptArg = "ConceptAttributeNameSquareRoot";

	private static final String squareRootConceptDesc = "Which Attribute valuse on Concepts should be square rooted?";

	private static final String cubeRootConceptArg = "ConceptAttributeNameCubeRoot";

	private static final String cubeRootConceptDesc = "Which Attribute valuse on Concepts should be cube rooted?";

	private static final String anRelationArg = "RelationAttributeNameRegex";

	private static final String anRelationDesc = "A comma separated pair of attribute name id and JAVA RegEx to be applied to Attribute value Strings on Relations.";

	private static final String squareRootRelationArg = "RelationAttributeNameSquareRoot";

	private static final String squareRootRelationDesc = "Which Attribute valuse on Relations should be square rooted?";

	private static final String cubeRootRelationArg = "RelationAttributeNameCubeRoot";

	private static final String cubeRootRelationDesc = "Which Attribute valuse on Relations should be cube rooted?";

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions() {
		StringMappingPairArgumentDefinition conceptAnToRegex = new StringMappingPairArgumentDefinition(
				anConceptArg, anConceptDesc, false, null, true);
		StringArgumentDefinition conceptAnSquareRoot = new StringArgumentDefinition(
				squareRootConceptArg, squareRootConceptDesc, false, null, true);
		StringArgumentDefinition conceptAnCubeRoot = new StringArgumentDefinition(
				cubeRootConceptArg, cubeRootConceptDesc, false, null, true);
		StringMappingPairArgumentDefinition relationAnToRegex = new StringMappingPairArgumentDefinition(
				anRelationArg, anRelationDesc, false, null, true);
		StringArgumentDefinition relationAnSquareRoot = new StringArgumentDefinition(
				squareRootRelationArg, squareRootRelationDesc, false, null,
				true);
		StringArgumentDefinition relationAnCubeRoot = new StringArgumentDefinition(
				cubeRootRelationArg, cubeRootRelationDesc, false, null, true);
		return new ArgumentDefinition<?>[] { conceptAnToRegex,
				conceptAnSquareRoot, relationAnToRegex, relationAnSquareRoot,
				conceptAnCubeRoot, relationAnCubeRoot };
	}

	@Override
	public String getId() {
		return "attributeRegex";
	}

	@Override
	public String getName() {
		return "Attribute RegEx transformer";
	}

	@Override
	public String getVersion() {
		return "01.04.2010";
	}

	@Override
	public boolean requiresIndexedGraph() {
		return false;
	}

	@Override
	public String[] requiresValidators() {
		return null;
	}

	@Override
	public void start() throws Exception {

		// parse attribute names and regex from parameters
		Map<AttributeName, String> conceptAns = new HashMap<AttributeName, String>();
		for (Object o : args.getObjectValueArray(anConceptArg)) {
			String[] pair = ((String) o).split(",");
			AttributeName an = graph.getMetaData().getAttributeName(pair[0]);
			if (an == null)
				throw new AttributeNameMissingException(pair[0]);
			if (!an.getDataTypeAsString().equals("java.lang.String"))
				throw new InconsistencyException("AttributeName " + pair[0]
						+ " does not contain value of type String.");
			conceptAns.put(an, pair[1]);
			System.out.println(an + " on Concepts with pattern " + pair[1]);
		}

		// parse attribute names and regex from parameters
		Map<AttributeName, String> relationAns = new HashMap<AttributeName, String>();
		for (Object o : args.getObjectValueArray(anRelationArg)) {
			String[] pair = ((String) o).split(",");
			AttributeName an = graph.getMetaData().getAttributeName(pair[0]);
			if (an == null)
				throw new AttributeNameMissingException(pair[0]);
			if (!an.getDataTypeAsString().equals("java.lang.String"))
				throw new InconsistencyException("AttributeName " + pair[0]
						+ " does not contain value of type String.");
			relationAns.put(an, pair[1]);
			System.out.println(an + " on Relations with pattern " + pair[1]);
		}

		// process all Attribute on concepts
		for (AttributeName an : conceptAns.keySet()) {
			Pattern p = Pattern.compile(conceptAns.get(an));

			// process all concepts
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(an)) {
				Attribute attribute = c.getAttribute(an);
				String value = (String) attribute.getValue();
				Matcher matcher = p.matcher(value);
				boolean matchFound = matcher.find();

				// only update Attribute when matched
				if (matchFound) {
					// Get first group for this match
					value = matcher.group(0);
					attribute.setValue(value);
				}
			}
		}

		// process all Attribute on relations
		for (AttributeName an : relationAns.keySet()) {
			Pattern p = Pattern.compile(relationAns.get(an));

			// process all relations
			for (ONDEXRelation r : graph.getRelationsOfAttributeName(an)) {
				Attribute attribute = r.getAttribute(an);
				String value = (String) attribute.getValue();
				Matcher matcher = p.matcher(value);
				boolean matchFound = matcher.find();

				// only update Attribute when matched
				if (matchFound) {
					// Get first group for this match
					value = matcher.group(0);
					attribute.setValue(value);
				}
			}
		}

		// calculating the square root for Attribute values on concepts
		for (Object o : args.getObjectValueArray(squareRootConceptArg)) {
			String id = (String) o;
			AttributeName an = graph.getMetaData().getAttributeName(id);
			if (an == null)
				throw new AttributeNameMissingException(id);
			if (!Number.class.isAssignableFrom(an.getDataType()))
				throw new InconsistencyException("AttributeName on Concepts "
						+ id + " does not contain value of type Number.");
			AttributeName newAn = null;
			if (!graph.getMetaData().checkAttributeName(
					an.getId() + "SquareRoot")) {
				newAn = graph.getMetaData().createAttributeName(
						an.getId() + "SquareRoot",
						an.getFullname() + " (SquareRoot)",
						an.getDescription() + " (SquareRoot)", an.getUnit(),
						Double.class, an);
			} else {
				newAn = graph.getMetaData().getAttributeName(
						an.getId() + "SquareRoot");
			}

			// process all concepts
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(an)) {
				Attribute attribute = c.getAttribute(an);
				Number number = (Number) attribute.getValue();
				c.createAttribute(newAn, Math.sqrt(number.doubleValue()), false);
			}
		}

		// calculating the square root for Attribute values on relations
		for (Object o : args.getObjectValueArray(squareRootRelationArg)) {
			String id = (String) o;
			AttributeName an = graph.getMetaData().getAttributeName(id);
			if (an == null)
				throw new AttributeNameMissingException(id);
			if (!Number.class.isAssignableFrom(an.getDataType()))
				throw new InconsistencyException("AttributeName on Relations "
						+ id + " does not contain value of type Number.");
			AttributeName newAn = null;
			if (!graph.getMetaData().checkAttributeName(
					an.getId() + "SquareRoot")) {
				newAn = graph.getMetaData().createAttributeName(
						an.getId() + "SquareRoot",
						an.getFullname() + " (SquareRoot)",
						an.getDescription() + " (SquareRoot)", an.getUnit(),
						Double.class, an);
			} else {
				newAn = graph.getMetaData().getAttributeName(
						an.getId() + "SquareRoot");
			}

			// process all relations
			for (ONDEXRelation r : graph.getRelationsOfAttributeName(an)) {
				Attribute attribute = r.getAttribute(an);
				Number number = (Number) attribute.getValue();
				r.createAttribute(newAn, Math.sqrt(number
                        .doubleValue()), false);
			}
		}

		// calculating the cube root for Attribute values on concepts
		for (Object o : args.getObjectValueArray(cubeRootConceptArg)) {
			String id = (String) o;
			AttributeName an = graph.getMetaData().getAttributeName(id);
			if (an == null)
				throw new AttributeNameMissingException(id);
			if (!Number.class.isAssignableFrom(an.getDataType()))
				throw new InconsistencyException("AttributeName on Concepts "
						+ id + " does not contain value of type Number.");
			AttributeName newAn = null;
			if (!graph.getMetaData()
					.checkAttributeName(an.getId() + "CubeRoot")) {
				newAn = graph.getMetaData().createAttributeName(
						an.getId() + "CubeRoot",
						an.getFullname() + " (CubeRoot)",
						an.getDescription() + " (CubeRoot)", an.getUnit(),
						Double.class, an);
			} else {
				newAn = graph.getMetaData().getAttributeName(
						an.getId() + "CubeRoot");
			}

			// process all concepts
			for (ONDEXConcept c : graph.getConceptsOfAttributeName(an)) {
				Attribute attribute = c.getAttribute(an);
				Number number = (Number) attribute.getValue();
				c.createAttribute(newAn, Math.pow(number
                        .doubleValue(), 1.0 / 3), false);
			}
		}

		// calculating the cube root for Attribute values on relations
		for (Object o : args.getObjectValueArray(cubeRootRelationArg)) {
			String id = (String) o;
			AttributeName an = graph.getMetaData().getAttributeName(id);
			if (an == null)
				throw new AttributeNameMissingException(id);
			if (!Number.class.isAssignableFrom(an.getDataType()))
				throw new InconsistencyException("AttributeName on Relations "
						+ id + " does not contain value of type Number.");
			AttributeName newAn = null;
			if (!graph.getMetaData()
					.checkAttributeName(an.getId() + "CubeRoot")) {
				newAn = graph.getMetaData().createAttributeName(
						an.getId() + "CubeRoot",
						an.getFullname() + " (CubeRoot)",
						an.getDescription() + " (CubeRoot)", an.getUnit(),
						Double.class, an);
			} else {
				newAn = graph.getMetaData().getAttributeName(
						an.getId() + "CubeRoot");
			}

			// process all relations
			for (ONDEXRelation r : graph.getRelationsOfAttributeName(an)) {
				Attribute attribute = r.getAttribute(an);
				Number number = (Number) attribute.getValue();
				r.createAttribute(newAn, Math.pow(number
                        .doubleValue(), 1.0 / 3), false);
			}
		}
	}

}
