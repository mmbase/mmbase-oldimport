<%-- a div to include: displays pages on same level as this page
--%><mm:present referid="page">
<mm:list nodes="$page" path="pages1,posrel,pages2" searchdir="source" max="1">
  <mm:first><div class="relatedpages"></mm:first>
  <mm:node element="pages2">
  <mm:related path="posrel,pages" searchdir="destination"
    fields="posrel.pos,pages.number,pages.title"
    orderby="posrel.pos">
    <mm:context><mm:field name="pages.number" id="p" write="false" />
    <mm:compare referid="page" value="$p"><strong></mm:compare>
    <a href="<mm:url>
      <mm:param name="page"><mm:field name="pages.number" /></mm:param>
      <mm:present referid="portal"><mm:param name="portal" value="$portal" /></mm:present>
    </mm:url>"><mm:field name="pages.title" /></a><mm:compare referid="page" value="$p"></strong></mm:compare>
    <mm:last inverse="true">&nbsp;|&nbsp;</mm:last></mm:context>
  </mm:related>
  </mm:node>
  <mm:last></div></mm:last>
</mm:list>
</mm:present>