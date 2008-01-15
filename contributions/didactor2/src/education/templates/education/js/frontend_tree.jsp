// -*- mode: java; -*-
<%@taglib uri="http://www.mmbase.org/mmbase-taglib-2.0" prefix="mm"
%><%@taglib uri="http://www.didactor.nl/ditaglib_1.0" prefix="di"
%><mm:content type="text/javascript" expires="600">
    <mm:cloud>

var ITEM_NONE   = '${mm:treefile("/gfx/icon_arrow_tab_none.gif", pageContext, includePath)}';
var ITEM_OPENED = '${mm:treefile("/gfx/icon_arrow_tab_open.gif", pageContext, includePath)}';
var ITEM_CLOSED = '${mm:treefile("/gfx/icon_arrow_tab_closed.gif", pageContext, includePath)}';

var may_open_future =
    <di:hasrole role="coach">true || //coach</di:hasrole>
    <di:hasrole role="teacher">true || // teacher</di:hasrole>
    <di:hasrole role="systemadministrator">true || //system administrator </di:hasrole>
    <di:getsetting component="core" setting="may_open_future" />; // may_open_future setting

// IE does not even support indexOf, fixing that here..
[].indexOf || (Array.prototype.indexOf = function(v,n){
  n = (n==null)?0:n; var m = this.length;
  for(var i = n; i < m; i++)
    if(this[i] == v)
       return i;
  return -1;
});


var currentnumber = -1;
var contenttype   = new Array();
var contentnumber = new Array();
var openDivs      = new Object();
var usedFrames    = new Object();
var enabledPopups = false;


// legacy
function resize() {
    if(browserVersion()[0] == "IE") {
        var oBody = content.document.body;
        var oFrame = document.all("content");
        oFrame.style.height = oBody.scrollHeight + 280;
    } else {
        var frameElem = document.getElementById("content");
        frameElem.style.overflow = "";
        var frameContentHeight = frameElem.contentWindow.parent.document.body.scrollHeight;
        frameElem.style.height = frameContentHeight + 80;
        frameElem.height = frameContentHeight + 80;
        frameElem.style.overflow = "hidden";
        //alert("set height to " + (frameContentHeight + 80));
    }
}


function addContent( type, number ) {
    contenttype[contenttype.length] = type;
    contentnumber[contentnumber.length] = number;
    if ( contentnumber.length == 1 ) {
        currentnumber = contentnumber[0];
    }
}

function nextContent() {
    for(var count = 0; count <= contentnumber.length; count++) {
        if ( contentnumber[count] == currentnumber ) {
            if ( count < contentnumber.length ) {
                if ("tests" == contenttype[count]) {
                    alert('<di:translate key="education.testalert" escape="js-single-quotes,java" />');
                    return;
                }
                var opentype = contenttype[count+1];
                var opennumber = contentnumber[count+1];
            }
        }
    }
    openContent(opentype, opennumber);
    openOnly('div' + opennumber, 'img' + opennumber);
}

function previousContent() {
    for(var count = 0; count <= contentnumber.length; count++) {
        if ( contentnumber[count] == currentnumber ) {
            if ( count > 0 ) {
                if ("tests" == contenttype[count]) {
                    alert('<di:translate key="education.testalert" escape="js-single-quotes,java" />');
                    return;
                }
                var opentype = contenttype[count-1];
                var opennumber = contentnumber[count-1];
            }
        }
    }
    openContent(opentype, opennumber);
    openOnly('div' + opennumber, 'img' + opennumber);
}


function invalidateCurrentFrame() {
    usedFrames[document.href_frame] = null;
}

function loadIconOn() {
    document.getElementById("ajax_loader").style.display = "inline";
}
function loadIconOff() {
    document.getElementById("ajax_loader").style.display = "none";
}

function disablePopups() {
    if (enabledPopups) {
        enabledPopups = false;
        var popups = getElementsByClass(document, "popup");
        for(var i = 0; i < popups.length; i++) {
            popups[i].style.display = "none";
        }
    }

}

function enablePopups() {
    if (! enabledPopups) {
        enabledPopups = true;
        var popups = getElementsByClass(document, "popup");
        for(var i = 0; i < popups.length; i++) {
            popups[i].style.display = "inline";
        }
    }

}

function check(className) {
    if (/\btests\b/.test(className)) {
        disablePopups();
    } else {
        enablePopups();
    }
}

