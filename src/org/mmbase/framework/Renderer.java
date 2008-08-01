/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.framework;
import java.io.*;
import java.net.URI;
import org.mmbase.util.functions.*;

/**
 * A Renderer renders a certain aspect of a {@link Block}. Currently every block has two renderers,
 * which are identified by the renderer 'type' (see {@link #getType }). Every block also has a
 * {@link Processor}, which is similar to a Renderer, but a processor never generates content, only
 * handles interaction.
 *
 * A Renderer is stateless.
 *
 * @author Michiel Meeuwissen
 * @version $Id: Renderer.java,v 1.20 2008-08-01 16:30:01 michiel Exp $
 * @since MMBase-1.9
 */
public interface Renderer {

    /**
     * Every block can be in a certain window state, which could be considered during rendering.
     */

    enum WindowState {
        /**
         * Rendering may suppose a full browser window
         */
        MAXIMIZED,
        /**
         * Rendering should suppose only a 'link' version from the component.
         */
        MINIMIZED,
        /**
         * Rendering may suppose quite a large area, but should be aware that other blocks are in a
         * similar state.
         */
        NORMAL;
    }

    enum Type {
        /**
         * Not yet rendering
         */
        NOT,
        /**
         * Rendering for 'HEAD' typically happens in the &lt;head&gt; block of HTML, and can
         * e.g. produce links to javascript.
         */
        HEAD,
       /**
        * A body typed renderer renders the actual content of a block. It should produce a &lt;div&gt;
        */
        BODY;

        /**
         * Returns a renderer that does nothing.
         */
        public Renderer getEmpty(final Block block) {
            return new Renderer() {
                public Type getType() { return Type.this; }
                public void render(Parameters parameters, Parameters urlparameters, Writer w, WindowState state) { };
                public Parameter[] getParameters() { return Parameter.emptyArray(); };
                public Block getBlock() { return block ; };
                public String toString() { return "EMPTY Renderer"; }
                public URI getUri() { try {return new URI("mmbase:/renderer/" + Type.this + "/empty");} catch (Exception e) { return null;} }
                public boolean equals(Object o) {
                    if (o instanceof Renderer) {
                        Renderer r = (Renderer) o;
                        return getUri().equals(r.getUri()) && getBlock().equals(r.getBlock());
                    } else {
                        return false;
                    }
                }
            };
        }
    }

    /**
     * Describes what kind of renderer this is
     */
    Type getType();

    /**
     * Every renderer renders for a certain block.
     */
    Block getBlock();

    /**
     * A renderer may need certain parameters. These are added to the block-parameters. This method
     * is called on instantation of the renderer.
     */
    Parameter[] getParameters();

    /**
     * Renders to a writer. In case of e.g. a JSPView, the parameters must also contain
     * the Http Servlet response and request, besided specific parameters for this component.
     */
    void render(Parameters blockParameters, Parameters frameworkParameters, Writer w, WindowState state) throws FrameworkException;


    /**
     * An URI which may identify the implementation of this Renderer.
     */

    URI getUri();
}
