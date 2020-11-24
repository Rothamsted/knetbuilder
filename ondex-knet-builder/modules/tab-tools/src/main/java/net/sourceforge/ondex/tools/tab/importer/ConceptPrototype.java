package net.sourceforge.ondex.tools.tab.importer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.exception.type.AccessDeniedException;
import net.sourceforge.ondex.exception.type.EmptyStringException;
import net.sourceforge.ondex.exception.type.NullValueException;
import net.sourceforge.ondex.tools.subgraph.AttributePrototype;
import net.sourceforge.ondex.tools.subgraph.DefConst;
import net.sourceforge.ondex.tools.subgraph.DefConst.PositionProcessor;

/**
 * @author lysenkoa
 */
@SuppressWarnings ( "unchecked" )
public class ConceptPrototype extends GraphEntityPrototype
{	
	private final List<String[]> evValues = new LinkedList<> ();
	private final List<String[]> nameValues = new LinkedList<> ();
	private final List<String[]> accValues = new LinkedList<> ();
	private final List<String[]> gdsValues = new LinkedList<> ();

	private final List<String[]>[] listSets = new List[] { evValues, nameValues, accValues, gdsValues };

	private ONDEXGraph graph;
	private ONDEXGraphMetaData meta;
	private String id = "";
	private DataSource elementOf;
	private ConceptClass ofType;
	private EvidenceType evidence;
	private List<AttributePrototype> attributePrototypes;
	private ONDEXConcept currentValue = null;

	private final Map<String, net.sourceforge.ondex.tools.subgraph.DefConst.PositionProcessor> typeToProcessor = new HashMap<String, PositionProcessor> ();
	
	public ConceptPrototype ( ONDEXGraph graph, AttributePrototype... aps )
		throws NullValueException, EmptyStringException
	{
		this.attributePrototypes = new LinkedList<AttributePrototype> ( Arrays.asList ( aps ) );
		this.graph = graph;
		this.meta = graph.getMetaData ();
		this.elementOf = createDataSource ( meta, "UC" );
		this.ofType = createCC ( meta, "Thing" );
		this.evidence = createEvidence ( meta, "IMPD" );

		typeToProcessor.put ( DefConst.DEFATTR, createTypeProcessor ( 3 ) );
		typeToProcessor.put ( DefConst.DEFEVIDENCE, createTypeProcessor ( 0 ) );
		typeToProcessor.put ( DefConst.DEFNAME, createTypeProcessor ( 1 ) );
		typeToProcessor.put ( DefConst.DEFACC, createTypeProcessor ( 2 ) );
		typeToProcessor.put ( DefConst.DEFDATASOURCE, createTypeProcessor ( newProt -> 
			elementOf = createDataSource ( meta, newProt [ newProt.length - 1 ] ) 
		));
		typeToProcessor.put ( DefConst.DEFCC, 
			createTypeProcessor ( 
				newProt -> ofType = createCC ( meta, newProt [ newProt.length - 1 ] ),
				() -> ofType = null
			)
		);
		typeToProcessor.put ( DefConst.DEFPID, createTypeProcessor ( newProt -> 
			id = newProt[ newProt.length - 1 ]
		));
	}

	/**
	 * See above. These methods create a processor that goes through a list of prototype values
	 * and do some processing/import action with them. 
	 */
	private PositionProcessor createTypeProcessorGeneric ( Consumer<List<String[]>> prototypeValuesProcessor )
	{
		return new PositionProcessor()
		{
			@Override
			public void process ( AttributePrototype ap ) throws NullValueException, EmptyStringException
			{
				prototypeValuesProcessor.accept ( ap.getValue () );
			}
		};
	}
	
	/**
	 * for each element in the list, checks it has all non-null values, if yes, pass the element to newValAction, 
	 * if not run nullAction (if it's != null).
	 */
	private PositionProcessor createTypeProcessor ( Consumer<String[]> newValAction, Runnable nullAction )
	{
		return createTypeProcessorGeneric ( newProts -> 
		{
			for ( String[] newProt : newProts )
			{
				for ( String s : newProt ) 
					if ( s == null ) {
						if ( nullAction != null ) nullAction.run ();
						return;
				}
				newValAction.accept ( newProt );
			}
		});
	}

	/**
	 * newValAction set to null
	 */
	private PositionProcessor createTypeProcessor ( Consumer<String[]> newValAction )
	{
		return createTypeProcessor ( newValAction, null );
	}
	
	/**
	 * The action is {@code listSets[ listSetIndex ].add ( newProt ) ) }
	 */
	private PositionProcessor createTypeProcessor ( int listSetIndex )
	{
		return createTypeProcessor ( newProt -> listSets[ listSetIndex ].add ( newProt ) );
	}

	
	public void addAttributes ( AttributePrototype... aps )
	{
		Collections.addAll ( attributePrototypes, aps );
	}

