<% // *** one article, with shorties and teasers  *** %>
<%@include file="includes/top0.jsp" %>
<mm:cloud jspvar="cloud">
<%@include file="includes/top1_params.jsp" %>
<%@include file="includes/top2_cacheparams.jsp" %>
<cache:cache groups="<%= paginaID %>" key="<%= cacheKey %>" time="<%= expireTime %>" scope="application">
<%@include file="includes/top3_nav.jsp" %>
<%@include file="includes/top4_head.jsp" %>
<%@include file="includes/top5_breadcrumbs_and_pano.jsp" %>
<mm:node number="<%= paginaID %>">
<%@include file="actie/includes/navsettings.jsp" %>
<% if(artikelID.equals("-1")) { %>
	<mm:relatednodes type="artikel" path="contentrel,artikel" orderby="begindatum" directions="down" max="1">
       <mm:field name="number" jspvar="artikel_number" vartype="String" write="false">
          <% artikelID = artikel_number;%>
       </mm:field>
    </mm:relatednodes><%
} %>
<br>
<table width="744" border="0" cellspacing="0" cellpadding="0" align="center" valign="top">
<tr>
	<td style="vertical-align:top;padding:10px;padding-top:0px;width:185px;">
   	<%@include file="includes/navleft.jsp" %>
      <br />
   	<jsp:include page="includes/teaser.jsp">
         <jsp:param name="s" value="<%= paginaID %>" />
         <jsp:param name="r" value="<%= rubriekID %>" />
         <jsp:param name="rs" value="<%= styleSheet %>" />
         <jsp:param name="sr" value="0" />
      </jsp:include>
	</td>
	<td style="vertical-align:top;width:100%;padding-left:10px;padding-right:10px;text-align:right;">
	   <jsp:include page="includes/artikel_12_column.jsp">
         <jsp:param name="r" value="<%= rubriekID %>" />
         <jsp:param name="rs" value="<%= styleSheet %>" />
         <jsp:param name="lnr" value="<%= lnRubriekID %>" />
         <jsp:param name="rnimageid" value="<%= rnImageID %>" />
         <jsp:param name="p" value="<%= paginaID %>" />
         <jsp:param name="a" value="<%= artikelID %>" />
         <jsp:param name="showpageintro" value="true" />
			<jsp:param name="shownav" value="true" />
      </jsp:include>
	</td>
</tr>
</table>
<%@include file="includes/footer.jsp" %>
</mm:node>
</cache:cache>
</mm:cloud>



