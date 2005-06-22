<!--
  This translates mmbase XML, normally containing an objects tag. The XML related to this XSL is generated by
  org.mmbase.bridge.util.Generator, and the XSL is invoked by FormatterTag.

  @author:  Michiel Meeuwissen
  @version: $Id: mmxf2kupu.xslt,v 1.6 2005-06-22 23:23:29 michiel Exp $
  @since:   MMBase-1.6
-->
<xsl:stylesheet  
  xmlns:xsl ="http://www.w3.org/1999/XSL/Transform"
  xmlns:node ="org.mmbase.bridge.util.xml.NodeFunction"
  xmlns:o="http://www.mmbase.org/xmlns/objects"
  xmlns:mmxf="http://www.mmbase.org/xmlns/mmxf"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns=""
  exclude-result-prefixes="node o mmxf html"
  version = "1.0"
>
  <xsl:import href="2xhtml.xslt" />   <!-- dealing with mmxf is done there -->

  <xsl:output method="xml" 
    omit-xml-declaration="yes" /><!-- xhtml is a form of xml -->


   <!-- If objects is the entrance to this XML, then only handle the root child of it -->
  <xsl:template match="o:objects">
    <xsl:apply-templates select="o:object[1]" />
  </xsl:template>

  <xsl:template match="mmxf:h" mode="h1"><xsl:if test=". != ''"><h1><xsl:apply-templates select="node()" /></h1></xsl:if></xsl:template>
  <xsl:template match="mmxf:h" mode="h2"><xsl:if test=". != ''"><h2><xsl:apply-templates select="node()" /></h2></xsl:if></xsl:template>
  <xsl:template match="mmxf:h" mode="h3"><xsl:if test=". != ''"><h3><xsl:apply-templates select="node()" /></h3></xsl:if></xsl:template>
  <xsl:template match="mmxf:h" mode="h4"><xsl:if test=". != ''"><h4><xsl:apply-templates select="node()" /></h4></xsl:if></xsl:template>
  <xsl:template match="mmxf:h" mode="h5"><xsl:if test=". != ''"><h5><xsl:apply-templates select="node()" /></h5></xsl:if></xsl:template>
  <xsl:template match="mmxf:h" mode="h6"><xsl:if test=". != ''"><h6><xsl:apply-templates select="node()" /></h6></xsl:if></xsl:template>
  <xsl:template match="mmxf:h" mode="h7"><xsl:if test=". != ''"><h7><xsl:apply-templates select="node()" /></h7></xsl:if></xsl:template>
  <xsl:template match="mmxf:h" mode="h8"><xsl:if test=". != ''"><h8><xsl:apply-templates select="node()" /></h8></xsl:if></xsl:template>


  <xsl:template match="/o:objects">
    <xsl:apply-templates select="o:object[1]" />
  </xsl:template>


  <!-- how to present a node -->
  <xsl:template match="o:object">
    <xsl:choose>
      <xsl:when test="o:field[@format='xml'][1]/mmxf:mmxf">
	<xsl:apply-templates select="o:field[@format='xml'][1]/mmxf:mmxf" />
      </xsl:when>
      <xsl:otherwise><!-- should present _something_, FF may hang otherwise -->
	<body>
	  <xsl:apply-templates />
	</body>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <xsl:template match = "mmxf:mmxf" >
    <body>
      <xsl:text>&#xA;</xsl:text>
      <xsl:apply-templates select="mmxf:p|mmxf:table|mmxf:section|mmxf:ul|mmxf:ol|mmxf:table" />
      <xsl:text>&#xA;</xsl:text>
    </body>
    <xsl:text>&#xA;</xsl:text>
  </xsl:template>

  <!-- don't want clickable images, and hope the id can survive in the title -->
  <xsl:template match="o:object[@type = 'images']" mode="inline">
    <xsl:param name="relation" />
    <xsl:variable name="icache" select="node:nodeFunction(., $cloud, string(./o:field[@name='number']), 'cachednode(s(100x100&gt;))')" />
    <img src="{node:function($cloud, string($icache/@id ), 'servletpath()')}" >
      <xsl:attribute name="alt"><xsl:apply-templates select="." mode="title" /></xsl:attribute>
      <xsl:attribute name="class"><xsl:value-of select="$relation/o:field[@name='class']"  /></xsl:attribute>
      <xsl:attribute name="title"><xsl:value-of select="$relation/o:field[@name='id']"  /></xsl:attribute>
      <xsl:if test="$icache/o:field[@name='width']">
	<xsl:attribute name="height"><xsl:value-of select="$icache/o:field[@name='height']" /></xsl:attribute>
	<xsl:attribute name="width"><xsl:value-of select="$icache/o:field[@name='width']" /></xsl:attribute>
      </xsl:if>
    </img> 
  </xsl:template>   


</xsl:stylesheet>
