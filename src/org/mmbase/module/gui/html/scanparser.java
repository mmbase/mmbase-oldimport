/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
/*
$Id: scanparser.java,v 1.41 2001-02-22 16:29:39 install Exp $

$Log: not supported by cvs2svn $
Revision 1.40  2001/02/16 09:22:15  pierre
scanparser : fixed GOTO command

Revision 1.39  2001/02/02 22:37:58  daniel
 changed some debug calls

Revision 1.38  2001/01/04 16:02:35  vpro
Wilbert: Added ( to start and ) to stop skipping arguments for tree and leafpart

Revision 1.37  2000/12/18 14:42:05  pierre
pierre: Fixed bug with GOTO withing PART ort TREEPART

Revision 1.36  2000/12/08 13:34:56  pierre
pierre: fixed use of GOTO in PART and TREEPART. Added optional use of Alias instead of nodenumber to TREEPART (TREEPART ALIAS node+filename)

Revision 1.35  2000/12/06 12:25:36  vpro
Dirk-Jan: added caching of parts after it mystiriously had disapeared

Revision 1.33  2000/11/23 14:50:43  install
Rob changed <transaction> tag in <transactions>

Revision 1.32  2000/11/21 16:40:05  vpro
davzev: Added code to method do_part that forbids parting with filepaths that contain .. parent directory files.

Revision 1.31  2000/11/20 13:37:50  install
Rob changed TRANSCATION tag to lower case

Revision 1.30  2000/11/19 00:17:51  daniel
re fixed the constructor to work with mmdemo

Revision 1.29  2000/11/06 13:32:37  vpro
Rico: Added code from david to support relative parts

Revision 1.28  2000/11/02 11:15:52  install
Changed evaluation sequence, TRANSACTION will be evaluated before GOTO

Revision 1.27  2000/10/15 22:51:46  gerard
gerard: added some checks
submitted by Eduard Witteveen

Revision 1.26  2000/10/13 12:52:39  case
cjr: added length check on taking substring to fix out of bounds error if no
other text follows </LIST> tag.

Revision 1.25  2000/10/13 09:38:11  vpro
Rico: added html-input hooks

Revision 1.24  2000/10/10 12:02:59  vpro
Rico: scanparser added better part support

Revision 1.23  2000/09/14 09:14:52  install
Rob made a change for Gerard ;-)

Revision 1.22  2000/09/12 12:22:45  install
Rob added the connection for the transaction handler <TRANSACTION arg1> arg2 </TRANSACTION>

Revision 1.21  2000/09/08 11:44:19  wwwtech
Rob added message ERROR in SORTPOS (sortpos counts from 0 .. n)

Revision 1.20  2000/07/22 21:12:20  daniel
changes mmbase.mode

Revision 1.19  2000/07/22 15:13:49  daniel
removed some debug

Revision 1.18  2000/07/18 19:00:52  daniel
add support for mmdemo

Revision 1.17  2000/07/15 23:34:28  daniel
trimmed some filename seems to give problems on NT

Revision 1.16  2000/07/12 09:18:48  install
Rob Added active builders support for editors

Revision 1.15  2000/07/12 08:19:41  install
Rob: added getMimeType methods

Revision 1.14  2000/07/03 08:37:16  vpro
Wilbert: Added (dollar)PARAMA to retrieve all params (querystring)

Revision 1.13  2000/06/20 14:23:08  install
Rob: turned debug off

Revision 1.12  2000/05/30 11:35:54  wwwtech
Wilbert: scanparser (still fake to keep it compilable) passes mimetype to newput2

Revision 1.11  2000/05/23 17:54:02  wwwtech
- (marcel) changed entries printUrl(sp) to sp.getUrl() (this prints parameters in url also), debugs faulty html-pages more easily

Revision 1.10  2000/03/30 13:11:28  wwwtech
Rico: added license

Revision 1.9  2000/03/29 10:42:20  wwwtech
Rob: Licenses changed

Revision 1.8  2000/03/27 15:08:33  wwwtech
Rico: removed references to PAGE / AREA

Revision 1.7  2000/03/10 14:15:19  wwwtech
Wilbert: Added  to retrieve number of parameters querystring in method do_param()

Revision 1.6  2000/03/10 12:09:58  wwwtech
Rico: added circular part detection to scanparser, it is also now possilbe to subclass ParseException and throw that in scanparser for those unholdable situations.

Revision 1.5  2000/03/09 16:39:22  wwwtech
Davzev added $MOD $PARAM etc... support to method:do_counter().


*/
package org.mmbase.module.gui.html;

import java.lang.*;
import java.net.*;
import java.util.*;
import java.io.*;
//import javax.servlet.*;
//import javax.servlet.http.*;

import org.mmbase.module.*;
import org.mmbase.servlet.*;
import org.mmbase.util.*;
import org.mmbase.module.core.*;

import org.mmbase.module.CounterInterface;

/**
 * scanpage is a container class it holds all objects needed per scan page
 * it was introduced to make servscan threadsafe but will probably in the future
 * hold all page related info instead of HttpServletR equest and HttpServletResponse
 * because we want extend the model of offline page generation.
 *
 * @author Daniel Ockeloen
 * @$Revision: 1.41 $ $Date: 2001-02-22 16:29:39 $
 */
public class scanparser extends ProcessorModule {

	private	String 	classname 	= getClass().getName();
	private void debug( String msg ) { System.out.println( classname +":"+ msg ); }

	private static HTMLFormGenerator htmlgen=new HTMLFormGenerator();

	public static scancacheInterface scancache=null;
    private static ProcessorModule grab=null;
    private static sessionsInterface sessions=null;
    private static idInterface id=null;
	private static MMBase mmbase=null;
	private static TransactionHandler transactionhandler;
    private static Hashtable processors = new Hashtable();
    private static boolean debug=false;

	private CounterInterface counter = null;

	// needs fix !
    private static String loadmode="no-cache";
    private static String htmlroot;
	private static String documentRoot;
    Hashtable Roots;


	public scanparser() {
		documentRoot=System.getProperty("mmbase.htmlroot");
		if (documentRoot==null) {
			String dtmp=System.getProperty("mmbase.mode");
			if (dtmp!=null && dtmp.equals("demo")) {
				String curdir=System.getProperty("user.dir");
				htmlroot=curdir+"/default-web-app/";
			} else {
				debug("ERROR: could not retrieve document root, use property (-D)mmbase.htmlroot=/my/html/root/dir !");
			}
		} else {
			if (documentRoot.endsWith(File.separator)) {
				documentRoot=documentRoot.substring(0,documentRoot.length()-1);
			}
			htmlroot=documentRoot+File.separatorChar;
			debug("Using documentRoot : "+documentRoot);
		}
	}

    /**
     * Init the servscan, this is needed because it was created using
     * a newInstanceOf().
     *
     * @param int worker id
     */
    public void init() {
        Roots=getRoots();
        // org.mmbase start();
        sessions=(sessionsInterface)getModule("SESSION");
		scancache=(scancacheInterface)getModule("SCANCACHE");
		counter=(CounterInterface)getModule("COUNTER");
		mmbase=(MMBase)getModule("MMBASEROOT");
		transactionhandler=(TransactionHandler)getModule("TRANSACTIONHANDLER");
        // org.mmbase stats=(StatisticsInterface)getModule("STATS");
    }

	public String doPrePart(String template, int last, int rpos, int numitems,int epos) {
		int precmd=0,postcmd=0,prepostcmd=0,index;
		StringBuffer dst=new StringBuffer();
		String cmd="$ITEM";
		int endc='^';
		if (template==null) return("No Template");
		while ((precmd=template.indexOf(cmd,postcmd))!=-1) {
			dst.append(template.substring(postcmd,precmd));
			prepostcmd=precmd+cmd.length();
				postcmd=precmd+6;
				try {
					index=Integer.parseInt(template.substring(prepostcmd,postcmd));
					try {
						index=Integer.parseInt(template.substring(prepostcmd,postcmd+1));
						postcmd++;
					} catch (Exception g) {}	
					index--;
					try {
						dst.append("DUMMY");
					} catch (ArrayIndexOutOfBoundsException e) {}
				} catch (NumberFormatException e) {
					//index=1;
					try {
						if (template.charAt(prepostcmd)=='L') {
							dst.append(""+last);
						} else if (template.charAt(prepostcmd)=='R') {
							dst.append(""+(rpos+1));
						} else if (template.charAt(prepostcmd)=='E') {
							dst.append(""+epos);
						} else if (template.charAt(prepostcmd)=='S') {
							dst.append(""+numitems);
						}
					} catch (ArrayIndexOutOfBoundsException f) {}
				}
		}
		dst.append(template.substring(postcmd));
		return(dst.toString());
	}

