<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:output method="xml" 
    omit-xml-declaration="no" 
    doctype-public="-//W3C/DTD HTML 4.0 Transitional//EN"
    version="1.0" encoding="utf-8" indent="no" />

<xsl:param name="imageaffix">+s(128x128)</xsl:param>
<xsl:param name="mmbaseurl"></xsl:param>
<xsl:param name="today_date"></xsl:param>
<xsl:param name="defaultsearchage">7</xsl:param>
<xsl:param name="debug">false</xsl:param>

<xsl:template match="@*">
	<xsl:copy><xsl:value-of select="." /></xsl:copy>
</xsl:template>

<xsl:template match="@name"></xsl:template>

<xsl:template match="/">
	<xsl:apply-templates select="wizard" />
</xsl:template>

<xsl:template match="wizard">
<html>
<head>
     <title>NOS Intranet <xsl:value-of select="title" /></title>
	<link rel="stylesheet" type="text/css" href="style.css" />
    <script language="javascript" src="tools.js" >&lt;!-- --&gt;</script>
	<script language="javascript" src="validator.js" >&lt;!-- --&gt;</script>
	<script language="javascript" src="wysiwyg.js" >&lt;!-- --&gt;</script>
	<script language="javascript" src="editwizard.js" >&lt;!-- --&gt;</script>
</head>
 <body link="#008374" vlink="#33CC66" text="#000000" topmargin="0" leftmargin="0" marginwidth="0" marginheight="0"
       onload="doOnLoad_ew();" onunload="doOnUnLoad_ew();" >
<form name="form" method="post" action="#" id="{/wizard/curform}">
		<input type="hidden" name="curform" value="{/wizard/curform}" />
		<input type="hidden" name="cmd" value="" id="hiddencmdfield" />
<table align="left"><tr><td>	
      <xsl:apply-templates select="/*/steps-validator" />
	</td></tr></table>
<table width="590" bgcolor="#F4E6DA" cellspacing="0" cellpadding="5" border="0" >
<tr bgcolor="#F4E6DA">
<td align="center"><a href="index.html"><span class="step">Terug</span></a></td> 
<td align="center"><xsl:choose>
	<xsl:when test="/wizard/form[@id=/wizard/nextform]">
                         <span class="step" align="left" width="100%" onclick="doGotoForm('{/wizard/nextform}')" title="Verder naar '{/wizard/form[@id=/wizard/nextform]/title}'">Verder</span>
				</xsl:when>
				<xsl:otherwise>
				<span class="step-disabled" align="left" width="100%" title="Geen volgende stap.">Verder</span>
				</xsl:otherwise>
				</xsl:choose>
</td> 
<td align="center">
<span id="bottombutton-save" onclick="doSave();" unselectable="on">
				<xsl:if test="/*/steps-validator/@allowsave='true'">
					<xsl:attribute name="class">step</xsl:attribute>
					<xsl:attribute name="title">Bewaar alle wijzigingen.</xsl:attribute>
				</xsl:if>
			    <xsl:if test="/*/steps-validator/@allowsave='false'">
					<xsl:attribute name="class">step-disabled</xsl:attribute>
					<xsl:attribute name="title">U kunt niet bewaren omdat er nog velden ongeldig zijn. U dient eerst te corrigeren.</xsl:attribute>
				</xsl:if>
				<xsl:choose>
					<xsl:when test="/*/steps-validator/step[@valid='false'][not(@form-schema=/wizard/curform)]">
						<xsl:attribute name="otherforms">invalid</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="otherforms">valid</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
			<span class="step">Bewaar</span>
</span>
</td> 
<td align="center">
			<span onclick="doCancel(this);" id="bottombutton-cancel">
				<xsl:if test="/*/steps-validator/@allowcancel='true'">
					<xsl:attribute name="class">step</xsl:attribute>
					<xsl:attribute name="title">Annuleer deze taak, wijzigingen worden NIET bewaard.</xsl:attribute>
				</xsl:if>
				<xsl:if test="/*/steps-validator/@allowcancel='false'">
					<xsl:attribute name="class">step-disabled</xsl:attribute>
				    <xsl:attribute name="title">U kunt deze taak niet annuleren.</xsl:attribute>
				</xsl:if>
				Annuleer
			</span>
