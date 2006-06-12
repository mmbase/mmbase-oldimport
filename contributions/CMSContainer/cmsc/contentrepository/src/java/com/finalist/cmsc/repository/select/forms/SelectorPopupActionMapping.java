/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package com.finalist.cmsc.repository.select.forms;

@SuppressWarnings("serial")
public class SelectorPopupActionMapping extends SelectorActionMapping {

    protected String getLinkPattern() {
        return "javascript:selectChannel({0},''{1}'')";
    }

    protected String getTarget() {
        return null;
    }

}
