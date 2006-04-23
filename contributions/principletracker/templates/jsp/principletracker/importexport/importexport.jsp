<mm:cloud rank="basic user">
<!-- action check -->
<mm:import externid="action" />
<mm:present referid="action">
 <mm:include page="importexport/actions.jsp" />
</mm:present>
<!-- end action check -->

<table cellpadding="0" cellspacing="0" class="list" style="margin-top : 20px;" width="70%">
<tr>
  <th><a href="<mm:url page="index.jsp" referids="main" />"><img src="images/mmbase-left.gif" align="left" border="0"></a> Import/Export principlesets</th>
</tr>
<tr>
	<td>
	The following commands can be used to export and import sets of principles, Use with care. 
	</td>
</tr>
<table>


<table cellpadding="0" cellspacing="0" class="list" style="margin-top : 40px;" width="60%">
<tr>
  <th align="left">Export a set to disk</th>
</tr>
<tr>
	<form action="<mm:url page="index.jsp" referids="main" />" method="post">
	<input name="action" type="hidden" value="exportprincipleset" />

	<td>
		<br />
		Export set <select name="exportsetid">
			<mm:listnodes type="principlesets">
			<option value="<mm:field name="number" />"><mm:field name="name" />
			</mm:listnodes>
		</select> to <input name="filepath" value="/tmp/principleset.xml" size="20"  /><input type="submit" value="export" />
		<br /><br />
	</td>
	</form>
</tr>
<table>



<table cellpadding="0" cellspacing="0" class="list" style="margin-top : 40px;" width="60%">
<tr>
  <th align="left">Import from disk</th>
</tr>
<tr>
	<form action="<mm:url page="index.jsp" referids="main" />" method="post">
	<input name="action" type="hidden" value="importprincipleset" />
	<td>
		<br />
		New name for set <input name="setname" size="10" /> from <input name="filepath" value="/tmp/principleset.xml" size="20"  /><input type="submit" value="import" />
		<br /><br />
	</td>
	</form>
</tr>
<table>
</mm:cloud>