</td>
<td align="center"><a href="logout.jsp"><span class="step">Uitloggen</span></a> </td>
</tr>
</table>


<table cellspacing="0" cellpadding="5" border="0" width="590" bgcolor="#F4E6DA">
	<tr>
			<td colspan="2" align="center" valign="bottom">
				<table border="0" cellspacing="0" cellpadding="0" width="547">
				<tr>
					<td class="head"><nobr><xsl:value-of select="title" /></nobr></td>
					<td class="superhead" align="right">
<nobr><xsl:if test="$debug='true'"><a href="debug.jsp" target="_blank">[debug]</a><br /></xsl:if><xsl:value-of select="form[@id=/wizard/curform]/title" /></nobr>
					</td>
				</tr>
				</table>
			</td>
		</tr>

		<tr><td colspan="2" class="divider"><span class="head"><nobr><xsl:value-of select="form[@id=/wizard/curform]/subtitle" /><xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text></nobr></span></td></tr>

		<xsl:apply-templates select="form[@id=/wizard/curform]" />
		
		<tr><td colspan="2"><hr color="#005A4A" size="1" noshade="true" /></td></tr>		
		</table>
	</form>

</body>
</html>

</xsl:template>

<xsl:template match="form">
	<xsl:for-each select="field|list">
		<tr>
		    <xsl:apply-templates select="." />
		</tr>
		<xsl:if test="@minoccurs or @maxoccurs">
			<tr><td><img src="media/nix.gif" width="1" height="1" hspace="0" vspace="0" border="0" alt="" /></td></tr>
		</xsl:if>
	</xsl:for-each>
</xsl:template>

