/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.framework;
import java.util.*;
import org.mmbase.util.*;
import java.io.*;
import javax.servlet.http.HttpServletRequest;
import org.mmbase.util.functions.*;
import org.mmbase.util.transformers.Url;
import org.mmbase.util.transformers.CharTransformer;
import org.mmbase.util.logging.Logger;
import org.mmbase.util.logging.Logging;
import org.mmbase.bridge.Node;
import org.mmbase.bridge.Cloud;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

/**
 * The URLConverter which deals with urls in /mmbase
 *
 *
 * @author Michiel Meeuwissen
 * @version $Id: MMBaseUrlConverter.java,v 1.7 2007-07-30 16:36:05 michiel Exp $
 * @since MMBase-1.9
 */
public class MMBaseUrlConverter implements UrlConverter {
    private static final Logger log = Logging.getLoggerInstance(MMBaseUrlConverter.class);

    /**
     * MMBaseUrlConverter points to a jsp which renders 1 block. This parameter indicates of which component.
     */
    public static final Parameter<String> COMPONENT = new Parameter<String>("component", String.class);

    /**
     * MMBaseUrlConverter points to a jsp which renders 1 block. This parameter indicates its name.
     */
    public static final Parameter<String> BLOCK     = new Parameter<String>("block", String.class);

    /**
     * MMBaseUrlConverter wants a 'category'.
     */
    public static final Parameter<String> CATEGORY  = new Parameter<String>("category", String.class);


    private final Framework framework;

    protected String dir = "/mmbase/";

    public MMBaseUrlConverter(Framework fw) {
        framework = fw;
    }

    public void setDir(String d) {
        dir = d;
    }

    public Parameter[] getParameterDefinition() {
        return new Parameter[] {CATEGORY, COMPONENT, BLOCK};
    }

