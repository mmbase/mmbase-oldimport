/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.bridge.util.fields;

import org.mmbase.bridge.*;

import org.mmbase.util.transformers.*;
import org.mmbase.util.xml.DocumentReader;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;

import org.mmbase.util.*;
import java.net.*;

import java.util.*;

/**
 * This class contains the static methods implementing 'value interception' of the set- and get-
 * methods of {@link org.mmbase.bridge.Node}.
 * 
 * It reads (on static init) 'fieldtypedefinitions.xml' to know which 
 * {@link org.mmbase.bridge.util.fields.Processor}(s)  and {@link org.mmbase.birdge.util.field.CommitProcessor}(s)
 * must be linked to which combinations of field-type and 'specialization'.
 * 
 * @todo The concept of 'specialization' ~= 'guitype' seems to be 'datatype name' now.
 *
 * @author Michiel Meeuwissen
 * @version $Id: ValueIntercepter.java,v 1.22 2005-07-22 09:13:39 michiel Exp $
 * @since MMBase-1.7
 */

public class ValueIntercepter {

    public static final String PUBLIC_ID_FIELD_TYPE_DEFINITIONS_0_1 = "-//MMBase//DTD fieldtypedefinitions 0.1//EN";
    public static final String DTD_FIELD_TYPE_DEFINITIONS_0_1 = "fieldtypedefinitions_0_1.dtd";

    public static final String PUBLIC_ID_FIELD_TYPE_DEFINITIONS = PUBLIC_ID_FIELD_TYPE_DEFINITIONS_0_1;
    public static final String DTD_FIELD_TYPE_DEFINITIONS = DTD_FIELD_TYPE_DEFINITIONS_0_1;

    public static final String XML_FIELD_TYPE_DEFINITIONS = "resources/fieldtypedefinitions.xml";

    private static Logger log;

    /*
      for reference by humans only:
    private static final int NUMBER_OF_FIELDTYPES = 8;

    public final static int TYPE_STRING  = 1;
    public final static int TYPE_INTEGER = 2;
    public final static int.TYPE_BINARY    = 4;
    public final static int TYPE_FLOAT   = 5;
    public final static int TYPE_DOUBLE  = 6;
    public final static int TYPE_LONG    = 7;
    public final static int TYPE_XML     = 8;
    public final static int TYPE_NODE    = 9;
    public final static int TYPE_DATETIME    = 10;
    public final static int TYPE_BOOLEAN    = 11;
    */

    private static  Processor[][] defaultSetProcessor;

    /**
     * @since MMBase-1.8
     */
    private static  CommitProcessor[] defaultCommitProcessor;

    private static  Map[][] setProcessor;

    /**
     * @since MMBase-1.8
     */
    private static  Map[] commitProcessor;

    private static  Processor[][] defaultGetProcessor;

    private static  Map[][] getProcessor;


    /**
     * @since MMBase-1.8
     */
    private static void initMaps() {
        defaultSetProcessor = new Processor[][] {
            //set  setString setInteger ...
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 0=object */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 1=string */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 2=integer */
            null,                                                                     /* 3 not used */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 4=byte */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 5=float */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 6=double */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 7=long */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 8=xml */
            {null, null, null, null, null, null, null, null, null, null, null, null, null},  /* 9=node */
            {null, null, null, null, null, null, null, null, null, null, null, null, null},  /* 10=datetime */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 11=boolean */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}  /* 12=list */
        };

        defaultCommitProcessor = new CommitProcessor[] {
            null,  /* 0=not used */
            null,  /* 1=string */
            null,  /* 2=integer */
            null,  /* 3=not used */
            null,  /* 4=byte */
            null,  /* 5=float */
            null,  /* 6=double */
            null,  /* 7=long */
            null,  /* 8=xml */
            null,  /* 9=node */
            null,  /* 10=datetime */
            null,  /* 11=boolean */
            null   /* 12=list */
        };


