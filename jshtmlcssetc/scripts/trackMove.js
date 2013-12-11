var mouseState = false;
var curTarget = null;
var mouseOffset = null;
var isMousedownOnRefCanvas = false;
Number.prototype.NaN0 = function() {
	return isNaN(this) ? 0 : this;
}
$(document).ready(function() {
	var trackTable = document.getElementById("tableTrack");
	var trackNameNodes = trackTable.getElementsByClassName("trackName");
	for(var i = 0; i < trackNameNodes.length; i++) {
		//thNodes[i].onmouseover = mouseOver;
		//thNodes[i].onmouseout = mouseOut;
		//thNodes[i].onmousedown = mouseDown;

		trackNameNodes[i].childNodes[0].onmouseover = mouseOver;
		trackNameNodes[i].childNodes[0].onmouseout = mouseOut;
		trackNameNodes[i].childNodes[0].onmousedown = mouseDown;
	}

	document.onmouseup = mouseUp;
	document.onmousemove = mouseMove;

	document.getElementById("refTrack1").addEventListener("mouseover", refSeqMouseover, true);
	
	$(document).bind("mousedown", function(){
		isMousedownOnRefCanvas = true;
		var verticalLine = $(document.getElementById("verticalLine"));
		var indexSpan = $(document.getElementById("indexSpan"));
		verticalLine.css("display", "none");
		indexSpan.css("display", "none");
	});
	$(document).bind("mouseup", function(){
		isMousedownOnRefCanvas = false;
	});

	/*add handle mouseover & mousemove event
	 *in order to set the display-value  of
	 * some html elements whose positon is
	 * sbsolute to none-value */
	document.getElementsByTagName("body")[0].addEventListener("mousemove", bodyMousemove, true);
	document.getElementsByTagName("body")[0].addEventListener("mouseover", bodyMouseover, true);
});
function mouseOver(ev) {
	if(mouseState == false) {
		ev = ev || window.event;
		var target = ev.target || ev.srcElement;
		target = target.parentNode;
		var trNode = target.parentNode;
		trNode.style.background = "#E8E8E8";
		var tdNodes = trNode.getElementsByTagName("td");
		tdNodes[0].style.background = "#E8E8E8";
		tdNodes[1].firstChild.style.background = "#E8E8E8";
		tdNodes[2].firstChild.style.background = "#E8E8E8";
	}
}

function mouseOut(ev) {
	if(mouseState == false) {
		ev = ev || window.event;
		var target = ev.target || ev.srcElement;
		target = target.parentNode;
		var trNode = target.parentNode;
		trNode.style.background = "#ffffff";
		var tdNodes = trNode.getElementsByTagName("td");
		tdNodes[0].style.background = "#ffffff";
		tdNodes[1].firstChild.style.background = "#ffffff";
		tdNodes[2].firstChild.style.background = "#ffffff";
	}
}

function mouseDown(ev) {
	ev = ev || window.event;
	var target = ev.target || ev.srcElement;
	var trackNameNode = target.parentNode;
	curTarget = trackNameNode.parentNode;
	mouseOffset = getMouseOffset(curTarget, ev);
	document.body.onselectstart = function() {
		return false;
	};
	mouseState = true;
}

function mouseUp() {
	mouseState = false;
	if(curTarget != null) {
		curTarget.style.background = "#ffffff";
		curTarget.getElementsByTagName("td")[0].style.background = "#fff";
		curTarget.getElementsByTagName("td")[1].firstChild.style.background = "#ffffff";
		curTarget.getElementsByTagName("td")[2].firstChild.style.background = "#ffffff";
	}
	document.body.onselectstart = function() {
		return true;
	};
	curTarget = null;
	//the bext code is about the mouseup event in the File "dragToZoomin.js"
	tracksImgareaselect.setOptions({
		disable : true
	});
	ppTracksImgareaselect.setOptions({
		disable : true
	});
}

function mouseCoords(ev) {
	if(ev.pageX || ev.pageY) {
		return {
			x : ev.pageX,
			y : ev.pageY
		};
	}
	return {
		x : ev.clientX + document.body.scrollLeft - document.body.clientLeft,
		y : ev.clientY + document.body.scrollTop - document.body.clientTop
	};
}

function getPosition(e) {
	var left = 0;
	var top = 0;
	while(e.offsetParent) {
		left += e.offsetLeft + (e.currentStyle ? (parseInt(e.currentStyle.borderLeftWidth)).NaN0() : 0);
		top += e.offsetTop + (e.currentStyle ? (parseInt(e.currentStyle.borderTopWidth)).NaN0() : 0);
		e = e.offsetParent;
	}
	left += e.offsetLeft + (e.currentStyle ? (parseInt(e.currentStyle.borderLeftWidth)).NaN0() : 0);
	top += e.offsetTop + (e.currentStyle ? (parseInt(e.currentStyle.borderTopWidth)).NaN0() : 0);

	return {
		x : left,
		y : top
	};

}

function getMouseOffset(target, ev) {
	ev = ev || window.event;

	var docPos = getPosition(target);
	var mousePos = mouseCoords(ev);
	return {
		x : mousePos.x - docPos.x,
		y : mousePos.y - docPos.y
	};
}

function mouseMove(ev) {
	ev = ev || window.event;
	var mousePos = mouseCoords(ev);
	if(curTarget != null) {
		curTarget.style.background = "#d3d3d3";
		curTarget.getElementsByTagName("td")[0].style.background = "#d3d3d3";
		curTarget.getElementsByTagName("td")[1].firstChild.style.background = "#d3d3d3";
		curTarget.getElementsByTagName("td")[2].firstChild.style.background = "#d3d3d3";

		var container = document.getElementById("tableTrack").childNodes[1];
		var trNodes = container.getElementsByTagName("tr");
		for(var i = 0; i < trNodes.length; i++) {
			with(trNodes[i]) {
				var pos = getPosition(trNodes[i]);

				setAttribute('startWidth', parseInt(offsetWidth));
				setAttribute('startHeight', parseInt(offsetHeight));
				setAttribute('startLeft', pos.x);
				setAttribute('startTop', pos.y);
			}
		}
		var beforeNode = null;
		var yPos = mousePos.y - mouseOffset.y + (parseInt(curTarget.getAttribute('startHeight')) / 2);
		for(var i = trNodes.length - 1; i >= 0; i--) {
			with(trNodes[i]) {
				if(nodeName == "#text")
					continue;
				if(curTarget != trNodes[i] && ((parseInt(getAttribute('startTop')) + parseInt(getAttribute('startHeight'))) > yPos)) {
					beforeNode = trNodes[i];
				}
			}
		}
		if(beforeNode) {
			if(beforeNode != curTarget.nextSibling) {
				container.insertBefore(curTarget, beforeNode);
			}
		} else {
			if((curTarget.nextSibling)) {
				container.appendChild(curTarget);
			}
		}

	}
}

function refSeqMouseover(ev) {
	ev = ev || window.event;
	var mouseCoordinates = mouseCoords(ev);
	var refSeqCanvas = $(document.getElementById("refTrack1"));
	var divTrack = $(document.getElementById("divTrack"));
	var div_ppContent = $(document.getElementById("ppContent"));
	var verticalLine = $(document.getElementById("verticalLine"));
	var verticalLine2 = $(document.getElementById("verticalLine2"));
	var indexSpan = $(document.getElementById("indexSpan"));
	var divTrackHeight = divTrack.height();
	var refSeqCanvasTop = refSeqCanvas.position().top;
	var refSeqCanvasLeft = refSeqCanvas.position().left;
	var refSeqCanvasWidth = refSeqCanvas.width();
	var div_ppContent_top = $("#personalPannel").position().top + $("#personalPannelTitle").height()+ 5;
	var div_ppContent_height = div_ppContent.height();
	verticalLine.css("top", divTrack.position().top);
	verticalLine.css("left", mouseCoordinates.x - 2);
	verticalLine.width(1);
	verticalLine.height(divTrackHeight);
	
	verticalLine2.css("top", div_ppContent_top);
	verticalLine2.css("left", mouseCoordinates.x - 2);
	verticalLine2.width(1);
	verticalLine2.height(div_ppContent_height);
	/*when the shift key is down, verticalLine & indexSpan are none in dispaly css*/
	if(shiftKeyState == false && isMousedownOnRefCanvas == false) {
		verticalLine.css("display", "block");
		verticalLine2.css("display", "block");
	} else {
		verticalLine.css("display", "none");
		verticalLine2.css("display", "none");
	}

	var refSeqIndex = Math.round((mouseCoordinates.x - refSeqCanvasLeft - refSeqCanvasWidth / (searchLength * 2)) / refSeqCanvasWidth * searchLength + startIndex);
	indexSpan.html(addCommas(refSeqIndex));
	indexSpan.css("top", refSeqCanvasTop);
	indexSpan.css("left", mouseCoordinates.x - indexSpan.width() / 2);
	if(shiftKeyState == false && isMousedownOnRefCanvas == false) {
		indexSpan.css("display", "block");
	} else {
		indexSpan.css("display", "none");
	}
	document.getElementById("refTrack1").addEventListener("mousemove", refSeqMousemove, true);
	document.getElementById("refTrack1").parentNode.addEventListener("mouseout", refSeqMouseout, true);
}

