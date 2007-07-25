<%@taglib uri="http://www.mmbase.org/mmbase-taglib-2.0" prefix="mm"
%><%@taglib uri="http://www.didactor.nl/ditaglib_1.0" prefix="di" 
%><%@taglib tagdir="/WEB-INF/tags/di/core" prefix="di-t" 
%><%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@page import="java.util.*"
%><%--
TODO: This JSP is much too big, and polluted with all kinds of functionality.

--%><mm:content postprocessor="reducespace" expires="0" language="${requestScope.language}">

<mm:cloud rank="didactor user">
  <mm:include page="/cockpit/cockpit_header.jsp">
    <mm:param name="reset" />
    <mm:param name="extraheader">
      <title><di:translate key="education.learnenvironmenttitle" /></title>
    </mm:param>
  </mm:include>

  <mm:hasnode number="component.drm">
    <mm:treeinclude page="/drm/testlicense.jsp" objectlist="$includePath" referids="$referids "/>
  </mm:hasnode>

  <mm:import externid="reset" />  
  <mm:present referid="reset">
    <mm:write session="learnobject_${education}" value="" />
    <mm:write session="learnobjecttype_${education}" value="" />
  </mm:present>

  <mm:import externid="learnobject_${education}" from="session" id="bookmarked_learnobject" />
  <mm:import externid="learnobject">${bookmarked_learnobject}</mm:import>
  <mm:write session="learnobject_${education}" referid="learnobject" />

  <mm:import externid="learnobject_type_${education}" from="session" id="bookmarked_learnobjecttype" />
  <mm:import externid="learnobjecttype">${bookmarked_learnobjecttype}</mm:import>
  <mm:write session="learnobjecttype_${education}" referid="learnobjecttype" />

  
  <!--
      We are using it to show only one node in the tree
      For cross-education references  
  -->
  <mm:import externid="the_only_node_to_show"/>

  <mm:import externid="frame"/>
  
  <mm:node number="$user">
    <mm:nodelistfunction referids="education" name="blockedLearnBlocks" id="blocked" />
  </mm:node>
  

  <!-- TODO when refreshing the page (F5) the old iframe content is shown -->

  <mm:import externid="justposted" />

  <mm:link page="/education/js/frontend_tree.jsp" referids="$referids">
    <script type="text/javascript" src="${_}"><!-- help IE --></script>      
  </mm:link>

  <mm:listnodescontainer type="classrel">
    <mm:constraint field="snumber" value="${user}" />
    <mm:composite operator="or">
      <mm:constraint field="dnumber" value="${class}" />
      <mm:constraint field="dnumber" value="${education}" />
    </mm:composite>
    <mm:listnodes >
      <%-- is lastpage field ever filled ? --%>
      <script type="text/javascript">
        openContent("learnblocks", ${_node.lastpage});
      </script>
    </mm:listnodes>
  </mm:listnodescontainer>
  


  <div class="rows" id="rows">
    <div class="navigationbar">
      <div class="pathbar">
        <mm:node number="$education">
          <mm:field name="name"/>
        </mm:node>
      </div>    
      <mm:import id="stepNavigator">
        <jsp:directive.include file="prev_next.jsp" />
      </mm:import>
      <div class="stepNavigator">
        <mm:write referid="stepNavigator" escape="none" />
      </div>
    </div>
    <div class="folders">
      <div class="folderHeader">
        <di:translate key="education.education" />
      </div>
      
      <div class="folderLesBody"
           id="education-tree">
        <mm:include page="tree.jspx" />
      </div>
      
   </div>


   <div class="mainContent">
     <div class="contentHeader">
       &nbsp;
     </div>
     <div class="contentBodywit" id="contentBodywit">
       <mm:present referid="the_only_node_to_show">
         <mm:import externid="fb_madetest" required="true" />
         <mm:import externid="return_to" required="true" />
         <mm:import externid="return_to_type" required="true" />
         
         <div align="right">
           <mm:link referids="$referids,learnobjecttype,class,fb_madetest,learnobject@return_to,learnobjecttype@return_to_type" page="index.jsp">
             <input type="submit" class="formbutton" 
                    value="${di:translate(pageContext, 'assessment.back_to_lession_button')}"
                    onClick="parent.document.location.href='${_}'" />
             <%-- WTF is a 'lession' ? --%>
           </mm:link>
         </div>
       </mm:present>

       <div id="contentFrame">...</div>

     </div>
   </div>
  </div>
  
  
  <mm:present referid="frame">
    <script type="text/javascript">
      closeAll();
      openContent('${learnobjectype}','${education}');
      openOnly('div${learnobject}','img${education}');      
    </script>
  </mm:present>
  
  <mm:notpresent referid="frame">
    <script type="text/javascript">
      closeAll();
      
      <%-- we open need menu item in case it is a reference from another education --%>
      <mm:present referid="the_only_node_to_show">
        openContent('${learnobjectype}','${the_only_node_to_show}');
        openOnly('div${the_only_node_to_show}','img${the_only_node_to_show}');
      </mm:present>
      
      
      <mm:notpresent referid="the_only_node_to_show">
        <mm:present referid="learnobject">
          openContent('${learnobjecttype}','${learnobject}');
          openOnly('div${learnobject}','img${learnobject}');
        </mm:present>
        
        <mm:notpresent referid="learnobject">
          if (contentnumber.length >= 1) {
             openContent(contenttype[0],contentnumber[0]);
             openOnly('div'+contentnumber[0],'img'+contentnumber[0]);
          }
        </mm:notpresent>
      </mm:notpresent>
    </script>
  </mm:notpresent>
  <mm:treeinclude page="/cockpit/cockpit_footer.jsp" objectlist="$includePath" referids="$referids "/>
</mm:cloud>
</mm:content>
