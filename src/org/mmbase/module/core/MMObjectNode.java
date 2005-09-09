/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.module.core;

import java.util.*;
import java.io.*;

import org.mmbase.cache.*;
import org.mmbase.bridge.Field;
import org.mmbase.module.corebuilders.InsRel;
import org.mmbase.security.*;
import org.mmbase.storage.search.*;
import org.mmbase.util.Casting;
import org.mmbase.util.logging.*;
import org.mmbase.util.functions.*;
import org.w3c.dom.Document;

/**
 * MMObjectNode is the core of the MMBase system.
 * This class is what its all about, because the instances of this class hold the content we are using.
 * All active Nodes with data and relations are MMObjectNodes and make up the
 * object world that is MMBase (Creating, searching, removing is done by the node's parent,
 * which is a class extended from MMObjectBuilder)
 *
 * @author Daniel Ockeloen
 * @author Pierre van Rooden
 * @author Eduard Witteveen
 * @author Michiel Meeuwissen
 * @author Ernst Bunders
 * @version $Id: MMObjectNode.java,v 1.151 2005-09-09 20:13:57 michiel Exp $
 */

public class MMObjectNode implements org.mmbase.util.SizeMeasurable {
    private static final Logger log = Logging.getLoggerInstance(MMObjectNode.class);

    /**
     * You cannot store real 'null's in a hashtable, so this constant can be used for this.
     * @since MMBase-1.7
     * @todo The _type_ of such a 'null' value cannot be determined. Do we need a NULL constant for every type in stead?
     */
    public final static Object VALUE_NULL = new Object() {
            public String toString() { return "[FIELD VALUE NULL]"; }
        };

    /**
     * Map which stores the current database value for fields when
     * then change in the node. 
     * it can be used to optimise cacheing  
     */
    public Map oldValues = new HashMap(20);

    /**
     * Large fields (blobs) are loaded 'lazily', so only on explicit request. Until the first exlicit request this value is stored in such fields.
     * It can be set back into the field with {@link #storeValue}, to unload the field again.
     * @since MMBase-1.7.4
     */
    public final static String VALUE_SHORTED = "$SHORTED";

    /**
     * @deprecated use RelationsCache.getCache().getHits()
     */
    public static int getRelationCacheHits() {
        return relationsCache.getHits();
    }

    /**
     * @deprecated use RelationsCache.getCache().getMisses()
     */
    public static int getRelationCacheMiss() {
        return relationsCache.getMisses();
    }

    /**
     * Results of getRelatedNodes
     * @since 1.7
     */
    protected static final RelatedNodesCache relatedCache = RelatedNodesCache.getCache();


    /**
     * objectNumber -> List of all relation nodes
     * @since MMBase-1.7
     */
    protected static final RelationsCache relationsCache = RelationsCache.getCache();
    // < MMBase-1.7, every mmobjectnode instance had a cache for relation nodes
    // private Vector relations=null; // possibly filled with insRels


    /**
     * Holds the name - value pairs of this node (the node's fields).
     * Most nodes will have a 'number' and an 'otype' field, and fields which will differ by builder.
     * This collection should not be directly queried or changed -
     * use the SetValue and getXXXValue methods instead.
     * @todo As suggested by keesj, should be changed to HashMap, which will allow for <code>null</code> values.
     * It should then be made private, and methods that change the map (storeValue) be made synchronized.
     * Note: To avoid synchronisation conflicts, we can't really change the type until the property is made private.
     * @scope private
     */
    public Hashtable values = new Hashtable();
    // private Map values = Collections.synchronizedMap(new HashMap());

    private Map sizes = Collections.synchronizedMap(new HashMap());


    /**
     * Determines whether the node is being initialized (typically when it is loaded from the database).
     * Use {@link #start} to start initializing, use {@link #finish} to end.
     * @since MMBase-1.7
     */
    protected boolean initializing = false;

    /**
     * Holds the 'extra' name-value pairs (the node's properties)
     * which are retrieved from the 'properties' table.
     * @scope private
     */
    public Hashtable properties;

    /**
     * Vector which stores the keys of the fields that were changed
     * since the last commit.
     * @scope private, and should be a Set, not a Vector
     */
    public Vector changed = new Vector();

    /**
     * Pointer to the parent builder that is responsible for this node.
     * Note: this may on occasion (due to optimization) duffer for the node's original builder.
     * Use {@link #getBuilder} instead.
     * @scope private
     */
    public MMObjectBuilder parent;

    /**
     * Pointer to the actual builder to which this node belongs.
     * This value is initialised through the first call to {@link #getBuilder}
     */
    private MMObjectBuilder builder = null;



    /**
     * Determines whether this node is virtual.
     * A virtual node is not persistent (that is, not stored in a table).
     * @scope private
     */
    protected boolean virtual = false;

    /**
     * Alias name of this node.
     * XXX: nodes can have multiple aliases.
     * @scope private
     */
    protected String alias;

    // object to sync access to properties
    private final Object properties_sync = new Object();

    /**
     * temporarily holds a new context for a node
     * @since MMBase-1.7
     */

    private String newContext = null;

    /**
     * Main constructor.
     * @param parent the node's parent, an instance of the node's builder.
     */
    public MMObjectNode(MMObjectBuilder parent) {
        if (parent != null) {
            this.parent = parent;
        } else {
            log.error("MMObjectNode-> contructor called with parent=null");
            throw new NullPointerException("contructor called with parent=null");
        }
    }

    /**
     * Returns the actual builder of the node.
     * Note that it is possible that, due to optimization, a node is currently associated with
     * another (parent) builder, i.e. a posrel node may be associated with a insrel builder.
     * This method returns the actual builder.
     * The node may miss vital information (not retrieved from the database) to act as a node of such
     * a builder - if you need actual status you need to reload it.
     * @since MMBase-1.6
     * @return the builder of this node
     */
    public MMObjectBuilder getBuilder() {
        if (builder == null) {
            int oType = getOType();
            if (oType == -1 || parent.oType == oType) {
                builder = parent;
            } else {
                String builderName = parent.mmb.getTypeDef().getValue(oType);
                if (builderName != null) { // avoid NPE from mmb.getBuilder.
                    builder = parent.mmb.getBuilder(builderName);
                }
            }
            if (builder == null) {
                log.warn("Builder of node " + getNumber() + " not found, taking 'object'");
                builder = parent.mmb.getBuilder("object");
            }
        }
        return builder;
    }

