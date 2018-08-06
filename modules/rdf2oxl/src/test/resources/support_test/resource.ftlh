<#assign js = json?eval>
<#assign js = js[ '@graph' ]???then( js[ '@graph' ], [ js ] )>


==== Publications Test ====
<#list js as pub>
	
	
=== Title: ${pub.AbstractHeader}
  	Year: ${pub[ "bka:YEAR" ]?c}
  	
  	Abstract:
${pub.Abstract}
===
</#list>
