/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.util.xml;

import java.io.*;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import org.mmbase.util.XMLBasicReader;
import org.mmbase.util.logging.*;

import org.w3c.dom.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

/**
 * Abstract class for creating xml documents.
 * Use this class as the base class for writers that construct and export DOM documents.
 * The document can then be used internally or serialized using a number of
 * utility methods.
 *
 * @since MMBase-1.6
 * @author Pierre van Rooden
 * @version $Id: DocumentWriter.java,v 1.2 2002-04-03 12:26:26 michiel Exp $
 */
abstract public class DocumentWriter  {

    // logger
    private static Logger log = Logging.getLoggerInstance(DocumentWriter.class.getName());

    /**
     * The XML builder document.
     * Since getDocument() constructs the document, this field is protected so derived readers can
     * access the document before generation.
     */
    protected Document document;
    /**
     * True if the document has been generated
     */
    private boolean documentGenerated=false;

    /**
     * If true, comments are included
     */
    private boolean includeComments = false;

    /**
     *  Resource bundle with builder comments
     */
    private ResourceBundle messageRB;

    /**
     * Constructs the document writer.
     * The constructor creates a basic document with a root element based on the specified document type parameters.
     * The document is empty after construction.
     * It is actually filled with a call to {@link #generateDocument()}, which is in turn called when
     * the document is first accessed through {@link #getDocument()}.
     * @param qualifiedName the qualified name of teh document's root element
     * @param publicID the PUBLIC id of the document type
     * @param systemID the SYSTEm id of the document type
     */
    public DocumentWriter(String qualifiedName, String publicId, String systemId) throws DOMException {
        DOMImplementation domImpl=XMLBasicReader.getDocumentBuilder().getDOMImplementation();
        DocumentType doctype=domImpl.createDocumentType(qualifiedName, publicId, systemId);
        document=domImpl.createDocument(null,qualifiedName,doctype);
    }

    /**
     * Initialize the ResourceBundle with the given resource.
     * You need a respource to use the addfCOmment() and getMessage() methods.
     * @param resourcelocation Resource.
     */
    protected void getMessageRetriever(String resourcelocation) {
        try {
            messageRB = ResourceBundle.getBundle(resourcelocation);
        } catch (MissingResourceException e) {
            log.error("Resource for XMLBuilderWriter is missing: "+resourcelocation);
        }
    }

    /**
     * Retrieves a message from the resource bundle.
     * @param key the key of the message
     */
    protected String getMessage(String key) {
        return getMessage(key,"");
    }

    /**
     * Retrieves a message from the resource bundle.
     * @param key the key of the message
     * @param a1 the first parameter to substitute in the message
     */
    protected String getMessage(String key, String a1) {
        return getMessage(key,a1,"","");
    }

    /**
     * Retrieves a message from the resource bundle.
     * @param key the key of the message
     * @param a1 the first parameter to substitute in the message
     * @param a2 the second parameter to substitute in the message
     */
    protected String getMessage(String key, String a1, String a2) {
        return getMessage(key,a1,a2,"");
    }

    /**
     * Retrieves a message from the resource bundle.
     * @param key the key of the message
     * @param a1 the first parameter to substitute in the message
     * @param a2 the second parameter to substitute in the message
     * @param a3 the third parameter to substitute in the message
     */
    protected String getMessage(String key, String a1, String a2, String a3) {
        if (messageRB!=null)
        try {
            String message = messageRB.getString(key);
            String[] args = new String[3];
            args[0] = a1;
            args[1] = a2;
            args[2] = a3;
            return java.text.MessageFormat.format(message, args);
        } catch (MissingResourceException e) {
            log.error("Resource for XMLBuilderWriter is broken. There is no " + key + " key in resource.");
        }
        return null;
    }

    /**
     * Creates a DOM element which contains a Text Node, and adds it to the
     * specified node as a child.
     * @param tagname name of the new element
     * @param content content of the new element as a string
     * @param out the element to which to add the new Element.
     * @return the newly created element
     */
    protected Element addContentElement(String tagname,String content, Element out) {
        Element el=document.createElement(tagname);
        if (content==null) content="";
        Text tel=document.createTextNode(content);
        el.appendChild(tel);
        out.appendChild(el);
        return el;
    }