    /**
     * Start the loading of a node
     * @since MMBase-1.7
     */
    public void start() {
        initializing = true;
    }

    /**
     * Finish the loading of a node
     * @since MMBase-1.7
     */
    public void finish() {
        initializing = false;
    }

    /**
     * Tests whether the data in a node is valid (throws an exception if this is not the case).
     * @throws org.mmbase.module.core.InvalidDataException
     *   If the data was unrecoverably invalid (the references did not point to existing objects)
     */
    public void testValidData() throws InvalidDataException {
        parent.testValidData(this);
    };

    /**
     * Commit the node to the database or other storage system.
     * This can only be done on a existing (inserted) node. It will use the
     * changed Vector as its base of what to commit/change.
     * @return <code>true</code> if the commit was succesfull, <code>false</code> is it failed
     */
    public boolean commit() {
        return parent.commit(this);
    }

    /**
     * Insert this node into the storage
     * @param username the name of the user who inserts the node. This value is ignored
     * @return the new node key (number field), or -1 if the insert failed
     */
    public int insert(String userName) {
        return parent.insert(userName, this);
    }

    /**
     * Insert this node into the database or other storage system.
     * @param user the user who inserts the node.
     *        Used to set security-related information
     * @return the new node key (number field), or -1 if the insert failed
     * @since MMBase-1.7
     */
    public int insert(UserContext user) {
        int nodeID = parent.safeInsert(this, user.getIdentifier());
        if (nodeID != -1) {
            MMBaseCop mmbaseCop = parent.getMMBase().getMMBaseCop();
            mmbaseCop.getAuthorization().create(user, nodeID);
            if (newContext != null) {
                mmbaseCop.getAuthorization().setContext(user, nodeID, newContext);
                newContext = null;
            }
        }
        return nodeID;
    }

    /**
     * Commit this node to the storage
     * @param user the user who commits the node.
     *        Used to set security-related information
     * @return <code>true</code> if succesful
     * @since MMBase-1.7
     */
    public boolean commit(UserContext user) {
        boolean success = parent.safeCommit(this);
        if (success) {
            MMBaseCop mmbaseCop = parent.getMMBase().getMMBaseCop();
            mmbaseCop.getAuthorization().update(user, getNumber());
            if (newContext != null) {
                mmbaseCop.getAuthorization().setContext(user,getNumber(), newContext);
                newContext = null;
            }
        }
        return success;
    }

    /**
     * Remove this node from the storage
     * @param user the user who removes the node.
     *        Used to set security-related information
     * @since MMBase-1.7
     */
    public void remove(UserContext user) {
        parent.removeNode(this);
        parent.getMMBase().getMMBaseCop().getAuthorization().remove(user, getNumber());
    }

    /**
     * Sets the security context for this node
     * @param user the user who changes the context of the node.
     * @param context the new context
     * @param now if <code>true</code>, the context is changed instantly, otherwise it is changed
     *        after the node is send to storage.
     * @since MMBase-1.7
     */
    public void setContext(UserContext user, String context, boolean now) {
       if (now) {
           parent.getMMBase().getMMBaseCop().getAuthorization().setContext(user, getNumber(), context);
       } else {
           newContext = context;
       }
    }

    /**
     * Returns the security context for this node
     * @param user the user who requests the context of the node.
     * @since MMBase-1.7.1
     */
    public String getContext(UserContext user) {
        if (newContext != null) return newContext;
        if (getNumber() < 0) return user.getOwnerField();
        return parent.getMMBase().getMMBaseCop().getAuthorization().getContext(user, getNumber());
    }

    /**
     * Returns the possible new security contexts for this node
     * @param user the user who requests the context of the node.
     * @since MMBase-1.7.1
     */
    public Set getPossibleContexts(UserContext user) {
        if (getNumber() < 0) {
            // a new node has yet no context (except the default).
            // instead of searching the database for data, return a
            // standard set of values existing of the current context
            // and the contexts "system" and "admin".
            // A better way involves rewriting the security layer to accept
            // MMObjectNodes instead of node numbers
            Set contexts = new HashSet();
            contexts.add(getContext(user));
            contexts.add("admin");
            contexts.add("system");
            return contexts;
/*
            NodeSearchQuery query = new NodeSearchQuery(parent);
            CoreField fieldDefs = parent.getField("owner");
            StepField field = query.getField(fieldDefs);
            BasicFieldValueConstraint cons = new BasicFieldValueConstraint(field, getContext(user));
            query.setMaxNumber(1);
            try {
                Iterator resultList = parent.getNodes(query).iterator();
                if (resultList.hasNext()) {
                    return ((MMObjectNode) resultList.next()).getPossibleContexts(user);
                }
            } catch (SearchQueryException sqe) {
                log.error(sqe.toString());
            }
            return new HashSet();
*/
        }
        return parent.getMMBase().getMMBaseCop().getAuthorization().getPossibleContexts(user, getNumber());
    }

    /**
     * Returns the core of this node in a string.
     * Used for debugging.
     * For data exchange use toXML() and getDTD().
     * @return the contents of the node as a string.
     */
    public String toString() {
        if (parent != null) {
            return parent.toString(this);
        } else {
            return defaultToString();
        }
    }

    /**
     * @since MMBase-1.6.2
     */
    String defaultToString() {
        StringBuffer result = new StringBuffer();
        try {
            Set entrySet = values.entrySet();
            synchronized(values) {
                Iterator i = entrySet.iterator();
                while (i.hasNext()) {
                    Map.Entry entry = (Map.Entry) i.next();
                    String key = (String) entry.getKey();
                    int dbtype = getDBType(key);
                    String value = "" + entry.getValue();  // XXX:should be retrieveValue ?
                    if ("".equals(result.toString())) {
                        result = new StringBuffer(key+"="+dbtype+":'"+value+"'"); // can this occur?
                    } else {
                        result.append(","+key+"="+dbtype+":'");
                        Casting.toStringBuffer(result, value).append("'");
                    }
                }
            }
        } catch(Throwable e) {
            result.append("" + values); // simpler version...
        }
        result.append(super.toString());
        return result.toString();
    }

    /**
     * Return the node as a string in XML format.
     * Used for data exchange, though, oddly enough, not by application export. (?)
     * @return the contents of the node as a xml-formatted string.
     */
    public String toXML() {
        // call is implemented by its builder so
        // call the builder with this node
        if (parent!=null) {
            return parent.toXML(this);
        } else {
            return null;
        }
    }

