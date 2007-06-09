<%@ page import = "java.util.HashSet" %>
<%@taglib uri="http://www.mmbase.org/mmbase-taglib-2.0" prefix="mm" %>
<mm:import id="lastIt" jspvar="lastIt" reset="true"><mm:field name="name"/></mm:import>
<%
String lastItem = "";
if(depth!=1){
	lastItem=" > "+lastIt;
}
%>
<mm:remove referid="type_of_node"/>
<mm:nodeinfo id="type_of_node" type="type" jspvar="sNodeType" vartype="String">

  <mm:compare referid="type_of_node" value="learnblocks">
    <table border="0" cellpadding="0" cellspacing="0">
      <tr>
        <mm:compare referid="the_last_parent" value="true" inverse="true">
          <td><img src="gfx/tree_spacer.gif" width="32px" height="16px" border="0" align="center" valign="middle"/></td>
          <td><img src="gfx/tree_vertline.gif" border="0" align="center" valign="middle"/></td>
        </mm:compare>
        <mm:compare referid="the_last_parent" value="true">
          <td><img src="gfx/tree_spacer.gif" width="48px" height="16px" border="0" align="center" valign="middle"/></td>
        </mm:compare>
        
        
        <%@include file="tree_shift_child.jsp" %>
        
        <mm:import id="dummyname" escape="lowercase" reset="true"><mm:nodeinfo nodetype="learnblocks" type="guitype"/></mm:import>
        <td><img src="gfx/tree_vertline-leaf.gif" border="0" align="center" valign="middle"/></td>
        <td><img src="gfx/new_education.gif" width="16" border="0" align="middle" /></td>
        <td><nobr>
          <mm:link referid="wizardjsp" referids="_node@origin">
            <mm:param name="wizard">config/learnblocks/learnblocks-origin</mm:param>
            <mm:param name="objectnumber">new</mm:param>
            <mm:param name="path"><%=session.getAttribute("eduname")%> > <%= session.getAttribute("path") %> <%=lastItem %></mm:param>             
            <a href="${_}" title="${di:translate(pageContext, 'education.new')}  ${dummyname} ${di:translate(pageContext, 'education.aanmaken')}" 
               target="text">
            <di:translate key="education.new" /> ${dummyname} <di:translate key="education.aanmaken" /></a>
          </mm:link>
        </nobr></td>
      </tr>
    </table><!-- the sillyness doesn't stop! -->
    <table border="0" cellpadding="0" cellspacing="0">
      <tr>
        <mm:compare referid="the_last_parent" value="true" inverse="true">
          <td><img src="gfx/tree_spacer.gif" width="32px" height="16px" border="0" align="center" valign="middle"/></td>
          <td><img src="gfx/tree_vertline.gif" border="0" align="center" valign="middle"/></td>
        </mm:compare>
        <mm:compare referid="the_last_parent" value="true">
          <td><img src="gfx/tree_spacer.gif" width="48px" height="16px" border="0" align="center" valign="middle"/></td>
        </mm:compare>
        
        
        <%@include file="tree_shift_child.jsp" %>
        
        <mm:import id="dummyname" escape="lowercase" reset="true"><mm:nodeinfo nodetype="pages" type="guitype"/></mm:import>
        <td><img src="gfx/tree_vertline-leaf.gif" border="0" align="center" valign="middle"/></td>
        <td><img src="gfx/new_education.gif" width="16" border="0" align="middle" /></td>        
        <td><nobr>
          <mm:link referid="wizardjsp" referids="_node@origin">
            <mm:param name="wizard">config/pages/pages-origin</mm:param>
            <mm:param name="objectnumber">new</mm:param>           
            <mm:param name="path"><%=session.getAttribute("eduname")%> > <%= session.getAttribute("path") %> <%=lastItem %></mm:param>
            <a href="${_}"
               title="${di:translate(pageContext, 'education.new')} ${dummyname} ${di:translate(pageContext, 'education.aanmaken')}"
               target="text"><di:translate key="education.new" /> ${dummyname} <di:translate key="education.aanmaken" />
            </a>
          </mm:link>
        </nobr></td>
      </tr>
    </table>
    <!-- WTF, yet more tables, and noise -->
    <table border="0" cellpadding="0" cellspacing="0">
      <tr>
        <mm:compare referid="the_last_parent" value="true" inverse="true">
          <td><img src="gfx/tree_spacer.gif" width="32px" height="16px" border="0" align="center" valign="middle"/></td>
          <td><img src="gfx/tree_vertline.gif" border="0" align="center" valign="middle"/></td>
            </mm:compare>
            <mm:compare referid="the_last_parent" value="true">
               <td><img src="gfx/tree_spacer.gif" width="48px" height="16px" border="0" align="center" valign="middle"/></td>
            </mm:compare>


            <%@include file="tree_shift_child.jsp" %>

            <mm:import id="dummyname" jspvar="dummyName" vartype="String" reset="true"><mm:nodeinfo nodetype="tests" type="guitype"/></mm:import>
            <td><img src="gfx/tree_vertline-leaf.gif" border="0" align="center" valign="middle"/></td>
            <td><img src="gfx/new_education.gif" width="16" border="0" align="middle" /></td>
            <td><nobr> 
              <mm:link referid="wizardjsp" referids="_node@origin">
                <mm:param name="wizard">config/tests/tests-origin</mm:param>
                <mm:param name="objectnumber">new</mm:param>           
                <mm:param name="path"><%=session.getAttribute("eduname")%> > <%= session.getAttribute("path") %> <%=lastItem %></mm:param>
                <a href="${_}" 
                   title="${di:translate(pageContext, 'education.new')} ${dummyname}"
                   target="text"><di:translate key="education.new" /> ${dummyname}</a>
              </mm:link>
            </nobr></td>
      </tr>
    </table>
    <table border="0" cellpadding="0" cellspacing="0">
      <tr>
        <mm:compare referid="the_last_parent" value="true" inverse="true">
          <td><img src="gfx/tree_spacer.gif" width="32px" height="16px" border="0" align="center" valign="middle"/></td>
          <td><img src="gfx/tree_vertline.gif" border="0" align="center" valign="middle"/></td>
        </mm:compare>
        <mm:compare referid="the_last_parent" value="true">
          <td><img src="gfx/tree_spacer.gif" width="48px" height="16px" border="0" align="center" valign="middle"/></td>
        </mm:compare>
        
        
        <%@include file="tree_shift_child.jsp" %>
        
        <mm:import id="dummyname" escape="lowercase" reset="true"><mm:nodeinfo nodetype="flashpages" type="guitype"/></mm:import>
        <td><img src="gfx/tree_vertline-leaf.gif" border="0" align="center" valign="middle"/></td>
        <td><img src="gfx/new_education.gif" width="16" border="0" align="middle" /></td>
        <td><nobr>
          <mm:link referid="wizardjsp" referids="_node@origin">
            <mm:param name="wizard">config/flashpages/flashpages-origin</mm:param>
            <mm:param name="objectnumber">new</mm:param>           
            <mm:param name="path"><%=session.getAttribute("eduname")%> > <%= session.getAttribute("path") %> <%=lastItem %></mm:param>
            <a href="${_}"
               title="${di:translate(pageContext, 'education.new')} {dummyname}" 
               target="text"><di:translate key="education.new" /> ${dummyname}</a>
          </mm:link>
        </nobr></td>
      </tr>
    </table>
    <table border="0" cellpadding="0" cellspacing="0">
      <tr>
        <mm:compare referid="the_last_parent" value="true" inverse="true">
          <td><img src="gfx/tree_spacer.gif" width="32px" height="16px" border="0" align="center" valign="middle"/></td>
          <td><img src="gfx/tree_vertline.gif" border="0" align="center" valign="middle"/></td>
        </mm:compare>
            <mm:compare referid="the_last_parent" value="true">
               <td><img src="gfx/tree_spacer.gif" width="48px" height="16px" border="0" align="center" valign="middle"/></td>
            </mm:compare>

            
            <%@include file="tree_shift_child.jsp" %>
            
            <mm:import id="dummyname" escape="lowercase" reset="true"><mm:nodeinfo nodetype="htmlpages" type="guitype"/></mm:import>
            <mm:compare referid="the_last_element" value="true" inverse="true">
               <td><img src="gfx/tree_vertline-leaf.gif" border="0" align="center" valign="middle"/></td>
            </mm:compare>
            <mm:compare referid="the_last_element" value="true">
               <td><img src="gfx/tree_leaflast.gif" border="0" align="center" valign="middle"/></td>
            </mm:compare>
            <td><img src="gfx/new_education.gif" width="16" border="0" align="middle" /></td>
            <td><nobr>
              <mm:link referid="wizardjsp" referids="_node@origin">
                <mm:param name="wizard">config/htmlpages/htmlpages-origin</mm:param>
                <mm:param name="objectnumber">new</mm:param>           
                <mm:param name="path"><%=session.getAttribute("eduname")%> > <%= session.getAttribute("path") %> <%=lastItem %></mm:param>
                <a href="${_}"                   
                   title="${di:translate(pageContext, 'education.new')} ${dummyname}"
                   target="text"><di:translate key="education.new" /> ${dummyname}</a>
              </mm:link>
            </nobr></td>
      </tr>
    </table>
  </mm:compare>
  
</mm:nodeinfo>
