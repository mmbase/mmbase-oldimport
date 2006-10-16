/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.storage.search.implementation;

import java.util.*;
import org.mmbase.storage.search.*;

/**
 * Basic implementation.
 *
 * @author Rob van Maris
 * @version $Id: BasicFieldValueInConstraint.java,v 1.14 2006-10-16 12:56:57 pierre Exp $
 * @since MMBase-1.7
 */
public class BasicFieldValueInConstraint extends BasicFieldConstraint implements FieldValueInConstraint {

    /** The values. */
    private SortedSet values = new TreeSet();

    /**
     * Constructor.
     *
     * @param field The associated field.
     */
    public BasicFieldValueInConstraint(StepField field) {
        super(field);
    }

    /**
     * Adds value.
     *
     * @param value The value.
     * @return This <code>BasicFieldValueInConstraint</code> instance.
     * @throws IllegalArgumentException when an invalid argument is supplied.
     * @see org.mmbase.storage.search.FieldValueInConstraint#getValues
     */
    public BasicFieldValueInConstraint addValue(Object value) {
        BasicStepField.testValue(value, getField());
        values.add(value);
        return this;
    }

    // javadoc is inherited
    public SortedSet getValues() {
        return Collections.unmodifiableSortedSet(values);
    }

    // javadoc is inherited
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        // Must be same class (subclasses should override this)!
        if (obj != null && obj.getClass() == getClass()) {
            BasicFieldValueInConstraint constraint = (BasicFieldValueInConstraint) obj;
            return isInverse() == constraint.isInverse()
                && isCaseSensitive() == constraint.isCaseSensitive()
                && getField().getFieldName().equals(constraint.getField().getFieldName())
                && BasicStepField.compareSteps(getField().getStep(),
                    constraint.getField().getStep())
                && values.equals(constraint.values);
        } else {
            return false;
        }
    }

    // javadoc is inherited
    public int hashCode() {
        return super.hashCode()
        + 89 * values.hashCode();
    }

    // javadoc is inherited
    public String toString() {
        StringBuilder sb = new StringBuilder("FieldValueInConstraint(inverse:").append(isInverse()).
        append(", field:").append(getFieldName()).
        append(", casesensitive:").append(isCaseSensitive()).
        append(", values:").append(getValues()).
        append(")");
        return sb.toString();
    }

}