<xsl:template match="field">

	<td align="left" valign="top" width="125">
	<xsl:if test="../../item[count(field) &gt; 1]">
		<xsl:attribute name="style">border-width:0 0 0 1; border-style:solid; border-color:#000000; padding-left:3;</xsl:attribute>
	</xsl:if>
		<img src="media/nix.gif" width="100" height="1" hspace="0" vspace="0" border="0" alt="" /><br />
		<span id="prompt_{@fieldname}" class="valid" prompt="{prompt}">
			<xsl:choose>
			<xsl:when test="description">
 				<xsl:attribute name="title"><xsl:value-of select="description" /></xsl:attribute>
 			</xsl:when>
			<xsl:otherwise>
 				<xsl:attribute name="title"><xsl:value-of select="prompt" /></xsl:attribute>
			</xsl:otherwise>
			</xsl:choose>

			<xsl:value-of select="prompt" />
		</span>
	</td>
	<td align="left">
	<xsl:if test="../../item[count(field) &gt; 1]">
		<xsl:attribute name="style">border-width:0 1 0 0; border-style:solid; border-color:#000000; padding-right:3;</xsl:attribute>
	</xsl:if>
		
		<xsl:choose>
		<xsl:when test="@ftype='data'">
			<span style="width:300;"><xsl:value-of select="value" /></span>
		</xsl:when>
		<xsl:when test="@ftype='line'">
			<input type="text" size="80" name="{@fieldname}" value="{value}" class="width400" onkeyup="validate_validator(event);" onblur="validate_validator(event);">
			<xsl:apply-templates select="@*" />
			</input>
		</xsl:when>
		<xsl:when test="@ftype='text'">
			<textarea name="{@fieldname}" class="width400" wrap="soft" cols="80" onkeyup="validate_validator(event);" onblur="validate_validator(event);">
			<xsl:choose>
				<xsl:when test="@rows"><xsl:attribute name="rows"><xsl:value-of select="@rows" /></xsl:attribute></xsl:when>
				<xsl:otherwise><xsl:attribute name="rows">10</xsl:attribute></xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates select="@*" />
				<xsl:value-of select="value" />
			</textarea>
		</xsl:when>
		<xsl:when test="@ftype='relation' or @ftype='enum'">
			<select name="{@fieldname}" class="width400" onchange="validate_validator(event);" onblur="validate_validator(event);">
			<xsl:apply-templates select="@*" />
				<xsl:choose>
					<xsl:when test="optionlist/option[@selected='true']"></xsl:when>
					<xsl:otherwise>
						<option value="-">select...</option>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:for-each select="optionlist/option">
					<option value="{@id}">
					<xsl:if test="@selected='true'">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>
						<xsl:value-of select="." />
					</option>
				</xsl:for-each>
			</select>
		</xsl:when>
		<xsl:when test="@ftype='date'">
			<div>
				<input type="hidden" name="{@fieldname}" value="{value}" id="{@fieldname}">
				<xsl:apply-templates select="@*" />
				</input>

				<xsl:if test="(@dttype='datetime') or (@dttype='date')">
					<select name="internal_{@fieldname}_day" dttype="day" onchange="validate_validator(event);" onblur="validate_validator(event);">
						<option value="1">1</option><option value="2">2</option><option value="3">3</option><option value="4">4</option><option value="5">5</option><option value="6">6</option><option value="7">7</option><option value="8">8</option><option value="9">9</option><option value="10">10</option><option value="11">11</option><option value="12">12</option><option value="13">13</option><option value="14">14</option><option value="15">15</option><option value="16">16</option><option value="17">17</option><option value="18">18</option><option value="19">19</option><option value="20">20</option><option value="21">21</option><option value="22">22</option><option value="23">23</option><option value="24">24</option><option value="25">25</option><option value="26">26</option><option value="27">27</option><option value="28">28</option><option value="29">29</option><option value="30">30</option><option value="31">31</option>
					</select><xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text>
					<select name="internal_{@fieldname}_month" dttype="month" onchange="validate_validator(event);" onblur="validate_validator(event);">
						<option value="1">january</option><option value="2">february</option><option value="3">march</option><option value="4">april</option><option value="5">may</option><option value="6">june</option><option value="7">july</option><option value="8">august</option><option value="9">september</option><option value="10">october</option><option value="11">november</option><option value="12">december</option>
					</select><xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text>
					<input name="internal_{@fieldname}_year" dttype="year" type="text" value="" size="4" maxlength="4" onkeyup="validate_validator(event);" onblur="validate_validator(event);" />
				</xsl:if>

				<xsl:if test="@dttype='datetime'">
					<xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text><xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text>at<xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text><xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text>
				</xsl:if>

				<xsl:if test="(@dttype='datetime') or (@dttype='time')">
					<input name="internal_{@fieldname}_hours" dttype="hour" type="text" value="" size="2" maxlength="2" onkeyup="validate_validator(event);" onblur="validate_validator(event);" />
					<xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text>:<xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text>
					<input name="internal_{@fieldname}_minutes" dttype="minutes" type="text" value="" size="2" maxlength="2" onkeyup="validate_validator(event);" onblur="validate_validator(event);" />
				</xsl:if>
			</div>
		</xsl:when>
		<xsl:when test="@ftype='wizard'">
			<div>
			<table border="0" cellspacing="0" cellpadding="0" style="display:inline;" width="616">
				<tr>
					<td align="right" valign="top" class="search" width="100%">
						<nobr>
							<a href="wizard.jsp?wizard={@wizardname}|{@did}&amp;objectnumber={@objectnumber}&amp;popup=true"><img src="media/new.gif" border="0" /></a>
							<img src="media/nix.gif" width="5" height="1" hspace="0" vspace="0" border="0" alt="" />
						</nobr>
					</td>
				</tr>
			</table>
			</div>
		</xsl:when>
		<xsl:when test="@ftype='startwizard'">
			<nobr><a href="popupwizard.jsp?wizard={@wizardname}&amp;objectnumber={@objectnumber}" target="_blank">(start new wizard)</a>
			</nobr>
		</xsl:when>
		<xsl:when test="@ftype='upload'">
			<xsl:choose>
				<xsl:when test="@dttype='image' and not(upload)">
					<div class="imageupload">
						<div><input type="hidden" name="{@fieldname}" value="YES" />
						<img src="{$mmbaseurl}/img.db?{@number}{$imageaffix}" hspace="0" vspace="0" border="0" /><br />
						<a href="upload.jsp?did={@did}&amp;wizard={/wizard/@instance}&amp;maxsize={@dtmaxsize}" onclick="return doStartUpload(this);">Upload new Image</a>
						</div>
						

					</div>

				</xsl:when>
				<xsl:when test="@dttype='image' and upload">
					<div class="imageupload"><input type="hidden" name="{@fieldname}" value="YES" />
						<img src="{upload/path}" hspace="0" vspace="0" border="0" /> <span><xsl:value-of select="upload/@name" /><xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text>(<xsl:value-of select="round((upload/@size) div 100) div 10" />K)</span><br />
						<a href="upload.jsp?did={@did}&amp;wizard={/wizard/@instance}&amp;maxsize={@dtmaxsize}" onclick="return doStartUpload(this);">Upload other Image</a>
					</div>
				</xsl:when>

			<xsl:otherwise>
			<nobr>File Upload.<input type="hidden" name="{@fieldname}" value="YES" />
			<xsl:choose>
				<xsl:when test="not(upload)">Nog geen (nieuw) bestand.
				<a href="{$mmbaseurl}/attachment.db?{@number}">download huidig</a><br />
				</xsl:when>
				<xsl:otherwise>Uploaded: <xsl:value-of select="upload/@name" /><xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text>(<xsl:value-of select="round((upload/@size) div 100) div 10" />K)
				</xsl:otherwise>
			</xsl:choose>
			
			<xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text><a href="upload.jsp?did={@did}&amp;wizard={/wizard/@instance}&amp;maxsize={@dtmaxsize}" onclick="return doStartUpload(this);">Upload new</a>
			
			</nobr>
			</xsl:otherwise>
			</xsl:choose>
		</xsl:when>
		<xsl:when test="@ftype='image'">
			<span>
				<img src="{$mmbaseurl}/img.db?{@number}{$imageaffix}" hspace="0" vspace="0" border="0" title="{field[@name='description']}" /><br />
			</span>

		</xsl:when>
		<xsl:otherwise>
			<input type="text" name="{@fieldname}" value="{value}" class="width400" onkeyup="validate_validator(event);" onblur="validate_validator(event);">
			<xsl:apply-templates select="@*" />
			</input>
		</xsl:otherwise>
		</xsl:choose>
	</td>
