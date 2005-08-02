/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.datatypes;

import java.util.*;
import org.mmbase.util.logging.*;

/**
 * @javadoc
 *
 * @author Pierre van Rooden
 * @since  MMBase-1.8
 * @version $Id: DataTypeCollector.java,v 1.1 2005-08-02 14:29:26 pierre Exp $
 */

public final class DataTypeCollector {

    private static final Logger log = Logging.getLoggerInstance(DataTypeCollector.class);

    // Map of datatypes local to this collector
    private Map dataTypes = new HashMap();

    // the object to finish datatypes with
    private Object signature = null;

    // dependent collectors
    private List collectors = new ArrayList();

    // the DataTypeCollector used to store datatypes accessible throughout the application
    private static DataTypeCollector systemDataTypeCollector;

    /**
     * Creates the DataTypeCollector used to store datatypes accessible throughout the application.
     * Called by the {@link DataTypes} class.
     */
    public static DataTypeCollector createSystemDataTypeCollector() {
        if (systemDataTypeCollector == null) {
            Object signature = new String( "MMBASE_" + System.currentTimeMillis());
            systemDataTypeCollector = new DataTypeCollector(signature);
            return systemDataTypeCollector;
        } else {
            throw new SecurityException("System datatype collector already defined, may not be created twice.");
        }
    }

    /**
     *  Constructor.
     * @param signature the object used to finish a data type for this collector.
     */
    public DataTypeCollector(Object signature) {
         this.signature = signature;
    }

    /**
     * Adds a datatype collector on which this collector depends.
     * when trying to obtain a datatype or datatype instance, if
     * the current collector does not contain the datatype, it tries to obtain
     * it from any colelctors it depends on.
     * @param colelctor the datatYpe colelctor to add add
     */
     public void addCollector(DataTypeCollector collector) {
         collectors.add(collector);
     }

    /**
     * Set local datatypes of the collector
     * @param dataTypes a <code>Map</code> containing the datatypes
     */
    public void setDataTypes(Map dataTypes) {
        this.dataTypes = dataTypes;
log.info("DataTypes for collector with signature " + signature + ":" +dataTypes);
    }

    /**
     * Set local datatypes of the collector
     * @param dataTypes a <code>Map</code> containing the datatypes
     */
    public Map getDataTypes() {
        return dataTypes;
    }

    /**
     * Get a datatype defined for this collector.
     * @param name the name of the datatype to retrieve
     * @return  a {@link DataType} with the given name, as defined for this collector, or <code>null</code>
     *      if no datatype is defined.
     */
    public DataType getDataType(String name) {
        return getDataType(name, false);
    }

    /**
     * Adds a datatype to this collector.
     * @param dataType the datatype to add
     */
    public void addDataType(DataType dataType) {
        dataTypes.put(dataType.getName(),dataType);
    }

    /**
     * Get a datatype defined for this collector, and possibly any collectors it depends on.
     * The collector first searches among datatypes defined in its own set.
     * If that fails, it tries ot get it from any of the other collectors it may depend on, and eventually
     * from the main repository.
     * @param name the name of the datatype to retrieve
     * @param recursive if <code>true</code>, the datatype is also searched in collectors it depends on.
     * @return  a {@link DataType} with the given name, as defined for this collector, or <code>null</code>
     *      if no datatype is defined.
     */
    public DataType getDataType(String name, boolean recursive) {
        DataType dataType = null;
        dataType = (DataType) dataTypes.get(name);
        if (this != systemDataTypeCollector && dataType == null && recursive) {
            for (Iterator i = collectors.iterator(); dataType == null && i.hasNext();) {
                DataTypeCollector collector = (DataTypeCollector) i.next();
                dataType = collector.getDataType(name, true);
            }
            if (dataType == null) {
                dataType = systemDataTypeCollector.getDataType(name, false);
            }
        }
        return dataType;
    }

    /**
     * Get a datatype instance through this collector.
     * The collector first searches among datatypes defined in its own set.
     * If that fails, it tries to get it from any of the other collectors it may depend on, and eventually
     * from the main repository.
     * If that fails too, it creates one itself based on the passed base datatype (if given).
     * @param name the name of the datatype to retrieve
     * @param baseDataType the datatype to base a new datatype on if it is not yet defined. Can be <code>null</code>.
     * @return  a {@link DataType} with the given name, as defined for this collector, or <code>null</code>
     *      if no datatype is defined and no base datatype was passed.
     */
    public DataType getDataTypeInstance(String name, DataType baseDataType) {
        DataType dataType = getDataType(name, true);
        if (dataType == null && baseDataType == null) {
            return null;
        } else if (dataType == null) {
            return (DataType)baseDataType.clone(name);
        } else {
            return (DataType)dataType.clone();
        }
    }

    /**
     * Returns whether the dataType with the given name is part of the current collection.
     */
    public boolean contains(String name) {
        return dataTypes.containsKey(name);
    }

    /**
     * Returns whether the dataType is part of the current collection.
     */
    public boolean contains(DataType dataType) {
        return dataTypes.containsValue(dataType);
    }

    /**
     * Unlock a dataType so it can be changed or latered.
     * This will likely fail if the datatype is not part of this collector.
     */
    public void rewrite(DataType dataType) {
        dataType.rewrite(signature);
    }

    /**
     * Lock a dataType so it can be changed or latered.
     * This will likely fail if the datatype is not part of this collector.
     */
    public void finish(DataType dataType) {
        dataType.finish(signature);
    }

    public String toString() {
        return signature +  ": " + dataTypes.values().toString();
    }

}

