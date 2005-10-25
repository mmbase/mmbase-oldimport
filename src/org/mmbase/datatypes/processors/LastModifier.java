/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.datatypes.processors;

import org.mmbase.bridge.*;

/**
 * This processor can be used as a 'set' processor on a (datetime) field. The field will then be set
 * to the current user id when the node is committed. If the field is set in another way, an exception is
 * thrown (in other words, the field is read only).
 *
 * @author Michiel Meeuwissen
 * @version $Id: LastModifier.java,v 1.1 2005-10-25 12:30:26 michiel Exp $
 * @since MMBase-1.8
 * @see   LastModifier
 */


public class LastModifier implements CommitProcessor, Processor {

    private static final int serialVersionUID = 1;

    /**
     * You can plug this in on every set-action besides 'object' which will make this
     * field unmodifiable, except for set(Object) itself (which is never used from editors).
     */
    public Object process(Node node, Field field, Object value) {
        throw new BridgeException("You cannot change the field " + field.getName());
    }

    public void commit(Node node, Field field) {
        node.setValueWithoutProcess(field.getName(),node.getCloud().getUser().getIdentifier());
    }

    public String toString() {
        return "lastmodifier";
    }
}
