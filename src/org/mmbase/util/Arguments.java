/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.util;

import java.util.*;
//import org.mmbase.util.logging.*;

/**
 * Arguments for functions, a way to make variable arguments in Java. In fact this class does
 * nothing more then providing a convenient way to create a List. This List is backed by a HashMap.
 *
 * This List is modifiable but not resizeable. It is always the size of the definition array.
 *
 * @author Michiel Meeuwissen
 * @since  MMBase-1.7
 * @see Argument
 */

public class Arguments extends AbstractList implements List  {
    //private static Logger log = Logging.getLoggerInstance(Arguments.class);

    /**
     * The contents of this List are stored in this HashMap.
     */
    private Map backing = new HashMap();

    /**
     * This array maps integers to map keys, making it possible to implement List.
     */
    private   Argument[] definition;

    /**
     * Constructor, taking an Argument[] array argument. The Argument may also be Argument.Wrapper
     * (to impelmente overriding of functions).  The idea is that these array arguments are defined
     * as constants in the classes which define a function with variable arguments.
     */

    public Arguments(Argument [] def) {
        definition = (Argument []) define(def, new ArrayList()).toArray(new Argument[0]);
        // fill with nulls, and check for non-unique keys.
        for (int i = 0; i < definition.length; i++) {
            if (backing.put(definition[i].key, null) != null) {
                throw new IllegalArgumentException("Argument keys not unique");
            } 
            
        }
    }
    /**
     * If you happen to have a List of arguments, then you can wrap it into an Arguments with this constructor
     */
    public Arguments(Argument [] def, List values) {
        this(def);
        if (values.size() > def.length) throw new IllegalArgumentException("Given too many values");
        for (int i = 0; i < values.size(); i++) {
            set(i, values.get(i));
        }
    }
    
    /**
     * Adds the definitions to a List. Resolves the Attribute.Wrapper's (recursively).
     * @return List with only simple Argument's.
     */
    protected List define(Argument[] def, List list) {        
        for (int i = 0; i < def.length; i++) {
            if (def[i] instanceof Argument.Wrapper) {
                define(((Argument.Wrapper) def[i]).arguments, list);
            } else {
                list.add(def[i]);
            }
        }
        return list;
    }


    // implementation of List
    public int size() {
        return definition.length;
    }

    // implementation of List
    public Object get(int i) {
        return backing.get(definition[i].key);
    }

    // implementation of (modifiable) List
    public Object set(int i, Object value) {
        return backing.put(definition[i].key, value);
    }



    /**
     * Sets the value of an argument. 
     * @throws IllegalArgumentException if either the argument name is unknown to this Arguments, or the value is of the wrong type.
     */
    public Arguments set(String arg, Object value) {
        for (int i = 0; i <= definition.length; i++) {
            Argument a = definition[i];
            if (a.key.equals(arg)) {
                if (! a.type.isInstance(value)) {
                    throw new IllegalArgumentException("Argument '" + arg + "' must be of type " + a.type + " (but is " + value.getClass() + ")");
                } else {
                    backing.put(arg, value);
                    return this;
                }
            }
        }
        throw new IllegalArgumentException("The argument '" + arg + "' is not defined");
    }

    public Object get(String arg) {
        return backing.get(arg);
    }
    public String getString(String arg) {
        return Casting.toString(get(arg));
    }

    /**
     * Gives the arguments back as a (unmodifiable) map.  Arguments
     * cannot implement Map itself because 'remove' of List and Map
     * exclude.
     *
     * Remark: Could return a modifiable map, but then need to make a
     * wrapper implementation.
     */

    public Map toMap() {
        return Collections.unmodifiableMap(backing);
    }


}
