/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.module.core;
import java.util.Enumeration;

/**
 *
 * @author Rob Vermeulen
 */
public interface NodeInterface {
	NodeInterface getSource();
	NodeInterface getDestination();
	int getDirection();
	String getForwardRole();
	String getReciprocalRole();

	//cardinaliteiten ?
}
