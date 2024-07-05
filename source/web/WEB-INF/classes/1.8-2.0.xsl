<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="java" xmlns:xsutil="csi.server.util.XslUtil" exclude-result-prefixes="java xsutil">

  <xsl:output method="xml" indent="yes" encoding="UTF-8" />
  
  <xsl:template match="modelDef">
  	
  	<xsl:call-template name="resetCounter">
  		<xsl:with-param name="dummy" select="xsutil:resetCounter()"/>
  	</xsl:call-template>
  	
    <xsl:element name="{name()}">
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*[not(self::optionSetName | self::nodeDefs | self::linkDefs | self::bundleDefs )]" />
	  
      <worksheets>
        <WorksheetDef>        
          <uuid><xsl:value-of select="xsutil:randomUuid()"/></uuid>
          <worksheetName>Untitled Worksheet</worksheetName>
          <annotations/>
          <visualizations>
            <xsl:for-each select="visualizations/*[ (name()='TableViewDef' and count(visibleFields/*) &gt; 0) or (name()='RelGraphViewDef' and count(nodeDefs/*) &gt; 0) or (name()='ChartViewDef' and count(dimensions/*) &gt; 0) or (name()='TimelineViewDef' and count(eventDefs/*) &gt; 0) or (name()='DrillDownChartViewDef' and count(dimensions/*) &gt; 0) ]">
              <xsl:element name="{name()}">
                <xsl:attribute name="reference">
                            <xsl:value-of select="@id" />
                        </xsl:attribute>
              </xsl:element>
            </xsl:for-each>
          </visualizations>
        </WorksheetDef>
      </worksheets>
    </xsl:element>
  </xsl:template>

  <xsl:template match="TableViewDef">
    <xsl:if test="count(visibleFields/*) &gt; 0">
      <xsl:element name="{name()}">
        <xsl:apply-templates select="@*" />
        <xsl:apply-templates select="*[not(self::clientProperties | self::name )]" />
  
        <xsl:if test="not(type)">
          <type>TABLE</type>
        </xsl:if>
        <name>Table</name>
        <isAttached>false</isAttached>
        
        <xsl:call-template name="update-client-props">
          <xsl:with-param name="prop" select="clientProperties" />
        </xsl:call-template>
      </xsl:element>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="RelGraphViewDef">
    <xsl:if test="count(nodeDefs/*) &gt; 0">
      <xsl:element name="{name()}">
        <xsl:apply-templates select="@*" />
        <xsl:apply-templates select="*[not(self::nodeDefs | self::linkDefs | self::clientProperties | self::name )]" />
  
        <xsl:if test="not(type)">
          <type>RELGRAPH_V2</type>
        </xsl:if>
        
        <name>Relationship Graph</name>
        <isAttached>false</isAttached>
        
        <xsl:call-template name="update-client-props">
          <xsl:with-param name="prop" select="clientProperties" />
        </xsl:call-template>
  
        <xsl:if test="//modelDef/optionSetName">
          <optionSetName>
            <xsl:value-of select="//modelDef/optionSetName" />
          </optionSetName>
        </xsl:if>
  
        <xsl:call-template name="deref-list">
          <xsl:with-param name="list" select="nodeDefs" />
        </xsl:call-template>
        <xsl:call-template name="deref-list">
          <xsl:with-param name="list" select="linkDefs" />
        </xsl:call-template>
        <xsl:apply-templates select="//modelDef/bundleDefs" />
      </xsl:element>
    </xsl:if>
  </xsl:template>  
  
  <xsl:template match="ChartViewDef">
    <xsl:if test="count(dimensions/*) &gt; 0">
      <xsl:element name="{name()}">
        <xsl:apply-templates select="@*" />
        <xsl:apply-templates select="*[not(self::clientProperties | self::name )]" />
  
        <xsl:if test="not(type)">
          <type>CHART</type>
        </xsl:if>
        
        <xsl:if test="not(chartType)">
        	<chartType>Spreadsheet</chartType>
        </xsl:if>
        	
        <name>Chart</name>
        <isAttached>false</isAttached>
        
        <xsl:call-template name="update-client-props">
          <xsl:with-param name="prop" select="clientProperties" />
        </xsl:call-template>
      </xsl:element>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="ChartViewDef/*//DimensionField/fieldDef">
	<xsl:call-template name="check-duplicate-field">
		<xsl:with-param name="element" select="."/>
	</xsl:call-template>
  </xsl:template>
  
  <xsl:template match="MapChartViewDef">
  </xsl:template>
  
  <xsl:template match="TimelineViewDef">
    <xsl:if test="count(eventDefs/*) &gt; 0">
      <xsl:element name="{name()}">
        <xsl:apply-templates select="@*" />
        <xsl:apply-templates select="*[not(self::eventDefs | self::clientProperties  | self::name )]" />
  
        <xsl:if test="not(type)">
          <type>TIMELINE</type>
        </xsl:if>
        
        <name>Timeline</name>
        <isAttached>false</isAttached>
        
        <xsl:call-template name="update-client-props">
          <xsl:with-param name="prop" select="clientProperties" />
        </xsl:call-template>
  
        <eventDefs>
          <xsl:for-each select="eventDefs/EventDef">
            <EventDef>
              <xsl:apply-templates select="@*" />
              <xsl:apply-templates select="*[not(self::eventNode)]" />
              <xsl:if test="eventNode">
                <eventNode>
                  <xsl:call-template name="deref-elem">
                    <xsl:with-param name="element" select="eventNode" />
                  </xsl:call-template>
                </eventNode>
              </xsl:if>
            </EventDef>
          </xsl:for-each>
        </eventDefs>
      </xsl:element>
    </xsl:if>
  </xsl:template>  
  
  <xsl:template match="GeoGraphViewDef">
  </xsl:template>  

  <xsl:template match="DrillDownChartViewDef">
    <xsl:if test="count(dimensions/*) &gt; 0">
      <xsl:element name="{name()}">
        <xsl:apply-templates select="@*" />
        <xsl:apply-templates select="*[not(self::clientProperties | self::name )]" />
        
        <xsl:if test="not(type)">
          <type>BAR_CHART</type>
        </xsl:if>
        
        
        <name>Drill Chart</name>
        <isAttached>false</isAttached>
        
        <xsl:call-template name="update-client-props">
          <xsl:with-param name="prop" select="clientProperties" />
          <xsl:with-param name="index" select="xsutil:incrementCounter()" />
        </xsl:call-template>
      </xsl:element>
    </xsl:if>
  </xsl:template>

  <xsl:template match="csi.server.common.model.worksheet.WorksheetDef">
    <WorksheetDef>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </WorksheetDef>
  </xsl:template>

  <xsl:template match="csi.server.model.DataSetOp">
    <DataSetOp>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </DataSetOp>
  </xsl:template>
  
  <xsl:template match="csi.server.model.OpMapItem">
    <OpMapItem>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </OpMapItem>
  </xsl:template>  
  
  <xsl:template match="csi.server.model.TableDef">
    <TableDef>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </TableDef>
  </xsl:template>

  <xsl:template match="csi.server.model.ColumnFilter">
    <ColumnFilter>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </ColumnFilter>
  </xsl:template>

  <xsl:template match="centrifuge.runtime.publishing.PdfAsset">
    <PdfAsset>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </PdfAsset>
  </xsl:template>

  <xsl:template match="centrifuge.runtime.publishing.LiveAsset">
    <LiveAsset>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </LiveAsset>
  </xsl:template>

  <xsl:template match="csi.server.model.security.ACL">
    <ACL>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </ACL>
  </xsl:template>

  <xsl:template match="csi.server.model.security.AccessControlEntry">
    <AccessControlEntry>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </AccessControlEntry>
  </xsl:template>

  <xsl:template match="centrifuge.model.Group">
    <Group>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </Group>
  </xsl:template>

  <xsl:template match="centrifuge.model.User">
    <User>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </User>
  </xsl:template>
  
  <xsl:template match="centrifuge.runtime.publishing.Tag">
    <AssetTag>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </AssetTag>
  </xsl:template>  
  
  <xsl:template match="centrifuge.runtime.publishing.Comment">
    <AssetComment>
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*" />
    </AssetComment>
  </xsl:template>    
  
  <xsl:template name="deref-list">
    <xsl:param name="list" />
    <xsl:element name="{name($list)}">
      <xsl:for-each select="$list/*">
        <xsl:element name="{name()}">
          <xsl:call-template name="deref-elem">
            <xsl:with-param name="element" select="." />
          </xsl:call-template>
        </xsl:element>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>

  <xsl:template name="deref-elem">
    <xsl:param name="element" />
    <xsl:if test="$element">
      <xsl:choose>
        <xsl:when test="$element/@reference">
          <xsl:variable name="src" select="//*[@id=$element/@reference]" />
          <xsl:if test="$src">
            <xsl:apply-templates select="$src/@*" />
            <xsl:apply-templates select="$src/*" />
          </xsl:if>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="$element/@*" />
          <xsl:apply-templates select="$element/*" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="check-duplicate-field">
    <xsl:param name="element" />
  	<xsl:element name="fieldDef">
  		<xsl:choose>
	  		<xsl:when test="//*/modelDef/fieldDefs/FieldDef[uuid=$element/uuid]">
	  			<xsl:attribute name="reference">
	  				<xsl:value-of select="//*/modelDef/fieldDefs/FieldDef[uuid=$element/uuid]/@id" />
	  			</xsl:attribute>
	  		</xsl:when>	  		
	  		<xsl:when test="//*/modelDef/fieldDefs/FieldDef[fieldName=$element/fieldName]">
	  			<xsl:attribute name="reference">
	  				<xsl:value-of select="//*/modelDef/fieldDefs/FieldDef[fieldName=$element/fieldName]/@id" />
	  			</xsl:attribute>
	  		</xsl:when>
	  		<xsl:otherwise>
	  			<xsl:apply-templates select="@*" />
        		<xsl:apply-templates select="*" />
	  		</xsl:otherwise>
  		</xsl:choose>
  	</xsl:element>
  </xsl:template>  

  <xsl:template name="update-client-props">
    <xsl:param name="prop" />
    
    <xsl:variable name="index" select="xsutil:incrementCounter()"/>
    <xsl:element name="clientProperties">
      <xsl:apply-templates select="$prop/@*" />
      <xsl:apply-templates select="$prop/*" />
      
      <xsl:if test="not($prop/entry[string='vizBox.left'])">
        <entry>
          <string>vizBox.left</string>
          <int><xsl:value-of select="$index * 50 + 50" /></int>
        </entry>
        <entry>
          <string>vizBox.top</string>
          <int><xsl:value-of select="$index * 50" /></int>
        </entry>
        <entry>
          <string>vizBox.height</string>
          <int>400</int>
        </entry>
          <entry>
        <string>vizBox.width</string>
          <int>800</int>
        </entry>
      </xsl:if>
      <xsl:if test="not($prop/entry[string='vizBox.loadOnStartup'])">
        <entry>
          <string>vizBox.loadOnStartup</string>
          <boolean>true</boolean>
        </entry>
      </xsl:if>
    
      <xsl:if test="name($prop/..)='RelGraphViewDef'">
      	<entry>
      		<string>render.threshold</string>
      		<int>2147483647</int>
      	</entry>
      </xsl:if>
      
    </xsl:element>
    
    

  </xsl:template>
  
  <xsl:template name="resetCounter">
  	<xsl:param name="dummy"/>
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
