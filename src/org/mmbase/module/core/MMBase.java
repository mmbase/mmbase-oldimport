/*

VPRO (C)

This source file is part of mmbase and is (c) by VPRO until it is being
placed under opensource. This is a private copy ONLY to be used by the
MMBase partners.

*/
package org.mmbase.module.core;

import java.lang.*;
import java.net.*;
import java.util.*;
import java.util.Date;
import java.io.*;
import java.sql.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.mmbase.util.*;
import org.mmbase.module.*;
import org.mmbase.module.builders.*;
import org.mmbase.module.corebuilders.*;
import org.mmbase.module.database.*;
import org.mmbase.module.database.support.*;


/**
 * The module which provides access to the MMbase defined
 * by the provided name/setup it holds the overal object
 * 'world' madeup of builders, objects and relations and
 * all the needed tools to use them.
 *
 * @author Daniel Ockeloen
 */
public class MMBase extends ProcessorModule  {

	private String	_classname = getClass().getName();
	private boolean debug=false;
	private void 	debug( String msg ) { System.out.println( _classname +":"+ msg ); }

	private SendMailInterface sendmail;
	public MMBaseMultiCast mmc;
	int delay;
	boolean	nodecachesdone=false;
	MMBaseProbe probe;

	JDBCInterface jdbc;
	public String baseName="def1";

	public TypeDef TypeDef;
	public RelDef RelDef;
	public OAlias OAlias;
	public FieldDef FieldDef;
	public InsRel InsRel;
	public TypeRel TypeRel;

	MultiRelations MultiRelations;
	private String dtdbase="http://www.mmbase.org";
	MMJdbc2NodeInterface database;
	//String databasename;

	public Hashtable mmobjs=new Hashtable();
	String machineName="unknown";
	String host="unknown";
	String authtype="none";
	public static String multicasthost=null;
	public static int multicastport=-1;
	private String language="us";



	public void init() {
		// is there a basename defined in MMBASE.properties ?
		String tmp=getInitParameter("BASENAME");
		if (tmp!=null) {
			// yes then replace the default name (def1)
			baseName=tmp;
		} else {
			debug("init(): No name defined for mmbase using default (def1)");
		}

		tmp=getInitParameter("AUTHTYPE");
		if (tmp!=null && !tmp.equals("")) {
			authtype=tmp;
		}


		tmp=getInitParameter("LANGUAGE");
		if (tmp!=null && !tmp.equals("")) {
			language=tmp;
		}

		tmp=getInitParameter("AUTH401URL");
		if (tmp!=null && !tmp.equals("")) {
			HttpAuth.setLocalCheckUrl(tmp);
		}
		tmp=getInitParameter("DTDBASE");
		if (tmp!=null && !tmp.equals("")) {
			dtdbase=tmp;
		}

		tmp=getInitParameter("HOST");
		if (tmp!=null && !tmp.equals("")) {
			host=tmp;
		}


		tmp=getInitParameter("MULTICASTPORT");
		if (tmp!=null && !tmp.equals("")) {
			try {
				multicastport=Integer.parseInt(tmp);
			} catch(Exception e) {}
		}

		tmp=getInitParameter("MULTICASTHOST");
		if (tmp!=null && !tmp.equals("")) {
			multicasthost=tmp;
		}

		/*
		tmp=getInitParameter("DATABASE");
		if (tmp!=null && !tmp.equals("")) {
			databasename=tmp;
		} else {
			debug("init(): ERROR: No database defined !");
		}
		*/

    		sendmail=(SendMailInterface)getModule("sendmail");
		machineName=getInitParameter("MACHINENAME");

		jdbc=(JDBCInterface)getModule("JDBC");

		mmc=new MMBaseMultiCast(this);

		if (!checkMMBase()) {
			// there is no base defined yet, create the core objects
			createMMBase();
		}

		// start the core objects
		TypeDef=new TypeDef(this); // switch arround with FielDef
		FieldDef=new FieldDef(this);

		RelDef=new RelDef(this);	
		InsRel=new InsRel(this);	
		TypeRel=new TypeRel(this);	
		OAlias=new OAlias(this);	

		// new loader system for now only checks the defines
		String builderfile=getInitParameter("BUILDERFILE");
		if (builderfile==null || builderfile.equals("")) {
			builderfile=MMBaseContext.getConfigPath()+("/defines/");
			initBuilders(builderfile);
		} else {
			initBuilders(builderfile);
		}

		Enumeration t = mmobjs.elements(); 
		while (t.hasMoreElements()) {
			MMObjectBuilder fbul=(MMObjectBuilder)t.nextElement();
			fbul.initFields(false);
		}

		if (debug) debug("Objects started");


		MultiRelations = new MultiRelations(this);

		// moved up ! mmc=new MMBaseMultiCast(this);

		// weird place needs to rethink (daniel).
		Vwms bul=(Vwms)getMMObject("vwms");
		if (bul!=null) {
			bul.startVwms();
		}
		Vwmtasks bul2=(Vwmtasks)getMMObject("vwmtasks");
		if (bul2!=null) {
			bul2.start();
		}

	}

