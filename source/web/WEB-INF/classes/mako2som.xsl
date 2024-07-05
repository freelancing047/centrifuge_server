<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method = "xml" indent = "yes" encoding="UTF-8"/>  

<xsl:template match="DataViewDef/datasource | DataView/meta/datasource">
          <dataSources>
              <DataSourceDef>
                <connection>
                    <xsl:apply-templates select="@*"/>
                    <xsl:apply-templates select="*"/>
                </connection>
                <xsl:copy-of select="../query"/>
              </DataSourceDef>
          </dataSources>                 
</xsl:template>

<xsl:template match="DataView">
    <xsl:element name="{name()}">
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates select="*"/>
        <xsl:if test="count(type) = 0">
            <type>BASIC</type>
        </xsl:if>
    </xsl:element>
</xsl:template>

<xsl:template match="centrifuge.runtime.publishing.Asset">
    <centrifuge.runtime.publishing.PdfAsset>
        <xsl:apply-templates select="@*"/>
        <description><xsl:value-of select="comment"/></description>
        <xsl:apply-templates select="*[not(self::comment | self::type | self::id)]"/>
    </centrifuge.runtime.publishing.PdfAsset>
</xsl:template>

<xsl:template match="csi.dataview.security.ACL | csi.server.model.security.ACL">
    <csi.server.model.security.ACL>
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates select="*[not(self::id)]"/>
    </csi.server.model.security.ACL>
</xsl:template>

<xsl:template match="csi.dataview.security.AccessControlEntry | csi.server.model.security.AccessControlEntry">
    <csi.server.model.security.AccessControlEntry>
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates select="*[not(self::id)]"/>
    </csi.server.model.security.AccessControlEntry>
</xsl:template>

<xsl:template match="centrifuge.model.Group | centrifuge.model.User">
    <xsl:element name="{name()}">
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates select="*[not(self::id)]"/>
    </xsl:element>
</xsl:template>

<xsl:template match="DataViewDef/query | DataView/meta/query"/>

<xsl:template match="*">
    <xsl:element name="{name()}">
        <xsl:apply-templates select="@*"/>
        <xsl:choose>
            <xsl:when test="count(*) &gt; 0">
                <xsl:apply-templates select="*"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="."/> 
                <xsl:text/>
            </xsl:otherwise>
        </xsl:choose>        
    </xsl:element>
</xsl:template>

<xsl:template match="@*">
    <xsl:attribute name="{name()}"><xsl:value-of select="."></xsl:value-of></xsl:attribute>
</xsl:template>

</xsl:stylesheet> 