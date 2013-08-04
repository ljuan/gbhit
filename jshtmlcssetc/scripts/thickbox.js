/*
 * Thickbox 3.1 - One Box To Rule Them All.
 * By Cody Lindley (http://www.codylindley.com)
 * Copyright (c) 2007 cody lindley
 * Licensed under the MIT License: http://www.opensource.org/licenses/mit-license.php
*/
		  
var tb_pathToImage = "./image/loading.gif";
var getParamsURL;
var currentTrack_IdOrSuperid_setParams;

/*!!!!!!!!!!!!!!!!! edit below this line at your own risk !!!!!!!!!!!!!!!!!!!!!!!*/

//on page load call tb_init
$(document).ready(function(){   
	tb_init('a.thickbox, area.thickbox, input.thickbox, span.thickbox, li.thickbox');//pass where to apply thickbox
	imgLoader = new Image();// preload image
	imgLoader.src = tb_pathToImage;
});

//add thickbox to href & area elements that have a class of .thickbox
function tb_init(domChunk){
	$(domChunk).click(function(){
	initialChrSelectList();//为了每次初始化上传数据的界面，自己添加的代码
	clearAllInput()//为了每次初始化上传数据的界面
	var t = this.title || this.name || null;
	var a = this.href || this.alt|| this.attributes["alt"].nodeValue;
	var g = this.rel || false;
	var x = this.id;
	getParamsURL = x;
	tb_show(t,a,g,x);
	this.blur();
	return false;
	});
}

