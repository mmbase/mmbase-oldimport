/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.storage.database;

import java.util.*;
import java.sql.*;
import org.mmbase.storage.*;
import org.mmbase.storage.search.*;

import org.mmbase.module.core.*;
import org.mmbase.module.corebuilders.FieldDefs;
import org.mmbase.module.database.*;
import org.mmbase.module.database.support.MMJdbc2NodeInterface;
import org.mmbase.util.XMLDatabaseReader;

import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;


/**
 * Wrapper of MMJdbc2NodeInterface for the storage classes
 *
 * @author Pierre van Rooden
 * @version $Id: JDBC2NodeWrapper.java,v 1.5 2003-08-18 14:42:46 pierre Exp $
 */
public class JDBC2NodeWrapper implements MMJdbc2NodeInterface {

    // logger
    private static Logger log = Logging.getLoggerInstance(JDBC2NodeWrapper.class);
    
    private StorageManagerFactory factory;
    
    private Map allowedFields;
    
    private Map disallowedFields;
    
    private MMBase mmbase;
    
    public JDBC2NodeWrapper() {
    }

    public boolean isAllowedParentBuilder(MMObjectBuilder builder) {
        // should always be true in storage
        return true;
    }

    public void registerParentBuilder(MMObjectBuilder parent, MMObjectBuilder child) throws StorageException {
        // ignore this call
    }

    public MMObjectNode decodeDBnodeField(MMObjectNode node, String fieldname, ResultSet rs, int i) {
        return decodeDBnodeField(node, fieldname, rs, i, "");
    }

    public MMObjectNode decodeDBnodeField(MMObjectNode node, String fieldname, ResultSet rs, int i, String prefix) {
        try {
            String mmfieldname = prefix+getDisallowedField(fieldname);
            FieldDefs field = node.getBuilder().getField(mmfieldname);
            DatabaseStorageManager sm = (DatabaseStorageManager)factory.getStorageManager();
            // getValue is protected, so can call it from the same package..
            Object value = sm.getValue(rs, i, field);
            node.setValue(mmfieldname, value);
        } catch (StorageException se) {
            log.error(se.getMessage());
        }
        return node;
    }

    public String getMMNodeSearch2SQL(String where,MMObjectBuilder bul) {
        throw new UnsupportedOperationException("Storage classes do not support MMNODE syntax. Use SearchQuery.");
    }
    
    /**
     * @javadoc
     */
    public String getShortedText(String tableName,String fieldname,int number) {
        try {
            MMObjectBuilder bul = mmbase.getMMObject(tableName);
            return factory.getStorageManager().getStringValue(bul.getNode(number),bul.getField(fieldname));
        } catch (StorageException se) {
            log.error(se.getMessage());
            return null;
        }
    }
    
    /**
     * @javadoc
     */
    public byte[] getShortedByte(String tableName,String fieldname,int number) {
        try {
            MMObjectBuilder bul = mmbase.getMMObject(tableName);
            return factory.getStorageManager().getBinaryValue(bul.getNode(number),bul.getField(fieldname));
        } catch (StorageException se) {
            log.error(se.getMessage());
            return null;
        }
    }
    
    public byte[] getDBByte(ResultSet rs,int idx) {
        try {
            DatabaseStorageManager sm = (DatabaseStorageManager)factory.getStorageManager();
            // getValue is protected, so can call it from the same package..
            return sm.getBinaryValue(rs, idx, null);
        } catch (SQLException e) {
            log.error(e.getMessage());
            return null;
        } catch (StorageException se) {
            log.error(se.getMessage());
            return null;
        }
    }
    
    public String getDBText(ResultSet rs,int idx) {
        try {
            DatabaseStorageManager sm = (DatabaseStorageManager)factory.getStorageManager();
            // getValue is protected, so can call it from the same package..
            return sm.getStringValue(rs, idx, null);
        } catch (SQLException e) {
            log.error(e.getMessage());
            return null;
        } catch (StorageException se) {
            log.error(se.getMessage());
            return null;
        }
    }
    
    public int insert(MMObjectBuilder bul,String owner, MMObjectNode node) {
        try {
            return factory.getStorageManager().create(node);
        } catch (StorageException se) {
            log.error(se.getMessage());
            return -1;
        }
    }
    
    public boolean commit(MMObjectBuilder bul,MMObjectNode node) {
        try {
            factory.getStorageManager().change(node);
            return true;
        } catch (StorageException se) {
            log.error(se.getMessage());
            return false;
        }
    }
    
    public void removeNode(MMObjectBuilder bul,MMObjectNode node) {
        try {
            factory.getStorageManager().delete(node);
        } catch (StorageException se) {
            log.error(se.getMessage());
        }
    }
    
    /**
     * Gives an unique number for a node to be inserted.
     * This method should be implemneted to work with multiple mmbase instances working on
     * the same storage.
     * @return unique number
     */
    public int getDBKey() {
        try {
            return factory.getStorageManager().createKey();
        } catch (StorageException se) {
            log.error(se.getMessage());
            return -1;
        }
    }
    