function refSeqMousemove(ev) {
	ev = ev || window.event;
	var mouseCoordinates = mouseCoords(ev);
	var refSeqCanvas = $(document.getElementById("refTrack1"));
	var divTrack = $(document.getElementById("divTrack"));
	var div_ppContent = $(document.getElementById("ppContent"));
	var verticalLine = $(document.getElementById("verticalLine"));
	var verticalLine2 = $(document.getElementById("verticalLine2"));
	var indexSpan = $(document.getElementById("indexSpan"));
	var divTrackHeight = divTrack.height();
	var refSeqCanvasTop = refSeqCanvas.position().top;
	var refSeqCanvasLeft = refSeqCanvas.position().left;
	var refSeqCanvasWidth = refSeqCanvas.width();
	var div_ppContent_top = $("#personalPannel").position().top + $("#personalPannelTitle").height()+ 5;
	var div_ppContent_height = div_ppContent.height();
	if(shiftKeyState == false && isMousedownOnRefCanvas == false) {
		verticalLine.css("display", "block");
		verticalLine2.css("display", "block");
	} else {
		verticalLine.css("display", "none");
		verticalLine2.css("display", "none");
	}
	verticalLine.css("top", divTrack.position().top);
	verticalLine.css("left", mouseCoordinates.x - 2);
	verticalLine.width(1);
	verticalLine.height(divTrackHeight);
	
	verticalLine2.css("top", div_ppContent_top);
	verticalLine2.css("left", mouseCoordinates.x - 2);
	verticalLine2.width(1);
	verticalLine2.height(div_ppContent_height);

	var refSeqIndex = Math.round((mouseCoordinates.x - refSeqCanvasLeft - refSeqCanvasWidth / (searchLength * 2)) / refSeqCanvasWidth * searchLength + startIndex);
	indexSpan.html(addCommas(refSeqIndex));
	indexSpan.css("top", refSeqCanvasTop);
	indexSpan.css("left", mouseCoordinates.x - indexSpan.width() / 2);
	if(shiftKeyState == false && isMousedownOnRefCanvas == false) {
		indexSpan.css("display", "block");
	} else {
		indexSpan.css("display", "none");
	}
}

function refSeqMouseout(evt) {
	evt = evt || window.event;
	var eventTarget = evt.target || evt.srcElement;

	if(eventTarget === document.getElementById("refTrack1").parentNode) {
		var verticalLine = $(document.getElementById("verticalLine"));
		var verticalLine2 = $(document.getElementById("verticalLine2"));
		var indexSpan = $(document.getElementById("indexSpan"));

		verticalLine.css("display", "none");
		verticalLine2.css("display", "none");
		indexSpan.css("display", "none");
		document.getElementById("refTrack1").removeEventListener("mousemove", refSeqMousemove, true);
	}

}

/*bodyMouseover & bodyMousemove function
 *are used to set the display-value  of
 * some html elements whose positon is
 * sbsolute to none-value */
function bodyMouseover(evt) {
	evt = evt || window.event;
	var evtMouseCoods = mouseCoords(evt);
	var refSeqCanvas = $(document.getElementById("refTrack1"));
	var refSeqCanvasTop = refSeqCanvas.position().top;
	var refSeqCanvasLeft = parseInt(refSeqCanvas.position().left) + trackLength/3;
	var refSeqCanvasHeight = refSeqCanvas.height();
	var refSeqCanvasWidth = parseInt(refSeqCanvas.width())/3;
	if(evtMouseCoods.x < refSeqCanvasLeft || evtMouseCoods.x > refSeqCanvasLeft + refSeqCanvasWidth || evtMouseCoods.y < refSeqCanvasTop || evtMouseCoods.y > refSeqCanvasTop + refSeqCanvasHeight) {
		var verticalLine = $(document.getElementById("verticalLine"));
		var verticalLine2 = $(document.getElementById("verticalLine2"));
		var indexSpan = $(document.getElementById("indexSpan"));

		verticalLine.css("display", "none");
		verticalLine2.css("display", "none");
		indexSpan.css("display", "none");
		document.getElementById("refTrack1").removeEventListener("mousemove", refSeqMousemove, true);
	}
	/*in order to makesure that refseq track can handle the mouse event
	 * when the mouse hover on the refseq track,because when the mouse
	 * moves quickly,the mouseover event may not happen */
	if(evtMouseCoods.x >= refSeqCanvasLeft && evtMouseCoods.x <= refSeqCanvasLeft + refSeqCanvasWidth && evtMouseCoods.y >= refSeqCanvasTop && evtMouseCoods.y <= refSeqCanvasTop + refSeqCanvasHeight) {
		document.getElementById("refTrack1").addEventListener("mousemove", refSeqMousemove, true);
		document.getElementById("refTrack1").parentNode.addEventListener("mouseout", refSeqMouseout, true);
	}
}

function bodyMousemove(evt) {
	evt = evt || window.event;
	var evtMouseCoods = mouseCoords(evt);
	var refSeqCanvas = $(document.getElementById("refTrack1"));
	var refSeqCanvasTop = refSeqCanvas.position().top;
	var refSeqCanvasLeft = parseInt(refSeqCanvas.position().left) + trackLength/3;
	var refSeqCanvasHeight = refSeqCanvas.height();
	var refSeqCanvasWidth = parseInt(refSeqCanvas.width())/3;
	if(evtMouseCoods.x < refSeqCanvasLeft || evtMouseCoods.x > refSeqCanvasLeft + refSeqCanvasWidth || evtMouseCoods.y < refSeqCanvasTop || evtMouseCoods.y > refSeqCanvasTop + refSeqCanvasHeight) {
		var verticalLine = $(document.getElementById("verticalLine"));
		var verticalLine2 = $(document.getElementById("verticalLine2"));
		var indexSpan = $(document.getElementById("indexSpan"));

		verticalLine.css("display", "none");
		verticalLine2.css("display", "none");
		indexSpan.css("display", "none");
		document.getElementById("refTrack1").removeEventListener("mousemove", refSeqMousemove, true);
	}
	/*in order to makesure that refseq track can handle the mouse event
	 * when the mouse hover on the refseq track,because when the mouse
	 * moves quickly,the mouseover event may not happen */
	if(evtMouseCoods.x >= refSeqCanvasLeft && evtMouseCoods.x <= refSeqCanvasLeft + refSeqCanvasWidth && evtMouseCoods.y >= refSeqCanvasTop && evtMouseCoods.y <= refSeqCanvasTop + refSeqCanvasHeight) {
		document.getElementById("refTrack1").addEventListener("mousemove", refSeqMousemove, true);
		document.getElementById("refTrack1").parentNode.addEventListener("mouseout", refSeqMouseout, true);
	}
}

function canvasMousemove(evt) {
	evt = evt || window.event;
	var evtMouseCoods = mouseCoords(evt);
	var eventTarget = evt.target || evt.srcElement;
	var trNode = eventTarget.parentNode.parentNode;
	var trackId = trNode.id;
	//add by Liran for Pfanno and Pclns track details
	if(trackId==null || trackId==""){
		trackId=trNode.parentNode.id;
	}
	//add by Liran for Pfanno and Pclns track details
	var i, j;
	var evtCanvasX, evtCanvasY;
	for( i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == trackId) {
			break;
		}
	}
	evtCanvasX = evtMouseCoods.x - $(eventTarget).position().left;
	evtCanvasY = evtMouseCoods.y - $(eventTarget).position().top;

	//add by Liran for Pfanno and Pclns track details
	var Pfanno_Pclns_Node=null;
	if(trackId.substring(0,1)=="_"){
		if(trackId==personalPannel.Pfanno.id){
			Pfanno_Pclns_Node=-1;
		} else {
			for(var Pclns_idx=0;Pclns_idx<personalPannel.Pclns.length;Pclns_idx++){
				if(trackId==personalPannel.Pclns[Pclns_idx].id){
					Pfanno_Pclns_Node=Pclns_idx;
				}
			}
		}

		evtCanvasX = evtCanvasX - $("#personalPannel").position().left;
		evtCanvasY = evtCanvasY - $("#personalPannel").position().top;
	}
	//add by Liran for Pfanno and Pclns track details

	if(i < trackItems.length) {
		for( j = 0; j < trackItems[i].details.length; j++) {
			if(evtCanvasX >= trackItems[i].details[j].left && evtCanvasX <= trackItems[i].details[j].right && evtCanvasY >= trackItems[i].details[j].top && evtCanvasY <= trackItems[i].details[j].bottom) {
				//document.body.style.cursor = "pointer";
				$(eventTarget).css("cursor", "pointer");
				break;
			}
		}
		if(j >= trackItems[i].details.length) {
			$(eventTarget).css("cursor", "url(./image/Grabber.cur),auto");
			//document.body.style.cursor = "auto";
		}
	}

	//add by Liran for Pfanno and Pclns track details
	if(Pfanno_Pclns_Node!=null && Pfanno_Pclns_Node==-1){
		for( j = 0; j < personalPannel.Pfanno.details.length; j++) {
			if(evtCanvasX >= personalPannel.Pfanno.details[j].left && evtCanvasX <= personalPannel.Pfanno.details[j].right && evtCanvasY >= personalPannel.Pfanno.details[j].top && evtCanvasY <= personalPannel.Pfanno.details[j].bottom) {
				//document.body.style.cursor = "pointer";
				$(eventTarget).css("cursor", "pointer");
				break;
			}
		}
		if(j >= personalPannel.Pfanno.details.length) {
			$(eventTarget).css("cursor", "url(./image/Grabber.cur),auto");
		}
	} else if(Pfanno_Pclns_Node!=null && Pfanno_Pclns_Node>=0){
		for( j = 0; j < personalPannel.Pclns[Pfanno_Pclns_Node].details.length; j++) {
			if(evtCanvasX >= personalPannel.Pclns[Pfanno_Pclns_Node].details[j].left && evtCanvasX <= personalPannel.Pclns[Pfanno_Pclns_Node].details[j].right && evtCanvasY >= personalPannel.Pclns[Pfanno_Pclns_Node].details[j].top && evtCanvasY <= personalPannel.Pclns[Pfanno_Pclns_Node].details[j].bottom) {
				//document.body.style.cursor = "pointer";
				$(eventTarget).css("cursor", "pointer");
				break;
			}
		}
		if(j >= personalPannel.Pclns[Pfanno_Pclns_Node].details.length) {
			$(eventTarget).css("cursor", "url(./image/Grabber.cur),auto");
		}
	}
	//add by Liran for Pfanno and Pclns track details
}