	public String doEndPart(String template, int last, int rpos, int numitems,int epos) {
		int precmd=0,postcmd=0,prepostcmd=0,index;
		StringBuffer dst=new StringBuffer();
		String cmd="$ITEM";
		int endc='^';
		if (template==null) return("No Template");
		while ((precmd=template.indexOf(cmd,postcmd))!=-1) {
			dst.append(template.substring(postcmd,precmd));
			prepostcmd=precmd+cmd.length();
				postcmd=precmd+6;
				try {
					index=Integer.parseInt(template.substring(prepostcmd,postcmd));
					try {
						index=Integer.parseInt(template.substring(prepostcmd,postcmd+1));
						postcmd++;
					} catch (Exception g) {}	
					index--;
					try {
						dst.append("DUMMY");
					} catch (ArrayIndexOutOfBoundsException e) {}
				} catch (NumberFormatException e) {
					//index=1;
					try {
						if (template.charAt(prepostcmd)=='L') {
							dst.append(""+last);
						} else if (template.charAt(prepostcmd)=='R') {
							dst.append(""+(rpos+1));
						} else if (template.charAt(prepostcmd)=='E') {
							dst.append(""+epos);
						} else if (template.charAt(prepostcmd)=='S') {
							dst.append(""+numitems);
						}
					} catch (ArrayIndexOutOfBoundsException f) {}
				}
		}
		dst.append(template.substring(postcmd));
		return(dst.toString());
	}


	public Vector reverse(Vector input,int num) {
		Vector results=new Vector();
		for (Enumeration e=input.elements();e.hasMoreElements();) {
			for (int i=0;i<num;i++) {
				String val=(String)e.nextElement();
				results.insertElementAt(val,i);
			}
		}
		return(results);
	}


	boolean do_vals(String cmd,sessionInfo session,scanpage sp) throws ParseException {
		int il=-1;
		int ir=-1;
		int andpos=cmd.indexOf(" AND ");
		int orpos=cmd.indexOf(" OR ");
		if (andpos!=-1 || orpos!=-1) {
			boolean result=false;
			boolean done=false;
			String part;
			do {
				if (andpos!=-1) {
					part=cmd.substring(0,andpos);	
					cmd=cmd.substring(andpos+5);
					result=do_val(part,session,sp);
					done=!result;
				} else if (orpos!=-1) {
					part=cmd.substring(0,orpos);	
					cmd=cmd.substring(orpos+4);
					result=do_val(part,session,sp);
					done=result;
				} else if (andpos<orpos) {
					part=cmd.substring(0,andpos);	
					cmd=cmd.substring(andpos+5);
					result=do_val(part,session,sp);
					done=!result;
				} else {
					part=cmd.substring(0,orpos);	
					cmd=cmd.substring(orpos+4);
					result=do_val(part,session,sp);
					done=result;
				}
				andpos=cmd.indexOf(" AND ");
				orpos=cmd.indexOf(" OR ");
			} while (!done && (andpos!=-1 || orpos!=-1));
			if (done) return(result);
			result=do_val(cmd,session,sp);
			return(result);
		} else {
			return(do_val(cmd,session,sp));
		}
	}


	boolean do_val(String cmd,sessionInfo session,scanpage sp) throws ParseException {
		int il=-1;
		int ir=-1;
		int pos=cmd.indexOf('=');
		if (pos!=-1) {
			String l=cmd.substring(0,cmd.indexOf("="));
			String r=cmd.substring(cmd.indexOf("=")+2);
			char e=cmd.charAt(pos+1);
			l=handle_line(l,session,sp);
			r=handle_line(r,session,sp);
			try {
				il=Integer.parseInt(l);
				ir=Integer.parseInt(r);
			} catch (Exception f){ }
			if (il!=-1) {
				if ((e=='=' || e=='E') && (il==ir)) return(true);
				if (e=='N' && (il!=ir)) return(true);
				if (e=='H' && (il>ir)) return(true);
				if (e=='L' && (il<ir)) return(true);
				if (e=='M' && (il<=ir)) return(true);
				if (e=='A' && (il>=ir)) return(true);
				return(false);
			} else {
				if ((e=='=' || e=='E') && (l.equals(r))) return(true);
				if (e=='N' && (!l.equals(r))) return(true);
				if (e=='C' && (l.equalsIgnoreCase(r))) return(true);
				return(false);
			}
		} else {
			return(false);
		}
	}


	/**
	 * Process the HTML for our own extensions
	 * 
	 */
	public final String handle_line(String body,sessionInfo session,scanpage sp) throws ParseException {

		if (debug) debug("handle_line(): scanparser-> debug 1");
		String part=null;
		int qw_pos,qw_pos2,end_pos,end_pos2;
		int precmd=0,postcmd=-1,prepostcmd=0;
		StringBuffer newbody=new StringBuffer();

		// old pos for 	IFs moved to past LIST
		if (body.indexOf("<IF")!=-1) {
			body=do_conditions(body,session,sp);
		}

		if (debug) debug("handle_line(): scanparser-> debug 2");

		// First find the processor (for the MACRO commands) 
		part=finddocmd(body,"<PROCESSOR ",'>',8,session,sp);
		body=part;


		// Include other HTML
		part=finddocmd(body,"<INCLUDE ",'>',5,session,sp);
		body=part;


		if (debug) debug("handle_line(): scanparser-> debug 3");
		
		// <LIST text1> text2 </LIST>
		// The code below will hand text1 and text2 to the method do_list(text1, text2, session, sp)
		while ((precmd=body.indexOf("<LIST ",postcmd))!=-1) {
			newbody.append(body.substring(postcmd+1,precmd));
			prepostcmd=precmd+6;
			if ((postcmd=body.indexOf('>',precmd))!=-1) {
				end_pos2=body.indexOf("</LIST>",prepostcmd);
				if (end_pos2!=-1) {
					try {
						newbody.append(do_list(body.substring(prepostcmd,postcmd),body.substring(postcmd+1,end_pos2),session,sp));
					} catch(Exception e) {
						String errorMsg = "Error in list: "+e.getMessage()+" in page "+sp.getUrl();
						newbody.append(errorMsg);
						debug("handle_line(): ERROR: do_list(): "+prepostcmd+","+postcmd+","+end_pos2+" in page("+sp.getUrl()+") : "+e);
						e.printStackTrace();
					}
					postcmd=end_pos2+7;
				} 
			} else {
				postcmd=prepostcmd;
			}
		}
		if (postcmd < body.length()) {
			newbody.append(body.substring(postcmd+1));
		}
		body=newbody.toString();


		// detect a <IF> page (daniel)

		// Unmap (special commands)
		part=finddocmd(body,"<UNMAP ",'>',7,session,sp);
		body=part; 

		// Macro's (special commands)
		part=finddocmd(body,"<MACRO ",'>',1,session,sp);
		body=part; 


		// Include other HTML
		part=finddocmd(body,"<INCLUDE ",'>',5,session,sp);
		body=part; 


		// Include other HTML in the LIST command
		part=finddocmd(body,"<LISTINCLUDE ",'>',5,session,sp);
		body=part; 


		// Unmap (special commands)
		part=finddocmd(body,"<UNMAP ",'>',7,session,sp);
		body=part; 


		// Macro's (special commands)
		part=finddocmd(body,"<MACRO ",'>',1,session,sp);
		body=part;
		// Do the dollar commands
		body=dodollar(body,session,sp);


		// TESTING 1 2 3  of the ifs after the include RICO
		if (body.indexOf("<IF")!=-1) {
			body=do_conditions(body,session,sp);
		}	

		// TESTING 1 2 3  of the ifs after the _ONLY_ after the list Daniel
		if (body.indexOf("<LIF")!=-1) {
		//	debug("handle_line(): LIF detected on servscan");
			body=do_conditions_lif(body,session,sp);
		}

		// <NORELOAD>, make it possible to jump pages
		part=finddocmd(body,"<NORELOAD ",'>',12,session,sp);
		body=part; 


		// DO set variables
		part=finddocmd(body,"<DO ",'>',6,session,sp);
		body=part; 


		// <SAVE, make it possible to save  
		part=finddocmd(body,"<SAVE ",'>',18,session,sp);
		body=part; 

		// <TRANSACTION text1> text2 </TRANSACTION>
		// The code below will hand text1 and text2 to the method do_transaction(text1, text2, session, sp)
		newbody=new StringBuffer();
		postcmd=0;

		while ((precmd=body.indexOf("<transactions",postcmd))!=-1) {
			newbody.append(body.substring(postcmd,precmd));
			prepostcmd=precmd+13;
			if ((postcmd=body.indexOf('>',precmd))!=-1) {
				end_pos2=body.indexOf("</transactions>",prepostcmd);
				if (end_pos2!=-1) {
					postcmd=end_pos2+15;
					try {
						newbody.append(do_transaction(body.substring(precmd,postcmd),session,sp));
					} catch(Exception e) {
						debug("handle_line(): ERROR: do_transaction(): "+prepostcmd+","+postcmd+","+end_pos2+" in page("+sp.getUrl()+") : "+e);
						e.printStackTrace();
					}
				} 
			} else {
				postcmd=prepostcmd;
			}
		}

		newbody.append(body.substring(postcmd));
		body=newbody.toString();


		// <GOTO, make it possible to jump pages
		part=finddocmd(body,"<GOTO ",'>',10,session,sp);
		body=part; 


		// <NEWPAGE, make it possible to  'jump' by loading new pages
		part=finddocmd(body,"<NEWPAGE ",'>',11,session,sp);
		body=part; 


		// <GRAB, make it possible to get information of other html-pages 
		part=finddocmd(body,"<GRAB ",'>',13,session,sp);
		body=part; 

		// <PART, make it possible to include other parsed ! and cached pages
		part=finddocmd(body,"<PART ",'>',19,session,sp);
		body=part; 

		// Counter tag
		part=finddocmd(body,"<COUNTER",'>',20,session,sp);
		body=part;

		// <TREEPART, TREEFILE
		part=finddocmd(body,"<TREE",'>',21,session,sp);
		body=part; 

		// <LEAFPART, LEAFFILE
		part=finddocmd(body,"<LEAF",'>',22,session,sp);
		body=part; 

		// Last one always
		part=finddocmd(body,"$LBJ-",'^',4,session,sp);
		body=part;


		return(body);
	}