function tb_show(caption, url, imageGroup,ajaxUrl) {//function called when the user clicks on a thickbox link

	try {
		if (typeof document.body.style.maxHeight === "undefined") {//if IE 6
			$("body","html").css({height: "100%", width: "100%"});
			$("html").css("overflow","hidden");
			if (document.getElementById("TB_HideSelect") === null) {//iframe to hide select elements in ie6
				$("body").append("<iframe id='TB_HideSelect'></iframe><div id='TB_overlay'></div><div id='TB_window'></div>");
				$("#TB_overlay").click(tb_remove);
			}
		}else{//all others
			if(document.getElementById("TB_overlay") === null){
				$("body").append("<div id='TB_overlay'></div><div id='TB_window'></div>");
				$("#TB_overlay").click(tb_remove);
			}
		}
		
		if(tb_detectMacXFF()){
			$("#TB_overlay").addClass("TB_overlayMacFFBGHack");//use png overlay so hide flash
		}else{
			$("#TB_overlay").addClass("TB_overlayBG");//use background and opacity
		}
		
		if(caption===null){caption="";}
		$("body").append("<div id='TB_load'><img src='"+imgLoader.src+"' /></div>");//add loader to the page
		$('#TB_load').show();//show loader
		
		var baseURL;
	   if(url.indexOf("?")!==-1){ //ff there is a query string involved
			baseURL = url.substr(0, url.indexOf("?"));
	   }else{ 
	   		baseURL = url;
	   }
	   
	   var urlString = /\.jpg$|\.jpeg$|\.png$|\.gif$|\.bmp$/;
	   var urlType = baseURL.toLowerCase().match(urlString);

		if(urlType == '.jpg' || urlType == '.jpeg' || urlType == '.png' || urlType == '.gif' || urlType == '.bmp'){//code to show images
				
			TB_PrevCaption = "";
			TB_PrevURL = "";
			TB_PrevHTML = "";
			TB_NextCaption = "";
			TB_NextURL = "";
			TB_NextHTML = "";
			TB_imageCount = "";
			TB_FoundURL = false;
			if(imageGroup){
				TB_TempArray = $("a[@rel="+imageGroup+"]").get();
				for (TB_Counter = 0; ((TB_Counter < TB_TempArray.length) && (TB_NextHTML === "")); TB_Counter++) {
					var urlTypeTemp = TB_TempArray[TB_Counter].href.toLowerCase().match(urlString);
						if (!(TB_TempArray[TB_Counter].href == url)) {						
							if (TB_FoundURL) {
								TB_NextCaption = TB_TempArray[TB_Counter].title;
								TB_NextURL = TB_TempArray[TB_Counter].href;
								TB_NextHTML = "<span id='TB_next'>&nbsp;&nbsp;<a href='#'>Next &gt;</a></span>";
							} else {
								TB_PrevCaption = TB_TempArray[TB_Counter].title;
								TB_PrevURL = TB_TempArray[TB_Counter].href;
								TB_PrevHTML = "<span id='TB_prev'>&nbsp;&nbsp;<a href='#'>&lt; Prev</a></span>";
							}
						} else {
							TB_FoundURL = true;
							TB_imageCount = "Image " + (TB_Counter + 1) +" of "+ (TB_TempArray.length);											
						}
				}
			}

			imgPreloader = new Image();
			imgPreloader.onload = function(){		
			imgPreloader.onload = null;
				
			// Resizing large images - orginal by Christian Montoya edited by me.
			var pagesize = tb_getPageSize();
			var x = pagesize[0] - 150;
			var y = pagesize[1] - 150;
			var imageWidth = imgPreloader.width;
			var imageHeight = imgPreloader.height;
			if (imageWidth > x) {
				imageHeight = imageHeight * (x / imageWidth); 
				imageWidth = x; 
				if (imageHeight > y) { 
					imageWidth = imageWidth * (y / imageHeight); 
					imageHeight = y; 
				}
			} else if (imageHeight > y) { 
				imageWidth = imageWidth * (y / imageHeight); 
				imageHeight = y; 
				if (imageWidth > x) { 
					imageHeight = imageHeight * (x / imageWidth); 
					imageWidth = x;
				}
			}
			// End Resizing
			
			TB_WIDTH = imageWidth + 80;
			TB_HEIGHT = imageHeight + 110;
			$("#TB_window").append("<a href='' id='TB_ImageOff' title='Close'><img id='TB_Image' src='"+url+"' width='"+imageWidth+"' height='"+imageHeight+"' alt='"+caption+"'/></a>" + "<div id='TB_caption'>"+caption+"<div id='TB_secondLine'>" + TB_imageCount + TB_PrevHTML + TB_NextHTML + "</div></div><div id='TB_closeWindow'><a href='#' id='TB_closeWindowButton' title='Close'>close</a> or Esc Key</div>"); 		
			
			$("#TB_closeWindowButton").click(tb_remove);
			
			if (!(TB_PrevHTML === "")) {
				function goPrev(){
					if($(document).unbind("click",goPrev)){$(document).unbind("click",goPrev);}
					$("#TB_window").remove();
					$("body").append("<div id='TB_window'></div>");
					tb_show(TB_PrevCaption, TB_PrevURL, imageGroup);
					return false;	
				}
				$("#TB_prev").click(goPrev);
			}
			
			if (!(TB_NextHTML === "")) {		
				function goNext(){
					$("#TB_window").remove();
					$("body").append("<div id='TB_window'></div>");
					tb_show(TB_NextCaption, TB_NextURL, imageGroup);				
					return false;	
				}
				$("#TB_next").click(goNext);
				
			}

			/*document.onkeydown = function(e){ 	
				if (e == null) { // ie
					keycode = event.keyCode;
				} else { // mozilla
					keycode = e.which;
				}
				if(keycode == 27){ // close
					tb_remove();
				} else if(keycode == 190){ // display previous image
					if(!(TB_NextHTML == "")){
						document.onkeydown = "";
						goNext();
					}
				} else if(keycode == 188){ // display next image
					if(!(TB_PrevHTML == "")){
						document.onkeydown = "";
						goPrev();
					}
				}	
			};*/
			
			tb_position();
			$("#TB_load").remove();
			$("#TB_ImageOff").click(tb_remove);
			$("#TB_window").css({display:"block"}); //for safari using css instead of show
			};
			
			imgPreloader.src = url;
		}else{//code to show html
			
			var queryString = url.replace(/^[^\?]+\??/,'');
			var params = tb_parseQuery( queryString );

			TB_WIDTH = (params['width']*1) + 80 || 630; //defaults to 630 if no paramaters were added to URL
			TB_HEIGHT = (params['height']*1) + 90 || 440; //defaults to 440 if no paramaters were added to URL
			ajaxContentW = TB_WIDTH - 30;
			ajaxContentH = TB_HEIGHT - 45;
			
			if(url.indexOf('TB_iframe') != -1){// either iframe or ajax window		
					urlNoQuery = url.split('TB_');
					$("#TB_iframeContent").remove();
					if(params['modal'] != "true"){//iframe no modal
						$("#TB_window").append("<div id='TB_title'><div id='TB_ajaxWindowTitle'>"+caption+"</div><div id='TB_closeAjaxWindow'><a href='#' id='TB_closeWindowButton' title='Close'>close</a></div></div><iframe frameborder='0' hspace='0' src='"+urlNoQuery[0]+"' id='TB_iframeContent' name='TB_iframeContent"+Math.round(Math.random()*1000)+"' onload='tb_showIframe()' style='width:"+(ajaxContentW + 29)+"px;height:"+(ajaxContentH + 17)+"px;' > </iframe>");
					}else{//iframe modal
					$("#TB_overlay").unbind();
						$("#TB_window").append("<iframe frameborder='0' hspace='0' src='"+urlNoQuery[0]+"' id='TB_iframeContent' name='TB_iframeContent"+Math.round(Math.random()*1000)+"' onload='tb_showIframe()' style='width:"+(ajaxContentW + 29)+"px;height:"+(ajaxContentH + 17)+"px;'> </iframe>");
					}
			}else{// not an iframe, ajax
					if($("#TB_window").css("display") != "block"){
						if(params['modal'] != "true"){//ajax no modal
						$("#TB_window").append("<div id='TB_title'><div id='TB_ajaxWindowTitle'>"+caption+"</div><div id='TB_closeAjaxWindow'><a href='#' id='TB_closeWindowButton'>close</a></div></div><div id='TB_ajaxContent' style='width:"+ajaxContentW+"px;height:"+ajaxContentH+"px'></div>");
						}else{//ajax modal
						$("#TB_overlay").unbind();
						$("#TB_window").append("<div id='TB_ajaxContent' class='TB_modal' style='width:"+ajaxContentW+"px;height:"+ajaxContentH+"px;'></div>");	
						}
					}else{//this means the window is already up, we are just loading new content via ajax
						$("#TB_ajaxContent")[0].style.width = ajaxContentW +"px";
						$("#TB_ajaxContent")[0].style.height = ajaxContentH +"px";
						$("#TB_ajaxContent")[0].scrollTop = 0;
						$("#TB_ajaxWindowTitle").html(caption);
					}
			}
					
			$("#TB_closeWindowButton").click(tb_remove);
			
				if(url.indexOf('TB_inline') != -1){	
					$("#TB_ajaxContent").append($('#' + params['inlineId']).children());
					$("#TB_window").unload(function () {
						$('#' + params['inlineId']).append( $("#TB_ajaxContent").children() ); // move elements back when you're finished
					});
					tb_position();
					$("#TB_load").remove();
					$("#TB_window").css({display:"block"}); 
				}else if(url.indexOf('TB_iframe') != -1){
					tb_position();
					if($.browser.safari){//safari needs help because it will not fire iframe onload
						$("#TB_load").remove();
						$("#TB_window").css({display:"block"});
					}
				}else{
					$("#TB_ajaxContent").load(url += "&random=" + (new Date().getTime()),function(){//to do a post change this load method
						tb_position();
						$("#TB_load").remove();
						tb_init("#TB_ajaxContent a.thickbox");
						$("#TB_window").css({display:"block"});
					});
				}
			
		}
		if(ajaxUrl)
			getParamsRequest(ajaxUrl);

		/*if(!params['modal']){
			document.onkeyup = function(e){ 	
				if (e == null) { // ie
					keycode = event.keyCode;
				} else { // mozilla
					keycode = e.which;
				}
				if(keycode == 27){ // close
					tb_remove();
				}	
			};
		}*/
		
	} catch(e) {
		//nothing here
	}
}