function canvasClick(evt) {
	evt = evt || window.event;
	var evtMouseCoods = mouseCoords(evt);
	var evtCanvasX, evtCanvasY;

	var eventTarget = evt.target || evt.srcElement;
	var trNode = eventTarget.parentNode.parentNode;
	var trackId = trNode.id;
	var i, j,k;
	
	evtCanvasX = evtMouseCoods.x - $(eventTarget).position().left;
	evtCanvasY = evtMouseCoods.y - $(eventTarget).position().top;

	for( i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == trackId) {
			break;
		}
	}

	if(i < trackItems.length) {
		for( j = 0; j < trackItems[i].details.length; j++) {
			if(evtCanvasX >= trackItems[i].details[j].left && evtCanvasX <= trackItems[i].details[j].right && evtCanvasY >= trackItems[i].details[j].top && evtCanvasY <= trackItems[i].details[j].bottom) {
				//show details
				break;
			}
		}
		if(j < trackItems[i].details.length) {
			//draw the pointer
			drawDecoriteminfopointer("decoriteminfopointer");
			//set the position & display
			$(document.getElementById("decoriteminfo")).css("top", evtMouseCoods.y);
			$(document.getElementById("decoriteminfo")).css("left", evtMouseCoods.x - 10);
			$(document.getElementById("decoriteminfo")).css("display", "block");
			//set the cursor style & event handler
			$(eventTarget).css("cursor", "auto");
			$(document.getElementById("decoriteminfo")).css("cursor", "auto");
			//document.body.style.cursor = "auto";
			$(document.getElementById("decoriteminfo")).unbind("mouseenter");
			$(document.getElementById("decoriteminfo")).mouseenter(function() {
				document.body.style.cursor = "auto";
			});
			document.body.addEventListener("mousedown", mousedownOutsideTooltip, false);
						
			//Ajax to get the gene details
			getDetailHttpRequest(trackId, trackItems[i].details[j].id, trackItems[i].details[j].from, trackItems[i].details[j].to);
		}
	}

}

function getDetailHttpRequest(trackId, id, from, to){
	var url = "servlet/test.do?" + "action=getDetail&tracks=" + trackId + "&id=" + id + "&start=" + from + "&end=" + to;
	XMLHttpReq3.onreadystatechange = handle_getDetailHttpRequest;
	XMLHttpReq3.open("GET", url, true);
	XMLHttpReq3.send(null);
}
function handle_getDetailHttpRequest(){
	if(XMLHttpReq3.readyState == 4) {
		if(XMLHttpReq3.status == 200) {
			var XMLDoc = XMLHttpReq3.responseXML;
			var geneNode = XMLDoc.getElementsByTagName(xmlTagElement)[0];
			var geneSymbol = geneNode.getAttribute(xmlAttributeSymbol) ? geneNode.getAttribute(xmlAttributeSymbol):"NO Symbol";
			var geneId = geneNode.getAttribute(xmlAttributeId);
			var geneFrom = geneNode.getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
			var geneTo = geneNode.getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
			var geneDirection = geneNode.getElementsByTagName(xmlTagDirection)[0].childNodes[0].nodeValue;
			var subElementNodes = geneNode.getElementsByTagName(xmlTagSubElement);
			var i,k;
			var subElementLength;
			var exons = [];
			var intros = [];
			var UTRs = [];
			for(i=0;i<subElementNodes.length;i++){
				if(subElementNodes[i].getAttribute(xmlAttributeType) == subElementTypeLineValue){
					intros[intros.length] = [];
					intros[intros.length - 1].from = subElementNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
					intros[intros.length - 1].to = subElementNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
				}else{
					if(i > 0){
						if(subElementNodes[i-1].getAttribute(xmlAttributeType) == subElementTypeBoxValue || subElementNodes[i-1].getAttribute(xmlAttributeType) == subElementTypeBandValue){
							exons[exons.length - 1].to = subElementNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
						}else{
							exons[exons.length] = [];
							exons[exons.length - 1].from = subElementNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
							exons[exons.length - 1].to = subElementNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
						}
					}else{
						exons[exons.length] = [];
						exons[exons.length - 1].from = subElementNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
						exons[exons.length - 1].to = subElementNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
					}
				}
			}
			/*for(i=0;i<subElementNodes.length;i++){
				if(subElementNodes[i].getAttribute(xmlAttributeType) == subElementTypeBoxValue){
					exons[exons.length] = [];
					exons[exons.length - 1].from = subElementNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
					exons[exons.length - 1].to = subElementNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
				}else if(subElementNodes[i].getAttribute(xmlAttributeType) == subElementTypeBandValue){
					UTRs[UTRs.length] = [];
					UTRs[UTRs.length - 1].from = subElementNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
					UTRs[UTRs.length - 1].to = subElementNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
				}else{
					intros[intros.length] = [];
					intros[intros.length - 1].from = subElementNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
					intros[intros.length - 1].to = subElementNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
				}
			}*/
			
			var trackId = XMLDoc.getElementsByTagName(xmlTagElements)[0].getAttribute(xmlAttributeId);
			var geneType,url;
			if(trackId == "knownGene"){
				geneType = "UCSC";
				url = "http://genome.ucsc.edu/cgi-bin/hgGene?hgg_type=knownGene&hgg_gene=" + geneId;
			}else if(trackId == "ensemblGene"){
				geneType = "Ensembl";
				url = "http://asia.ensembl.org/Homo_sapiens/Gene/Summary?g=" + geneId;
			}else if(trackId == "refGene"){
				geneType = "RefSeq";
				url = "http://www.ncbi.nlm.nih.gov/nuccore/" + geneId;
			}

			
			//output the information to tooltip
			document.getElementById("tooltip_symbol").innerHTML = geneSymbol;
			document.getElementById("tooltip_id").innerHTML = geneId;
			document.getElementById("tooltip_geneType").innerHTML = geneType + "<span style=\"font-size:14px\">↗<\/span>";
			document.getElementById("tooltip_geneType").onclick = function(){
				window.open(url);
			};
			document.getElementById("tooltip_scale").innerHTML = chrNum + ":" + geneFrom + "-" + geneTo + " " + geneDirection;
			
			var structureDetails1 = "", structureDetails2 = "";
			structureDetails1 = structureDetails1 + exons.length +" exons"+"<br>" + intros.length + " introns";
			//structureDetails1 = structureDetails1 + exons.length +" exons"+"<br>" + intros.length + " intros" + "<br>" + UTRs.length + " UTRs";
			structureDetails2 = structureDetails2 + "<div style=\"color:#D6B8CE;height:140px;overflow-y:scroll; font-size: 12px\">";
			if(exons.length>0){
				structureDetails2 = structureDetails2 + "exons:"+"<ol>";
				for(k=0; k < exons.length; k++){
					structureDetails2 = structureDetails2 + "<li>";
					subElementLength = exons[k].to - exons[k].from +1;
					structureDetails2 = structureDetails2 + subElementLength + "bp, " + exons[k].from + "-" + exons[k].to + "<\/li>";
				}
				structureDetails2 = structureDetails2 + "<\/ol>";
			}
			if(intros.length > 0){
				structureDetails2 = structureDetails2 + "introns:"+"<ol>";
				for(k=0;k < intros.length; k++){
					structureDetails2 = structureDetails2 + "<li>";
					subElementLength = intros[k].to - intros[k].from + 1;
					structureDetails2 = structureDetails2 + subElementLength + "bp, " + intros[k].from + "-" + intros[k].to + "<\/li>";
				}
				structureDetails2 = structureDetails2 + "<\/ol>";
			}
			if(UTRs.length>0){
				structureDetails2 = structureDetails2 + "UTRs:"+"<ol>";
				for(k=0;k < UTRs.length; k++){
					structureDetails2 = structureDetails2 + "<li>";
					subElementLength = UTRs[k].to - UTRs[k].from+1;
					structureDetails2 = structureDetails2 + subElementLength + "bp, " + UTRs[k].from + "-" + UTRs[k].to + "<\/li>";
				}
				structureDetails2 = structureDetails2 + "<\/ol>";
			}
			structureDetails2 = structureDetails2 + "<\/div>";
			document.getElementById("bait39").innerHTML = structureDetails1 + structureDetails2;
			
			$(document.getElementById("detailInforLoading")).css("display","none");
			$(document.getElementById("decoriteminfosays")).css("display","block");;
		}
	}
}

