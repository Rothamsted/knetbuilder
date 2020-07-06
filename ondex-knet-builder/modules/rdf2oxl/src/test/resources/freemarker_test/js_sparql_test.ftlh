<#assign js = json?eval>


==== FreeMarker JS/SPARQL Test ====
<#list js [ '@graph' ] as person>
	
=== Name: ${person.name}
	<#list person.friendName![] as friendName>
	Friend: ${friendName}
	</#list>
===
</#list>

