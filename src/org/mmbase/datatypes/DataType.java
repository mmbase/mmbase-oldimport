/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.datatypes;

import java.util.*;

import org.mmbase.bridge.*;
import org.mmbase.datatypes.processors.*;
import org.mmbase.bridge.util.Queries;
import org.mmbase.storage.search.*;
import org.mmbase.core.util.Fields;
import org.mmbase.core.AbstractDescriptor;
import org.mmbase.datatypes.DataTypes;
import org.mmbase.util.*;
import org.mmbase.util.logging.*;

/**
 * A value in MMBase (such as the value of a field, or function parameter) is associated with a
 * 'datatype'.  A DataType is actually an elaborated wrapper arround a Class object, but besides
 * this basic type of the value, it also defines restrictions on the values, a default value,
 * Processors, and perhaps other properties (e.g. properties which describe indications for edit
 * tool implementations).
 * 
 * There are several extensions of DataType which normally add other kinds of restrictions which are
 * specific for certain classes of values. All implementations of DataType extend from {@link
 * BasicDataType}, but they can sometimes implement different extensions of DataType at the same time
 * ('multiple inheritance').
 *
 * @author Pierre van Rooden
 * @author Michiel Meeuwissen
 * @since  MMBase-1.8
 * @version $Id: DataType.java,v 1.43 2005-11-17 18:06:51 michiel Exp $
 */

public interface DataType extends Descriptor, Cloneable, Comparable, java.io.Serializable {

    // XXXX MM: I think 'action' must be gone; it is silly.
    static final int PROCESS_GET    = 1;
    static final int PROCESS_SET    = 2;


    /**
     * Return value for {@link DataType.Restriction#getEnforceStrength}. This means that the value must be enforced always.
     */
    static final int ENFORCE_ALWAYS   = Integer.MAX_VALUE;

    /**
     * Return value for {@link DataType.Restriction#getEnforceStrength}. This means that the value must be enforced only if it was changed.
     */
    static final int ENFORCE_ONCHANGE = 10000;

    /**
     * Return value for {@link DataType.Restriction#getEnforceStrength}. This means that the value must be enforced only on creation.
     */
    static final int ENFORCE_ONCREATE = 1000;

    /**
     * Return value for {@link DataType.Restriction#getEnforceStrength}. This means that the
     * value must be enforced never, so the restriction serves only as UI indication.
     */
    static final int ENFORCE_NEVER    = 0;

    /**
     * An empty Parameter array.
     */
    static final DataType[] EMPTY  = new DataType[0];

    /**
     * Returned by {@link #validate} if no errors.
     */
    public static final Collection VALID = Collections.EMPTY_LIST;

    /**
     * Inherit properties and processors from the passed datatype and
     * sets the passed datatype as the origin for this datatype.
     */
    public void inherit(BasicDataType origin);

    /**
     * Return the DataType from which this one inherited, or <code>null</code>
     */
    public DataType getOrigin();
    /**
     * Returns the type of values that this data type accepts.
     * @return the type as a Class
     */
    public Class getTypeAsClass();

    /**
     * Checks if the passed object is of the correct class (compatible with the type of this data type),
     * and throws an IllegalArgumentException if it doesn't.
     * @param value the value whose type (class) to check
     * @throws IllegalArgumentException if the type is not compatible
     */
    public void checkType(Object value);

    /**
     * Tries to 'cast' an object for use with this parameter. E.g. if value is a String, but this
     * parameter is of type Integer, then the string can be parsed to Integer.
     *
     *
     * @param value The value to be filled in a value with this DataType.
     * @param node  Sometimes a node might be needed.
     * @param field Sometimes a (or 'the') field might be needed.
     */
    public Object cast(Object value, Node node, Field field);

    /**
     * If datatypes outside 'fields' are imaginable, which still need cloud, then the following may need addition
     */
    // public Object cast(Object value, Cloud cloud);


