<%@ taglib uri="http://www.mmbase.org/mmbase-taglib-1.0" prefix="mm" %>
<mm:cloud name="mmbase">

<html>

<head>
  <title>Taglib examples</title>
</head>

<body>

<%@ include file="menu.jsp"%>

<h1>Home</h1>

<p>
The pages found here are made to give some taglib examples. Things are kept
simple so that the page sources are as clear as possible.
</p>

<p>
This page should work after building Tomcat or Orion from the build file. The
other example pages use an additional taglib from the Jakarta project. Please
download and install the request taglib from
<a href="http://jakarta.apache.org/taglibs/doc/request-doc/intro.html">the jakarta website</a>.
</p>

<p>
Here's a simple list of jumpers to see if the MMBase taglib is working:
</p>

<mm:listnodes type="jumpers" fields="name,url">
 <mm:first><ul></mm:first>
 <li>
   <mm:field name="name"/> redirects to <mm:field name="url"/>
   <mm:first>(first)</mm:first>
   <mm:last>(last)</mm:last>
   <mm:odd>(odd)</mm:odd>
   <mm:even>(even)</mm:even>
   <mm:changed>(changed)</mm:changed>
 </il>
 <mm:last></ul></mm:last>
</mm:listnodes>

</body>

</html>

</mm:cloud>