	/**
	 * Main function, calls the several handlers for the diffenrent TAGS
	 */
	private final String finddocmd(String body,String cmd,int endc,int docmd,sessionInfo session,scanpage sp) throws ParseException {
		return(finddocmd(body,cmd,""+(char)endc,docmd,session,sp));
	}

	/**
	 * Main function, calls the several handlers for the diffenrent TAGS
	 */
	private final String finddocmd(String body,String cmd,String endc,int docmd,sessionInfo session,scanpage sp) throws ParseException {
		int precmd=0,postcmd=0,prepostcmd=0;
		StringBuffer newbody=new StringBuffer();
		int pos,pos2;
		boolean removeToken=false;

	
		while ((precmd=body.indexOf(cmd,postcmd))!=-1) {
			if (removeToken) {
				newbody.append(body.substring(postcmd+1,precmd));
			} else {
				newbody.append(body.substring(postcmd,precmd));
			}

			removeToken=false;
			prepostcmd=precmd+cmd.length();

			//if ((postcmd=body.indexOf(endc,precmd))!=-1) {
			pos=-1;
			for (int i=0;i<endc.length();i++) {
				pos2=body.indexOf(endc.charAt(i),precmd);
				if (pos2!=-1 && (pos==-1 || pos2<pos)) {
					pos=pos2;
					if (i==0) {
						removeToken=true;
					} else {
						removeToken=false;
					}
				}
			}


			if (pos!=-1) {
				postcmd=pos;

				//debug("newpart "+newpart);

				String partbody;
				switch(docmd) {
					case 1: // '<MACRO '
						newbody.append(do_macro(body.substring(prepostcmd,postcmd),session,sp));
						break;
					case 2: // '$SQL-'
						//org.mmbase newbody.append(do_sql(body.substring(prepostcmd,postcmd)));
						break;
					case 3: // '$ID-'
						newbody.append(do_id(body.substring(prepostcmd,postcmd),sp));
						break;
					case 4: // '$OBJ-' , '$VAR-' , '$COLOR-' '$TEXT-' and '$LBJ-'
						newbody.append(body.substring(prepostcmd,postcmd));
						break;
					case 5: // '<INCLUDE '
						newbody.append(do_include(body.substring(prepostcmd,postcmd),session,sp));
						break;
					case 6: // '<DO '
						newbody.append(do_do(body.substring(prepostcmd,postcmd),session,sp));
						break;
					case 7: // '<UNMAP '
						newbody.append(do_unmap(body.substring(prepostcmd,postcmd),session,sp));
						break;
					case 8: // '<PROCESSOR '
						sp.processor=getProcessor(body.substring(prepostcmd,postcmd));
						break;
					//case 9: // '$DOC- '
					//		newbody.append(do_doc(body.substring(prepostcmd,postcmd)));
				//		break;
					case 9: // '$MOD-'
							newbody.append(do_mod(sp,body.substring(prepostcmd,postcmd)));
						break;
					case 10: // '<GOTO'
//							newbody=new StringBuffer();
//							newbody.append(do_goto(sp,body.substring(prepostcmd,postcmd)));
							return do_goto(sp,body.substring(prepostcmd,postcmd));
//						break;
					case 11: // '<NEWPAGE'
							newbody=new StringBuffer();
							newbody.append(do_newpage(sp,body.substring(prepostcmd,postcmd)));
						break;
					case 12: // '<RELOAD'
							// org.mmbase newbody.append(do_reload(body.substring(prepostcmd,postcmd)));
						break;
					case 13: // '<GRAB'
							// org.mmbase newbody.append(do_grab(body.substring(prepostcmd,postcmd)));
						break;
					case 14: // '$PAGE-'
						//	newbody.append(do_page(body.substring(prepostcmd,postcmd)));
						break;
					case 15: // '$AREA-'
						newbody.append(body.substring(prepostcmd,postcmd));
						break;
					case 16: // '$SESSION-'
						newbody.append(do_session(body.substring(prepostcmd,postcmd),session));
						break;
					case 17: // '$LASTLIST'
						// org.mmbase newbody.append(""+lastlistitem);
						break;
					case 18: // '<SAVE'
							newbody.append(do_save(session,body.substring(prepostcmd,postcmd)));
						break;
					case 19: // '<PART '
						partbody=do_part(body.substring(prepostcmd,postcmd),session,sp,0);
						if ((sp.rstatus==1) || (sp.rstatus==2)) {
							return partbody;
						};
						newbody.append(partbody);
						break;
					case 20: // '<COUNTER'
						newbody.append(do_counter(body.substring(prepostcmd,postcmd),session,sp));
						break;
					case 21: // '<TREEPART, TREEFILE'
					case 22: // '<LEAFPART, LEAFFILE'
						partbody=do_smart(body.substring(prepostcmd,postcmd),session,sp, docmd==22);
						if ((sp.rstatus==1) || (sp.rstatus==2)) {
							return partbody;
						};
						newbody.append(partbody);
						break;
					default: 
						debug("Woops broken case in method finddocmd");
						break;
				}
			} else {
				postcmd=prepostcmd;
			}
		}
		if (removeToken) {
			newbody.append(body.substring(postcmd+1));
		} else {
			newbody.append(body.substring(postcmd));
		}
		return(newbody.toString());
	}

	/**
	 * do_counter: This method retrieves the counter value for this page. 
	 * It also checks for any $ attribs used in the COUNTER tag. 
	 *
	 * $param part A string containing the remaining COUNTER part.
	 * $param session The sessionInfo object.
	 * $param sp The current scanpage object.
	 * $return A String containing the counter value.
	 */
	private String do_counter( String part, sessionInfo session, scanpage sp ) throws ParseException
	{
		String result = null;

		// Scan & Parse all $ attributes used in the tag.
		String parsedPart = dodollar(part,session,sp);

		if( debug ) debug("do_counter("+parsedPart+"): inserting tag in page.");
		long time = System.currentTimeMillis();
		result = counter.getTag(parsedPart, session, sp);
		debug("do_counter(): done inserting, took "+ (System.currentTimeMillis() - time ) + " ms.");

		return result;
	}


	private final String do_part(String part2,sessionInfo session,scanpage sp,int markPart) throws ParseException {

		String part="",filename,paramline=null;;
		Vector oldparams=sp.getParamsVector();

		sp.partlevel++;

		part2=dodollar(part2,session,sp);
		// we now may have a a param setup like part.shtml?1212+1212+1212
		// split it so we can load the file and get the params
		int pos=part2.indexOf('?');
		if (pos!=-1) {
			filename=part2.substring(0,pos);
			paramline=part2.substring(pos+1);
			sp.setParamsLine(paramline);
			if (sp.req_line==null) sp.req_line=filename;
		} else {
			filename=part2;
		}

		if (filename.indexOf("..")>=0) {
			sp.setParamsVector(oldparams);
			sp.partlevel--;
			debug("do_part: Usage of '..' in filepath not allowed!");
			return("Usage of '..' in filepath not allowed!");
		}

 		if ((filename.length()>0) && (filename.charAt(0)!='/')) {
 			//davzev trying out part from dir 4okt2000
 			String servletPath = sp.req.getServletPath();
 			//debug("do_part: filename:"+servletPath.substring(0,servletPath.lastIndexOf("/")+1)+filename);
 			filename = servletPath.substring(0,servletPath.lastIndexOf("/")+1)+filename;
 			if (debug) debug("do_part: filename:"+filename);
 		}
 

		// Test if we are going circular
		if (sp.partlevel>8) {
			debug("Warning more then "+sp.partlevel+" nested parts "+sp.req_line);
			if (sp.partlevel>14) throw new CircularParseException("Too many parts, level="+sp.partlevel+" URI "+sp.getUrl());
		}

		// debug("do_part(): filename="+filename);
		// debug("do_part(): paramline="+paramline);
		part=getfile(filename);
		if (part!=null) {
		
				// unlike include we need to map this ourselfs before including it
				// in this page !!
			try {
				String cachedPart = handlePartCache(filename + "?" + paramline, part, session, sp);
				if (cachedPart == null) part = handle_line(part,session,sp); else part = cachedPart;
			} catch (Exception e) {
				String errorMsg = "Error in part "+filename;
				if (paramline!=null) errorMsg += "?" + paramline;
				errorMsg += "\n" + e.getMessage() + "\n Parted by "+sp.getUrl();
				part = errorMsg;
				debug("do_part(): "+errorMsg);
				e.printStackTrace();
			}
	
			if (markPart>0) {
				// Add start and end comments to part
				String marker = "part "+filename;
				if (paramline!=null) marker += "?"+paramline;
				String startComment, endComment;
				if (markPart==1) { startComment = "<!--"; endComment = "-->"; }
				else  { startComment = "/*"; endComment = "*/"; }
				marker += "\n"+endComment;
				part = startComment+"\nStart "+ marker + part + startComment+"\nEnd of " + marker;
			}

			sp.setParamsVector(oldparams);
			sp.partlevel--;
			return(part);
		} else {
			sp.setParamsVector(oldparams);
			sp.partlevel--;
			return("");
		}
	}

