/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.util.transformers;

import java.util.regex.*;
import java.util.*;

import org.mmbase.util.logging.*;

/**
 * Replaces certain 'forbidden' words by something more decent. Of course, censoring is evil, but
 * sometimes it can be amusing too. This is only an example implementation.
 *
 * @author Michiel Meeuwissen 
 * @since MMBase-1.7
 * @version $Id: Censor.java,v 1.4 2004-07-13 12:08:44 michiel Exp $
 */

public class Censor extends RegexpReplacer {
    private static final Logger log = Logging.getLoggerInstance(Censor.class);

    protected static Map forbidden = new LinkedHashMap();
    
    static {        
        new Censor().readPatterns(forbidden);   
    }

    protected Map getPatterns() {        
        return forbidden;
    }

    protected String getConfigFile() {
        return "censor.xml";
    }



    protected void readDefaultPatterns(Map patterns) {
        patterns.put(Pattern.compile("(?i)mmbase"),      "MMBase");
        patterns.put(Pattern.compile("(?i)microsoft"),   "Micro$soft");
        patterns.put(Pattern.compile("(?i)fuck"),        "****");
    }
    

    public String toString() {
        return "CENSOR";
    }
}