function requestContent(href) {
   var content = usedFrames[href];
   if (content == null) {
       loadIconOn();
       var xmlhttp = new XMLHttpRequest();
       xmlhttp.open("GET", href, true);
       xmlhttp.onreadystatechange = function()  {
           if (xmlhttp.readyState == 4) {
               try {
                    var contentEl = document.getElementById('contentFrame');
                    //                    console.log("updating " + contentEl + "with" + xmlhttp.responseXML);
                    Sarissa.updateContentFromNode(xmlhttp.responseXML, contentEl, null, loadIconOff);
                    contentEl.validator = new MMBaseValidator();
                    contentEl.validator.validatePage(false, contentEl);
                    contentEl.validator.validateHook = function(valid) {
                        var buttons = getElementsByClass(contentEl, "formbutton", "input");
                        for (i = 0; i < buttons.length; i++) {
                            var disabled = (contentEl.validator.invalidElements > 0);
                            buttons[i].disabled = disabled;
                            // just because IE does not recognized input[disabled]
                            buttons[i].className = "formbutton " + (disabled ? "disabled" : "enabled");
                        }
                    };
                    contentEl.validator.addValidation(contentEl);
                    check(xmlhttp.responseXML.documentElement.getAttribute('class'));
                    document.href_frame = href;
               } catch (exception) {
                   // backwards compatibility
                   contentEl.innerHTML = "<iframe width='100%' height='100%' id='content' name='content' frameborder='0'  src='" + href + "' />";
                   resize();
                   //throw exception;
                   alert(exception);
               }
               var array = new Array();
               // in case it is more than one element (e.g. comments or so), store all childnodes.

               try {
                   for (var i = 0; i < contentEl.childNodes.length; i++) {
                       array.push(contentEl.childNodes[i]);
                   }
               } catch (ex) {
                   alert(ex);
               }
               usedFrames[href] = array;
           }
       };
       xmlhttp.send(null);
   } else {
       var contentEl = document.getElementById('contentFrame');
       Sarissa.clearChildNodes(contentEl);
       for (var i=0; i < content.length; i++) {
           contentEl.appendChild(content[i]);
       }
       document.href_frame = href;
   }
   scrollToTop();
}

function postContent(href, form) {
    loadIconOn();
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.open("POST", href, true);
    xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xmlhttp.onreadystatechange =  function()  {
        //console.log("" + xmlhttp);
        if (xmlhttp.readyState == 4) {
            //console.log("ready")
            if (xmlhttp.status == 200) {
                var contentEl = document.getElementById('contentFrame');
                Sarissa.updateContentFromNode(xmlhttp.responseXML, contentEl, null, afterPost);
                usedFrames[document.href_frame] = null;
                document.href_frame = href;
                //console.log("Found result " + contentEl);
                usedFrames[href] = contentEl.childNodes;
            } else {
                alert(xmlhttp.status);
            }
        }
    };
    var content = '';
    var sep = '';
    var textareas = form.getElementsByTagName("textarea");
    for (i=0; i<textareas.length; i++) {
        var ta = textareas[i];
        content += sep + ta.name + '=' + ta.value;
        sep = '&';
    }
    var textareas = form.getElementsByTagName("input");
    for (i=0; i<textareas.length; i++) {
        var ta = textareas[i];
        content += sep + ta.name + '=' + ta.value;
        sep = '&';
    }
    //console.log("posting " + content);
    xmlhttp.send(content);
    scrollToTop();
}

function openContent(type, number) {

    if (document.getElementById('content-'+currentnumber)) {
        var el = document.getElementById('content-'+currentnumber);
        var classNames = el.className.split(" ");
        var newClassNames = new Array();
        for (var c in classNames) {
            if (classNames[c] != 'selectedContent') {
                newClassNames.push(classNames[c]);
            }
        }
        el.className = newClassNames.join(" ");
    }
    if ( number > 0 ) {
        currentnumber = number;
    }

    var href;
    switch ( type ) {
    case "educations":
        href = addParameter('<mm:treefile page="/education/educations.jsp" objectlist="$includePath" referids="$referids" escapeamps="false"/>', 'edu='+number);
        break;
    case "learnblocks":
    case "htmlpages":
        href= addParameter('<mm:treefile page="/education/learnblocks/index.jsp" objectlist="$includePath" referids="$referids,fb_madetest?" escapeamps="false"/>', 'learnobject='+number);
        break;
    case "tests":
        href= addParameter('<mm:treefile page="/education/tests/index.jsp" objectlist="$includePath" referids="$referids,fb_madetest?,justposted?" escapeamps="false"/>', 'learnobject='+number);
        break;
    case "pages":
        href= addParameter('<mm:treefile page="/education/pages/index.jsp" objectlist="$includePath" referids="$referids,fb_madetest?" escapeamps="false"/>', 'learnobject='+number);
        break;
    case "flashpages":
        href= addParameter('<mm:treefile page="/education/flashpages/index.jsp" objectlist="$includePath" referids="$referids,fb_madetest?" escapeamps="false"/>', 'learnobject='+number);
        break;
    }
    requestContent(href);


    if (document.getElementById('content-'+currentnumber)) {
        var el = document.getElementById('content-'+currentnumber);
        var orig = el.className;
        var classNames = el.className.split(" ");
        if (classNames.indexOf("selectedContent") == -1) {
            classNames.push("selectedContent");
        }
        el.className = classNames.join(" ");
    }

}