	/**
	 * Returns the cached part when the part contains the tag <CACHE HENK> and the page is not exprired and reload is off
	 * else a null wil be returned.
	 */ 
	private String handlePartCache(String filename, String part, sessionInfo session,scanpage sp) throws ParseException
	{		
		if (part == null) return null;
		if (sp.reload) return null;

		/* Test if cache HENK is used in this page or not.
		 */
		int start = part.indexOf("<CACHE HENK");
		int end;
		if (start>=0)
		{	start+=11;
			end = part.indexOf(">", start);
		}
		else
			return null;

		/* Ok it's used. Now look for the specified filename in the cache by (poolname, key, <CACHE HENK expire_time>).
		 */
		// if (debug) debug("handlePartCache(): lookup " + filename);		
		String result = scancache.get("HENK", filename, part.substring(start,end+1));
		if (result != null)
		{	//if (debug) debug("handlePartCache(): got " + filename + "out of cache HENK.");
			return result;
		}

		/* The page couldn't be retrieved out of the cache.
		 * Parse it and put it in the cache.
		 */
		try
		{	result = handle_line(part.substring(end + 1),session,sp);
		}
		catch (Exception e)
		{		String errorMsg = "Error in part "+filename;				
				errorMsg += "\n" + e.getMessage() + "\n Parted by "+sp.getUrl();
				part = errorMsg;
				debug("handlePartCache(): "+errorMsg);
				e.printStackTrace();
		}
		scancache.put("HENK", filename, result);

		return result;
	}

	/**
	 * Support method for do_smart
	 * Add version to path if version defined
	 */
	private static String getVersion(String name, sessionInfo session) {
		String version = session.getValue(name+"version");
		if (version!=null) {
			version=version.trim();
			if (version.equals("")) version = null;
		}
		return version;
	}
	
	/**
	 * Support method for do_smart
	 * Return the path for the file to part
	 */
	private String getSmartFileName( String path, // Path currently investigated
									 String builderPath, // path to add between path and filename for LEAVE version
									 String fileName, // File name of part we are looking for
									 String bestFile, // Last found file which is ok
									 Enumeration nodes, // The passed object nodes
									 sessionInfo session, // The session for version control
									 boolean leaf, // TREE or LEAF version
									 boolean byALias // NAME version
									
									) throws ParseException {
		// Get node from args
		MMObjectNode node = (MMObjectNode)nodes.nextElement();
		String nodeNumber;
		if (byALias) {
		    nodeNumber = ""+mmbase.OAlias.getAlias(node.getIntValue("number"));
		} else {
		    nodeNumber = ""+node.getValue("number");
		}
				
		// Ask the builder of the node to create the path to search for the part
		// If null returned we're done and return bestFile
		path = node.parent.getSmartPath(documentRoot, path, nodeNumber, getVersion(node.getName(), session));
		if (path==null) {
			if (debug) debug("getSmartFile: no dir found for node "+nodeNumber+". Returning "+bestFile);
			return bestFile;
		}

		String newFileName;
		if (leaf) {
			// Remove one builder name from the builder path
			int i = builderPath.indexOf(File.separatorChar);
			if (i<0) newFileName = path+fileName;
			else {
				builderPath = builderPath.substring(i+1);
				newFileName = path + builderPath + File.separator + fileName;
			}
		} else newFileName = path + fileName;
					
		// Check if file present if so, select it as the new bestFile to use
		String fileToCheck = documentRoot+newFileName;
		File f = new File(fileToCheck);
		if (f.exists()) {
			bestFile = newFileName;
			if (debug) debug("Found and selecting " + newFileName + " as new best file");
		} else if (debug) debug(fileToCheck + " not found, continuing search");
		
		// If no more object numbers then return the bestFile so far else continue the travel
		if (!nodes.hasMoreElements())
			return bestFile;
		
		return getSmartFileName( path, builderPath, fileName, bestFile, nodes, session, leaf, byALias);
	}
	
	/**
	 * TREEPART, TREEFILE, LEAFPART or LEAFFILE
	 * @param args action+objectnumbers+filepath
	 * action: PATH or FILE
	 * objectnumbers: + seperated list of objectnumbers, a ( will start skipping args, a ) will stop skipping args
	 * filepath: (optional) file to part
	 * @param leaf false for TREE and true for LEAF version
	 */
	
	private String do_smart(String args, sessionInfo session, scanpage sp, boolean leaf) throws ParseException {
		// Get action: PART or FILE
		String cmdName;
		if (leaf) cmdName = "LEAF"; else cmdName = "TREE";
		int pos = args.indexOf(" ");
		if (pos<0) throw new ParseException("Blank expected after <"+cmdName+"PART or FILE");
		String action = args.substring(0, pos);
		if (!(action.equals("PART") || action.equals("FILE")))
			throw new ParseException("PART or FILE expected after <"+cmdName);
		args = args.substring(pos+1);
		args = dodollar(args, session, sp);
		if (debug) debug(cmdName+action+" "+args);
		
		boolean byALias=false;
		if ((args.length()>=6) && args.substring(0,6).equals("ALIAS ")) {
			byALias=true;
			args = args.substring(6); // Returns empty if length 6, no exception
		}	
		
		int addMarkers = 0;
		if ((args.length()>=6) && args.substring(0,6).equals("DEBUG ")) {
			addMarkers = 1;
			args = args.substring(6); // Returns empty if length 6, no exception
		}	
		if ((args.length()>=12) && args.substring(0,12).equals("DEBUGCSTYLE ")) {
			addMarkers = 2;
			args = args.substring(12); // Returns empty if length 12, no exception
		}	
	
		// Set root path
		String path = File.separator;
				
		// Use the last argument or the builder name of last arg to compose the filename to find
		// Add the buildernames of the passed nodes to builderPath for leafpart and leaffile
		String filename = "";
		String builderPath = "";
		Vector nodes = new Vector();
		String arg = "";
		boolean skip = false;
		StringTokenizer tokens = new StringTokenizer(args, "+");
		while (tokens.hasMoreTokens()) {
			arg = tokens.nextToken().trim();
			if ((arg==null) || arg.equals(""))
				throw new ParseException(cmdName+action+" "+args+": no or empty object number specified");
			if (skip) { // Skip all args until closing ) found
				if (arg.equals(")")) skip = false;
			}
			else if (arg.equals("(")) skip = true;
			else { 
				boolean isNumber = true;
				try { Integer.parseInt(arg); } catch (NumberFormatException n) { isNumber = false; }
				if (isNumber) {
					MMObjectNode node = mmbase.getTypeDef().getNode(arg);
					if (node==null) throw new ParseException(cmdName+action+" node "+arg+" not found");
					nodes.addElement(node);
					if (leaf) builderPath += File.separator + node.getName();
				}
				else {
					// Select the non number as filename to part, first add the remaining tokens
					while (tokens.hasMoreTokens()) arg+="+"+tokens.nextToken();
					filename = arg; // Use it as filename
					args = "";		// Clear args to pass to part and split filename on ? for new args
					pos = filename.indexOf('?');
					if (pos>=0) {
						if (pos<filename.length()-1) args = filename.substring(pos+1);
						filename = filename.substring(0, pos);
					}
					break; // Save one test, we're done
				}//else
			}//else 
		}//while
		
		// If no part name passed as arg, use parts/buildername.shtml?args
		if (filename.equals("")) {
			MMObjectNode node = mmbase.getTypeDef().getNode(arg);
			if (node==null) throw new ParseException(cmdName+action + " node " + arg + " not found");
			filename = "parts"+File.separator+node.getName()+".shtml";
		}
		
		String bestFile;
		if (leaf) {
			// Remove leading slash from builderPath
			if (builderPath.length()>0) builderPath = builderPath.substring(1);
			// If nothing better found part bestFile
			bestFile = path + builderPath + File.separator +filename;
		} else bestFile = path + filename; // If nothing better found part bestFile
		
		// Travel the smart object tree to find an override of the default part
		Enumeration e = nodes.elements();
		if (e.hasMoreElements())
			bestFile = getSmartFileName( path, builderPath, filename, bestFile, e, session, leaf, byALias);
		
		if (!args.equals("")) bestFile += "?"+args;
		if (debug) debug(cmdName+action+" using "+bestFile);

		if (action.equals("FILE"))
			return bestFile;
		
		return do_part(bestFile, session, sp, addMarkers);
	}

