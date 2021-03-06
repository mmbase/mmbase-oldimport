/*
 * This software is OSI Certified Open Source Software.
 * OSI Certified is a certification mark of the Open Source Initiative. The
 * license (Mozilla version 1.0) can be read at the MMBase site. See
 * http://www.MMBase.org/license
 */
package org.mmbase.searchrelate.handlers;

import org.mmbase.bridge.*;
import org.mmbase.storage.search.*;
import org.mmbase.datatypes.handlers.html.*;
import org.mmbase.datatypes.handlers.Request;
import org.mmbase.util.*;
import javax.servlet.ServletException;
import java.io.*;
import java.util.*;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/*
 *
 * @author  Michiel Meeuwissen
 * @version $Id: Submitter.java 41575 2010-03-23 17:40:50Z michiel $
 */
public class SRNodeHandler extends IntegerHandler {
    private static final Logger LOG = Logging.getLoggerInstance(SRNodeHandler.class);


    private int enumHandlerLimit = 30;

    public void setEnumHandlerLimit(int e) {
        enumHandlerLimit = e;
    }

    @Override
    protected void appendClasses(Request request, StringBuilder buf, Node node, Field field, boolean search) {
        buf.append("mm_sr_searcher ");
        super.appendClasses(request, buf, node, field, search);
    }

    Query getQuery(Cloud cloud, Node node, Field field) {
        try {
            LocalizedEntryListFactory factory = field.getDataType().getEnumerationFactory();
            List<Query> queries = factory.getQueries(cloud, node, field);
            if (queries.isEmpty()) {
                // a node field, but no query given. So any node is possible?
                return cloud.getNodeManager("object").createQuery();
            }
            if (queries.size() > 1) {
                throw new IllegalStateException("More than one query found (" + queries.size() + ") This is not supported");
            }
            return queries.get(0);
        } catch(SearchQueryException sqe) {
            throw new RuntimeException(sqe.getMessage(), sqe);
        }

    }

    @Override
    public String input(Request request, Node node, Field field, boolean search)  {
        if (field.getDataType().getEnumerationFactory().size() < enumHandlerLimit) { // a drop down for small lists suffices
            return new EnumHandler().input(request, node, field, search);
        }
        if (search) {
            return super.input(request, node, field, search);
        } else {
            // Field to contain the actual information
            // Will be made hidden by the javascript.
            StringBuilder show =  new StringBuilder("<input type=\"text\" class=\" ");
            appendClasses(request, show, node, field, search);
            show.append("\" ");
            appendNameId(show, request, field);
            Object value = getFieldValue(request, node.isNew() ? null : node, field, node.isNew());
            show.append("value=\"");
            show.append((value == null ? "" : Casting.toString(value)));
            show.append("\" />");

            Writer w = new StringBuilderWriter(show);
            Jsp jsp = new Jsp(request);
            Map<String, Object> arguments = new HashMap<String, Object>();
            arguments.put("node", node);
            arguments.put("field", field);
            arguments.put("query", getQuery(request.getCloud(), node, field));
            arguments.put("id", id(request, field));
            try {
                jsp.render("/mmbase/searchrelate/handlers/nodeinput.jspx", arguments, w);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe.getMessage(), ioe);
            } catch (ServletException se) {
                throw new RuntimeException(se.getMessage(), se);
            }
            return w.toString();

        }
    }
}

