/*

This software is OSI Certified Open Source Software.
OSI Certified is a certification mark of the Open Source Initiative.

The license (Mozilla version 1.0) can be read at the MMBase site.
See http://www.MMBase.org/license

*/
package org.mmbase.servlet;

import org.mmbase.module.core.*;
import org.mmbase.datatypes.processors.FormatFileSize;
import org.mmbase.util.transformers.*;
import java.util.*;

import javax.servlet.http.*;
import javax.servlet.*;
import java.util.regex.*;

import java.io.*;
import java.text.*;

import org.mmbase.util.logging.*;


/**
 * Straight-forward filter which can serve files from one directory (the directory 'files' in the
 * mmbase 'datadir') outside the web application root.
 *
 * @version $Id$
 * @author Michiel Meeuwissen
 * @since  MMBase-1.9
 * @see    AttachmentServlet
 */
public class FileServlet extends BridgeServlet {
    private static Logger log;

    private static final UrlEscaper URL = new UrlEscaper();
    private static final String SESSION_EXTENSION  = ".SESSION";

    private static FileServlet firstInstance;
    private File files = null;

    private Pattern ignore = Pattern.compile("");

    // implementation of CERN httpd meta-file processing. (like mod_cern_meta of apache)
    private String metaDir = ".web";
    private String metaSuffix = ".meta";
    private boolean metaFiles = true;

    private Comparator<File> comparator = null;


    //private static final Properties properties= new Properties();

    @Override
    public void init() throws ServletException {
        super.init();
        if (firstInstance == null) firstInstance = this;
        String ig = getInitParameter("ignore");
        if (ig != null && ig.length() > 0) {
            ignore = Pattern.compile(ig);
        }
        String comparatorClass = getInitParameter("comparator");
        if (comparatorClass != null && comparatorClass.length() > 0) {
            try {
                Class clazz = Class.forName(comparatorClass);
                comparator = (Comparator<File>) clazz.newInstance();
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    @Override
    public void setMMBase(MMBase mmb) {
        super.setMMBase(mmb);
        if (log == null) {
            log = Logging.getLoggerInstance(FileServlet.class);
        }
        if (files == null) {
            File dataDir = MMBase.getMMBase().getDataDir();

            files = new File(dataDir, "files");
            if (! files.exists()) {
                if (files.mkdirs()) {
                    log.info("Directory " + files + " was created");
                } else {
                    log.warn("Directory " + files + " could not be created");
                }
            }
            log.info("Using datadir " + files);
        }
    }


    @Override
    public String getServletInfo()  {
        return "Serves files from <MMBase data directory>/files";
    }

    @Override
    protected Map<String, Integer> getAssociations() {
        Map<String, Integer> a = super.getAssociations();
        a.put("files",      50);
        return a;
    }

    public static FileServlet getInstance() {
        return firstInstance;
    }
    public static File getDirectory() {
        return getInstance().files;
    }

    public static File getFile(String pathInfo, ServletResponse res) {
        if (pathInfo == null) {
            return getDirectory();
        } else {
            return new File(getDirectory(), URL.transformBack(pathInfo).replace("/", File.separator));
        }
    }

    protected static File getSessionFile(File f) {
        if (f.getName().endsWith(SESSION_EXTENSION)) return f;
        return new File(f.getParentFile(), f.getName() + SESSION_EXTENSION);
    }

    /**
     * @since MMBase-1.9.2
     */
    public static void protectFile(HttpServletRequest req, File f) throws IOException {
        File sessionFile = getSessionFile(f);
        Writer w = new FileWriter(sessionFile);
        w.write(req.getSession(true).getId());
        w.close();
    }


    /**
     * Returns whether the given file can be served out for the given request. You can use {@link
     * #protectFile} to make the file only accessible to the current http session.
     */
    protected boolean canRead(HttpServletRequest req, File f) {
        if (! f.canRead()) return false;

        // something with mmbase security ?
        File sessionFile = getSessionFile(f);
        if (sessionFile.exists()) {
            if (! sessionFile.canRead()) return true;
            try {
                BufferedReader r = new BufferedReader(new FileReader(sessionFile));
                String sessionId = r.readLine();
                r.close();
                return sessionId.equals(req.getSession(true).getId());
            } catch (IOException ioe) {
                log.warn(ioe);
                return false;
            }
        } else {
            return true;
        }
    }

    protected boolean ignores(String pi) {
        return pi != null && (ignore.matcher(pi).matches());
    }

    /**
     * FileServlet supports 'meta' files like Cern HTTPD (and apaches mod_cern_meta).
     */
    public  File getMetaFile(File f) {
        File webDir = new File(f.getParentFile(), metaDir);
        File metaFile = new File(webDir, f.getName() + metaSuffix);
        return metaFile;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pi = req.getPathInfo();
        if (ignores(pi)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "The file '" + pi + "' is explicitely ignored by the file servlet.");
            return;
        }

        File file = getFile(pi, resp);
        if (! file.exists()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "The file '" + pi + "' does not exist");
            log.debug("" + file + " does not exist");
            return;
        }
        if (! canRead(req, file)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "The file '" + pi + "' may not be read");
            log.debug("" + file + " may not be read");
            return;
        }
        if (file.isDirectory()) {
            listing(req, resp, file);
            return;
        }
        resp.setContentType(getServletContext().getMimeType(file.getName()));
        resp.setContentLength((int) file.length());
        if (metaFiles) {
            File metaFile = getMetaFile(file);
            if (metaFile.exists() && metaFile.canRead()) {
                BufferedReader r = new BufferedReader(new FileReader(metaFile));
                String line = r.readLine();
                while (line != null) {
                    line = line.trim();
                    if (! line.startsWith("#")) { // support for comments
                        String[] header = line.split("\\s+", 2);
                        if (header.length == 2) {
                            resp.setHeader(header[0], header[1]);
                        }
                    }
                    line = r.readLine();
                }
            }
        }
        BufferedOutputStream out = new BufferedOutputStream(resp.getOutputStream());
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        byte[] buf = new byte[1024];
        int b = 0;
        while ((b = in.read(buf)) != -1) {
            out.write(buf, 0, b);
        }
        out.flush();
        in.close();
        out.close();
    }


