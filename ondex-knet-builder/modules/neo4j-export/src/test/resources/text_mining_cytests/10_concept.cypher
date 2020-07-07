MATCH  
  (toterm:TO { 
  		iri: bkr + "to_0000233",
  		prefName: "root volume",
  		altName: "RTVOL",
  		comment: "Is an indirect measure of root mass or density. Quantified in terms of cubic centimeters by water displacement method. ( Reference: GR:pj GR_REF:6917 )"
  }) - [ :is_a ] -> ( :TO { identifier: "TO:0000043" } ),
  
  (toterm) - [:identifier] -> (:Accession{ identifier: "TO:0000233" } ) 
           - [:dataSource] -> ({ iri: bk + "TO" })
  
RETURN 
  COUNT ( toterm ) = 1