</xsl:template>

<xsl:template match="item">
	<span class="listitem" style="display:inline; ">
		<!-- here we figure out how to draw this repeated item. It depends on the displaytype -->
		<xsl:choose>
		<xsl:when test="@displaytype='link'">
			<span style="width:600;"><a href="wizard.jsp?wizard={@wizardname}&amp;objectnumber={field[@name='number']/value}">- <xsl:value-of select="field[@name='title']/value" /></a></span>
		</xsl:when>
		<xsl:when test="@displaytype='image'">
			<span style="width:128; height:168;" >
				<table border="0" cellspacing="0" cellpadding="0" width="128" style="display:inline; width:128;">
					<tr>
						<td>
				<xsl:if test="command[@name='delete-item']">
					<span class="imagebutton" xstyle="position:absolute; top:-10; left:88;" onclick="doSendCommand('{command[@name='delete-item']/@cmd}');">
						<img src="media/remove.gif" width="20" height="20" />
					</span>
				</xsl:if>
				
				<xsl:if test="command[@name='move-up']">
					<span class="imagebutton" xstyle="position:absolute; top:-10; left:88;"
					    onclick="doSendCommand('{command[@name='move-up']/@cmd}','parent::*/@orderby');">
				        <img src="media/up.gif" width="20" height="20" />
				    </span>
				</xsl:if>
				<xsl:if test="command[@name='move-down']">
					<span class="imagebutton" xstyle="position:absolute; top:-10; left:88;"
					    onclick="doSendCommand('{command[@name='move-down']/@cmd}','parent::*/@orderby');">
				        <img src="media/down.gif" width="20" height="20" />
				    </span>
				</xsl:if>
				
					</td>
					</tr>
					<tr>
					<td>

				<span>
					<img src="{$mmbaseurl}/img.db?{@destination}{$imageaffix}" hspace="0" vspace="0" border="0" title="{field[@name='description']}" /><br />
				</span>
					</td>
					<td width="20"><img src="media/nix.gif" width="20" height="1" /></td>
					<xsl:if test="field[not(@ftype='data')]">
						<!-- another field, not just data, eg. a position editor -->
							<td colspan="10" valign="top">
								<table border="0" cellspacing="0" cellpadding="0" width="200" style="width:200">
								<xsl:for-each select="field[not(@ftype='data')]">
								<tr>
									<xsl:apply-templates select="." />
								</tr>
								</xsl:for-each>
								</table>
							</td>
					</xsl:if>
						</tr>
					<tr>
						<td width="128">
				
				<span style="width:128; height:26; overflow:hidden; font-size:10px;"><xsl:value-of select="field[@ftype='data']/value" /></span>
						</td>
					</tr>
				</table>
			</span>
		</xsl:when>
		<xsl:when test="count(field) &lt; 2">
			<table border="0" cellspacing="0" cellpadding="0" width="610">
			<tr>
				<td align="left" valign="top">
					<table border="0" cellspacing="0" cellpadding="0">
					<tr>
						<xsl:apply-templates select="field" />
					</tr>
					</table>
				</td>
				<td align="right" valign="top">
					<nobr>
						<xsl:if test="@displaytype='audio'">
							<a href="{$mmbaseurl}/rastreams.db?{@destination}"><img src="media/audio.gif" xstyle="position:relative; top:3; left:0;" width="20" height="20" hspace="3" vspace="0" border="0" alt="" /></a>
						</xsl:if>
						<xsl:if test="@displaytype='video'">
							<a href="{$mmbaseurl}/rmstreams.db?{@destination}"><img src="media/video.gif" xstyle="position:relative; top:3; left:0;" width="20" height="20" hspace="3" vspace="0" border="0" alt="" /></a>
						</xsl:if>
						<xsl:if test="command[@name='delete-item']">
							<span class="imagebutton" onclick="doSendCommand('{command[@name='delete-item']/@cmd}');">
								<img src="media/remove.gif" width="20" height="20" />
							</span>
						</xsl:if>
					</nobr>
				</td>
			</tr>
			</table>
		</xsl:when>
		<xsl:otherwise>
			<table border="0" cellspacing="0" cellpadding="0" >
			<tr>
				<td align="right" valign="top" xstyle="position:relative; top:0; left:0;">
					<xsl:if test="@displaytype='audio'">
						<a href="{$mmbaseurl}/rastreams.db?{@destination}"><img src="media/audio.gif" width="20" height="20" hspace="0" vspace="0" border="0" alt="" /></a>
					</xsl:if>
					<xsl:if test="@displaytype='video'">
						<a href="{$mmbaseurl}/rmstreams.db?{@destination}"><img src="media/video.gif" width="20" height="20" hspace="0" vspace="0" border="0" alt="" /></a>
					</xsl:if>
					<xsl:if test="command[@name='delete-item']">
						<span class="imagebutton" onclick="doSendCommand('{command[@name='delete-item']/@cmd}');">
							<img src="media/remove.gif" width="20" height="20" hspace="0" vspace="0" border="0"/>
						</span>
					</xsl:if>
					<xsl:if test="command[@name='move-up']">
						<span style="position:absolute; top:-10; left:88;" onclick="doSendCommand('{command[@name='move-up']/@cmd}');"><img src="media/up.gif" width="20" height="20" /></span>
					</xsl:if>
					<xsl:if test="command[@name='move-down']">
						<span style="position:absolute; top:-10; left:88;" onclick="doSendCommand('{command[@name='move-down']/@cmd}');"><img src="media/down.gif" width="20" height="20" /></span>
					</xsl:if>
				</td>
			</tr>

			<!-- draw all fields, if there are any for this item -->
			<tr><td colspan="2" style="border-width:1 1 0 1; border-style:solid; border-color:#000000;"><img src="media/nix.gif" width="1" height="3" hspace="0" vspace="0" border="0" alt="" /></td></tr>
			<xsl:for-each select="field">
				<tr>
					<xsl:apply-templates select="." />
				</tr>
			</xsl:for-each>
			<tr><td colspan="2" style="border-width:0 1 1 1; border-style:solid; border-color:#000000;"><img src="media/nix.gif" width="1" height="3" hspace="0" vspace="0" border="0" alt="" /></td></tr>
			</table>
		</xsl:otherwise>
		</xsl:choose>
	</span><wbr />
