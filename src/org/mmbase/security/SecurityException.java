/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.security;

/**
 * Thrown by the security classes to indicate a security violation/malfunction.
 * 
 * @author Eduard Witteveen
 * @version $Id: SecurityException.java,v 1.6 2003-08-29 09:38:24 pierre Exp $
 */
public class SecurityException extends java.lang.SecurityException {

    //javadoc is inherited
    public SecurityException() {
        super();
    }

    //javadoc is inherited
    public SecurityException(String message) {
        super(message);
    }

    //javadoc is inherited
    public SecurityException(Throwable cause) {
        super();
        initCause(cause);
    }

    //javadoc is inherited
    public SecurityException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

}
