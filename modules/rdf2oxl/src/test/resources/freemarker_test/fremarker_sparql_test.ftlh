<#function literal_string literal>
  <#return literal.getString ()!''>
</#function>

<#function sparql_string solution var>
	<#local literal = solution.getLiteral ( var )!''>
	<#return (literal != '')?then ( literal_string ( literal ), '' )>
</#function>

==== FreeMarker SPARQL Test ====


	Hello, ${who}!

Person Name	Friend Name
<#list solutions as sol>
${sparql_string (sol, 'aname')}	${sparql_string (sol, 'bname')}
</#list>
.
