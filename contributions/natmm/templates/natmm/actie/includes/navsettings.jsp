<mm:import externid="object_type" jspvar="objecttype">artikel</mm:import>
<mm:import externid="object_title" jspvar="objecttitle">titel</mm:import>
<mm:import externid="object_intro" jspvar="objectintro">intro</mm:import>
<mm:import externid="object_date" jspvar="objectdate">begindatum</mm:import>
<mm:import externid="extra_constraint" jspvar="extra_constraint"></mm:import>
<mm:import externid="show_links" jspvar="show_links">true</mm:import>
<%
int TITLE = 1;
int DATE = 2;
int QUOTE = 3;

int menuType = TITLE;
int objectPerPage = 10;

String objectOrderby = "contentrel.pos";
String objectConstraint = "";
String objectDirections = "UP";
%>
<mm:relatednodes type="menutemplate" path="related,menutemplate">
   <mm:field name="url" jspvar="url" vartype="String" write="false">
      <% 
      if(url.indexOf("quote")>-1) { 
         Calendar c = Calendar.getInstance();
         c.setTime(now);
         c.set(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DATE),0,0);
         menuType = QUOTE;
         objectPerPage = 5;
         objectOrderby = objecttype + "." + objectdate;
         objectConstraint = objectOrderby + " < '" + c.getTime().getTime()/1000 + "'"; // the begin of today, so start with yesterday
         objectDirections = "DOWN";
      } 
      if(url.indexOf("date")>-1) {
         menuType = DATE;
         objectPerPage = 7;
         objectOrderby = objecttype + "." + objectdate;
         objectConstraint = objectOrderby + " < '" + nowSec + "'"; 
         objectDirections = "DOWN";
      }
      %>
   </mm:field>
</mm:relatednodes>
<%
if(extra_constraint!=null && !extra_constraint.equals("")) {
   objectConstraint += " AND " + extra_constraint;
}
%>