    /**
     * Stores a value in the values hashtable.
     * This is a low-level method that circumvents typechecking and the triggers of extended classes.
     * You should normally call {@link #setValue} to change fields.
     * @todo This should become a synchronized method, once values becomes a private HashMap instead of a
     * public Hashtable.
     *
     *@param fieldName the name of the field to change
     *@param fieldValue the value to assign
     */
    public void storeValue(String fieldName, Object fieldValue) {
        if (fieldValue == null) {
            values.remove(fieldName);
        } else {
            values.put(fieldName, fieldValue);
        }
    }

    /**
     * Retrieves a value from the values hashtable.
     * This is a low-level method that circumvents typechecking and the triggers of extended classes.
     * You should normally call {@link #getValue} to load fields.
     *
     * @param fieldName the name of the field to change
     * @return the value of the field
     */
    public Object retrieveValue(String fieldName) {
        return values.get(fieldName);
    }

    /**
     * Determines whether the node is virtual.
     * A virtual node is not persistent (that is, stored in a database table).
     */
    public boolean isVirtual() {
        return virtual;
    }

    /*
     *
     * @since MMBase-1.6
     */

    protected Document toXML(Object value, String fieldName) {
        Document doc = Casting.toXML(value);
        if (doc == null && parent.getField(fieldName).isRequired()) {
            doc = Casting.toXML("<p/>");
        }
        return doc;
    }

    /**
     *  Sets a key/value pair in the main values of this node.
     *  Note that if this node is a node in cache, the changes are immediately visible to
     *  everyone, even if the changes are not committed.
     *  The fieldName is added to the (public) 'changed' vector to track changes.
     *  @param fieldName the name of the field to change
     *  @param fieldValue the value to assign
     *  @return <code>true</code> When the field was changed, false otherwise.
     */
    public boolean setValue(String fieldName, Object fieldValue) {
        // check the value also when the parent thing is null
        Object originalValue = values.get(fieldName);
        if (fieldValue == null) fieldValue = VALUE_NULL;

        if (fieldValue != VALUE_SHORTED) {
            // make sure this value remains not in the blob-cache.
            BlobCache blobs = parent.getBlobCache(fieldName);
            blobs.remove(blobs.getKey(getNumber(), fieldName));
        }

        if (log.isDebugEnabled()) {
            String string;
            if (fieldValue instanceof byte[]) {
                string = "byte array of size " + ((byte[])fieldValue).length;
            } else {
                string = Casting.toString(fieldValue);
                if (string.length()>200) string = string.substring(0, 200);
            }
            log.debug("Setting " + fieldName + " to " +  string);
        }
        
        //store the old value
        storeOldValue(fieldName, values.get(fieldName));
        
        // put the key/value in the value hashtable
        storeValue(fieldName, fieldValue);

        // process the changed value (?)
        if (parent != null) {
            if(!parent.setValue(this, fieldName, originalValue)) {
                // setValue of parent returned false, no update needed...
                return false;
            }
        } else {
            log.error("parent was null for node with number" + getNumber());
        }
        setUpdate(fieldName);
        return true;
    }
    
    /**
     * this method stores a fieldvalue only once. the purpose is to
     * store the value only the first time a field changes, so it reflects
     * the value in the database.
	 * @param fieldName
	 * @param object
	 */
	private void storeOldValue(String fieldName, Object object) {
		 if(oldValues.get(fieldName) == null)
		 	oldValues.put(fieldName, object);
	}

    /**
     * Sets the size (in byte) of the given field. This is meant for byte-array fields, which you
     * fill using an InputStream.
     * @see #getSize(String)
     * @since MMBase-1.8
     */
    public void setSize(String fieldName, long size) {
        sizes.put(fieldName, new Long(size));
    }
    /**
     * Returns the size (in byte) of the given field. This is mainly targeted at fields of the type
     * byte array. For other fields this method will return something reasonable, but it is as yet
     * not well defined what...
     *
     * @since MMBase-1.8
     */
    public long getSize(String fieldName) {
        Long l = (Long) sizes.get(fieldName);
        if (l != null)  return l.intValue();
        Object value = values.get(fieldName);
        if (value == null) return -1;
        return Casting.toString(value).getBytes().length;
    }

    /**
     * Sets a key/value pair in the main values of this node. The value to set is of type <code>boolean</code>.
     * Note that if this node is a node in cache, the changes are immediately visible to
     * everyone, even if the changes are not committed.
     * The fieldName is added to the (public) 'changed' vector to track changes.
     * @param fieldName the name of the field to change
     * @param fieldValue the value to assign
     * @return always <code>true</code>
     */
    public boolean setValue(String fieldName, boolean fieldValue) {
        return setValue(fieldName, Boolean.valueOf(fieldValue));
    }

    /**
     *  Sets a key/value pair in the main values of this node. The value to set is of type <code>int</code>.
     *  Note that if this node is a node in cache, the changes are immediately visible to
     *  everyone, even if the changes are not committed.
     *  The fieldName is added to the (public) 'changed' vector to track changes.
     *  @param fieldName the name of the field to change
     *  @param fieldValue the value to assign
     *  @return always <code>true</code>
     */
    public boolean setValue(String fieldName, int fieldValue) {
        return setValue(fieldName, new Integer(fieldValue));
    }

    /**
     *  Sets a key/value pair in the main values of this node. The value to set is of type <code>int</code>.
     *  Note that if this node is a node in cache, the changes are immediately visible to
     *  everyone, even if the changes are not committed.
     *  The fieldName is added to the (public) 'changed' vector to track changes.
     *  @param fieldName the name of the field to change
     *  @param fieldValue the value to assign
     *  @return always <code>true</code>
     */
    public boolean setValue(String fieldName, long fieldValue) {
        return setValue(fieldName, new Long(fieldValue));
    }

    /**
     *  Sets a key/value pair in the main values of this node. The value to set is of type <code>double</code>.
     *  Note that if this node is a node in cache, the changes are immediately visible to
     *  everyone, even if the changes are not committed.
     *  The fieldName is added to the (public) 'changed' vector to track changes.
     *  @param fieldName the name of the field to change
     *  @param fieldValue the value to assign
     *  @return always <code>true</code>
     */
    public boolean setValue(String fieldName, double fieldValue) {
        return setValue(fieldName, new Double(fieldValue));
    }


