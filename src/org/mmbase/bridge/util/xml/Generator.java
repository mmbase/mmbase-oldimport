/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/

package org.mmbase.bridge.util.xml;

import org.w3c.dom.*;
import org.w3c.dom.Node;
import org.mmbase.bridge.*;

import org.mmbase.util.logging.*;

/**
 * Uses the XML functions from the bridge the construct DOM elements
 * representing MMBase data structures.
 *
 * @author Michiel Meeuwissen
 * @author Eduard Witteveen
 * @version $Id: Generator.java,v 1.13 2002-06-24 13:46:57 michiel Exp $
 */
public class Generator {

    private static Logger log = Logging.getLoggerInstance(Generator.class.getName());
    private Document document = null;
    private Cloud    cloud    = null;

    /**
     * To create documents representing structures from the cloud, it
     * needs a documentBuilder, to contruct the DOM Document, and the
     * cloud from which the data to be inserted will come from.    
     *
     * @param documentBuilder The DocumentBuilder which will be used to create the Document.
     * @param cloud           The cloud from which the data will be.
     */
    public Generator(javax.xml.parsers.DocumentBuilder documentBuilder, Cloud cloud) {
        DOMImplementation impl = documentBuilder.getDOMImplementation();        
        this.document = impl.createDocument(null, "objects", impl.createDocumentType("objects",  "-//MMBase/DTD objects config 1.0//EN", "http://www.mmbase.org/dtd/objects_1_0.dtd"));
        this.cloud = cloud;
        //Element rootElement = document.createElement("objects");
        //document.appendChild(rootElement);
    }
    
    public Generator(javax.xml.parsers.DocumentBuilder documentBuilder) {
        this(documentBuilder, null);
    }
    
    /**
     * Returns the working DOM document.
     * @return The document, build with the operations done on the generator class
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Returns the document as a String.
     * @return the xml generated as an string
     */
    public String toString() {
        return toString(false);
    }

    /**
     * Returns the document as a String.
     * @param ident if the string has to be idented
     * @return the generated xml as a (formatted) string
     */
    public String toString(boolean ident) {
        try {
            org.apache.xml.serialize.OutputFormat format = new org.apache.xml.serialize.OutputFormat(document);
            if(ident) {
                format.setIndenting(true);
                format.setPreserveSpace(false);
                //  format.setOmitXMLDeclaration(true);
                //  format.setOmitDocumentType(true);
            }
            java.io.StringWriter result = new java.io.StringWriter();
            org.apache.xml.serialize.XMLSerializer prettyXML = new org.apache.xml.serialize.XMLSerializer(result, format);
            prettyXML.serialize(document);
            return result.toString();
        }
        catch (Exception e) {
            return e.toString();
        }
    }
    
    
    /**
     * Adds a field to the DOM Document. This means that there will
     * also be added a Node if this is necessary.
     * @param An MMbase bridge Node.
     * @param An MMBase bridge Field.
     */
    public void add(org.mmbase.bridge.Node node, Field fieldDefinition) {
        if (cloud == null) {
            cloud = node.getCloud();
            document.getDocumentElement().setAttribute("cloud", cloud.getName());    
        }

        Element object = getNode(node);

        // get the field...
        Element field = (Element) object.getFirstChild();
        while(field != null && !fieldDefinition.getName().equals(field.getAttribute("name"))) {
            field = (Element) field.getNextSibling();
        }
        // when not found, we are in a strange situation..
        if(field==null) throw new BridgeException("field with name: " + fieldDefinition.getName() + " of node " + node.getNumber() + " with  nodetype: " + fieldDefinition.getNodeManager().getName() + " not found, while it should be in the node skeleton.. xml:\n" + toString(true));        
        // when it is filled (allready), we can return
        if(field.getTagName().equals("field")) return;

        // was not filled, so fill it... first remove the unfilled 
        Element filledField = document.createElement("field");


        field.getParentNode().replaceChild(filledField, field);
        field = filledField;
        // the name
        field.setAttribute("name", fieldDefinition.getName());
        // now fill it with the new info...
        // format
        field.setAttribute("format", getFieldFormat(node, fieldDefinition));
        // the value
        switch(fieldDefinition.getType()) {
            case Field.TYPE_XML:
                Document doc = node.getXMLValue(fieldDefinition.getName());                
                // only fill the field, if field has a value..
                if(doc!= null) {                    
                    // put the xml inside the field...
                    field.appendChild(importDocument(field, doc));
                }
            break;
            case Field.TYPE_BYTE:
                org.mmbase.util.transformers.Base64 transformer = new org.mmbase.util.transformers.Base64();
                field.appendChild(document.createTextNode(transformer.transform(node.getByteValue(fieldDefinition.getName()))));
            break;
            default:
                field.appendChild(document.createTextNode(node.getStringValue(fieldDefinition.getName())));
        }
        // or do we need more?
    }

