/*

VPRO (C)

This source file is part of mmbase and is (c) by VPRO until it is being
placed under opensource. This is a private copy ONLY to be used by the
MMBase partners.

*/
package org.mmbase.servlet;
 
// import the needed packages
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.mmbase.util.*;
import org.mmbase.module.builders.*;
import org.mmbase.module.core.*;

/**
 * remoteXML servlet has one job and one job only receive remoteXML files
 * for the mmbase system, check them and give them to mmbase so it can
 * be seen as a change on a object. It uses a POST command to get the
 * XML and will respond with a 200 OK if the xml was understood by the
 * mmbase system.
 */
public class remoteXML extends JamesServlet {

	MMBase mmbase;
	
	public void init() {
		mmbase=(MMBase)getModule("MMBASEROOT");
		//System.out.println("MMBASE="+mmbase);
	}

	/**
 	* service call will be called by the server when a request is done
	* by a user.
 	*/
	public synchronized void service(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException
	{	

		// POST
		if (req.getMethod().equals("POST")) {
			handlePost(req,res);
		} else if (req.getMethod().equals("GET")) {
			handleGet(req,res);
		}
	}

	private void handlePost(HttpServletRequest req,HttpServletResponse res) {
		try {
		HttpPost poster=new HttpPost(req);

		String xml=poster.getPostParameter("xmlnode");
		//System.out.println("WOW GOT="+xml);
		commitXML(xml,req);
		} catch(Exception e) {
			System.out.println("POST failed from remoteXML");
		}
	}

	private void handleGet(HttpServletRequest req,HttpServletResponse res) {
		String body="";
		String buildername=getParam(req,0);
		String nodename=getParam(req,1);
		MMObjectBuilder bul=mmbase.getMMObject(buildername);
		String number=bul.getNumberFromName(nodename);
		//System.out.println("WWWWWWWWWWWWOOOO="+number);
		if (number==null) {
			ServiceBuilder sbul=(ServiceBuilder)bul;	
			MMServers server=(MMServers)mmbase.getMMObject("mmservers");
			String snumber=server.getNumberFromName("mmbaseorgrunner");
			try {
				sbul.addService(nodename,"org.test",server.getNode(snumber));
			} catch(Exception e) {
				e.printStackTrace();
			}
			number=bul.getNumberFromName(number);
		}
		MMObjectNode node=bul.getNode(number);
		if (node!=null) {
			body=node.toXML();	
		}
		try {
			// Open	a output stream so you can write to the client
			PrintStream out = new PrintStream(res.getOutputStream());
			// Set the content type of this request
			res.setContentType("text/plain");
			res.setContentLength(body.length());
			out.print(body);
			out.flush();
			out.close();
		} catch(Exception e) {
			System.out.println("GET failed from remoteXML");
		}
	}
	


	public boolean commitXML(String xml,HttpServletRequest req) {
		Hashtable values=getXMLValues(xml);
		String remhost=req.getRemoteAddr();
		String givenhost=(String)values.get("host");

		// hack for braindead psion jdk
		if (givenhost!=null && givenhost.indexOf("http://localhost")!=-1) {
			System.out.println("HOST REPLACE=http://"+remhost+":8080");
			values.put("host","http://"+remhost+":8080");	
		}

			

		String buildername=(String)values.get("buildername");
		values.remove("buildername");
		MMObjectBuilder bul=mmbase.getMMObject(buildername);
		if (bul!=null) {
			String number=(String)values.get("number");

			// convert name to number if needed
			try {
				int i=Integer.parseInt(number);
			} catch(Exception e) {
				// wow lets convert this sucker
				number=bul.getNumberFromName(number);
			}
			if (number==null) {
				ServiceBuilder sbul=(ServiceBuilder)bul;	
				MMServers server=(MMServers)mmbase.getMMObject("mmservers");
				String snumber=server.getNumberFromName("stationrunner");
				try {
					sbul.addService((String)values.get("name"),"cdplayerDummy",server.getNode(snumber));
				} catch(Exception e) {
					e.printStackTrace();
				}
				number=bul.getNumberFromName(number);
			}
			MMObjectNode node=bul.getNode(number);
			if (node!=null) {
				 mergeXMLNode(node,values);
				node.commit();
			} else {
				System.out.println("remoteXML-> can't get node "+number);
			}
		} 
		return(true);
	}

	private void mergeXMLNode(MMObjectNode node,Hashtable values) {


		Enumeration t=values.keys();
		while (t.hasMoreElements()) {
			String key=(String)t.nextElement();
			String value=(String)values.get(key);

			// set the node
			String dbtype=node.getDBType(key);
			if (!key.equals("number") 
				&& !key.equals("otype") 
				&& !key.equals("buildername") 
				&& !key.equals("owner")) {
					node.setValue( key, dbtype, value );
			}
		}
	}

 
	public Hashtable getXMLValues(String body) {
		Hashtable values=new Hashtable();
		StringTokenizer tok = new StringTokenizer(body,"\n\r");
		String xmlline=tok.nextToken();
		String docline=tok.nextToken();
		
	
		String builderline=tok.nextToken();
		values.put("buildername",builderline.substring(1,builderline.length()-1));
		String endtoken="</"+builderline.substring(1);
		

		String nodedata=body.substring(body.indexOf(builderline)+builderline.length());
		nodedata=nodedata.substring(0,nodedata.indexOf(endtoken));

		int bpos=nodedata.indexOf("<");
		while (bpos!=-1) {
			String key=nodedata.substring(bpos+1);
			key=key.substring(0,key.indexOf(">"));
			String begintoken="<"+key+">";
			endtoken="</"+key+">";
			
			String value=nodedata.substring(nodedata.indexOf(begintoken)+begintoken.length());
			value=value.substring(0,value.indexOf(endtoken));

			values.put(key,value);

			nodedata=nodedata.substring(nodedata.indexOf(endtoken)+endtoken.length());
			bpos=nodedata.indexOf("<");
		}
		return(values);
	}
}