function canvasClickForPersonalGene(evt) {
	evt = evt || window.event;
	var evtMouseCoods = mouseCoords(evt);
	var evtCanvasX, evtCanvasY;

	var eventTarget = evt.target || evt.srcElement;
	var trNode = eventTarget.parentNode.parentNode.parentNode;
	var trackId = trNode.id;
	var j;
	
	evtCanvasX = evtMouseCoods.x - $("#personalPannel").position().left - $(eventTarget).position().left;
	evtCanvasY = evtMouseCoods.y - $("#personalPannel").position().top - $(eventTarget).position().top;
	
	if(trackId == personalPannel.Panno.id){
		for( j = 0; j < personalPannel.Panno.details.length; j++) {
			if(evtCanvasX >= personalPannel.Panno.details[j].left && evtCanvasX <= personalPannel.Panno.details[j].right && evtCanvasY >= personalPannel.Panno.details[j].top && evtCanvasY <= personalPannel.Panno.details[j].bottom) {
				break;
			}
		}
		if(j < personalPannel.Panno.details.length){
			//draw the pointer
			drawDecoriteminfopointer("personalGeneDetailPointer");
			//set the position & display
			$(document.getElementById("personalGeneDetail")).css("top", evtCanvasY + $(eventTarget).position().top - 3);
			$(document.getElementById("personalGeneDetail")).css("left", evtCanvasX + $(eventTarget).position().left - 15);
			$(document.getElementById("personalGeneDetail")).css("display", "block");
			//set the cursor style & event handler
			$(eventTarget).css("cursor", "auto");
			$(document.getElementById("personalGeneDetail")).css("cursor", "auto");
			//document.body.style.cursor = "auto";
			$(document.getElementById("personalGeneDetail")).unbind("mouseenter");
			$(document.getElementById("personalGeneDetail")).mouseenter(function() {
				document.body.style.cursor = "auto";
			});
			document.body.addEventListener("mousedown", mousedownOutsideTooltipForPersonalGene, false);
			//冗余代码：在detail box之外单击，让其消失，而在之内单击不消失
			/*$(document.getElementById("personalGeneDetail")).mouseout(function(){
				document.body.addEventListener('mousedown', mouseLeftPersonalGeneDetailClickHide, false);
			});
			$(document.getElementById("personalGeneDetail")).mouseover(function(){
				document.body.removeEventListener('mousedown', mouseLeftPersonalGeneDetailClickHide, false);
			});*/
						
			//Ajax to get the gene details
			getPersonalGeneDetailRequest(trackId, personalPannel.Panno.details[j].id, personalPannel.Panno.details[j].from, personalPannel.Panno.details[j].to);
		}
	}

}

function mousedownOutsideTooltipForPersonalGene(evt) {
	evt = evt || window.event;
	var eventTarget = evt.target || evt.srcElement;
	var flag=0;
	while(eventTarget){
		if(eventTarget==document.getElementById("personalGeneDetail")){
			flag=1;
			break;
		}else{
			eventTarget = eventTarget.parentNode;
		}
	}
	if(flag==0) {
		document.getElementById("personalGeneDetail").style.display = "none";
	}
}

function mouseLeftPersonalGeneDetailClickHide(event) {
	event.preventDefault();
	document.getElementById("personalGeneDetail").style.display = "none";
}

function getPersonalGeneDetailRequest(trackId, id, from, to){
	var url = "servlet/test.do?" + "action=getDetail&tracks=" + trackId + "&id=" + id + "&start=" + from + "&end=" + to;
	XMLHttpReq3.onreadystatechange = handle_getPersonalGeneDetailRequest;
	XMLHttpReq3.open("GET", url, true);
	XMLHttpReq3.send(null);
}

function handle_getPersonalGeneDetailRequest(){
	if(XMLHttpReq3.readyState == 4) {
		if(XMLHttpReq3.status == 200) {
			var XMLDoc = XMLHttpReq3.responseXML;
			var geneNode = XMLDoc.getElementsByTagName(xmlTagElement)[0];
			var geneSymbol = geneNode.getAttribute(xmlAttributeSymbol) ? geneNode.getAttribute(xmlAttributeSymbol):"NO Symbol";
			var geneId = geneNode.getAttribute(xmlAttributeId);
			var geneFrom = geneNode.getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
			var geneTo = geneNode.getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
			var geneDirection = geneNode.getElementsByTagName(xmlTagDirection)[0].childNodes[0].nodeValue;
			var subElementNodes = geneNode.getElementsByTagName(xmlTagSubElement);
			var variantNodes = XMLDoc.getElementsByTagName(xmlTagVariant);
			var statusNodes = XMLDoc.getElementsByTagName(xmlTagStatus);
			var exons = [];
			var introns = [];
			var variants = [];
			var statuses = [];
			var i, j, k;
			for(i=0;i<subElementNodes.length;i++){
				if(subElementNodes[i].getAttribute(xmlAttributeType) == subElementTypeLineValue){
					introns[introns.length] = [];
					introns[introns.length - 1].from = subElementNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
					introns[introns.length - 1].to = subElementNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
				}else{
					if(i > 0){
						if(subElementNodes[i-1].getAttribute(xmlAttributeType) == subElementTypeBoxValue || subElementNodes[i-1].getAttribute(xmlAttributeType) == subElementTypeBandValue){
							exons[exons.length - 1].to = subElementNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
						}else{
							exons[exons.length] = [];
							exons[exons.length - 1].from = subElementNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
							exons[exons.length - 1].to = subElementNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
						}
					}else{
						exons[exons.length] = [];
						exons[exons.length - 1].from = subElementNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
						exons[exons.length - 1].to = subElementNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
					}
				}
			}
			if(variantNodes.length > 0){
				for(i=0; i<variantNodes.length; i++){
					var variantLetters = (variantNodes[i].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue + "").split(":");
					var Amino_Acid_variantType = null;
					if(variantLetters.length == 1){
						if(variantLetters[0] == "^"){
							Amino_Acid_variantType = "Initiator lost";
						}else if(variantLetters[0] == "#"){
							Amino_Acid_variantType = "Frame shift";
						}else if(variantLetters[0] == "("){
							Amino_Acid_variantType = "DSS lost";
						}else if(variantLetters[0] == ")"){
							Amino_Acid_variantType = "ASS lost";
						}
					}else{
						if((variantLetters[0] + "").indexOf("$")!= -1 && (variantLetters[1] + "").indexOf("$")==-1){
							Amino_Acid_variantType = "Terminator loss";
						}else if((variantLetters[1] + "").indexOf("$")!= -1 && (variantLetters[0] + "").indexOf("$")==-1){
							Amino_Acid_variantType = "Terminator gain";
						}else if((variantLetters[0] + "").length > (variantLetters[1] + "").length || (variantLetters[1] + "").indexOf("_") != -1){
							Amino_Acid_variantType = "Amino acid deletion: " + variantLetters[0] + "->" + variantLetters[1];
						}else if((variantLetters[0] + "").length < (variantLetters[1] + "").length || (variantLetters[0] + "").indexOf("_") != -1){
							Amino_Acid_variantType = "Amino acid insertion: " + variantLetters[0] + "->" + variantLetters[1];
						}else if(variantLetters[0] != variantLetters[1]){
							Amino_Acid_variantType = "Amino acid variant: "+ variantLetters[0] + "->" + variantLetters[1];
						}
					}
					if(Amino_Acid_variantType){
						variants[variants.length] = [];
						variants[variants.length - 1].from = variantNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
						variants[variants.length - 1].to = variantNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
						variants[variants.length - 1].type = Amino_Acid_variantType;
					}
					if(variants.length > 1){
						for(j = 0; j < variants.length - 1; j++ ){
							last_ele_in_vars = variants[variants.length - 1].type + "" + variants[variants.length - 1].from + variants[variants.length - 1].to;
							if(variants[j].type + "" + variants[j].from + variants[j].to == last_ele_in_vars){
								variants[variants.length - 1].type = variants[variants.length - 1].type + "(allele2)";
								break;
							}
						}
					}
				}
			}
			if(statusNodes.length > 0){
				for(i=0; i < statusNodes.length; i++){
					var temp_statues = (statusNodes[i].childNodes[0].nodeValue + "").split(";");
					for(j=0; j < temp_statues.length; j++){
						var is_same_flag = false;
						for(k = 0; k < statuses.length; k++){
							if(statuses[k] == temp_statues[j]){
								is_same_flag = true;
								break;
							}
						}
						if(is_same_flag){
							statuses[statuses.length] = temp_statues[j] + "(allele2)";
						}else{
							statuses[statuses.length] = temp_statues[j];
						}
					}
				}
			}
			document.getElementById("personalGeneDetailSymbol").innerHTML = geneSymbol;
			document.getElementById("personalGeneDetail-ID").innerHTML = geneId;
			document.getElementById("personalGeneDetailScale").innerHTML = chrNum + ":" + geneFrom + "-" + geneTo + " " + geneDirection;
			
			var structureDetails1 = "", structureDetails2 = "";
			structureDetails1 = structureDetails1 + exons.length +" exons"+"<br>" + introns.length + " introns";
			structureDetails2 = structureDetails2 + "<div style=\"color:#D6B8CE;height:140px;overflow-y:scroll; font-size: 12px\">";
			if(exons.length>0){
				structureDetails2 = structureDetails2 + "exons:"+"<ol>";
				for(k=0; k < exons.length; k++){
					structureDetails2 = structureDetails2 + "<li>";
					subElementLength = exons[k].to - exons[k].from +1;
					structureDetails2 = structureDetails2 + subElementLength + "bp, " + exons[k].from + "-" + exons[k].to + "<\/li>";
				}
				structureDetails2 = structureDetails2 + "<\/ol>";
			}
			if(introns.length > 0){
				structureDetails2 = structureDetails2 + "introns:"+"<ol>";
				for(k=0;k < introns.length; k++){
					structureDetails2 = structureDetails2 + "<li>";
					subElementLength = introns[k].to - introns[k].from + 1;
					structureDetails2 = structureDetails2 + subElementLength + "bp, " + introns[k].from + "-" + introns[k].to + "<\/li>";
				}
				structureDetails2 = structureDetails2 + "<\/ol>";
			}
			structureDetails2 = structureDetails2 + "<\/div>";
			document.getElementById("personalGeneDetail-geneStruct").innerHTML = structureDetails1 + structureDetails2;
			
			var variantDetails1 = "", variantDetails2 = "";
			if(variants.length > 0){
				$("#personalGeneDetailVariantTrnode").css("display","table-row");
				variantDetails1 = variantDetails1 + variants.length + " variants";
				variantDetails2 = variantDetails2 + "<div style=\"color:#D6B8CE;height:40px;overflow-y:scroll; font-size: 12px\">" + "<ol>";
				for(k = 0; k < variants.length; k++){
					variantDetails2 = variantDetails2 + "<li>";
					variantDetails2 = variantDetails2 + variants[k].type + "<br\/>" + variants[k].from + "-" + variants[k].to + "<\/li>";
				}
				variantDetails2 = variantDetails2 + "<\/ol><\/div>";
				document.getElementById("personalGeneDetail-variants").innerHTML = variantDetails1 + variantDetails2;
			}else{
				$("#personalGeneDetailVariantTrnode").css("display","none");
			}
			
			var statusDetails1 = "", statusDetails2 = "";
			if(statuses.length > 0){
				$("#personalGeneDetailStatusTrnode").css("display","table-row");
				statusDetails1 = statusDetails1 + statuses.length + " statuses";
				statusDetails2 = statusDetails2 + "<div style=\"color:#D6B8CE;height:40px;overflow-y:scroll; font-size: 12px\">" + "<ol>";
				for(k = 0; k < statuses.length; k++){
					statusDetails2 = statusDetails2 + "<li>";
					statusDetails2 = statusDetails2 + statuses[k] + "<\/li>";
				}
				statusDetails2 = statusDetails2 + "<\/ol><\/div>";
				document.getElementById("personalGeneDetail-status").innerHTML = statusDetails1 + statusDetails2;
			}else{
				$("#personalGeneDetailStatusTrnode").css("display","none");
			}
			
			$(document.getElementById("personalGeneDetailLoading")).css("display","none");
			$(document.getElementById("personalGeneDetailInfor")).css("display","block");
		}
	}
}
/*in order to make sure that the tooltip box is disappear when click outside the tooltip box;
 because sometimes the function decorCanvasLeftclickHide is failed;
 decorCanvasLeftclickHide function is bind to mouseout event which does not always exactly happen. */