    /**
     * Adds one Node to a DOM Document.
     * @param An MMBase bridge Node.
     */
    public void add(org.mmbase.bridge.Node node) {
        // process all the fields..
        FieldIterator i = node.getNodeManager().getFields().fieldIterator();
        while(i.hasNext()) {
            add(node, i.nextField());
        }                
    }

    /**
     * Adds one Relation to a DOM Document.
     * @param An MMBase bridge Node.
     */
    public void add(Relation relation) {        
        add((org.mmbase.bridge.Node) relation);   
    }

    /**
     * Adds a whole MMBase bridge NodeList to the DOM Document.
     * @param An MMBase bridge NodeList.
     */
    public void add(org.mmbase.bridge.NodeList nodes) {    
        NodeIterator i = nodes.nodeIterator();
        while(i.hasNext()) {
            add(i.nextNode());
        }    
    }

    /**
     * Adds one Relation to a DOM Document.
     * @param An MMBase bridge Node.
     */
    public void add(RelationList relations) {
        RelationIterator i = relations.relationIterator();
        while(i.hasNext()) {
            add(i.nextRelation());
        }
    }
    /**
     * Creates an Element which represents a bridge.Node with all fields unfilled.
     */
    private Element getNode(org.mmbase.bridge.Node node) { 
        // MMBASE BUG...
        // we dont know if we have the correct typee...
        node = cloud.getNode(node.getNumber());
        
        // if we are a relation,.. behave like one!
        // why do we find it out now, and not before?       
                     
        // TODO: reseach!!
        boolean getElementByIdWorks = false;
        Element object = null;
        if(getElementByIdWorks) {
            // Michiel: I tried it by specifieing id as ID in dtd, but that also doesn't make it work.
            
            object = document.getElementById("" + node.getNumber());
        } else {
            // TODO: this code should be removed!! but other code doesnt work :(
            // this cant be fast in performance...
            String xpath = "//*[@id='"+node.getNumber()+"']";
            try {
                object = (Element) org.apache.xpath.XPathAPI.selectSingleNode(document.getDocumentElement(), xpath);
            }
            catch(javax.xml.transform.TransformerException te) {
                String msg = "error executing query: '" + xpath + "'";
                log.error(msg);
                log.error(Logging.stackTrace(te));
                throw new BridgeException(msg);
            }
        }

        if (object != null) return object;

        // if it is a realtion... first add source and destination info thingies..
        // can only happen after the node = node.getCloud().getNode(node.getNumber()); thing!
        if(node instanceof Relation) {
            Relation relation = (Relation) node;
            getNode(relation.getSource()).appendChild(createRelationEntry(relation, relation.getSource()));
            getNode(relation.getDestination()).appendChild(createRelationEntry(relation, relation.getDestination()));
        }
        
        // node didnt exist, so we need to create it...
        object = document.createElement("object");        
        object.setAttribute("id",    "" + node.getNumber());
        object.setAttribute("type",  node.getNodeManager().getName());
        // and the otype (type as number)
        object.setAttribute("otype", node.getStringValue("otype"));
        
        // add the fields (empty) 
        // While still having 'unfilledField's
        // you know that the node is not yet presented completely.

        FieldIterator i = node.getNodeManager().getFields().fieldIterator();
        while(i.hasNext()) {
            Field fieldDefinition = i.nextField();
            Element field = document.createElement("unfilledField");
            // the name
            field.setAttribute("name", fieldDefinition.getName());
            // add it to the object
            object.appendChild(field);
        }        
        document.getDocumentElement().appendChild(object);
        return object;        
    }    
    

    /**
     * Imports an XML document as a value of a field. Can be any XML, so the namespace is set.
     *
     * @param fieldElement The Element describing the field
     * @param toImport     The Document to set as the field's value
     */
    private Element importDocument(Element fieldElement, Document toImport) {
        DocumentType dt = toImport.getDoctype();
        String tagName = toImport.getDocumentElement().getTagName();

        String  namespace;
        if (dt != null) {
            namespace = dt.getSystemId();
        }  else {
            namespace = "http://www.mmbase.org/" + tagName;
        }
                      
        String prefix = tagName + ":";       
        
        if (log.isDebugEnabled()) {
            log.debug("using namepace: " + namespace + " with prefix: " + prefix);
        }
        toImport.getDocumentElement().setAttribute("xmlns:" + tagName, namespace);
        return importElement(fieldElement, toImport.getDocumentElement(), namespace, prefix);
    }

