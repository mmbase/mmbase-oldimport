<mm:import externid="name" />
<table cellpadding="0" cellspacing="0" style="margin-top : 10px;" width="95%">
<tr>
		<td colspan="8" align="left">
		<mm:write referid="main">
		  <mm:compare value="projects">	
			<mm:compare referid="sub" value="project">
		 	<a href="index.jsp?main=<mm:write referid="main" />"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a> <mm:write referid="name" />
			</mm:compare>
			<mm:compare referid="sub" value="projectpackage">
		 	<a href="<mm:url page="index.jsp" referids="main,name"><mm:param name="sub" value="project" /></mm:url>"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a> 
			 <mm:write referid="name" /> / <mm:import externid="package" id="p" /><mm:write referid="p" />
			</mm:compare>
			<mm:compare referid="sub" value="projectbundle">
		 	<a href="<mm:url page="index.jsp" referids="main,name"><mm:param name="sub" value="project" /></mm:url>"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a> 
			 <mm:write referid="name" /> / <mm:import externid="bundle" id="p" /><mm:write referid="p" />
			</mm:compare>
			<mm:compare referid="sub" value="none" inverse="false">
		  	Projects defined	
			</mm:compare>
		  </mm:compare>
		  <mm:compare value="packages">	
			<mm:compare referid="sub" value="none" inverse="true">
		 	<a href="index.jsp?main=<mm:write referid="main" />"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a>
			</mm:compare>
			<mm:compare referid="sub" value="none" inverse="false">
		  	Available Packages to this MMBase
			</mm:compare>
		  </mm:compare>

		  <mm:compare value="providers">	
			<mm:compare referid="sub" value="provider">
		 	<a href="index.jsp?main=<mm:write referid="main" />"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a> Im done
			</mm:compare>
			<mm:compare referid="sub" value="changesettings">
		 	<a href="index.jsp?main=<mm:write referid="main" />&sub=provider&id=<mm:write referid="id" />"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a> Im done
			</mm:compare>
			<mm:compare referid="sub" value="addprovider">
		 	<a href="index.jsp?main=<mm:write referid="main" />&sub=none"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a> Im done
			</mm:compare>
			<mm:compare referid="sub" value="none" inverse="false">
		   	Local and Remote Providers
			</mm:compare>
		  </mm:compare>


		
		  <mm:compare value="sharing">	
			<mm:compare referid="sub" value="share">
		 	<a href="index.jsp?main=<mm:write referid="main" />"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a>
			</mm:compare>
			<mm:compare referid="sub" value="changeusers">
		 	<a href="index.jsp?main=<mm:write referid="main" />&sub=share&id=<mm:write referid="id" />"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a> Im done
			</mm:compare>
			<mm:compare referid="sub" value="changeuser">
		 	<a href="index.jsp?main=<mm:write referid="main" />&sub=allusers"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a> Im done
			</mm:compare>
			<mm:compare referid="sub" value="addshare">
		 	<a href="index.jsp?main=<mm:write referid="main" />&sub=none"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a> Im done
			</mm:compare>
			<mm:compare referid="sub" value="allusers">
		 	<a href="index.jsp?main=<mm:write referid="main" />&sub=share&id=<mm:write referid="id" />"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a> Im done
			</mm:compare>
			<mm:compare referid="sub" value="allgroups">
		 	<a href="index.jsp?main=<mm:write referid="main" />&sub=share&id=<mm:write referid="id" />"><img src="<mm:write referid="image_arrowleft" />" border="0"/></a> Im done
			</mm:compare>
			<mm:compare referid="sub" value="none" inverse="false">
		  	Created Shares we provide for others
			</mm:compare>
		  </mm:compare>



		</mm:write>
		</B></FONT>
		</TD>
</tr>
</table>
