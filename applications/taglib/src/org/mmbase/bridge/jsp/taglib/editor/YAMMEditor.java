/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib.editor;

import java.io.IOException;
import java.util.*;

import org.mmbase.bridge.*;
import org.mmbase.storage.search.*;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;
import javax.servlet.jsp.PageContext;

import org.mmbase.util.Entry;

/**
 * This is an example implementation of the EditTag. It extends the implementation
 * class Editor of EditTag. To create your own editor with the edittag you could
 * or rather should also extend Editor.<br />
 * EditTagYAMMe works together with the editor yammeditor.jsp. It creates an URL 
 * that looks something like this:<br />
 * yammeditor.jsp?nrs=76&fields=76_number;76_title;76_subtitle;76_intro;80_gui();
 *
 * @author Andr&eacute; van Toly
 * @version $Id: YAMMEditor.java,v 1.7 2006-01-06 14:45:47 andre Exp $
 * @see EditTag, Editor
 * @since MMBase-1.8
 */

public class YAMMEditor extends Editor {

    private static final Logger log = Logging.getLoggerInstance(YAMMEditor.class);

    private List startList = new ArrayList();       // startnodes: 346
    private List pathList  = new ArrayList();       // paths: 346_news,posrel,urls
    private List nList = new ArrayList();           // nodes: 346_602
    private List fList = new ArrayList();           // 602_news.title
    // Map to accommadate the fields and their startnodes
    private Map fld2snMap = new HashMap();
    

    /**
     * Create a string containing all the needed html to find the editor. Or rather
     * it calls one of the older methods i wrote, that was easier then rewriting
     * the old method.
     *
     * @return  A String with a link and an icon to access yammeditor.jsp
     */
    public void getEditorHTML(PageContext context) throws IOException {
        String html = "Sorry. You should see an icon and a link to yammeditor here.";
        
        // Parameters 
        String url = "";
        String icon = "";
        Iterator pi = parameters.iterator();
        while (pi.hasNext()) {
            Entry entry = (Entry) pi.next();
            String key = (String) entry.getKey();
            
            if (key.equals("url")) url = (String) entry.getValue();
            if (key.equals("icon")) icon = (String) entry.getValue();
        } 
        
        url = makeRelative(url, context);
        html = makeHTML(url, icon);
        
        log.debug("returning: " + html);
        context.getOut().write(html);
    }
    
    /**
     * Values passed by the EditTag from the FieldTags.
     *
     * @param queryList     List with SearchQuery objects from fields
     * @param nodenrList    List with nodenumbers
     * @param fieldList     List with fieldnames 
     */ 
    public void registerFields(List queryList, List nodenrList, List fieldList) {
        log.debug("processing fields");
        for (int i = 0; i < nodenrList.size(); i++) {
            String fldName = (String) fieldList.get(i);
            log.debug("processing field '" + fldName + "'");
            Query query = (Query) queryList.get(i);
            String nodenr = (String) nodenrList.get(i);
            
            processField(query, nodenr, fldName);
        }
        
        // clear the lists
        //queryList.clear();
        //nodenrList.clear();
        //fieldList.clear();
    }

    /**
     * Processes a field with nodenr and query into several lists 
     *
     * @param query     SearchQuery that delivered the field
     * @param nodenr    Nodenumber of the node the field belongs to
     * @param fieldName Name of the field
     */ 
    protected void processField(Query query, String nodenr, String fieldName) {
        
        // fill paths
        String path = getPathFromQuery(query);
        if (!path.equals("") && !pathList.contains(path)) {
            pathList.add(path);
            log.debug("Added path : " + path);
        }
        
        List nl = getNodesFromQuery(query, nodenr);
        Iterator e = nl.iterator();         // iterate over the startnodes
        while (e.hasNext()) {
            String nr = (String)e.next();
            boolean startnode = false;
            
            /* fills fld2snMap (only used to keep track of startnodes, 
            it contains nodenr's
            when a nr is found in this map ...
            */
            
            if (!fld2snMap.containsValue(nr) ) {
                fld2snMap.put(String.valueOf(nodenr), nr);
                log.debug("Added nodenr : " + nodenr + " sn : " + nr + " to fld2snMap");
            } else if (fld2snMap.isEmpty()) {
                fld2snMap.put(String.valueOf(nodenr), nr);
                startnode = true;
                log.debug("Added nodenr (startnode): " + nodenr + " sn : " + nr + " to fld2snMap");
            } else {                    // a node is a startnode when there was
                startnode = true;       //   no previous field with this nodenr as startnodenr
            }
            
            // fill startList (startnodes)
            if (!startList.contains(nr) && startnode) {         // 507 (= just startnodenr)
                startList.add(nr);
                log.debug("Added startnode : " + nr);
            }
            
            // fill nodeList (just the nodes in a page)
            String str = nr + "_" + String.valueOf(nodenr);     // 507_234 (= startnodenr_nodenr)
            if (!nList.contains(str)) {
                nList.add(str);
                log.debug("Added nodenr : " + str);
            }
            
            // fill fieldList (all the used fields in a page)   // 507_title (= startnodenr_fieldname)
            String fieldstr = nr + "_" + fieldName;
            if (!fList.contains(fieldstr)) {
                fList.add(fieldstr);
                log.debug("Added field : " + fieldstr);
            }
        }

    }
        