    private static final FormatFileSize formatFileSize = new FormatFileSize();

    private static final Xml XML = new Xml();
    protected void listing(HttpServletRequest req, HttpServletResponse resp, File directory) throws IOException {
        if ("true".equals(getInitParameter("listings"))) {
            resp.setContentType("application/xhtml+xml"); // We hate IE anyways.
            StringBuilder result = new StringBuilder();
            result.append("<?xml version='1.0'?>\n");
            result.append("<html xmlns='http://www.w3.org/1999/xhtml'>");
            result.append("<head>");
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || ! pathInfo.endsWith("/")) {
                resp.sendRedirect(req.getContextPath() + req.getServletPath() + (pathInfo == null ? "" : pathInfo) + "/");
                return;
            }
            result.append("<title>Directory Listing For " + XML.transform(URL.transformBack(pathInfo)) + "</title>");
            result.append("<link rel='stylesheet' href='" + req.getContextPath() + "/mmbase/style/css/mmbase.css' type='text/css' />");
            result.append("</head>");
            result.append("<body class='filelisting'>");
            result.append("<h1>Directory Listing For " + XML.transform(URL.transformBack(pathInfo)) + "</h1>");
            String header = getInitParameter("header");
            if (header != null && ! "".equals(header)) {
                File headerFile = new File(directory, header);
                if (headerFile.canRead()) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(headerFile), "UTF-8"));
                    String line = in.readLine();
                    while (line != null) {
                        result.append(line);
                        line = in.readLine();
                    }
                    in.close();
                }
            }
            result.append("<table>");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            result.append("<tr><td class='lastmodified'>" + df.format(new Date(directory.lastModified())) + "</td><td class='filesize'> </td><td class='filename'><a href='.'>./</a></td></tr>");
            if (! pathInfo.equals("/")) {
                result.append("<tr><td class='lastmodified'>" + df.format(new Date(directory.getParentFile().lastModified()))  +"</td><td class='filesize'> </td><td class='filename'><a href='..'>../</a></td></tr>");
            }
            List<File> list = Arrays.asList(directory.listFiles());
            if (comparator != null) {
                Collections.sort(list, comparator);
            }
            for (File file : list) {
                String name = file.getName() + (file.isDirectory() ? "/" : "");
                if (ignores(pathInfo + name)) continue;

                result.append("<tr><td class='lastmodified'>");
                result.append(df.format(new Date(file.lastModified())));
                result.append("</td>");
                result.append("<td class='filesize'>");
                result.append(formatFileSize.process(null, null, file.length()));
                result.append("</td>");
                result.append("<td class='filename'>");
                if (canRead(req, file)) {
                    String url = URL.transform(file.getName()) + (file.isDirectory() ? "/" : "");
                    result.append("<a href='" + url + "'>" + XML.transform(name) + "</a>");
                } else {
                    result.append(XML.transform(name));
                }
                result.append("</td></tr>");
            }
            result.append("</table>");
            result.append("<h3>" + org.mmbase.Version.get() + "</h3>");
            result.append("</body>");
            result.append("</html>");
            try {
                byte [] bytes = result.toString().getBytes("UTF-8");
                resp.setContentLength(bytes.length);
                BufferedOutputStream out = new BufferedOutputStream(resp.getOutputStream());
                out.write(bytes);
                out.flush();
            } catch (java.io.UnsupportedEncodingException uue) {
                // cannot happen UTF-8 is supported.
                log.fatal(uue);
            }
            return;
        } else {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Directory listings are not enabled. (see web.xml)");
            return;
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        File file = getFile(req.getPathInfo(), resp);
        if (file.exists()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "The file '" + req.getPathInfo() + "' already exists");
            return;
        }
        org.mmbase.bridge.Cloud cloud = getCloud(readQuery(req.getQueryString()));
        if (cloud.getUser().getRank() == org.mmbase.security.Rank.ANONYMOUS) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "The file '" + req.getPathInfo() + "' already exists");
            return;
        }
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        BufferedInputStream in = new BufferedInputStream(req.getInputStream());
        byte[] buf = new byte[1024];
        int b = 0;
        while ((b = in.read(buf)) != -1) {
            out.write(buf, 0, b);
        }
        out.flush();
        in.close();
        out.close();
    }

    @Override
    protected long getLastModified(HttpServletRequest req) {
        return getFile(req.getPathInfo(), null).lastModified();
    }

    /**
     * The directory as a function. This can makes the information available via RMMCI.
     */
    public static class DirectoryFunction  extends org.mmbase.util.functions.AbstractFunction<String> {
        public DirectoryFunction() {
            super("fileServletDirectory");
        }
        String dir = null;
        public String getFunctionValue(org.mmbase.util.functions.Parameters parameters) {
            if (dir != null) {
                return dir;
            } else {
                return FileServlet.getDirectory().toString();
            }

        }
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            dir = in.readUTF();
        }
        private void writeObject(ObjectOutputStream out) throws IOException {
            if (dir != null) {
                out.writeUTF(dir);
            } else {
                out.writeUTF(FileServlet.getDirectory().toString());
            }
        }
    }

}


