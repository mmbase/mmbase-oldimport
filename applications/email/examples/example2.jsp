<%@taglib uri="http://www.mmbase.org/mmbase-taglib-1.0" prefix="mm" %>
<html>
<body>
<mm:cloud name="mmbase" method="http" rank="administrator" jspvar="cloud">


		<!--  create the email node -->
                <mm:createnode id="mail1" type="email">
                        <mm:setfield name="from">daniel@mmbase.org</mm:setfield>
                        <mm:setfield name="to">daniel@mmbase.org</mm:setfield>
                        <mm:setfield name="subject">my first mmbase mail !!</mm:setfield>
                        <mm:setfield name="body">Just testing email</mm:setfield>
                </mm:createnode>


                <!-- mail the email node -->
                <mm:node referid="mail1">
                     <mm:functioncontainer>
                       <mm:param name="type" value="oneshot" />
                       <mm:function name="mail" />
                     </mm:functioncontainer>
                 </mm:node>
 
                 <!--check if mmbase could mail the message -->
                 <mm:node referid="mail1">
                    <mm:field name="mailstatus">
                      <mm:compare value="1">
                         Mail was delivered at <mm:field name="mailedtime"><mm:time format=":LONG.LONG" /></mm:field>
                      </mm:compare>
                      <mm:compare value="2">
                         Mail failed
                      </mm:compare>
                    </mm:field>
                 </mm:node>
	
</mm:cloud>
</body>
</html>
