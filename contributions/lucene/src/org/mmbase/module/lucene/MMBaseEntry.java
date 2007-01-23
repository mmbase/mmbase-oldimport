/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.module.lucene;

import java.util.*;
import java.text.*;
import java.io.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import org.mmbase.module.lucene.extraction.*;


import org.mmbase.bridge.*;
import org.mmbase.storage.search.*;
import org.mmbase.bridge.util.xml.query.*;

import org.mmbase.util.logging.*;

/**
 * This defines how MMBase Nodes are added to Lucene documents. This also takes into account
 * 'related' nodes, by using the 'sub definitions'.
 *
 * @author Pierre van Rooden
 * @author Michiel Meeuwissen
 * @version $Id: MMBaseEntry.java,v 1.15 2007-01-23 21:34:07 michiel Exp $
 **/
public class MMBaseEntry implements IndexEntry {
    static private final Logger log = Logging.getLoggerInstance(MMBaseEntry.class);

    // format for dates to index
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private final Collection<IndexFieldDefinition> fields;
    private final Node node;
    private final boolean multiLevel; // it this not the same as node instanceof VirtualNode?
    private final NodeManager elementManager;
    private final Step elementStep;

    private final Collection<IndexDefinition> subQueries;

    // set with numbers of nodes indexed so far - used to prevent the indexing
    // of fields already indexed
    private final Set<String> indexed = new HashSet<String>();

    private final float boost = 1.0f;

    MMBaseEntry(Node node, Collection<IndexFieldDefinition> fields, boolean multiLevel,
                NodeManager elementManager, Step elementStep,
                Collection<IndexDefinition> subQueries) {
        this.fields = fields;
        this.multiLevel = multiLevel;
        this.elementManager = elementManager;
        this.elementStep = elementStep;
        this.subQueries = subQueries;
        this.node = node;
    }

    public String getIdentifier() {
        if (multiLevel) {
            String alias = elementStep.getAlias();
            if (alias == null) alias = elementStep.getTableName();
            return "" + node.getIntValue(alias);
        } else {
            return "" + node.getNumber();
        }
    }

    // For MMBase indexing the 'key' for sub-queries is always equal to the identifier of the current node ('related nodes')
    public String getKey() {
        return getIdentifier();
    }

    protected void addStandardKeys(Document document) {
        // always add the 'element' number first, because that ensures that document.get("number") returns 'the' node
        String id = getIdentifier();
        document.add(new Field("number",   id,  Field.Store.YES, Field.Index.UN_TOKENIZED)); 
        if (multiLevel) {
            document.add(new Field("builder", elementManager.getName(),    Field.Store.YES, Field.Index.UN_TOKENIZED)); // keyword
            for (org.mmbase.bridge.Field field : node.getNodeManager().getFields()) {
                if (field.getName().indexOf(".") >=0 ) continue;
                if (id.equals(field.getName())) continue; // was added already
                Node subNode = node.getNodeValue(field.getName());
                document.add(new Field("number",  "" + subNode.getNumber(), Field.Store.YES, Field.Index.UN_TOKENIZED)); // keyword
                document.add(new Field("owner",  subNode.getStringValue("owner"), Field.Store.YES, Field.Index.UN_TOKENIZED));
            }
        } else {
            document.add(new Field("builder",  node.getNodeManager().getName(),    Field.Store.YES, Field.Index.UN_TOKENIZED)); // keyword
            document.add(new Field("owner",  node.getStringValue("owner"), Field.Store.YES, Field.Index.UN_TOKENIZED));
        }

    }


