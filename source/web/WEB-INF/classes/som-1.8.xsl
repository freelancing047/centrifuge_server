<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="java" xmlns:xsutil="csi.server.util.XslUtil" exclude-result-prefixes="java xsutil">

  <xsl:variable name="CRLF" select="string('&#10;')" />
  <xsl:variable name="SQL_BREAK" select="string(' -- sql_break -- ')" />
  <xsl:variable name="PARAM_REGEX">(?i)\?PARAM\s*\(.*?,.*?,\s*["']?(.*?)["']?\s*,.*?\)</xsl:variable>
  <xsl:variable name="PARAM_REPLACE">{:$1}</xsl:variable>


  <xsl:output method="xml" indent="yes" encoding="UTF-8" />

  <xsl:template match="/DataViewDef | /DataView/meta">
    <xsl:element name="{name()}">
      <xsl:apply-templates select="@*" />
      <xsl:apply-templates select="*[not(self::dataSources | self::modelDef)]" />
      <dataSetOps>
        <xsl:call-template name="createOp">
          <xsl:with-param name="leftOp" select="dataSources/DataSourceDef[last()-1]" />
          <xsl:with-param name="rightOp" select="dataSources/DataSourceDef[last()]" />
        </xsl:call-template>
      </dataSetOps>
      <dataSources>
        <xsl:for-each select="dataSources/DataSourceDef">
          <DataSourceDef>
            <xsl:choose>
              <xsl:when test="@id">
                <xsl:attribute name="reference"><xsl:value-of select="@id" />
                </xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="reference"><xsl:value-of select="generate-id(.)" />
                </xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
          </DataSourceDef>
        </xsl:for-each>
      </dataSources>
      <dataSetParameters>
        <xsl:for-each select="//QueryParameterDef[count(*) &gt; 0]">
          <QueryParameterDef>
            <xsl:apply-templates select="@*" />
            <xsl:apply-templates select="*[not(self::defaultValue | self::value)]" />
            <defaultValues>
              <string><xsl:value-of select="defaultValue" /></string>
            </defaultValues>
            <values>
              <string><xsl:value-of select="value" /></string>
            </values>
          </QueryParameterDef>
        </xsl:for-each>
      </dataSetParameters>
      <xsl:apply-templates select="modelDef" />
    </xsl:element>
  </xsl:template>

  <xsl:template name="createOp">
    <xsl:param name="leftOp" />
    <xsl:param name="rightOp" />
    <xsl:choose>
      <xsl:when test="$leftOp">
        <csi.server.model.DataSetOp>
          <name>Join Operation</name>
          <mapType>JOIN</mapType>
          <joinType>EQUI_JOIN</joinType>
		  <childType>LEFT</childType>
          <children>
            <xsl:for-each select="$leftOp">
              <xsl:choose>
                <xsl:when test="preceding-sibling::DataSourceDef[1]">
                  <xsl:call-template name="createOp">
                    <xsl:with-param name="leftOp" select="preceding-sibling::DataSourceDef[1]" />
                    <xsl:with-param name="rightOp" select="$leftOp" />
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="createTableOp">
                    <xsl:with-param name="opType" select="'LEFT'" />
                    <xsl:with-param name="op" select="$leftOp" />
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
            <xsl:call-template name="createTableOp">
              <xsl:with-param name="opType" select="'RIGHT'" />
              <xsl:with-param name="op" select="$rightOp" />
            </xsl:call-template>
          </children>
          <mapItems>
            <xsl:for-each select="$rightOp/mergeMap/ParamMapEntry">
              <csi.server.model.OpMapItem>
                <fromTableLocalId>
                  <xsl:call-template name="choose-local-name">
                    <xsl:with-param name="name1" select="$leftOp/localName" />
                    <xsl:with-param name="node" select="$leftOp" />
                  </xsl:call-template>
                </fromTableLocalId>
                <fromColumnLocalId>
                  <xsl:choose>
                    <xsl:when test="fieldDef/@reference">
                      <xsl:variable name="ref" select="fieldDef/@reference" />
                      <xsl:value-of select="generate-id(/*//*[@id=$ref])" />
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="generate-id(fieldDef)" />
                    </xsl:otherwise>
                  </xsl:choose>
                </fromColumnLocalId>
                <toTableLocalId>
                  <xsl:call-template name="choose-local-name">
                    <xsl:with-param name="name1" select="$rightOp/localName" />
                    <xsl:with-param name="node" select="$rightOp" />
                  </xsl:call-template>
                </toTableLocalId>
                <toColumnLocalId>
                  <xsl:value-of select="generate-id(.)" />
                </toColumnLocalId>
              </csi.server.model.OpMapItem>
            </xsl:for-each>
          </mapItems>
        </csi.server.model.DataSetOp>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="createTableOp">
          <xsl:with-param name="opType" select="'RIGHT'" />
          <xsl:with-param name="op" select="$rightOp" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>


  <xsl:template name="createTableOp">
    <xsl:param name="opType" />
    <xsl:param name="op" />
    <xsl:for-each select="$op">
      <csi.server.model.DataSetOp>
        <name>Table <xsl:value-of select="ordinal" /></name>
        <xsl:if test="$opType">
          <childType><xsl:value-of select="$opType" /></childType>
        </xsl:if>
        <tableDef>
          <localId>
            <xsl:call-template name="choose-local-name">
              <xsl:with-param name="name1" select="localName" />
              <xsl:with-param name="node" select="$op" />
            </xsl:call-template>
          </localId>
          <sqlTable>true</sqlTable>
          <tableName>Custom Query <xsl:if test="ordinal &gt; 0"><xsl:value-of select="ordinal" /></xsl:if></tableName>
          <source>
            <xsl:apply-templates select="@*" />
            <xsl:if test="not(@id)">
              <xsl:attribute name="id"><xsl:value-of select="generate-id(.)" /></xsl:attribute>
            </xsl:if>
            <localId>
              <xsl:call-template name="choose-local-name">
                <xsl:with-param name="name1" select="localName" />
                <xsl:with-param name="node" select="$op" />
              </xsl:call-template>
            </localId>
            <name>
              <xsl:value-of select="connection/type" />
              <xsl:if test="ordinal &gt; 0">
                <xsl:value-of select="ordinal" />
              </xsl:if>
            </name>
            <connection>
              <xsl:apply-templates select="connection/@*" />
              <xsl:apply-templates select="connection/*[not(name()='type')]" />
			  <type>
			  	<xsl:choose>
					<xsl:when test="xsutil:lowerCase(connection/type)='legacy'">
			  			<xsl:value-of select="'legacy'"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="connection/type"/>
					</xsl:otherwise>
				</xsl:choose>
			  </type>

              <xsl:variable name="preSql">
                <xsl:for-each select="query/interceptors/QueryInterceptorDef[type='PRE']">
                  <xsl:sort select="ordinal" data-type="number" />
                  <xsl:variable name="curOrdinal" select="ordinal" />
                  <xsl:if test="../QueryInterceptorDef[(type='PRE' and ordinal &lt; $curOrdinal)]">
                    <xsl:value-of select="$SQL_BREAK" />
                  </xsl:if>
                  <xsl:value-of select="queryText" />
                </xsl:for-each>
              </xsl:variable>

              <xsl:variable name="postSql">
                <xsl:for-each select="query/interceptors/QueryInterceptorDef[type='POST']">
                  <xsl:sort select="ordinal" data-type="number" />
                  <xsl:variable name="curOrdinal" select="ordinal" />
                  <xsl:if test="../QueryInterceptorDef[(type='POST' and ordinal &lt; $curOrdinal)]">
                    <xsl:value-of select="$SQL_BREAK" />
                  </xsl:if>
                  <xsl:value-of select="queryText" />
                </xsl:for-each>
              </xsl:variable>

              <preSql>
                <xsl:value-of select="xsutil:replaceAll(string($preSql), $PARAM_REGEX, $PARAM_REPLACE)" />
              </preSql>
              <postSql>
                <xsl:value-of select="xsutil:replaceAll(string($postSql), $PARAM_REGEX, $PARAM_REPLACE)" />
              </postSql>
            </connection>
            <query />
            <xsl:apply-templates select="*[not(self::localName | self::query | self::mergeMap | self::connection)]" />

            <sqlTables>
              <csi.server.model.TableDef>
                <localId>
                  <xsl:call-template name="choose-local-name">
                    <xsl:with-param name="name1" select="localName" />
                    <xsl:with-param name="node" select="$op" />
                  </xsl:call-template>
                </localId>
                <sqlTable>true</sqlTable>
                <tableName>Custom Query <xsl:if test="ordinal &gt; 0"><xsl:value-of select="ordinal" /></xsl:if></tableName>
                <xsl:call-template name="createCustomQuery" />
                <xsl:call-template name="createColumns">
                  <xsl:with-param name="op" select="$op" />
                  <xsl:with-param name="genId" select="false()" />
                  <xsl:with-param name="selected" select="false()" />
                </xsl:call-template>
              </csi.server.model.TableDef>
            </sqlTables>
          </source>
          <xsl:call-template name="createCustomQuery" />
          <xsl:call-template name="createColumns">
            <xsl:with-param name="op" select="$op" />
            <xsl:with-param name="genId" select="true()" />
            <xsl:with-param name="selected" select="true()" />
          </xsl:call-template>
        </tableDef>
      </csi.server.model.DataSetOp>
    </xsl:for-each>

  </xsl:template>

  <xsl:template name="createColumns">
    <xsl:param name="op" />
    <xsl:param name="genId" />
    <xsl:param name="selected" />
    <columns>
      <xsl:choose>
        <xsl:when test="count(/*//dataSources/DataSourceDef) &gt; 1">
          <xsl:variable name="fieldDefs" select="/*//FieldDef[(dsLocalName=$op/localName and fieldType='COLUMN_REF')] | /*//mergeMap/ParamMapEntry/fieldDef[dsLocalName=$op/localName]" />

          <xsl:for-each select="$fieldDefs">
            <xsl:sort data-type="number" select="ordinal" />
            <xsl:call-template name="createColumnFromField">
              <xsl:with-param name="makeId" select="$genId" />
              <xsl:with-param name="selected" select="$selected" />
              <xsl:with-param name="field" select="." />
            </xsl:call-template>
          </xsl:for-each>

          <xsl:for-each select="$op/mergeMap/ParamMapEntry">
            <xsl:variable name="pname" select="./paramName" />
            <xsl:if test="count($fieldDefs[columnName=$pname and dsLocalName=$op/localName])=0">
              <xsl:choose>
                <xsl:when test="fieldDef/@reference">
                  <xsl:variable name="refid" select="fieldDef/@reference" />
                  <xsl:call-template name="createColumnFromParam">
                    <xsl:with-param name="makeId" select="$genId" />
                    <xsl:with-param name="param" select="paramName" />
                    <xsl:with-param name="field" select="/*//*[@id=$refid]" />
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="createColumnFromParam">
                    <xsl:with-param name="makeId" select="$genId" />
                    <xsl:with-param name="param" select="paramName" />
                    <xsl:with-param name="field" select="fieldDef" />
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="/*//FieldDef[fieldType='COLUMN_REF']">
            <xsl:call-template name="createColumnFromField">
              <xsl:with-param name="makeId" select="$genId" />
              <xsl:with-param name="selected" select="$selected" />
              <xsl:with-param name="field" select="." />
            </xsl:call-template>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>

    </columns>
  </xsl:template>

  <xsl:template name="createColumnFromField">
    <xsl:param name="makeId" />
    <xsl:param name="selected" />
    <xsl:param name="field" />
    <ColumnDef>
      <xsl:if test="$makeId">
        <localId><xsl:value-of select="generate-id($field)" /></localId>
      </xsl:if>
      <columnName>
        <xsl:choose>
            <xsl:when test="$field/columnName">
              <xsl:value-of select="$field/columnName" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$field/fieldName" />
            </xsl:otherwise>
        </xsl:choose>
      </columnName>
      <xsl:if test="$field/ordinal">
        <ordinal>
          <xsl:value-of select="$field/ordinal" />
        </ordinal>
      </xsl:if>
      <csiType>
        <xsl:value-of select="$field/cacheType" />
      </csiType>
      <overrideCsiType>
        <xsl:value-of select="$field/valueType" />
      </overrideCsiType>
      <selected>
        <xsl:value-of select="$selected" />
      </selected>

    </ColumnDef>
  </xsl:template>

  <xsl:template name="createColumnFromParam">
    <xsl:param name="makeId" />
    <xsl:param name="param" />
    <xsl:param name="field" />

    <ColumnDef>
      <xsl:if test="$makeId">
        <localId><xsl:value-of select="generate-id(.)" /></localId>
      </xsl:if>
      <columnName>
        <xsl:value-of select="$param" />
      </columnName>
      <xsl:if test="$field/ordinal">
        <ordinal>
          <xsl:value-of select="$field/ordinal" />
        </ordinal>
      </xsl:if>
      <csiType>
        <xsl:value-of select="$field/cacheType" />
      </csiType>
      <overrideCsiType>
        <xsl:value-of select="$field/valueType" />
      </overrideCsiType>
      <selected>false</selected>
    </ColumnDef>
  </xsl:template>

  <xsl:template name="createCustomQuery">
    <customQuery>
      <xsl:apply-templates select="query/@*" />

      <queryText>
        <xsl:choose>
          <xsl:when test="query/queryText">
            <xsl:value-of select="xsutil:replaceAll(string(query/queryText), $PARAM_REGEX, $PARAM_REPLACE)" />
          </xsl:when>
          <xsl:when test="query//Property[name='query.tableName' or name='query.worksheetName']">
            <xsl:text>select * from </xsl:text>
            <xsl:value-of select="query//Property[name='query.tableName' or name='query.worksheetName'][1]/value" />
          </xsl:when>
          <xsl:when test="query//Property[name='csi.remoteFilePath']">
            <xsl:text>select * from </xsl:text>
            <xsl:value-of select="query//Property[name='csi.remoteFilePath'][1]/value" />
          </xsl:when>

        </xsl:choose>
      </queryText>

      <xsl:apply-templates select="query/*[not(self::uuid | self::queryText | self::parameters | self::interceptors | self::timeout)]" />
    </customQuery>
  </xsl:template>

  <xsl:template match="MoreDetailQuery">
  	<MoreDetailQuery>
	  	<xsl:apply-templates select="@*"/>
		<dataViewDefId><xsl:value-of select="xsutil:mapLinkupUuid(string(dataViewName), string(dataViewDefId))"/></dataViewDefId>
		<xsl:apply-templates select="*[not(self::dataViewDefId)]"/>
	</MoreDetailQuery>
  </xsl:template>

  <xsl:template match="/*//modelDef/fieldDefs/FieldDef">
    <FieldDef>
      <xsl:choose>
        <xsl:when test="@reference">
          <xsl:variable name="refid" select="@reference" />
          <xsl:choose>
            <xsl:when test="/*//dataSources/DataSourceDef/mergeMap/ParamMapEntry/fieldDef[@id=$refid]">
              <xsl:for-each select="/*//dataSources/DataSourceDef/mergeMap/ParamMapEntry/fieldDef[@id=$refid]">
                <xsl:call-template name="copy-field" />
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="@*" />
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="copy-field" />
        </xsl:otherwise>
      </xsl:choose>
    </FieldDef>
  </xsl:template>

  <xsl:template name="copy-field">
    <xsl:apply-templates select="@*" />
    <xsl:apply-templates select="*[not(self::dsLocalName)]" />

    <dsLocalId>
      <xsl:call-template name="choose-local-name">
        <xsl:with-param name="name1" select="dsLocalName" />
        <xsl:with-param name="name2" select="/*/dataSources/DataSourceDef[1]/localName" />
        <xsl:with-param name="node" select="/*/dataSources/DataSourceDef[1]" />
      </xsl:call-template>
    </dsLocalId>
    <tableLocalId>
      <xsl:call-template name="choose-local-name">
        <xsl:with-param name="name1" select="dsLocalName" />
        <xsl:with-param name="name2" select="/*/dataSources/DataSourceDef[1]/localName" />
        <xsl:with-param name="node" select="/*/dataSources/DataSourceDef[1]" />
      </xsl:call-template>
    </tableLocalId>
    <columnLocalId>
      <xsl:value-of select="generate-id(.)" />
    </columnLocalId>
  </xsl:template>

  <xsl:template name="choose-local-name">
    <xsl:param name="name1" />
    <xsl:param name="name2" />
    <xsl:param name="node" />
    <xsl:choose>
      <xsl:when test="$name1 and not($name1='')">
        <xsl:value-of select="$name1" />
      </xsl:when>
      <xsl:when test="$name2 and not($name2='')">
        <xsl:value-of select="$name2" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="generate-id($node)" />
      </xsl:otherwise>
    </xsl:choose>
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
