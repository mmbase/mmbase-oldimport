<% response.setContentType("text/html; charset=utf-8");
// as many browsers as possible should not cache:
response.setHeader("Cache-Control", "no-cache");
response.setHeader("Pragma","no-cache");
long now = System.currentTimeMillis();
response.setDateHeader("Expires",  now);
response.setDateHeader("Last-Modified",  now);
response.setDateHeader("Date",  now);
%><%@ page language="java" contentType="text/html; charset=utf-8"
%><%@ page import="java.util.*"
%><%@ page import="org.mmbase.bridge.*"
%><%@ taglib uri="http://www.mmbase.org/mmbase-taglib-1.0"  prefix="mm"
%>
