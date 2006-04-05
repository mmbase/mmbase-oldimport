<%! public String getTreeSetsOfTreeMap(TreeMap thisTreeMap){
   String s = "";
   TreeMap tmIterator = (TreeMap) thisTreeMap.clone();
   while(tmIterator.size()>0) { 
		String thisKey = (String) tmIterator.firstKey();
		TreeSet thisValue =  (TreeSet) tmIterator.get(thisKey);
	   s += thisKey + " - " + thisValue + "\n";
      tmIterator.remove(thisKey); 
   }
   return s;
} 
public TreeSet unify(TreeSet ts1, TreeSet ts2){
   TreeSet uSet = new TreeSet();
   Iterator ts1Iterator = ts1.iterator();
   while(ts1Iterator.hasNext()) { 
		Object nextObject = ts1Iterator.next();
		if(ts2.contains(nextObject)) {
		   uSet.add(nextObject);
		}
   }
   return uSet;
}
%><%
	String username = request.getParameter("vid");
   if(username==null) { username = ""; }

   String shopEmailAddress = defaultFromAddress;
	String emailSubject = "Interne Webwinkel: ordernr. ";
	
	String noAnswer = "-";
	
	String defaultProductTitle = "Product zonder titel";
	String responseTitle = "IntraShop";
	
	String okTitle = "Bedankt voor uw bestelling!";
	String okMessage = "We zullen u zo spoedig mogelijk op de hoogte brengen van de status van uw bestelling.";
	String okLink = "Naar de homepage";
	
	String warningTitle = "U bent vergeten de volgende velden in te vullen:<br>";
	String warningMessage = "";
	String warningLink = "Terug naar het formulier";

	boolean isValidAnswer = true;
	String clientEmail = "";                // *** see shoppingcartresponse.jsp
	String clientDept  = "";	             // *** see shoppingcartresponse.jsp
	TreeMap responses = new TreeMap();      // *** treemap with response text for every shop item ordered ***
   TreeMap emails = new TreeMap();         // *** treemap with email address for every shop item ordered ***
	TreeMap product_groups = new TreeMap(); // *** treemap with product group for every shop item ordered ***
	// *** example: 
   // shop_items = {140827=2, 140829=2}
   // product_groups = - - [140827, 140829]
   // emails = hangyi@xs4all.nl - [140827, 140829] mmatch@xs4all.nl - [140829]

   shop_itemsIterator = (TreeMap) shop_items.clone();
	while(shop_itemsIterator.size()>0) { 
		String thisShop_item = (String) shop_itemsIterator.firstKey();
		String numberOfItems = (String) shop_itemsIterator.get(thisShop_item);
	   %><%@include file="../includes/shoppingcartresponse.jsp" %><%
		shop_itemsIterator.remove(thisShop_item);
   }

   %>
