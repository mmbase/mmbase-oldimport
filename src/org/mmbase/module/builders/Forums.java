/*

VPRO (C)

This source file is part of mmbase and is (c) by VPRO until it is being
placed under opensource. This is a private copy ONLY to be used by the
MMBase partners.

*/
package org.mmbase.module.builders;

import java.util.*;
import java.sql.*;

import org.mmbase.module.database.*;
import org.mmbase.module.core.*;
import org.mmbase.util.*;

/**
 * @author Daniel Ockeloen
 * @author Rico Jansen
 * @version 8 Dec 1999
 */
public class Forums extends MMObjectBuilder {

	/*
	public Forums(MMBase m) {
		this.mmb=m;
		this.tableName="forums";
		this.description="Forums grouping of contributions";
		this.dutchSName="Forum";
		init();
		m.mmobjs.put(tableName,this);
	}
	*/

	
	/**
	* insert a new object, normally not used (only subtables are used)
	*/
	/*
	public boolean create() {
		// create the main object table
		try {
			MultiConnection con=mmb.getConnection();
			Statement stmt=con.createStatement();
			stmt.executeUpdate("create row type "+mmb.baseName+"_"+tableName+"_t (title varchar(255) not null"
				+", intro char(2048) not null) under "+mmb.baseName+"_object_t");
			System.out.println("Created "+tableName);
			stmt.close();
			con.close();
		} catch (SQLException e) {
			System.out.println("can't create type "+tableName);
			e.printStackTrace();
		}
		try {
			MultiConnection con=mmb.getConnection();
			Statement stmt=con.createStatement();
			stmt.executeUpdate("create table "+mmb.baseName+"_"+tableName+" of type "+mmb.baseName+"_"+tableName+"_t ("
				+" primary key(number)) under "+mmb.baseName+"_object");
			stmt.close();
			con.close();
		} catch (SQLException e) {
			System.out.println("can't create table "+tableName);
			e.printStackTrace();
		}
		return(false);
	}
	*/


	public void setDefaults(MMObjectNode node) {
		node.setValue("description","");
	}

	public String getGUIIndicator(MMObjectNode node) {
        String str=node.getStringValue("title");
        if (str.length()>15) {
            return(str.substring(0,12)+"...");
        } else {
            return(str);
        }
	}

	 public String replace(scanpage sp, StringTokenizer tok) {
		if (tok.hasMoreTokens()) {
			String cmd=tok.nextToken();	
			if (cmd.equals("FIELD")) { 
				return(getObjectField(sp,tok));
			}
		}
		return("");
	 }

	/**
	 * Generate a list of values from a command to the processor
	 */
	 public Vector  getList(scanpage sp,StringTagger tagger, StringTokenizer tok) {
		Vector results=new Vector();
		if (tok.hasMoreTokens()) {
			String cmd=tok.nextToken();
			System.out.println("FORUMS->"+cmd);
			if (cmd.equals("ACTIVEOPINIONS")) {
				results=getActiveOpinions(tagger);
			} else if (cmd.equals("ACTIVEARGUMENTS")) {
				results=getActiveArguments(tagger);
			} else if (cmd.equals("PROPOSEDOPINIONS")) {
				results=getProposedOpinions(tagger);
			} else if (cmd.equals("PROPOSEDARGUMENTS")) {
				results=getProposedArguments(tagger);
			} else if (cmd.equals("OPINION")) {
				results=getOpinion(tagger);
//			} else if (cmd.equals("ARGUMENT")) {
//				results=getArgument(tagger);
			}
			System.out.println("FORUMS->"+cmd+" done");
		}
		return(results);
	}

