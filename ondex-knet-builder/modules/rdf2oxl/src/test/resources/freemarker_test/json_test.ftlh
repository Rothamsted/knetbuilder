<#assign js = json?eval>


==== JSON/FreeMarker Test ====

Title: ${js.title}
  
ID	Name
<#list js.items as i>
${i [ 'id' ]}	${i [ 'name' ]}
</#list>