    /**
     * Creates a Comment (provided comments should be included), and adds it to the
     * specified node as a child.
     * The comment is retrieved from a resource bundle - if no resource was specified,
     * no comments are added.
     * @param key the key of the comment to add as a string
     * @param out the element to which to add the new Comment.
     * @return the newly created comment or null if nothing was added
     * @see #setIncludeComments
     */
    protected Comment addComment(String key, Element out) {
        return addComment(key, "", "", out);
    }

    /**
     * Creates a Comment (provided comments should be included), and adds it to the
     * specified node as a child.
     * The comment is retrieved from a resource bundle - if no resource was specified,
     * no comments are added.
     * @param key the key of the comment to add as a string
     * @param a1 the first parameter to substitute in the comment
     * @param out the element to which to add the new Comment.
     * @return the newly created comment or null if nothing was added
     * @see #setIncludeComments
     */
    protected Comment addComment(String key, String a1, Element out) {
        return addComment(key, a1, "", out);
    }

    /**
     * Creates a Comment (provided comments should be included), and adds it to the
     * specified node as a child.
     * The comment is retrieved from a resource bundle - if no resource was specified,
     * no comments are added.
     * @param key the comment to add as a string
     * @param a1 the first parameter to substitute in the comment
     * @param a2 the second parameter to substitute in the comment
     * @param out the element to which to add the new Comment.
     * @return the newly created comment or null if nothing was added
     * @see #setIncludeComments
     */
    protected Comment addComment(String key, String a1, String a2, Element out) {
        Comment comm=null;
        if (includeComments) {
            String message=getMessage(key,a1,a2);
            if (message!=null) {
                comm=document.createComment(" "+message+" ");
                out.appendChild(comm);
            }
        }
        return comm;
    }

    /**
     * Generates the document.
     * You need to override this class with the code that constructs your document.
     * @throws DOMException when an error occurred during generation
     */
    abstract protected void generate() throws DOMException;

    /**
     * Generates the document if it hadn't be done so already.
     * If not, an exception is thrown.
     * Use getDocument() to safely retrive a generated coeumnt.
     * @throws DOMException when an error occurred during generation
     * @throws DOMException when the document was already constructed
     */
    public final Document generateDocument() throws DOMException {
        if (!documentGenerated) {
            generate();
            documentGenerated=true;
            return document;
        } else {
            throw new IllegalStateException("Document already constructed");
        }
    }

    /**
     * Returns the completed document representation;
     * If the document was not yet generated, it is generated  by calling generateDocument().
     * @return the generated document
     * @throws DOMException when an error occurred during generation
     */
    public Document getDocument() throws DOMException {
        if (!documentGenerated) {
            generateDocument();
        }
        return document;
    }

    /**
     * Sets whether the document will include comments
     * @param value if true, the document will include comments
     */
    public void setIncludeComments(boolean value) {
        includeComments=value;
    }

    /**
     * Gets whether the document will include comments
     * @return  if true, the document will include comments
     */
    public boolean getIncludeComments() {
        return includeComments;
    }

    /**
     * Generates the builder configuration and returns it as astring.
     * @return the buidler configuration as a string
     */
    public String writeToString(String filename) throws IOException, TransformerException {
        StringWriter strw=new StringWriter(500);
        write(new StreamResult(strw));
        return strw.toString();
    }

    /**
     * Generates the builder configuration and store it as a file in the given path.
     * @param filename the filepath where the configuration is to be stored
     */
    public void writeToFile(String filename) throws IOException, TransformerException {
        writeToStream(new FileOutputStream(filename));
    }

    /**
     * Generates the builder configuration and store it in the given stream.
     * @param out the output stream where the configuration is to be stored
     */
    public void writeToStream(OutputStream out) throws IOException, TransformerException {
        write(new StreamResult(out));
    }

    /**
     * Creates a builder configuration file and writes it to the result object.
     * @param result the StreamResult object where to store the configueration'
     */
    public void write(StreamResult result) throws IOException, TransformerException {
        Document doc=getDocument();
        TransformerFactory tfactory = TransformerFactory.newInstance();
        tfactory.setURIResolver(new org.mmbase.util.xml.URIResolver(new java.io.File("")));
        // This creates a transformer that does a simple identity transform,
        // and thus can be used for all intents and purposes as a serializer.
        Transformer serializer = tfactory.newTransformer();
        // sets indent amount for xalan
        // should be done elsewhere, but where?
        serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        // xml output configuration
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        serializer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "//MMBase - builder//");
        serializer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.mmbase.org/dtd/builder.dtd");
        serializer.transform(new DOMSource(doc), result);
    }
}