    // Add the field to update to the changed Vector
    //
    private void setUpdate(String fieldName) {
        // obtain the type of field this is
        int state = getDBState(fieldName);

        // add it to the changed vector so we know that we have to update it
        // on the next commit
        if (! initializing && !changed.contains(fieldName) && state == Field.STATE_PERSISTENT) {
            changed.add(fieldName);
        }
        // is it a memory only field ? then send a fieldchange
        if (state == 0) sendFieldChangeSignal(fieldName);
    }

    /**
     * Retrieve an object's number.
     * In case of a new node that is not committed, this will return -1.
     * @return the number of the node
     */
    public int getNumber() {
        return Casting.toInt(values.get("number"));
    }

    /**
     * Retrieve an object's object type.
     * This is a number (an index in the typedef builer), rather than a name.
     * @return the object type number of the node
     */
    public int getOType() {
        return getIntValue("otype");
    }


    /**
     * @since MMBase-1.8
     */
    public boolean isNull(String fieldName) {
        // get the value from the values table
        Object value = values.get(fieldName);
        return value == null || value.equals(VALUE_NULL);
    }

    /**
     * Get a value of a certain field.
     * @performance do not store byte values directly in node (?)
     * @param fieldName the name of the field who's data to return
     * @return the field's value as an <code>Object</code>
     */
    public Object getValue(String fieldName) {
        // get the value from the values table
        Object value = values.get(fieldName);

        // explicitly load byte values if they are 'shortened'
        if (VALUE_SHORTED.equals(value)) {   // could use == if we are sure that everybody uses the constant

            BlobCache blobs = parent.getBlobCache(fieldName);
            String key = blobs.getKey(getNumber(), fieldName);
            value = blobs.get(key);
            if (value == null) {
                int type = getDBType(fieldName);
                switch (type) {
                case Field.TYPE_BINARY:
                    value = parent.getShortedByte(fieldName, this);
                    break;
                case Field.TYPE_STRING:
                    value = parent.getShortedText(fieldName, this);
                    break;
                default:
                    throw new UnsupportedOperationException("Found shorted value for type " + type);
                }
                blobs.put(key, value);
            }
        }

        // if we have an XML-dbtype field, we always have to return a Document (or null).
        // note that if the value is null we store it as a null value
        if (parent != null && value != null && value != VALUE_NULL && !(value instanceof Document) &&
            getDBType(fieldName) == Field.TYPE_XML) {
            String string = Casting.toString(value).trim();
            Document doc = toXML(string, fieldName);
            if(doc != null) {
                // store the document inside the field.. much faster...
                value = doc;
                values.put(fieldName, value);
            } else {
                values.put(fieldName, VALUE_NULL);
            }
        }

        // routine to check for indirect values
        // this are used for functions for example
        // its implemented per builder so lets give this
        // request to our builder
        if (value == null) value = parent.getValue(this, fieldName);

        // return the found object
        return value;
    }


    /**
     * Get a value of a certain field.  The value is returned as a
     * String. Non-string values are automatically converted to
     * String. 'null' is converted to an empty string.
     * @param fieldName the name of the field who's data to return
     * @return the field's value as a <code>String</code>
     */
    public String getStringValue(String fieldName) {
        return Casting.toString(getValue(fieldName));
    }

    /**
     * @javadoc
     * @since MMBase-1.6
     */
    public Object getFunctionValue(String functionName, List parameters) {
        return parent.getFunctionValue(this, functionName, parameters);
    }

    /**
     * @javadoc
     * @since MMBase-1.8
     */
    public Parameters createParameters(String functionName) {
        return parent.createParameters(functionName);
    }

    /**
     * @javadoc
     * @since MMBase-1.8
     */
    public Function getFunction(String functionName) {
        return parent.getFunction(this, functionName);
    }

    /**
     * @javadoc
     * @since MMBase-1.8
     */
    public Collection getFunctions() {
        return parent.getFunctions(this);
    }

    /**
     * Returns the value of the specified field as a <code>dom.Document</code>
     * If the node value is not itself a Document, the method attempts to
     * attempts to convert the String value into an XML.
     * If the value cannot be converted, this method returns <code>null</code>
     *
     * @param fieldName  the name of the field to be returned
     * @return  the value of the specified field as a DOM Element or <code>null</code>
     * @throws  IllegalArgumentException if the value cannot be converted.
     * @since MMBase-1.6
     */
    public Document getXMLValue(String fieldName) {
        Document o =  toXML(getValue(fieldName), fieldName);
        if(o != null && getDBType(fieldName) == Field.TYPE_XML) {
            storeValue(fieldName, o);
        }
        return o;
    }


    /**
     * If the values map contains an InputStream, care must be taken because often an InputStream can be used only once.
     * @since MMBase-1.8
     */ 
    private byte[] useInputStream(String fieldName, InputStream stream) {
        // first, convert to byte-array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int c;
        try {
            while ((c = stream.read()) > -1) {
                bos.write(c);
            }
        } catch (IOException ioe) {
            log.error(ioe);
        }
        byte[] b = bos.toByteArray();
        // check if we can cache it.
        BlobCache blobs = parent.getBlobCache(fieldName);
        String key = blobs.getKey(getNumber(), fieldName);
        if (b.length < blobs.getMaxEntrySize()) {
            blobs.put(key, b);
        }
        values.put(fieldName, b);
        return b;
    }

    /**
     * Get a binary value of a certain field.
     * @performance do not store byte values directly in node (?)
     * @param fieldName the name of the field who's data to return
     * @return the field's value as an <code>byte []</code> (binary/blob field)
     */
    public byte[] getByteValue(String fieldName) {

        Object obj = getValue(fieldName);
        if (obj == null || obj == VALUE_NULL) {
            return new byte[0];
        } else if (obj instanceof byte[]) {
            // was already unmapped so return the value
            return (byte[]) obj;
        } else if (obj instanceof InputStream) {
            return useInputStream(fieldName, (InputStream) obj);
        } else {
            byte[] b;
            if (getDBType(fieldName) == Field.TYPE_STRING) {
                String s = getStringValue(fieldName);
                try {
                    b = s.getBytes(parent.getMMBase().getEncoding());
                } catch (UnsupportedEncodingException uee) {
                    log.error(uee.getMessage());
                    b = s.getBytes();
                }
            } else {
                b = new byte[0];
            }
            return b;
        }
    }