<!--
products = 
<%= shop_items %>
product groups = 
<%= getTreeSetsOfTreeMap(product_groups) %>
emails = 
<%= getTreeSetsOfTreeMap(emails) %>
-->
   <%
	
   if(isValidAnswer) {
		
		%><%@include file="../includes/shoppingcartsavestats.jsp" 
		
		%><mm:createnode type="email" id="productorder_email"
        ><mm:setfield name="from"><%=  shopEmailAddress  %></mm:setfield
        ><mm:setfield name="replyto"><%= shopEmailAddress %></mm:setfield
      ></mm:createnode><%
      
      cal.setTime(ddd);
	   int week_of_year = cal.get(Calendar.WEEK_OF_YEAR);
	   if(cal.get(Calendar.YEAR)==2005) { week_of_year--; }

	   // *** reset time to beginning of this week ***
	   int minute = cal.get(Calendar.MINUTE);
      int hour_of_day = cal.get(Calendar.HOUR_OF_DAY);
      int day_of_week = cal.get(Calendar.DAY_OF_WEEK);
      cal.add(Calendar.MINUTE,-minute-60*hour_of_day-60*24*day_of_week); 
      long startOfWeek = (cal.getTime().getTime()/1000);
      
      TreeMap pcIterator = (TreeMap) product_groups.clone();
      while(pcIterator.size()>0) { // *** for all product groups in this order ***
		   String thisProductGroup = (String) pcIterator.firstKey();
         String thisProductGroupTitle = "";
         %><mm:node number="<%= thisProductGroup %>"
	            ><mm:field name="titel_fra" jspvar="page_subtitle" vartype="String" write="false"><%
                  thisProductGroupTitle = page_subtitle;
               %></mm:field
         ></mm:node><%
		   TreeSet shopItemsForPC =  (TreeSet) pcIterator.get(thisProductGroup);
         
         String week = "00" + week_of_year;
         String orderId = thisProductGroup + "." + week.substring(week.length()-2) + "." + clientDept.toUpperCase();
	      
	      // *** see if there is an mmevent with this orderId ***
	      String mmeventNumber = "";
	      %><mm:listnodes type="mmevents" constraints="<%= "name LIKE '" + orderId + "'" %>" jspvar="thisEvent"><%
	         mmeventNumber = thisEvent.getStringValue("number");
	      %></mm:listnodes><%
	      if(mmeventNumber.equals("")) { 
	         %><mm:createnode type="mmevents"
	            ><mm:setfield name="name"><%= orderId %></mm:setfield
	            ><mm:setfield name="start"><%= startOfWeek %></mm:setfield
		         ><mm:setfield name="stop">0</mm:setfield
		      ></mm:createnode
		      ><mm:listnodes type="mmevents" constraints="<%= "name LIKE '" + orderId + "'" %>" jspvar="thisEvent"><%
	            mmeventNumber = thisEvent.getStringValue("number");
	         %></mm:listnodes><%
		   }
         
         orderId = thisProductGroupTitle + week.substring(week.length()-2) + "." + clientDept.toUpperCase();
	      
         long lCounter = 1;
	      %><mm:node number="<%= mmeventNumber %>"
	         ><mm:field name="stop" jspvar="stop" vartype="Long" write="false"><%
	            lCounter = stop.longValue()+1;
	         %></mm:field
	         ><mm:setfield name="stop"><%= lCounter %></mm:setfield
	      ></mm:node><%
	      
	      String counter = "000" + lCounter;
	      orderId += "." + counter.substring(counter.length()-3);

         TreeMap emailIterator = (TreeMap) emails.clone();
         while(emailIterator.size()>0) { // *** for all emails in this order ***

		      String thisEmail = (String) emailIterator.firstKey();
		      TreeSet shopItemsForEmail =  (TreeSet) emailIterator.get(thisEmail);
            // *** get all the shopitems in this productgroup for this email address
		      Iterator shopItems = unify(shopItemsForPC,shopItemsForEmail).iterator();

            if(shopItems.hasNext()) { 
   		    
               String responseText = "";
               while(shopItems.hasNext()) {
                  responseText += responses.get(shopItems.next());
               }
               
               %><mm:node referid="productorder_email"
                  ><mm:setfield name="subject"><%=  emailSubject + orderId %></mm:setfield
                  ><mm:setfield name="body">
                     <multipart id="plaintext" type="text/plain" encoding="UTF-8">
                     </multipart>
                     <multipart id="htmltext" alt="plaintext" type="text/html" encoding="UTF-8">
                         <%= "<html>" + responseText  + "</html>" %>
                     </multipart>
                  </mm:setfield
                  ><mm:setfield name="to"><%= thisEmail %></mm:setfield
                ></mm:node
               ><mm:node referid="productorder_email"
                  ><mm:field name="mail(oneshot)" 
               /></mm:node><%
               
               // *** send confirmation email, multiple emails with multiple addresses
               if(!clientEmail.equals("")) {
                  %><mm:node referid="productorder_email"
                  ><mm:setfield name="subject"><%=  emailSubject + orderId + " (bevestiging)" %></mm:setfield
                  ><mm:setfield name="body">
                     <multipart id="plaintext" type="text/plain" encoding="UTF-8">
                     </multipart>
                     <multipart id="htmltext" alt="plaintext" type="text/html" encoding="UTF-8">
                         <%= "<html>Uw onderstaande bestelling heeft als ordernummer " + orderId + " en is verstuurd naar " + thisEmail + "<br><br>"
            		            + responseText  + "</html>" %>
                     </multipart>
                  </mm:setfield>
                  </mm:node><%
      
                  String cEmails = clientEmail + ";";
                  int semicolon = cEmails.indexOf(";"); 
                  while(semicolon>-1) {
                     String emailAddress = cEmails.substring(0,semicolon).trim();
                     %><mm:node referid="productorder_email"
                        ><mm:setfield name="to"><%= emailAddress %></mm:setfield
                     ></mm:node
                     ><mm:node referid="productorder_email"
                        ><mm:field name="mail(oneshot)" 
                     /></mm:node><%
                     cEmails = cEmails.substring(semicolon+1);
                     semicolon = cEmails.indexOf(";");
                  }
               }
            }
            emailIterator.remove(thisEmail);
         }
         pcIterator.remove(thisProductGroup);
      }
		
	   String formTitle =  okTitle;
      String formMessage =  okMessage;
      String formMessageHref = "shoppingcart.jsp";
      String formMessageLinktext = okLink;
      
      %><%@include file="../includes/formmessage.jsp" %><%
      
      shop_items.clear();
		
	} else { 
		String formTitle = warningTitle;
		String formMessage = warningMessage;
		String formMessageHref = "javascript:history.go(-1)";
		String formMessageLinktext = warningLink;
		%><%@include file="../includes/formmessage.jsp" %><% 
	} 

%>