        setProcessor = new Map[][] { // maps with guiType -> Processor
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 0=object */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 1=string */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 2=integer */
            null,                                                                     /* 3 not used */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 4=byte */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 5=float */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 6=double */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 7=long */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 8=xml */
            {null, null, null, null, null, null, null, null, null, null, null, null, null},  /* 9=node */
            {null, null, null, null, null, null, null, null, null, null, null, null, null},  /* 10=datetime */
            {null, null, null, null, null, null, null, null, null, null, null, null, null},  /* 11=boolean */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}  /* 12=list */
        };


        commitProcessor = new Map[] {
            null,  /* 0=not used */
            null,  /* 1=string */
            null,  /* 2=integer */
            null,  /* 3=not used */
            null,  /* 4=byte */
            null,  /* 5=float */
            null,  /* 6=double */
            null,  /* 7=long */
            null,  /* 8=xml */
            null,   /* 9=node */
            null,   /* 10=datetime */
            null,   /* 11=boolean */
            null   /* 12=boolean */
        };

        defaultGetProcessor = new Processor[][] {
            //set  setString setInteger ...
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 0=object */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 1=string */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 2=integer */
            null,                                                                     /* 3 not used */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 4=byte */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 5=float */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 6=double */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 7=long */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 8=xml */
            {null, null, null, null, null, null, null, null, null, null, null, null, null},  /* 9=node */
            {null, null, null, null, null, null, null, null, null, null, null, null, null},  /* 10=datetime */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 11=boolean */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}  /* 12=list */
        };
        getProcessor = new Map[][] { // maps with guiType -> Processor
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 0=object */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 1=string */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 2=integer */
            null,                                                                     /* 3 not used */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 4=byte */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 5=float */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 6=double */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 7=long */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}, /* 8=xml */
            {null, null, null, null, null, null, null, null, null, null, null, null, null},  /* 9=node */
            {null, null, null, null, null, null, null, null, null, null, null, null, null},  /* 10=datetime */
            {null, null, null, null, null, null, null, null, null, null, null, null, null},  /* 11=boolean */
            {null, null, null, null, null, null, null, null, null, null, null, null, null}  /* 12=list */
        };
    }

    static {
        log = Logging.getLoggerInstance(ValueIntercepter.class);
        XMLEntityResolver.registerPublicID(PUBLIC_ID_FIELD_TYPE_DEFINITIONS, DTD_FIELD_TYPE_DEFINITIONS, ValueIntercepter.class);
        // read the XML
        try {

            // XXX location of resource will probably change?
            ResourceWatcher watcher = new ResourceWatcher(ResourceLoader.getConfigurationRoot()) {
                    public void onChange(String resource) {
                        initMaps();
                        readFieldDefinitions(getResourceLoader(), resource);
                    }
                };
            watcher.add("fieldtypedefinitions.xml");
            watcher.start();
            watcher.onChange("fieldtypedefinitions.xml");
        } catch (Throwable t) {
            log.error(t.getClass().getName() + ": " + Logging.stackTrace(t));
        }
    }

    protected static int getType(final String s) {
        if (s.equals("object")) return 0;
        return org.mmbase.core.util.Fields.getType(s);
    }

    private static Object createProcessor(DocumentReader reader, Element processorElement) {
        Object processor = null;
        
        for (Iterator classes = reader.getChildElements(processorElement, "class"); classes.hasNext();) {
            Element clazElement = (Element) classes.next();
            String clazString = reader.getElementValue(reader.getElementByPath(clazElement, "class"));
            try {
                Class claz = Class.forName(clazString);
                Object newProcessor = null;
                if (CharTransformer.class.isAssignableFrom(claz)) {
                    CharTransformer charTransformer = Transformers.getCharTransformer(clazString, null, " valueintercepter ", false);
                    if (charTransformer != null) {
                        newProcessor = new CharTransformerProcessor(charTransformer);
                    }
                } else if (Processor.class.isAssignableFrom(claz)) {
                    newProcessor = claz.newInstance();
                } else {
                    log.error("Found class " + clazString + " is not a Processor or a CharTransformer");
                }

                if (newProcessor != null) {
                    if (processor == null) {
                        processor = newProcessor;
                    } else if (processor instanceof ChainedProcessor) {
                        ((ChainedProcessor) processor).add(newProcessor);
                    } else {
                        ChainedProcessor chain = new ChainedProcessor();
                        chain.add(processor);
                        chain.add(newProcessor);
                        processor = chain;
                    }
                }
            } catch (ClassNotFoundException ex) {
                log.error("Class " + clazString + " could not be found");
            } catch (IllegalAccessException iae) {
                log.error("Class " + clazString + " may  not be instantiated");
            } catch (InstantiationException ie) {
                log.error("Class " + clazString + " can not be instantiated");
            }
        }
        return processor;
    }


    /**
     * Initialize the type handlers defaultly supported by the system, plus those configured in WEB-INF/config.
     */
    private static void readFieldDefinitions(ResourceLoader loader, String resource) {
        // backwards compatibilty
        InputSource fieldTypes = new InputSource(ValueIntercepter.class.getResourceAsStream(XML_FIELD_TYPE_DEFINITIONS));
        readFieldDefinitions(fieldTypes);

        List resources = loader.getResourceList(resource);
        log.info("Using " + resources);
        ListIterator i = resources.listIterator();
        while (i.hasNext()) i.next();
        while (i.hasPrevious()) {
            try {
                URL u = (URL) i.previous();
                log.info("Reading " + u);
                URLConnection con = u.openConnection();
                if (con.getDoInput()) {
                    InputSource source = new InputSource(con.getInputStream());
                    readFieldDefinitions(source);
                }
            } catch (Exception e) {
                log.error(e);
            }
        }


    }

    /**
     * Obtains a map in an array. If the map is not present, a map is instantiated and added.
     * @param processorMaps Maps an array of maps. The index of the array is a fieldtype, such as {@link Field.TYPE_STRING}
     * @param fieldType the index in the array
     */
    private static Map getProcessorMap(Map[] processorMaps, int fieldType) {
        if (processorMaps[fieldType] == null) {
            processorMaps[fieldType] = new HashMap();
        }
        return processorMaps[fieldType];
    }

    /**
     * Adds a processor for a gui type to the maps in a fieldtype-index array, depending on the type passed.
     * Creates new maps if needed.
     * @param processor the processor to add
     * @param processor Maps an array of maps. The index of the array is a fieldtype, such as {@link Field.TYPE_STRING}
     * @param guiType the gui type that serves as the key to the processor when adding it to a map
     * @param type the type of field to which this processor should be added.
     *                this is either a type name (i.e. "STRING"), an empty string (""), or a wildcard ("*").
     *                An empty settype adds the processor to all types except the 'object' type.
     *                A wildcard adds the processor to all types, including the 'object' type.
     * @since MMBase-1.8
     */
    private static void addProcessorToFieldSet(Processor processor, Map[] processorMaps, String guiType, String type) {
        if (type.equals("") || type.equals("*")) {
            if (type.equals("*")) {
                getProcessorMap(processorMaps, 0).put(guiType, processor);
            }
            // set for all except 'object'.
            getProcessorMap(processorMaps, Field.TYPE_STRING).put(guiType, processor);
            getProcessorMap(processorMaps, Field.TYPE_INTEGER).put(guiType, processor);
            getProcessorMap(processorMaps, Field.TYPE_BINARY).put(guiType, processor);
            getProcessorMap(processorMaps, Field.TYPE_FLOAT).put(guiType, processor);
            getProcessorMap(processorMaps, Field.TYPE_DOUBLE).put(guiType, processor);
            getProcessorMap(processorMaps, Field.TYPE_LONG).put(guiType, processor);
            getProcessorMap(processorMaps, Field.TYPE_XML).put(guiType, processor);
            getProcessorMap(processorMaps, Field.TYPE_NODE).put(guiType, processor);
            getProcessorMap(processorMaps, Field.TYPE_DATETIME).put(guiType, processor);
            getProcessorMap(processorMaps, Field.TYPE_BOOLEAN).put(guiType, processor);
            getProcessorMap(processorMaps, Field.TYPE_LIST).put(guiType, processor);
        } else {
            getProcessorMap(processorMaps, getType(type)).put(guiType, processor);
        }
    }

    /**
     * Sets the given processor in the correct location of the given processors array, using the
     * given type.  The array is a one dimensional array which will only be used for providing
     * defaults, the 'real' arrays are two-dimensional.  If the type is '*' the processor wil be set
     * for all types (even for 'object'), if the type is '', then for all but object.
     * @param processor The processor to be remembered
     * @param processors The processor array in which to store.
     * @param type       For which type.
     
     * @since MMBase-1.8
     */
    private static void setDefaultProcessor(Processor processor, Processor[] processors, String type) {
        if (type.equals("") || type.equals("*")) {
            if (type.equals("*")) {
                processors[0] = processor;
            }
            // set for all except 'object'.
            processors[Field.TYPE_STRING] = processor;
            processors[Field.TYPE_INTEGER] = processor;
            processors[Field.TYPE_BINARY] = processor;
            processors[Field.TYPE_FLOAT] = processor;
            processors[Field.TYPE_DOUBLE] = processor;
            processors[Field.TYPE_LONG] = processor;
            processors[Field.TYPE_XML] = processor;
            processors[Field.TYPE_NODE] = processor;
            processors[Field.TYPE_DATETIME] = processor;
            processors[Field.TYPE_BOOLEAN] = processor;
            processors[Field.TYPE_LIST] = processor;
        } else {
            processors[getType(type)] = processor;
        }
    }

    /**
     * Initialize the type handlers default supported by the system.
     * @since MMBase-1.8
     */
    private static void readFieldDefinitions(InputSource fieldTypes) {
        if (fieldTypes.getSystemId() == null) {
            String name = ValueIntercepter.class.getName();
            int dot = name.lastIndexOf('.');
            if (dot > 0) {
                name = name.substring(0, dot);
            }
            fieldTypes.setSystemId("resource:" + name + "/" + XML_FIELD_TYPE_DEFINITIONS); // I've honestly no idea what it should be, but this is at least fit for humans (in case of errors)
        }
        log.service("Reading fieldtype-definitions from " + fieldTypes.getSystemId());
        DocumentReader reader  = new DocumentReader(fieldTypes, ValueIntercepter.class);

        Element fieldtypesElement = reader.getElementByPath("fieldtypedefinitions");
        for (Iterator typeIter = reader.getChildElements(fieldtypesElement, "fieldtype"); typeIter.hasNext();) {
            Element typeElement = (Element) typeIter.next();
            String typeString = typeElement.getAttribute("id");
            int fieldType =  getType(typeString);


            // commit processor for no specialization
            for (Iterator commitProcessorIter = reader.getChildElements(typeElement, "commitprocessor"); commitProcessorIter.hasNext();) {
                Element commitProcessorElement = (Element) commitProcessorIter.next();
                CommitProcessor newProcessor = (CommitProcessor) createProcessor(reader, commitProcessorElement);
                defaultCommitProcessor[fieldType] = newProcessor;
            }

            // fill the set-processor for no, or unknown, guitype
            for (Iterator setProcessorIter = reader.getChildElements(typeElement, "setprocessor"); setProcessorIter.hasNext();) {
                Element setProcessorElement = (Element) setProcessorIter.next();
                String setTypeString = setProcessorElement.getAttribute("type");
                Processor newProcessor = (Processor) createProcessor(reader, setProcessorElement);
                setDefaultProcessor(newProcessor, defaultSetProcessor[fieldType], setTypeString);
                log.service("Defined for field type " + typeString + "/DEFAULT setprocessor (" + setTypeString + ")" + newProcessor);
            }

            for (Iterator getProcessorIter = reader.getChildElements(typeElement, "getprocessor"); getProcessorIter.hasNext();) {
                Element getProcessorElement = (Element) getProcessorIter.next();
                String getTypeString = getProcessorElement.getAttribute("type");
                Processor newProcessor = (Processor) createProcessor(reader, getProcessorElement);
                setDefaultProcessor(newProcessor, defaultGetProcessor[fieldType], getTypeString);
                log.service("Defined for field type " + typeString + "/DEFAULT getprocessor (" + getTypeString + ")" + newProcessor);
            }

            // now for known guitypes (specializations)
            for (Iterator specializationIter = reader.getChildElements(typeElement, "specialization"); specializationIter.hasNext();) {
                Element specializationElement = (Element) specializationIter.next();
                String guiType = specializationElement.getAttribute("id");

                // commit processor for this specialization
                for (Iterator commitProcessorIter = reader.getChildElements(specializationElement, "commitprocessor"); commitProcessorIter.hasNext();) {
                    Element commitProcessorElement = (Element) commitProcessorIter.next();
                    CommitProcessor newProcessor = (CommitProcessor) createProcessor(reader, commitProcessorElement);

                    Map commitMap = commitProcessor[fieldType];
                    if (commitMap == null) {
                        commitMap = new HashMap();
                        commitProcessor[fieldType] = commitMap;
                    }
                    commitMap.put(guiType, newProcessor);
                }

                // fill the set-processor for this guitype
                for (Iterator setProcessorIter = reader.getChildElements(specializationElement, "setprocessor"); setProcessorIter.hasNext();) {
                    Element setProcessorElement = (Element) setProcessorIter.next();
                    String setTypeString = setProcessorElement.getAttribute("type");
                    Processor newProcessor = (Processor) createProcessor(reader, setProcessorElement);
                    addProcessorToFieldSet(newProcessor, setProcessor[fieldType], guiType, setTypeString);
                    log.service("Defined for field type " + typeString + "/" + guiType + " setprocessor(" + ("".equals(setTypeString) ? "ALL TYPES" : setTypeString)  + ") " + newProcessor);
                }

                for (Iterator getProcessorIter = reader.getChildElements(specializationElement, "getprocessor"); getProcessorIter.hasNext();) {
                    Element getProcessorElement = (Element) getProcessorIter.next();
                    String getTypeString = getProcessorElement.getAttribute("type");
                    Processor newProcessor = (Processor) createProcessor(reader, getProcessorElement);
                    addProcessorToFieldSet(newProcessor, getProcessor[fieldType], guiType, getTypeString);
                    log.service("Defined for field type " + typeString + "/" + guiType + " getprocessor(" + ("".equals(getTypeString) ? "ALL TYPES" : getTypeString)  + ") " + newProcessor);
                }
            }
        }
    }

    protected static final Processor getDefaultSetProcessor(final int setType, final int type) {
        Processor processor = defaultSetProcessor[type][setType];
        if (processor == null) {
            processor = defaultSetProcessor[type][0];
        }
        return processor;
        //log.info("default " + processor);
    }

    protected static final Processor getDefaultGetProcessor(final int setType, final int type) {
        Processor processor = defaultGetProcessor[type][setType];
        if (processor == null) {
            processor = defaultGetProcessor[type][0];
        }
        return processor;
        //log.info("default " + processor);
    }


    /**
     * @since MMBase-1.8
     */
    public static final void  commit(final Node node, final Field field) {
        if (log.isDebugEnabled()) {
            log.debug("Committing for field " + field.getName() + node.getNumber());
        }
        int type = field.getType();

        if (type == Field.TYPE_UNKNOWN) {
            if (node.getNumber() != -1) {
                log.warn("TYPE UNKNOWN commit " + type + " " + field.getName() + " " + field.getGUIType() + " for node " + node.getNumber());
            }
            return;
        }

        CommitProcessor processor;
        Map map = commitProcessor[type];

        if (map == null) { // if that too not configured
            processor = defaultCommitProcessor[type];
        } else {
            String guiType = field.getGUIType();
            processor = (CommitProcessor) map.get(guiType);
            if (processor == null) {
                processor = defaultCommitProcessor[type];
            } else {
            }
        }
        if (processor != null) {
            processor.commit(node, field);
        }

    }

    public static final Object processSet(final int setType, final Node node, final Field field, final  Object value) {
        int type = field.getType();
        if (type == Field.TYPE_UNKNOWN) {
            if (node.getNumber() != -1) {
                log.warn("TYPE UNKNOWN processSet " + setType + "/" + type + " " + field.getName() + " " + field.getGUIType() + " for node " + node.getNumber());
            }
            return value;
        }

        Processor processor;
        Map map = setProcessor[type][setType];

        if (map == null) { // not configured
            map = setProcessor[type][0];
        }
        if (map == null) { // if that too not configured
            processor = getDefaultSetProcessor(setType, type);
        } else {
            String guiType = field.getGUIType();
            processor = (Processor) map.get(guiType);
            if (processor == null) {
                processor = getDefaultSetProcessor(setType, type);
            } else {
            }
        }
        if (processor == null) return value;
        return processor.process(node, field, value);

    }

    public static final Object processGet(final int getType, final Node node, final Field field, Object value) {

        int type = field.getType();

        if (type == Field.TYPE_UNKNOWN) {
            if (node.getNumber() != -1) {
                log.warn("TYPE UNKNOWN processGet " + getType + "/" + type + " " + field.getName() + " " + field.getGUIType() + " for node " + node.getNumber());
            }
            return value;
        }

        Processor processor;
        Map map = getProcessor[type][getType];


        if (map == null) {  // not configured
            map = getProcessor[type][0];
        }
        if (map == null) { // if that too not configured
            processor = getDefaultGetProcessor(getType, type);
        } else {
            String guiType = field.getGUIType();
            processor = (Processor) map.get(guiType);
            if (processor == null) {
                processor = getDefaultGetProcessor(getType, type);
            } else {
            }
        }
        if (processor == null) return value;

        return processor.process(node, field, value);

    }


}