    public InputStream getInputStreamValue(String fieldName) {
        Object value = values.get(fieldName);
        if (value == null) {
            log.debug("NUL on " + fieldName + " " + this, new Exception());
            return new ByteArrayInputStream(new byte[0]);
        }
        if (VALUE_NULL.equals(value)) {
            return new ByteArrayInputStream(new byte[0]);
        }
        if (value instanceof InputStream) {
            // cannot return it directly, it would kill the inputstream, and perhaps it cannot be saved in db anymore then.
            // Sad, we have a buffer always now.
            // XXX think of something that the buffer is only needed if actually used a second time
            //     help-file, i think
            return new ByteArrayInputStream(useInputStream(fieldName, (InputStream) value));
        }

        if (VALUE_SHORTED.equals(value)) {
            BlobCache blobs = parent.getBlobCache(fieldName);
            String key = blobs.getKey(getNumber(), fieldName);
            byte[] v = (byte[]) blobs.get(key);
            if (v == null) {
                if (getSize(fieldName) < blobs.getMaxEntrySize()) {
                    v = parent.mmb.getStorageManager().getBinaryValue(this, parent.getField(fieldName));
                    if (log.isDebugEnabled()) {
                        log.debug("Putting in blob cache " + key);
                    }
                    blobs.put(key, v);
                } else {
                    log.debug("Too big for cache, requesting InputStream directly from storage");
                    return parent.mmb.getStorageManager().getInputStreamValue(this, parent.getField(fieldName));
                }
            } else {
                log.debug("Found in blob cache " + fieldName);
            }
            return new ByteArrayInputStream(v);
        } else {
            if (value instanceof byte[]) {
                return new ByteArrayInputStream((byte[]) value);
            } else {
                // probably not a byte-array field, do something.
                // this behavior is undefined!, don't depend on it.
                return new ByteArrayInputStream(("" + value).getBytes());
            }
        }
    }


    /**
     * Get a value of a certain field.
     * The value is returned as an MMObjectNode.
     * If the field contains an Numeric value, the method
     * tries to obtrain the object with that number.
     * If it is a String, the method tries to obtain the object with
     * that alias. The only other possible values are those created by
     * certain virtual fields.
     * All remaining situations return <code>null</code>.
     * @param fieldName the name of the field who's data to return
     * @return the field's value as an <code>int</code>
     */
    public MMObjectNode getNodeValue(String fieldName) {
        if (fieldName == null || fieldName.equals("number")) return this;
        return Casting.toNode(getValue(fieldName), parent);
    }

    /**
     * Get a value of a certain field.
     * The value is returned as an int value. Values of non-int, numeric fields are converted if possible.
     * Booelan fields return 0 for false, 1 for true.
     * String fields are parsed to a number, if possible.
     * If a value is an MMObjectNode, it's numberfield is returned.
     * All remaining field values return -1.
     * @param fieldName the name of the field who's data to return
     * @return the field's value as an <code>int</code>
     */
    public int getIntValue(String fieldName) {
        return Casting.toInt(getValue(fieldName));
    }

    /**
     * Get a value of a certain field.
     * The value is returned as an boolean value.
     * If the actual value is numeric, this call returns <code>true</code>
     * if the value is a positive, non-zero, value. In other words, values '0'
     * and '-1' are concidered <code>false</code>.
     * If the value is a string, this call returns <code>true</code> if
     * the value is "true" or "yes" (case-insensitive).
     * In all other cases (including calling byte fields), <code>false</code>
     * is returned.
     * Note that there is currently no basic MMBase boolean type, but some
     * <code>excecuteFunction</code> calls may return a Boolean result.
     *
     * @param fieldName the name of the field who's data to return
     * @return the field's value as an <code>int</code>
     */
    public boolean getBooleanValue(String fieldName) {
        return Casting.toBoolean(getValue(fieldName));
    }

    /**
     * Get a value of a certain field.
     * The value is returned as an Integer value. Values of non-Integer, numeric fields are converted if possible.
     * Boolean fields return 0 for false, 1 for true.
     * String fields are parsed to a number, if possible.
     * All remaining field values return -1.
     * @param fieldName the name of the field who's data to return
     * @return the field's value as an <code>Integer</code>
     */
    public Integer getIntegerValue(String fieldName) {
        return Casting.toInteger(getValue(fieldName));
    }

    /**
     * Get a value of a certain field.
     * @see #getValue
     * @see Casting#toLong
     * @param fieldName the name of the field who's data to return
     * @return the field's value as a <code>long</code>
     */
    public long getLongValue(String fieldName) {
        return Casting.toLong(getValue(fieldName));
    }


    /**
     * Get a value of a certain field.
     * The value is returned as a float value. Values of non-float, numeric fields are converted if possible.
     * Boolean fields return 0 for false, 1 for true.
     * String fields are parsed to a number, if possible.
     * All remaining field values return -1.
     * @param fieldName the name of the field who's data to return
     * @return the field's value as a <code>float</code>
     */
    public float getFloatValue(String fieldName) {
        return Casting.toFloat(getValue(fieldName));
    }


    /**
     * Get a value of a certain field.
     * The value is returned as a double value. Values of non-double, numeric fields are converted if possible.
     * Boolean fields return 0 for false, 1 for true.
     * String fields are parsed to a number, if possible.
     * All remaining field values return -1.
     * @param fieldName the name of the field who's data to return
     * @return the field's value as a <code>double</code>
     */
    public double getDoubleValue(String fieldName) {
        return Casting.toDouble(getValue(fieldName));
    }

    /**
     * Get a value of a certain field.
     * The value is returned as a Date value. Values of numeric fields are converted as if they were
     * time in seconds since 1/1/1970.
     * String fields are parsed to a date, if possible.
     * All remaining field values return -1.
     * @since MMBase-1.8
     * @param fieldName the name of the field who's data to return
     * @return the field's value as a <code>Date</code>
     */
    public Date getDateValue(String fieldName) {
        return Casting.toDate(getValue(fieldName));
    }

    /**
     * Get a value of a certain field.
     * The value is returned as a List value.
     * Strings are treated as comma-seperated value lists, and split into their component parts.
     * Values of other fields are returned as Lists of one object.
     * @since MMBase-1.8
     * @param fieldName the name of the field who's data to return
     * @return the field's value as a <code>List</code>
     */
    public List getListValue(String fieldName) {
        return Casting.toList(getValue(fieldName));
    }

