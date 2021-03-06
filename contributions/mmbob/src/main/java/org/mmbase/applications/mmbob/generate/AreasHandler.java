/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.applications.mmbob.generate;

import java.lang.*;
import java.net.*;
import java.util.*;
import java.io.*;

import org.mmbase.util.*;
import org.mmbase.module.core.*;
import org.mmbase.module.corebuilders.*;
import org.mmbase.applications.mmbob.*;

import org.mmbase.util.logging.Logging;
import org.mmbase.util.logging.Logger;

/**
 * @author Daniel Ockeloen
 * 
 */
public class AreasHandler extends Handler {

    private int generatecount=0; 
    private int delaytime=0; 
    private String inforum;
 
    // logger
    static private Logger log = Logging.getLoggerInstance(AreasHandler.class); 

    public AreasHandler(int generatecount, int delaytime, String inforum) {
	this.generatecount=generatecount;
	this.delaytime=delaytime;
	this.inforum=inforum;
	init();
    }

    public void run() {
	try {
	log.info("Run called on Areas Generate Thread");
	if (generatecount>0) {
		for (int i=0;i<generatecount;i++) {
			log.info("generate area : "+i);
			if (!inforum.equals("all")) {
				createArea(inforum,i);
			}
		}
	}
	} catch(Exception e) {
	}
	Generate.setState(0);
    }


    public boolean createArea(String forumid,int i) {
	String name=generateArea();
	String description="This is the description generated for post area : "+name;
        Forum f=ForumManager.getForum(forumid);
        if (f!=null) {
               int postareaid=f.newPostArea(name,description);
        }
	return true;
    }

}