	public final String getfile(String where) {
		File scanfile=null;
		int filesize,len=-1;
		cacheline cline=null;
		FileInputStream scan=null;
		Date lastmod;
		String rtn=null;

		String filename=htmlroot.trim()+where.trim();

		filename=filename.replace('/',(System.getProperty("file.separator")).charAt(0));
		filename=filename.replace('\\',(System.getProperty("file.separator")).charAt(0));
		scanfile = new File(filename);
		filesize = (int)scanfile.length();
		lastmod=new Date(scanfile.lastModified());
		cline=new cacheline(filesize);
		cline.lastmod=lastmod;
		try {
			scan = new FileInputStream(scanfile);
			len=scan.read(cline.buffer,0,filesize);
			scan.close();
		} catch(FileNotFoundException e) {
			//give_404_error("getfile");
			//debug("getfile("+where+"): error getfile servscan : "+scanfile.getName());
	 	} catch(IOException e) {}
		if (len!=-1) {
			rtn=new String(cline.buffer,0);
		}
		return(rtn);
	}


	/**
	 * Main function, replaces most of the $ commands
	 */
	private final String dodollar(String newbody,sessionInfo session,scanpage sp) throws ParseException {

		if ( newbody.indexOf('$') == -1) {
			return newbody;
		}

		long oldtime = System.currentTimeMillis();
 
		String part,part2;
		int qw_pos,qw_pos2,end_pos,end_pos2;

		// Parameter fill in 
		while (newbody.indexOf("$PARAM")!=-1) {
			qw_pos=newbody.indexOf("$PARAM");	
			qw_pos2=qw_pos+7;	
			part=newbody.substring(0,qw_pos);
			part+=do_param(sp,newbody.substring(qw_pos+6,qw_pos2));
			part+=newbody.substring(qw_pos2);
			newbody=part;
		}


		// locals fill in 
		while (newbody.indexOf("$LOCAL")!=-1) {
			qw_pos=newbody.indexOf("$LOCAL");	
			qw_pos2=qw_pos+7;	
			part=newbody.substring(0,qw_pos);
			part+=do_local(newbody.substring(qw_pos+6,qw_pos2));
			part+=newbody.substring(qw_pos2);
			newbody=part;
		}

		//debug("dodollar(): 2 " + (System.currentTimeMillis() - oldtime) + " ms." + cookie);

		// Personal obj's
		part=finddocmd(newbody,"$ID-","^\n\r\"=<> ,",3,session,sp);
		newbody=part; 

		part=finddocmd(newbody,"$LASTLIST","^\n\r\"=<> ,",17,session,sp);
		newbody=part;
		
		// OBJects new VERSION
		part=finddocmd(newbody,"$AREA-","^\n\r\"=<> ,",15,session,sp);
		newbody=part; 

		// find pages
		part=finddocmd(newbody,"$PAGE-","^\n\r\"=<> ,",14,session,sp);
		newbody=part; 

		// find sessions
		part=finddocmd(newbody,"$SESSION-","^\n\r\"=<> ,",16,session,sp);
		newbody=part; 

		// OBJects
		part=finddocmd(newbody,"$OBJ-",'^',4,session,sp);
		newbody=part; 
		// clone of OBJ
		part=finddocmd(newbody,"$VAR-",'^',4,session,sp);
		newbody=part; 
		// clone of OBJ
		part=finddocmd(newbody,"$COLOR-",'^',4,session,sp);
		newbody=part; 
		// clone of OBJ
		part=finddocmd(newbody,"$TEXT-",'^',4,session,sp); 
		newbody=part; 
		// SQL queries
		part=finddocmd(newbody,"$SQL-",'^',2,session,sp); 
		newbody=part;
		// CONST , not recognized in finddocmd , so must be build and added there
		// part=finddocmd(newbody,"$CONST-",'^',7,session,sp);
		// newbody=part; 
		// Cineserv
		// part=finddocmd(newbody,"$DOC-",'^',9,); 
		// newbody=part;
		
		// Modules
		part=finddocmd(newbody,"$MOD-","^\n\r\"=< ,",9,session,sp);
		newbody=part; 


		//debug("dodollar(): 3 " + (System.currentTimeMillis() - oldtime) + " ms." + cookie);

		return(newbody);
	}


	// local fill in routine
	//private final String do_local(HttpPost poster,String part2) {
	private final String do_local(String part2) {
		// daniel
		String rtn=null;

		//rtn=poster.getPostParameter("LOCAL"+part2);
		rtn=null;
		if (rtn==null) {
			rtn="";
		}
		return(rtn);
	}	


	// Parameter fill in routine
	private final String do_param(scanpage sp,String part2) {
		// rico
		int i;
		String rtn=null;
		
		if (part2!=null) {
			if (part2.equals("L")) {
				// Eval $PARAML: the number of params
				if (sp.params==null) {
					sp.getParam(0); // Force build of params
					if (sp.params==null) // No params
						return "0";
				}
				return ""+sp.params.size();
			}
			if (part2.equals("A")) {
				// Eval $PARAMA
				if (sp.querystring == null) 
					return "";
				return sp.querystring;
			}
			if (part2.equals("T")) {
				// Eval $PARAMT: Returns value of the tail parameter.
				if (sp.params==null) {
					sp.getParam(0); // Force build of params
					if (sp.params==null) // No params
						return "";
				}
				return sp.getParam(sp.params.size()-1);
			}
		}
		
		// Handle $PARAMn
		i=Integer.parseInt(part2);
		rtn=sp.getParam(i-1);
		if (rtn==null) rtn="";
		return(rtn);
	}	

    /**
     * Special commands, this will grow
     */
    private final String do_save(sessionInfo session,String part) {
        StringTokenizer tok= new StringTokenizer(part," -",true);
        if (tok.hasMoreTokens()) {
            String type=tok.nextToken();
            if (type.equals("SESSION")) {
                sessions.saveValue(session,part.substring(8));
            }
        }
        return("");
    }


    private final String do_session(String part2,sessionInfo session) {
        if (sessions!=null) {
            String value=sessions.getValue(session,part2);
            if (value!=null) {
                return(value);
            } else {
                return("");
            }
        } else {
            return("");
        }
    }

    /**
    */
    private final String do_newpage(scanpage sp,String part)
    {
		//debug( "do_newpage("+sp.getUrl()+")");
        sp.rstatus=2;
        return(part);
    }

   /**
    */
    private final String do_goto(scanpage sp,String part)
    {
        sp.rstatus=1;
        return(part);
    }

    /**
    *  try to acces a module (Must be a ProcessorInterface)
    *  and replace "part" by whatever the processor returns.
    */
    private final String do_mod(scanpage sp,String part) {
        int index = part.indexOf('-');
        if (index == -1) {
            debug("do_mod(): ERROR: part (no '-'): '" + part+"' ("+sp.getUrl()+")");
            return "";
        } else {
            String moduleName = part.substring(0,index);
            String moduleCommand = part.substring(index+1,part.length());

            ProcessorInterface proc = getProcessor(moduleName);
            if (proc == null) {
                debug("do_mod(): ERROR: no Processor(" + moduleName +") found for page("+sp.getUrl()+")");
                return "";
            } else {
                return proc.replace(sp, moduleCommand);
            }
        }
    }


	/**
	* give a name an you get the  processor (Interface), if procName does not exists then null is returned.
	*/	
	private final ProcessorInterface getProcessor(String procName)
	{
			if (processors.containsKey(procName))
			{
				return (ProcessorInterface) processors.get(procName);
			}
			else
			{
				Object obj = getModule (procName);
				if (obj == null)
				{
					debug("getProcessor(): Not authorized or not a valid class name: " + procName);
					return null; 
				} else {
					// debug(obj);
				}
					
				if (obj instanceof ProcessorInterface)
				{
					//debug("servscan.getProcessor: we have a new Processor("+procName+")");
					ProcessorInterface pr = (ProcessorInterface) obj;		
					processors.put(procName,pr);
					return pr;
				}
				else
				{	
					debug("getProcessor(): ERROR: not a valid Processor("+ procName+")");
					return null;	
				}
			}
	}