	public Vector getActiveOpinions(StringTagger tagger) {
		Vector results=new Vector();
		String forum=tagger.Value("FORUM");
		MMObjectNode forumnode=getNode(forum);

		if (forumnode!=null) {
			boolean pollinfo;
			String pi=tagger.Value("POLL");
			if (pi!=null && (pi.equals("YES") || pi.equals("\"YES\""))) pollinfo=true;
			else pollinfo=false;
			
			Enumeration tk=mmb.getInsRel().getRelated(forumnode.getIntValue("number"),1938425); // find the active opinion container
			if (tk.hasMoreElements()) {
				MMObjectNode actopinode=(MMObjectNode)tk.nextElement();
				Enumeration r=mmb.getInsRel().getRelated(actopinode.getIntValue("number"),1946004); // find the conarguments in this container
				while (r.hasMoreElements()) {
					MMObjectNode conopinode=(MMObjectNode)r.nextElement();
					results.addElement(""+conopinode.getIntValue("number"));
					results.addElement(""+conopinode.getIntValue("id"));
					MMObjectNode opinode=getNode(conopinode.getIntValue("id"));
					if (opinode!=null) {
						String tmp=opinode.getStringValue("title");
						if (tmp!=null) {
							results.addElement(tmp);
						} else {
							results.addElement("");
						}
						tmp=opinode.getStringValue("body");
						if (tmp!=null) {
							results.addElement(tmp);
						} else {
							results.addElement("");
						}
					} else {
						results.addElement("");
						results.addElement("");
					}

					// add the attached poll (question/posrel/answer)
					if (pollinfo) {
						results=getPollInfo2(conopinode,results);	
					} else {
						results=getPollInfo(conopinode,results);	
					}
					Enumeration y=mmb.getInsRel().getRelated(opinode.getIntValue("number"),"people"); // find the user this statement belongs to
					if (y.hasMoreElements()) {
						MMObjectNode peoplenode=(MMObjectNode)y.nextElement();
						results.addElement(peoplenode.getStringValue("firstname")+" "+peoplenode.getStringValue("lastname"));	
						results.addElement(""+peoplenode.getIntValue("number"));	
					} else {
						results.addElement("");
						results.addElement("");
					}
				}
				
			}
		}
		return(results);
	}

	public Vector getOpinion(StringTagger tagger) {
		Vector results=new Vector();
		String conopi=tagger.Value("CONOPI");
		MMObjectNode conopinode=getNode(conopi);

		// conopi.number,conopi.id,opi.title,opi.body,X,people.name,people.number
		// Poll=NO X= "" , question.number ITEMS=8
		// Poll=YES X= question.number, 1, yes% , 2 no%, 0 , maybe% ITEMS=13
		if (conopinode!=null) {
			boolean pollinfo;
			String pi=tagger.Value("POLL");
			if (pi!=null && (pi.equals("YES") || pi.equals("\"YES\""))) pollinfo=true;
			else pollinfo=false;
			
			results.addElement(""+conopinode.getIntValue("number"));
			results.addElement(""+conopinode.getIntValue("id"));
			MMObjectNode opinode=getNode(conopinode.getIntValue("id"));
			if (opinode!=null) {
				String tmp=opinode.getStringValue("title");
				if (tmp!=null) {
					results.addElement(tmp);
				} else {
					results.addElement("");
				}
				tmp=opinode.getStringValue("body");
				if (tmp!=null) {
					results.addElement(tmp);
				} else {
					results.addElement("");
				}
			} else {
				results.addElement("");
				results.addElement("");
			}

			// add the attached poll (question/posrel/answer)
			if (pollinfo) {
				results=getPollInfo2(conopinode,results);	
			} else {
				results=getPollInfo(conopinode,results);	
			}
			Enumeration y=mmb.getInsRel().getRelated(opinode.getIntValue("number"),"people"); // find the user this statement belongs to
			if (y.hasMoreElements()) {
				MMObjectNode peoplenode=(MMObjectNode)y.nextElement();
				results.addElement(peoplenode.getStringValue("firstname")+" "+peoplenode.getStringValue("lastname"));	
				results.addElement(""+peoplenode.getIntValue("number"));	
			} else {
				results.addElement("");
				results.addElement("");
			}
		}
		return(results);
	}