</xsl:template>

<xsl:template match="list">
	<td align="left" valign="top" colspan="2" class="listcanvas" width="558">
		<div class="subhead" title="{description}">
			<nobr><xsl:value-of select="title" /></nobr>
		</div>

		<xsl:if test="item">
			<br />
			<div style="position:relative; top:0; left:0;">
				<xsl:choose>
				<xsl:when test="@ordertype='number'">
					<xsl:apply-templates select="item" >
						<xsl:sort select="@orderby" data-type="number" />
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="item" >
						<xsl:sort select="@orderby" />
					</xsl:apply-templates>
				</xsl:otherwise>
				</xsl:choose>
			</div><br />
		</xsl:if>

		<xsl:if test="command[@name='add-item']">
			<xsl:for-each select="command[@name='search']">
				<table border="0" cellspacing="0" cellpadding="0" style="display:inline;" width="616">
					<tr>
						<td align="right" valign="top" class="search" width="100%">
							<nobr>
								<xsl:value-of select="prompt" /><xsl:text disable-output-escaping="yes" >&amp;nbsp;</xsl:text>
								
								<select name="searchage_{../command[@name='add-item']/@cmd}" style="width:80">
									<option value="0"><xsl:call-template name="searchage"><xsl:with-param name="real" select="'0'" /><xsl:with-param name="pref" select="@age" /></xsl:call-template>0 days</option>
									<option value="1"><xsl:call-template name="searchage"><xsl:with-param name="real" select="'1'" /><xsl:with-param name="pref" select="@age" /></xsl:call-template>1 day</option>
									<option value="7"><xsl:call-template name="searchage"><xsl:with-param name="real" select="'7'" /><xsl:with-param name="pref" select="@age" /></xsl:call-template>7 days</option>
									<option value="31"><xsl:call-template name="searchage"><xsl:with-param name="real" select="'31'" /><xsl:with-param name="pref" select="@age" /></xsl:call-template>1 month</option>
									<option value="365"><xsl:call-template name="searchage"><xsl:with-param name="real" select="'365'" /><xsl:with-param name="pref" select="@age" /></xsl:call-template>1 year</option>
									<option value="-1"><xsl:call-template name="searchage"><xsl:with-param name="real" select="'-1'" /><xsl:with-param name="pref" select="@age" /></xsl:call-template>any age</option>
								</select>
							    <select name="searchfields_{../command[@name='add-item']/@cmd}" style="width:125;" onchange="dochange_searchfields('{../command[@name='add-item']/@cmd}')">
									<xsl:choose>
										<xsl:when test="search-filter">
											<xsl:for-each select="search-filter">
												<option value="{search-fields}"><xsl:value-of select="name" /></option>
											</xsl:for-each>
										</xsl:when>
										<xsl:otherwise>
											<option value="title">Content contains</option>
										</xsl:otherwise>
									</xsl:choose>
									<option value="owner">Owner is</option>
								</select>
								<img src="media/nix.gif" width="2" height="1" hspace="0" vspace="0" border="0" alt="" />
								<input type="text" name="searchterm_{../command[@name='add-item']/@cmd}" value="" style="width:175;" />
								<img src="media/nix.gif" width="2" height="1" hspace="0" vspace="0" border="0" alt="" />
								<span class="imagebutton" onclick="doSearch(this,'{../command[@name='add-item']/@cmd}');"	>
									<xsl:for-each select="@*"><xsl:copy /></xsl:for-each>
									<img src="media/search.gif" width="16" height="16"/>
								</span>
								<img src="media/nix.gif" width="5" height="1" hspace="0" vspace="0" border="0" alt="" />
							</nobr>
						</td>
					</tr>
				</table>
			</xsl:for-each>
		</xsl:if>

		<xsl:for-each select="command[@name='add-item']">
			<table border="0" cellspacing="0" cellpadding="0" width="616">
			<xsl:choose>
			<xsl:when test="../command[@name='search']">
				<xsl:attribute name="style">display:inline; visibility:hidden; position:absolute; top:0; left:0;</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<xsl:attribute name="style">display:inline;</xsl:attribute>
			</xsl:otherwise>
			</xsl:choose>
				<tr>
					<td align="right" valign="top" class="search" width="100%">
						<nobr>
							<span class="imagebutton" onclick="doSendCommand('{@cmd}');"><img src="media/new.gif" width="20" height="20" /></span>
							<img src="media/nix.gif" width="5" height="1" hspace="0" vspace="0" border="0" alt="" />
						</nobr>
					</td>
				</tr>
			</table>
		</xsl:for-each>
		<xsl:for-each select="command[@name='startwizard']">
			<table border="0" cellspacing="0" cellpadding="0" style="display:inline;" width="616">
				<tr>
					<td align="right" valign="top" class="search" width="100%">
						<nobr>
							<a href="popupwizard.jsp?wizard={@wizardname}&amp;objectnumber={@objectnumber}" target="_blank"><img src="media/new.gif" border="0" /></a>
							<img src="media/nix.gif" width="5" height="1" hspace="0" vspace="0" border="0" alt="" />
						</nobr>
					</td>
				</tr>
			</table>
		</xsl:for-each>
	</td>
