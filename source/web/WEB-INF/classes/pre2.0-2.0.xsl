<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="java" xmlns:xsutil="csi.server.util.XslUtil" exclude-result-prefixes="java xsutil">

  <xsl:output method="xml" indent="yes" encoding="UTF-8" />
  
  <xsl:template match="csi.server.common.model.DataSetOp">
    <DataSetOp>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </DataSetOp>
  </xsl:template>

  <xsl:template match="csi.server.common.model.operator.OpMapItem">
    <OpMapItem>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </OpMapItem>
  </xsl:template>

  <xsl:template match="csi.server.common.model.tableview.TableDef">
    <TableDef>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </TableDef>
  </xsl:template>
  
  <xsl:template match="csi.server.common.model.AnnotationDef">
      <AnnotationDef>
        <xsl:apply-templates select="@*" />
        <xsl:apply-templates select="*" />
      </AnnotationDef>
  </xsl:template>
  
  <xsl:template match="csi.server.common.publishing.Tag">
      <AssetTag>
        <xsl:apply-templates select="@*" />
        <xsl:apply-templates select="*" />
      </AssetTag>
  </xsl:template>  
  
  <xsl:template match="csi.server.common.publishing.Comment">
      <AssetComment>
        <xsl:apply-templates select="@*" />
        <xsl:apply-templates select="*" />
      </AssetComment>
  </xsl:template>  
  
  <xsl:template match="csi.server.common.model.GoogleMapsViewDef">
      <GoogleMapsViewDef>
        <xsl:apply-templates select="@*" />
        <xsl:apply-templates select="*" />
      </GoogleMapsViewDef>
  </xsl:template>  
  
  <xsl:template match="*">
    <xsl:element name="{name()}">
      <xsl:apply-templates select="@*" />
      <xsl:choose>
        <xsl:when test="count(*) &gt; 0">
          <xsl:apply-templates select="*" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="." />
          <xsl:text />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*">
    <xsl:attribute name="{name()}">
        <xsl:value-of select=".">
    </xsl:value-of>
    </xsl:attribute>
  </xsl:template>
</xsl:stylesheet>
