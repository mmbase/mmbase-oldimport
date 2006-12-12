/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package com.finalist.cmsc.services.sitemanagement;

import java.io.Serializable;
import java.util.List;

import net.sf.mmapps.commons.beans.MMBaseNodeMapper;

import org.mmbase.bridge.Node;

import com.finalist.cmsc.beans.om.View;
import com.finalist.cmsc.navigation.PortletUtil;


public class ViewCacheEntryFactory extends MMBaseCacheEntryFactory {

    public ViewCacheEntryFactory() {
        super(PortletUtil.VIEW);
    }

    protected Serializable loadEntry(Serializable key) throws Exception {
        Node viewNode = getNode(key);
        if (viewNode == null) {
            return null;
        }

        View view = (View) MMBaseNodeMapper.copyNode(viewNode, View.class);
        List<String> types = PortletUtil.getAllowedTypes(viewNode);
        for (String type : types) {
            view.addContenttype(type);
        }
        return view;
    }

}