     private final String do_unmap(String part,sessionInfo session,scanpage sp) throws ParseException {
        String part1,part2;
        int pnt;

        part=dodollar(part,session,sp);
        return("");
    }


	private final String do_do(String part,sessionInfo session,scanpage sp) throws ParseException {
		String part1,part2;
		int pnt;
		part=dodollar(part,session,sp);

		// Chop
		pnt=part.indexOf('=');
		part2=part.substring(pnt+1);
		part1=part.substring(0,pnt);
		if (part1.indexOf("SESSION-")==0) {
			if (sessions!=null) sessions.setValue(session,part1.substring(8),part2);
		} else if (part1.indexOf("ID-")==0) {
			String name=HttpAuth.getRemoteUser(sp);
			if (name!=null && name.length()>1) {
				//setUserServletProperty(part1.substring(3),part2,0);
				id.setValue(name,part1.substring(3),part2);
			}
		}
		return("");
	}

    private final String do_include(String part2,sessionInfo session,scanpage sp) throws ParseException {
        String part="";
        part2=dodollar(part2,session,sp);
        //time(" - cookie = " + cookie + ", doinclude - dodollar", false);
        part=getfile(part2);
        //time(" - cookie = " + cookie + ", doinclude - doinclude", false);
        if (part!=null) {
            return(part);
        } else {
            return("");
        }
    }

    private final String do_id(String part2,scanpage sp) {
        String name=HttpAuth.getRemoteUser(sp);
        String part="";
        if (name!=null && name.length()>1) {
            //part=getUserServletProperty(part2,0);
            part=id.getValue(name,part2);
            if (part==null) return("");
        } else {
            part="Unknown";
        }
        return(part);
    }


	/**
	 * This is the generic macro processor.
	 * It processes the tokens after the <MACRO tag and gives them in a
	 * Vector to the processor specified in the page
	 * Tokens supported of the form :
	 * tokje=blah 
	 * tokje="blah"
	 * tokje
	 * "tokje"
	 * All seperated by spaces
	 * To hook into to this create a Processor (@see vpro.james.coreserver.Module
	 * and @see vpro.james.modules.Processor)
	 * load it in the server, give servscan permission and put the tag with
	 * the module name in the html page (<PROCESSOR YourProcessor>, and
	 * for forms <INPUT TYPE="HIDDEN" NAME="PRC-VAR-PROCESSOR" VALUE="YourProcessor">)
	 */
	private final String do_macro(String part,sessionInfo session,scanpage sp) throws ParseException {
		String tokje;

		Vector cmds;
		ProcessorInterface tmpprocessor=null;

		part=dodollar(part,session,sp);

		int pos=part.indexOf("PROCESSOR=");
		if (pos!=-1) {
			String str=part.substring(pos+10);
			if (str.indexOf(' ')!=-1) str=str.substring(0,str.indexOf(' '));
			if (str!=null) {
				 if (str.charAt(0)=='"') str=str.substring(1,str.length()-1);
				tmpprocessor=getProcessor(str);

				if( tmpprocessor==null )
				{
					if (sp.processor!=null)
						debug("do_macro(): WARNING: No processor("+str+") found for page("+sp.getUrl()+"), but scanpage has one.");
					else
						debug("do_macro(): ERROR: No processor("+str+") found for page("+sp.getUrl()+")");
				}
			}
		}

		cmds=tokenizestring(part);

		if (tmpprocessor!=null) {
			tokje=htmlgen.getHTMLElement(sp, tmpprocessor,cmds);
		} else if (sp.processor!=null) {
			tokje=htmlgen.getHTMLElement(sp, sp.processor,cmds);
		} else {
			debug("do_macro(): ERROR: No processor() specified in page("+sp.getUrl()+")");
			tokje="<B> No Processor specified in page </B><BR>";
		}
		return(tokje);
	}


	/**
	 * Method that tokenizes a string into pieces
	 * Tokens supported of the form :
	 * tokje=blah 
	 * tokje="blah"
	 * tokje
	 * "tokje"
	 * All seperated by spaces
	 */
	private Vector tokenizestring(String part) {
		String current="",tokje;
		boolean inDQuote=false;
		int pos;
		Vector cmds=new Vector();
		StringTokenizer tok;

		// OK lets chop the parts 
		tok= new StringTokenizer(part," \"=",true);
		while(tok.hasMoreTokens()) {
			tokje=tok.nextToken();
			if (inDQuote) {
				if (tokje.equals("\"")) {
					inDQuote=false;
					current+=tokje;
				} else {
					current+=tokje;
				}
			} else {
				if (tokje.equals("\"")) {
					inDQuote=true;
					current+=tokje;
				} else if (tokje.equals(" ")) {
					cmds.addElement(current);
					current="";
				} else if (tokje.equals("=")) {
					current+=tokje;
				} else {
					current+=tokje;
				}
			}
		}
		if (!current.equals("")) {
			cmds.addElement(current);
		}
		return(cmds);
	}


	/** 
	* handle if/then/elseif/\/if
	*/
	String do_conditions_lif(String body,sessionInfo session,scanpage sp) throws ParseException {
		StringBuffer buffer = new StringBuffer();

		int depth=0;
		int ifpos=0;
		int newifpos=0;
		int elsepos=0;
		int elseifpos=0;
		int endifpos=0;

		// counters for occurrence
		int ifcount=0;
		int elsecount=0;
		int elseifcount=0;
		int endifcount=0;

		// boolean if a token was found
		boolean found=true;

		buffer = new StringBuffer();
		ifpos=body.indexOf("<LIF",ifpos);
		while (ifpos!=-1) {
			// append the part before the <IF
			buffer.append(body.substring(0,ifpos));
			
			// unmap this one IF, result will be the correct part+rest
			buffer.append(do_if(body.substring(ifpos+4),session,sp));
			
			// convert buffer back to body and clear buffer
			body=buffer.toString();
			buffer=new StringBuffer();

			// try to get a new </IF pos
			ifpos=body.indexOf("<LIF");
		}
		return(body);	
	}


	String do_if(String body,sessionInfo session,scanpage sp) throws ParseException {
		int endpos=-1;
		// first hunt down the command
		int pos = body.indexOf('>');
		if (pos!=-1) {
			String cmd=body.substring(1,pos);
			boolean state=do_vals(cmd,session,sp);
			body=body.substring(pos+1);	
			
			boolean found=false;
			int depth=0;
			pos=0;
			int beg=0;
			int end=0;
			int els=-1;
			while (!found) {
				beg=body.indexOf("<IF",pos);
				end=body.indexOf("</IF>",pos);
				if (beg==-1 || beg>end) {
					if (depth==0) {
						found=true;	
						if (els==-1) {
							els=body.indexOf("<ELSE>",pos);
							if (els>end) els=-1;
						}
					} else {
						depth--;
						pos=end+2;
					}
				} else {
					if (depth==0 && els==-1) {
						els=body.indexOf("<ELSE>",pos);
						if (els>end && els<beg) els=-1;
					}
					pos=beg+2;
					depth++;
				}
			}
			if (els==-1) {
				if (state) {
					body=body.substring(0,end)+body.substring(end+5);
				} else {
						body=body.substring(end+5);
				}
			} else {
				if (state) {
					body=body.substring(0,els)+body.substring(end+5);
				} else {
					body=body.substring(els+6,end)+body.substring(end+5);
				}
			}
		} else {
			debug("do_if(): ERROR: no end on if command");
		}
		return(body);
	}


	/** 
	* handle if/then/elseif/\/if
	*/
	String do_conditions(String body,sessionInfo session,scanpage sp) throws ParseException {
		StringBuffer buffer = new StringBuffer();

		int depth=0;
		int ifpos=0;
		int newifpos=0;
		int elsepos=0;
		int elseifpos=0;
		int endifpos=0;

		// counters for occurrence
		int ifcount=0;
		int elsecount=0;
		int elseifcount=0;
		int endifcount=0;

		// boolean if a tken was found
		boolean found=true;

		buffer = new StringBuffer();
		ifpos=body.indexOf("<IF",ifpos);
		while (ifpos!=-1) {
			// append the part before the <IF
			buffer.append(body.substring(0,ifpos));
			
			// unmap this one IF, result will be the correct part+rest
			buffer.append(do_if(body.substring(ifpos+3),session,sp));
			
			// convert buffer back to body and clear buffer
			body=buffer.toString();
			buffer=new StringBuffer();

			// try to get a new </IF pos
			ifpos=body.indexOf("<IF");
		}
		return(body);	
	}