	public Vector getProposedOpinions(StringTagger tagger) {
		Vector results=new Vector();
		String forum=tagger.Value("FORUM");
		MMObjectNode forumnode=getNode(forum);
		if (forumnode!=null) {
			boolean pollinfo;
			String pi=tagger.Value("POLL");
			if (pi!=null && (pi.equals("YES") || pi.equals("\"YES\""))) pollinfo=true;
			else pollinfo=false;
			Enumeration tk=mmb.getInsRel().getRelated(forumnode.getIntValue("number"),1938427); // find the proposed opinion container
			if (tk.hasMoreElements()) {
				MMObjectNode proopinode=(MMObjectNode)tk.nextElement();
				Enumeration r=mmb.getInsRel().getRelated(proopinode.getIntValue("number"),1946004); // find the conarguments in this container
				while (r.hasMoreElements()) {
					MMObjectNode conopinode=(MMObjectNode)r.nextElement();
					results.addElement(""+conopinode.getIntValue("number"));
					results.addElement(""+conopinode.getIntValue("id"));
					MMObjectNode opinode=getNode(conopinode.getIntValue("id"));
					if (opinode!=null) {
						String tmp=opinode.getStringValue("title");
						if (tmp!=null) {
							results.addElement(tmp);
						} else {
							results.addElement("");
						}
						tmp=opinode.getStringValue("body");
						if (tmp!=null) {
							results.addElement(tmp);
						} else {
							results.addElement("");
						}
					} else {
						results.addElement("");
						results.addElement("");
					}

					// add the attached poll (question/posrel/answer)
					if (pollinfo) {
						results=getPollInfo2(conopinode,results);	
					} else {
						results=getPollInfo(conopinode,results);	
					}
					Enumeration y=mmb.getInsRel().getRelated(opinode.getIntValue("number"),"people"); // find the user this statement belongs to
					if (y.hasMoreElements()) {
						MMObjectNode peoplenode=(MMObjectNode)y.nextElement();
						results.addElement(peoplenode.getStringValue("firstname")+" "+peoplenode.getStringValue("lastname"));	
						results.addElement(""+peoplenode.getIntValue("number"));	
					} else {
						results.addElement("");
						results.addElement("");
					}
				}
				
			}
		}
		return(results);
	}

	public Vector getPollInfo(MMObjectNode conopinode, Vector results) {
		Enumeration r=mmb.getInsRel().getRelated(conopinode.getIntValue("number"),68254); // find the question attached to this conopi
		if (r.hasMoreElements()) {
			MMObjectNode questionnode=(MMObjectNode)r.nextElement();
/*
			Enumeration t=questionnode.getRelations(621); // find the relation to the answer (621)
			while (t.hasMoreElements()) {
				MMObjectNode posrelnode=(MMObjectNode)t.nextElement();
				posrelnode=getNode(posrelnode.getIntValue("number"));
				System.out.println("Forums-> posrel = "+posrelnode+" "+posrelnode.getIntValue("pos"));
			}
*/
			results.addElement("");
			results.addElement(""+questionnode.getIntValue("number"));
		} else {
			results.addElement("");
			results.addElement("");
		}
		return(results);
	}

