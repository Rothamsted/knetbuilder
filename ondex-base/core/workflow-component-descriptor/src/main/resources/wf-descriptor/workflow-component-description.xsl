<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wfc="http://ondex.sourceforge.net/workflow_element">

<xsl:template match="/plugins">

  <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
  <body>
  <h2>Workflow Component Info - <xsl:value-of select="wfc:artifactId"/></h2>
  <table>
  <tr><td><b>Group Id</b></td><td><xsl:value-of select="wfc:groupId"/></td></tr>
  <tr><td><b>Version</b></td><td><xsl:value-of select="wfc:version"/></td></tr>
  </table>
  
  <xsl:for-each select="wfc:plugin">
  	<h3>Plugin</h3>
	<table>
	<tr><td><b>Status</b></td>
	<xsl:choose>
          <xsl:when test="wfc:status/@type='Experemental'">
            <td><font color="red">
            <xsl:value-of select="wfc:status/@type"/>
	    </font></td>
          </xsl:when>
          <xsl:otherwise>
            <td><font color="green">
	    <xsl:value-of select="wfc:status/@type"/>
	    </font></td>
          </xsl:otherwise>
        </xsl:choose>
	</tr>
	<tr><td><b>Type</b></td><td><xsl:value-of select="@type"/></td></tr>
	<tr><td><b>Entry class</b></td><td><xsl:value-of select="wfc:entryClass"/></td></tr>
	<tr><td valign="top"><b>Comment</b></td><td><xsl:value-of select="wfc:comment" disable-output-escaping="yes"/></td></tr>
	<tr><td valign="top"><b>Authors</b></td><td>
	<table>
	<xsl:for-each select="wfc:authors/wfc:author">
		<tr><td><xsl:value-of select="."/></td></tr>
	</xsl:for-each>
	</table>
	</td></tr>
	<tr><td valign="top"><b>Database</b></td><td>
		<table>
		<tr><td><b><i><xsl:value-of select="wfc:database/@name"/></i></b></td></tr>
		<tr><td><xsl:value-of select="wfc:database/wfc:description"/></td></tr>
		<tr><td>
		<a><xsl:attribute name='href'><xsl:value-of select="wfc:database/wfc:url"/></xsl:attribute>
		<xsl:value-of select="wfc:database/wfc:url"/></a></td></tr>
		</table>
	</td></tr>
	<tr><td valign="top"><b>Files</b></td><td>
		<table>
		<tr><td><b><xsl:value-of select="wfc:data_files/wfc:description"/></b></td></tr>
		<xsl:for-each select="wfc:data_files/wfc:urls/wfc:url">
			<tr><td>
			<a><xsl:attribute name='href'><xsl:value-of select="."/></xsl:attribute>
			<xsl:value-of select="."/></a></td></tr>
		</xsl:for-each>
		</table>
	</td></tr>
	<tr><td valign="top"><b>Meta data</b></td><td>
		<table>
		<xsl:for-each select="wfc:ondex-metadata/wfc:metadata">
			<tr><td><b><xsl:value-of select="@type"/></b></td><td>
			<table><tr>
			<xsl:for-each select="wfc:id">
				<td><xsl:value-of select="."/></td>
			</xsl:for-each>
			</tr></table>
			</td></tr>
		</xsl:for-each>
		</table>
	</td></tr>
	
	</table>
  </xsl:for-each>
  
  </body>
  </html>
</xsl:template>

</xsl:stylesheet>