/*
 
This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.
 
The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license
 
*/
package org.mmbase.config;

import java.util.*;
import java.io.*;
import java.sql.*;

import org.mmbase.util.*;

/**
 * @author Case Roole, cjr@dds.nl
 * @version $Id: JVMOptionsReport.java,v 1.1 2000-10-07 17:06:07 case Exp $
 *
 * $Log: not supported by cvs2svn $
 *
 * Check JVM options, e.g. -Dmmbase.config=myconfigpath
 * 
 * Report can be generated by SCAN, see: /mmadmin/config/report.shtml
 * or from the commandline:
 * java -Dmmbase.config=myconfigpath org.mmbase.config.Test
 *
 */
public class JVMOptionsReport extends AbstractReport {
    
    // --- public methods ---------------------------------------
    public String label() {
	return "JVM options";
    }

    /**
     * @return String with java and classpath configuration
     */
    public String report() {
	String res = "";
	String eol = (String)specialChars.get("eol");
	
	String[] optionList = new String[] {
	    "mmbase.config",
	    "mmbase.htmlroot",
	    "mmbase.outputlog",
	    "mmbase.mode"
	};
	
	Hashtable optionUsage = new Hashtable();
	optionUsage.put("mmbase.config","(Mandatory) Path to root of mmbase configuration tree");
	optionUsage.put("mmbase.htmlroot","(Mandatory) Path to root of mmbase administration directories, that is, dir containing mmeditors and mmadmin");
	optionUsage.put("mmbase.outputlog","(Optional) File to which stdout can be written - can be useful for logging");
	optionUsage.put("mmbase.mode","(Optional) Can be set to 'mmdemo' to run mmbase in mmdemo mode");

	String option, value;
	
	for (int i=0;i<optionList.length;i++) {
	    option = optionList[i];
	    value = System.getProperty(option);
	    if (value == null) {
		res = res + option + " .. NOT SET .. " + eol + "  Usage of "+option+": " + (String)optionUsage.get(option) + eol;
	    } else {
		res = res + option + " = '"+ value+"'" + eol;
	    }
	}
;
	return res;
    }

    // --- private methods ---------------------------------------
    private boolean checkServletAPILoadable() {
	try {
	    Class c = Class.forName("java.util.Vector");
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }
    
    private boolean checkXercesLoadable() {
	try {
	    Class c = Class.forName("org.apache.xerces.parsers.DOMParser");
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

    private boolean checkXalanLoadable() {
	try {
	    Class c = Class.forName("org.apache.xalan.xslt.XSLTProcessor");
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

}
