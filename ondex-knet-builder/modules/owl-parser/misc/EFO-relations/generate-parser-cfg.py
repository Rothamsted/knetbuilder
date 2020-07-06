import csv
from xml.sax.saxutils import quoteattr

"""
  Takes the constraints data generated with efo-restrictions.sparql and generates Spring elements
  for the OWL parser configuration, that is linkers and mappers for some/all values-from properties.
"""

# Converts a generic string into an id-suitable value
def normalize_id ( str ):
	return str.replace ( ' ', '_' )

# Converts between naming conventions
def snake2camel ( snake_str ):
    snake_str = snake_str.replace ( ' ', '_' )
    components = snake_str.split ( '_' )
    return components[ 0 ] + ''.join ( x.title() for x in components[ 1: ] ) 

def clean_input ( row ):
  for key in row:
    row [ key ] = row [ key ].strip ()
  for key in [ "onProplabel", "onPropDef" ]:
    if not row.get ( key ): row [ key ] = ''
  return row

parser_linker_tpl = \
"""<bean id = "{parserLinkerName}" class = "net.sourceforge.ondex.parser.ExploringMapper.LinkerConfiguration">
  <property name = "scanner">
    <bean class = "net.sourceforge.ondex.parser.owl.OWL{scannerVariant}Scanner">
      <property name="propertyIri" value="{onProp}" /><!-- {onPropLabel} -->
    </bean>
  </property>
  <property name = "mapper" ref = "{parserMapperName}" />
</bean>"""


parser_mapper_tpl = \
"""<bean id = "{parserMapperName}" class = "net.sourceforge.ondex.parser.SimpleRelationMapper">
  <property name ="relationTypePrototype">
    <bean class = "net.sourceforge.ondex.core.util.prototypes.RelationTypePrototype">
      <property name="id" value = "{ondexMapping}" />
      <property name="fullName" value = "{onPropLabel}" />
      <property name="description" value = {onPropDef} />
      <property name="parentPrototype" ref = "efoRelation" />
    </bean>
  </property> 
</bean>"""


with open( 'restrictions.csv' ) as f:
  rdr = csv.DictReader ( f, delimiter = ';', quotechar = '"' )
  linker_names = set ()
  created_mappers = set ()
  for row in rdr:
    #print ( row )
    row = clean_input ( row )
    id = row.get ( "ondexMapping" )
    if id == '': continue
    
    id = normalize_id ( id )
    
    row [ "ondexMapping" ] = id
    restriction_type = "some" if "someValuesFrom" in row[ 'kind' ] else 'all'

    # Work out the mapper some/all mapper
    parser_mapper_name = row.get ( "parserMapper" )
    if not parser_mapper_name: parser_mapper_name = snake2camel ( id ) + "Mapper"
    
    if not row.get ( "parserMapper" ) and parser_mapper_name not in created_mappers:
      # Create a mapper definition to this property
      row [ "parserMapperName" ] = parser_mapper_name
      if row [ "onPropDef" ] != '':
      	if not row [ "onPropDef" ].endswith ( '.' ): row [ "onPropDef" ] += '.'
      	row [ "onPropDef" ] += " "
      row [ "onPropDef" ] += "Mapped from the original URI: '" + row[ "onProp" ] + "'."
      row [ "onPropDef" ] = quoteattr ( row [ "onPropDef" ] )
      #print ( row )
      print ( parser_mapper_tpl.format ( **row ) )
      created_mappers.add ( parser_mapper_name )
    else:
      row [ "parserMapperName" ] = parser_mapper_name # We need it anyway, below

    # Now the linker
    parserLinkerName = row.get ( "parserLinker" )
    if not parserLinkerName:
      # Create a new linker bean
      parserLinkerName = snake2camel ( id ) + restriction_type.title() + "FromLinker"
      row [ "parserLinkerName" ] = parserLinkerName
      row [ "scannerVariant" ] = "Some" if restriction_type == "some" else "AllFrom"
      print ( parser_linker_tpl.format ( **row ) )
      linker_names.add ( parserLinkerName )
      
  print (
"""

---- Use this for the linkers -----

<property name = "linkers">
  <!-- These will be added to the existing is-a linker (in default-mappings.xml) -->
  <list merge = "true">
    
    <!-- Follows intersections of classes of which the starting class is declared equivalent to  -->
    <ref bean = "eqIntersctLinker" />				
"""        
      )

  for linkerName in linker_names:
    print ( "    <ref bean = '{}' />".format ( linkerName ) )

  print(
"""  </list>
</property> <!-- /linkers -->"""
  )