	public void onload() {
	}

	public void unload() {
	}

	public void shutdown() {
	}


	/**
	 * INFO, a support module for servscan. provides const and
	 * general info like dates, times, browser info etc etc
	 */
	public MMBase() {
		if (debug) debug("MMBase constructed");
	}

	/**
	* is this mmbase as defined in baseName created ?
	*/
	boolean checkMMBase() {
		try {
			MultiConnection con=getConnection();
			Statement stmt=con.createStatement();
			ResultSet rs=stmt.executeQuery("select count(*) from "+baseName+"_object");
			stmt.close();
			con.close();	
			return(true);	
		} catch (SQLException e) {
			//e.printStackTrace();
			return(false);	
		}
	}

	/**
	* create a new mmbase under the name defined in baseName
	*/
	boolean createMMBase() {
		debug("MMBASE-> creating new multimedia base : "+baseName);
		Vector v;
		database=getDatabase();
		database.create("object");
		return(true);
	}


	public MMObjectBuilder getMMObject(String name) {
		Object o=mmobjs.get(name);
  		return((MMObjectBuilder)o);
	}

	public InsRel getInsRel() {
		return(InsRel);
	}

	public RelDef getRelDef() {
		return(RelDef);
	}

	public TypeDef getTypeDef() {
		return(TypeDef);
	}

	public TypeRel getTypeRel() {
		return(TypeRel);
	}

	public FieldDef getFieldDef() {
		return(FieldDef);
	}

	public MultiConnection getConnection() {
		try {
			//MultiConnection con=jdbc.getConnection(jdbc.makeUrl(databasename));
			MultiConnection con=jdbc.getConnection(jdbc.makeUrl());
			return(con);
		} catch (SQLException e) {
			e.printStackTrace();
			return(null);
		}
	}

	public String getBaseName() {
		return(baseName);
	}

	public void doProbeRun() {
		DayMarkers bul=(DayMarkers)getMMObject("daymarks");
		if (bul!=null) {
			bul.createMarker();
		} else {
			debug("doProbeRun(): ERROR: Can't access builder : daymarks");
		}
	}

	public void maintainance() {
		if (probe==null) probe=new MMBaseProbe(this);

		//LStreams.checkBroadcastState(LStreams.getNode(573949));
		if (2==1 && mmc!=null) {
			// debug for in/out multicast check
			if( debug) debug("maintenance(): in="+mmc.incount+" out="+mmc.outcount+" spawn="+mmc.spawncount);
		}
	}


	public Hashtable getSearchHash(Vector se,String mapper) {
		Hashtable results=new Hashtable();
		Enumeration t = se.elements(); 
		MMObjectNode node;
		while (t.hasMoreElements()) {
			node=(MMObjectNode)t.nextElement();
			results.put(new Integer(node.getIntValue(mapper)),node);
		}
		return(results);
	}