    /**
     * Before actually 'cast' an object to the right type, it may undergo some conversion by the
     * datatype, e.g. enumerations may get resolved (enumeration have the feature that they can
     * e.g. resolve java-constants to their values).
     *
     * This does not garantuee that the value has the 'proper' type, but only that it now can be
     * casted to the right type without further problems. (Casting.toType should do).
     * 
     * preCast should not change the actual type of value. It is e.g. used in the
     * Node#setStringValue, and the processor may expect a String there.
     */
    public Object preCast(Object value, Node node, Field field);

    /**
     * If datatypes outside 'fields' are imaginable, which still need cloud, then the following may need addition (implemented already as a util in BasicDataType)
     */
    // public Object preCast(Object value, Cloud cloud);



    /**
     * Returns the default value of this data type.
     * @return the default value
     */
    public Object getDefaultValue();

    public DataType setDefaultValue(Object def);

    /**
     * @javadoc
     */
    public DataType rewrite(Object owner);
    /**
     * @javadoc
     */
    public boolean isFinished();
    /**
     * @javadoc
     */
    public DataType finish(Object owner);


    /**
     * Checks if the passed object is of the correct type (compatible with the type of this data type),
     * and obeys the restrictions defined for this type.
     * @return An error message if the value is not compatible. An empty collection if valid.
     * @param value the value to validate
     */
    public Collection /*<LocalizedString>*/ validate(Object value);

    /**
     * Checks if the passed object obeys the restrictions defined for this type.
     * @param value the value to validate
     * @param node the node for which the datatype is checked. If not <code>null</code>, and the
     *        datatype is determined as unique, than uniquness is checked for this value using the passed field.
     * @param field the field for which the datatype is checked.
     *
     * @return The error message(s) if the value is not compatible. An empty collection if the value is valid.
     */
    public Collection /*<LocalizedString> */ validate(Object value, Node node, Field field);
    /**
     * Returns whether this field is required (should have content).
     * Note that the MMBase core does not generally enforce required fields to be filled -
     * If not provided, a default value (generally an empty string or the integer value -1)
     * is filled in by the system.
     *
     * @return  <code>true</code> if the field is required
     */
    public boolean isRequired();

    /**
     * Returns the 'required' restriction, containing the value, errormessages, and fixed status of this attribute.
     * @return the restriction as a {@link DataType.Restriction}
     */
    public DataType.Restriction getRequiredRestriction();

    /**
     * Sets whether the data type requires a value, which means that it may not remain unfilled.
     * @param required <code>true</code> if a value is required
     * @throws InvalidStateException if the datatype was finished (and thus can no longer be changed)
     * @return the datatype restriction that was just set
     */
    public DataType.Restriction setRequired(boolean required);

    /**
     * Returns whether this field has a unique restriction.
     * Uniqueness is generally achieved through association of the datatype with one or more sets of fields.
     * This is notably different from other datatype properties.
     *
     * Note that the MMBase core does not generally enforce uniqueness, but the storage layer might.
     *
     * @return  <code>true</code> if the field is unique
     */
    public boolean isUnique();

    /**
     * Returns the 'unique' restriction, containing the value, error messages, and fixed status of this attribute.
     * @return the restriction as a {@link DataType.Restriction}
     */
    public DataType.Restriction getUniqueRestriction();

    /**
     * Sets whether the data type requires a value.
     * @param unique <code>true</code> if a value is unique
     * @throws InvalidStateException if the datatype was finished (and thus can no longer be changed)
     * @return the datatype restricton that was just set
     */
    public DataType.Restriction setUnique(boolean unique);

