/*
  
This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.
  
The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license
  
*/

package org.mmbase.applications.media.urlcomposers;

import org.mmbase.module.core.*;
import org.mmbase.servlet.MMBaseServlet;
import org.mmbase.util.logging.*;
import org.mmbase.applications.media.Format;
import java.util.*;
import java.net.*;


/**
 * Provides the functionality to create URL's (or URI's) for a certain
 * fragment, source, provider combination.
 *
 * Depends on mediafragment.ram.jsp and mediafragment.asf.jsp in the
 * templates dir. These can be mapped to something else in
 * web.xml. The servlet name must be media-asf and media-rm then.
 * 
 *
 *
 * @author Michiel Meeuwissen
 * @author Rob Vermeulen (VPRO)
 * @since MMBase-1.7
 * @see   Config
 */
public class RamURLComposer extends FragmentURLComposer { // also for wmp/asx
    private static Logger log = Logging.getLoggerInstance(RamURLComposer.class.getName());
    
    protected  Format          format;
    public RamURLComposer(MMObjectNode provider, MMObjectNode source, MMObjectNode fragment, Map info, List cacheExpireObjects) {
        super(provider, source, fragment, info, cacheExpireObjects);
        this.format = Format.get(source.getIntValue("format"));
    }
    protected StringBuffer  getURLBuffer() {
        List servlets = MMBaseServlet.getServletMappings("media-" + format);
        String servlet;
        if (servlets == null || servlets.size() == 0) {
            log.error("No mapping found to media-" + format + " servlet. Change this in your web.xml");
            servlet = Config.templatesDir + "mediafragment." + format + ".jsp";
        } else {
            String root = MMBaseContext.getHtmlRootUrlPath();
            root = root.substring(0, root.length() - 1);
            servlet = root  + (String) servlets.get(0);
        }
        
        return new StringBuffer("http://" + Config.host + servlet + "?fragment=" + (fragment == null ? "" : "" + fragment.getNumber()));

        // todo, perhaps simply the right source number should be passed
    }

    public Format  getFormat()   { 
        if (format == Format.RM)  return Format.RAM; 
        if (format == Format.ASF) return Format.WMP; 
        return format;
    } 

}