//XMLHttpReq是jump.js中定义的全局XMLHttpRequest对象
function getParamsRequest(url) {
	var settingPanel = document.getElementById("TB_ajaxContent");
	settingPanel.innerHTML = "";
	var imgEle = document.createElement("img");
	imgEle.setAttribute("src","./image/loading.gif");
	settingPanel.appendChild(imgEle);
	imgEle.setAttribute("style","position:absolute;left:35%; top:50%");
		
	XMLHttpReq.onreadystatechange = handleGetParamsRequestStateChange;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}
function handleGetParamsRequestStateChange(){
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			var XMLDoc = XMLHttpReq.responseXML;
			var settingPanel = document.getElementById("TB_ajaxContent");
			var paramsTable = document.createElement("table");
			var paramstrNode, paramstdNode;
			var paramId = (XMLDoc.getElementsByTagName(xmlTagParameters)[0]).getAttribute("id");
			currentTrack_IdOrSuperid_setParams = paramId;
			var paramNodes = XMLDoc.getElementsByTagName(xmlTagParameter);
			var paramStruct = [];
			for(var i=0;i<paramNodes.length;i++){
				paramstrNode = paramsTable.insertRow(-1);
				paramstrNode.setAttribute("style","vertical-align: top;");
				paramstdNode = paramstrNode.insertCell(-1);
				paramStruct[i] = [];
				paramStruct[i].id = paramNodes[i].getAttribute("id");
				paramStruct[i].type = paramNodes[i].getAttribute(xmlAttributeType);
				paramstdNode.innerHTML = paramStruct[i].id;
				if(paramNodes[i].getAttribute(xmlAttributeType)=="STRING"){
					paramstdNode.setAttribute("style","text-align:right;padding-top:3px;");
				}else if(paramNodes[i].getAttribute(xmlAttributeType)=="CHECKBOX"){
					paramstdNode.setAttribute("style","text-align:right;padding-top:4px;");
				}else{
					paramstdNode.setAttribute("style","text-align:right;padding-top:3px;");
				}
				paramstdNode = paramstrNode.insertCell(-1);
				if(paramNodes[i].getAttribute(xmlAttributeType)=="STRING"){
					paramstdNode.innerHTML = "<input type=\"text\" style=\" border:solid 1px #A3A3A3;\" name=\"" + paramStruct[i].id + "\" value=\""+ paramNodes[i].childNodes[0].nodeValue +"\"/>";
				}else if(paramNodes[i].getAttribute(xmlAttributeType)=="CHECKBOX"){
					var options = getNodeText(paramNodes[i].getElementsByTagName(xmlTagOptions)[0]).split(";");
					var optionsTable = document.createElement("table");
					var optionstrNode, optionstdNode;
					for(var j=0;j<options.length;j++){
						if(j%5==0){
							optionstrNode = optionsTable.insertRow(-1);
						}
						optionstdNode = optionstrNode.insertCell(-1);
						var option = options[j].split(':');
						var optionInnerHTML = "<input type=\"checkbox\"";
						if(option[1]=="1"){
							optionInnerHTML = optionInnerHTML + "checked=\"true\"";
						}
						optionInnerHTML = optionInnerHTML + " name=\""+ paramStruct[i].id +"\" value=\"" + option[0]+ "\"/>" + option[0];
						optionstdNode.innerHTML = optionInnerHTML;
					}
					var divBorderOfTable = document.createElement("div");
					divBorderOfTable.setAttribute("style","border:solid 1px #A3A3A3");
					paramstdNode.appendChild(divBorderOfTable);
					divBorderOfTable.appendChild(optionsTable);
					//paramstdNode.appendChild(optionsTable);
				}else if(paramNodes[i].getAttribute(xmlAttributeType)=="VCFSAMPLE"){
					var options = getNodeText(paramNodes[i].getElementsByTagName(xmlTagOptions)[0]).split(";");
					var optionsTable = document.createElement("table");
					//optionsTable.setAttribute("style","height:200px;overflow:scroll;");
					var optionstrNode, optionstdNode;
					for(var j=0;j<options.length;j++){
						if(j%5==0){
							optionstrNode = optionsTable.insertRow(-1);
						}
						optionstdNode = optionstrNode.insertCell(-1);
						var option = options[j].split(':');
						var optionInnerHTML = "<input type=\"checkbox\"";
						if(option[1]=="1"){
							optionInnerHTML = optionInnerHTML + "checked=\"true\"";
						}
						optionInnerHTML = optionInnerHTML + " name=\""+ paramStruct[i].id +"\" value=\"" + option[0]+ "\"/>" + option[0];
						/*if(option[1]=="1"){
							optionInnerHTML = optionInnerHTML + "checked=\"true\"" +"/>" + option[0];
						}else{
							optionInnerHTML = optionInnerHTML + "/>" + option[0];
						}*/
						optionstdNode.innerHTML = optionInnerHTML;
					}
					var divBorderOfTable = document.createElement("div");
					divBorderOfTable.setAttribute("style","height:100px;overflow:scroll;overflow-x:hidden;border:solid 1px #A3A3A3");
					paramstdNode.appendChild(divBorderOfTable);
					divBorderOfTable.appendChild(optionsTable);
					//paramstdNode.appendChild(optionsTable);
				}
			}
			settingPanel.innerHTML = "";
			var centerEle = document.createElement("center");
			centerEle.setAttribute("style","margin-top:25px;");
			settingPanel.appendChild(centerEle);
			centerEle.appendChild(paramsTable);
			
			var BtnTabEle = document.createElement("table");
			BtnTabEle.setAttribute("style","margin-top:40px;");
			var BtnTabTrEle = BtnTabEle.insertRow(-1);
			var BtnTabTdEle = BtnTabTrEle.insertCell(-1);
			BtnTabTdEle.setAttribute("style","text-align:center; width:200px;");
			BtnTabTdEle.innerHTML = "<input id=\"paramSubmit\" type=\"button\" value=\"Submit\"/>"
			BtnTabTdEle = BtnTabTrEle.insertCell(-1);
			BtnTabTdEle.setAttribute("style","text-align:center; width:200px;");
			BtnTabTdEle.innerHTML = "<input id=\"paramReset\" type=\"button\" value=\"Reset\"/>"
			centerEle.appendChild(BtnTabEle);
			
			document.getElementById("paramSubmit").onclick = function(p,pId){
				return function(){
					var trackId,trackmode,params="",values="",tempValue;
					trackId = pId;
					for(var i=0; i< p.length;i++){
						var valueNodes = document.getElementsByName(p[i].id);
						if(p[i].type == "STRING"){
							tempValue = valueNodes[0].value;
							if(tempValue!=""){
								params = params + p[i].id + ";";
								values = values + tempValue + ";";
							}
						}else if(p[i].type == "CHECKBOX"|| p[i].type == "VCFSAMPLE"){
							tempValue = "";
							for(var j=0; j<valueNodes.length;j++){
								if(valueNodes[j].checked){
									tempValue = tempValue + valueNodes[j].value + ":";
								}
							}
							params = params + p[i].id + ";";
							if(tempValue!=""){
								tempValue = tempValue.substr(0, tempValue.length-1);						
							}else{
								tempValue = "0";								
							}
							values = values + tempValue + ";";
						}
					}
					for(var i=0; i<trackItems.length;i++){
						if(trackItems[i].id == trackId){
							trackmode = trackItems[i].mode;
							break; 
						}else if(trackItems[i].superid == trackId){
							trackmode = trackItems[i].mode;
							break;
						}
					}
					if(params!=""){
						params = params.substr(0, params.length-1);
						values = values.substr(0, values.length-1);
						
						var setParamsURL = ("servlet/test.do?" + "action=setParams&tracks=" + trackId + "&params=" + params + "&values=" + values + "&modes=" + trackmode);
						setParamsRequest(setParamsURL);
					}
				};
			}(paramStruct, paramId);
			
			document.getElementById("paramReset").onclick = function(){
				getParamsRequest(getParamsURL);
			};
			
			//settingPanel.appendChild(paramsTable);
			//settingPanel.innerHTML = XMLHttpReq.responseXML;
		}
	}
}

