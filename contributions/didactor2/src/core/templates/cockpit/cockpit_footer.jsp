<%@taglib uri="http://www.mmbase.org/mmbase-taglib-2.0" prefix="mm" %>
<%@taglib uri="http://www.didactor.nl/ditaglib_1.0" prefix="di" %>
<mm:content postprocessor="reducespace">
<mm:cloud method="asis">
  <mm:hasrank value="anonymous" inverse="true">
    <jsp:directive.include file="/shared/setImports.jsp" />
    <di:ifsetting component="core" setting="showtranslationbox">
      <di:hasrole role="systemadministrator">
        <mm:treeinclude page="/translation/render.jsp" objectlist="$includePath" referids="$referids" />
      </di:hasrole>
    </di:ifsetting>
  </mm:hasrank>
</mm:cloud>
</mm:content>
</body>
</html>