function mousedownOutsideTooltip(evt) {
	evt = evt || window.event;
	var eventTarget = evt.target || evt.srcElement;
	var flag=0;
	while(eventTarget){
		if(eventTarget==document.getElementById("decoriteminfo")){
			flag=1;
			break;
		}else{
			eventTarget = eventTarget.parentNode;
		}
	}
	if(flag==0) {
		document.getElementById("decoriteminfo").style.display = "none";
		document.body.removeEventListener("mousedown", mousedownOutsideTooltip, false);
		document.body.removeEventListener("mousedown", decorCanvasLeftclickHide, false);
		$(document.getElementById("decoriteminfo")).unbind("mouseenter");
	}
}

function toggle39() {
	var hook = document.getElementById('hook39');
	var bait = document.getElementById('bait39');
	if(bait.style.display == 'none') {
		bait.style.display = 'block';
		hook.innerHTML = '&#8863; hide';
	} else {
		bait.style.display = 'none';
		hook.innerHTML = '&#8862; show gene structure';
	}
}

function personalGeneStructureShow() {
	var hook = document.getElementById('personalGeneDetail-showGeneStructBtn');
	var bait = document.getElementById('personalGeneDetail-geneStruct');
	if(bait.style.display == 'none') {
		bait.style.display = 'block';
		hook.innerHTML = '&#8863; hide';
	} else {
		bait.style.display = 'none';
		hook.innerHTML = '&#8862; show gene structure';
	}
}

function personalGeneVariantShow() {
	var hook = document.getElementById('personalGeneDetail-showVariantsBtn');
	var bait = document.getElementById('personalGeneDetail-variants');
	if(bait.style.display == 'none') {
		bait.style.display = 'block';
		hook.innerHTML = '&#8863; hide';
	} else {
		bait.style.display = 'none';
		hook.innerHTML = '&#8862; show variants';
	}
}

function personalGeneStatusShow() {
	var hook = document.getElementById('personalGeneDetail-showStatusBtn');
	var bait = document.getElementById('personalGeneDetail-status');
	if(bait.style.display == 'none') {
		bait.style.display = 'block';
		hook.innerHTML = '&#8863; hide';
	} else {
		bait.style.display = 'none';
		hook.innerHTML = '&#8862; show statuses';
	}
}

function decorCanvasLeftclickHide(event) {
	event.preventDefault();
	document.getElementById("decoriteminfo").style.display = "none";
	document.body.removeEventListener("mousedown", decorCanvasLeftclickHide, false);
	document.body.removeEventListener("mousedown", mousedownOutsideTooltip, false);
	$(document.getElementById("decoriteminfo")).unbind("mouseenter");
}

function drawDecoriteminfopointer(canvasId) {
	c = document.getElementById(canvasId);
	ctx = c.getContext("2d");
	ctx.fillStyle = "rgba(0,0,0,0.75)";
	ctx.beginPath();
	ctx.moveTo(0, 9);
	ctx.lineTo(9, 0);
	ctx.lineTo(17, 9);
	ctx.fill();
}

function canvasClickForVariant(evt){
	evt = evt || window.event;
	var evtMouseCoods = mouseCoords(evt);
	var evtCanvasX, evtCanvasY;

	var eventTarget = evt.target || evt.srcElement;
	var trNode = eventTarget.parentNode.parentNode;
	var trackId = trNode.id, trackSuperId;
	var i, j,k;
	
	evtCanvasX = evtMouseCoods.x - $(eventTarget).position().left;
	evtCanvasY = evtMouseCoods.y - $(eventTarget).position().top;

	for( i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == trackId) {
			trackSuperId = trackItems[i].superid ? trackItems[i].superid : trackId;
			break;
		}
	}
	
	if(i < trackItems.length) {
		for( j = 0; j < trackItems[i].details.length; j++) {
			if(evtCanvasX >= trackItems[i].details[j].left && evtCanvasX <= trackItems[i].details[j].right && evtCanvasY >= trackItems[i].details[j].top && evtCanvasY <= trackItems[i].details[j].bottom) {
				//show details
				break;
			}
		}
		if(j < trackItems[i].details.length) {
			//alert(""+trackId + trackItems[i].details[j].id + trackItems[i].details[j].from + trackItems[i].details[j].to);
			
			//draw the pointer
			drawDecoriteminfopointer("variantDetailTooltipPointer");
			//set the position & display
			$(document.getElementById("variantDetailTooltip")).css("top", evtMouseCoods.y);
			$(document.getElementById("variantDetailTooltip")).css("left", evtMouseCoods.x - 10);
			$(document.getElementById("variantDetailTooltip")).css("display", "block");
			//set the cursor style & event handler
			$(eventTarget).css("cursor", "auto");
			$(document.getElementById("variantDetailTooltip")).css("cursor", "auto");
			//document.body.style.cursor = "auto";
			$(document.getElementById("variantDetailTooltip")).unbind("mouseenter");
			$(document.getElementById("variantDetailTooltip")).mouseenter(function() {
				document.body.style.cursor = "auto";
			});
			$(document).bind("mousedown", mousedownOutsideVariantTooltip);
						
			//Ajax to get the gene details
			getVariantDetailHttpRequest(trackSuperId, trackItems[i].details[j].id, trackItems[i].details[j].from, trackItems[i].details[j].to);
			
		}
	}
}

function getVariantDetailHttpRequest(trackId, id, from, to){
	var url = "servlet/test.do?" + "action=getDetail&tracks=" + trackId + "&id=" + id + "&start=" + from + "&end=" + to;
	XMLHttpReq3.onreadystatechange = handle_getVariantDetailHttpRequest;
	XMLHttpReq3.open("GET", url, true);
	XMLHttpReq3.send(null);
}

function handle_getVariantDetailHttpRequest(){
	if(XMLHttpReq3.readyState == 4) {
		if(XMLHttpReq3.status == 200) {
			var XMLDoc = XMLHttpReq3.responseXML;
			var variantNode =  XMLDoc.getElementsByTagName(xmlTagVariant)[0];
			var variantId = variantNode.getAttribute(xmlAttributeId);
			var variantType = variantNode.getAttribute(xmlAttributeType);
			var variantFrom = variantNode.getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
			var variantTo = variantNode.getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
			var variantLetter = null;
			if(variantNode.getElementsByTagName(xmlTagLetter).length > 0){
				variantLetter = variantNode.getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
			}
			var variantDescription = variantNode.getElementsByTagName(xmlTagDescription)[0].childNodes[0].nodeValue + "";
			var variantQUAL, variantFILTER, variantINFO, variantFILTERIndex, variantINFOIndex;
			variantFILTERIndex = variantDescription.indexOf("FILTER");
			variantINFOIndex = variantDescription.indexOf("INFO");
			variantQUAL = (variantDescription.substring(0, variantFILTERIndex - 1)).split(":")[1];
			variantFILTER = variantDescription.substring(variantFILTERIndex, variantINFOIndex - 1).split(":")[1];
			variantINFO = (variantDescription.substring(variantINFOIndex)).substring(variantDescription.substring(variantINFOIndex).indexOf(":") + 1);
			$("#variantDetailContent_id").html(variantId);
			if(variantId!="."&&(/^rs/).test(variantId)){
				$("#variantDetailContent_link").html("dbSNP" + "<span style=\"font-size:14px\">↗</span>");
				document.getElementById("variantDetailContent_link").onclick = function(){
					window.open("http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?searchType=adhoc_search&type=rs&rs=" + variantId);
				};
			}else{
				$("#variantDetailContent_link").html("");
				document.getElementById("variantDetailContent_link").onclick = function(){};
			}
			$("#variantDetailContent_scale").html(chrNum + ":" + variantFrom + "-" + variantTo);
			$("#variantDetailContent_Type_Content").html(variantType);
			$("#variantDetailContent_QUAL_Content").html(variantQUAL);
			$("#variantDetailContent_FILTER_Content").html(variantFILTER);
			$("#variantDetailContent_INFO_Content").html(variantINFO);
			if(variantLetter){
				$("#variantDetailContent_Letter_trNode").css("display","table-row");
				$("#variantDetailContent_Letter_Content").html(variantLetter);
			}else{
				$("#variantDetailContent_Letter_trNode").css("display","none");
			}
			$("#variantDetailLoading").css("display","none");
			$("#variantDetailContent").css("display","block");
		}
	}
}

