package com.finalist.cmsc.portalImpl.headerresource;

import net.sf.mmapps.commons.util.XmlUtil;

public class MetaHeaderResource extends HeaderResource {

	/**
	 * <meta name="name" content="content" lang="lang" http-equiv="httpEquiv"/>
	 */
	
	private String name;
	private String content;
	private String lang;
	private String httpEquiv;

	public MetaHeaderResource(boolean dublin, String name, String content, String lang, String httpEquiv) {
		super(dublin);
		this.name = name;
		this.content = content;
		this.lang = lang;
		this.httpEquiv = httpEquiv;
	}


	public MetaHeaderResource(boolean dublin, String name, String content) {
		this(dublin, name, content, null, null);
	}

	public void render(StringBuffer buffer) {
        buffer.append("<meta name=\"");
        if(isDublin()) {
        	buffer.append("DC.");
        }
        buffer.append(name);
        buffer.append("\" content=\"");
        buffer.append((content == null)?"":XmlUtil.xmlEscape(content));
        buffer.append("\"");
        if (lang != null) {
            buffer.append(" lang=\"");
            buffer.append(lang);
            buffer.append("\"");
        }
        if (httpEquiv != null) {
            buffer.append(" http-equiv=\"");
            buffer.append(httpEquiv);
            buffer.append("\"");
        }
        buffer.append("/>");
	}
	
	public String toString() {
		return "meta_"+(isDublin()?"DC.":".")+name;
	}
}