	public Vector getPollInfo2(MMObjectNode conopinode, Vector results) {
		MMObjectNode node,yesnode=null,nonode=null,maynode=null;
		String atitle;
		Vector tables=new Vector();
		tables.addElement("conopi");
		tables.addElement("questions");
		tables.addElement("posrel");
		tables.addElement("answers");
		Vector fields=new Vector();
		fields.addElement("questions.number");
		fields.addElement("posrel.pos");
		fields.addElement("answers.number");
		fields.addElement("answers.title");
		Vector ordervec=new Vector();
		Vector dirvec=new Vector();
		MultiRelations multirelations=(MultiRelations)mmb.getMMObject("multirelations");		
		Vector vec=multirelations.searchMultiLevelVector(conopinode.getIntValue("number"),fields,"YES",tables,"",ordervec,dirvec);

		if (vec.size()>0) {
			if (vec.size()!=3) System.out.println("Forums -> More then 3 answers ? "+vec);
			for (Enumeration e=vec.elements();e.hasMoreElements();) {
				node=(MMObjectNode)e.nextElement();
				atitle=node.getStringValue("answers.title");
				// Note add the one for 'Accepteer Ja/Nee');
				if (atitle.startsWith("One") || atitle.startsWith("Nee")) {
					nonode=node;
				} else if (atitle.startsWith("Mee") || atitle.startsWith("Ja")) {
					yesnode=node;
				} else if (atitle.startsWith("Nog")) {
					maynode=node;
				} else {
					System.out.println("Jerry -> Unknown title "+atitle+" ("+node+") ");
				}
			}
			calcPercentages(yesnode,nonode,maynode);
			results.addElement(""+yesnode.getIntValue("questions.number")); // Question number

			results.addElement("1"); // Yes ( );
			results.addElement(""+yesnode.getStringValue("posrel.percentage")); // Percent Yes

			results.addElement("2"); // No ( );
			results.addElement(""+nonode.getStringValue("posrel.percentage")); // Percent No

			results.addElement("0"); // Don't know ( );
			results.addElement(""+maynode.getStringValue("posrel.percentage")); // Percent Don't know
		} else {
			// Perhaps add q number here ?
			results.addElement(""); // Question number

			results.addElement("1"); // Yes ( );
			results.addElement("0"); // Percent Yes

			results.addElement("2"); // No ( );
			results.addElement("0"); // Percent No

			results.addElement("0"); // Don't know ( );
			results.addElement("0"); // Percent Don't know
		}
		return(results);
	}

	private void calcPercentages(MMObjectNode yesnode,MMObjectNode nonode,MMObjectNode maynode) {
		double sum;
		double ypercent,npercent,mpercent;
		double y,n,m;

		y=yesnode.getIntValue("posrel.pos");
		n=nonode.getIntValue("posrel.pos");
		m=maynode.getIntValue("posrel.pos");
		sum=y+n+m;
		if (sum==0.0) {
			ypercent=npercent=mpercent=0.0;
		} else {
			ypercent=(100.0*y)/sum;
			npercent=(100.0*n)/sum;
			mpercent=(100.0*m)/sum;
		}
		System.out.println("Forums -> calcPercentage "+ypercent+" , "+npercent+" , "+mpercent+" totalvotes "+sum);
		yesnode.setValue("posrel.percentage",""+(int)(ypercent+0.5));
		nonode.setValue("posrel.percentage",""+(int)(npercent+0.5));
		maynode.setValue("posrel.percentage",""+(int)(mpercent+0.5));
	}

	public Vector getActiveArguments(StringTagger tagger) {
		MMObjectNode argnode=null;
		Vector results=new Vector();
		String conopi=tagger.Value("CONOPI");
		MMObjectNode conopinode=getNode(conopi);
		if (conopinode!=null) {
			Enumeration tk=mmb.getInsRel().getRelated(conopinode.getIntValue("number"),1938424);
			if (tk.hasMoreElements()) {
				MMObjectNode actargnode=(MMObjectNode)tk.nextElement();
				Enumeration r=mmb.getInsRel().getRelated(actargnode.getIntValue("number"),1946005);
				while (r.hasMoreElements()) {
					MMObjectNode conargnode=(MMObjectNode)r.nextElement();
					results.addElement(""+conargnode.getIntValue("number"));
					results.addElement(""+conargnode.getIntValue("id"));
					argnode=getNode(conargnode.getIntValue("id"));
					if (argnode!=null) {
						String tmp=argnode.getStringValue("title");
						if (tmp!=null) {
							results.addElement(tmp);
						} else {
							results.addElement("");
						}
						tmp=argnode.getStringValue("body");
						if (tmp!=null) {
							results.addElement(tmp);
						} else {
							results.addElement("");
						}
					} else {
						results.addElement("");
						results.addElement("");
					}
					results=getArgumentPollInfo(conargnode,results);
					Enumeration y=mmb.getInsRel().getRelated(argnode.getIntValue("number"),"people"); // find the user this statement belongs to
					if (y.hasMoreElements()) {
						MMObjectNode peoplenode=(MMObjectNode)y.nextElement();
						results.addElement(peoplenode.getStringValue("firstname")+" "+peoplenode.getStringValue("lastname"));	
						results.addElement(""+peoplenode.getIntValue("number"));	
					} else {
						results.addElement("");
						results.addElement("");
					}
				}
			}
		}
		return(results);
	}

