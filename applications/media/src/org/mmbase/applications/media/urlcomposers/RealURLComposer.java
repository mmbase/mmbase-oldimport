/*
  
This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.
  
The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license
  
*/

package org.mmbase.applications.media.urlcomposers;
import org.mmbase.applications.media.builders.MediaFragments;
import org.mmbase.applications.media.Format;
import org.mmbase.module.core.MMObjectNode;
import java.util.Map;
import java.util.Hashtable;

/**
 * A RealURLComposer is an URLComposer which can produce URL's to RM/RA streams.
 *
 *
 * @author Michiel Meeuwissen
 * @version $Id: RealURLComposer.java,v 1.2 2003-02-04 17:43:33 michiel Exp $
 * @todo    Move to org.mmbase.util.media, I think
 */

public class RealURLComposer extends FragmentURLComposer  {

    public RealURLComposer(MMObjectNode provider, MMObjectNode source, MMObjectNode fragment, Map info) {
        super(provider, source, fragment, info);
    }

    protected StringBuffer getURLBuffer() {
        StringBuffer buff = super.getURLBuffer();
        if (getFormat().equals(Format.RM)) {
            return getRMArgs(buff, fragment);
        } else {
            return buff;
        }
    }

    protected static StringBuffer getRMArgs(StringBuffer args, MMObjectNode fragment) {
        if (fragment != null) { // can add this for RM-sources
            int start = fragment.getIntValue("start");
            int end   = fragment.getIntValue("stop");
            char sep = '?';
            if (start > -1) {            
                appendTime(start, args.append(sep).append("start="));
                sep = '&';
            }
            if (end > -1) {            
                appendTime(end, args.append(sep).append("end="));
                sep = '&';
            }
            
            // real...
            String title = fragment.getStringValue("title");
            args.append(sep).append("title=").append(title);
        }
        return args;
    }
    

    /**
     * Script accept times that look like dd:hh:mm:ss.th, where t is tenths of seconds.
     * @param time the time in milliseconds
     * @return the time in real format
     */
    public static StringBuffer appendTime(int time, StringBuffer buf) {
        time /= 10; // in centis

        int centis = -1;
        int s     = 0;
        int min   = 0;
        int h     = 0;
        int d     = 0;

        if (time != 0) {
            centis = time % 100;
            time /= 100;  // in s
            if (time != 0) {
                s = time % 60;
                time /= 60;  // in min
                if (time != 0) {
                    min = time % 60;
                    time /= 60; // in hour
                    if (time != 0) {
                        h = time % 24;
                        d = time / 24;   // in day
                    }
                }
            }
        }
        boolean append = false;

        if (d > 0) append = true;
        if (append) buf.append(d).append(':');
        if (h > 0) append = true;
        if (append) buf.append(h).append(':');
        if (min > 0) append = true;
        if (append) buf.append(min).append(':');
        buf.append(s);
        if (centis > 0) {
            buf.append('.');
            if (centis < 10) buf.append('0');
            buf.append(centis);
        }
        
        return buf;
    }


}