    /**
     * Returns the DBType of a field.
     * @param fieldName the name of the field which' type to return
     * @return the field's DBType
     */
    public int getDBType(String fieldName) {
        return parent.getDBType(fieldName);
    }

    /**
     * Returns the DBState of a field.
     * @param fieldName the name of the field who's state to return
     * @return the field's DBState
     */
    public int getDBState(String fieldName) {
        if (parent!=null)    {
            return parent.getDBState(fieldName);
        } else {
            return Field.STATE_UNKNOWN;
        }
    }

    /**
     * Return the names of all persistent fields that were changed.
     * Note that this is a direct reference. Changes (i.e. clearing the vector) will affect the node's status.
     * @param a <code>Vector</code> containing all the fieldNames
     * @todo  Should it not return a (unmodifiable) Collection or a Set?
     * @todo  Should this function not be replaced by a more generic 'isChanged(String fieldName)'?
     */
    public Vector getChanged() {
        return changed;
    }

    /**
     * Tests whether one of the values of this node was changed since the last commit/insert.
     * @return <code>true</code> if changes have been made, <code>false</code> otherwise
     */
    public boolean isChanged() {
        return changed.size() > 0;
    }

    /**
     * Clear the 'signal' Vector with the changed keys since last commit/insert.
     * Marks the node as 'unchanged'.
     * Does not affect the values of the fields, nor does it commit the node.
     * @return always <code>true</code>
     */
    public boolean clearChanged() {
        changed.clear();
        oldValues.clear();
        return true;
    }


    /**
     * Deletes the propertie cache for this node.
     * Forces a reload of the properties on next use.
     */
    public void delPropertiesCache() {
        synchronized(properties_sync) {
            properties = null;
        }
    }

    public Hashtable getValues() {
        return values;
    }



    /**
     * Return a the properties for this node.
     * @return the properties as a <code>Hashtable</code>
     */
    public Hashtable getProperties() {
        synchronized(properties_sync) {
            if (properties == null) {
                properties = new Hashtable();
                MMObjectBuilder bul = parent.mmb.getMMObject("properties");
                Enumeration e = bul.search("parent=="+getNumber());
                while (e.hasMoreElements()) {
                    MMObjectNode pnode = (MMObjectNode)e.nextElement();
                    String key = pnode.getStringValue("key");
                    properties.put(key, pnode);
                }
            }
        }
        return properties;
    }


    /**
     * Returns a specified property of this node.
     * @param key the name of the property to retrieve
     * @return the property object as a <code>MMObjectNode</code>
     */
    public MMObjectNode getProperty(String key) {
        MMObjectNode n;
        synchronized(properties_sync) {
            if (properties == null) {
                getProperties();
            }
            n=(MMObjectNode)properties.get(key);
        }
        if (n!=null) {
            return n;
        } else {
            return null;
        }
    }


    /**
     * Sets a specified property for this node.
     * This method does not commit anything - it merely updates the node's propertylist.
     * @param node the property object as a <code>MMObjectNode</code>
     */
    public void putProperty(MMObjectNode node) {
        synchronized(properties_sync) {
            if (properties==null) {
                getProperties();
            }
            properties.put(node.getStringValue("key"),node);
        }
    }

    /**
     * Return the GUI indicator for this node.
     * The GUI indicator is a string that represents the contents of this node.
     * By default it is the string-representation of the first non-system field of the node.
     * Individual builders can alter this behavior.
     * @return the GUI iddicator as a <code>String</code>
     */
    public String getGUIIndicator() {
        if (parent!=null) {
            return parent.getGUIIndicator(this);
        } else {
            log.error("MMObjectNode -> can't get parent");
            return "problem";
        }
    }

    /**
     * Return the buildername of this node
     * @return the builder table name
     */
    public String getName() {
        return  parent.getTableName();
    }

    /**
     * Delete the relation cache for this node.
     * This means it will be reloaded from the database/storage on next use.
     */
    public void delRelationsCache() {
        relationsCache.remove(new Integer(getNumber()));
    }

    /**
     * Returns whether this node has relations.
     * This includes unidirection relations which would otherwise not be counted.
     * @return <code>true</code> if any relations exist, <code>false</code> otherwise.
     */
    public boolean hasRelations() {
        // return getRelationCount()>0;
        return parent.mmb.getInsRel().hasRelations(getNumber());
    }

    /**
     * Return all the relations of this node.
     * Use only to delete the relations of a node.
     * Note that this returns the nodes describing the relation - not the nodes 'related to'.
     * @return An <code>Enumeration</code> containing the nodes
     */
    public Enumeration getAllRelations() {
        Vector allrelations=parent.mmb.getInsRel().getAllRelationsVector(getNumber());
        if (allrelations!=null) {
            return allrelations.elements();
        } else {
            return null;
        }
    }

    /**
     * Return the relations of this node.
     * Note that this returns the nodes describing the relation - not the nodes 'related to'.
     * @return An <code>Enumeration</code> containing the nodes
     */
    public Enumeration getRelations() {
        List relations = getRelationNodes();
        if (relations != null) {
            return Collections.enumeration(relations);
        } else {
            return null;
        }
    }

    /**
     * @since MMBase-1.7
     * @scope public?
     */
    protected List getRelationNodes() {
        Integer number = new Integer(getNumber());
        List relations;
        if (! relationsCache.contains(number)) {
            relations = parent.getRelations_main(getNumber());
            relationsCache.put(number, relations);

        } else {
            relations = (List) relationsCache.get(number);
        }
        return relations;
    }

    /**
     * Remove the relations of the node.
     */
    public void removeRelations() {
        parent.removeRelations(this);
    }

    /**
     * Returns the number of relations of this node.
     * @return An <code>int</code> indicating the number of nodes found
     */
    public int getRelationCount() {
        List relations = getRelationNodes();
        if (relations!=null) {
            return relations.size();
        } else {
            return 0;
        }
    }


    /**
     * Return the relations of this node, filtered on a specified type.
     * Note that this returns the nodes describing the relation - not the nodes 'related to'.
     * @param otype the 'type' of relations to return. The type identifies a relation (InsRel-derived) builder, not a reldef object.
     * @return An <code>Enumeration</code> containing the nodes
     */
    public Enumeration getRelations(int otype) {
        Enumeration e = getRelations();
        Vector result=new Vector();
        if (e!=null) {
            while (e.hasMoreElements()) {
                MMObjectNode tnode=(MMObjectNode)e.nextElement();
                if (tnode.getOType()==otype) {
                    result.addElement(tnode);
                }
            }
        }
        return result.elements();
    }