function setParamsRequest(url){
	XMLHttpReq.onreadystatechange = handlesetParamsRequestStateChange;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}

function handlesetParamsRequestStateChange(){
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			tb_remove();
			
			var XMLDoc = XMLHttpReq.responseXML;
			var idTrackObj = [], superidTrackArray=[];
			var dataType_original , group_original , mode_original;
			var nodeXmltag = xmlTagVariants;
			var k =0;
			for(var i=0; i< trackItems.length;i++){
				if(trackItems[i].id == currentTrack_IdOrSuperid_setParams){
					idTrackObj.id = currentTrack_IdOrSuperid_setParams;
					idTrackObj.idx = i;
					dataType_original = trackItems[i].dataType;
					group_original = trackItems[i].group;
					mode_original = trackItems[i].mode;
					break;
				}
				if(trackItems[i].superid == currentTrack_IdOrSuperid_setParams){
					superidTrackArray[k] = [];
					superidTrackArray[k].id = trackItems[i].id;
					superidTrackArray[k].idx = i;
					dataType_original = trackItems[i].dataType;
					group_original = trackItems[i].group;
					mode_original = trackItems[i].mode;
					
					k++;
				}
			}
			
			/*
			if(dataType_original=="BAM"){
				
			}else if(dataType_original=="VCF"||dataType_original=="GVF"){
				
			}else if(dataType_original=="BED"||dataType_original=="BEDGZ"||dataType_original=="ANNO"||dataType_original=="GRF"||dataType_original=="GDF"){
				
			}else if(dataType_original=="BW"||dataType_original=="WIG"){
				
			}*/
			var variantsNodes = XMLDoc.getElementsByTagName(nodeXmltag);
		//	if(superidTrackArray.length>0|| variantsNodes[0].getAttribute("superid")){// ----Del by Liran for enable refresh of non-splitted vcf tracks
				if(superidTrackArray.length>0 && variantsNodes[0].getAttribute("superid")){
					for(var i=0; i< superidTrackArray.length;i++){
						trackItems.splice(superidTrackArray[i].idx-i,1);
						removeTrack(superidTrackArray[i].id);
					}
					for(var i=0; i<variantsNodes.length;i++){
						var tempTrackItemObj = [];
						tempTrackItemObj.id = variantsNodes[i].getAttribute("id");
						tempTrackItemObj.superid = variantsNodes[i].getAttribute("superid");
						tempTrackItemObj.mode = mode_original;
						tempTrackItemObj.dataType = dataType_original;
						tempTrackItemObj.group = group_original;
						tempTrackItemObj.isServer = 1;
						tempTrackItemObj.details = [];
						
						trackItems.push(tempTrackItemObj);
						
						createTrack(tempTrackItemObj.id, tempTrackItemObj.mode);
						
						var tempcanvasNode = document.getElementById(tempTrackItemObj.id).getElementsByTagName("canvas");
						showVariant(tempcanvasNode[0], tempcanvasNode[1], variantsNodes[i], mode_original);
					}
				}else if(superidTrackArray.length>0 && (!(variantsNodes[0].getAttribute("superid")))){
					for(var i=0; i< superidTrackArray.length;i++){
						trackItems.splice(superidTrackArray[i].idx-i,1);
						removeTrack(superidTrackArray[i].id);
						//
					}
					var tempTrackItemObj = [];
					tempTrackItemObj.id = variantsNodes[0].getAttribute("id");
					tempTrackItemObj.mode = mode_original;
					tempTrackItemObj.dataType = dataType_original;
					tempTrackItemObj.group = group_original;
					tempTrackItemObj.isServer = 1;
					tempTrackItemObj.details = [];
					
					trackItems.push(tempTrackItemObj);
	
					createTrack(tempTrackItemObj.id, tempTrackItemObj.mode);
						
					var tempcanvasNode = document.getElementById(tempTrackItemObj.id).getElementsByTagName("canvas");
					showVariant(tempcanvasNode[0], tempcanvasNode[1], variantsNodes[0], mode_original);
				}else{
					trackItems.splice(idTrackObj.idx,1);
					removeTrack(idTrackObj.id);
					
					for(var i=0; i<variantsNodes.length;i++){
						var tempTrackItemObj = [];
						tempTrackItemObj.id = variantsNodes[i].getAttribute("id");
						tempTrackItemObj.superid = variantsNodes[i].getAttribute("superid");
						tempTrackItemObj.mode = mode_original;
						tempTrackItemObj.dataType = dataType_original;
						tempTrackItemObj.group = group_original;
						tempTrackItemObj.isServer = 1;
						tempTrackItemObj.details = [];
						
						trackItems.push(tempTrackItemObj);
						
						createTrack(tempTrackItemObj.id, tempTrackItemObj.mode);
						
						var tempcanvasNode = document.getElementById(tempTrackItemObj.id).getElementsByTagName("canvas");
						showVariant(tempcanvasNode[0], tempcanvasNode[1], variantsNodes[i], mode_original);
					}
				}
				personalPannel.personalTrackItems.Pvars = [];
				for(var i=0; i< trackItems.length; i++){
					if(trackItems[i].dataType == "VCF" || trackItems[i].dataType == "GVF"){
						personalPannel.personalTrackItems.Pvars.push("_"+ trackItems[i].id);
					}
				}
		//	}
		// ----Del by Liran for enable refresh of non-splitted vcf tracks
		}
	}
}

