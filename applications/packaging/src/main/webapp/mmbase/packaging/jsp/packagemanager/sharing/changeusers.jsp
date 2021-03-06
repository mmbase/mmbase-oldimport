<mm:import externid="version">best</mm:import>
<mm:import externid="provider">ignore</mm:import>

<!-- action check -->
<mm:import externid="action" />
<mm:present referid="action">
 <mm:include page="sharing/actions.jsp" />
</mm:present>
<!-- end action check -->
<table cellpadding="0" cellspacing="0" class="list" style="margin-top : 10px;" width="45%">
<tr>
	<th colspan="2">
	Add user to :
		  <mm:nodefunction set="mmpm" name="getShareInfo" referids="id,version">
			'<mm:field name="name" />'
		  </mm:nodefunction>
	</th>
</tr>
<form action="<mm:url page="index.jsp" referids="main,sub,id,version" />" method="post">
		  <tr>
		  <td width="100">
			<select name="newuser">
				  <mm:nodelistfunction set="mmpm" name="getShareUsersReverse" referids="id,version">
					<option><mm:field name="name" />
		  		  </mm:nodelistfunction>
			</select>
			<input type="hidden" name="action" value="adduser">
		  </td>
		  <td width="100">
			<input type="submit" value="add">
		  </td>
		  </tr>
</form>
</td>
</tr>
</table>

<table cellpadding="0" cellspacing="0" class="list" style="margin-top : 10px;" width="45%">
<TR>
	<th colspan="2">
	Remove user from :
		  <mm:nodefunction set="mmpm" name="getShareInfo" referids="id,version">
			'<mm:field name="name" />'
		  </mm:nodefunction>
	</th>
</tr>
<form action="<mm:url page="index.jsp" referids="main,sub,id,version" />" method="post">
		  <tr>
		  <td width="100">
			<select name="olduser">
				  <mm:nodelistfunction set="mmpm" name="getShareUsers" referids="id,version">
					<option><mm:field name="name" />
		  		  </mm:nodelistfunction>
			</select>
			<input type="hidden" name="action" value="removeuser">
		  </td>
		  <td width="100">
			<input type="submit" value="remove">
		  </td>
		  </tr>
</form>
</table>