	public ONDEXConcept getValue ()
	{
		return currentValue;
	}

	public ONDEXConcept parse ( String[] vector ) throws NullValueException, AccessDeniedException, EmptyStringException
	{
		currentValue = null;

		for ( AttributePrototype ap : attributePrototypes )
		{
			ap.parse ( vector );
			typeToProcessor.get ( ap.getType () ).process ( ap );
		}

		if ( elementOf == null )
		{
			System.err.println ( "Error!" );
			throw new NullValueException ( "Could not create concept - no valid type specifed." );
		}
		currentValue = graph.getFactory ().createConcept ( id, elementOf, ofType, evidence );

		/* evValues */
		for ( String[] prot : listSets[ 0 ] )
			currentValue.addEvidenceType ( createEvidence ( meta, prot[ prot.length - 1 ] ) );

		/* nameValues */
		for ( String[] prot : listSets[ 1 ] )
		{
			if ( prot[ 2 ].equals ( "" ) || prot[ 2 ] == null )
				continue;
			try
			{
				boolean isPreferred = Boolean.valueOf ( prot[ 1 ] );
				currentValue.createConceptName ( prot[ 2 ], isPreferred );
			}
			catch ( Exception e )
			{
				currentValue.createConceptName ( prot[ 2 ], false );
			}
		}

		/* accValues */
		for ( String[] prot : listSets[ 2 ] )
		{
			if ( prot[ 3 ].equals ( "" ) || prot[ 3 ] == null )
				continue;
			// System.err.println(Arrays.asList(prot).toString());
			try
			{
				boolean isAmbiguous = Boolean.valueOf ( prot[ 1 ] );
				currentValue.createConceptAccession ( prot[ 3 ].trim ().toUpperCase (), createDataSource ( meta, prot[ 2 ] ),
						isAmbiguous );
			}
			catch ( Exception e )
			{
				currentValue.createConceptAccession ( prot[ 3 ].trim ().toUpperCase (), createDataSource ( meta, prot[ 2 ] ),
						true );
			}
		}

		/* gdsValues */
		for ( String[] prot : listSets[ 3 ] )
		{
			Class<?> cls = null;
			if ( prot[ 1 ].equals ( "" ) || prot[ 1 ] == null || prot[ 3 ].equals ( "" ) || prot[ 3 ] == null )
				continue;
			boolean index = false;
			if ( prot.length > 4 && prot[ 4 ] != null )
			{
				index = prot[ 4 ].equalsIgnoreCase ( "true" );
			}
			if ( prot[ 2 ].equalsIgnoreCase ( DefConst.NUMBER ) )
			{
				try
				{
					cls = java.lang.Double.class;

					AttributeName attName = createAttName ( meta, prot[ 1 ], cls );
					// Marco Brandizi: this is to fix the fact that PVALUE is declared as float but PathParser always
					// gets double values from input
					// TODO: this occurs in many places, we need to factorise
					Number value;
					if ( attName.getDataType ().equals ( Float.class ) )
						value = Float.valueOf ( prot[ 3 ] );
					else
						value = Double.valueOf ( prot[ 3 ] );

					currentValue.createAttribute ( attName, value, index );
				}
				catch ( Exception e )
				{
					System.err.println ( String.format ( "%s while parsing %s: %s", e.getClass ().getSimpleName (),
							Arrays.toString ( prot ), e.getMessage () ) );
				}

			}

			else if ( prot[ 2 ].equalsIgnoreCase ( DefConst.INTEGER ) )
			{
				cls = java.lang.Integer.class;
				try
				{
					currentValue.createAttribute ( createAttName ( meta, prot[ 1 ], cls ), Integer.valueOf ( prot[ 3 ] ), index );
				}
				catch ( Exception e )
				{
					System.err.println ( String.format ( "%s while parsing %s: %s", e.getClass ().getSimpleName (),
							Arrays.toString ( prot ), e.getMessage () ) );
				}
			}

			else if ( prot[ 2 ].equalsIgnoreCase ( DefConst.SMILES ) )
			{
				cls = net.sourceforge.ondex.tools.data.ChemicalStructure.class;
				net.sourceforge.ondex.tools.data.ChemicalStructure cs = new net.sourceforge.ondex.tools.data.ChemicalStructure ();
				cs.setSMILES ( prot[ 3 ] );
				currentValue.createAttribute ( createAttName ( meta, prot[ 1 ], cls ), cs, index );
			}

			else
			{
				cls = java.lang.String.class;
				if ( prot[ 3 ] == null )
				{
					System.err.println ( "Skipped value" );
					continue;
				}
				currentValue.createAttribute ( createAttName ( meta, prot[ 1 ], cls ), prot[ 3 ], index );
			}
		}
		for ( List<String[]> dataList : listSets )
			dataList.clear ();
		return currentValue;
	}
}