    /**
     * Return the relations of this node, filtered on a specified type.
     * Note that this returns the nodes describing the relation - not the nodes 'related to'.
     * @param wantedtype the 'type' of relations to return. The type identifies a relation (InsRel-derived) builder, not a reldef object.
     * @return An <code>Enumeration</code> containing the nodes
     */
    public Enumeration getRelations(String wantedtype) {
        int otype=parent.mmb.getTypeDef().getIntValue(wantedtype);
        if (otype!=-1) {
            return getRelations(otype);
        }
        return null;
    }

    /**
     * Return the number of relations of this node, filtered on a specified type.
     * @param wantedtype the 'type' of related nodes (NOT the relations!).
     * @return An <code>int</code> indicating the number of nodes found
     */
    public int getRelationCount(String wt) {
        int count = 0;
        MMObjectBuilder wantedType = parent.mmb.getBuilder(wt);
        if (wantedType != null) {
            List relations = getRelationNodes();
            if (relations != null) {
                for(Enumeration e= Collections.enumeration(relations); e.hasMoreElements();) {
                    MMObjectNode tnode=(MMObjectNode)e.nextElement();
                    int relation_number =tnode.getIntValue("snumber");
                    int nodetype =0;

                    // bugfix #6432: marcel: determine source of relation, get type, display
                    // error when nodetype is determined to be -1, which is a possible wrongly inserted relation

                    if (relation_number==getNumber()) {
                        relation_number = tnode.getIntValue("dnumber");
                        nodetype=parent.getNodeType(relation_number);
                    } else {
                        nodetype=parent.getNodeType(relation_number);
                    }

                    // Display situation where snumber or dnumber from a relation-node does not seem to
                    // exsist in the database. This can be fixed by mannually removing the node out of the insrel-table
                    if(nodetype==-1) {
                        log.warn("Warning: relation_node("+tnode.getNumber()+") has a possible removed relation_number("+relation_number+"), manually check its consistency!");
                    }

                    MMObjectBuilder nodeType = parent.mmb.getBuilder(parent.mmb.getTypeDef().getValue(nodetype));
                    if (nodeType!=null && (nodeType.equals(wantedType) || nodeType.isExtensionOf(wantedType))) {
                        count++;
                    }
                }
            }
        } else {
            log.warn("getRelationCount is requested with an invalid Builder name (otype "+wt+" does not exist)");
        }
        return count;
    }


    /**
     * Returns the node's age
     * @return the age in days
     */
    public int getAge() {
        return parent.getAge(this);
    }

    /**
     * Sends a field-changed signal.
     * @param fieldName the name of the changed field.
     * @return always <code>true</code>
     */
    public boolean sendFieldChangeSignal(String fieldName) {
        return parent.sendFieldChangeSignal(this,fieldName);
    }

    /**
     * Sets the node's alias.
     * The code only sets a (memory) property, it does not actually add the alias to the database.
     * Does not support multiple aliases.
     */
    public void setAlias(String alias) {
        this.alias=alias;
    }

    /**
     * Returns the node's alias.
     * Does not support multiple aliases.
     * @return the alias as a <code>String</code>
     */
    public String getAlias() {
        return alias;
    }


    /**
     * Get all related nodes. The returned nodes are not the
     * nodes directly attached to this node (the relation nodes) but the nodes
     * attached to the relation nodes of this node.
     * @return a <code>Vector</code> containing <code>MMObjectNode</code>s
     */
    public Vector getRelatedNodes() {
        return getRelatedNodes("object", null, RelationStep.DIRECTIONS_EITHER);
    }

    /**
     * Makes number -> MMObjectNode of a vector of MMObjectNodes.
     * @since MMBase-1.6.2
     */
    private Map makeMap(List v) {
        Map       result = new HashMap();
        Iterator  i      = v.iterator();
        while(i.hasNext()) {
            MMObjectNode node = (MMObjectNode) i.next();
            result.put(node.getStringValue("number"), node);
        }
        return result;
    }


    /**
     * Get the related nodes of a certain type. The returned nodes are not the
     * nodes directly attached to this node (the relation nodes) but the nodes
     * attached to the relation nodes of this node.
     *
     * @param type the type of objects to be returned
     * @return a <code>Vector</code> containing <code>MMObjectNode</code>s
     */
    public Vector getRelatedNodes(String type) {
        if (log.isDebugEnabled()) {
            log.debug("Getting related nodes of " + this + " of type " + type);
        }

        if(InsRel.usesdir) {
            return  getRelatedNodes(type, RelationStep.DIRECTIONS_BOTH);
        } else {
            //
            // determine related nodes
            Map source = makeMap(getRelatedNodes(type, RelationStep.DIRECTIONS_SOURCE));
            Map destin = makeMap(getRelatedNodes(type, RelationStep.DIRECTIONS_DESTINATION));

            if (log.isDebugEnabled()) {
                log.debug("source("+source.size()+") - destin("+destin.size()+")");
            }
            // remove duplicates (can happen if multirel is being used when no dir on insrel exists)
            destin.putAll(source);
            return new Vector(destin.values());
        }
    }

    /**
     * If you query from this_node_type(type) (typex, insrel, typey where typex == typey) {
     *   if the insrel table is directional, use the multirelations.SEARCH_BOTH
     *   if the insrel table is not directional, use the multirelations.SEARCH_SOURCE + multirelations.SEARCH_DESTINATION
     * }
     * Otherwise the SEARCH_BOTH will result in an OR on insrel which will never return in
     * (huge) databases.
     * @param type the type of teh realted node to return
     * @param search_type the type of directionality to use
     * @since MMBase-1.6.3
     */
    public Vector getRelatedNodes(String type, int search_type) {
        return getRelatedNodes(type, "insrel",  search_type);
    }