	public String getWhereList(Vector se,String mapper) {
		if (se==null) return null;
		StringBuffer inlist = new StringBuffer();
		inlist.append(" (");
		Enumeration t = se.elements(); 
		MMObjectNode node;
		while (t.hasMoreElements()) {
			node=(MMObjectNode)t.nextElement();
			inlist.append(node.getIntValue(mapper) + ",");
		}
		if (inlist.length() >= 1 ) inlist.setLength(inlist.length()-1);
		inlist.append( ") ");
		return (inlist.toString());
	}


	private String getFile(String file) {
        File scanfile;
        int filesize,len=0;
        byte[] buffer;
        FileInputStream scan;
        Date lastmod;
        String rtn=null;

		//debug("MMBase -> Opening file:"+file); 
        scanfile = new File(file);
        filesize = (int)scanfile.length();
        lastmod=new Date(scanfile.lastModified());
        buffer=new byte[filesize];
        try {
            scan = new FileInputStream(scanfile);
            len=scan.read(buffer,0,filesize);
            scan.close();
        } catch(FileNotFoundException e) {
			// oops we have a problem
        } catch(IOException e) {}
        if (len!=-1) {
            rtn=new String(buffer,0);
        }
        return(rtn);
	}


	private byte[] getFileBytes(String file) {
        File scanfile;
        int filesize,len=0;
        byte[] buffer;
        FileInputStream scan;
        Date lastmod;
        String rtn=null;

        scanfile = new File(file);
        filesize = (int)scanfile.length();
        lastmod=new Date(scanfile.lastModified());
        buffer=new byte[filesize];
        try {
            scan = new FileInputStream(scanfile);
            len=scan.read(buffer,0,filesize);
            scan.close();
        } catch(FileNotFoundException e) {
			// oops we have a problem
        } catch(IOException e) {}
        if (len!=-1) {
        	return(buffer);
        }
        return(null);
	}

	public SendMailInterface getSendMail() {
		return(sendmail);
	}

	public String getMachineName() {
		return(machineName);
	}

	public String getHost() {
		return(host);
	}

	public boolean addRemoteObserver(String type,MMBaseObserver obs) {
		MMObjectBuilder bul=getMMObject(type);
		if (bul!=null) {
			return(bul.addRemoteObserver(obs));
		} else {
			debug("addRemoteObserver(): ERROR: Can't find builder : "+type);
			return(false);
		}
	}


	public boolean addLocalObserver(String type,MMBaseObserver obs) {
		MMObjectBuilder bul=getMMObject(type);
		if (bul!=null) {
			return(bul.addLocalObserver(obs));
		} else {
			debug("addLocalObserver(): ERROR: Can't find builder : "+type);
			return(false);
		}
	}

	public String doGetAgeMarker(StringTokenizer tok) {
		if (tok.hasMoreTokens()) {
			String age=tok.nextToken();
			try {
				int agenr=Integer.parseInt(age);
				int agecount=((DayMarkers)getMMObject("daymarks")).getDayCountAge(agenr);
				return(""+agecount);
			} catch (Exception e) {
				debug("MMBASE -> Not a valid AGE");
				return("No valid age given");
			}
		} else {
			return("No age given");
		}
	}

	public synchronized int getDBKey() {
		return(database.getDBKey());
	}


	private Vector removeFunctions(Vector fields) {
		Vector results=new Vector();
		Enumeration f=fields.elements();
		for (;f.hasMoreElements();) {
			// hack hack this is way silly Strip needs to be fixed
			String fieldname=Strip.DoubleQuote((String)f.nextElement(),Strip.BOTH);
			int pos1=fieldname.indexOf('(');
			if (pos1!=-1) {
				int pos2=fieldname.indexOf(')');
				results.addElement(fieldname.substring(pos1+1,pos2));
			} else {
				results.addElement(fieldname);
			}
		}
		return(results);
	}
	
