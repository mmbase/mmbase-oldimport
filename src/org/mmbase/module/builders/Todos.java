/*

VPRO (C)

This source file is part of mmbase and is (c) by VPRO until it is being
placed under opensource. This is a private copy ONLY to be used by the
MMBase partners.

*/
package org.mmbase.module.builders;

import java.sql.*;

import org.mmbase.module.database.*;
import org.mmbase.module.core.*;
import org.mmbase.util.*;

/**
 */
public class Todos extends MMObjectBuilder {


	public String getGUIIndicator(String field,MMObjectNode node) {
		if (field.equals("status")) {
			int val=node.getIntValue("status");
			switch(val) {
				case -1: return("Unknown");
				case 1: return("Described");
				case 2: return("Claimed");
				case 3: return("Researched");
				case 4: return("Underway");
				case 5: return("Stopped");
				case 6: return("Testing");
				case 7: return("Finished");
				default: return("Unknown");
			}
		}
		return(null);
	}


	public Object getValue(MMObjectNode node, String field) {
		if (field.equals("showstatus")) {
			int val=node.getIntValue("status");
			switch(val) {
				case -1: return("Unknown");
				case 1: return("Described");
				case 2: return("Claimed");
				case 3: return("Researched");
				case 4: return("Underway");
				case 5: return("Stopped");
				case 6: return("Testing");
				case 7: return("Finished");
				default: return("Unknown");
			}
		}
		return(null);
	}
}
