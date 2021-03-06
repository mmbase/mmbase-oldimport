/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.util;

import org.mmbase.bridge.*;
import java.util.*;

/**
 * As {@link AbstractSequentialList}, but implements some extra methods required by BridgeList
 *
 *
 * @author  Michiel Meeuwissen
 * @version $Id$
 * @since   MMBase-1.7
 */

abstract public  class AbstractSequentialBridgeList<E extends Comparable<? super E>>
    extends AbstractSequentialList<E> implements BridgeList<E> {

    private Map<Object,Object> properties = new HashMap<Object,Object>();

    // javadoc inherited
    @Override
    public Object getProperty(Object key) {
        return properties.get(key);
    }

    // javadoc inherited
    @Override
    public void setProperty(Object key, Object value) {
        properties.put(key, value);
    }

    @Override
    public Map<Object, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    // javadoc inherited
    @Override
    public void sort() {
        Collections.sort(this);
    }

    // javadoc inherited
    @Override
    public void sort(Comparator<? super E> comparator) {
        Collections.sort(this, comparator);
    }
    @Override
    abstract public BridgeList<E> subList(int a, int b);


}