    protected List getNodesFromQuery(Query query, String nr) {
        List nl = new ArrayList();
        List steps = query.getSteps();
        
        if (steps.size() == 1) {    // why ?
            nl.add(nr);
            log.debug("1. added nr to list of all the nodes in query: " + nr);
        } 
        
        Iterator si = steps.iterator();
        while (si.hasNext()) {
            Step step = (Step) si.next();
            
            // Get the nodes from this step 
            //   (haalt alle nodes uit step, itereert erover en stopt ze in nl )
            SortedSet nodeSet = step.getNodes();
            for (Iterator nsi = nodeSet.iterator(); nsi.hasNext();) {
                Integer n = (Integer)nsi.next();
                nr = String.valueOf(n);
                
                if (!nl.contains(nr)) {
                    nl.add(nr);
                    log.debug("2. added nr to list of all the nodes in query: " + nr);
                }
            }
            
        }
        return nl;
    }
    
    
    /**
     * Generates pathList with the needed paths for the editor from an list 
     * with queries.
     *
     * @param   ql  List with queries
     * @return      List with paths from #getPathFromQuery
     */
    protected List fillPathList(List ql) {
        List pl = new ArrayList();
        
        Iterator i = ql.iterator();
        while (i.hasNext()) {
            Query q = (Query) i.next();
            String path = getPathFromQuery(q);
            if (!path.equals("") && !pl.contains(path)) {
                pl.add(path);
                log.debug("Added path '" + path + "' to pl"); 
            }
        }
        log.debug("Filled pathList with " + pl.size() + " paths.");
        
        return pl;
    }
    
    /**
    * Get the path from this query where the field originated from. 
    * Returns an empty String when there is no path (startnode f.e.).
    *
    * @param    query   The query
    * @return   A String containing a path in the form 345_news,posrel,urls 
    *           meaning: startnode(s)_path
    */  
    protected String getPathFromQuery(Query query) {
        StringBuffer path = new StringBuffer();
        
        java.util.List steps = query.getSteps();
        log.debug("Nr of steps : " + steps.size());
        if (steps.size() > 1) {     // no need to look for a path when there is just 1 step
            Iterator si = steps.iterator();
            while (si.hasNext()) {
                Step step = (Step) si.next();
                            
                String nodenrs = "";
                SortedSet nodeSet = step.getNodes();    // Get the (start?)nodes from this step
                for (Iterator nsi = nodeSet.iterator(); nsi.hasNext();) {
                    Integer number = (Integer)nsi.next();
                    if (nodenrs.equals("")) {
                        nodenrs = String.valueOf(number);
                    } else {
                        nodenrs = nodenrs + "," + String.valueOf(number);
                    }
                    
                }
                
                // path: Get one nodetype at the time (the steps)
                if (step.getAlias() != null) {
                    if (path.length() == 0) {
                        path.append(nodenrs).append('_').append(step.getAlias());
                    } else {
                        path.append(",").append(step.getAlias());
                    }
                }
            }
        }
        return path.toString();
    }
    
    /**
    * Creates a ; seperated String from a List to create the url to yammeditor with 
    * paths, fields, startnodes or whatnot
    * 
    * @param    al One of the Lists to use
    * @return   A ; seperated String with the elements in the List
    *
    */
    protected String makeList4Url(List al) {
        StringBuffer sb = new StringBuffer();
        if (al.size() > 0) {
            Iterator e = al.iterator();
            while(e.hasNext()) {
                if (sb.length() == 0) { 
                    sb.append( (String) e.next() );
                } else { 
                    sb.append(';').append( (String) e.next() );
                }
            }
        }
        return sb.toString();
    }
    
    /**
    * Creates a string with the link (and icon) to the editor
    *
    * @param editor     An url to an editor
    * @param icon       An url to a graphic file
    * @return           An HTML string with a link suitable for the editor yammeditor.jsp
    * 
    */
    public String makeHTML(String url, String icon) {
        StringBuffer html = new StringBuffer();

        html.append("<div class=\"et\"><a title=\"click to edit\" href=\"");
        html.append(url);
        html.append("?nrs=").append(makeList4Url(startList));       // startnodes:  676 (startnode)
        html.append("&amp;paths=").append(makeList4Url(pathList));  // paths:       676_news,posrel,images (startnode_path)
        html.append("&amp;fields=").append(makeList4Url(fList));    // fields:      345_images.number (startnode_nodetype.field)
        html.append("&amp;nodes=").append(makeList4Url(nList)); // nodes:       676_345 (startnode_nodenr)
        html.append("\" onclick=\"window.open(this.href); return false;\">");
        
        if (!icon.equals("")) {
            html.append("<img src=\"").append(icon).append("\" alt=\"edit\">");
        } else {
            html.append("edit");            
        }
        
        html.append("</a></div>");
        
        return html.toString();
    }
    
    /**
     * Copied this method from the UrlTag.
     * If it would be nice that an URL starting with '/' would be generated relatively to the current request URL, then this method can do it.
     * If the URL is not used to write to (this) page, then you probably don't want that.
     *
     * The behaviour can be overruled by starting the URL with two '/'s.
     *
     */
    protected String makeRelative(String url, PageContext pageContext) {
        StringBuffer show = new StringBuffer(url);
        javax.servlet.http.HttpServletRequest req = (javax.servlet.http.HttpServletRequest)pageContext.getRequest();
        if (show.charAt(0) == '/') { // absolute on servletcontex
            if (show.length() > 1 && show.charAt(1) == '/') {
                log.debug("'absolute' url, not making relative");
                /*
                if (addContext()) {
                    show.deleteCharAt(0);
                    show.insert(0, req.getContextPath());
                }
                */
            } else {
                log.debug("'absolute' url");
                String thisDir = new java.io.File(req.getServletPath()).getParent();
                show.insert(0,  org.mmbase.util.UriParser.makeRelative(thisDir, "/")); // makes a relative path to root.
            }
        }
        return show.toString();
    }
    
}
