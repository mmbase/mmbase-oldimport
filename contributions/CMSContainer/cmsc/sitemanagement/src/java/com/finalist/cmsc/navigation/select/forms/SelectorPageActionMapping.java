/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

 */
package com.finalist.cmsc.navigation.select.forms;

import com.finalist.cmsc.struts.SelectorActionMapping;

@SuppressWarnings("serial")
public class SelectorPageActionMapping extends SelectorActionMapping {

   private boolean strictPageOnly;


   public boolean isStrictPageOnly() {
      return strictPageOnly;
   }


   public void setStrictPageOnly(boolean strictPageOnly) {
      this.strictPageOnly = strictPageOnly;
   }

}