    public StringBuilder getUrl(String path,
                                Collection<Map.Entry<String, Object>> parameters,
                                Parameters frameworkParameters, boolean escapeAmps) {
        if (log.isDebugEnabled()) {
            log.debug(" framework parameters " + frameworkParameters);
        }
        HttpServletRequest request = frameworkParameters.get(Parameter.REQUEST);
        State state = State.getState(request);

        String category = frameworkParameters.get(CATEGORY);
        if (category == null && state.isRendering()) {
            category = state.getFrameworkParameters().get(CATEGORY);
        }

        // MMBase urls always shows only one block
        Component component  = ComponentRepository.getInstance().getComponent(frameworkParameters.get(COMPONENT));
        if (component == null) {
            // if no explicit component specified, suppose current component, if there is one:
            if (state.isRendering()) {
                component = state.getBlock().getComponent();
            } else {
                log.debug("No rendering state object found, so no current component.");
                if (category != null) {
                    log.debug("Found category " + category);
                    return new StringBuilder(dir + category);
                } else {
                    return null;
                }
            }
        }

        assert component != null;

        boolean filteredMode = request.getServletPath().startsWith(dir);


        Map<String, Object> map = new TreeMap<String, Object>();

        // now determin the block:
        Block block;
        String blockParam = frameworkParameters.get(BLOCK);
        if (blockParam != null) {
            if (log.isDebugEnabled()) {
                log.debug("found block " + blockParam + " trying it on " + component);
            }

            if (path != null && ! "".equals(path)) throw new IllegalArgumentException("Cannot use both 'path' argument and 'block' parameter");
            block = component.getBlock(blockParam);
            if (block == null) throw new IllegalArgumentException("No block '" + blockParam + "' found in component '" + component + "'");
        } else {
            block = component.getBlock(path);
            if (block != null) {
                if (! filteredMode) {
                    path = null; // used, determin path with block name
                }
            } else {
                // no such block
                if (path != null && ! "".equals(path)) {
                    log.debug("No block '" + path + "' found");
                    return null;
                }

            }
            if (block == null) {
                if(state.isRendering()) {
                    // current block
                    block = state.getRenderer().getBlock();
                } else {
                    // default block
                    block = component.getDefaultBlock();
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Creating URL to component " + component + " generating URL to " + block + " State " + state + " category " + category);
        }
        boolean processUrl = Boolean.TRUE.equals(frameworkParameters.get("process"));
        if (processUrl) {
            // get current compoennts ids
            if (state.isRendering()) {
                map.put("action", state.getId());
            } else {
                log.warn("Needing state, but no state found ");
            }
        }


        if (! processUrl && state.isRendering()) {
            // copy all current parameters of the request.
            for (Object e : request.getParameterMap().entrySet()) {
                Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>) e;
                String k = entry.getKey();
                if (k.equals(BLOCK.getName())) continue;
                if (k.equals(COMPONENT.getName())) continue;
                if (k.equals(CATEGORY.getName())) continue;
                map.put(k, entry.getValue()[0]);
            }
        } else {
            //log.debug("Now processing " + processor);
        }

        if (! processUrl) {
            Parameters blockParameters = block.createParameters();
            for (Map.Entry<String, Object> entry : parameters) {
                blockParameters.set(entry.getKey(), entry.getValue());
            }
            map.putAll(framework.prefix(state, blockParameters.toMap()));
        }

        if (category == null) {
            Block.Type[] classification = block.getClassification();
        }
        //boolean subComponent = state.getDepth() > 0;

       
        String page;
        if (state.isRendering() && state.getBlock().equals(block)) {
            page = FrameworkFilter.getPath(request);
        } else {
            page = dir + (category == null ? "_" : category) + "/" + component.getName() + "/" + block.getName() ;
        }

        //path == null || subComponent ? 

        StringBuilder sb = BasicUrlConverter.getUrl(page, map.entrySet(), request, escapeAmps);
        return sb;

    }
    public StringBuilder getInternalUrl(String page, Collection<Map.Entry<String, Object>> params, Parameters frameworkParameters) {
        HttpServletRequest request = frameworkParameters.get(Parameter.REQUEST);
        if (page == null) throw new IllegalArgumentException();
        if (page.startsWith(dir)) {
            String sp = FrameworkFilter.getPath(request);
            String[] path = sp.split("/");
            if (log.isDebugEnabled()) {
                log.debug("Going to filter " + Arrays.asList(path));
            }
            if (path.length >= 3) {
                assert path[0].equals("");
                assert path[1].equals(dir.split("/")[1]);
                String category = path[2];
                if (! category.equals("_")) {
                    boolean categoryOk = false;
                    for (Block.Type rootType : ComponentRepository.getInstance().getBlockClassification("mmbase")[0].getSubTypes()) {
                        categoryOk = rootType.getName().equals(category);
                        if (categoryOk) break;
                    }
                    if (! categoryOk) {
                        log.debug("No such component clasification, ignoring this");
                        return null;
                    }
                }

                StringBuilder url = new StringBuilder("/mmbase/admin/index.jsp?category=");
                url.append(category);

                if (path.length == 3) return url;

                Component comp = ComponentRepository.getInstance().getComponent(path[3]);
                if (comp == null) {
                    log.debug("No such component, ignoring this too");
                    return null;
                }
                url.append("&component=").append(comp.getName());

                if (path.length == 4) return url;

                Block block = comp.getBlock(path[4]);
                if (log.isDebugEnabled()) {
                    log.debug("Will try to display " + block);
                }
                if (block == null) {
                    log.debug("No block " + path[4] + " in component " + path[3]);
                    return null;

                }
                url.append("&block=").append(block.getName());
                if (log.isDebugEnabled()) {
                    log.debug("internal URL " + url);
                }
                return url;
            } else {
                log.debug("path length " + path.length);
                return null;
            }
        } else {
            log.debug("Leaving unfiltered");
            return null;
        }
    }

    public String toString() {
        return dir;
    }

}