	public Object getBaseModule(String name) {
		return(getModule(name));
	}

	public void stop()
	{

	}

	boolean initBuilders(String path) {
		String filestring=getFile(path+"objects.def");
		if (filestring!=null) {
			StringTokenizer tok = new StringTokenizer(filestring,"\n\r");
			while (tok.hasMoreTokens()) {
				String cmdline=tok.nextToken();	
				int pos=cmdline.indexOf("=");
				if (pos!=-1) {
					String cmdtype=cmdline.substring(0,pos);
					String cmdrest=cmdline.substring(pos+1);
					//if( debug ) debug("initBuilders(): cmd="+cmdtype+" line="+cmdrest);
					if (cmdtype.equals("object")) initBuilder(cmdrest,path);
				} else {
					debug("initBuilders(): ERROR: no command defines in line : '"+cmdline+"'");
				}
			}
		} else {
			debug("initBuilders(): ERROR: no objects.def file defined");
		}
		return(true);
	}

	boolean initBuilder(String builder,String path) {
		//if( debug ) debug("MMBase -> "+builder);
		String classname=null;
		MMObjectBuilder bul=null;
		String objectname=null;
		String description=null;
		String dutchsname=null;
		String searchage=null;
		Vector fields=new Vector();

		String filestring=getFile(path+builder+".def");
		if (filestring!=null) {
			//if( debug ) debug(filestring);
			StringTokenizer tok = new StringTokenizer(filestring,"\n\r");
			while (tok.hasMoreTokens()) {
				String cmdline=tok.nextToken();	
				int pos=cmdline.indexOf("=");
				if (pos!=-1) {
					String cmdtype=cmdline.substring(0,pos);
					String cmdrest=cmdline.substring(pos+1);
    				cmdrest = Strip.DoubleQuote(cmdrest,Strip.BOTH);
					//if( debug ) debug("MMBASE-> cmd="+cmdtype+" line="+cmdrest);
					if (cmdtype.equals("classname")) {
						classname=cmdrest;
					} else if (cmdtype.equals("objectname")) {
						objectname=cmdrest;
					} else if (cmdtype.equals("description")) {
						description=cmdrest;
					} else if (cmdtype.equals("dutchsname")) {
						dutchsname=cmdrest;
					} else if (cmdtype.equals("searchage")) {
						searchage=cmdrest;
					} else if (cmdtype.equals("fielddef")) {
						fields.addElement(cmdrest);
					}
				} else {
					debug("initBuilder("+builder+"): ERROR: No command defines in line : '"+cmdline+"'");
				}
			}
			if (classname!=null) {
				if(objectname!=null) {
					if(description!=null) {
						if(dutchsname!=null) {
							startBuilder(classname,objectname,description,dutchsname,searchage,fields);
						} else {
							debug("initBuilder("+builder+"): ERROR: Can't start object("+objectname+"), missing dutchname.");
						}
					} else {
						debug("initBuilder("+builder+"): ERROR: Can't start object("+objectname+"), missing description.");
					}
				} else {
					debug("initBuilder("+builder+"): ERROR: Can't start object("+objectname+"), missing objectname.");
				}
			} else {
				debug("initBuilder("+builder+"): ERROR: Can't start object("+objectname+"), missing classname.");
			}
		}
		return(true);
	}