	public Vector getProposedArguments(StringTagger tagger) {
		MMObjectNode argnode=null;
		Vector results=new Vector();
		String conopi=tagger.Value("CONOPI");
		MMObjectNode conopinode=getNode(conopi);
		if (conopinode!=null) {
			Enumeration tk=mmb.getInsRel().getRelated(conopinode.getIntValue("number"),1938427); // related proposed arguments
			if (tk.hasMoreElements()) {
				MMObjectNode proargnode=(MMObjectNode)tk.nextElement();
				Enumeration r=mmb.getInsRel().getRelated(proargnode.getIntValue("number"),1946004); // related argument containers 
				while (r.hasMoreElements()) {
					MMObjectNode conargnode=(MMObjectNode)r.nextElement();
					results.addElement(""+conargnode.getIntValue("number"));
					results.addElement(""+conargnode.getIntValue("id"));
					argnode=getNode(conargnode.getIntValue("id"));
					if (argnode!=null) {
						String tmp=argnode.getStringValue("title");
						if (tmp!=null) {
							results.addElement(tmp);
						} else {
							results.addElement("");
						}
						tmp=argnode.getStringValue("body");
						if (tmp!=null) {
							results.addElement(tmp);
						} else {
							results.addElement("");
						}
					} else {
						results.addElement("");
						results.addElement("");
					}
					results=getArgumentPollInfo(conargnode,results);
					Enumeration y=mmb.getInsRel().getRelated(argnode.getIntValue("number"),"people"); // find the user this statement belongs to
					if (y.hasMoreElements()) {
						MMObjectNode peoplenode=(MMObjectNode)y.nextElement();
						results.addElement(peoplenode.getStringValue("firstname")+" "+peoplenode.getStringValue("lastname"));	
						results.addElement(""+peoplenode.getIntValue("number"));	
					} else {
						results.addElement("");
						results.addElement("");
					}
				}
			}
		}
		return(results);
	}

	public Vector getArgumentPollInfo(MMObjectNode conargnode, Vector results) {
		Enumeration t=conargnode.getRelations(951212); // find the relation to the answer (951212)
		if (t.hasMoreElements()) {
			MMObjectNode posrelnode=null;
			while (t.hasMoreElements()) {
				posrelnode=(MMObjectNode)t.nextElement();
				posrelnode=getNode(posrelnode.getIntValue("number"));
				System.out.println("Forums-> posrel = "+posrelnode+" "+posrelnode.getIntValue("contexttype"));
			}
			results.addElement(""+posrelnode.getIntValue("contexttype"));
		} else {
			results.addElement("");
		}
		return(results);
	}