    private Element importElement(Element parent, Element toImport, String namespace, String prefix) {
        // first create the Element
        Element current = parent.getOwnerDocument().createElementNS(namespace, prefix + toImport.getTagName());
        // add all the attributs..
        NamedNodeMap namednodes = toImport.getAttributes();        
        for(int i=0; i < namednodes.getLength(); i++) {
            Node namesnode = namednodes.item(i);
            switch(namesnode.getNodeType()) {
                case Node.ATTRIBUTE_NODE:
                    Attr attr = (Attr)namesnode;
                    if(attr.getNamespaceURI() == null) {
                        String name = attr.getName();
                        // when there is a : inside the name, assume that it _should_ be a namespace :p
                        if(name.indexOf(':') != -1) {
                            // we have somekinda namespace thingie...
                            current.setAttribute(attr.getName(), attr.getValue());
                        }
                        else {
                            current.setAttributeNS(namespace, prefix + attr.getName(), attr.getValue());
                        }                        
                    }
                    else {
                        current.setAttribute(attr.getName(), attr.getValue());
                    }                    
                break;
                case Node.CDATA_SECTION_NODE:
                case Node.COMMENT_NODE:
                case Node.ELEMENT_NODE:
                case Node.TEXT_NODE:
                case Node.DOCUMENT_FRAGMENT_NODE :
                case Node.DOCUMENT_NODE:
                case Node.DOCUMENT_TYPE_NODE:
                case Node.ENTITY_NODE:
                case Node.ENTITY_REFERENCE_NODE:
                case Node.NOTATION_NODE:
                case Node.PROCESSING_INSTRUCTION_NODE:
                    throw new RuntimeException("type #" + namesnode.getNodeType() +"not implemented is not implemented");
                default:
                    throw new RuntimeException("type #" + namesnode.getNodeType() + "was unknown!");
            }
        }
        // add to the parent
        parent.appendChild(current);        
        
        // add all the childnodes...
        Node childnode = toImport.getFirstChild();
        while(childnode != null) {
             switch(childnode.getNodeType()) {              
                case Node.CDATA_SECTION_NODE:
                    // throw new RuntimeException("not implemented");
                    CDATASection cdata = current.getOwnerDocument().createCDATASection(((CDATASection)childnode).getData());
                    current.appendChild(cdata);
                break;
                case Node.COMMENT_NODE:
                    Comment comment = current.getOwnerDocument().createComment(((Comment)childnode).getData());
                    current.appendChild(comment);
                break;                    
                case Node.ELEMENT_NODE:
                    importElement(current, (Element)childnode, namespace, prefix);
                break;
                case Node.TEXT_NODE:
                    Text text = current.getOwnerDocument().createTextNode(((Text)childnode).getData()); 
                    current.appendChild(text);
                break;
                
                case Node.ATTRIBUTE_NODE:
                case Node.DOCUMENT_FRAGMENT_NODE :
                case Node.DOCUMENT_NODE:
                case Node.DOCUMENT_TYPE_NODE:
                case Node.ENTITY_NODE:
                case Node.ENTITY_REFERENCE_NODE:
                case Node.NOTATION_NODE:
                case Node.PROCESSING_INSTRUCTION_NODE:
                    throw new RuntimeException("type #" + childnode.getNodeType() +"not implemented is not implemented");
                default:
                    throw new RuntimeException("type #" + childnode.getNodeType() + "was unknown!");
            }
            // go to nextone..
            childnode = childnode.getNextSibling();
        }
        // return the Element...
        return current;
    }
    
    private String getFieldFormat(org.mmbase.bridge.Node node, Field field) {
        switch (field.getType()) {
            case Field.TYPE_XML:
                return "xml";
            case Field.TYPE_STRING:
                return "string";
            case Field.TYPE_NODE:
                return "object";  // better would be "node" ?
            case Field.TYPE_INTEGER:
            case Field.TYPE_LONG:
                // was it a builder?
                String fieldName = field.getName();
                String guiType = field.getGUIType();

                // I want a object database type!
                if(fieldName.equals("otype")
                || fieldName.equals("number")                
                || fieldName.equals("snumber")
                || fieldName.equals("dnumber")
                || fieldName.equals("rnumber")
                || fieldName.equals("role")
                || guiType.equals("reldefs")) {
                    return "object";  // better would be "node" ?
                }
                if(guiType.equals("eventtime")) {
                    return "date";
                }
            case Field.TYPE_FLOAT:
            case Field.TYPE_DOUBLE:
                return "numeric";
            case Field.TYPE_BYTE:
                return "bytes";
            default:
                throw new RuntimeException("could not find field-type for:" + field.getType() + " for field: " + field);
        }
    }

    private Element createRelationEntry(Relation relation, org.mmbase.bridge.Node relatedNode) {
        Element fieldElement = document.createElement("relation");
        // we have to know what the relation type was...
        org.mmbase.bridge.Node reldef = cloud.getNode(relation.getStringValue("rnumber"));

        fieldElement.setAttribute("object", "" + relation.getNumber());

        if(relation.getSource().getNumber() ==  relatedNode.getNumber()) {
            fieldElement.setAttribute("role", reldef.getStringValue("sname"));
            fieldElement.setAttribute("related", "" + relation.getDestination().getNumber());
            fieldElement.setAttribute("type", "source");
        }
        else {
            fieldElement.setAttribute("role", reldef.getStringValue("dname"));
            fieldElement.setAttribute("related", "" + relation.getSource().getNumber());
            fieldElement.setAttribute("type", "destination");
        }
        return fieldElement;
    }    
}