	private String do_list(String cmd,String template, sessionInfo session,scanpage sp) throws ParseException {
		long ll1,ll2;
		StringBuffer rtn=new StringBuffer();
		ProcessorInterface tmpprocessor=null;
		String command=null;
		String sorted=null;
		String sortedpos=null;
		String str,key=null;
		StringTagger tagger=null;
		Object obj;
		Vector t,cmds,result;
		int numitems=1,maxitems=-1,curitem=0,offset=0;
		int maxtotal=-1;


		ll1=System.currentTimeMillis();
		cmd=dodollar(cmd,session,sp);
		cmd=Strip.Whitespace(cmd,Strip.BOTH);
		String oldcmd=cmd;

		String  wantCache="HENK";
		if (sp.reload) {
			if (cmd.indexOf(" CACHE=")!=-1) {
				String rst=scancache.get("HENK","/LISTS/"+cmd+template);
				if (rst!=null) {
					return(rst);
				}
			}
		}

		cmds=tokenizestring(cmd);

		t=new Vector();
		int pos=cmd.indexOf('"');
		if (pos==0) {
			pos=cmd.indexOf('"',pos+1);
			if (pos!=-1) {
				command=cmd.substring(0,pos+1);
				if ((pos+1)!=cmd.length()) {
					cmd=cmd.substring(pos+2);
				} else {
					cmd="";
				}
			}
		} else { 
			pos=cmd.indexOf(' ');
			if (pos!=-1) {
				command=cmd.substring(0,pos);
				cmd=cmd.substring(pos+1);
			}
		}

		
		// Process commands to list if any parse them but they can be overiden
		// by the processor
		if (cmds.size()>1) {
			tagger=new StringTagger(cmd,' ','=',',','"');	

			/*
			str=tagger.Value("ITEMS");
			try {
				numitems=Integer.parseInt(str);
			} catch (NumberFormatException x) {
				numitems=1;
			}
			*/

			str=tagger.Value("MAX");
			try {
				maxitems=Integer.parseInt(str);
			} catch (NumberFormatException x) {
				maxitems=-1;
			}
			maxtotal=maxitems;

			str=tagger.Value("OFFSET");
			try {
				offset=Integer.parseInt(str);
			} catch (NumberFormatException x) {
				offset=0;
			}


			str=tagger.Value("AUTORANGE");
			if (str!=null && maxitems!=-1) {
				int u=str.indexOf('/');
				if (u!=-1) {
					key=str.substring(0,u);
					String acmd=str.substring(u+1);
					String value=sessions.getValue(session,key);
					try {
						offset=Integer.parseInt(value);
					} catch (NumberFormatException x) {
						offset=0;
					}
					if (acmd.equals("NEXT")) {
						//sessions.setValue(session,key,""+(offset+maxitems));
						offset=offset+maxitems;
					} else 
					if (acmd.equals("PREV")) {
						//sessions.setValue(session,key,""+(offset-maxitems));
						offset=offset-maxitems;
					} else {
						offset=0;
						sessions.setValue(session,key,"0");
					}
				}
			}

			sorted=tagger.Value("SORTED");
			sortedpos=tagger.Value("SORTPOS");
			
			str=tagger.Value("PROCESSOR");
			if (str!=null) {
				 if (str.charAt(0)=='"') str=str.substring(1,str.length()-1);
				tmpprocessor=getProcessor(str);
			}

		} else {
			tagger=new StringTagger("",' ','=',',','"');	
		}

		if (sessions!=null && session!=null && key!=null) {
			sessions.setValue(session,key,""+offset);
		}
	
		String version=tagger.Value("VERSION");
		if (version==null || version.equals("1.0")) {
		// is there a proccesor defined ?
		if (sp.processor!=null || tmpprocessor!=null) {
			// Call the processor to get the real info this might override
			// some of the layout calls like numitems and sort etc etc
			if (tmpprocessor==null) {
				result=sp.processor.getList(sp,tagger,command);
			} else {
				result=tmpprocessor.getList(sp,tagger,command);
			}
			// do we have a result ifso do the layout stuff
			if (result!=null) {
				// added reverse, should be changed soon
				if (sorted!=null && sorted.equals("ALPHA")) result=SortedVector.SortVector(result);	

				// get the number of items set by the user or by the processor
				str=tagger.Value("ITEMS");
				if (str!=null) {
					try {
						numitems=Integer.parseInt(str);
					} catch (NumberFormatException x) {
						numitems=1;
					}
				} else {
					numitems=1;
				}

				if (sorted!=null && sorted.equals("MALPHA")) {
					if (sortedpos!=null) {
						result=doMAlphaSort(result,sortedpos,numitems);	
					} else {
						result=doMAlphaSort(result,"1",numitems);	
					}
				}

				if (sorted!=null && sorted.equals("REVERSE")) result=reverse(result,numitems);	

				curitem=1;
				int jsize=result.size();
				if (maxtotal==-1 || maxtotal>(jsize/numitems)) maxtotal=jsize/numitems;
				if (maxitems==-1) {
					maxitems=jsize;
				} else {
					maxitems=(maxitems*numitems)+(offset*numitems);
					if (maxitems>jsize) {
						 maxitems=jsize;
						if (key!=null) {
							//sessions.setValue(session,key,"-1");
						}
					}
				}
				offset*=numitems;
				for (int j=offset;j<maxitems;j+=numitems) {
					t.removeAllElements();
					for (int i=0;i<numitems;i++) {
						try {
 							obj=result.elementAt(j+i);
							if (obj==null) obj="NULL";
						} catch (ArrayIndexOutOfBoundsException r) {
							obj="No Such Element";
						}
						t.addElement(obj);
					}
					rtn.append(processtemplate(t,template,curitem+(offset/numitems),jsize/numitems,curitem,numitems));
					//rtn.append(processtemplate(t,template,curitem+(offset/numitems),maxtotal,curitem));
					curitem++;
 				}
				// org.mmbase lastlistitem=curitem;
			} else {
				rtn.append(" Processor failed to process command <br>");
				debug("do_list(): Processor failed to process command : "+cmd+" ("+sp.processor+") ("+tmpprocessor+")");
			}
		} else {
			rtn.append(" No Processor specified in page <br>");
		}
		} else if (version.equals("2.0")) {
		// is there a proccesor defined ?
		//debug("do_list(): LIST 2.0 -> Start");
		long ltime1=System.currentTimeMillis();
		if (sp.processor!=null || tmpprocessor!=null) {
			// Call the processor to get the real info this might override
			// some of the layout calls like numitems and sort etc etc
			if (tmpprocessor==null) {
				result=sp.processor.getList(sp,tagger,command);
			} else {
				result=tmpprocessor.getList(sp,tagger,command);
			}
			long ltime2=System.currentTimeMillis();
			//debug("do_list(): LIST 2.0 -> GETLIST="+(ltime2-ltime1));
			ltime1=ltime2;

			// do we have a result ifso do the layout stuff
			if (result!=null) {
				// added reverse, should be changed soon
				if (sorted!=null && sorted.equals("ALPHA")) result=SortedVector.SortVector(result);	

				// get the number of items set by the user or by the processor
				str=tagger.Value("ITEMS");
				if (str!=null) {
					try {
						numitems=Integer.parseInt(str);
					} catch (NumberFormatException x) {
						numitems=1;
					}
				} else {
					numitems=1;
				}

				if (sorted!=null && sorted.equals("REVERSE")) result=reverse(result,numitems);	

				//debug("PART="+template);
					
				String listprepart=null;
				String listendpart=null;
				// if there is a LISTLOOP split it in 3 parts
				int listprepos=template.indexOf("<LISTLOOP>");
				int listendpos=template.indexOf("</LISTLOOP>");
				if (listprepos!=-1) {
					// oke found a listloop
					listprepart=template.substring(0,listprepos);	
					listendpart=template.substring(listendpos+11);	
					template=template.substring(listprepos+10,listendpos);
				}

				//debug("do_list(): PRE="+listprepart+"*");
				//debug("do_list(): PART="+template+"*");
				//debug("do_list(): END="+listendpart+"*");

				// new part handles the 'prepart' of LISTLOOP
				// ends new part handles the 'prepart' of LISTLOOP

				curitem=1;
				int jsize=result.size();
				if (maxtotal==-1 || maxtotal>(jsize/numitems)) maxtotal=jsize/numitems;
				if (maxitems==-1) {
					maxitems=jsize;
				} else {
					maxitems=(maxitems*numitems)+(offset*numitems);
					if (maxitems>jsize) {
						 maxitems=jsize;
						if (key!=null) {
							//sessions.setValue(session,key,"-1");
						}
					}
				}
				offset*=numitems;
				
				// added for prelist/endlist
				if (listprepart!=null && !listprepart.equals("")) {
					rtn.append(doPrePart(listprepart,jsize/numitems,offset/numitems,maxitems/numitems,numitems));
				}

				for (int j=offset;j<maxitems;j+=numitems) {
					t.removeAllElements();
					for (int i=0;i<numitems;i++) {
						try {
 							obj=result.elementAt(j+i);
							if (obj==null) obj="NULL";
						} catch (ArrayIndexOutOfBoundsException r) {
							obj="No Such Element";
						}
						t.addElement(obj);
					}
					rtn.append(processtemplate(t,template,curitem+(offset/numitems),jsize/numitems,curitem,numitems));
					//rtn.append(processtemplate(t,template,curitem+(offset/numitems),maxtotal,curitem));
					curitem++;
 				}
				// org.mmbase lastlistitem=curitem;
	
				// added for prelist/endlist
				if (listendpart!=null && !listendpart.equals("")) {
					rtn.append(doEndPart(listendpart,jsize/numitems,offset/numitems,maxitems/numitems,numitems));
				}

				ltime2=System.currentTimeMillis();
				//debug("do_list(): LIST 2.0 -> PROCESSTEMPLATE="+(ltime2-ltime1));
				ltime1=ltime2;
			} else {
				rtn.append(" Processor failed to process command <br>");
				debug("do_list(): ERROR: Processor failed to process command : "+cmd+" ("+sp.processor+") ("+tmpprocessor+")");
			}
		} else {
			rtn.append(" No Processor specified in page <br>");
		}
		//debug("do_list(): LIST 2.0 -> End");
		}

		// -----------------------------------

		if (cmd.indexOf(" CACHE=")!=-1) {
			scancache.put("HENK","/LISTS/"+oldcmd+template,rtn.toString());
		}
		ll2=System.currentTimeMillis();
		if (debug && (ll2-ll1)>300) {
			debug("do_list(): time("+(ll2-ll1)+" ms)");
		}
		return(rtn.toString());
	}