	String getObjectField(scanpage sp, StringTokenizer tok) {
		if (tok.hasMoreTokens()) {
			String nodeNr=tok.nextToken();
			String fieldname=tok.nextToken();
			String result=null;
			MMObjectNode cnode=getNode(nodeNr);
			if (cnode!=null) {
				nodeNr=""+cnode.getIntValue("id");
			}
			MMObjectNode node=getNode(nodeNr);
			if (result!=null) {
				return(result);
			} else  {
				//String result=node.getValue(fieldName);
				String type2=node.getDBType(fieldname);
				if (type2==null) {
					result=""+node.getValue(fieldname);
				} else if (type2.equals("varchar")) {
					result=node.getStringValue(fieldname);
				} else if (type2.equals("varchar_ex")) {
					result=node.getStringValue(fieldname);
				} else if (type2.equals("int")) {
					result=""+node.getIntValue(fieldname);
				} else if (type2.equals("text")) {
					result=node.getStringValue(fieldname);
				}	
				if (result!=null && !result.equals("null")) {
					return(result);
				} else {
					return("");
				}
			}
		}
		return("no command defined");
	}

	/*
	 * Forum opinion / argument migration 
	 *
	 */

	private void migrateOpinion(int forum,int conopi,boolean accept) {
		MMObjectNode forumnode=getNode(forum);
		MMObjectNode rnode,proopi,go_opi=null,connode,newcon,actnode;
		Enumeration tk,e1;
		int pronumber;
		int found=0;

		// Get proposed opininon container
		tk=mmb.getInsRel().getRelated(forum,"proopi");
		if (tk.hasMoreElements()) {
			proopi=(MMObjectNode)tk.nextElement();
			pronumber=proopi.getIntValue("number");
			// Select correct container to move to.
			if (accept) {
				e1=mmb.getInsRel().getRelated(forum,"accopi");
			} else {
				e1=mmb.getInsRel().getRelated(forum,"rejopi");
			}
			// Find container
			if (e1.hasMoreElements()) {
				go_opi=(MMObjectNode)e1.nextElement();
				connode=getNode(conopi);
				if (connode!=null) {
					// Get relations of conopi
					Enumeration rels=connode.getRelations("insrel");
					if (rels.hasMoreElements()) {
						// Find the one to the proposed opinion container
						rnode=null;
						while(rels.hasMoreElements() && found==0) {
							rnode=(MMObjectNode)rels.nextElement();
							if (rnode.getIntValue("snumber")==pronumber) found=1;
							if (rnode.getIntValue("dnumber")==pronumber) found=2;
						}
						// Move the relation to the go container (rejopi/accopi)
						if (found!=0) {
							if (found==1) {
								rnode.setValue("snumber",go_opi.getIntValue("number"));
								rnode.commit();
							} else if (found==2) {
								rnode.setValue("dnumber",go_opi.getIntValue("number"));
								rnode.commit();

							} else {
								System.out.println("Forums -> BitRotter strikes again!!! "+conopi);
							}
							// Clone conopi and attach to actopi
							e1=mmb.getInsRel().getRelated(forum,"actopi");
							if (e1.hasMoreElements()) {
								actnode=(MMObjectNode)e1.nextElement();
								newcon=mmb.getMMObject("conopi").getNewNode("Forums");
								newcon.setValue("id",connode.getIntValue("id"));
								int r=newcon.insert("Forums");
								if (r>=0) {
									rnode=mmb.getInsRel().getNewNode("Forums");
									rnode.setValue("rnumber",14);
									rnode.setValue("snumber",r);
									rnode.setValue("dnumber",actnode.getIntValue("number"));
									r=rnode.insert("Forums");
									if (r<0) {
										System.out.println("Forums -> can't create relations between "+newcon.getIntValue("number")+" AND "+actnode.getIntValue("number"));
									}
								} else {
									System.out.println("Forums -> can't insert new conopi"+forum+","+conopi);
								}
							} else {
							
								System.out.println("Forums -> forum has no relation to actopi "+forum);
							}
						} else {
							System.out.println("Forums -> conopi has no relation to proopi "+conopi);
						}
					} else {
						System.out.println("Forums -> conopi has no relations ? "+conopi);
					}
				}
			} else {
				System.out.println("Forums -> No accopi/rejopi to migrate to "+forumnode);
			}

		} else {
			System.out.println("Forums -> Can't promote conopi not attached to proopi : "+conopi);
		}

	}
}