    public void index(Document document) {
        Map<String, Object> data = new HashMap<String, Object>();
        if (log.isTraceEnabled()) {
            log.trace("Indexing " + getIdentifier() + "(" + node.getNodeManager().getName() + ")");
        }
        storeData(data);
        addStandardKeys(document);
        for (IndexFieldDefinition fieldDefinition : fields) {
            String fieldName = fieldDefinition.alias;
            if (fieldName == null)  fieldName = fieldDefinition.fieldName;
            if (document.getField(fieldName) == null || !fieldDefinition.keyWord) {
                String value = getFieldDataAsString(data, fieldName);
                if (fieldDefinition.keyWord) {
                    if (log.isDebugEnabled()) {
                        log.debug("add " + fieldName + " text, keyword" + value);
                    }
                    Indexer.addField(document, new Field(fieldName, value, Field.Store.YES, Field.Index.UN_TOKENIZED), fieldDefinition.multiple);
                } else if (fieldDefinition.storeText) {
                    if (log.isDebugEnabled()) {
                        log.trace("add " + fieldName + " text, store");
                    }
                    Field field = new Field(fieldName, value, Field.Store.YES, Field.Index.TOKENIZED);
                    field.setBoost(fieldDefinition.boost);
                    Indexer.addField(document, field, fieldDefinition.multiple);
                } else {
                    if (log.isDebugEnabled()) {
                        log.trace("add " + fieldName + " text, no store");
                    }
                    Field field = new Field(fieldName, value, Field.Store.NO, Field.Index.TOKENIZED);
                    field.setBoost(fieldDefinition.boost);
                    Indexer.addField(document, field, fieldDefinition.multiple);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.trace("Indexed " + data + " --> " + document);
        }
    }

    public Collection<IndexDefinition> getSubDefinitions() {
        return subQueries != null ? subQueries : Collections.EMPTY_LIST;
    }

    protected Node getNode(IndexFieldDefinition fd) {
        String fieldName = fd.fieldName;
        // determine number
        Node n = node;
        int pos = fieldName.indexOf(".");
        if (pos != -1) {
            n = node.getNodeValue(fieldName.substring(0, pos));
        }
        return n;
    }
    
    protected String getRealField(IndexFieldDefinition fd) {
        String fieldName = fd.fieldName;
        String realFieldName = fieldName;
        int pos = fieldName.indexOf(".");
        if (pos != -1) {
            realFieldName = fieldName.substring(pos + 1);
        }
        return realFieldName;
    }

    protected boolean shouldIndex(IndexFieldDefinition fd) {
        String fieldName = fd.fieldName;
        String alias     = fd.alias;
        String realFieldName = getRealField(fd);
        Node   n = getNode(fd);

        int number = (n == null) ? -1 : n.getNumber();
        if (! isIndexed(number, fieldName, alias)) {
            if (fd.optional != null && (! fd.optional.matcher(n.getNodeManager().getName()).matches() || ! n.getNodeManager().hasField(realFieldName))) {
                log.debug("Skipped optional field " + fieldName + " because node " + n.getNumber() + " does not have it");
                return false;
            } else {
                addToIndexed(number, fieldName, alias);
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Store data from field in a node into the cursor
     * @param map The map of fieldName/value mappings
     */
    protected void storeData(Map<String, Object> map) {
        Cloud cloud = elementManager.getCloud();
        for (IndexFieldDefinition fieldDefinition : fields) {
            String fieldName = fieldDefinition.fieldName;
            String alias = fieldDefinition.alias;
            if (alias == null)  alias = fieldDefinition.fieldName;
            String decryptionPassword = fieldDefinition.decryptionPassword;
            if (shouldIndex(fieldDefinition)) {
                // some hackery
                int type = org.mmbase.bridge.Field.TYPE_UNKNOWN;
                Node n = node;
                if (fieldDefinition.stepField != null) {
                    org.mmbase.storage.search.StepField sf = fieldDefinition.stepField;
                    org.mmbase.bridge.Field field = cloud.getNodeManager(sf.getStep().getTableName()).getField(sf.getFieldName());
                    type = field.getDataType().getBaseType();
                    // stepField.getType will not do, because this is the actual database type (when multilevel)
                    // changed especially because of datetimes...
                } else if (fieldDefinition.optional != null) {
                    log.debug("found optional field " + fieldName + " in node " + n.getNumber());
                    n = getNode(fieldDefinition);
                    fieldName = getRealField(fieldDefinition);
                    type = n.getNodeManager().getField(fieldName).getDataType().getBaseType();
                }
                String documentText = null;
                switch (type) {
                    case org.mmbase.bridge.Field.TYPE_DATETIME : {
                        try {
                            documentText = DATE_FORMAT.format(n.getDateValue(fieldName));
                        } catch (Exception e) {
                            // can't index dates prior to 1970, pretty dumb if you ask me
                        }
                        break;
                    }
                    case org.mmbase.bridge.Field.TYPE_BOOLEAN : {
                        if (log.isDebugEnabled()) {
                            log.trace("add " + alias + " keyword:" + n.getIntValue(fieldName));
                        }
                        documentText = "" + n.getIntValue(fieldName);
                        break;
                    }
                    case org.mmbase.bridge.Field.TYPE_NODE :
                    case org.mmbase.bridge.Field.TYPE_INTEGER :
                    case org.mmbase.bridge.Field.TYPE_LONG :
                    case org.mmbase.bridge.Field.TYPE_DOUBLE :
                    case org.mmbase.bridge.Field.TYPE_FLOAT : {
                        documentText =  node.getStringValue(fieldName);
                        break;
                    }
                    case org.mmbase.bridge.Field.TYPE_UNKNOWN : // unknown field may be binary
                    case org.mmbase.bridge.Field.TYPE_BINARY : {
                        String mimeType = "unknown";
                        mimeType = "" + n.getFunctionValue("mimetype", null);
                        Extractor extractor = ContentExtractor.getInstance().findExtractor(mimeType);
                        if (extractor != null) {
                            byte[] help = n.getByteValue(fieldName);
                            log.service("Analyzing document of " + n.getNumber() + " with " + extractor + " " + n.getByteValue(fieldName).length);

                            InputStream input = new ByteArrayInputStream(help);
                            try {
                                documentText = extractor.extract(input);
                            } catch (Exception e) {
                                if (log.isDebugEnabled()) {
                                    log.error(e.getMessage(), e);
                                } else {
                                    log.error(e.getMessage(), e);
                                }
                            }
                        } else  {
                            log.warn("Cannot read document: unknown mimetype of node " + n.getNumber() + ", trying stringvalue");
                            documentText = n.getStringValue(fieldName);
                        }
                        break;
                    }
                    default: {
                        if (log.isDebugEnabled()) {
                            log.trace("index " + alias + " as text");
                        }
                        documentText = n.getStringValue(fieldName);
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug("Storing  " + documentText);
                }
                if (documentText != null) {
                    if (fieldDefinition.keyWord) {
                        storeFieldData(map, alias, documentText);
                    } else {
                        storeFieldTextData(map, alias, documentText);
                    }
                }
            }
        }
    }

    /**
     * Store textual data for a field to index.
     * The data is merged with any text already stored for indexing if appropriate.
     * @param fieldname the name of the field used for indexing (the 'as' name of a field where appropriate)
     * @param value the textual value to index
     * @param data The map of fieldName/value mappings
     */
    void storeFieldTextData(Map<String, Object> data, String fieldName, String value) {
        StringBuffer sb = null;
        try {
            sb = (StringBuffer) data.get(fieldName);
        } catch (ClassCastException cce) {
            log.warn("Tried to store data of '" + fieldName + "' as a standard index, but data was already stored as a special index");
        }
        if (sb == null) {
            sb = new StringBuffer();
        } else {
            sb.append(" ");
        }
        sb.append(value);
        data.put(fieldName, sb);
    }

    /**
     * Store data for a field to index.
     * Data is only stored if it doesn't exist yet for this field.
     * @param fieldname the name of the field used for indexing (the 'as' name of a field where appropriate)
     * @param value the value to index
     */
    void storeFieldData(Map<String, Object> data, String fieldName, Object value) {
        Object o = data.get(fieldName);
        if (o == null)  {
            data.put(fieldName, value);
        }
    }



    /**
     * Return the data of a field as a string.
     * @param fieldname the name of the field used for indexing (the 'as' name of a field where appropriate)
     */
    String getFieldDataAsString(Map<String, ?> data, String fieldName) {
        Object o = data.get(fieldName);
        if (o != null)  {
            return o.toString();
        } else {
            return "";
        }
    }

    /**
     * Add a name of a node with the specified number as having been indexed (so it won't be attempted to index it again)
     * @param number the number of the node
     * @param fieldName the name of the field
     * @param alias the alias under which the field is indexed
     */
    void addToIndexed(int number, String fieldName, String alias) {
        indexed.add(number + "_" + fieldName + "_" + alias);
    }

    /**
     * Returns <code>true</code> if a field of a node indicated by the number has already been indexed.
     * @param number the number of the node
     * @param fieldName the name of the field
     * @param alias the alias under which the field is indexed
     */
    boolean isIndexed(int number, String fieldName, String alias) {
        return indexed.contains(number + "_" + fieldName + "_" + alias);
    }

    public String toString() {
        return node.getNumber() + " " + subQueries;
    }

}