    /**
     * Returns an iterator over all possible values for this datatype, as {@link java.util.Map.Entry}s, or
     * <code>null</code> if no enumeration restrictions apply. Every Map entry contains as key the
     * 'value' for this datatype and as value it contains the description for this value in the
     * given locale.
     *
     * This Iterator skips all entries which are impossible because of other restrictions on this datatype.
     *
     * @param locale for which locale to produce
     * @param cloud  Possibly the possible values depend on a cloud (security)
     * @param node   Possibly the possible values depend on an actual node (this may be, and in the default implementation is, ignored)
     * @param field   Possibly the possible values depend on an actual field (this may be, and in the default implementation is, ignored)
     *
     */
    public Iterator getEnumerationValues(Locale locale, Cloud cloud, Node node, Field field);

    /**
     * @return the LocalizedEntryListFactory which will be used to produce the result of {@link
     * #getEnumerationValues}. Never <code>null</code>. This can be used to add more possible values.
     */
    public LocalizedEntryListFactory getEnumerationFactory();

    /**
     * The enumeration for this datatype as a {@link Restriction}. 
     */
    public DataType.Restriction getEnumerationRestriction();



    public CommitProcessor getCommitProcessor();

    /**
     */
    public void setCommitProcessor(CommitProcessor cp);

    /**
     * Returns the default processor for this action
     * @param action either {@link #PROCESS_GET}, or {@link #PROCESS_SET}
     * XXX What exactly would be against getCommitProcessor(), getGetProcesor(), getSetProcessor() ?
     */
    public Processor getProcessor(int action);
    /**
     * Returns the processor for this action and processing type
     * @param action either PROCESS_COMMIT, PROCESS_GET, or PROCESS_SET
     * @param processingType the MMBase type defining the type of value to process, ignored if action - PROCESS_COMMIT
     */
    public Processor getProcessor(int action, int processingType);
    /**
     * Sets the processor for this action
     * @param action either PROCESS_COMMIT, PROCESS_GET, or PROCESS_SET
     */
    public void setProcessor(int action, Processor processor);

    /**
     * Sets the processor for this action
     * @param action either PROCESS_COMMIT, PROCESS_GET, or PROCESS_SET
     * @param processingType the MMBase type defining the type of value to process, ignored if action - PROCESS_COMMIT
     */
    public void setProcessor(int action, Processor processor, int processingType);


    /**
     * Returns a cloned instance of this datatype, inheriting all validation rules.
     * Unlike the original datatype though, the cloned copy is declared unfinished even if the original
     * was finished. This means that the cloned datatype can be changed.
     */
    public Object clone();
    /**
     * Returns a cloned instance of this datatype, inheriting all validation rules.
     * Similar to calling clone(), but changes the data type name if one is provided.
     * @param name the new name of the copied datatype (can be <code>null</code>, in which case the name is not changed).
     */
    public Object clone(String name);


    /**
     * A restriction controls the acceptable values of a DataType.
     */
    public interface Restriction extends java.io.Serializable {

        public String getName();
        /** 
         * A Value describing the restriction, so depending on the semantics of this restriction, it
         * can have virtually every type.
         */
        public Object getValue();
        public Restriction setValue(java.io.Serializable value);
        /**
         * If the restriction does not hold, the following error description can be used. On default
         * these descriptions are searched in a resource bundle based on the name of this
         * restriction.
         */
        public LocalizedString getErrorDescription();
        public void setErrorDescription(LocalizedString errorDescription);

        /**
         * This function should contain the actual logic of the restriction. This does not consider the 'enforceStrength'.
         * @param value The value to check the restriction for
         * @param node  Some constrainst may need the Node.
         * @param field Some constrainst may need the Field.
         * @return Whether the supplied value is a valid value for this restriction.
         */
        public boolean valid(Object value, Node node, Field field);

        /**
         * If a restriction is 'fixed', the value and error-description cannot be changed any more.
         */
        public void setFixed(boolean fixed);
        
        /**
         * See {@link DataType#ENFORCE_ALWAYS}, {@link DataType#ENFORCE_ONCHANGE}, {@link DataType#ENFORCE_NEVER}.
         */
        public int getEnforceStrength();
        public void setEnforceStrength(int v);
    }

}
