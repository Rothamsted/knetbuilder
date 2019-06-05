import csv

"""
	Never used. Generates OXL elements for new constaint properties in EFO.
"""

tpl = """<relation_type>
	<id>{ondexMapping}</id>
	<fullname>{onProplabel}</fullname>
	<description>{onPropDef}</description>
	<inverseName>none</inverseName>
	<isAntisymmetric>false</isAntisymmetric>
	<isReflexive>false</isReflexive>
	<isSymmetric>false</isSymmetric>
	<isTransitive>false</isTransitive>
	<specialisationOf><idRef>related</idRef></specialisationOf>
</relation_type>"""

with open( 'restrictions.csv' ) as f:
  rdr = csv.DictReader ( f, delimiter = ';', quotechar = '"' )
  visited = {}
  for row in rdr:
    #print ( row )
    for key in row:
      row [ key ] = row [ key ].strip ()
    if row.get ( "ondexExisting" ) == 'y': continue
    id = row.get ( "ondexMapping" )
    if id == '': continue
    if visited.get ( id ): continue
    visited [ id ] = True
    for key in [ "onProplabel", "onPropDef" ]:
      if not row.get ( key ): row [ key ] = ''
    print ( tpl.format ( **row ) )
