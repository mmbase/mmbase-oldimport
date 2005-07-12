/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.implementation.datatypes;

import java.util.*;

import org.mmbase.bridge.Field;
import org.mmbase.bridge.DataType;
import org.mmbase.bridge.datatypes.LongDataType;

/**
 * @javadoc
 *
 * @author Pierre van Rooden
 * @version $Id: BasicLongDataType.java,v 1.5 2005-07-12 15:03:35 pierre Exp $
 * @see org.mmbase.bridge.DataType
 * @see org.mmbase.bridge.datatypes.LongDataType
 * @since MMBase-1.8
 */
public class BasicLongDataType extends BasicNumberDataType implements LongDataType {

    /**
     * Constructor for long field.
     */
    public BasicLongDataType(String name) {
        super(name, Long.class);
    }

    public int getBaseType() {
        return Field.TYPE_LONG;
    }

    public Long getMin() {
        Number min = getMinValue();
        if (min instanceof Long) {
            return (Long)min;
        } else {
            return new Long(min.longValue());
        }
    }

    public Long getMax() {
        Number max = getMaxValue();
        if (max instanceof Long) {
            return (Long)max;
        } else {
            return new Long(max.longValue());
        }
    }

}