	boolean startBuilder(String classname,String objectname, String description, String dutchsname,String searchage, Vector fields) {
		if (searchage==null) searchage="14";
		if (!classname.equals("core")) {
			System.out.println("MMBASE -> Starting builder : "+objectname);
			try {
				// is it a full name or inside the org.mmase.* path
				int pos=classname.indexOf('.');
				Class newclass=null;
				if	(pos==-1) {
					newclass=Class.forName("org.mmbase.module.builders."+classname);
				} else {
					newclass=Class.forName(classname);
				}
				//debug("Vwms -> Loaded load class : "+newclass);

				MMObjectBuilder bul = (MMObjectBuilder)newclass.newInstance();
				// debug("MMBase -> started : "+newclass);

				bul.setMMBase(this);
				bul.setTableName(objectname);
				bul.setDescription(description);
				bul.setDutchSName(dutchsname);
				bul.setClassName(classname);
				bul.setSearchAge(searchage);
				bul.init();
				mmobjs.put(objectname,bul);

				// oke set the huge hack for insert layout 
				bul.setDBLayout(fields);

				} catch (Exception e) {
					e.printStackTrace();
					return(false);
				}
		} else {
			// its a core object but to try to set the vars needed
			MMObjectBuilder bul=getMMObject(objectname);
			if (bul!=null) {
				bul.setDescription(description);
				bul.setDutchSName(dutchsname);
				bul.setClassName(classname);
				bul.setSearchAge(searchage);
				// oke set the huge hack for insert layout 
				bul.setDBLayout(fields);
			}
		}
	

		// check the fielddefs defined for this object
		if (FieldDef!=null) {
			//debug("MMBase-> checking fielddefs");
			for (Enumeration e=fields.elements();e.hasMoreElements();) {
				String field=(String)e.nextElement();
				StringTokenizer tok = new StringTokenizer(field,",\n\r");
				String dbtype=tok.nextToken();	
				String guiname=tok.nextToken();	
				String guitype=tok.nextToken();	
				String guipos=tok.nextToken();	
				String guilist=tok.nextToken();	
				String guisearch=tok.nextToken();	
				String dbstate=tok.nextToken();	
				String dbname=tok.nextToken();	

				// get the object number of this builder
				int objectid=TypeDef.getIntValue(objectname);
				Vector f=FieldDef.searchVector("WHERE dbtable="+objectid+" AND dbname='"+dbname+"'");
				if (f==null || f.size()==0) {
					if (debug) debug("No fielddef for : "+objectname+"("+objectid+") field : "+dbname);
					MMObjectNode node=FieldDef.getNewNode("system");
					node.setValue("dbtable",objectid);
					node.setValue("dbtype",dbtype);
					node.setValue("guiname",guiname);
					node.setValue("guitype",guitype);
					node.setValue("dbname",dbname);
					try {
						int i=Integer.parseInt(guipos);
						node.setValue("guipos",i);
						i=Integer.parseInt(guilist);
						node.setValue("guilist",i);
						i=Integer.parseInt(guisearch);
						node.setValue("guisearch",i);
						i=Integer.parseInt(dbstate);
						node.setValue("dbstate",i);
					} catch(Exception u) {
						debug("MMBase -> can't make new FieldDef node invalid int");
					}
					if (objectid!=-1) FieldDef.insert("system",node);
				} else {
					if (debug) debug("found fielddef for : "+objectname+"("+objectid+") field : "+dbname);
				}

			}
		}
		return(true);
	}
	
	public String getDTDBase() {	
		return(dtdbase);
	}

	public MMJdbc2NodeInterface getDatabase() {
		if (database==null) {
			try {
				String tmp=getInitParameter("JDBC2NODE");
				Class newclass=Class.forName("org.mmbase.module.database.support."+tmp);
				if (debug) debug("MMBASE -> Loaded load class : "+newclass);
				database=(MMJdbc2NodeInterface)newclass.newInstance();
				database.init(this);
			} catch(Exception e) {
				e.printStackTrace();	
			}
		}
		return(database);
	}


  public MMObjectNode castNode(MMObjectNode node) {
                /* fake because solved
                */
                int otype=node.getIntValue("otype");
                String ename=TypeDef.getValue(otype);
                if (ename==null) return(null);
                MMObjectBuilder res=getMMObject(ename);
                MMObjectNode node2=res.getNode(node.getIntValue("number"));
                return(node2);
                //return(node);
        }

	public String getAuthType() {
		return(authtype);
	}
	
	public String getLanguage() {
		return(language);
	}
}
