/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

 */

package org.mmbase.mojo.remote;

import java.io.File;
import java.lang.reflect.*;

/**
 * @javadoc
 *
 * @since MMBase-1.9
 * @author Pierre van Rooden
 * @version $Id: InterfaceGenerator.java,v 1.2 2009-04-30 20:06:07 michiel Exp $
 */
public class InterfaceGenerator extends AbstractClassGenerator {

    String originalName;
    String interfaceName;

    public InterfaceGenerator(Class<?> c) {
        super(c);
    }

    public void generateHeader() {
        generateLicense();
        //create the default imports for the interface
        buffer.append("package org.mmbase.bridge.remote;\n");
        buffer.append("\n");
        buffer.append("import java.rmi.*;\n");
        buffer.append("import org.mmbase.cache.*;\n");
        buffer.append("import org.mmbase.datatypes.*;\n");
        buffer.append("import org.mmbase.security.*;\n");
        buffer.append("import org.mmbase.cache.*;\n");
        buffer.append("import org.mmbase.util.LocalizedString;\n");
        buffer.append("import org.mmbase.util.functions.*;\n");
        buffer.append("\n");
        buffer.append("/**\n");
        buffer.append(" * " + interfaceName + " is a generated interface based on " + currentClass.getName() + "<br />\n");
        buffer.append(" * This interface has almost the same methods names as the " + originalName + " interface.\n");
        buffer.append(" * The interface is created in such way that it can implement java.rmi.Remote.\n");
        buffer.append(" * Where needed other return values or parameters are used.\n");
        buffer.append(" * @author generated by " + this.getClass().getName() + "\n");
        buffer.append(" */\n");
        buffer.append(" //DO NOT EDIT THIS FILE, IT IS GENERATED by " + this.getClass().getName() + "\n");
    }

    @Override
    protected void appendMethod(Method m) {
        appendMethodHeader(m, true, true);
        buffer.append(" throws RemoteException;\n");
    }

    @Override
    public void generate() {
        originalName = getShortName(currentClass);
        interfaceName = "Remote" + getShortName(currentClass);

        generateHeader();

        buffer.append("public interface ");
        appendTypeInfo(currentClass);
        appendTypeParameters(currentClass.getTypeParameters());

        if (isListIterator(currentClass)) {
            Type[] typeParameters = getListIteratorTypeParameters(currentClass);
            buffer.append(" extends RemoteIterator");
            appendTypeParameters(typeParameters, true, true);
        }
        else {
            if (isList(currentClass)) {
                Type[] typeParameters = getListTypeParameters(currentClass);
                buffer.append(" extends RemoteBridgeList");
                appendTypeParameters(typeParameters, true, true);
            }
            else {
                buffer.append(" extends ");

                Type[] interfaces = currentClass.getGenericInterfaces();
                resolveTypeParameters(interfaces);

                for (Type element : interfaces) {
                    Type ct = getComponentType(element);
                    if (needsRemote(ct)) {
                        appendTypeInfo(element, true, true);
                        buffer.append(", ");
                    }
                }
                buffer.append(" ServerMappedObject");
            }
        }

        buffer.append(" {\n");
        // methods
        appendMethods();

        buffer.append("}\n");
    }

    public void generate(File remoteDir) {
        generate();
        writeSourceFile(new File(remoteDir, interfaceName + ".java"));
    }

}
