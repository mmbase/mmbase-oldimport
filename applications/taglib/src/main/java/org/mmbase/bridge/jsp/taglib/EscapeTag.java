/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.jsp.taglib;
import javax.servlet.jsp.JspTagException;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

/**

 * @author Michiel Meeuwissen
 * @version $Id$
 * @since MMBase-1.8.6
 */

public class EscapeTag extends ContextReferrerTag {
    private static final Logger log = Logging.getLoggerInstance(EscapeTag.class);


    @Override
    public int doStartTag() throws JspTagException {
        return EVAL_BODY_BUFFERED;
    }

    @Override
    public int doEndTag() throws JspTagException {
        helper.setValue(bodyContent != null ? bodyContent.getString() : "");
        helper.doEndTag();
        super.doEndTag();
        return EVAL_PAGE;
    }


}
