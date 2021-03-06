<script type="text/javascript">
function check(tr, checked) {
    var pattern = new RegExp("\\bselected\\b");
    if (! checked &amp;&amp; pattern.test(tr.className)) {
	tr.className = tr.className.replace(pattern, "");
    } else if (checked &amp;&amp; ! pattern.test(tr.className)) {
	tr.className += " selected";
    }
}
window.addEventListener("load", function() {
    var els = document.getElementById("searchresult").getElementsByTagName("td");
    var pattern  = new RegExp("\\bclick\\b");
    var pattern2 = new RegExp("\\bnodeselect\\b");
    for (var i = 0; i &lt; els.length; i++) {
	if ( pattern.test(els[i].className) ) {
	    els[i].addEventListener("click", function() {
		document.location = this.parentNode.cells[1].getElementsByTagName("a")[0].href;
	    }, false);
	}
	if (pattern2.test(els[i].className)) {
	    els[i].addEventListener("click", function(e) {
		var input = this.childNodes[0];
		input.checked = ! input.checked;
		check(this.parentNode, input.checked);
      }, false);
	    els[i].childNodes[0].addEventListener("click", function() {
		check(this.parentNode.parentNode, this.checked);
	    }, false);
	}
    }
    var selectAll = document.getElementById("selectall");
    selectAll.addEventListener("change", function() {
	var els = document.getElementsByTagName('input');
	for (var el in els) {
	    if (els[el].className == 'nodesbox') {
		els[el].checked = this.checked; els[el].checked
		check(els[el].parentNode.parentNode, this.checked);
	    }
	}
    }, false);
    var els = document.getElementById("searchresult").getElementsByTagName("td");

    for (var i = 0; i &lt; els.length; i++) {
	if ( pattern.test(els[i].className) ) {
	    els[i].addEventListener("click", function() {
		document.location.href = this.parentNode.cells[1].firstChild.href;
	    }, false);
	}
    }
}, false);
</script>