function mousedownOutsideVariantTooltip(evt){
	evt = evt || window.event;
	var eventTarget = evt.target || evt.srcElement;
	var flag=0;
	while(eventTarget){
		if(eventTarget==document.getElementById("variantDetailTooltip")){
			flag=1;
			break;
		}else{
			eventTarget = eventTarget.parentNode;
		}
	}
	if(flag==0) {
		document.getElementById("variantDetailTooltip").style.display = "none";
	}
}

function canvasClickForRead(evt){
	evt = evt || window.event;
	var evtMouseCoods = mouseCoords(evt);
	var evtCanvasX, evtCanvasY;

	var eventTarget = evt.target || evt.srcElement;
	var trNode = eventTarget.parentNode.parentNode;
	var trackId = trNode.id, trackSuperId;
	var i, j,k;
	
	evtCanvasX = evtMouseCoods.x - $(eventTarget).position().left;
	evtCanvasY = evtMouseCoods.y - $(eventTarget).position().top;

	for( i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == trackId) {
			trackSuperId = trackItems[i].superid ? trackItems[i].superid : trackId;
			break;
		}
	}
	
	if(i < trackItems.length) {
		for( j = 0; j < trackItems[i].details.length; j++) {
			if(evtCanvasX >= trackItems[i].details[j].left && evtCanvasX <= trackItems[i].details[j].right && evtCanvasY >= trackItems[i].details[j].top && evtCanvasY <= trackItems[i].details[j].bottom) {
				//show details
				break;
			}
		}
		if(j < trackItems[i].details.length) {
			//draw the pointer
			drawDecoriteminfopointer("readDetailTooltipPointer");
			//set the position & display
			$(document.getElementById("readDetailTooltip")).css("top", evtMouseCoods.y);
			$(document.getElementById("readDetailTooltip")).css("left", evtMouseCoods.x - 10);
			$(document.getElementById("readDetailTooltip")).css("display", "block");
			//set the cursor style & event handler
			$(eventTarget).css("cursor", "auto");
			$(document.getElementById("readDetailTooltip")).css("cursor", "auto");
			//document.body.style.cursor = "auto";
			$(document.getElementById("readDetailTooltip")).unbind("mouseenter");
			$(document.getElementById("readDetailTooltip")).mouseenter(function() {
				document.body.style.cursor = "auto";
			});
			$(document).bind("mousedown", mousedownOutsideReadTooltip);
						
			//Ajax to get the gene details
			getReadDetailHttpRequest(trackSuperId, trackItems[i].details[j].id, trackItems[i].details[j].from, trackItems[i].details[j].to);
			
		}
	}
}
function getReadDetailHttpRequest(trackId, id, from, to){
	var url = "servlet/test.do?" + "action=getDetail&tracks=" + trackId + "&id=" + id + "&start=" + from + "&end=" + to;
	XMLHttpReq3.onreadystatechange = handle_getReadDetailHttpRequest;
	XMLHttpReq3.open("GET", url, true);
	XMLHttpReq3.send(null);
}
function handle_getReadDetailHttpRequest(){
	if(XMLHttpReq3.readyState == 4) {
		if(XMLHttpReq3.status == 200) {
			var XMLDoc = XMLHttpReq3.responseXML;
			var readNode =  XMLDoc.getElementsByTagName(xmlTagRead)[0];
			var readId = readNode.getAttribute(xmlAttributeId);
			var readFrom = readNode.getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
			var readTo = readNode.getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
			var readScale = chrNum + ":" + readFrom + "-" + readTo;
			$("#readDetailContentTable").html("");
			$("#readDetailContentTable").append("<tr><td style=\"color:white;font:italic bold 12px Georgia;\" colspan=\"2\">" + readId + "<\/td><\/tr>");
			$("#readDetailContentTable").append("<tr style=\"color:white;font-size: 12px\"><td valign=\"top\">Scale:<\/td><td valign=\"top\">" + readScale + "<\/td><\/tr>");
			
			var i;
			for(i = 0; i < readNode.childNodes.length; i++){
				if(readNode.childNodes[i].nodeName != xmlTagFrom && readNode.childNodes[i].nodeName != xmlTagTo){
					$("#readDetailContentTable").append("<tr style=\"color:white;font-size: 12px;\"><td valign=\"top\">" + readNode.childNodes[i].nodeName + ":" + "<\/td><td valign=\"top\" style=\"width: 300px; word-break: break-all;\">" + readNode.childNodes[i].childNodes[0].nodeValue + "<\/td><\/tr>");
				}
			}
			
			$("#readDetailLoading").css("display","none");
			$("#readDetailContent").css("display","block");
		}
	}
}

function mousedownOutsideReadTooltip(evt){
	evt = evt || window.event;
	var eventTarget = evt.target || evt.srcElement;
	var flag=0;
	while(eventTarget){
		if(eventTarget==document.getElementById("readDetailTooltip")){
			flag=1;
			break;
		}else{
			eventTarget = eventTarget.parentNode;
		}
	}
	if(flag==0) {
		document.getElementById("readDetailTooltip").style.display = "none";
	}
}

function canvasMousemoveOnPP(evt) {
	evt = evt || window.event;
	var evtMouseCoods = mouseCoords(evt);
	var eventTarget = evt.target || evt.srcElement;
	var trNode = eventTarget.parentNode.parentNode.parentNode;
	var trackId = trNode.id;
	var i, j;
	var evtCanvasX, evtCanvasY;
	for( i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == trackId) {
			break;
		}
	}
	evtCanvasX = evtMouseCoods.x - $("#personalPannel").position().left - $(eventTarget).position().left;
	evtCanvasY = evtMouseCoods.y - $("#personalPannel").position().top - $(eventTarget).position().top;

	if(i < trackItems.length) {
		for( j = 0; j < trackItems[i].details.length; j++) {
			if(evtCanvasX >= trackItems[i].details[j].left && evtCanvasX <= trackItems[i].details[j].right && evtCanvasY >= trackItems[i].details[j].top && evtCanvasY <= trackItems[i].details[j].bottom) {
				//document.body.style.cursor = "pointer";
				$(eventTarget).css("cursor", "pointer");
				break;
			}
		}
	}else{
		if(trackId == personalPannel.Pvar.id){
			for( j = 0; j < personalPannel.Pvar.details.length; j++) {
				if(evtCanvasX >= personalPannel.Pvar.details[j].left && evtCanvasX <= personalPannel.Pvar.details[j].right && evtCanvasY >= personalPannel.Pvar.details[j].top && evtCanvasY <= personalPannel.Pvar.details[j].bottom) {
					//document.body.style.cursor = "pointer";
					$(eventTarget).css("cursor", "pointer");
					break;
				}
			}
		}
		if(trackId == personalPannel.Panno.id){
			for( j = 0; j < personalPannel.Panno.details.length; j++) {
				if(evtCanvasX >= personalPannel.Panno.details[j].left && evtCanvasX <= personalPannel.Panno.details[j].right && evtCanvasY >= personalPannel.Panno.details[j].top && evtCanvasY <= personalPannel.Panno.details[j].bottom) {
					//document.body.style.cursor = "pointer";
					$(eventTarget).css("cursor", "pointer");
					break;
				}
			}
		}
	}
	
	if(i < trackItems.length) {
		if(j >= trackItems[i].details.length) {
			$(eventTarget).css("cursor", "url(./image/Grabber.cur),auto");
		}
	}else{
		if(trackId == personalPannel.Pvar.id){
			if(j >= personalPannel.Pvar.details.length) {
				$(eventTarget).css("cursor", "url(./image/Grabber.cur),auto");
			}
		}
		if(trackId == personalPannel.Panno.id){
			if(j >= personalPannel.Panno.details.length) {
				$(eventTarget).css("cursor", "url(./image/Grabber.cur),auto");
			}
		}
	}
}