function openClose(div, img) {
    var realdiv = document.getElementById(div);
    var realimg = document.getElementById(img);

    if (realdiv != null) {
        try {
            var o = openDivs[div];
            if (o != null) {
                openDivs[div] = null;
                realdiv.style.display = "none";
                realimg.src = ITEM_CLOSED;
            } else {
                if (! may_open_future) {
                    if (/\bnon_completed\b/.test(realimg.parentNode.className)) {
                        alert('<di:translate key="education.future" escape="js-single-quotes,java" />');
                        return false;
                    }
                }
                if (/\bblocked\b/.test(realimg.parentNode.className)) {
                    alert('<di:translate key="education.future" escape="js-single-quotes,java" />');
                    return false;
                }
                openDivs[div] = img;
                realdiv.style.display = "block";
                realimg.src = ITEM_OPENED;
            }
        } catch (ex) {
            alert(ex);
        }
    }
    return true;
}

function openOnly(div, img) {
    var realdiv = document.getElementById(div);
    var realimg = document.getElementById(img);
    // alert("openOnly("+div+","+img+"); - "+realdiv);
    if (realdiv != null) {
        try {
            if (! may_open_future) {
                if (/\bnon_completed\b/.test(realimg.parentNode.className)) {
                    alert('<di:translate key="education.future" escape="js-single-quotes,java" />');
                    return false;
                }
            }
            if (/\bblocked\b/.test(realimg.parentNode.className)) {
                alert('<di:translate key="education.future" escape="js-single-quotes,java" />');
                return false;
            }
            openDivs[div] = img;
            realdiv.style.display = "block";
            realimg.src = ITEM_OPENED;

            var className = realdiv.className;
            if (className) {
                // ignore "lbLevel" in classname to get the level depth
                var level = className.substring(7, className.length);
                // alert("level = "+level);
                var findparent = realdiv;
                var findparentClass = className;


                if (level > 1) {
                    // also open parents
                    do {
                        findparent = findparent.parentNode;
                        findparentClass = findparent.className || "";
                    } while (findparent && findparentClass.indexOf("lbLevel") != 0);

                    if (findparent) {
                        var divid = findparent.id;
                        var imgid = "img" + divid.substring(3,divid.length);
                        openOnly(divid, imgid);
                    }
                }


            }
        } catch (ex) {
            alert(ex);
        }
    } else { // find enclosing div
        try {
            var finddiv = realimg;
            while (finddiv != null && (! finddiv.className || finddiv.className.substring(0,7) != "lbLevel")) {
                finddiv = finddiv.parentNode;
                // if (finddiv.className) alert(finddiv.className.substring(0,7));
            }
            if (finddiv != null) {
                var divid = finddiv.id;
                var imgid = "img" + divid.substring(3,divid.length);
                openOnly(divid,imgid);
            }
        } catch (ex) {
            alert(ex);
        }
    }

    return true;
}

function closeAll() {
    var divs = document.getElementsByTagName("div");
    for (i=0; i<divs.length; i++) {
        var div = divs[i];
        var cl = "" + div.className;
        if (cl.match("lbLevel")) {
            divs[i].style.display = "none";
        }
    }
    removeButtons();
}
function closeAppropriate() {
    var divs = document.getElementsByTagName("div");
    for (i=0; i<divs.length; i++) {
        var div = divs[i];
        var cl = "" + div.className;
        if (cl.match("lbLevel")) {
            if (openDivs[div.id] == null) {
                divs[i].style.display = "none";
            }
        }
    }
    removeButtons();
}

function removeButtons() {
    // Remove all the buttons in front of divs that have no children
    var imgs = document.getElementsByTagName("img");
    for (i=0; i<imgs.length; i++) {
        var img = imgs[i];
        if (/\bimgClosed\b/.test(img.className)) {
            var id = img.id;
            var divid = "div" + img.id.substring(3);
            var div = document.getElementById(divid);
            if (div == null || div.childNodes.length == 1) {
                img.src = ITEM_NONE;
            }
        }
    }
}

function afterPost() {
    <mm:hasnode number="component.progress">
        reloadProgress();
    </mm:hasnode>
    reloadEducationTree();
    scrollToTop();
    loadIconOff();
}

<mm:treefile page="/education/tree.jspx" objectlist="$includePath" referids="$referids" write="false" escapeamps="false">
    function reloadEducationTree() {
        usedFrames    = new Object();
        //console.log("Updating '" + document.getElementById('education-tree') + " with url ${_}");
        Sarissa.updateContentFromURI('${_}', document.getElementById('education-tree'), null, closeAppropriate);

    }
</mm:treefile>

function scrollToTop() {
    var fromElement =  document.getElementById("rows");
    var scroll = fromElement.offsetTop;
    if (document.documentElement && document.documentElement.scrollTop) {
        document.documentElement.scrollTop = scroll;
    }
    if (document.body && document.body.scrollTop) {
        document.body.scrollTop = scroll;
    }
}

</mm:cloud>
</mm:content>