//helper functions below
function tb_showIframe(){
	$("#TB_load").remove();
	$("#TB_window").css({display:"block"});
}

function tb_remove() {
 	$("#TB_imageOff").unbind("click");
	$("#TB_closeWindowButton").unbind("click");
	$("#TB_window").fadeOut("fast",function(){
		$('#TB_window,#TB_overlay,#TB_HideSelect').trigger("unload").unbind().remove();
		});
	$("#TB_load").remove();
	if (typeof document.body.style.maxHeight == "undefined") {//if IE 6
		$("body","html").css({height: "auto", width: "auto"});
		$("html").css("overflow","");
	}
	//document.onkeydown = "";
	//document.onkeyup = "";
	return false;
}

function tb_position() {
$("#TB_window").css({marginLeft: '-' + parseInt((TB_WIDTH / 2),10) + 'px', width: TB_WIDTH + 'px'});
	if ( !(jQuery.browser.msie && jQuery.browser.version < 7)) { // take away IE6
		$("#TB_window").css({marginTop: '-' + parseInt((TB_HEIGHT / 2),10) + 'px'});
	}
}

function tb_parseQuery ( query ) {
   var Params = {};
   if ( ! query ) {return Params;}// return empty object
   var Pairs = query.split(/[;&]/);
   for ( var i = 0; i < Pairs.length; i++ ) {
      var KeyVal = Pairs[i].split('=');
      if ( ! KeyVal || KeyVal.length != 2 ) {continue;}
      var key = unescape( KeyVal[0] );
      var val = unescape( KeyVal[1] );
      val = val.replace(/\+/g, ' ');
      Params[key] = val;
   }
   return Params;
}

function tb_getPageSize(){
	var de = document.documentElement;
	var w = window.innerWidth || self.innerWidth || (de&&de.clientWidth) || document.body.clientWidth;
	var h = window.innerHeight || self.innerHeight || (de&&de.clientHeight) || document.body.clientHeight;
	arrayPageSize = [w,h];
	return arrayPageSize;
}

function tb_detectMacXFF() {
  var userAgent = navigator.userAgent.toLowerCase();
  if (userAgent.indexOf('mac') != -1 && userAgent.indexOf('firefox')!=-1) {
    return true;
  }
}