    /**
     * If you query from this_node_type(type) (typex, insrel, typey where typex == typey) {
     *   if the insrel table is directional, use the multirelations.SEARCH_BOTH
     *   if the insrel table is not directional, use the multirelations.SEARCH_SOURCE + multirelations.SEARCH_DESTINATION
     * }
     * Otherwise the SEARCH_BOTH will result in an OR on insrel which will never return in
     * (huge) databases.
     * @param type the type of teh realted node to return
     * @param role the role of the relation (null if no role specified)
     * @param search_type the type of directionality to use
     * @since MMBase-1.6.3
     */
    public Vector getRelatedNodes(String type, String role, int search_type) {
        Vector result = null;

        MMObjectBuilder builder = parent.mmb.getBuilder(type);

        // example: we want a thisnode.relatedNodes(mediaparts) where mediaparts are of type
        // audioparts and videoparts. This method will return the real nodes (thus of type audio/videoparts)
        // when asked to get nodes of type mediaparts.
        //
        // - get a list of virtual nodes from a multilevel("this.parent.name, type") ordered on otype
        //   (this will return virtual audio- and/or videoparts ordered on their *real* parent)
        // - construct a list of nodes for each parentbuilder seperately
        // - ask the parentbuilder for each list of virtual nodes to get a list of the real nodes


        if( builder != null ) {

            ClusterBuilder clusterBuilder = parent.mmb.getClusterBuilder();


            // multilevel from table this.parent.name -> type
            List tables = new ArrayList();
            tables.add(parent.getTableName() + "1");
            if (role != null) {
                tables.add(role);
            }
            tables.add(type + "2");

            // return type.number (and otype for sorting)
            List fields = new ArrayList();
            fields.add(type + "2.number");
            fields.add(type + "2.otype");

            // order list UP
            List directions = new ArrayList();
            directions.add("UP");

            // and order on otype
            List ordered = new ArrayList();
            ordered.add(type + "2.otype");

            List snodes = new ArrayList();
            snodes.add("" + getNumber());

            SearchQuery query = clusterBuilder.getMultiLevelSearchQuery(snodes, fields, "NO", tables,  null, ordered, directions, search_type);
            List v = (List) relatedCache.get(query);
            if (v == null) {
                try {
                    v = clusterBuilder.getClusterNodes(query);
                    relatedCache.put(query, v);
                } catch (SearchQueryException sqe) {
                    log.error(sqe.toString());
                    v = null;
                }
            }
            if(v == null) {
                result = new Vector();
            } else {
                result = new Vector(getRealNodes(v, type + "2"));
            }
        } else {
            log.error("This type(" + type + ") is not a valid buildername!");
            result = new Vector(); // return empty vector
        }

        if (log.isDebugEnabled()) {
            log.debug("related("+parent.getTableName()+"("+getNumber()+")) -> "+type+" = size("+result.size()+")");
        }

        return result;
    }

    /**
     * Loop through the virtuals vector, group all same nodes based on parent and fetch the real nodes from those parents
     *
     * @param List  of virtual nodes (only type.number and type.otype fields are set)
     * @param type, needed to retreive the otype, which is set in node as type + ".otype"
     * @returns List of real nodes
     *
     * @see getRelatedNodes(String type)
     * @since MMBase-1.6.2
     */
    private List getRealNodes(List virtuals, String type) {

        log.debug("Getting real nodes");
        List            result  = new ArrayList();

        MMObjectNode    node    = null;
        MMObjectNode    convert = null;
        Iterator     i       = virtuals.iterator();
        List            list    = new ArrayList();
        int             otype   = -1;
        int             ootype  = -1;

        // fill the list
        while(i.hasNext()) {
            node    = (MMObjectNode)i.next();
            otype   = node.getIntValue(type + ".otype");

            // convert the nodes of type ootype to real numbers
            if(otype != ootype) {
                // if we have nodes return real values
                if(ootype != -1) {
                    result.addAll(getRealNodesFromBuilder(list, ootype));
                    list = new ArrayList();
                }
                ootype  = otype;
            }
            // convert current node type.number and type.otype to number and otype
            String builderName = parent.mmb.getTypeDef().getValue(otype);
            if (builderName == null) {
                log.warn("Could not find builder of node " + node.getNumber() + " taking 'object'");
                builderName = "object";
                otype = parent.mmb.getBuilder(builderName).getObjectType();
            }


            convert = new MMObjectNode(parent.mmb.getBuilder(builderName));
            // parent needs to be set or else mmbase does nag nag nag on a setValue()
            convert.setValue("number", node.getValue(type + ".number"));
            convert.setValue("otype", ootype);
            list.add(convert);
            // first and only list or last list, return real values
            if(!i.hasNext()) {
                // log.debug("subconverting last "+list.size()+" nodes of type("+otype+")");
                result.addAll(getRealNodesFromBuilder(list, otype));
            }
        }

        // check that we didnt loose any nodes

        // Java 1.4
        // assert(virtuals.size() == result.size());

        // Below Java 1.4
        if(virtuals.size() != result.size()) {
            log.error("We lost a few nodes during conversion from virtualnodes(" + virtuals.size() + ") to realnodes(" + result.size() + ")");
        }

        return result;
    }

    /**
     * Upgrade a certain list of MMObectNodes to the right type.
     * @since MMBase-1.6.2
     */
    private List getRealNodesFromBuilder(List list, int otype) {
        List result = new ArrayList();
        String name = parent.mmb.getTypeDef().getValue(otype);
        if(name != null) {
            MMObjectBuilder rparent = parent.mmb.getBuilder(name);
            if(rparent != null) {
                result.addAll(rparent.getNodes(list));
            } else {
                log.error("This otype(" + otype + ") does not denote a valid typedef-name(" + name + ")!");
            }
        } else {
            log.error("This otype(" + otype + ") gives no name(" + name + ") from typedef!");
        }
        return result;
    }


    public int getByteSize() {
        return getByteSize(new org.mmbase.util.SizeOf());
    }

    public int getByteSize(org.mmbase.util.SizeOf sizeof) {
        return sizeof.sizeof(values);
    }


    /**
     * @since MMBase-1.6.2
     */
    public int hashCode() {
        if (parent != null) {
            return parent.hashCode(this);
        } else {
            return super.hashCode();
        }
    }

    /**
     * @since MMBase-1.6.2
     */
    public boolean equals(Object o) {
        if (o instanceof MMObjectNode) {
            MMObjectNode n = (MMObjectNode) o;
            if (parent != null) {
                return parent.equals(this, n);
            } else {
                return defaultEquals(n);
            }
        }
        return false;
    }
    /**
     * @since MMBase-1.6.2
     */
    public boolean defaultEquals(MMObjectNode n) {
        /*
          if (getNumber() >= 0) {  // we know when real nodes are equal
          return n.getNumber() == getNumber();
          } else { // I don't know about others
          return super.equals(n); // compare as objects.
          }
        */
        return super.equals(n); // compare as objects.
    }

}
