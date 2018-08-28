

==== FreeMarker Framed-JS/SPARQL Test ====
<#list persons as person>
	
=== Name: ${person.name}
	<#list (person.knows)![] as friend>
	Friend: ${(friend.name)!"<Unknown>"}
	</#list>
===
</#list>

