/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.util.xml.query;

import java.util.Collection;
import org.w3c.dom.*;

import org.mmbase.bridge.*;
import org.mmbase.storage.search.*;

/**
 * Defines a query and possible options for the fields to index.
 *
 * @author Pierre van Rooden
 * @version $Id: QueryDefinition.java,v 1.4 2005-11-01 18:01:44 michiel Exp $
 * @since MMBase-1.8
 **/
public class QueryDefinition {

    /**
     * The query to run
     */
    public Query query = null;

    /**
     * If <code>true</code>, the query in this definition returns cluster nodes
     * XXX: how is this different from query instanceof NodeQuery
     */
    public boolean isMultiLevel = false;

    /**
     * A collection of FieldDefinition objects, containing properties for the fields to index.
     * XXX: Is 'index' not specific for Lucene?
     */
    public Collection fields = null;

    /**
     * The NodeManager of the 'main' nodetype in this query (if appropriate).
     * XXX: How is this different from NodeQuery#getNodeManager() ?
     */
    public NodeManager elementManager = null;

    /**
     * The step in the query describing the 'main' nodetype (if appropriate).
     * XXX: How is this different from NodeQuery#getNodeStep() ?
     */
    public Step elementStep = null;

    /**
     * The Query configurer that instantiated this definition
     * XXX: unused (logically, because it is instantatiated by it)
     */
    protected QueryConfigurer configurer = null;
    
    /**
     * Constructor.
     */
    public QueryDefinition(QueryConfigurer configurer) {
        this.configurer = configurer;
    }

    /**
     * Constructor, copies all data from the specified QueryDefinition object.
     */
    public QueryDefinition(QueryDefinition queryDefinition) {
        this.configurer = queryDefinition.configurer;
        this.query = queryDefinition.query;
        this.fields = queryDefinition.fields;
        this.isMultiLevel = queryDefinition.isMultiLevel;
        this.elementManager = queryDefinition.elementManager;
        this. elementStep = queryDefinition.elementStep;
    }

    /**
     * Configures the query definition, using data from a DOM element
     */
    public void configure(Element queryElement) {
    }

}