    public void init(MMBase mmb,XMLDatabaseReader parser) {
        mmbase = mmb;
        try {
            this.factory = StorageManagerFactory.newInstance(mmbase);
        } catch (StorageException se) {
            log.error(se.getMessage());
            throw new StorageError();
        }
        disallowedFields = factory.getDisallowedFields();
        allowedFields = new HashMap();
        for (Iterator i = disallowedFields.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry)i.next();
            String val = (String) e.getValue();
            String key = (String) e.getKey();
            if (val != null) {
                allowedFields.put(val, key);
            }
        }
    }
    
    public void setDBByte(int i, PreparedStatement stmt, byte[] bytes) {
        try {
            DatabaseStorageManager sm = (DatabaseStorageManager)factory.getStorageManager();
            // getValue is protected, so can call it from the same package..
            sm.setBinaryValue(stmt, i, bytes, null);
        } catch (SQLException e) {
            log.error(e.getMessage());
        } catch (StorageException se) {
            log.error(se.getMessage());
        }
    }
    
    public boolean created(String tableName) {
        try {
            DatabaseStorageManager sm = (DatabaseStorageManager)factory.getStorageManager();
            // getValue is protected, so can call it from the same package..
            return sm.exists(tableName);
        } catch (StorageException se) {
            log.error(se.getMessage());
            log.error(Logging.stackTrace(se));
            return false;
        }
    }

    public boolean create(MMObjectBuilder bul) {
        try {
            factory.getStorageManager().create(bul);
            return true;
        } catch (StorageException se) {
            log.error(se.getMessage());
            log.error(Logging.stackTrace(se));
            return false;
        }
    }
    
    public boolean createObjectTable(String baseName) {
        try {
            factory.getStorageManager().create();
            return true;
        } catch (StorageException se) {
            log.error(se.getMessage());
            log.error(Logging.stackTrace(se));
            return false;
        }
    }

    public MultiConnection getConnection(JDBCInterface jdbc) throws SQLException {
        javax.sql.DataSource ds = (javax.sql.DataSource)factory.getAttribute("database.dataSource");
        Connection con = ds.getConnection();
        if (con instanceof MultiConnection) {
            return (MultiConnection)con; 
        } else {
            return new MultiConnection(null, con);
        }        
    }

    public String getDisallowedField(String allowedfield) {
        String disallowedfield = (String)allowedFields.get(allowedfield);
        if (disallowedfield != null) {
            return disallowedfield;
        } else {
            return allowedfield;
        }
    }

    public String getAllowedField(String disallowedfield) {
        String allowedfield = (String)disallowedFields.get(disallowedfield);
        if (allowedfield != null) {
            return allowedfield;
        } else {
            return disallowedfield;
        }
    }

    public String getNumberString() {
        return getAllowedField("number");
    }

    public String getOwnerString() {
        return getAllowedField("owner");
    }
    
    public String getOTypeString() {
        return getAllowedField("otype");
    }

    public boolean drop(MMObjectBuilder bul) {
        try {
            factory.getStorageManager().delete(bul);
            return true;
        } catch (StorageException se) {
            log.error(se.getMessage());
            return false;
        }
    }

    
    public boolean updateTable(MMObjectBuilder bul) {
        try {
            factory.getStorageManager().change(bul);
            return true;
        } catch (StorageException se) {
            log.error(se.getMessage());
            return false;
        }
    }

    public boolean addField(MMObjectBuilder bul,String dbname) {
        try {
            factory.getStorageManager().create(bul.getField(dbname));
            return true;
        } catch (StorageException se) {
            log.error(se.getMessage());
            return false;
        }
    }

    public boolean removeField(MMObjectBuilder bul,String dbname) {
        try {
            factory.getStorageManager().delete(bul.getField(dbname));
            return true;
        } catch (StorageException se) {
            log.error(se.getMessage());
            return false;
        }
    }

    public boolean changeField(MMObjectBuilder bul,String dbname) {
        try {
            factory.getStorageManager().change(bul.getField(dbname));
            return true;
        } catch (StorageException se) {
            log.error(se.getMessage());
            return false;
        }
    }
    
    public int getSupportLevel(int feature, SearchQuery query) throws SearchQueryException {
        try {
            return factory.getSearchQueryHandler().getSupportLevel(feature, query);
        } catch (StorageException se) {
            throw new SearchQueryException(se.getMessage());
        }
    }

    public int getSupportLevel(Constraint constraint, SearchQuery query) throws SearchQueryException {
        try {
            return factory.getSearchQueryHandler().getSupportLevel(constraint, query);
        } catch (StorageException se) {
            throw new SearchQueryException(se.getMessage());
        }
    }

    public List getNodes(SearchQuery query, MMObjectBuilder builder) throws SearchQueryException {
        try {
            return factory.getSearchQueryHandler().getNodes(query, builder);
        } catch (StorageException se) {
            throw new SearchQueryException(se.getMessage());
        }
    }
}