</xsl:template>



<xsl:template match="steps-validator">

<xsl:for-each select="step">
				
	<xsl:variable name="schemaid" select="@form-schema" />
<p><span onclick="doGotoStep('{@form-schema}');" id="bottombutton-step-{$schemaid}" class="stepicon">
<xsl:attribute name="class"><xsl:if test="$schemaid=/wizard/curform">current</xsl:if>stepicon<xsl:if test="@valid='true'">-valid</xsl:if></xsl:attribute>
<xsl:attribute name="title"><xsl:if test="@valid='true'"><xsl:value-of select="/*/form[@id=$schemaid]/title" /></xsl:if><xsl:if test="@valid='false'"><xsl:value-of select="/*/form[@id=$schemaid]/title" /> is NOT valid. Click here to correct the errors.</xsl:if></xsl:attribute>
<img width="116" height="16" ><xsl:attribute name="src">media/stap<xsl:value-of select="position()" />a.gif</xsl:attribute></img><br />
<span class="step-info" ><xsl:value-of select="/*/form[@id=$schemaid]/title" /></span>
</span>
</p>
</xsl:for-each>

     
</xsl:template>

<xsl:template match="xsteps-validator">
    	<table cellspacing="0" cellpadding="3" border="0" align="left" >
	    <tr><td width="121" align="center"><img src="/rnw/gfx/clearpixel.gif" width="121" height="1" border="0" /><br />
			<xsl:if test="count(step) &gt; 1">
				<p><xsl:choose>
				<xsl:when test="/wizard/form[@id=/wizard/prevform]">
					<span class="step" align="left" width="100%" onclick="doGotoForm('{/wizard/prevform}')" title="Terug naar '{/wizard/form[@id=/wizard/prevform]/title}'">[ Terug ]</span>
				</xsl:when>
				<xsl:otherwise>
					<span class="step-disabled" align="left" width="100%" title="Geen vorige stap.">[ Terug ]</span>
				</xsl:otherwise>
				</xsl:choose></p>
				<xsl:for-each select="step">
					<xsl:variable name="schemaid" select="@form-schema" />
				    <p><span onclick="doGotoStep('{@form-schema}');" id="bottombutton-step-{$schemaid}" class="stepicon">
						<xsl:attribute name="class"><xsl:if test="$schemaid=/wizard/curform">current</xsl:if>stepicon<xsl:if test="@valid='true'">-valid</xsl:if></xsl:attribute>
						<xsl:attribute name="title"><xsl:if test="@valid='true'"><xsl:value-of select="/*/form[@id=$schemaid]/title" /></xsl:if><xsl:if test="@valid='false'"><xsl:value-of select="/*/form[@id=$schemaid]/title" /> is NOT valid. Click here to correct the errors.</xsl:if></xsl:attribute>
						[ Stap <xsl:value-of select="position()" /> ]<br />
					</span>
					<span class="step-info" ><xsl:value-of select="/*/form[@id=$schemaid]/title" /></span>
					</p>
				</xsl:for-each>
				<p><xsl:choose>
				<xsl:when test="/wizard/form[@id=/wizard/nextform]">
					<span class="step" align="left" width="100%" onclick="doGotoForm('{/wizard/nextform}')" title="Verder naar '{/wizard/form[@id=/wizard/nextform]/title}'">[ Verder ]</span>
				</xsl:when>
				<xsl:otherwise>
					<span class="step-disabled" align="left" width="100%" title="Geen volgende stap.">[ Verder] </span>
				</xsl:otherwise>
				</xsl:choose></p>
			</xsl:if>
			<p><span onclick="doCancel(this);" id="bottombutton-cancel">
				<xsl:if test="@allowcancel='true'">
					<xsl:attribute name="class">bottombutton</xsl:attribute>
					<xsl:attribute name="title">Annuleer deze taak, wijzigingen worden NIET bewaard.</xsl:attribute>
				</xsl:if>
				<xsl:if test="@allowcancel='false'">
					<xsl:attribute name="class">bottombutton-disabled</xsl:attribute>
				    <xsl:attribute name="title">U kunt deze taak niet annuleren.</xsl:attribute>
				</xsl:if>
				[ Annuleer ]
			</span></p>
			<p><span id="bottombutton-save" onclick="doSave();" unselectable="on">
				<xsl:if test="@allowsave='true'">
					<xsl:attribute name="class">bottombutton</xsl:attribute>
					<xsl:attribute name="title">Bewaar alle wijzigingen.</xsl:attribute>
				</xsl:if>
			    <xsl:if test="@allowsave='false'">
					<xsl:attribute name="class">bottombutton-disabled</xsl:attribute>
					<xsl:attribute name="title">U kunt niet bewaren omdat er nog velden ongeldig zijn. U dient eerst te corrigeren.</xsl:attribute>
				</xsl:if>
				<xsl:choose>
					<xsl:when test="step[@valid='false'][not(@form-schema=/wizard/curform)]">
						<xsl:attribute name="otherforms">invalid</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="otherforms">valid</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				[ Bewaar ]
			</span></p>
        </td></tr>
        </table>

</xsl:template>

<xsl:template name="searchage">
	<xsl:param name="real" />
	<xsl:param name="pref" />
	<xsl:if test="($real=$pref) or (not($pref) and $defaultsearchage=$real)"><xsl:attribute name="selected">true</xsl:attribute></xsl:if>
</xsl:template>

</xsl:stylesheet>