function canvasClickForVariantOnPP(evt){
	evt = evt || window.event;
	var evtMouseCoods = mouseCoords(evt);
	var evtCanvasX, evtCanvasY;

	var eventTarget = evt.target || evt.srcElement;
	var trNode = eventTarget.parentNode.parentNode.parentNode;
	var trackId = trNode.id, trackSuperId;
	var i, j, k;
	
	evtCanvasX = evtMouseCoods.x - $("#personalPannel").position().left - $(eventTarget).position().left;
	evtCanvasY = evtMouseCoods.y - $("#personalPannel").position().top - $(eventTarget).position().top;

	for( i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == trackId) {
			trackSuperId = trackItems[i].superid ? trackItems[i].superid : trackId;
			break;
		}
	}
	
	if(trackSuperId + "." == "undefined."){
		trackSuperId = initPvar_superid;
	}
	
	if(i < trackItems.length || trackId == personalPannel.Pvar.id) {
		if(i < trackItems.length){
			for( j = 0; j < trackItems[i].details.length; j++) {
				if(evtCanvasX >= trackItems[i].details[j].left && evtCanvasX <= trackItems[i].details[j].right && evtCanvasY >= trackItems[i].details[j].top && evtCanvasY <= trackItems[i].details[j].bottom) {
					//show details
					break;
				}
			}
		}else{
			for(k = 0; k < trackItems.length; k++){
				if("_" + trackItems[k].id == trackId){
					trackSuperId = trackItems[k].superid ? "_"+trackItems[k].superid : trackId;
				}
			}
			for( j = 0; j < personalPannel.Pvar.details.length; j++) {
				if(evtCanvasX >= personalPannel.Pvar.details[j].left && evtCanvasX <= personalPannel.Pvar.details[j].right && evtCanvasY >= personalPannel.Pvar.details[j].top && evtCanvasY <= personalPannel.Pvar.details[j].bottom) {
					break;
				}
			}
		}
		
		if((i < trackItems.length && j < trackItems[i].details.length )|| (personalPannel.Pvar.details.length && j < personalPannel.Pvar.details.length)) {
			//draw the pointer
			drawDecoriteminfopointer("variantDetailTooltipPointerOnPP");
			//set the position & display
			$(document.getElementById("variantDetailTooltipOnPP")).css("top", evtCanvasY + $(eventTarget).position().top - 3);
			$(document.getElementById("variantDetailTooltipOnPP")).css("left", evtCanvasX + $(eventTarget).position().left - 15);
			$(document.getElementById("variantDetailTooltipOnPP")).css("display", "block");
			//set the cursor style & event handler
			$(eventTarget).css("cursor", "auto");
			$(document.getElementById("variantDetailTooltipOnPP")).css("cursor", "auto");
			//document.body.style.cursor = "auto";
			$(document.getElementById("variantDetailTooltipOnPP")).unbind("mouseenter");
			$(document.getElementById("variantDetailTooltipOnPP")).mouseenter(function() {
				document.body.style.cursor = "auto";
			});
			$(document).bind("mousedown", mousedownOutsideVariantTooltipOnPP);
			
			//Ajax to get the gene details
			if(i < trackItems.length){
				getVariantDetailHttpRequest(trackSuperId, trackItems[i].details[j].id, trackItems[i].details[j].from, trackItems[i].details[j].to);
			}else{
				getVariantDetailHttpRequestOnPP(trackSuperId, personalPannel.Pvar.details[j].id, personalPannel.Pvar.details[j].from, personalPannel.Pvar.details[j].to);
			}
		}
	}
}

function getVariantDetailHttpRequestOnPP(trackId, id, from, to){
	var url = "servlet/test.do?" + "action=getDetail&tracks=" + trackId + "&id=" + id + "&start=" + from + "&end=" + to;
	XMLHttpReq3.onreadystatechange = handle_getVariantDetailHttpRequestOnPP;
	XMLHttpReq3.open("GET", url, true);
	XMLHttpReq3.send(null);
}

function handle_getVariantDetailHttpRequestOnPP(){
	if(XMLHttpReq3.readyState == 4) {
		if(XMLHttpReq3.status == 200) {
			var XMLDoc = XMLHttpReq3.responseXML;
			var variantNode =  XMLDoc.getElementsByTagName(xmlTagVariant)[0];
			var variantId = variantNode.getAttribute(xmlAttributeId);
			var variantType = variantNode.getAttribute(xmlAttributeType);
			var variantFrom = variantNode.getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
			var variantTo = variantNode.getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
			var variantLetter = null;
			var variant_dbSNPID = null;
			var variantdbSNPAnno = null;
			var variant_BLS_mate = null;
			if(variantNode.getElementsByTagName(xmlTagDbsnp).length > 0){
				variantdbSNPAnno = variantNode.getElementsByTagName(xmlTagDbsnp)[0].childNodes[0].nodeValue;
			}
			if(variantNode.getElementsByTagName(xmlTagLetter).length > 0){
				variantLetter = variantNode.getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
			}
			if(variantNode.getAttribute(xmlAttribute_dbSNPID)){
				variant_dbSNPID = variantNode.getAttribute(xmlAttribute_dbSNPID);
			}
			if(variantNode.getElementsByTagName(xmlTagMate).length > 0){
				variant_BLS_mate = variantNode.getElementsByTagName(xmlTagMate)[0].childNodes[0].nodeValue;
			}
			var variantDescription = variantNode.getElementsByTagName(xmlTagDescription)[0].childNodes[0].nodeValue + "";
			var variantQUAL, variantFILTER, variantINFO, variantFILTERIndex, variantINFOIndex;
			variantFILTERIndex = variantDescription.indexOf("FILTER");
			variantINFOIndex = variantDescription.indexOf("INFO");
			variantQUAL = (variantDescription.substring(0, variantFILTERIndex - 1)).split(":")[1];
			variantFILTER = variantDescription.substring(variantFILTERIndex, variantINFOIndex - 1).split(":")[1];
			variantINFO = (variantDescription.substring(variantINFOIndex)).substring(variantDescription.substring(variantINFOIndex).indexOf(":") + 1);
			$("#variantDetailContent_idOnPP").html(variantId);
			if(variantId!="."&&(/^rs/).test(variantId)){
				$("#variantDetailContent_linkOnPP").html("dbSNP" + "<span style=\"font-size:14px\">↗</span>");
				document.getElementById("variantDetailContent_linkOnPP").onclick = function(){
					window.open("http://www.ncbi.nlm.nih.gov/projects/SNP/snp_ref.cgi?searchType=adhoc_search&type=rs&rs=" + variantId);
				};
			}else{
				$("#variantDetailContent_linkOnPP").html("");
				document.getElementById("variantDetailContent_linkOnPP").onclick = function(){};
			}
			$("#variantDetailContent_scaleOnPP").html(chrNum + ":" + variantFrom + "-" + variantTo);
			$("#variantDetailContent_Type_ContentOnPP").html(variantType);
			$("#variantDetailContent_QUAL_ContentOnPP").html(variantQUAL);
			$("#variantDetailContent_FILTER_ContentOnPP").html(variantFILTER);
			$("#variantDetailContent_INFO_ContentOnPP").html(variantINFO);
			if(variantLetter){
				$("#variantDetailContent_Letter_trNodeOnPP").css("display","table-row");
				$("#variantDetailContent_Letter_ContentOnPP").html(variantLetter);
			}else{
				$("#variantDetailContent_Letter_trNodeOnPP").css("display","none");
			}
			if(variantdbSNPAnno){
				$("#variantDetailContent_dbSNPAnno_trNodeOnPP").css("display","table-row");
				$("#variantDetailContent_dbSNPAnno_ContentOnPP").html(variantdbSNPAnno);
			}else{
				$("#variantDetailContent_dbSNPAnno_trNodeOnPP").css("display","none");
			}
			if(variant_dbSNPID){
				$("#variantDetailContent_dbSNPID_trNodeOnPP").css("display","table-row");
				$("#variantDetailContent_dbSNPID_ContentOnPP").html(variant_dbSNPID);
			}else{
				$("#variantDetailContent_dbSNPID_trNodeOnPP").css("display","none");
			}
			if(variant_BLS_mate){
				$("#variantDetailContent_Mate_trNodeOnPP").css("display","table-row");
				$("#variantDetailContent_Mate_ContentOnPP").html(variant_BLS_mate);
			}else{
				$("#variantDetailContent_Mate_trNodeOnPP").css("display","none");
			}
			$("#variantDetailLoadingOnPP").css("display","none");
			$("#variantDetailContentOnPP").css("display","block");
		}
	}
}

function mousedownOutsideVariantTooltipOnPP(evt){
	evt = evt || window.event;
	var eventTarget = evt.target || evt.srcElement;
	var flag=0;
	while(eventTarget){
		if(eventTarget==document.getElementById("variantDetailTooltipOnPP")){
			flag=1;
			break;
		}else{
			eventTarget = eventTarget.parentNode;
		}
	}
	if(flag==0) {
		document.getElementById("variantDetailTooltipOnPP").style.display = "none";
	}
}