	private String processtemplate(Vector v,String template, int pos, int last, int rpos, int numitems) {
		int precmd=0,postcmd=0,prepostcmd=0,index;
		StringBuffer dst=new StringBuffer();
		String cmd="$ITEM";
		int endc='^';
		if (template==null) return("No Template");
		if (v==null) return("Vector is null");
		while ((precmd=template.indexOf(cmd,postcmd))!=-1) {
			dst.append(template.substring(postcmd,precmd));
			prepostcmd=precmd+cmd.length();
				postcmd=precmd+6;
				try {
					index=Integer.parseInt(template.substring(prepostcmd,postcmd));
					try {
						index=Integer.parseInt(template.substring(prepostcmd,postcmd+1));
						postcmd++;
					} catch (Exception g) {}	
					index--;
					try {
						dst.append((v.elementAt(index)).toString());
					} catch (ArrayIndexOutOfBoundsException e) {}
				} catch (NumberFormatException e) {
					//index=1;
					try {
						if (template.charAt(prepostcmd)=='P') {
							dst.append(""+pos);
						} else if (template.charAt(prepostcmd)=='L') {
							dst.append(""+last);
						} else if (template.charAt(prepostcmd)=='R') {
							dst.append(""+rpos);
						} else if (template.charAt(prepostcmd)=='S') {
							dst.append(""+numitems);
						} else if (template.charAt(prepostcmd)=='F') {
							if (pos==last) {
								dst.append("YES");
							} else {
								dst.append("NO");
							}
						}
					} catch (ArrayIndexOutOfBoundsException f) {}
				}
		}
		dst.append(template.substring(postcmd));
		return(dst.toString());
	}

    // how do we handle multihosts in servlet API now ?
    Hashtable getRoots() {
        /*
        int pos;
        String tmp,tmp2;
        Hashtable result=new Hashtable();
        result.put("www",DocumentRoot);

        for (Enumeration e=getInitParameters().keys();e.hasMoreElements();) {
            tmp=(String)e.nextElement();
            tmp2=(String)getInitParameter(tmp);
            pos=tmp.indexOf("Root.");
            if (pos==0) {
                result.put(tmp.substring(5),tmp2);
            }
        }
        */
        Hashtable result=new Hashtable();
        return(result);
    }


	/**
	 * This does the handling of the Form inputs.
	 * It calls the processor to handle the PRC-CMD's it finds after
	 * it has done the PRC-VAR's fill in.
	 */
	public final void do_proc_input(String rq_line,HttpPost poster,Hashtable proc_var,Hashtable proc_cmd,scanpage sp) {
		String part,part2,sqlstatement;
		Object obj;
		int qw_pos,qw_pos2;
		Vector	results;

		// First find the processor of this page
		if ((part=(String)proc_var.get("PROCESSOR"))!=null) {
			//processor=(Processor)getModule(part);
			sp.processor=getProcessor(part);
			if (sp.processor!=null) {
				sp.processor.process(sp,proc_cmd,proc_var);
			} else {
				debug("do_proc_input(): ERROR: Processor("+part+") is not loaded in server for page("+sp.getUrl()+")");
			}
		} else {
			debug("do_proc_input(): No Processor specified : "+rq_line);
			debug("do_proc_input(): proc_var="+proc_var);
			debug("do_proc_input(): proc_cmd="+proc_cmd);
			return;
		}
		return;	
	}

	/** 
	* maintainance call, will be called by the admin to perform managment
	* tasks. This can be used instead of its own thread.
	*/
	public void maintainance() {
	}

	public synchronized String calcPage(String part2,scanpage sp,int cachetype) {

		if (debug) debug("calcPage("+part2+","+sp.getUrl()+","+cachetype+")");

		try {
			String filename,paramline=null;
	
			// we now may have a a param setup like part.shtml?1212+1212+1212
			// split it so we can load the file and get the params
			int pos=part2.indexOf('?');
			if (pos!=-1) {
				filename=part2.substring(0,pos);
				paramline=part2.substring(pos+1);
				//((worker)req).setParamLine(paramline);
				sp.setParamsLine(paramline);
				if (sp.req_line==null) sp.req_line=filename;
				if (debug) debug("calcPage(): setting paramline="+paramline);
			} else {
				filename=part2;
			}
			if ((sp.mimetype==null) || sp.mimetype.equals("")) {
				sp.mimetype=getMimeTypeFile(filename);
			}
			
			if (debug) {	
				debug("calcPage(): filename="+filename);
				debug("calcPage(): paramline="+paramline);
				debug("calcPage(): mimetype="+sp.mimetype);
			}
			sp.body=getfile(filename);
	
			if (sp.body!=null) {
	
				String wantCache=null;
	
				if (sp.body.indexOf("<CACHE PAGE")!=-1) {
					wantCache="PAGE";
				}
				sp.wantCache=wantCache;
				// end cache
				// unlike include we need to map this ourselfs before including it
				// in this page !!
				//part=handle_line(part,req);
				sp.body=handle_line(sp.body,null,sp);
				if (wantCache!=null) {
					scancache.newput2(wantCache,part2,sp.body,cachetype, sp.mimetype);
				}
		
				return(sp.body);
			} else {
				return("");
			}
		} catch (Exception r) {
			debug("calcPage("+part2+","+sp.getUrl()+","+cachetype+"): ERROR: "+r);
			r.printStackTrace();
			return("");
		}
	}

	/**
	* davzev changed method syntax from int sortonnumber to String sortonnumbers.
	* Sort the lines in the vector, to do this we need to first group
	* them and then sort them by item number defined
	* @param input - the result Vector unsorted.
	* @param sortonnumbers - a String with colpositions eg. "6:4" denoting the 
	* order and colpositions for the sort.
	* @param numberofitems - integer with the amount of items.
	* @return a Vector with sorted items.
	*/
	//Vector doMAlphaSort(Vector input,int sortonnumber,int numberofitems) {
	Vector doMAlphaSort(Vector input,String sortonnumbers,int numberofitems) {

		//SortedVector output = new SortedVector(new RowVectorCompare(sortonnumber));
		if (debug) debug("doMAlphaSort: Sorting using MultiColCompare("+sortonnumbers+ ")");
		SortedVector output = new SortedVector(new MultiColCompare(sortonnumbers));
		// first create vectors with numberofitems per vector
		Enumeration einput=input.elements();
		while (einput.hasMoreElements()) {
			Vector row=new Vector();	
			for (int i=0;i<numberofitems;i++) {
				row.addElement((String)einput.nextElement());	
			}
			output.addSorted(row);	
		}
		Vector result=new Vector();
		Enumeration eoutput=output.elements();
		while (eoutput.hasMoreElements()) {
			Vector row=(Vector)eoutput.nextElement();
			Enumeration erow=row.elements();
			while (erow.hasMoreElements()) {
				result.addElement(erow.nextElement());	
			}
		}
		return(result);
	}

	private String printURI(scanpage sp) {
		String rtn="";
		if (sp!=null) {
			if (sp.req_line!=null) {
				rtn=sp.req_line;
			} else {
				rtn="req_line==NULL";
			}
		} else {
			rtn="scanpage==NULL";
		}
		return(rtn);
	}

	private String do_transaction(String template, sessionInfo session,scanpage sp) throws ParseException {
		transactionhandler.handleTransaction(template,session,sp);
		return "";	
	}
}
