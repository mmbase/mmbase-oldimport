<!--
  This translates mmbase XML, normally containing an objects tag. The XML related to this XSL is generated by
  org.mmbase.bridge.util.Generator, and the XSL is invoked by FormatterTag.

  @author:  Michiel Meeuwissen
  @version: $Id: 2rich.xslt,v 1.1 2005-05-18 22:32:38 michiel Exp $
  @since:   MMBase-1.6
-->
<xsl:stylesheet  
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:node="org.mmbase.bridge.util.xml.NodeFunction"
    xmlns:o="http://www.mmbase.org/objects"
    xmlns:mmxf="http://www.mmbase.org/mmxf"
    exclude-result-prefixes="node mmxf o"
    version="1.0" >

  <xsl:import href="mmxf2rich.xslt" />   <!-- dealing with mmxf is done there -->

  <xsl:output method="text" omit-xml-declaration="yes" /> <!-- xhtml is a form of xml -->

  <xsl:param name="cloud">mmbase</xsl:param>

  <xsl:template match="o:objects">
    <div class="objects">
      <xsl:apply-templates select="o:object[1]" />
    </div>
  </xsl:template>


  <!-- how to present a node -->
  <xsl:template match="o:object">
    <xsl:apply-templates select="o:field" />
  </xsl:template>



  <xsl:template match="o:field">

  </xsl:template>

  <xsl:template match="o:field[@format='xml']">
    <xsl:apply-templates  />
  </xsl:template>



  <!-- template to override mmxf tags with an 'id', we support links to it here -->
  <xsl:template match="mmxf:*" mode="rels">
    <!-- store the 'relation' nodes for convenience in $rels:-->
    <xsl:variable name="rels"  select="ancestor::o:object/o:relation[@role='idrel']" />
    <xsl:variable name="id"   select="@id" />
    
    <!-- also for conveniences: all related nodes to this node-->
    <xsl:variable name="related_to_node"   select="//o:objects/o:object[@id=$rels/@related]" />

    <!-- There are two type of relations, it is handy to treat them seperately: -->
    <xsl:variable name="srelations" select="//o:objects/o:object[@id=$rels[@type='source']/@object and o:field[@name='id'] = current()/@id]" />
    <xsl:variable name="drelations" select="//o:objects/o:object[@id=$rels[@type='destination']/@object and o:field[@name='id'] = current()/@id]" />


    <!-- now link the relationnodes with the nodes related to this node, the find the 'relatednodes' -->

    <xsl:variable name="relatednodes" select="$related_to_node[@id = $srelations/o:field[@name = 'dnumber']] | $related_to_node[@id = $drelations/o:field[@name='snumber']]" />
    
    <xsl:if test="count($relatednodes) &gt; 0" >
      <xsl:text>[</xsl:text><xsl:value-of select="@id" /><xsl:text>]</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mmxf:a">
    <!-- store the 'relation' nodes for convenience in $rels:-->
    <xsl:variable name="rels"  select="ancestor::o:object/o:relation[@role='idrel']" />
    <xsl:variable name="id"   select="@id" />
    
    <!-- also for conveniences: all related nodes to this node-->
    <xsl:variable name="related_to_node"   select="//o:objects/o:object[@id=$rels/@related]" />
   
    <!-- There are two type of relations, it is handy to treat them seperately: -->
    <xsl:variable name="srelations" select="//o:objects/o:object[@id=$rels[@type='source']/@object and o:field[@name='id'] = current()/@id]" />
    <xsl:variable name="drelations" select="//o:objects/o:object[@id=$rels[@type='destination']/@object and o:field[@name='id'] = current()/@id]" />


    <!-- now link the relationnodes with the nodes related to this node, the find the 'relatednodes' -->

    <xsl:variable name="relatednodes" select="$related_to_node[@id = $srelations/o:field[@name = 'dnumber']] | $related_to_node[@id = $drelations/o:field[@name='snumber']]" />
    
    <xsl:choose>
      <xsl:when test="count($relatednodes) &gt; 0" >
	<xsl:text>[</xsl:text><xsl:value-of select="@id" />:<xsl:apply-templates /><xsl:text>]</xsl:text>
      </xsl:when>
      <xsl:otherwise>
	<xsl:apply-templates select="*" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



</xsl:stylesheet>
