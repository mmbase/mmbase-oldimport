/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package com.finalist.cmsc.repository.select.forms;

import org.apache.struts.action.ActionMapping;

@SuppressWarnings("serial")
public abstract class SelectorActionMapping extends ActionMapping {

    protected abstract String getLinkPattern();
    protected abstract String getTarget();
}