function canvasClickForRepeat(evt) {
	evt = evt || window.event;
	var evtMouseCoods = mouseCoords(evt);
	var evtCanvasX, evtCanvasY;

	var eventTarget = evt.target || evt.srcElement;
	var trNode = eventTarget.parentNode.parentNode;
	var trackId = trNode.id;

	//add by Liran for Pfanno and Pclns track details
	if(trackId==null || trackId==""){
		trackId=trNode.parentNode.id;
	}
	//add by Liran for Pfanno and Pclns track details
	var i, j,k;
	
	for( i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == trackId) {
			break;
		}
	}
	evtCanvasX = evtMouseCoods.x - $(eventTarget).position().left;
	evtCanvasY = evtMouseCoods.y - $(eventTarget).position().top;

	//add by Liran for Pfanno and Pclns track details
	var Pfanno_Pclns_Node=null;
	if(trackId.substring(0,1)=="_"){
		if(trackId==personalPannel.Pfanno.id){
			Pfanno_Pclns_Node=-1;
		} else {
			for(var Pclns_idx=0;Pclns_idx<personalPannel.Pclns.length;Pclns_idx++){
				if(trackId==personalPannel.Pclns[Pclns_idx].id){
					Pfanno_Pclns_Node=Pclns_idx;
				}
			}
		}

		evtCanvasX = evtCanvasX - $("#personalPannel").position().left;
		evtCanvasY = evtCanvasY - $("#personalPannel").position().top;
	}
	//add by Liran for Pfanno and Pclns track details


	if(i < trackItems.length) {
		for( j = 0; j < trackItems[i].details.length; j++) {
			if(evtCanvasX >= trackItems[i].details[j].left && evtCanvasX <= trackItems[i].details[j].right && evtCanvasY >= trackItems[i].details[j].top && evtCanvasY <= trackItems[i].details[j].bottom) {
				//show details
				break;
			}
		}
		if(j < trackItems[i].details.length) {
			//draw the pointer
			drawDecoriteminfopointer("repeatMaskDetailTooltipPointer");
			//set the position & display
			$(document.getElementById("repeatMaskDetailTooltip")).css("top", evtMouseCoods.y);
			$(document.getElementById("repeatMaskDetailTooltip")).css("left", evtMouseCoods.x - 10);
			$(document.getElementById("repeatMaskDetailTooltip")).css("display", "block");
			//set the cursor style & event handler
			$(eventTarget).css("cursor", "auto");
			$(document.getElementById("repeatMaskDetailTooltip")).css("cursor", "auto");
			//document.body.style.cursor = "auto";
			$(document.getElementById("repeatMaskDetailTooltip")).unbind("mouseenter");
			$(document.getElementById("repeatMaskDetailTooltip")).mouseenter(function() {
				document.body.style.cursor = "auto";
			});
			document.body.addEventListener("mousedown", mousedownOutsideRepeatTooltip, false);
						
			//Ajax to get the gene details
			getRepeatDetailHttpRequest(trackId, trackItems[i].details[j].id, trackItems[i].details[j].from, trackItems[i].details[j].to);
		}
	}
	if(Pfanno_Pclns_Node!=null && Pfanno_Pclns_Node==-1) {
		for( j = 0; j < personalPannel.Pfanno.details.length; j++) {
			if(evtCanvasX >= personalPannel.Pfanno.details[j].left && evtCanvasX <= personalPannel.Pfanno.details[j].right && evtCanvasY >= personalPannel.Pfanno.details[j].top && evtCanvasY <= personalPannel.Pfanno.details[j].bottom) {
				//show details
				break;
			}
		}
		if(j < personalPannel.Pfanno.details.length) {
			//draw the pointer
			drawDecoriteminfopointer("repeatMaskDetailTooltipPointer");
			//set the position & display
			$(document.getElementById("repeatMaskDetailTooltip")).css("top", evtMouseCoods.y);
			$(document.getElementById("repeatMaskDetailTooltip")).css("left", evtMouseCoods.x - 10);
			$(document.getElementById("repeatMaskDetailTooltip")).css("display", "block");
			//set the cursor style & event handler
			$(eventTarget).css("cursor", "auto");
			$(document.getElementById("repeatMaskDetailTooltip")).css("cursor", "auto");
			//document.body.style.cursor = "auto";
			$(document.getElementById("repeatMaskDetailTooltip")).unbind("mouseenter");
			$(document.getElementById("repeatMaskDetailTooltip")).mouseenter(function() {
				document.body.style.cursor = "auto";
			});
			document.body.addEventListener("mousedown", mousedownOutsideRepeatTooltip, false);
						
			//Ajax to get the gene details
			getRepeatDetailHttpRequest(trackId, personalPannel.Pfanno.details[j].id, personalPannel.Pfanno.details[j].from, personalPannel.Pfanno.details[j].to);
		}
	} else if(Pfanno_Pclns_Node!=null && Pfanno_Pclns_Node>=0) {
		for( j = 0; j < personalPannel.Pclns[Pfanno_Pclns_Node].details.length; j++) {
			if(evtCanvasX >= personalPannel.Pclns[Pfanno_Pclns_Node].details[j].left && evtCanvasX <= personalPannel.Pclns[Pfanno_Pclns_Node].details[j].right && evtCanvasY >= personalPannel.Pclns[Pfanno_Pclns_Node].details[j].top && evtCanvasY <= personalPannel.Pclns[Pfanno_Pclns_Node].details[j].bottom) {
				//show details
				break;
			}
		}
		if(j < personalPannel.Pclns[Pfanno_Pclns_Node].details.length) {
			//draw the pointer
			drawDecoriteminfopointer("repeatMaskDetailTooltipPointer");
			//set the position & display
			$(document.getElementById("repeatMaskDetailTooltip")).css("top", evtMouseCoods.y);
			$(document.getElementById("repeatMaskDetailTooltip")).css("left", evtMouseCoods.x - 10);
			$(document.getElementById("repeatMaskDetailTooltip")).css("display", "block");
			//set the cursor style & event handler
			$(eventTarget).css("cursor", "auto");
			$(document.getElementById("repeatMaskDetailTooltip")).css("cursor", "auto");
			//document.body.style.cursor = "auto";
			$(document.getElementById("repeatMaskDetailTooltip")).unbind("mouseenter");
			$(document.getElementById("repeatMaskDetailTooltip")).mouseenter(function() {
				document.body.style.cursor = "auto";
			});
			document.body.addEventListener("mousedown", mousedownOutsideRepeatTooltip, false);
						
			//Ajax to get the gene details
			getRepeatDetailHttpRequest(trackId, personalPannel.Pclns[Pfanno_Pclns_Node].details[j].id, personalPannel.Pclns[Pfanno_Pclns_Node].details[j].from, personalPannel.Pclns[Pfanno_Pclns_Node].details[j].to);
		}
	}
}

function getRepeatDetailHttpRequest(trackId, id, from, to){
	var url = "servlet/test.do?" + "action=getDetail&tracks=" + trackId + "&id=" + id + "&start=" + from + "&end=" + to;
	XMLHttpReq3.onreadystatechange = handle_getRepeatDetailHttpRequest;
	XMLHttpReq3.open("GET", url, true);
	XMLHttpReq3.send(null);
}

function handle_getRepeatDetailHttpRequest(){
	if(XMLHttpReq3.readyState == 4) {
		if(XMLHttpReq3.status == 200) {
			var XMLDoc = XMLHttpReq3.responseXML;
			var repeatsNode =  XMLDoc.getElementsByTagName(xmlTagElements)[0];
			var trackId = repeatsNode.getAttribute(xmlAttributeId);
			var repeatNode =  XMLDoc.getElementsByTagName(xmlTagElement)[0];
			var repeatId = repeatNode.getAttribute(xmlAttributeId);
			var repeatFrom = repeatNode.getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
			var repeatTo = repeatNode.getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
			
			$("#repeatMaskDetailContent_id").html(repeatId);
			$("#repeatMaskDetailContent_scale").html(chrNum + ":" + repeatFrom + "-" + repeatTo);
			if(repeatNode.getElementsByTagName(xmlTagDirection).length > 0){
				var repeatDirection = repeatNode.getElementsByTagName(xmlTagDirection)[0].childNodes[0].nodeValue;
				$("#repeatMaskDetailContent_direction_trNode").css("display","table-row");
				$("#repeatMaskDetailContent_direction_Content").html(repeatDirection);
			}else{
				$("#repeatMaskDetailContent_direction_trNode").css("display","none");
			}
			if(repeatNode.getElementsByTagName(xmlTagColor).length > 0){
				var repeatColor = repeatNode.getElementsByTagName(xmlTagColor)[0].childNodes[0].nodeValue;
				$("#repeatMaskDetail_color_trNode").css("display","table-row");
				$("#repeatMaskDetail_color_content").html(repeatColor);
			}else{
				$("#repeatMaskDetail_color_trNode").css("display","none");
			}
			if(repeatNode.getElementsByTagName(xmlTagDescription).length > 0){
				var repeatDes = repeatNode.getElementsByTagName(xmlTagDescription)[0].childNodes[0].nodeValue;
				$("#repeatMaskDetail_Des_trNode").css("display","table-row");
				$("#repeatMaskDetail_Des_content").html(repeatDes);
			}else{
				$("#repeatMaskDetail_Des_trNode").css("display","none");
			}
			if(trackId == "OMIM" || trackId =="_OMIM" ){
				var patternOMIM = /^.*,.*(\d{6}).*$/;
				if(patternOMIM.test(repeatId)){
					var OMIMentry = RegExp.$1;
					$("#repeatMaskDetailContent_link").css("display","block");
					document.getElementById("repeatMaskDetailContent_link").onclick = function(){
						window.open("http://omim.org/entry/"+OMIMentry);
					};
				}else{
					$("#repeatMaskDetailContent_link").css("display","none");
				//	document.getElementById("repeatMaskDetailContent_link").onclick = function(){};
				}
			}else{
					$("#repeatMaskDetailContent_link").css("display","none");
			}
			
			$("#repeatMaskDetailLoading").css("display","none");
			$("#repeatMaskDetailContent").css("display","block");
		}
	}
}
function mousedownOutsideRepeatTooltip(evt){
	evt = evt || window.event;
	var eventTarget = evt.target || evt.srcElement;
	var flag=0;
	while(eventTarget){
		if(eventTarget==document.getElementById("repeatMaskDetailTooltip")){
			flag=1;
			break;
		}else{
			eventTarget = eventTarget.parentNode;
		}
	}
	if(flag==0) {
		document.getElementById("repeatMaskDetailTooltip").style.display = "none";
	}
}
