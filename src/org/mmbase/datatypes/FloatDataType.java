/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.datatypes;

/**
 * DataType associated with {@link java.lang.Float}, as NumberDataType, but provides getMin and getMax as float.
 *
 * @author Pierre van Rooden
 * @version $Id: FloatDataType.java,v 1.9 2006-12-15 13:38:01 michiel Exp $
 * @since MMBase-1.8
 */
public class FloatDataType extends NumberDataType<Float> {

    private static final long serialVersionUID = 1L; // increase this if object serialization changes (which we shouldn't do!)
    /**
     * @param primitive indicate if a primitive type should be used
     */
    public FloatDataType(String name, boolean primitive) {
        super(name, primitive ? Float.TYPE : Float.class);
        setMin(Float.valueOf(Float.NEGATIVE_INFINITY), false);
        setMax(Float.valueOf(Float.POSITIVE_INFINITY), false);
    }


    /**
     * @return the minimum value as a <code>float</code>, or {@link Float#NEGATIVE_INFINITY} if there is no minimum.
     */
    public float getMin() {
        Number min = getMinRestriction().getValue();
        return min == null ? Float.NEGATIVE_INFINITY : min.floatValue();
    }

    /**
     * @return the maximum value as a <code>float</code>, or {@link Float#POSITIVE_INFINITY} if there is no maximum.
     */
    public float getMax() {
        Number max = getMaxRestriction().getValue();
        return max == null ? Float.POSITIVE_INFINITY : max.floatValue();
    }
}
