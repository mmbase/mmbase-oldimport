/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.module;

import java.util.*;
import java.awt.*;

import org.mmbase.util.*;

public interface PlaylistsInterface {
	public void init();
	public byte[] getRAMfile(Vector params);
}
