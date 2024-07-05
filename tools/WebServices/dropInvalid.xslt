<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version='1.0'
	xmlns:xsd="http://www.w3.org/2001/XMLSchema"
>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
  </xsl:copy>
</xsl:template>

<xsl:template match="xsd:element[ @ref = 'xsd:schema' and count( following-sibling::xsd:any ) = 0 ]">
  <xsd:any />
</xsl:template>

<xsl:template match="xsd:element[ @ref = 'xsd:schema' and count( following-sibling::xsd:any ) > 0 ]">
</xsl:template>
<!-- 
-->

</xsl:stylesheet>
