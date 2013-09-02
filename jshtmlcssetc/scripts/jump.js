/**
 * @author Hao Yafeng
 */
/*global variants stand for element tags of data-exchange xml */
var xmlTagAssembly = "Assembly";
var xmlTagChrNum = "Chromosome";
var xmlTagChrLength = "Length";
var xmlTagChrStart = "Start";
var xmlTagChrEnd = "End";
var xmlTagSequence = "Sequence";
var xmlTagCytobands = "Cbs";
var xmlTagElements = "Es";
var xmlTagReads = "Rs";
var xmlTagValues = "Values";
var xmlTagVariants = "Vs";
var xmlTagError = "Error";
var xmlTagRead = "R";
var xmlTagVariant = "V";
var xmlTagValueList = "ValueList";
var xmlTagCytoBand = "Cb";
var xmlTagElement = "E";
var xmlTagSubElement = "S";
var xmlTagAnnotationList = "AnnotationList";
var xmlTagFrom = "F";
var xmlTagTo = "T";
var xmlTagDirection = "s";
var xmlTagType = "Type";
var xmlTagStep = "Step";
var xmlTagColor = "Color";
var xmlTagLetter = "B";
var xmlTagDescription = "Des";
var xmlTagParameters = "Parameters";
var xmlTagParameter = "Parameter"
var xmlTagOptions = "Options";
var xmlTagDbsnp = "Dbsnp";
var xmlTagMate = "Mate";
var xmlTagStatus = "status";

var xmlGroupIG= "PersonalGenome";
var xmlParamSample= "Samples";

var xmlAttributeId = "id";
var xmlAttributeType = "Y";
var xmlAttributeEffect = "e";//added by Liran for recording variant effect
var xmlAttribute_gieStain = "gS";
var xmlAttributeSymbol = "Symbol";
var xmlAttribute_dbSNPID = "dd";

var trackDisplayModeHide = "hide";
var trackDispalyModeDense = "dense";
var trackDisplayModeSquish = "squish";
var trackDisplayModePack = "pack";
var trackDisplayModeFull = "full";

var subElementTypeBoxValue = "X";
var subElementTypeBandValue = "D";
var subElementTypeLineValue = "L";

var variantType_SNV = "SNV";
var variantType_CNV = "CNV";
var variantType_BLS = "BLS";
var variantType_DUPLICATION = "DUP";
var variantType_INSERTION = "INS";
var variantType_INVERSION = "INV";
var variantType_DELETION = "DEL";
var variantType_MUTIPLE = "MUL";
var variantType_OTHERS = "OTH";

var fileFormat_BED = "BED";
var fileFormat_BIGBED = "BB";
var fileFormat_BEDGRAPH = "BG";
var fileFormat_VCF = "VCF";
var fileFormat_WIG = "WIG";
var fileFormat_BIGWIG = "BW";
var fileFormat_BEDGZ = "BEDGZ";
var fileFormat_FASTA = "FASTA";
var fileFormat_REF = "REF";
var fileFormat_BAM = "BAM";
var fileFormat_GFF = "GFF";
var fileFormat_GTF = "GTF";
var fileFormat_GVF = "GVF";
var fileFormat_GDF = "GDF";
var fileFormat_GRF = "GRF";
var fileFormat_CYTO = "CYTO";
var fileFormat_ANNO = "ANNO";
var fileFormat_FUNCTIONANNO = "FANNO";
/*END of the definition about element tags' global variants*/

/*the constants of glyph about its' size*/
var height_gene_box = 10;
var height_gene_band = 5;
var height_variant = 10;
var height_read = 10;
var height_cytoband = 15;
var width_cytoband = 1000;
var space_vertical = 3;
// the horizontal space between two elements is determined by there position in the genome, so here is minimum value
var space_horizontal_min = 5;
var space_id_glyph = 3;
/*end of the glyphs's size constants' definition*/

/*style constants:color、font*/

/*end of style contants' definition*/
var start_user,end_user,searchLength_user;
var trackLength_user = 950;
var startIndex, endIndex, chrNum, assemblyNum, searchLength;
var trackLength = trackLength_user * 3;
//the search does or not occure in browser.html page
var pageFlag = 0;
//the length of chrosome
var chrLength = 123456789456;
var chrImgLength;
var chr_Lengths = [];
//record the show mode of tracks
var trackItems = [];
var loadingId;
var ppLoadingId;
var initPvar_superid;

var personalPannel = {};
personalPannel.Pvar = {
	id : "",
	mode : "",
};
personalPannel.Panno = {
	id : "",
	mode : "",
	num : 0
};
personalPannel.Pfanno = {
	id : "",
	mode : ""
};
personalPannel.Pclns = [];
var personalTrackItems = {};
personalTrackItems.Pvars = [];
personalTrackItems.Pannos = [];
personalTrackItems.Pfannos = [];
personalTrackItems.Pclnss = [];
personalPannel.personalTrackItems = personalTrackItems;
var scoremethPvar = "PGB"; // added by Liran for recording current scoring method

var control_scanning=0;//for stop scanning when close BrowseJumpWindow
/*$(function(){
	window.onunload = function(){
		window.location.href = window.location.href;
	};
});*/

$(function(){
	$("#backward").bind("click", historyback);
	$("#forward").bind("click", historyforward);
});
//$.ajaxSetup({cache:false});

function historyback(){
	history.back();
	var urlParameter = QueryString();
	assemblyNum = urlParameter["Assembly"];
	chrNum = urlParameter["Chr"];
	startIndex = parseInt(urlParameter["Start"]);
	endIndex = parseInt(urlParameter["End"]);
	if(startIndex > endIndex) {
		var start2end;
		start2end = endIndex;
		endIndex = startIndex;
		startIndex = start2end;
	}
	if(endIndex - startIndex + 1 < parseInt(trackLength / 10)) {
		var temp_d = parseInt(trackLength / 10) - (endIndex - startIndex + 1);
		if(temp_d % 2 == 0) {
			endIndex = endIndex + temp_d / 2;
			startIndex = startIndex - temp_d / 2;
		} else {
			endIndex = endIndex + parseInt(temp_d / 2);
			startIndex = startIndex - (parseInt(temp_d / 2) + 1)
		}
	}
	searchLength = endIndex - startIndex + 1;
	searchLength_user = Math.round(searchLength / 3);
	start_user = startIndex + searchLength_user;
	end_user = endIndex - searchLength_user;
		
	if(start_user < 1) {
		start_user = 1;
	}
	if(end_user > parseInt(chr_Lengths[chrNum])) {
		end_user = parseInt(chr_Lengths[chrNum]);
	}
	searchLength_user = end_user - start_user + 1;
	if(searchLength_user< trackLength_user/10){
		if(start_user ==1){
			end_user = trackLength_user/10;
		}else{
			start_user = end_user - trackLength_user/10 + 1;
		}
	}
	searchLength_user = end_user - start_user + 1;
	startIndex = start_user - searchLength_user;
	endIndex = end_user + searchLength_user;
	searchLength = searchLength_user * 3;
	showRefForHistory();
}

function historyforward(){
	history.forward();
	var urlParameter = QueryString();
	assemblyNum = urlParameter["Assembly"];
	chrNum = urlParameter["Chr"];
	startIndex = parseInt(urlParameter["Start"]);
	endIndex = parseInt(urlParameter["End"]);
	if(startIndex > endIndex) {
		var start2end;
		start2end = endIndex;
		endIndex = startIndex;
		startIndex = start2end;
	}
	if(endIndex - startIndex + 1 < parseInt(trackLength / 10)) {
		var temp_d = parseInt(trackLength / 10) - (endIndex - startIndex + 1);
		if(temp_d % 2 == 0) {
			endIndex = endIndex + temp_d / 2;
			startIndex = startIndex - temp_d / 2;
		} else {
			endIndex = endIndex + parseInt(temp_d / 2);
			startIndex = startIndex - (parseInt(temp_d / 2) + 1)
		}
	}
	searchLength = endIndex - startIndex + 1;
	searchLength_user = Math.round(searchLength / 3);
	start_user = startIndex + searchLength_user;
	end_user = endIndex - searchLength_user;
		
	if(start_user < 1) {
		start_user = 1;
	}
	if(end_user > parseInt(chr_Lengths[chrNum])) {
		end_user = parseInt(chr_Lengths[chrNum]);
	}
	searchLength_user = end_user - start_user + 1;
	if(searchLength_user< trackLength_user/10){
		if(start_user ==1){
			end_user = trackLength_user/10;
		}else{
			start_user = end_user - trackLength_user/10 + 1;
		}
	}
	searchLength_user = end_user - start_user + 1;
	startIndex = start_user - searchLength_user;
	endIndex = end_user + searchLength_user;
	searchLength = searchLength_user * 3;
	showRefForHistory();
}

function jump() {
	pageFlag = 0;
	var searchItemPattern = /chr([1-9]|1[0-9]|2[0-2]|X|Y|M):[0-9]+-[0-9]+$/;
	var searchInputNode = document.getElementById("search_field");
	var str = searchInputNode.value;
	str = str.replace(/\s+/g,"");
	str = str.replace(/(,)+/g,"");
	str = str.replace(/chrx/,"chrx");
	str = str.replace(/chry/,"chrY");
	str = str.replace(/chrm/,"chrM");
	if(!searchItemPattern.exec(str)) {
		alert("Please input correct form of your search item!");
		searchInputNode.value = "";
	} else {
		var strArray = str.split(/-|:/);
		chrNum = strArray[0];
		chrLength = chr_Lengths[chrNum];
		start_user = parseInt(strArray[1]);
		end_user = parseInt(strArray[2]);
		if(start_user > end_user){
			var temp_start2end = end_user;
			end_user = start_user;
			start_user = temp_start2end;
		}
		searchLength_user = end_user - start_user + 1;
		if(searchLength_user < parseInt(trackLength_user/10)){
			var temp_d = parseInt(trackLength_user/10) - searchLength_user;
			if(temp_d%2==0){
				start_user = start_user - temp_d/2;
				end_user = end_user + temp_d/2;
			}else{
				end_user = end_user + parseInt(temp_d/2);
				start_user = start_user - (parseInt(temp_d/2) + 1)
			}
		}
		if(start_user<1 || end_user> parseInt(chr_Lengths[chrNum])){
			if(start_user < 1){
				start_user = 1;
			}
			if(end_user > parseInt(chr_Lengths[chrNum])){
				end_user = parseInt(chr_Lengths[chrNum]);
			}
			searchLength_user = end_user - start_user + 1;
			if(searchLength_user < trackLength_user/10){
				if(start_user ==1){
					end_user = trackLength_user/10;
				}else{
					start_user = end_user - trackLength_user/10 + 1;
				}
				searchLength_user = end_user - start_user + 1;
			}
		}
		searchLength_user = end_user - start_user + 1;
		startIndex = start_user - searchLength_user;
		endIndex = end_user + searchLength_user;
		searchLength = searchLength_user * 3;
		
		setSliderMax();
		setSliderValue(searchLength_user);
		
		searchInputNode.value = "" + chrNum + ":" + addCommas(start_user) + "-" + addCommas(end_user);
		showuserSearchIndex(start_user, end_user);

		showRef();
	}
}

function showRefForHistory(){
	var url;
	url = "servlet/test.do?";
	url = url + "action=update";
	url = url + "&chr=";
	url = url + chrNum;
	url = url + "&start=";
	url = url + startIndex;
	url = url + "&end=";
	url = url + endIndex;
	url = url + "&width=";
	url = url + trackLength;
	
	window.localStorage.clear();

	updateRequest(url);
}

function showRef() {
	var url;
	url = "servlet/test.do?";
	url = url + "action=update";
	url = url + "&chr=";
	url = url + chrNum;
	url = url + "&start=";
	url = url + startIndex;
	url = url + "&end=";
	url = url + endIndex;
	url = url + "&width=";
	url = url + trackLength;
	
	var tempHref = "browser.html?Chr=";
	tempHref = tempHref + chrNum;
	tempHref = tempHref + "&Start=";
	tempHref = tempHref + startIndex;
	tempHref = tempHref + "&End=";
	tempHref = tempHref + endIndex;
//	tempHref = tempHref + "&width=" + trackLength;
	window.history.pushState("", "title", tempHref);

	window.localStorage.clear();

	updateRequest(url);
}

function createXMLHttpRequest() {
	var xmlHttp = null;
	try {
		// Firefox, Opera 8.0+, Safari
		xmlHttp = new XMLHttpRequest();
	} catch (e) {
		// Internet Explorer
		try {
			xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
		}
	}
	return xmlHttp;
}

var XMLHttpReq = createXMLHttpRequest();
var XMLHttpReq2 = createXMLHttpRequest();
//for getDetail
var XMLHttpReq3 = createXMLHttpRequest();
var XMLHttpReq4 = createXMLHttpRequest();
var XMLHttpReq5 = createXMLHttpRequest();
var XMLHttpReq6 = createXMLHttpRequest();//for individual setting.
var XMLHttpReq7 = createXMLHttpRequest();//for browse jump
var XMLHttpReq8 = createXMLHttpRequest();//for BJW_upStat
var XMLHttpReq10 = createXMLHttpRequest();//for BJW_scan
var showType;

function updateRequest(url) {
	if(document.getElementById(loadingId)) {
		hideLoadingImage(loadingId);
	}
	loadingId = showLoadingImage("tableTrack", "body");
	
	if(document.getElementById(ppLoadingId)){
		hideLoadingImage(ppLoadingId);
	}
	if($("#personalPannel").css("display") != "none"){
		ppLoadingId = showLoadingImage("ppContent", "#personalPannel");
	}

	/*$(".canvasTrackcontent").each(function(arrayindex, arrayele) {
		$(arrayele).width(trackLength);
		$(arrayele).css("left", - trackLength_user);
	});*/

	XMLHttpReq4.onreadystatechange = handleUpdateStateChange;
	XMLHttpReq4.open("GET", url, false);
	XMLHttpReq4.send(null);
}

function handleUpdateStateChange() {
	if(XMLHttpReq4.readyState == 4) {
		if(XMLHttpReq4.status == 200) {
			var XMLDoc = XMLHttpReq4.responseXML;
			var Errors = XMLDoc.getElementsByTagName(xmlTagError);
			if(Errors.length != 0) {
				var errorInfor = Errors[0].childNodes[0].nodeValue;
				var warnBox = document.getElementById("errorDiv");
				warnBox.style.display = "";
				if(pageFlag == 1) {
					document.getElementById("hiddenButton").style.display = "none";
					document.getElementById("returnButton").style.display = "";
				} else {
					document.getElementById("hiddenButton").style.display = "";
					document.getElementById("returnButton").style.display = "none";
				}
				var tempInnerHTML = "<li>";
				tempInnerHTML = tempInnerHTML + errorInfor;
				tempInnerHTML = tempInnerHTML + "</li>";
				document.getElementById("warnList").innerHTML = tempInnerHTML;
			} else {
				chrNum = XMLDoc.getElementsByTagName(xmlTagChrNum)[0].childNodes[0].nodeValue;
				chrLength = parseInt(XMLDoc.getElementsByTagName(xmlTagChrLength)[0].childNodes[0].nodeValue);
				searchLength = endIndex - startIndex + 1;
				if(end_user> chrLength){
					end_user = chrLength;
					searchLength_user = end_user - start_user + 1;
					startIndex = start_user - searchLength_user;
					endIndex = end_user + searchLength_user;
					searchLength = searchLength_user * 3;
				}
				
				setSliderValue(searchLength_user);
				
				drawScaleboxOnCytobandsImg(searchLength_user, start_user);
				
				showuserSearchIndex(start_user, end_user);
				
				tracksImgareaselect.setOptions({
					disable : true
				});

				var cytobandsNode = XMLDoc.getElementsByTagName(xmlTagCytobands)[0];
				if(cytobandsNode) {
					var cytobandsCanvas = document.getElementById("cytobandsCanvas");
					document.getElementById("chrNumTd").childNodes[0].nodeValue = chrNum;
					showCytoband(cytobandsCanvas, cytobandsNode);
				}

				var canvas = document.getElementById('thRefTrack1');
				var thCanvasWidth = canvas.width;

				if(canvas.getContext) {
					var ctx = canvas.getContext('2d');
					ctx.clearRect(0, 0, canvas.width, canvas.height);
					ctx.fillStyle = "#000000";
					var textWidth = ctx.measureText(chrNum);
					if(searchLength <= trackLength/10) {
						ctx.fillText(chrNum, thCanvasWidth - textWidth.width, 35);
					}else if(searchLength > trackLength/10 && searchLength <= trackLength/2){
						ctx.fillText(chrNum, thCanvasWidth - textWidth.width, 38);
					}else {
						ctx.fillText(chrNum, thCanvasWidth - textWidth.width, 27);
					}
				}

				var refSeq = null;

				var widthOfOneBase = trackLength / searchLength;
				if(searchLength <= trackLength/10) {//处理第一种显示情况
					refSeq = XMLDoc.getElementsByTagName(xmlTagSequence)[0].childNodes[0].nodeValue;
					drawRefSeq(widthOfOneBase, refSeq);
				} else if(searchLength > trackLength/10 && searchLength <= trackLength/2) {//处理第二种显示情况
					refSeq = XMLDoc.getElementsByTagName(xmlTagSequence)[0].childNodes[0].nodeValue;
					drawRefColor(widthOfOneBase, refSeq);
				} else {//处理第三种显示情况
					drawRefAxis();
				}
				var trackItem_i;
				for( trackItem_i = 0; trackItem_i < trackItems.length; trackItem_i++) {
					if(trackItems[trackItem_i].mode != "hide") {
						if(trackItems[trackItem_i].dataType == "BAM") {
							var readNodes = XMLDoc.getElementsByTagName(xmlTagReads);
							var readsNode;
							var readsId;
							var i;
							var refSeqTrackNode = document.getElementById(trackItems[trackItem_i].id);
							var refSeqCanvasNodes = refSeqTrackNode.getElementsByTagName("canvas");
							for( i = 0; i < readNodes.length; i++) {
								readsId = readNodes[i].getAttribute("id");
								if(readsId == trackItems[trackItem_i].id) {
									readsNode = readNodes[i];
									break;
								}
							}
							if(i == readNodes.length) {
								readNodes = XMLDoc.getElementsByTagName(xmlTagValues);
								for( i = 0; i < readNodes.length; i++) {
									readsId = readNodes[i].getAttribute("id");
									if(readsId == trackItems[trackItem_i].id) {
										readsNode = readNodes[i];
										break;
									}
								}
								showPositiveValueCombine(refSeqCanvasNodes[0], refSeqCanvasNodes[1], readsNode, trackItems[trackItem_i].mode, "topdown", 70, 50);
							} else {
								showRead(refSeqCanvasNodes[0], refSeqCanvasNodes[1], readsNode, trackItems[trackItem_i].mode, false);
							}
						} else if(trackItems[trackItem_i].dataType == "VCF" || trackItems[trackItem_i].dataType == "GVF") {
							var variantsNodes = XMLDoc.getElementsByTagName(xmlTagVariants);
							var variantsId;
							var variantsNode = null;
							if(variantsNodes.length > 0) {
								for(var i = 0; i < variantsNodes.length; i++) {
									variantsId = variantsNodes[i].getAttribute("id");
									if(variantsId == trackItems[trackItem_i].id) {
										variantsNode = variantsNodes[i];
										break;
									}
								}
								if(variantsNode != null) {
									var trackNode = document.getElementById(trackItems[trackItem_i].id);
									var canvasNodes = trackNode.getElementsByTagName("canvas");
									showVariant(canvasNodes[0], canvasNodes[1], variantsNode, trackItems[trackItem_i].mode);
								} else {
									var overScaleFlag = true;
									for(var i = 0; i < variantsNodes.length; i++) {
										if(variantsNodes[i].getAttribute("superid")) {
											if(variantsNodes[i].getAttribute("superid") == trackItems[trackItem_i].id) {
												if(document.getElementById(trackItems[trackItem_i].id)) {
													removeTrack(trackItems[trackItem_i].id);
												}
												var tt;
												for( tt = 0; tt < trackItems.length; tt++) {
													if(trackItems[tt].id == variantsNodes[i].getAttribute("id")) {
														break;
													}
												}
												if(tt >= trackItems.length) {
													var subtrackObj = [];
													subtrackObj.id = variantsNodes[i].getAttribute("id");
													subtrackObj.superid = variantsNodes[i].getAttribute("superid");
													subtrackObj.mode = trackItems[trackItem_i].mode;
													subtrackObj.dataType = trackItems[trackItem_i].dataType;
													subtrackObj.group = trackItems[trackItem_i].group;
													subtrackObj.isServer = trackItems[trackItem_i].isServer;
													subtrackObj.details = [];
													trackItems.push(subtrackObj);
													createTrack(subtrackObj.id,subtrackObj.mode);
												}
												overScaleFlag = false;
											}
											personalPannel.personalTrackItems.Pvars.push("_" + variantsNodes[i].getAttribute("id"));
											var Pvars_index;
											for( Pvars_index = 0; Pvars_index < personalPannel.personalTrackItems.Pvars.length; Pvars_index++) {
												if(personalPannel.personalTrackItems.Pvars[Pvars_index] == ("_" + variantsNodes[i].getAttribute("superid"))) {
													personalPannel.personalTrackItems.Pvars.splice(Pvars_index, 1);
													break;
												}
											}
										}
									}
									if(!overScaleFlag) {
										trackItems.splice(trackItem_i, 1);
										trackItem_i--;
									}
									if(overScaleFlag) {
										overScaleShow(trackItems[trackItem_i].id);
									}
								}
							} else {
								overScaleShow(trackItems[trackItem_i].id);
							}
						} else if(trackItems[trackItem_i].dataType == "BED" || trackItems[trackItem_i].dataType == "BEDGZ" || trackItems[trackItem_i].dataType == "ANNO" || trackItems[trackItem_i].dataType == "GRF" || trackItems[trackItem_i].dataType == "GDF") {
							var elementsNodes = XMLDoc.getElementsByTagName(xmlTagElements);
							var geneNode;
							var elementsId;
							for(var i = 0; i < elementsNodes.length; i++) {
								elementsId = elementsNodes[i].getAttribute("id");
								if(elementsId == trackItems[trackItem_i].id) {
									geneNode = elementsNodes[i];
									break;
								}
							}
							var refSeqTrackNode = document.getElementById(trackItems[trackItem_i].id);
							var refSeqCanvasNodes = refSeqTrackNode.getElementsByTagName("canvas");
							showGene(refSeqCanvasNodes[0], refSeqCanvasNodes[1], geneNode, trackItems[trackItem_i].mode);
						} else if(trackItems[trackItem_i].dataType == "BW" || trackItems[trackItem_i].dataType == "WIG") {
							var elementsNodes = XMLDoc.getElementsByTagName(xmlTagValues);
							var geneNode;
							var elementsId;
							for(var i = 0; i < elementsNodes.length; i++) {
								elementsId = elementsNodes[i].getAttribute("id");
								if(elementsId == trackItems[trackItem_i].id) {
									geneNode = elementsNodes[i];
									break;
								}
							}
							var refSeqTrackNode = document.getElementById(trackItems[trackItem_i].id);
							var refSeqCanvasNodes = refSeqTrackNode.getElementsByTagName("canvas");
							showValueCombine(refSeqCanvasNodes[0], refSeqCanvasNodes[1], geneNode, trackItems[trackItem_i].mode, "downtop", 100, 50);
						}
					}
				}

				hideLoadingImage(loadingId);
				
				if(personalPannel.Pvar.id){
					var element_nodes = XMLDoc.getElementsByTagName(xmlTagElements);
					var canvasNodes;
					//show personal genome
					var Pvar_node = null, variant_nodes = XMLDoc.getElementsByTagName(xmlTagVariants);
					for(var i = 0; i < variant_nodes.length; i++){
						if(personalPannel.Pvar.id == variant_nodes[i].getAttribute("id")){
							Pvar_node = variant_nodes[i];
							break;
						}
					}
					if(Pvar_node){
						canvasNodes = document.getElementById(personalPannel.Pvar.id).getElementsByTagName("canvas");
						showVariantByImg(canvasNodes[0], canvasNodes[1], Pvar_node, personalPannel.Pvar.mode);
					}else{
						overScaleShow(personalPannel.Pvar.id);
					}
					//show Pfanno
					if(personalPannel.Pfanno.id) {
						var personal_fanno_node = null;
						for(var i = 0; i < element_nodes.length; i++) {
							if(personalPannel.Pfanno.id == element_nodes[i].getAttribute("id")) {
								personal_fanno_node = element_nodes[i];
								break;
							}
						}
						if(personal_fanno_node){
							canvasNodes = document.getElementById(personalPannel.Pfanno.id).getElementsByTagName("canvas");
							showGene(canvasNodes[0], canvasNodes[1], personal_fanno_node, personalPannel.Pfanno.mode);
						}else{
							overScaleShow(personalPannel.Pfanno.id);
						}
					}
					//show Panno
					if(personalPannel.Panno.id) {
						var personal_anno_nodes = [];
						for(var i = 0; i < element_nodes.length; i++) {
							if(element_nodes[i].getAttribute("id") == personalPannel.Panno.id) {
								personal_anno_nodes.push(element_nodes[i]);
							}
						}
						canvasNodes = document.getElementById(personalPannel.Panno.id).getElementsByTagName("canvas");
						if(personal_anno_nodes.length == 1) {
							showPersonalGeneByImg_OneNode(canvasNodes[0], canvasNodes[1], personal_anno_nodes[0], personalPannel.Panno.mode);
						} else if(personal_anno_nodes.length == 2) {
							showPersonalGeneByImg_TwoNode(canvasNodes[0], canvasNodes[1], personal_anno_nodes[0], personal_anno_nodes[1], personalPannel.Panno.mode);
						}else{
							overScaleShow(personalPannel.Panno.id);
						}
					}
					//Pclns tracks
					if(personalPannel.Pclns.length > 0) {
						var pclns_flag;
						for(var i = 0; i < personalPannel.Pclns.length; i++) {
							pclns_flag = false;
							for(var j = 0; j < element_nodes.length; j++) {
								if(element_nodes[j].getAttribute("id") == personalPannel.Pclns[i].id) {
									pclns_flag = true;
									canvasNodes = document.getElementById(personalPannel.Pclns[i].id).getElementsByTagName("canvas");
									showGene(canvasNodes[0], canvasNodes[1], element_nodes[j], personalPannel.Pfanno.mode);
									break;
								}
							}
							if(!pclns_flag){
								overScaleShow(personalPannel.Pclns[i].id);
							}
						}
					}
					var ppTop = (document.body.clientHeight - $("#personalPannel").height() - 10);
					ppTop = ppTop > 50 ? ppTop : 50;
					$(window).resize();
					$("#personalPannel").animate({top:ppTop});
					$("#ppTrackTable tbody").sortable({axis:"y" ,cancel:".cannotSortable"});
				}
				
				if(document.getElementById(ppLoadingId)) {
					hideLoadingImage(ppLoadingId);
				}

				$(".canvasTrackcontent").each(function(arrayindex, arrayele) {
					$(arrayele).width(trackLength);
					$(arrayele).css("left", -trackLength_user);
				});
			}
		}
	}
	$( ".canvasTrackcontent" ).draggable({ 
		axis: "x" ,
		cursor: "url(./image/Grabber.cur),auto" ,
		drag : function(event, ui) {
			$(".canvasTrackcontent").each(function(arrayindex, arrayele) {
				$(arrayele).css("left", ui.position.left);
			});
			dragDragHandler(ui.position.left);
		},
		stop : function(event, ui){
			dragStopHandler(ui.position.left);
		}
	});
	trackItems_setting2();
}

function drawRefSeq(widthOfOneBase, refSeq) {
	var canvas = document.getElementById('refTrack1');
	var startIndex_axis, endIndex_axis;
	if(startIndex<1){
		startIndex_axis = 1;
	}else{
		startIndex_axis = startIndex;
	}
	if(endIndex> chrLength){
		endIndex_axis = chrLength;
	}else{
		endIndex_axis = endIndex;
	}
	if(canvas.getContext) {
		var ctx = canvas.getContext('2d');
		ctx.clearRect(0, 0, canvas.width, canvas.height);
		ctx.fillStyle = "#000000";
		//ctx.font = "10px Arial";
		ctx.textBaseline = "alphabetic";
		//y is the base coordinate in y axis,textBaseline is bootom(default)
		//bg_y is the y coordinate of the base_background
		//coordinate_y and coordiante_tag_y are the y coordinate of the coordinate
		var i, x, y = 35, bg_x, bg_x_next, bg_y = 26;
		var coordinate_tag_y = 14, coordinate_y = 23;
		var coordinate_refSeq = startIndex_axis, width_coordinate = ctx.measureText(addCommas(endIndex_axis)).width;
		//1.5 is a coefficient(>=1)
		var baseNumOftwoCoordinate = width_coordinate * 1.5 / widthOfOneBase;
		var x_coordinate;
		if(baseNumOftwoCoordinate <= 1) {
			baseNumOftwoCoordinate = 1;
		} else if(baseNumOftwoCoordinate <= 5) {
			baseNumOftwoCoordinate = 5;
		} else {
			baseNumOftwoCoordinate = parseInt(baseNumOftwoCoordinate / 10) * 10 + 10;
		}
		var coordinate_refSeq_first;
		if(baseNumOftwoCoordinate == 1) {
			coordinate_refSeq_first = startIndex_axis;
		} else if(baseNumOftwoCoordinate == 5) {
			i = 0;
			while(i < searchLength) {
				coordinate_refSeq = startIndex_axis + i;
				if(coordinate_refSeq % 5 == 0 && width_coordinate < (i + 1) * widthOfOneBase - 4) {
					coordinate_refSeq_first = coordinate_refSeq;
					break;
				}
				i++;
			}
		} else {
			i = 0;
			while(i < searchLength) {
				coordinate_refSeq = startIndex_axis + i;
				if(coordinate_refSeq % 10 == 0 && width_coordinate < (i + 1) * widthOfOneBase - 4) {
					coordinate_refSeq_first = coordinate_refSeq;
					break;
				}
				i++;
			}
		}
		coordinate_refSeq = coordinate_refSeq_first;
		while(coordinate_refSeq <= endIndex_axis) {
			x_coordinate = Math.round((coordinate_refSeq - startIndex + 1) * widthOfOneBase) - 1;
			ctx.fillRect(x_coordinate, coordinate_tag_y, 1, 9);
			ctx.fillStyle = "#000";
			ctx.textAlign = "right";
			ctx.fillText(addCommas(coordinate_refSeq), x_coordinate - 2, coordinate_y);
			coordinate_refSeq = coordinate_refSeq + baseNumOftwoCoordinate;
		}
		var length_beforeCoordinate = (startIndex_axis - startIndex)/ searchLength * trackLength;
		for( i = 0; i < refSeq.length; i++) {
			bg_x = Math.round(i * widthOfOneBase);
			bg_x_next = Math.round((i + 1) * widthOfOneBase);
			if(refSeq[i] == 'A' || refSeq[i] == 'a') {
				ctx.fillStyle = "rgb(249,194,56)";
			} else if(refSeq[i] == 'C' || refSeq[i] == 'c') {
				ctx.fillStyle = "rgb(236,95,75)";
			} else if(refSeq[i] == 'G' || refSeq[i] == 'g') {
				ctx.fillStyle = "rgb(122,197,131)";
			} else if(refSeq[i] == 'T' || refSeq[i] == 't'){
				ctx.fillStyle = "rgb(133,122,185)";
			}else{
				ctx.fillStyle = "rgb(163,165,167)";
			}
			ctx.fillRect(bg_x + length_beforeCoordinate, bg_y, bg_x_next - bg_x, 10);
			//plot the bases,textAlign is "center" mode
			x = Math.round((bg_x_next + bg_x) / 2);
			ctx.fillStyle = "#000";
			ctx.textAlign = "center";
			ctx.fillText(refSeq[i], x + length_beforeCoordinate, y);
		}
	}
}

function insertSettingBtn(trackId, ifParam, superId) {
	if(ifParam == "true") {
		var trackNode = document.getElementById(trackId);
		var trackOperatorNode = trackNode.getElementsByClassName("trackOperator")[0];
		if(trackOperatorNode.getElementsByClassName("setting").length == 0) {
			var spanNode = document.createElement("span");
			spanNode.setAttribute("class", "setting thickbox");
			spanNode.setAttribute("title", "track parameters setting");
			spanNode.setAttribute("alt", "#TB_inline?height=300;width=650;inlineId=tracksettingDIV");
			spanNode.setAttribute("id", "servlet/test.do?action=getParams&tracks=" + superId);
			trackOperatorNode.appendChild(spanNode);
			tb_init(spanNode);
		}
	}
}

function drawRefColor(widthOfOneBase, refSeq) {
	var canvas = document.getElementById('refTrack1');
	var startIndex_axis, endIndex_axis;
	if(startIndex<1){
		startIndex_axis = 1;
	}else{
		startIndex_axis = startIndex;
	}
	if(endIndex> chrLength){
		endIndex_axis = chrLength;
	}else{
		endIndex_axis = endIndex;
	}
	if(canvas.getContext) {
		var ctx = canvas.getContext('2d');
		ctx.clearRect(0, 0, canvas.width, canvas.height);
		var i, x, y = 21, height = 24;

		var coordinate_tag_y = 8, coordinate_y = 8;
		var coordinate_refSeq = startIndex_axis, width_coordinate = ctx.measureText(addCommas(endIndex_axis)).width;
		//1.5 is a coefficient(>=1)
		var baseNumOftwoCoordinate = width_coordinate * 1.5 / widthOfOneBase;
		if(baseNumOftwoCoordinate <= 1) {
			baseNumOftwoCoordinate = 1;
		} else if(baseNumOftwoCoordinate <= 5) {
			baseNumOftwoCoordinate = 5;
		} else {
			baseNumOftwoCoordinate = parseInt(baseNumOftwoCoordinate / 10) * 10 + 10;
		}
		//找开始坐标
		var coordinate_refSeq_first;
		if(baseNumOftwoCoordinate == 1) {
			coordinate_refSeq_first = startIndex_axis;
		} else if(baseNumOftwoCoordinate == 5) {
			i = 0;
			while(i < searchLength) {
				coordinate_refSeq = startIndex_axis + i;
				if(coordinate_refSeq % 5 == 0 && width_coordinate < (i + 1) * widthOfOneBase - 4) {
					coordinate_refSeq_first = coordinate_refSeq;
					break;
				}
				i++;
			}
		} else {
			i = 0;
			while(i < searchLength) {
				coordinate_refSeq = startIndex_axis + i;
				if(coordinate_refSeq % 10 == 0 && width_coordinate < (i + 1) * widthOfOneBase - 4) {
					coordinate_refSeq_first = coordinate_refSeq;
					break;
				}
				i++;
			}
		}
		//画坐标
		var x_coordinate;
		coordinate_refSeq = coordinate_refSeq_first;
		while(coordinate_refSeq <= endIndex_axis) {
			x_coordinate = Math.round((coordinate_refSeq - startIndex + 1) * widthOfOneBase) - 1;
			ctx.fillStyle = "#000";
			ctx.fillRect(x_coordinate, coordinate_tag_y, 1, 10);
			ctx.textAlign = "right";
			ctx.textBaseline = "top";
			ctx.fillText(addCommas(coordinate_refSeq), x_coordinate - 2, coordinate_y);
			coordinate_refSeq = coordinate_refSeq + baseNumOftwoCoordinate;
		}
		//画剪辑颜色条
		var length_beforeCoordinate = (startIndex_axis - startIndex)/ searchLength * trackLength;
		for(i=0; i< refSeq.length;i++){
			x = Math.round(i * widthOfOneBase);
			if(refSeq[i] == 'A') {
				ctx.fillStyle = "rgb(249,194,56)";
			} else if(refSeq[i] == 'C') {
				ctx.fillStyle = "rgb(236,95,75)";
			} else if(refSeq[i] == 'G') {
				ctx.fillStyle = "rgb(122,197,131)";
			} else if(refSeq[i] == 'T'){
				ctx.fillStyle = "rgb(133,122,185)";
			}else{
				ctx.fillStyle = "rgb(163,165,167)"
			}
			ctx.fillRect(x + length_beforeCoordinate, y, Math.round((i + 1) * widthOfOneBase) - x, height);
		}
	}
}

function drawRefAxis() {//规定最小显示粒度为10个pixel
	var canvas = document.getElementById('refTrack1');
	var startIndex_axis, endIndex_axis;
	if(startIndex<1){
		startIndex_axis = 1;
	}else{
		startIndex_axis = startIndex;
	}
	if(endIndex> chrLength){
		endIndex_axis = chrLength;
	}else{
		endIndex_axis = endIndex;
	}
	if(canvas.getContext) {
		var ctx = canvas.getContext('2d');
		ctx.clearRect(0, 0, canvas.width, canvas.height);
		ctx.fillStyle = "#000000";
		var minPixels = 5;
		var n = trackLength / minPixels;
		var bpNumPer = searchLength / n;
		var bitNumOfbpNumPer = calcBitNum(bpNumPer);

		var bpNumResult;
		if(Math.pow(10, bitNumOfbpNumPer - 1) != bpNumPer) {
			bpNumResult = Math.pow(10, bitNumOfbpNumPer);
		} else {
			bpNumResult = Math.pow(10, bitNumOfbpNumPer - 1);
		}
		//找到第一个小格的起始位置
		var width = trackLength / searchLength * bpNumResult;
		var bpNumFromStartToFirst = bpNumResult - startIndex % bpNumResult;
		var firstX = bpNumFromStartToFirst / bpNumResult * width;

		var widthOfAxis = trackLength;
		var yOfAxis = 25;
		
		var axis_start_in_canvas = (startIndex_axis - startIndex) / searchLength * trackLength;
		var axis_end_in_canvas = (endIndex_axis - startIndex + 1) / searchLength * trackLength;
		var axis_width_in_canvas = axis_end_in_canvas - axis_start_in_canvas + 1;

		ctx.fillRect(axis_start_in_canvas, yOfAxis, axis_width_in_canvas, 1);

		var temp_x = firstX;
		var i = 0;
		var indexOfAxis = startIndex + bpNumFromStartToFirst;
		var index_str = "";
		while(temp_x< widthOfAxis){	
			if(temp_x >= axis_start_in_canvas && temp_x <= axis_end_in_canvas) {
				if(indexOfAxis % (10 * bpNumResult) == 0) {
					ctx.beginPath();
					ctx.moveTo(temp_x + 0.5, yOfAxis - 10);
					ctx.lineTo(temp_x + 0.5, yOfAxis);
					ctx.closePath();
					ctx.stroke();
					index_str = "";
					index_str = index_str + scientificNotation(indexOfAxis, 10 * bpNumResult);
					ctx.textAlign = "center";
					ctx.fillText(index_str, temp_x, yOfAxis + 12);
					ctx.textAlign = "start";
				} else {
					ctx.beginPath();
					ctx.moveTo(temp_x + 0.5, yOfAxis - 4);
					ctx.lineTo(temp_x + 0.5, yOfAxis);
					ctx.closePath();
					ctx.stroke();
				}
			}
			i++;
			temp_x = parseInt(i * width + firstX);
			indexOfAxis = indexOfAxis + bpNumResult;
		}
	}
}

function calcBitNum(x) {
	var bitNum = 0;
	while(parseInt(x / Math.pow(10, bitNum))) {
		bitNum++;
	}
	return bitNum;
}

function addCommas(nStr) {
	nStr += '';
	var x = nStr.split('.');
	var x1 = x[0];
	var x2 = x.length > 1 ? '.' + x[1] : '';
	var rgx = /(\d+)(\d{3})/;
	while(rgx.test(x1)) {
		x1 = x1.replace(rgx, '$1' + ',' + '$2');
	}
	return x1 + x2;
}

function clearComma(strNum) {
	return strNum.replace(/,/g, "");
}

function scientificNotation(num, measurement){
	var result = num;
	switch(measurement) {
		case 10:
			result = addCommas(num);
			break;
		case 100:
		case 1000:
		case 10000:
			result = addCommas(num / 1000) + "K";
			break;
		case 100000:
		case 1000000:
		case 10000000:
			result = addCommas(num / 1000000) + "M";
			break;
		default:
			result = addCommas(num / 100000000) + "G";
	}
	return result;
}

function showRead(canvas1, canvas2, readNode, mode, isShowId) {
	var readNodeName = readNode.getAttribute(xmlAttributeId);
	var readNodes = readNode.getElementsByTagName(xmlTagRead);
	var readIds = [], readFroms = [], readTos = [], readDireactions = [], readMapqs = [];
	var readRelativeFroms = [], readRelativeTos = [], readRelativeWidth;
	var variantNodes = [], variantIds = [], variantTypes = [];
	var variantFroms = [], variantTos = [], variantLetters = [], variantDescriptions = [];
	var variantRelativeFroms = [], variantRelativeTos = [];

	var i, j, k, y;
	var trackItemIndex;
	
	for( i = 0; i < trackItems.length; i++) {
		if(readNodeName == trackItems[i].id) {
			trackItemIndex = i;
			break;
		}
	}
	
	if(trackItemIndex< trackItems.length){
		trackItems[trackItemIndex].details = [];
	}
	
	for( i = 0; i < readNodes.length; i++) {
		readFroms[i] = (readNodes[i].firstChild.childNodes[0].nodeValue).split(',');
		readTos[i] = (readNodes[i].childNodes[1].childNodes[0].nodeValue).split(',');
		readDireactions[i] = readNodes[i].childNodes[2].childNodes[0].nodeValue;
		readMapqs[i] = readNodes[i].childNodes[3].childNodes[0].nodeValue;
		readIds[i] = readNodes[i].getAttribute(xmlAttributeId);

		readRelativeFroms[i] = [];
		readRelativeTos[i] = [];
		for( j = 0; j < readFroms[i].length; j++) {
			readRelativeFroms[i][j] = parseInt((parseInt(readFroms[i][j]) - startIndex) / searchLength * trackLength);
			readRelativeTos[i][j] = parseInt((parseInt(readTos[i][j]) - startIndex + 1) / searchLength * trackLength);
		}
		
		if(trackItemIndex< trackItems.length){
			trackItems[trackItemIndex].details[i] = [];
			trackItems[trackItemIndex].details[i].id = readIds[i];
			trackItems[trackItemIndex].details[i].from = readFroms[i][0];
			trackItems[trackItemIndex].details[i].to = readTos[i][readTos[i].length - 1];
			trackItems[trackItemIndex].details[i].left = readRelativeFroms[i][0];
			trackItems[trackItemIndex].details[i].right = readRelativeTos[i][readRelativeTos[i].length - 1];
		}

		variantNodes[i] = readNodes[i].getElementsByTagName(xmlTagVariant);
		variantIds[i] = [];
		variantTypes[i] = [];
		variantFroms[i] = [];
		variantTos[i] = [];
		variantLetters[i] = [];
		variantDescriptions[i] = [];
		variantRelativeFroms[i] = [];
		variantRelativeTos[i] = [];

		for( j = 0; j < variantNodes[i].length; j++) {
			variantFroms[i][j] = parseInt(variantNodes[i][j].firstChild.childNodes[0].nodeValue);
			variantTos[i][j] = parseInt(variantNodes[i][j].childNodes[1].childNodes[0].nodeValue);

			if(variantFroms[i][j] >= startIndex && variantTos[i][j] <= endIndex) {
				variantIds[i][j] = variantNodes[i][j].getAttribute("id");
				variantTypes[i][j] = variantNodes[i][j].getAttribute(xmlAttributeType);
				if(variantTypes[i][j] == variantType_DELETION) {
					//variantDescriptions[i][j] = variantNodes[i][j].childNodes[2].childNodes[0].nodeValue;
					variantLetters[i][j] = "×";
				} else {//other Type:SNV,INS
					variantLetters[i][j] = variantNodes[i][j].childNodes[2].childNodes[0].nodeValue;
					//variantDescriptions[i][j] = variantNodes[i][j].childNodes[3].childNodes[0].nodeValue;
				}

				variantRelativeFroms[i][j] = parseInt((variantFroms[i][j] - startIndex ) / searchLength * trackLength);
				variantRelativeTos[i][j] = parseInt((variantTos[i][j] - startIndex + 1) / searchLength * trackLength);
			}
		}
	}

	if(canvas1.getContext && canvas2.getContext) {
		var ctx1 = canvas1.getContext('2d');
		var ctx2 = canvas2.getContext('2d');
		if(readNodes.length == 0) {
			canvas1.height = 10;
			canvas1.style.height = 10;
			canvas2.height = 10;
			canvas2.style.height = 10;
			ctx1.strokeStyle = "#000";
			ctx1.fillStyle = "#000";
			ctx1.fillText(readNodeName, canvas1.width - ctx1.measureText(readNodeName).width, 8);
		} else {
			if(mode == "dense") {
				canvas1.height = 10;
				canvas2.height = 10;
				canvas1.style.height = 10;
				canvas2.style.height = 10;

				ctx1.strokeStyle = "#000";
				ctx1.fillStyle = "#000";
				ctx1.fillText(readNodeName, canvas1.width - ctx1.measureText(readNodeName).width, 8);

				for( i = 0; i < readNodes.length; i++) {
					if(readDireactions[i] == "+") {
						if(parseInt(readMapqs[i]) == 0) {
							ctx2.strokeStyle = "#6ff9f";
							ctx2.fillStyle = "#6fff9f";
						} else if(0 < parseInt(readMapqs[i]) <= 10) {
							ctx2.strokeStyle = "#4eee94";
							ctx2.fillStyle = "#4eee94";
						} else if(10 < parseInt(readMapqs[i]) <= 20) {
							ctx2.strokeStyle = "#43cd80";
							ctx2.fillStyle = "#43cd80";
						} else if(20 < parseInt(readMapqs[i]) <= 30) {
							ctx2.strokeStyle = "#5fa080";
							ctx2.fillStyle = "#5fa080";
						} else {//  >30的情况，一般最大到40
							ctx2.strokeStyle = "#458b74";
							ctx2.fillStyle = "#458b74";
						}
					} else {
						if(parseInt(readMapqs[i]) == 0) {
							ctx2.strokeStyle = "#8deeff";
							ctx2.fillStyle = "#8deeff";
						} else if(0 < parseInt(readMapqs[i]) <= 10) {
							ctx2.strokeStyle = "#70caee";
							ctx2.fillStyle = "#70caee";
						} else if(10 < parseInt(readMapqs[i]) <= 20) {
							ctx2.strokeStyle = "#48acff";
							ctx2.fillStyle = "#48acff";
						} else if(20 < parseInt(readMapqs[i]) <= 30) {
							ctx2.strokeStyle = "#3a85ff";
							ctx2.fillStyle = "#3a85ff";
						} else {//  >30的情况，一般最大到40
							ctx2.strokeStyle = "#3a6acd";
							ctx2.fillStyle = "#3a6acd";
						}
					}
					ctx2.fillRect(readRelativeFroms[i][0], 0, Math.abs(readRelativeTos[i][0] - readRelativeFroms[i][0] + 1), 10);
					for( j = 1; j < readRelativeFroms[i].length; j++) {
						ctx2.fillRect(readRelativeTos[i][j - 1], 5, Math.abs(readRelativeFroms[i][j] - readRelativeTos[i][j - 1] + 1), 1);
						ctx2.fillRect(readRelativeFroms[i][j], 0, Math.abs(readRelativeTos[i][j] - readRelativeFroms[i][j] + 1), 10);
					}
					for( j = 0; j < variantNodes[i].length; j++) {
						if(variantFroms[i][j] >= startIndex && variantTos[i][j] <= endIndex) {
							if(variantTypes[i][j] == variantType_SNV) {
								ctx2.strokeStyle = "#ffff00";
								ctx2.fillStyle = "#ffff00";
							} else if(variantTypes[i][j] == variantType_INSERTION) {
								ctx2.strokeStyle = "#000";
								ctx2.fillStyle = "#000";
							} else {
								ctx2.fillStyle = "#FF0000";
								ctx2.strokeStyle = "#FF0000";
							}
							if(variantTypes[i][j] == variantType_INSERTION) {//triangle is used to express insertion
								ctx2.beginPath();
								ctx2.moveTo((variantRelativeFroms[i][j] + variantRelativeTos[i][j]) / 2, 0);
								ctx2.lineTo((variantFroms[i][j] + 1 + 0.5 * variantLetters[i][j].length - startIndex) / searchLength * trackLength, 10);
								ctx2.lineTo((variantFroms[i][j] + 1 - 0.5 * variantLetters[i][j].length - startIndex) / searchLength * trackLength, 10);
								ctx2.closePath();
								ctx2.fill();
							} else {
								ctx2.fillRect(variantRelativeFroms[i][j], 0, Math.abs(variantRelativeTos[i][j] - variantRelativeFroms[i][j] + 1), 10);
							}
						}
					}
				}
			} else {
				var packReads = [], squishReads = [];
				if((mode == "pack") && (readNodes.length <= parseInt(trackLength / 50 * 50))) {
					packReads[packReads.length] = [];
					packReads[0][0] = 0;
					if(isShowId == true) {
						for( i = 1; i < readNodes.length; i++) {
							for( j = 0; j < packReads.length; j++) {
								if((readRelativeFroms[i][0] - ctx2.measureText(readIds[i]).width - 8) > readRelativeTos[packReads[j][packReads[j].length - 1]][readRelativeTos[packReads[j][packReads[j].length - 1]].length - 1]) {
									packReads[j][packReads[j].length] = i;
									break;
								}
							}
							if(j == packReads.length) {
								packReads[packReads.length] = [];
								packReads[j][0] = i;
							}
						}
						if(packReads.length <= 50) {
							canvas1.height = 10 * packReads.length + 3*(packReads.length-1);
							canvas1.style.height = 10 * packReads.length + 3*(packReads.length-1);
							canvas2.height = 10 * packReads.length + 3*(packReads.length-1);
							canvas2.style.height = 10 * packReads.length + 3*(packReads.length-1);

							ctx1.fillStyle = "#000";
							ctx1.strokeStyle = "#000";
							ctx1.fillText(readNodeName, canvas1.width - ctx1.measureText(readNodeName).width, 8);
							
							y = 10;
							for( i = 0; i < packReads.length; i++) {
								for( j = 0; j < packReads[i].length; j++) {
									if(trackItemIndex< trackItems.length){
										trackItems[trackItemIndex].details[packReads[i][j]].left = readRelativeFroms[packReads[i][j]][0] - ctx2.measureText(readIds[packReads[i][j]]).width - 3;
										trackItems[trackItemIndex].details[packReads[i][j]].right = readRelativeTos[packReads[i][j]][readRelativeTos[packReads[i][j]].length - 1];
										trackItems[trackItemIndex].details[packReads[i][j]].top = y - 10;
										trackItems[trackItemIndex].details[packReads[i][j]].bottom = y;
									}
									
									ctx2.fillStyle = "#000";
									ctx2.strokeStyle = "#000";
									ctx2.fillText(readIds[packReads[i][j]], readRelativeFroms[packReads[i][j]][0] - ctx2.measureText(readIds[packReads[i][j]]).width - 3, y-2);
									
									if(readDireactions[packReads[i][j]] == "+") {
										if(parseInt(readMapqs[packReads[i][j]]) == 0) {
											ctx2.strokeStyle = "#6ff9f";
											ctx2.fillStyle = "#6fff9f";
										} else if(0 < parseInt(readMapqs[packReads[i][j]]) <= 10) {
											ctx2.strokeStyle = "#4eee94";
											ctx2.fillStyle = "#4eee94";
										} else if(10 < parseInt(readMapqs[packReads[i][j]]) <= 20) {
											ctx2.strokeStyle = "#43cd80";
											ctx2.fillStyle = "#43cd80";
										} else if(20 < parseInt(readMapqs[packReads[i][j]]) <= 30) {
											ctx2.strokeStyle = "#5fa080";
											ctx2.fillStyle = "#5fa080";
										} else {//  >30的情况，一般最大到40
											ctx2.strokeStyle = "#458b74";
											ctx2.fillStyle = "#458b74";
										}
									} else {
										if(parseInt(readMapqs[packReads[i][j]]) == 0) {
											ctx2.strokeStyle = "#8deeff";
											ctx2.fillStyle = "#8deeff";
										} else if(0 < parseInt(readMapqs[packReads[i][j]]) <= 10) {
											ctx2.strokeStyle = "#70caee";
											ctx2.fillStyle = "#70caee";
										} else if(10 < parseInt(readMapqs[packReads[i][j]]) <= 20) {
											ctx2.strokeStyle = "#48acff";
											ctx2.fillStyle = "#48acff";
										} else if(20 < parseInt(readMapqs[packReads[i][j]]) <= 30) {
											ctx2.strokeStyle = "#3a85ff";
											ctx2.fillStyle = "#3a85ff";
										} else {//  >30的情况，一般最大到40
											ctx2.strokeStyle = "#3a6acd";
											ctx2.fillStyle = "#3a6acd";
										}
									}
									ctx2.fillRect(readRelativeFroms[packReads[i][j]][0], y - 10, Math.abs(readRelativeTos[packReads[i][j]][0] - readRelativeFroms[packReads[i][j]][0] + 1), 10);
									for( k = 1; k < readRelativeFroms[packReads[i][j]].length; k++) {
										ctx2.fillRect(readRelativeTos[packReads[i][j]][k - 1], y - 5, Math.abs(readRelativeFroms[packReads[i][j]][k] - readRelativeTos[packReads[i][j]][k - 1] + 1), 1);
										ctx2.fillRect(readRelativeFroms[packReads[i][j]][k], y - 10, Math.abs(readRelativeTos[packReads[i][j]][k] - readRelativeFroms[packReads[i][j]][k] + 1), 10);
									}
									for( k = 0; k < variantNodes[packReads[i][j]].length; k++) {
										if(variantFroms[packReads[i][j]][k] >= startIndex && variantTos[packReads[i][j]][k] <= endIndex) {
											if(variantTypes[packReads[i][j]][k] == variantType_SNV) {
												ctx2.strokeStyle = "#ffff00";
												ctx2.fillStyle = "#ffff00";
											} else if(variantTypes[packReads[i][j]][k] == variantType_INSERTION) {
												ctx2.strokeStyle = "#000";
												ctx2.fillStyle = "#000";
											} else {
												ctx2.fillStyle = "#FF0000";
												ctx2.strokeStyle = "#FF0000";
											}

											if(variantTypes[packReads[i][j]][k] == variantType_INSERTION) {//triangle is used to express insertion
												ctx2.beginPath();
												ctx2.moveTo((variantRelativeFroms[packReads[i][j]][k] + variantRelativeTos[packReads[i][j]][k]) / 2, y - 10);
												ctx2.lineTo((variantFroms[packReads[i][j]][k] + 1 + 0.5 * variantLetters[packReads[i][j]][k].length - startIndex) / searchLength * trackLength, y);
												ctx2.lineTo((variantFroms[packReads[i][j]][k] + 1 - 0.5 * variantLetters[packReads[i][j]][k].length - startIndex) / searchLength * trackLength, y);
												ctx2.closePath();
												ctx2.fill();
											} else {
												ctx2.fillRect(variantRelativeFroms[packReads[i][j]][k], y - 10, Math.abs(variantRelativeTos[packReads[i][j]][k] - variantRelativeFroms[packReads[i][j]][k] + 1), 10);
											}

											if(searchLength <= trackLength / 10) {
												if(variantTypes[packReads[i][j]][k] == variantType_INSERTION) {
													ctx2.fillStyle = "#fff";
													ctx2.strokeStyle = "#fff";
												} else {
													if(readDireactions[packReads[i][j]] == "+") {
														ctx2.fillStyle = "#458b74";
														ctx2.strokeStyle = "#458b74";
													} else {
														ctx2.fillStyle = "#3a6acd";
														ctx2.strokeStyle = "#3a6acd";
													}
												}
												if(variantTypes[packReads[i][j]][k] != variantType_INSERTION) {
													ctx2.textAlign = "center";
													ctx2.fillText(variantLetters[packReads[i][j]][k], (variantRelativeTos[packReads[i][j]][k] + variantRelativeFroms[packReads[i][j]][k] + 1) / 2, y-2);
													ctx2.textAlign = "start";
												}
											}
										}
									}
								}
								y = y + 13;
							}
							canvas2.addEventListener("mousemove", canvasMousemove, false);
							canvas2.addEventListener("click", canvasClickForRead, false);
						}
					} else {
						for( i = 1; i < readNodes.length; i++) {
							for( j = 0; j < packReads.length; j++) {
								if((readRelativeFroms[i][0] - 5) > readRelativeTos[packReads[j][packReads[j].length - 1]][readRelativeTos[packReads[j][packReads[j].length - 1]].length - 1]) {
									packReads[j][packReads[j].length] = i;
									break;
								}
							}
							if(j == packReads.length) {
								packReads[packReads.length] = [];
								packReads[j][0] = i;
							}
						}
						if(packReads.length <= 50) {
							canvas1.height = 10 * packReads.length + 3*(packReads.length-1);
							canvas1.style.height = 10 * packReads.length + 3*(packReads.length-1);
							canvas2.height = 10 * packReads.length + 3*(packReads.length-1);
							canvas2.style.height = 10 * packReads.length + 3*(packReads.length-1);

							ctx1.fillStyle = "#000";
							ctx1.strokeStyle = "#000";
							ctx1.fillText(readNodeName, canvas1.width - ctx1.measureText(readNodeName).width, 8);
							
							y = 10;
							for( i = 0; i < packReads.length; i++) {
								for( j = 0; j < packReads[i].length; j++) {
									if(trackItemIndex< trackItems.length){
										trackItems[trackItemIndex].details[packReads[i][j]].top = y - 10;
										trackItems[trackItemIndex].details[packReads[i][j]].bottom = y;
									}
									
									if(readDireactions[packReads[i][j]] == "+") {
										if(parseInt(readMapqs[packReads[i][j]]) == 0) {
											ctx2.strokeStyle = "#6ff9f";
											ctx2.fillStyle = "#6fff9f";
										} else if(0 < parseInt(readMapqs[packReads[i][j]]) <= 10) {
											ctx2.strokeStyle = "#4eee94";
											ctx2.fillStyle = "#4eee94";
										} else if(10 < parseInt(readMapqs[packReads[i][j]]) <= 20) {
											ctx2.strokeStyle = "#43cd80";
											ctx2.fillStyle = "#43cd80";
										} else if(20 < parseInt(readMapqs[packReads[i][j]]) <= 30) {
											ctx2.strokeStyle = "#5fa080";
											ctx2.fillStyle = "#5fa080";
										} else {//  >30的情况，一般最大到40
											ctx2.strokeStyle = "#458b74";
											ctx2.fillStyle = "#458b74";
										}
									} else {
										if(parseInt(readMapqs[packReads[i][j]]) == 0) {
											ctx2.strokeStyle = "#8deeff";
											ctx2.fillStyle = "#8deeff";
										} else if(0 < parseInt(readMapqs[packReads[i][j]]) <= 10) {
											ctx2.strokeStyle = "#70caee";
											ctx2.fillStyle = "#70caee";
										} else if(10 < parseInt(readMapqs[packReads[i][j]]) <= 20) {
											ctx2.strokeStyle = "#48acff";
											ctx2.fillStyle = "#48acff";
										} else if(20 < parseInt(readMapqs[packReads[i][j]]) <= 30) {
											ctx2.strokeStyle = "#3a85ff";
											ctx2.fillStyle = "#3a85ff";
										} else {//  >30的情况，一般最大到40
											ctx2.strokeStyle = "#3a6acd";
											ctx2.fillStyle = "#3a6acd";
										}
									}
									ctx2.fillRect(readRelativeFroms[packReads[i][j]][0], y - 10, Math.abs(readRelativeTos[packReads[i][j]][0] - readRelativeFroms[packReads[i][j]][0] + 1), 10);
									for( k = 1; k < readRelativeFroms[packReads[i][j]].length; k++) {
										ctx2.fillRect(readRelativeTos[packReads[i][j]][k - 1], y - 5, Math.abs(readRelativeFroms[packReads[i][j]][k] - readRelativeTos[packReads[i][j]][k - 1] + 1), 1);
										ctx2.fillRect(readRelativeFroms[packReads[i][j]][k], y - 10, Math.abs(readRelativeTos[packReads[i][j]][k] - readRelativeFroms[packReads[i][j]][k] + 1), 10);
									}
									for( k = 0; k < variantNodes[packReads[i][j]].length; k++) {
										if(variantFroms[packReads[i][j]][k] >= startIndex && variantTos[packReads[i][j]][k] <= endIndex) {
											if(variantTypes[packReads[i][j]][k] == variantType_SNV) {
												ctx2.strokeStyle = "#ffff00";
												ctx2.fillStyle = "#ffff00";
											} else if(variantTypes[packReads[i][j]][k] == variantType_INSERTION) {
												ctx2.strokeStyle = "#000";
												ctx2.fillStyle = "#000";
											} else {
												ctx2.fillStyle = "#FF0000";
												ctx2.strokeStyle = "#FF0000";
											}
											if(variantTypes[packReads[i][j]][k] == variantType_INSERTION) {//triangle is used to express insertion
												ctx2.beginPath();
												ctx2.moveTo((variantRelativeFroms[packReads[i][j]][k] + variantRelativeTos[packReads[i][j]][k]) / 2, y - 10);
												ctx2.lineTo((variantFroms[packReads[i][j]][k] + 1 + 0.5 * variantLetters[packReads[i][j]][k].length - startIndex) / searchLength * trackLength, y);
												ctx2.lineTo((variantFroms[packReads[i][j]][k] + 1 - 0.5 * variantLetters[packReads[i][j]][k].length - startIndex) / searchLength * trackLength, y);
												ctx2.closePath();
												ctx2.fill();
											} else {
												ctx2.fillRect(variantRelativeFroms[packReads[i][j]][k], y - 10, Math.abs(variantRelativeTos[packReads[i][j]][k] - variantRelativeFroms[packReads[i][j]][k] + 1), 10);
											}
											//ctx2.fillRect(variantRelativeFroms[packReads[i][j]][k], y - 10, Math.abs(variantRelativeTos[packReads[i][j]][k] - variantRelativeFroms[packReads[i][j]][k] + 1), 10);

											if(searchLength <= trackLength / 10) {
												if(variantTypes[packReads[i][j]][k] == variantType_INSERTION) {
													ctx2.fillStyle = "#fff";
													ctx2.strokeStyle = "#fff";
												} else {
													if(readDireactions[packReads[i][j]] == "+") {
														ctx2.fillStyle = "#458b74";
														ctx2.strokeStyle = "#458b74";
													} else {
														ctx2.fillStyle = "#3a6acd";
														ctx2.strokeStyle = "#3a6acd";
													}
												}
												if(variantTypes[packReads[i][j]][k] != variantType_INSERTION) {
													ctx2.textAlign = "center";
													ctx2.fillText(variantLetters[packReads[i][j]][k], (variantRelativeTos[packReads[i][j]][k] + variantRelativeFroms[packReads[i][j]][k] + 1) / 2, y-2);
													ctx2.textAlign = "start";
												}
											}
										}
									}
								}
								y = y + 13;
							}
							canvas2.addEventListener("mousemove", canvasMousemove, false);
							canvas2.addEventListener("click", canvasClickForRead, false);
						}
					}
				}
				if(mode == "squish" || ((mode == "pack") && (packReads.length > 50 || readNodes.length > parseInt(trackLength / 50) * 50))) {
					squishReads[squishReads.length] = [];
					squishReads[0][0] = 0;
					for( i = 1; i < readNodes.length; i++) {
						for( j = 0; j < squishReads.length; j++) {
							if(readRelativeFroms[i][0] > readRelativeTos[squishReads[j][squishReads[j].length - 1]][readRelativeTos[squishReads[j][squishReads[j].length - 1]].length - 1]) {
								squishReads[j][squishReads[j].length] = i;
								break;
							}
						}
						if(j == squishReads.length) {
							squishReads[squishReads.length] = [];
							squishReads[j][0] = i;
						}
					}
					
					if(squishReads.length <= 300){
						canvas1.height = 5 * squishReads.length + 3*(squishReads.length - 1);
						canvas1.style.height = 5 * squishReads.length + 3*(squishReads.length - 1);
						canvas2.height = 5 * squishReads.length + 3*(squishReads.length - 1);
						canvas2.style.height = 5 * squishReads.length + 3*(squishReads.length - 1);
						
						ctx1.fillStyle = "#000";
						ctx1.strokeStyle = "#000";
						ctx1.fillText(readNodeName, canvas1.width - ctx1.measureText(readNodeName).width, 8);
	
						y = 0;
						for( i = 0; i < squishReads.length; i++) {
							for( j = 0; j < squishReads[i].length; j++) {
								if(readDireactions[squishReads[i][j]] == "+") {
									if(parseInt(readMapqs[squishReads[i][j]]) == 0) {
										ctx2.strokeStyle = "#6ff9f";
										ctx2.fillStyle = "#6fff9f";
									} else if(0 < parseInt(readMapqs[squishReads[i][j]]) <= 10) {
										ctx2.strokeStyle = "#4eee94";
										ctx2.fillStyle = "#4eee94";
									} else if(10 < parseInt(readMapqs[squishReads[i][j]]) <= 20) {
										ctx2.strokeStyle = "#43cd80";
										ctx2.fillStyle = "#43cd80";
									} else if(20 < parseInt(readMapqs[squishReads[i][j]]) <= 30) {
										ctx2.strokeStyle = "#5fa080";
										ctx2.fillStyle = "#5fa080";
									} else {//  >30的情况，一般最大到40
										ctx2.strokeStyle = "#458b74";
										ctx2.fillStyle = "#458b74";
									}
								} else {
									if(parseInt(readMapqs[squishReads[i][j]]) == 0) {
										ctx2.strokeStyle = "#8deeff";
										ctx2.fillStyle = "#8deeff";
									} else if(0 < parseInt(readMapqs[squishReads[i][j]]) <= 10) {
										ctx2.strokeStyle = "#70caee";
										ctx2.fillStyle = "#70caee";
									} else if(10 < parseInt(readMapqs[squishReads[i][j]]) <= 20) {
										ctx2.strokeStyle = "#48acff";
										ctx2.fillStyle = "#48acff";
									} else if(20 < parseInt(readMapqs[squishReads[i][j]]) <= 30) {
										ctx2.strokeStyle = "#3a85ff";
										ctx2.fillStyle = "#3a85ff";
									} else {//  >30的情况，一般最大到40
										ctx2.strokeStyle = "#3a6acd";
										ctx2.fillStyle = "#3a6acd";
									}
								}
								ctx2.fillRect(readRelativeFroms[squishReads[i][j]][0], y, Math.abs(readRelativeTos[squishReads[i][j]][0] - readRelativeFroms[squishReads[i][j]][0] + 1), 5);
								for( k = 1; k < readRelativeFroms[squishReads[i][j]].length; k++) {
									ctx2.fillRect(readRelativeTos[squishReads[i][j]][k - 1], y + 2, Math.abs(readRelativeFroms[squishReads[i][j]][k] - readRelativeTos[squishReads[i][j]][k - 1] + 1), 1);
									ctx2.fillRect(readRelativeFroms[squishReads[i][j]][k], y, Math.abs(readRelativeTos[squishReads[i][j]][k] - readRelativeFroms[squishReads[i][j]][k] + 1), 5);
								}
								for( k = 0; k < variantNodes[squishReads[i][j]].length; k++) {
									if(variantFroms[squishReads[i][j]][k] >= startIndex && variantTos[squishReads[i][j]][k] <= endIndex) {
										if(variantTypes[squishReads[i][j]][k] == variantType_SNV) {
											ctx2.strokeStyle = "#ffff00";
											ctx2.fillStyle = "#ffff00";
										} else if(variantTypes[squishReads[i][j]][k] == variantType_INSERTION) {
											ctx2.strokeStyle = "#000";
											ctx2.fillStyle = "#000";
										} else {
											ctx2.fillStyle = "#FF0000";
											ctx2.strokeStyle = "#FF0000";
										}
										if(variantTypes[squishReads[i][j]][k] == variantType_INSERTION) {//triangle is used to express insertion
											ctx2.beginPath();
											ctx2.moveTo((variantRelativeFroms[squishReads[i][j]][k] + variantRelativeTos[squishReads[i][j]][k]) / 2, y);
											ctx2.lineTo((variantFroms[squishReads[i][j]][k] + 1 + 0.5 * variantLetters[squishReads[i][j]][k].length - startIndex) / searchLength * trackLength, y + 5);
											ctx2.lineTo((variantFroms[squishReads[i][j]][k] + 1 - 0.5 * variantLetters[squishReads[i][j]][k].length - startIndex) / searchLength * trackLength, y + 5);
											ctx2.closePath();
											ctx2.fill();
										} else {
											ctx2.fillRect(variantRelativeFroms[squishReads[i][j]][k], y, Math.abs(variantRelativeTos[squishReads[i][j]][k] - variantRelativeFroms[squishReads[i][j]][k] + 1), 5);
										}
									}
								}
							}
							y = y + 8;
						}
					}else{
						canvas1.height = 10;
						canvas2.height = 10;
						canvas1.style.height = 10;
						canvas2.style.height = 10;
		
						ctx1.strokeStyle = "#000";
						ctx1.fillStyle = "#000";
						ctx1.fillText(readNodeName, canvas1.width - ctx1.measureText(readNodeName).width, 8);
		
						for( i = 0; i < readNodes.length; i++) {
							if(readDireactions[i] == "+") {
								if(parseInt(readMapqs[i]) == 0) {
									ctx2.strokeStyle = "#6ff9f";
									ctx2.fillStyle = "#6fff9f";
								} else if(0 < parseInt(readMapqs[i]) <= 10) {
									ctx2.strokeStyle = "#4eee94";
									ctx2.fillStyle = "#4eee94";
								} else if(10 < parseInt(readMapqs[i]) <= 20) {
									ctx2.strokeStyle = "#43cd80";
									ctx2.fillStyle = "#43cd80";
								} else if(20 < parseInt(readMapqs[i]) <= 30) {
									ctx2.strokeStyle = "#5fa080";
									ctx2.fillStyle = "#5fa080";
								} else {//  >30的情况，一般最大到40
									ctx2.strokeStyle = "#458b74";
									ctx2.fillStyle = "#458b74";
								}
							} else {
								if(parseInt(readMapqs[i]) == 0) {
									ctx2.strokeStyle = "#8deeff";
									ctx2.fillStyle = "#8deeff";
								} else if(0 < parseInt(readMapqs[i]) <= 10) {
									ctx2.strokeStyle = "#70caee";
									ctx2.fillStyle = "#70caee";
								} else if(10 < parseInt(readMapqs[i]) <= 20) {
									ctx2.strokeStyle = "#48acff";
									ctx2.fillStyle = "#48acff";
								} else if(20 < parseInt(readMapqs[i]) <= 30) {
									ctx2.strokeStyle = "#3a85ff";
									ctx2.fillStyle = "#3a85ff";
								} else {//  >30的情况，一般最大到40
									ctx2.strokeStyle = "#3a6acd";
									ctx2.fillStyle = "#3a6acd";
								}
							}
							ctx2.fillRect(readRelativeFroms[i][0], 0, Math.abs(readRelativeTos[i][0] - readRelativeFroms[i][0] + 1), 10);
							for( j = 1; j < readRelativeFroms[i].length; j++) {
								ctx2.fillRect(readRelativeTos[i][j - 1], 5, Math.abs(readRelativeFroms[i][j] - readRelativeTos[i][j - 1] + 1), 1);
								ctx2.fillRect(readRelativeFroms[i][j], 0, Math.abs(readRelativeTos[i][j] - readRelativeFroms[i][j] + 1), 10);
							}
							for( j = 0; j < variantNodes[i].length; j++) {
								if(variantFroms[i][j] >= startIndex && variantTos[i][j] <= endIndex) {
									if(variantTypes[i][j] == variantType_SNV) {
										ctx2.strokeStyle = "#ffff00";
										ctx2.fillStyle = "#ffff00";
									} else if(variantTypes[i][j] == variantType_INSERTION) {
										ctx2.strokeStyle = "#000";
										ctx2.fillStyle = "#000";
									} else {
										ctx2.fillStyle = "#FF0000";
										ctx2.strokeStyle = "#FF0000";
									}
									if(variantTypes[i][j] == variantType_INSERTION) {//triangle is used to express insertion
										ctx2.beginPath();
										ctx2.moveTo((variantRelativeFroms[i][j] + variantRelativeTos[i][j]) / 2, 0);
										ctx2.lineTo((variantFroms[i][j] + 1 + 0.5 * variantLetters[i][j].length - startIndex) / searchLength * trackLength, 10);
										ctx2.lineTo((variantFroms[i][j] + 1 - 0.5 * variantLetters[i][j].length - startIndex) / searchLength * trackLength, 10);
										ctx2.closePath();
										ctx2.fill();
									} else {
										ctx2.fillRect(variantRelativeFroms[i][j], 0, Math.abs(variantRelativeTos[i][j] - variantRelativeFroms[i][j] + 1), 10);
									}
								}
							}
						}
					}
				}
			}
		}
	}
}


function showPositiveValue(canvas1, canvas2, valueNode, mode, direaction, canvasHeight, logRatioValue) {//direaction :topdown downtop;ratioMode:logRatio normalRatio
	var valueId = valueNode.getAttribute(xmlAttributeId);
	var valueType = valueNode.getAttribute(xmlAttributeType);
	var valueFrom = valueNode.getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
	var valueTo = valueNode.getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
	var valueStep = parseInt(valueNode.getElementsByTagName(xmlTagStep)[0].childNodes[0].nodeValue);
	var valueList = getNodeText(valueNode.getElementsByTagName(xmlTagValueList)[0]).split(";");
	
	//var valueDescription = valueNode.getElementsByTagName(xmlTagDescription)[0].childNodes[0].nodeValue;
	var valueMax = parseFloat(valueList[0]), valueMin = parseFloat(valueList[0]);
	var valueMax_axisIndex = 0;
	var i = 0;

	if(valueList[0] == "") {// no values,the length of valueList is zero
		canvas1.height = 10;
		canvas2.height = 10;
		canvas1.style.height = 10;
		canvas2.style.height = 10;
		if(canvas1.getContext) {
			var ctx1 = canvas1.getContext('2d');
			ctx1.strokeStyle = "#000";
			ctx1.fillStyle = "#000";
			ctx1.fillText(valueId, canvas1.width - ctx1.measureText(valueId).width, 8);
		}
		return;
	}
	
	var startIndex_axis, endIndex_axis, trackLength_axis, searchLength_axis;
	var offsetWidth;
	if(startIndex<1){
		startIndex_axis = 1;
	}else{
		startIndex_axis = startIndex;
	}
	if(endIndex > chrLength){
		endIndex_axis = chrLength;
	}else{
		endIndex_axis = endIndex;
	}
	searchLength_axis = endIndex_axis - startIndex_axis + 1;
	trackLength_axis = Math.round((endIndex_axis - startIndex_axis + 1) / searchLength * trackLength);
	offsetWidth = (startIndex_axis - startIndex) / searchLength * trackLength;
	
	
	for( i = 0; i < valueList.length; i++) {
		if(parseFloat(valueList[i]) > valueMax) {
			valueMax = parseFloat(valueList[i]);
		}
		if(parseFloat(valueList[i]) < valueMin) {
			valueMin = parseFloat(valueList[i]);
		}
	}
	valueMax_axisIndex = valueMax;
	if(logRatioValue){
		if(valueMin >= 1){
			valueMax = Math.round(Math.log(valueMax) / Math.log(logRatioValue));
			for( i = 0; i < valueList.length; i++) {
				valueList[i] = Math.round(Math.log(valueList[i]) / Math.log(logRatioValue));
			}
		}
	}
	if(valueMax>0){
		for( i = 0; i < valueList.length; i++) {
			valueList[i] = Math.round(valueList[i] * canvasHeight / valueMax);
		}
	}
	if(canvas1.getContext && canvas2.getContext) {
		var ctx1 = canvas1.getContext('2d');
		var ctx2 = canvas2.getContext('2d');
		
		if(valueMax > 0) {
			canvas1.height = canvasHeight;
			canvas2.height = canvasHeight;
			canvas1.style.height = canvasHeight;
			canvas2.style.height = canvasHeight;
			
			ctx1.strokeStyle = "#000";
			ctx1.fillStyle = "#000";
			if(canvasHeight >= 30) {
				ctx1.fillRect(canvas1.width - 5, 0, 4, 1);
				ctx1.fillRect(canvas1.width - 2, 0, 1, canvasHeight);
				ctx1.fillRect(canvas1.width - 5, canvasHeight - 1, 4, 1);
				if(direaction == "downtop") {
					ctx1.fillText("0", canvas1.width - ctx1.measureText("0").width - 6, canvasHeight - 2);
					ctx1.fillText(valueMax_axisIndex, canvas1.width - ctx1.measureText(valueMax_axisIndex).width - 6, 8);
				} else {
					ctx1.textBaseline = "top";
					ctx1.fillText("0", canvas1.width - ctx1.measureText("0").width - 6, 0);
					ctx1.textBaseline = "alphabetic";
					ctx1.fillText(valueMax_axisIndex, canvas1.width - ctx1.measureText(valueMax_axisIndex).width - 6, canvasHeight - 2);
				}
				ctx1.fillText(valueId, canvas1.width - ctx1.measureText(valueId).width - ctx1.measureText(valueMax_axisIndex).width - 6, 8);
			} else {
				ctx1.fillText(valueId, canvas1.width - ctx1.measureText(valueId).width, 8);
			}

			ctx2.fillStyle = "#A2CD5A";
			ctx2.strokeStyle = "#A2CD5A";
			if(direaction == "downtop") {
				for( i = 0; i < valueList.length; i++) {
					ctx2.fillRect(1 + i * valueStep + offsetWidth, canvasHeight - parseInt(valueList[i]), valueStep, parseInt(valueList[i]));
				}
			} else {
				for( i = 0; i < valueList.length; i++) {
					ctx2.fillRect(1 + i * valueStep + offsetWidth, 0, valueStep, parseInt(valueList[i]));
				}
			}
		}else{
			canvas1.height = 10;
			canvas2.height = 10;
			canvas1.style.height = 10;
			canvas2.style.height = 10;
			ctx1.strokeStyle = "#000";
			ctx1.fillStyle = "#000";
			ctx1.fillText(valueId, canvas1.width - ctx1.measureText(valueId).width, 8);
		}
		
	}
}

function getNodeText(node) {
	var r = "";
	for(var x = 0; x < node.childNodes.length; x++) {
		r = r + node.childNodes[x].nodeValue;
	}
	return r;
}

function showPositiveValueCombine(canvas1, canvas2, valueNode, mode, direaction, canvasHeight, heatmapHeight, logRatioValue) {
	if(mode == "dense"){
		showValuesByHeatmap(canvas1, canvas2, valueNode, heatmapHeight, logRatioValue);
	}else{
		showPositiveValue(canvas1, canvas2, valueNode, mode, direaction, canvasHeight, logRatioValue);
	}
}

function showValue(canvas1, canvas2, valueNode, mode, direction, canvasHeight, logRatioValue) {
	var valueId = valueNode.getAttribute(xmlAttributeId);
	var valueType = valueNode.getAttribute(xmlAttributeType);
	var valueFrom = valueNode.getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
	var valueTo = valueNode.getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
	var valueStep = parseInt(valueNode.getElementsByTagName(xmlTagStep)[0].childNodes[0].nodeValue);
	var valueList = getNodeText(valueNode.getElementsByTagName(xmlTagValueList)[0]).split(";");
	//var valueList = (valueNode.getElementsByTagName("ValueList")[0].childNodes[0].nodeValue).split(";");
	//var valueDescription = valueNode.getElementsByTagName(xmlTagDescription)[0].childNodes[0].nodeValue;
	var valueMax = parseFloat(valueList[0]), valueMin = parseFloat(valueList[0]), valueMinAbs = Math.abs(parseFloat(valueList[0]));
	var valueMax_axisIndex, valueMin_axisIndex;
	var ratioValueMax = 0, valueStep = 2;
	var i = 0;

	var colorStyle = "RGB(8,117,184)";

	if(valueList[0] == "") {
		canvas1.height = 10;
		canvas2.height = 10;
		canvas1.style.height = 10;
		canvas2.style.height = 10;
		if(canvas1.getContext) {
			var ctx1 = canvas1.getContext('2d');
			ctx1.strokeStyle = "#000";
			ctx1.fillStyle = "#000";
			ctx1.fillText(valueId, canvas1.width - ctx1.measureText(valueId).width, 8);
		}
		return;
	}
	
	var startIndex_axis, endIndex_axis, trackLength_axis, searchLength_axis;
	var offsetWidth;
	if(startIndex<1){
		startIndex_axis = 1;
	}else{
		startIndex_axis = startIndex;
	}
	if(endIndex > chrLength){
		endIndex_axis = chrLength;
	}else{
		endIndex_axis = endIndex;
	}
	searchLength_axis = endIndex_axis - startIndex_axis + 1;
	trackLength_axis = Math.round((endIndex_axis - startIndex_axis + 1) / searchLength * trackLength);
	offsetWidth = (startIndex_axis - startIndex) / searchLength * trackLength;
	
	if(valueList.length < trackLength_axis / 2) {
		valueStep = trackLength_axis / valueList.length;
	}
	
	for( i = 0; i < valueList.length; i++) {
		if(parseFloat(valueList[i]) > valueMax) {
			valueMax = parseFloat(valueList[i]);
		}
		if(parseFloat(valueList[i]) < valueMin) {
			valueMin = parseFloat(valueList[i]);
		}
		if(Math.abs(valueList[i]) < valueMinAbs) {
			valueMinAbs = Math.abs(valueList[i]);
		}
	}
	valueMax_axisIndex = valueMax;
	valueMin_axisIndex = valueMin;
	if(logRatioValue){
		if(valueMinAbs >= 1){
			valueMax = Math.round(Math.log(valueMax) / Math.log(logRatioValue));
			valueMin = Math.round(Math.log(-valueMin) / Math.log(logRatioValue));
			for( i = 0; i < valueList.length; i++) {
				if(valueList[i] > 0)
					valueList[i] = Math.round(Math.log(valueList[i]) / Math.log(logRatioValue));
				else
					valueList[i] = -Math.round(Math.log(-valueList[i]) / Math.log(logRatioValue));
			}
		}
	}
	if(valueMin >= 0 && valueMax >= 0){
		if(valueMax>0){
			for( i = 0; i < valueList.length; i++) {
				valueList[i] = Math.round(valueList[i] * canvasHeight / valueMax);
			}
		}
		if(canvas1.getContext && canvas2.getContext) {
			var ctx1 = canvas1.getContext('2d');
			var ctx2 = canvas2.getContext('2d');

			if(valueMax > 0) {
				canvas1.height = canvasHeight;
				canvas2.height = canvasHeight;
				canvas1.style.height = canvasHeight;
				canvas2.style.height = canvasHeight;

				ctx1.strokeStyle = "#000";
				ctx1.fillStyle = "#000";		
				ctx1.fillText(valueId, canvas1.width - ctx1.measureText(valueId).width, 8);
				
				ctx2.fillStyle = colorStyle;
				ctx2.strokeStyle = colorStyle;
				
				if(direction == "topdown") {
					for( i = 0; i < valueList.length; i++) {
						ctx2.fillRect(Math.round(i * valueStep) + offsetWidth, 0, Math.round((i+1)*valueStep) - Math.round(i * valueStep), parseFloat(valueList[i]));
					}
				} else {
					for( i = 0; i < valueList.length; i++) {
						ctx2.fillRect(Math.round(i * valueStep) + offsetWidth, canvasHeight - parseFloat(valueList[i]), Math.round((i+1)*valueStep) - Math.round(i * valueStep), parseFloat(valueList[i]));
					}
				}
			} else {
				canvas1.height = 10;
				canvas2.height = 10;
				canvas1.style.height = 10;
				canvas2.style.height = 10;
				ctx1.strokeStyle = "#000";
				ctx1.fillStyle = "#000";
				ctx1.fillText(valueId, canvas1.width - ctx1.measureText(valueId).width, 8);
			}
		}

	}else if(valueMin < 0 && valueMax > 0){
		var valueScale = valueMax - valueMin;
		valueMax = Math.round(valueMax * canvasHeight / valueScale);
		valueMin = Math.round(valueMin * canvasHeight / valueScale);
		for( i = 0; i < valueList.length; i++) {
			valueList[i] = Math.round(valueList[i] * canvasHeight / valueScale);
		}
		
		if(canvas1.getContext && canvas2.getContext) {
			var ctx1 = canvas1.getContext('2d');
			var ctx2 = canvas2.getContext('2d');

			canvas1.height = canvasHeight;
			canvas2.height = canvasHeight;
			canvas1.style.height = canvasHeight;
			canvas2.style.height = canvasHeight;
			
			ctx1.strokeStyle = "#000";
			ctx1.fillStyle = "#000";		
			ctx1.fillText(valueId, canvas1.width - ctx1.measureText(valueId).width, 8);
			
			ctx2.fillStyle = "#8B5A2B";
			ctx2.fillRect(0, valueMax + 1, trackLength, 1);

			ctx2.fillStyle = colorStyle;
			ctx2.strokeStyle = colorStyle;
			
			for( i = 0; i < valueList.length; i++) {
				if(valueList[i] > 0) {
					ctx2.fillRect(Math.round(i * valueStep) + offsetWidth, valueMax + 1 - parseFloat(valueList[i]), Math.round((i+1)*valueStep) - Math.round(i * valueStep), parseFloat(valueList[i]));
				} else {
					ctx2.fillRect(Math.round(i * valueStep) + offsetWidth, canvasHeight + valueMin + 1, Math.round((i+1)*valueStep) - Math.round(i * valueStep), Math.abs(parseFloat(valueList[i])));
				}
			}
		}
	}else{
		for( i = 0; i < valueList.length; i++) {
			valueList[i] = Math.abs(Math.round(valueList[i] * canvasHeight / valueMin));
		}
		if(canvas1.getContext && canvas2.getContext) {
			var ctx1 = canvas1.getContext('2d');
			var ctx2 = canvas2.getContext('2d');
			canvas1.height = canvasHeight;
			canvas2.height = canvasHeight;
			canvas1.style.height = canvasHeight;
			canvas2.style.height = canvasHeight;
			
			ctx1.strokeStyle = "#000";
			ctx1.fillStyle = "#000";		
			ctx1.fillText(valueId, canvas1.width - ctx1.measureText(valueId).width, 8);
				
			ctx2.fillStyle = colorStyle;
			ctx2.strokeStyle = colorStyle;
			if(direction == "topdown") {
				for( i = 0; i < valueList.length; i++) {
					ctx2.fillRect(Math.round(i * valueStep) + offsetWidth, 0, Math.round((i+1)*valueStep) - Math.round(i * valueStep), parseFloat(valueList[i]));
				}
			} else {
				for( i = 0; i < valueList.length; i++) {
					ctx2.fillRect(Math.round(i * valueStep) + offsetWidth, canvasHeight - parseFloat(valueList[i]), Math.round((i+1)*valueStep) - Math.round(i * valueStep), parseFloat(valueList[i]));
				}
			}
		}
	}
}

function showValuesByHeatmap(canvas1, canvas2, valueNode, heatmapHeight, logRatioValue) {
	var valueId = valueNode.getAttribute(xmlAttributeId);
	var valueType = valueNode.getAttribute(xmlAttributeType);
	var valueFrom = valueNode.getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
	var valueTo = valueNode.getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
	var valueStep = parseInt(valueNode.getElementsByTagName(xmlTagStep)[0].childNodes[0].nodeValue);
	var valueList = getNodeText(valueNode.getElementsByTagName(xmlTagValueList)[0]).split(";");

	var Lvalue_of_HSL_inheatmap = 50;
	var valueMax = parseFloat(valueList[0]), valueMin = parseFloat(valueList[0]);
	var valueMinAbs = Math.abs(parseFloat(valueList[0])), valueMaxAbs;
	var Lvalue_per_maxValue = 0;
	var valueStep = 2;
	var i = 0;

	var H_P = 0, H_N = 120;
	var hh, ss, ll;
	
	var startIndex_axis, endIndex_axis, trackLength_axis, searchLength_axis;
	var offsetWidth;
	if(startIndex<1){
		startIndex_axis = 1;
	}else{
		startIndex_axis = startIndex;
	}
	if(endIndex > chrLength){
		endIndex_axis = chrLength;
	}else{
		endIndex_axis = endIndex;
	}
	searchLength_axis = endIndex_axis - startIndex_axis + 1;
	trackLength_axis = Math.round((endIndex_axis - startIndex_axis + 1) / searchLength * trackLength);
	offsetWidth = (startIndex_axis - startIndex) / searchLength * trackLength;

	if(valueList.length < trackLength_axis / 2) {
		valueStep = trackLength_axis / valueList.length;
	}

	for( i = 0; i < valueList.length; i++) {
		if(parseFloat(valueList[i]) > valueMax) {
			valueMax = parseFloat(valueList[i]);
		}
		if(parseFloat(valueList[i]) < valueMin) {
			valueMin = parseFloat(valueList[i]);
		}
		if(Math.abs(valueList[i]) < valueMinAbs) {
			valueMinAbs = Math.abs(valueList[i]);
		}
	}
	valueMaxAbs = Math.abs(valueMax) > Math.abs(valueMin) ? Math.abs(valueMax) : Math.abs(valueMin);
	Lvalue_per_maxValue = Lvalue_of_HSL_inheatmap / valueMaxAbs;
	
	if(logRatioValue && (valueMinAbs > 1)){
		Lvalue_per_maxValue = Lvalue_of_HSL_inheatmap / (Math.round(Math.log(valueMaxAbs) / Math.log(logRatioValue)));
		for( i = 0; i < valueList.length; i++) {
			if(valueList[i] > 0)
				valueList[i] = Math.round(Math.log(valueList[i]) / Math.log(logRatioValue));
			else
				valueList[i] = -Math.round(Math.log(-valueList[i]) / Math.log(logRatioValue));
		}
	}
	if(canvas1.getContext && canvas2.getContext) {
		var ctx1 = canvas1.getContext('2d');
		var ctx2 = canvas2.getContext('2d');
		canvas1.height = heatmapHeight;
		canvas2.height = heatmapHeight;
		canvas1.style.height = heatmapHeight;
		canvas2.style.height = heatmapHeight;
		ctx1.fillText(valueId, canvas1.width - ctx1.measureText(valueId).width, 8);

		for( i = 0; i < valueList.length; i++) {
			if(valueList[i] >= 0) {
				hh = H_P;
			} else {
				hh = H_N;
			}
			ss = Math.abs(valueList[i]) * Lvalue_per_maxValue * 2;
			ll = 100 - Math.abs(valueList[i]) * Lvalue_per_maxValue;
			ctx2.fillStyle = HSL2RGB(hh, ss, ll);
			ctx2.fillRect(Math.round(i * valueStep) + offsetWidth, 0, Math.round((i + 1) * valueStep) - Math.round(i * valueStep), heatmapHeight);
		}
	}
}

function HSL2RGB(h, s, l) {
	var r, g, b;
	var q, p;
	var Tc = [0, 0, 0];
	var Color = [0, 0, 0];
	var i = 0;
	h = h / 360;
	s = s / 100;
	l = l / 100;
	//规范到[0,1]
	if(s == 0) {
		r = 1;
		g = 1;
		b = 1;
	} else {
		if(l < 0.5) {
			q = l * (1 + s);
		} else {
			q = l + s - (l * s);
		}
		p = 2 * l - q;

		Tc[0] = h + 1 / 3;
		Tc[1] = h;
		Tc[2] = h - 1 / 3;

		for( i = 0; i < Tc.length; i++) {
			if(Tc[i] < 0) {
				Tc[i] = Tc[i] + 1.0;
			}
			if(Tc[i] > 1) {
				Tc[i] = Tc[i] - 1.0;
			}
		}
		for( i = 0; i < Color.length; i++) {
			if(Tc[i] < 1 / 6) {
				Color[i] = p + ((q - p) * 6 * Tc[i]);
			} else if(Tc[i] >= 1 / 6 && Tc[i] < 1 / 2) {
				Color[i] = q;
			} else if(Tc[i] >= 1 / 2 && Tc[i] < 2 / 3) {
				Color[i] = p + ((q - p) * 6 * (2 / 3 - Tc[i]));
			} else {
				Color[i] = p;
			}
		}
		r = Color[0];
		g = Color[1];
		b = Color[2];
	}
	r = Math.round(r * 255);
	g = Math.round(g * 255);
	b = Math.round(b * 255);
	return "rgb(" + r + "," + g + "," + b + ")";
}

function showValueCombine(canvas1, canvas2, valueNode, mode, direction, canvasHeight, heatmapHeight,logRatioValue){
	if(mode == "dense"){
		showValuesByHeatmap(canvas1, canvas2, valueNode, heatmapHeight, logRatioValue);
	}else{
		showValue(canvas1, canvas2, valueNode, mode, direction, canvasHeight, logRatioValue);
	}
}

function showVariantByImg(canvas1, canvas2, variantNode, mode) {
	var fromNodes = variantNode.getElementsByTagName(xmlTagFrom);
	var toNodes = variantNode.getElementsByTagName(xmlTagTo);
	var variantNodes = variantNode.getElementsByTagName(xmlTagVariant);
	var variantIds = [];
	var variantTypes = [];
	var variantName = variantNode.getAttribute(xmlAttributeId);
	var relativeFroms = [], relativeTos = [], imgFroms = [];
	var relativeWidth;
	var i, j, y;
	var trackItemIndex;
	var variantName_show = variantName;

	if(!((/^_/).test(variantName))){
		var ifParam = variantNode.getAttribute("ifParam");
		var variantsSuperId = variantNode.getAttribute("superid") || variantName;
		insertSettingBtn(variantName, ifParam, variantsSuperId);
	}
	
	for( i = 0; i < trackItems.length; i++) {
		if(variantName == trackItems[i].id) {
			trackItemIndex = i;
			break;
		}
	}
	
	if(trackItemIndex< trackItems.length){
		trackItems[trackItemIndex].details = [];
	}else{
		if(variantName == personalPannel.Pvar.id){
			personalPannel.Pvar.details = [];
		}
	}

	var imgWidth = 21, imgHeight = 30;
	var imgDEL, imgINS, imgSNV, imgCNV, imgDUP, imgINV, imgBLS, imgOTH, imgSNV_A, imgSNV_C, imgSNV_G, imgSNV_T;
	imgINS = document.getElementById("imgINS");
	imgDEL = document.getElementById("imgDEL");
	imgCNV = document.getElementById("imgCNV");
	imgDUP = document.getElementById("imgDUP");
	imgINV = document.getElementById("imgINV");
	imgBLS = document.getElementById("imgBLS");
	imgOTH = document.getElementById("imgOTH");
	imgSNV_A = document.getElementById("imgSNV_A");
	imgSNV_C = document.getElementById("imgSNV_C");
	imgSNV_G = document.getElementById("imgSNV_G");
	imgSNV_T = document.getElementById("imgSNV_T");
/////color code variant by effect --Liran
	var imgDELe, imgINSe, imgSNVe, imgCNVe, imgDUPe, imgINVe, imgBLSe, imgOTHe, imgSNV_Ae, imgSNV_Ce, imgSNV_Ge, imgSNV_Te;
	imgINSe = document.getElementById("imgINSe");
	imgDELe = document.getElementById("imgDELe");
	imgCNVe = document.getElementById("imgCNVe");
	imgDUPe = document.getElementById("imgDUPe");
	imgINVe = document.getElementById("imgINVe");
	imgBLSe = document.getElementById("imgBLSe");
	imgOTHe = document.getElementById("imgOTHe");
	imgSNV_Ae = document.getElementById("imgSNV_Ae");
	imgSNV_Ce = document.getElementById("imgSNV_Ce");
	imgSNV_Ge = document.getElementById("imgSNV_Ge");
	imgSNV_Te = document.getElementById("imgSNV_Te");
/////color code variant by effect --Liran

	for( i = 0; i < variantNodes.length; i++) {
		variantIds[i] = variantNodes[i].getAttribute(xmlAttributeId);
		variantTypes[i] = variantNodes[i].getAttribute(xmlAttributeType);
		relativeFroms[i] = parseInt((parseInt(fromNodes[i].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
		relativeTos[i] = parseInt((parseInt(toNodes[i].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
		imgFroms[i] = (relativeFroms[i] + relativeTos[i] - imgWidth) / 2;
		
		/*if(trackItemIndex< trackItems.length){
			trackItems[trackItemIndex].details[i] = [];
			trackItems[trackItemIndex].details[i].id = variantIds[i];
			trackItems[trackItemIndex].details[i].from = parseInt(fromNodes[i].childNodes[0].nodeValue);
			trackItems[trackItemIndex].details[i].to = parseInt(toNodes[i].childNodes[0].nodeValue);
			if(mode == "pack"){
				trackItems[trackItemIndex].details[i].left = imgFroms[i];
				trackItems[trackItemIndex].details[i].right = imgFroms[i] + imgWidth;
			}
		}*/
		
		if(mode != "dense" && mode != "squish") {
			if(relativeTos[i] - relativeFroms[i] + 1 < imgWidth) {
				relativeFroms[i] = imgFroms[i];
				relativeTos[i] = imgFroms[i] + imgWidth;
			}
		}
	}
	if(((/^_/).test(variantName))){
		variantName_show = (variantName + "").replace("_","");
	}
	if(canvas1.getContext && canvas2.getContext) {
		var ctx1 = canvas1.getContext('2d');
		var ctx2 = canvas2.getContext('2d');
		if(mode == "dense") {
			canvas1.height = 10;
			canvas1.style.height = 10;
			canvas2.height = 10;
			canvas2.style.height = 10;
			ctx1.fillStyle = "#000";
			ctx1.fillText(variantName_show, canvas1.width - ctx1.measureText(variantName_show).width, 8);

			//y = 13;
			for( i = 0; i < fromNodes.length; i++) {
				relativeWidth = Math.abs(relativeTos[i] - relativeFroms[i] + 1);
				switch(variantTypes[i]) {
					case variantType_DELETION:
						ctx2.fillStyle = "rgb(0,255,0)";
						break;
					case variantType_INSERTION:
						ctx2.fillStyle = "rgb(255,0,0)";
						break;
					case variantType_SNV:
						ctx2.fillStyle = "rgb(0,0,0)";
						break;
					case variantType_CNV:
						ctx2.fillStyle = "rgb(20,177,75)";
						break;
					case variantType_DUPLICATION:
						ctx2.fillStyle = "rgb(19,165,157)";
						break;
					case variantType_INVERSION:
						ctx2.fillStyle = "rgb(19,127,165)";
						break;
					case variantType_BLS:
						ctx2.fillStyle = "rgb(85,19,165)";
						break;
					default:
						ctx2.fillStyle = "#0000FF";
				}
				ctx2.fillRect(relativeFroms[i], 0, relativeWidth, 10);
				/*switch(variantTypes[i]) {
					case variantType_DELETION:
						ctx2.drawImage(imgDEL, imgFroms[i], y);
						break;
					case variantType_INSERTION:
						ctx2.drawImage(imgINS, imgFroms[i], y);
						break;
					case variantType_SNV:						
						switch(variantNodes[i].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue) {
							case "A":
								imgSNV = imgSNV_A;
								break;
							case "C":
								imgSNV = imgSNV_C;
								break;
							case "G":
								imgSNV = imgSNV_G
								break;
							default:
								imgSNV = imgSNV_T;
						}
						//ctx2.drawImage(imgSNV, imgFroms[i], y);
						break;
					case variantType_CNV:
						ctx2.drawImage(imgCNV, imgFroms[i], y);
						break;
					case variantType_DUPLICATION:
						ctx2.drawImage(imgDUP, imgFroms[i], y);
						break;
					case variantType_INVERSION:
						ctx2.drawImage(imgINV, imgFroms[i], y);
						break;
					case variantType_BLS:
						ctx2.drawImage(imgBLS, imgFroms[i], y);
						break;
					default:
						ctx2.drawImage(imgOTH, imgFroms[i], y);
				}*/
			}
		}else {
			var packVariants = [], squishVariants = [];
			if((mode == "pack") && (variantNodes.length <= parseInt(trackLength / 60) * 10)) {
				if(variantNodes.length > 0) {//the situation: not contain the variant
					packVariants[packVariants.length] = [];
					packVariants[0][0] = 0;
				}
				for( i = 1; i < variantNodes.length; i++) {
					for( j = 0; j < packVariants.length; j++) {
						if((relativeFroms[i] - ctx1.measureText(variantIds[i]).width - 5) > relativeTos[packVariants[j][packVariants[j].length - 1]]) {
							packVariants[j][packVariants[j].length] = i;
							break;
						}
					}
					if(j == packVariants.length) {
						packVariants[packVariants.length] = [];
						packVariants[j][0] = i;
					}
				}
				if(packVariants.length <= 10) {
					canvas1.height = imgHeight * packVariants.length + 3*(packVariants.length - 1) > 10 ? imgHeight * packVariants.length + 3*(packVariants.length - 1) : 10;
					canvas1.style.height = imgHeight * packVariants.length + 3*(packVariants.length - 1) > 10 ? imgHeight * packVariants.length + 3*(packVariants.length - 1) : 10;
					canvas2.height = imgHeight * packVariants.length + 3*(packVariants.length - 1) > 10 ? imgHeight * packVariants.length + 3*(packVariants.length - 1) : 10;
					canvas2.style.height = imgHeight * packVariants.length + 3*(packVariants.length - 1) > 10 ? imgHeight * packVariants.length + 3*(packVariants.length - 1) : 10;
					
					ctx1.fillStyle = "#000";
					ctx1.fillText(variantName_show, canvas1.width - ctx1.measureText(variantName_show).width, 8);

					y = 0;
					for( i = 0; i < packVariants.length; i++) {
						for( j = 0; j < packVariants[i].length; j++) {
							if(trackItemIndex< trackItems.length){
								trackItems[trackItemIndex].details[packVariants[i][j]] = [];
								trackItems[trackItemIndex].details[packVariants[i][j]].id = variantIds[packVariants[i][j]];
								trackItems[trackItemIndex].details[packVariants[i][j]].from = parseInt(fromNodes[packVariants[i][j]].childNodes[0].nodeValue);
								trackItems[trackItemIndex].details[packVariants[i][j]].to = parseInt(toNodes[packVariants[i][j]].childNodes[0].nodeValue);
								
								trackItems[trackItemIndex].details[packVariants[i][j]].left = imgFroms[packVariants[i][j]];
								trackItems[trackItemIndex].details[packVariants[i][j]].right = imgFroms[packVariants[i][j]] + imgWidth;
								
								trackItems[trackItemIndex].details[packVariants[i][j]].top = y;
								trackItems[trackItemIndex].details[packVariants[i][j]].bottom = y + imgHeight;
							}else{
								if(variantName == personalPannel.Pvar.id){
									personalPannel.Pvar.details[packVariants[i][j]] = [];
									personalPannel.Pvar.details[packVariants[i][j]].id = variantIds[packVariants[i][j]];
									personalPannel.Pvar.details[packVariants[i][j]].from = parseInt(fromNodes[packVariants[i][j]].childNodes[0].nodeValue);
									personalPannel.Pvar.details[packVariants[i][j]].to = parseInt(toNodes[packVariants[i][j]].childNodes[0].nodeValue);
									
									personalPannel.Pvar.details[packVariants[i][j]].left = imgFroms[packVariants[i][j]];
									personalPannel.Pvar.details[packVariants[i][j]].right = imgFroms[packVariants[i][j]] + imgWidth;
									
									personalPannel.Pvar.details[packVariants[i][j]].top = y;
									personalPannel.Pvar.details[packVariants[i][j]].bottom = y + imgHeight;
								}
							}
							var variantEffect = variantNodes[packVariants[i][j]].getAttribute(xmlAttributeEffect);
							if (!variantEffect){
								variantEffect = 0;
							}
							if(variantTypes[packVariants[i][j]] == variantType_SNV) {
								switch(variantNodes[packVariants[i][j]].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue) {
									case "A":
										imgSNV = variantEffect>2 ? imgSNV_Ae : imgSNV_A;
										break;
									case "C":
										imgSNV = variantEffect>2 ? imgSNV_Ce : imgSNV_C;
										break;
									case "G":
										imgSNV = variantEffect>2 ? imgSNV_Ge : imgSNV_G;
										break;
									default:
										imgSNV = variantEffect>2 ? imgSNV_Te : imgSNV_T;
								}
								ctx2.drawImage(imgSNV, imgFroms[packVariants[i][j]], y);
							} else if(variantTypes[packVariants[i][j]] == variantType_INSERTION) {
								ctx2.drawImage(variantEffect>2 ? imgINSe : imgINS, imgFroms[packVariants[i][j]], y);
							} else if(variantTypes[packVariants[i][j]] == variantType_DELETION) {
								ctx2.drawImage(variantEffect>2 ? imgDELe : imgDEL, imgFroms[packVariants[i][j]], y);
							} else if(variantTypes[packVariants[i][j]] == variantType_CNV) {
								ctx2.drawImage(variantEffect>2 ? imgCNVe : imgCNV, imgFroms[packVariants[i][j]], y);
							} else if(variantTypes[packVariants[i][j]] == variantType_DUPLICATION) {
								ctx2.drawImage(variantEffect>2 ? imgDUPe : imgDUP, imgFroms[packVariants[i][j]], y);
							} else if(variantTypes[packVariants[i][j]] == variantType_INVERSION) {
								ctx2.drawImage(variantEffect>2 ? imgINVe : imgINV, imgFroms[packVariants[i][j]], y);
							} else if(variantTypes[packVariants[i][j]] == variantType_BLS) {
								ctx2.drawImage(variantEffect>2 ? imgBLSe : imgBLS, imgFroms[packVariants[i][j]], y);
							} else {
								ctx2.drawImage(variantEffect>2 ? imgOTHe : imgOTH, imgFroms[packVariants[i][j]], y);
							}
							
							ctx2.fillText(variantIds[packVariants[i][j]], imgFroms[packVariants[i][j]] - ctx2.measureText(variantIds[packVariants[i][j]]).width, y + imgHeight - 2);
							/////added by Liran for effect block
							var vee = 0;
							if (scoremethPvar != "PGB") {
								vee = (variantEffect - 2) / 10;
							} else {
								vee = variantEffect - 2;
							}

							ctx2.fillStyle="rgb(133,122,185)";
							var minuss = 0;
							while(minuss < vee && minuss < 10){
								ctx2.fillRect(imgFroms[packVariants[i][j]]+imgWidth-3, y+imgHeight-minuss-1, 4, 1);
								minuss = minuss + 2;
							}
							ctx2.fillStyle="rgb(0,0,0)";
							/////added by Liran for effect block
						}
						y = y + imgHeight + 3;
					}
					canvas2.addEventListener("mousemove", canvasMousemoveOnPP, false);
					canvas2.addEventListener("click", canvasClickForVariantOnPP, false);
				}
			}
			if(mode == "squish" || ( mode == "pack" && (packVariants.length > 10 || variantNodes.length > parseInt(trackLength / 60) * 10))) {
				if(variantNodes.length > 0) {
					squishVariants[squishVariants.length] = [];
					squishVariants[0][0] = 0;
				}
				for( i = 0; i < variantNodes.length; i++) {
					relativeFroms[i] = parseInt((parseInt(fromNodes[i].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
					relativeTos[i] = parseInt((parseInt(toNodes[i].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
				}
				for( i = 1; i < variantNodes.length; i++) {
					for( j = 0; j < squishVariants.length; j++) {
						if(relativeFroms[i] > relativeTos[squishVariants[j][squishVariants[j].length - 1]]) {
							squishVariants[j][squishVariants[j].length] = i;
							break;
						}
					}
					if(j == squishVariants.length) {
						squishVariants[squishVariants.length] = [];
						squishVariants[j][0] = i;
					}
				}
				canvas1.height = 5 * squishVariants.length + 3*(squishVariants.length - 1);
				canvas1.style.height = 5 * squishVariants.length + 3*(squishVariants.length - 1);
				canvas2.height = 5 * squishVariants.length + 3*(squishVariants.length - 1);
				canvas2.style.height = 5 * squishVariants.length + 3*(squishVariants.length - 1);
				
				ctx1.fillStyle = "#000";
				ctx1.fillText(variantName_show, canvas1.width - ctx1.measureText(variantName_show).width, 8);

				y = 0;
				for( i = 0; i < squishVariants.length; i++) {
					for( j = 0; j < squishVariants[i].length; j++) {
						relativeWidth = Math.abs(relativeTos[squishVariants[i][j]] - relativeFroms[squishVariants[i][j]] + 1);
						switch(variantTypes[squishVariants[i][j]]) {
							case variantType_DELETION:
								ctx2.fillStyle = "rgb(0,255,0)";
								break;
							case variantType_INSERTION:
								ctx2.fillStyle = "rgb(255,0,0)";
								break;
							case variantType_SNV:
								ctx2.fillStyle = "rgb(0,0,0)";
								break;
							case variantType_CNV:
								ctx2.fillStyle = "rgb(20,177,75)";
								break;
							case variantType_DUPLICATION:
								ctx2.fillStyle = "rgb(19,165,157)";
								break;
							case variantType_INVERSION:
								ctx2.fillStyle = "rgb(19,127,165)";
								break;
							case variantType_BLS:
								ctx2.fillStyle = "rgb(85,19,165)";
								break;
							default:
								ctx2.fillStyle = "#0000FF";
						}
						ctx2.fillRect(relativeFroms[squishVariants[i][j]], y, relativeWidth, 5);
					}
					y = y + 8;
				}
			}
		}
	}
}

function showVariant(canvas1, canvas2, variantNode, mode) {
	var fromNodes = variantNode.getElementsByTagName(xmlTagFrom);
	var toNodes = variantNode.getElementsByTagName(xmlTagTo);
	var variantNodes = variantNode.getElementsByTagName(xmlTagVariant);
	var variantIds = [];
	var variantTypes = [];
	var variantName = variantNode.getAttribute(xmlAttributeId);
	var relativeFroms = [], relativeTos = [];
	var relativeWidth;
	var i, j, y;
	var trackItemIndex;
	
	for( i = 0; i < trackItems.length; i++) {
		if(variantName == trackItems[i].id) {
			trackItemIndex = i;
			break;
		}
	}
	
	if(trackItemIndex< trackItems.length){
		trackItems[trackItemIndex].details = [];
	}

	var ifParam = variantNode.getAttribute("ifParam");
	var variantsSuperId = variantNode.getAttribute("superid") || variantName;
	insertSettingBtn(variantName, ifParam, variantsSuperId);

	for( i = 0; i < variantNodes.length; i++) {
		variantIds[i] = variantNodes[i].getAttribute(xmlAttributeId);
		variantTypes[i] = variantNodes[i].getAttribute(xmlAttributeType);
		relativeFroms[i] = parseInt((parseInt(fromNodes[i].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
		relativeTos[i] = parseInt((parseInt(toNodes[i].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
		if(trackItemIndex< trackItems.length){
			trackItems[trackItemIndex].details[i] = [];
			trackItems[trackItemIndex].details[i].id = variantIds[i];
			trackItems[trackItemIndex].details[i].from = parseInt(fromNodes[i].childNodes[0].nodeValue);
			trackItems[trackItemIndex].details[i].to = parseInt(toNodes[i].childNodes[0].nodeValue);
		}
	}
	
	if(canvas1.getContext && canvas2.getContext) {
		var ctx1 = canvas1.getContext('2d');
		var ctx2 = canvas2.getContext('2d');
		if(mode == "dense") {
			canvas1.height = 10;
			canvas1.style.height = 10;
			canvas2.height = 10;
			canvas2.style.height = 10;
			ctx1.fillStyle = "#000";
			ctx1.fillText(variantName, canvas1.width - ctx1.measureText(variantName).width, 8);

			for( i = 0; i < fromNodes.length; i++) {
				relativeWidth = Math.abs(relativeTos[i] - relativeFroms[i] + 1);
				if(variantTypes[i] == variantType_SNV || variantTypes[i] == variantType_OTHERS) {
					ctx2.fillStyle = "#000";
				} else if(variantTypes[i] == variantType_INSERTION) {
					ctx2.fillStyle = "#FF0000";
				} else if(variantTypes[i] == variantType_DELETION) {
					ctx2.fillStyle = "#00FF00";
				} else {
					ctx2.fillStyle = "#0000FF";
				}
				ctx2.fillRect(relativeFroms[i], 0, relativeWidth, 10);
			}
		} else {
			var packVariants = [], squishVariants = [];
			if((mode == "pack") && (variantNodes.length <= parseInt(trackLength / 60) * 50)) {
				if(variantNodes.length > 0) {//the situation: not contain the variant
					packVariants[packVariants.length] = [];
					packVariants[0][0] = 0;
				}
				for( i = 1; i < variantNodes.length; i++) {
					for( j = 0; j < packVariants.length; j++) {
						if((relativeFroms[i] - ctx1.measureText(variantIds[i]).width - 8) > relativeTos[packVariants[j][packVariants[j].length - 1]]) {
							packVariants[j][packVariants[j].length] = i;
							break;
						}
					}
					if(j == packVariants.length) {
						packVariants[packVariants.length] = [];
						packVariants[j][0] = i;
					}
				}
				if(packVariants.length <= 50) {
					canvas1.height = 10 * packVariants.length + 3*(packVariants.length - 1) > 10 ? 10 * packVariants.length + 3*(packVariants.length - 1) : 10;
					canvas1.style.height = 10 * packVariants.length + 3*(packVariants.length - 1) > 10 ? 10 * packVariants.length + 3*(packVariants.length - 1) : 10;
					canvas2.height = 10 * packVariants.length + 3*(packVariants.length - 1) > 10 ? 10 * packVariants.length + 3*(packVariants.length - 1) : 10;
					canvas2.style.height = 10 * packVariants.length + 3*(packVariants.length - 1) > 10 ? 10 * packVariants.length + 3*(packVariants.length - 1) : 10;
					
					ctx1.fillStyle = "#000";
					ctx1.fillText(variantName, canvas1.width - ctx1.measureText(variantName).width, 8);

					y = 10;
					for( i = 0; i < packVariants.length; i++) {
						for( j = 0; j < packVariants[i].length; j++) {
							if(trackItemIndex< trackItems.length){
								trackItems[trackItemIndex].details[packVariants[i][j]].left = relativeFroms[packVariants[i][j]] - ctx2.measureText(variantIds[packVariants[i][j]]).width - 3;
								trackItems[trackItemIndex].details[packVariants[i][j]].right = relativeTos[packVariants[i][j]];
								trackItems[trackItemIndex].details[packVariants[i][j]].top = y - 10;
								trackItems[trackItemIndex].details[packVariants[i][j]].bottom = y;
							}
							relativeWidth = Math.abs(relativeTos[packVariants[i][j]] - relativeFroms[packVariants[i][j]] + 1);
							if(variantTypes[packVariants[i][j]] == variantType_SNV || variantTypes[packVariants[i][j]] == variantType_OTHERS) {
								ctx1.fillStyle = "#000";
								ctx2.fillStyle = "#000";
							} else if(variantTypes[packVariants[i][j]] == variantType_INSERTION) {
								ctx1.fillStyle = "#FF0000";
								ctx2.fillStyle = "#FF0000";
							} else if(variantTypes[packVariants[i][j]] == variantType_DELETION) {
								ctx1.fillStyle = "#00FF00";
								ctx2.fillStyle = "#00FF00";
							} else {
								ctx1.fillStyle = "#0000FF";
								ctx2.fillStyle = "#0000FF";
							}
							ctx2.fillText(variantIds[packVariants[i][j]], relativeFroms[packVariants[i][j]] - ctx2.measureText(variantIds[packVariants[i][j]]).width - 3, y - 2);
							ctx2.fillRect(relativeFroms[packVariants[i][j]], y - 10, relativeWidth, 10);
						}
						y = y + 13;
					}
					canvas2.addEventListener("mousemove", canvasMousemove, false);
					canvas2.addEventListener("click", canvasClickForVariant, false);
				}
			}
			if(mode == "squish" || ((mode == "pack") && (packVariants.length > 50 || variantNodes.length > parseInt(trackLength / 60) * 50))) {
				if(variantNodes.length > 0) {
					squishVariants[squishVariants.length] = [];
					squishVariants[0][0] = 0;
				}
				for( i = 1; i < variantNodes.length; i++) {
					for( j = 0; j < squishVariants.length; j++) {
						if(relativeFroms[i] > relativeTos[squishVariants[j][squishVariants[j].length - 1]]) {
							squishVariants[j][squishVariants[j].length] = i;
							break;
						}
					}
					if(j == squishVariants.length) {
						squishVariants[squishVariants.length] = [];
						squishVariants[j][0] = i;
					}
				}
				canvas1.height = 5 * squishVariants.length + 3*(squishVariants.length - 1);
				canvas1.style.height = 5 * squishVariants.length + 3*(squishVariants.length - 1);
				canvas2.height = 5 * squishVariants.length + 3*(squishVariants.length - 1);
				canvas2.style.height = 5 * squishVariants.length + 3*(squishVariants.length - 1);
				
				ctx1.fillStyle = "#000";
				ctx1.fillText(variantName, canvas1.width - ctx1.measureText(variantName).width, 8);
				
				y = 0;
				for( i = 0; i < squishVariants.length; i++) {
					for( j = 0; j < squishVariants[i].length; j++) {
						relativeWidth = Math.abs(relativeTos[squishVariants[i][j]] - relativeFroms[squishVariants[i][j]] + 1);
						switch(variantTypes[squishVariants[i][j]]) {
							case variantType_DELETION:
								ctx2.fillStyle = "#00FF00";
								break;
							case variantType_INSERTION:
								ctx2.fillStyle = "#FF0000";
								break;
							case variantType_MUTIPLE:
								ctx2.fillStyle = "#0000FF";
								break;
							default:
								ctx2.fillStyle = "#000";
						}
						ctx2.fillRect(relativeFroms[squishVariants[i][j]], y, relativeWidth, 5);
					}
					y = y + 8;
				}
			}
		}
	}
}

function overScaleShow(trackId) {
	var trackNode = document.getElementById(trackId);
	var canvasNodes = trackNode.getElementsByTagName("canvas");
	var canvas1 = canvasNodes[0], canvas2 = canvasNodes[1];
	if(canvas1.getContext && canvas2.getContext) {
		var ctx1 = canvas1.getContext('2d');
		var ctx2 = canvas2.getContext('2d');
		canvas1.height = 10;
		canvas1.style.height = 10;
		canvas2.height = 10;
		canvas2.style.height = 10;
		ctx1.fillStyle = "#000";
		ctx1.fillText(trackId, canvas1.width - ctx1.measureText(trackId).width, 8);
		ctx2.fillStyle = "#000";
		var tempStr = "zoom in to <= 1,000,000 bases to view items";
		ctx2.fillText(tempStr, (canvas2.width - ctx2.measureText(tempStr).width) / 2, 8);
	}
	if(document.getElementById(trackId).getElementsByClassName("thickbox").length > 0){
		$(document.getElementById(trackId).getElementsByClassName("thickbox")[0]).remove();
	}
	canvas2.removeEventListener("mousemove", canvasMousemove, false);
	canvas2.removeEventListener("click", canvasClickForVariant, false);
}

function showPersonalGeneByImg_TwoNode(canvas1, canvas2, geneNode, geneNode2, mode) {
	var geneNodes = geneNode.getElementsByTagName(xmlTagElement);
	var elementFroms = [], elementTos = [], elementIds = [], elementDirections = [], elementColors = [];
	var elementRelativeFroms = [], elementRelativeTos = [];
	var geneNodeName = geneNode.getAttribute(xmlAttributeId);
	var subElements = [], subElementRelativeFroms = [], subElemnetRelativeTos = [], subElementTypes = [];
	var variants = [], variantRelativeFroms = [], variantRelativeTos = [], variantTypes = [], variantIds = [], variantLetters = [];
	var subElementWidth, elementRelativeWidth;
	
	var geneNodes2 = geneNode2.getElementsByTagName(xmlTagElement);
	var elementFroms2 = [], elementTos2 = [], elementIds2 = [], elementDirections2 = [], elementColors2 = [];
	var elementRelativeFroms2 = [], elementRelativeTos2 = [];
	var geneNodeName2 = geneNode2.getAttribute(xmlAttributeId);
	var subElements2 = [], subElementRelativeFroms2 = [], subElemnetRelativeTos2 = [], subElementTypes2 = [];
	var variants2 = [], variantRelativeFroms2 = [], variantRelativeTos2 = [], variantTypes2 = [], variantIds2 = [], variantLetters2 = [];
	var subElementWidth, elementRelativeWidth;
	var geneNodeName_show = geneNodeName;
	var elementStatus = [], elementStatus2 = [];
	
	var i, j, k, m;
	var extend_width = 10;
	var img_height = 30, img_width = 21;
	
	var img_Letter_Array = [];
	var imgDEL_AA, imgINS_AA, imgOTH_AA;
	var imgStart, imgStart2, imgStop, imgStop2, imgASS, imgDSS, imgShift;
	var bkgImg_shift, bkgImg_pshift;
	img_Letter_Array["A"] = document.getElementById("img_A");
	img_Letter_Array["B"] = document.getElementById("img_B");
	img_Letter_Array["C"] = document.getElementById("img_C");
	img_Letter_Array["D"] = document.getElementById("img_D");
	img_Letter_Array["E"] = document.getElementById("img_E");
	img_Letter_Array["F"] = document.getElementById("img_F");
	img_Letter_Array["G"] = document.getElementById("img_G");
	img_Letter_Array["H"] = document.getElementById("img_H");
	img_Letter_Array["I"] = document.getElementById("img_I");
	img_Letter_Array["J"] = document.getElementById("img_J");
	img_Letter_Array["K"] = document.getElementById("img_K");
	img_Letter_Array["L"] = document.getElementById("img_L");
	img_Letter_Array["M"] = document.getElementById("img_M");
	img_Letter_Array["N"] = document.getElementById("img_N");
	img_Letter_Array["O"] = document.getElementById("img_O");
	img_Letter_Array["P"] = document.getElementById("img_P");
	img_Letter_Array["Q"] = document.getElementById("img_Q");
	img_Letter_Array["R"] = document.getElementById("img_R");
	img_Letter_Array["S"] = document.getElementById("img_S");
	img_Letter_Array["T"] = document.getElementById("img_T");
	img_Letter_Array["U"] = document.getElementById("img_U");
	img_Letter_Array["V"] = document.getElementById("img_V");
	img_Letter_Array["W"] = document.getElementById("img_W");
	img_Letter_Array["Y"] = document.getElementById("img_Y");
	img_Letter_Array["Z"] = document.getElementById("img_Z");

	imgDEL_AA = document.getElementById("imgDEL_AA");
	imgINS_AA = document.getElementById("imgINS_AA");
	imgOTH_AA = document.getElementById("imgOTH_AA");
	imgStart = document.getElementById("imgStart");
	imgStart2 = document.getElementById("imgStart2");
	imgStop = document.getElementById("imgStop");
	imgStop2 = document.getElementById("imgStop2");
	imgASS = document.getElementById("imgASS");
	imgDSS = document.getElementById("imgDSS");
	imgShift = document.getElementById("imgShift");
	imgStatus = document.getElementById("imgStatus");
	bkgImg_shift = document.getElementById("bkgImg_shift");
	bkgImg_pshift = document.getElementById("bkgImg_pshift");

	var colorStyle = "#000";
	if(geneNodeName == "_refGene") {
		colorStyle = "#8B8B00";
	} else if(geneNodeName == "_ensemblGene") {
		colorStyle = "RGB(109,115,243)";
	} else if(geneNodeName == "_knownGene") {
		colorStyle = "RGB(163,17,90)";
	} else {
		colorStyle = "#000";
	}

	for( i = 0; i < geneNodes.length; i++) {
		elementIds[i] = geneNodes[i].getAttribute(xmlAttributeId);
		elementFroms[i] = geneNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
		elementTos[i] = geneNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
		elementDirections[i] = geneNodes[i].getElementsByTagName(xmlTagDirection)[0].childNodes[0].nodeValue;
		if(geneNodes[i].getElementsByTagName(xmlTagColor).length > 0) {
			elementColors[i] = "RGB(" + geneNodes[i].getElementsByTagName(xmlTagColor)[0].firstChild.nodeValue + ")";
		} else {
			elementColors[i] = colorStyle;
		}
		elementRelativeFroms[i] = parseInt((parseInt(elementFroms[i]) - startIndex) / searchLength * trackLength);
		elementRelativeTos[i] = parseInt((parseInt(elementTos[i]) - startIndex + 1) / searchLength * trackLength);
		
		if(geneNodes[i].getElementsByTagName(xmlTagStatus).length > 0){
			elementStatus[i] = true;
		}else{
			elementStatus[i] = false;
		}

		subElements[i] = geneNodes[i].getElementsByTagName(xmlTagSubElement);
		subElementRelativeFroms[i] = [];
		subElemnetRelativeTos[i] = [];
		subElementTypes[i] = [];

		variants[i] = [];
		variantIds[i] = [];
		variantTypes[i] = [];
		variantRelativeFroms[i] = [];
		variantRelativeTos[i] = [];
		variantLetters[i] = [];

		if(subElements[i].length == 0) {
			subElements[i] = [];
			subElements[i][0] = " ";
			//virtual subElement Node
			subElementTypes[i][0] = subElementTypeBoxValue;
			subElementRelativeFroms[i][0] = elementRelativeFroms[i];
			subElemnetRelativeTos[i][0] = elementRelativeTos[i];

			variants[i][0] = geneNodes[i].getElementsByTagName(xmlTagVariant);
			variantIds[i][0] = [];
			variantTypes[i][0] = [];
			variantRelativeFroms[i][0] = [];
			variantRelativeTos[i][0] = [];
			variantLetters[i][0] = [];
			for( k = 0; k < variants[i][0].length; k++) {
				variantIds[i][0][k] = variants[i][0][k].getAttribute(xmlAttributeId);
				variantTypes[i][0][k] = variants[i][0][k].getAttribute(xmlAttributeType);
				variantRelativeFroms[i][0][k] = parseInt((parseInt(variants[i][0][k].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
				variantRelativeTos[i][0][k] = parseInt((parseInt(variants[i][0][k].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
				variantLetters[i][0][k] = variants[i][0][k].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
			}
		} else {
			for( j = 0; j < subElements[i].length; j++) {
				subElementTypes[i][j] = subElements[i][j].getAttribute(xmlAttributeType);
				subElementRelativeFroms[i][j] = parseInt((parseInt(subElements[i][j].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
				subElemnetRelativeTos[i][j] = parseInt((parseInt(subElements[i][j].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
				variants[i][j] = subElements[i][j].getElementsByTagName(xmlTagVariant);
				variantIds[i][j] = [];
				variantTypes[i][j] = [];
				variantRelativeFroms[i][j] = [];
				variantRelativeTos[i][j] = [];
				variantLetters[i][j] = [];
				for( k = 0; k < variants[i][j].length; k++) {
					variantIds[i][j][k] = variants[i][j][k].getAttribute(xmlAttributeId);
					variantTypes[i][j][k] = variants[i][j][k].getAttribute(xmlAttributeType);
					variantRelativeFroms[i][j][k] = parseInt((parseInt(variants[i][j][k].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
					variantRelativeTos[i][j][k] = parseInt((parseInt(variants[i][j][k].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
					variantLetters[i][j][k] = variants[i][j][k].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
				}
			}
		}
	}
	
	for( i = 0; i < geneNodes2.length; i++) {
		elementIds2[i] = geneNodes2[i].getAttribute(xmlAttributeId);
		elementFroms2[i] = geneNodes2[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
		elementTos2[i] = geneNodes2[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
		elementDirections2[i] = geneNodes2[i].getElementsByTagName(xmlTagDirection)[0].childNodes[0].nodeValue;
		if(geneNodes2[i].getElementsByTagName(xmlTagColor).length > 0) {
			elementColors2[i] = "RGB(" + geneNodes2[i].getElementsByTagName(xmlTagColor)[0].firstChild.nodeValue + ")";
		} else {
			elementColors2[i] = colorStyle;
		}
		elementRelativeFroms2[i] = parseInt((parseInt(elementFroms2[i]) - startIndex) / searchLength * trackLength);
		elementRelativeTos2[i] = parseInt((parseInt(elementTos2[i]) - startIndex + 1) / searchLength * trackLength);
		
		if(geneNodes2[i].getElementsByTagName(xmlTagStatus).length > 0){
			elementStatus2[i] = true;
		}else{
			elementStatus2[i] = false;
		}

		subElements2[i] = geneNodes2[i].getElementsByTagName(xmlTagSubElement);
		subElementRelativeFroms2[i] = [];
		subElemnetRelativeTos2[i] = [];
		subElementTypes2[i] = [];

		variants2[i] = [];
		variantIds2[i] = [];
		variantTypes2[i] = [];
		variantRelativeFroms2[i] = [];
		variantRelativeTos2[i] = [];
		variantLetters2[i] = [];

		if(subElements2[i].length == 0) {
			subElements2[i] = [];
			subElements2[i][0] = " ";
			//virtual subElement Node
			subElementTypes2[i][0] = subElementTypeBoxValue;
			subElementRelativeFroms2[i][0] = elementRelativeFroms2[i];
			subElemnetRelativeTos2[i][0] = elementRelativeTos2[i];

			variants2[i][0] = geneNodes2[i].getElementsByTagName(xmlTagVariant);
			variantIds2[i][0] = [];
			variantTypes2[i][0] = [];
			variantRelativeFroms2[i][0] = [];
			variantRelativeTos2[i][0] = [];
			variantLetters2[i][0] = [];
			for( k = 0; k < variants2[i][0].length; k++) {
				variantIds2[i][0][k] = variants2[i][0][k].getAttribute(xmlAttributeId);
				variantTypes2[i][0][k] = variants2[i][0][k].getAttribute(xmlAttributeType);
				variantRelativeFroms2[i][0][k] = parseInt((parseInt(variants2[i][0][k].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
				variantRelativeTos2[i][0][k] = parseInt((parseInt(variants2[i][0][k].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
				variantLetters2[i][0][k] = variants2[i][0][k].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
			}
		} else {
			for( j = 0; j < subElements2[i].length; j++) {
				subElementTypes2[i][j] = subElements2[i][j].getAttribute(xmlAttributeType);
				subElementRelativeFroms2[i][j] = parseInt((parseInt(subElements2[i][j].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
				subElemnetRelativeTos2[i][j] = parseInt((parseInt(subElements2[i][j].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
				variants2[i][j] = subElements2[i][j].getElementsByTagName(xmlTagVariant);
				variantIds2[i][j] = [];
				variantTypes2[i][j] = [];
				variantRelativeFroms2[i][j] = [];
				variantRelativeTos2[i][j] = [];
				variantLetters2[i][j] = [];
				for( k = 0; k < variants2[i][j].length; k++) {
					variantIds2[i][j][k] = variants2[i][j][k].getAttribute(xmlAttributeId);
					variantTypes2[i][j][k] = variants2[i][j][k].getAttribute(xmlAttributeType);
					variantRelativeFroms2[i][j][k] = parseInt((parseInt(variants2[i][j][k].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
					variantRelativeTos2[i][j][k] = parseInt((parseInt(variants2[i][j][k].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
					variantLetters2[i][j][k] = variants2[i][j][k].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
				}
			}
		}
	}
	
	if(((/^_/).test(geneNodeName))){
		geneNodeName_show = (geneNodeName + "").replace("_","");
	}
	
	personalPannel.Panno.details = [];

	if(canvas1.getContext && canvas2.getContext) {
		var ctx1 = canvas1.getContext('2d');
		var ctx2 = canvas2.getContext('2d');
		if(geneNodes.length == 0 && geneNodes2.length == 0) {
			canvas1.height = 10;
			canvas1.style.height = 10;
			canvas2.height = 10;
			canvas2.style.height = 10;
			ctx1.strokeStyle = "#000";
			ctx1.fillStyle = "#000";
			ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);
			
			return;
		}
		if(mode == "dense") {
			canvas1.height = (img_height + 10)* 2 + 3;
			canvas1.style.height = (img_height + 10)* 2 + 3;
			canvas2.height = (img_height + 10)* 2 + 3;
			canvas2.style.height = (img_height + 10)* 2 + 3;
			ctx1.strokeStyle = "#000";
			ctx1.fillStyle = "#000";
			ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);

			for( i = 0; i < subElements.length; i++) {
				ctx2.fillStyle = elementColors[i];
				for( j = 0; j < subElements[i].length; j++) {
					subElementWidth = Math.abs(subElemnetRelativeTos[i][j] - subElementRelativeFroms[i][j] + 1);
					switch(subElementTypes[i][j]) {
						case subElementTypeBoxValue:
							ctx2.fillRect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
							break;
						case subElementTypeBandValue:
							ctx2.fillRect(subElementRelativeFroms[i][j], 3 + img_height, subElementWidth, 5);
							break;
						case subElementTypeLineValue:
							ctx2.fillRect(subElementRelativeFroms[i][j], 5 + img_height, subElementWidth, 1);
							break;
						case "eBox":
							ctx2.fillRect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
							if(elementDirections[i] == "+"){
								ctx2.rect(subElemnetRelativeTos[i][j], img_height, extend_width, 10);
							}else{
								ctx2.rect(subElementRelativeFroms[i][j] - extend_width, img_height, extend_width, 10);
							}
							
							break;
						case "sBand":
							ctx2.rect(subElementRelativeFroms[i][j], 3 + img_height, subElementWidth, 5);
							ctx2.fillRect(subElementRelativeFroms[i][j], 5 + img_height, subElementWidth, 1);
							
							break;
						case "shBox":
							var drawImage_eleWidth = subElementWidth;
							var drawImage_imgWidth = $(bkgImg_shift).width();
							var drawImage_from = subElementRelativeFroms[i][j];
							var drawImage_drawWidth;
							while(drawImage_eleWidth > 0){
								drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
								ctx2.drawImage(bkgImg_shift, 0, 0, drawImage_drawWidth, 10, drawImage_from, img_height, drawImage_drawWidth, 10);
								drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
								drawImage_from = drawImage_from + drawImage_drawWidth;
							}
							//ctx2.drawImage(bkgImg_shift, 0, 0, subElementWidth, 10, subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
							ctx2.rect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
							
							break;
						case "lBox":
							ctx2.rect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
							ctx2.fillRect(subElementRelativeFroms[i][j], 3 + img_height, subElementWidth, 5);
							
							break;
						case "skBox":
							ctx2.rect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
							ctx2.fillRect(subElementRelativeFroms[i][j], 5 + img_height, subElementWidth, 1);
							
							break;
						case "psBox":
							var drawImage_eleWidth = subElementWidth;
							var drawImage_imgWidth = $(bkgImg_pshift).width();
							var drawImage_from = subElementRelativeFroms[i][j];
							var drawImage_drawWidth;
							while(drawImage_eleWidth > 0){
								drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
								ctx2.drawImage(bkgImg_pshift, 0, 0, drawImage_drawWidth, 10, drawImage_from, img_height, drawImage_drawWidth, 10);
								drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
								drawImage_from = drawImage_from + drawImage_drawWidth;
							}
							//ctx2.drawImage(bkgImg_pshift, 0, 0, subElementWidth, 10, subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
							ctx2.rect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
							break;
						case "eBand":
							ctx2.fillRect(subElementRelativeFroms[i][j], 3 + img_height, subElementWidth, 5);
							if(elementDirections[i] == "+"){
								ctx2.rect(subElemnetRelativeTos[i][j], 3 + img_height, extend_width, 5);
							}else{
								ctx2.rect(subElementRelativeFroms[i][j] - extend_width, 3 + img_height, extend_width, 5);
							}
							
							
							break;
						case "seBox":
							var drawImage_eleWidth = subElementWidth;
							var drawImage_imgWidth = $(bkgImg_shift).width();
							var drawImage_from = subElementRelativeFroms[i][j];
							var drawImage_drawWidth;
							while(drawImage_eleWidth > 0){
								drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
								ctx2.drawImage(bkgImg_shift, 0, 0, drawImage_drawWidth, 10, drawImage_from, img_height, drawImage_drawWidth, 10);
								drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
								drawImage_from = drawImage_from + drawImage_drawWidth;
							}
							//ctx2.drawImage(bkgImg_shift, 0, 0, subElementWidth, 10, subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
							ctx2.rect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
							if(elementDirections[i] == "+"){
								ctx2.rect(subElemnetRelativeTos[i][j], img_height, extend_width, 10);
							}else{
								ctx2.rect(subElementRelativeFroms[i][j] - extend_width, img_height, extend_width, 10);
							}
							
							break;
						case "pseBox":
							var drawImage_eleWidth = subElementWidth;
							var drawImage_imgWidth = $(bkgImg_pshift).width();
							var drawImage_from = subElementRelativeFroms[i][j];
							var drawImage_drawWidth;
							while(drawImage_eleWidth > 0){
								drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
								ctx2.drawImage(bkgImg_pshift, 0, 0, drawImage_drawWidth, 10, drawImage_from, img_height, drawImage_drawWidth, 10);
								drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
								drawImage_from = drawImage_from + drawImage_drawWidth;
							}
							//ctx2.drawImage(bkgImg_pshift, 0, 0, subElementWidth, 10, subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
							ctx2.rect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
							if(elementDirections[i] == "+"){
								ctx2.rect(subElemnetRelativeTos[i][j], img_height, extend_width, 10);
							}else{
								ctx2.rect(subElementRelativeFroms[i][j] - extend_width, img_height, extend_width, 10);
							}
							
							break;
					}
					for( k = 0; k < variants[i][j].length; k++) {
						var geneVariantLetters = variantLetters[i][j][k].split(":");
						if(geneVariantLetters.length == 1) {
							if(geneVariantLetters[0] == "^") {
								if(elementDirections[i] == "+"){
									ctx2.drawImage(imgStart, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
								}else{
									ctx2.drawImage(imgStart2, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
								}
							} else if(geneVariantLetters[0] == "#") {
								ctx2.drawImage(imgShift, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
							} else if(geneVariantLetters[0] == "(") {
								if(elementDirections[i] == "+") {
									ctx2.drawImage(imgASS, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
								} else {
									ctx2.drawImage(imgDSS, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
								}
							} else if(geneVariantLetters[0] == ")") {
								if(elementDirections[i] == "+") {
									ctx2.drawImage(imgDSS, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
								} else {
									ctx2.drawImage(imgASS, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
								}
							}
						} else {
							if((variantLetters[i][j][k] + "").indexOf("$") != -1) {
								if((geneVariantLetters[0] + "").indexOf("$") != -1) {
									ctx2.drawImage(imgStop2, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
								} else {
									ctx2.drawImage(imgStop, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
								}
							} else {
								if((geneVariantLetters[0] + "").length > (geneVariantLetters[1] + "").length || (geneVariantLetters[1] + "").indexOf("_") != -1) {
									ctx2.drawImage(imgDEL_AA, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
								} else if((geneVariantLetters[0] + "").length < (geneVariantLetters[1] + "").length || (geneVariantLetters[0] + "").indexOf("_") != -1) {
									ctx2.drawImage(imgINS_AA, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
								} else {
									if(variantLetters[i][j][k].split(":")[1] != variantLetters[i][j][k].split(":")[0]){
										ctx2.drawImage(img_Letter_Array[variantLetters[i][j][k].split(":")[1]], (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
									}
								}
							}
						}
					}
				}
			}
			/*********************************************************************************************************/
			for( i = 0; i < subElements2.length; i++) {
				ctx2.fillStyle = elementColors2[i];
				for( j = 0; j < subElements2[i].length; j++) {
					subElementWidth2 = Math.abs(subElemnetRelativeTos2[i][j] - subElementRelativeFroms2[i][j] + 1);
					switch(subElementTypes2[i][j]) {
						case subElementTypeBoxValue:
							ctx2.fillRect(subElementRelativeFroms2[i][j], img_height + 10 + 3 + img_height, subElementWidth2, 10);
							break;
						case subElementTypeBandValue:
							ctx2.fillRect(subElementRelativeFroms2[i][j], 3 + img_height + 10 + 3 + img_height, subElementWidth2, 5);
							break;
						case subElementTypeLineValue:
							ctx2.fillRect(subElementRelativeFroms2[i][j], 5 + img_height + 10 + 3 + img_height, subElementWidth2, 1);
							break;
						case "eBox":
							ctx2.fillRect(subElementRelativeFroms2[i][j], img_height + 10 + 3 + img_height, subElementWidth2, 10);
							if(elementDirections2[i] == "+"){
								ctx2.rect(subElemnetRelativeTos2[i][j], img_height + 10 + 3 + img_height, extend_width, 10);
							}else{
								ctx2.rect(subElementRelativeFroms2[i][j] + extend_width, img_height + 10 + 3 + img_height, extend_width, 10);
							}
							
							break;
						case "sBand":
							ctx2.rect(subElementRelativeFroms2[i][j], 3 + img_height + 10 + 3 + img_height, subElementWidth2, 5);
							ctx2.fillRect(subElementRelativeFroms2[i][j], 5 + img_height + 10 + 3 + img_height, subElementWidth2, 1);
							
							break;
						case "shBox":
							var drawImage_eleWidth = subElementWidth2;
							var drawImage_imgWidth = $(bkgImg_shift).width();
							var drawImage_from = subElementRelativeFroms2[i][j];
							var drawImage_drawWidth;
							while(drawImage_eleWidth > 0){
								drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
								ctx2.drawImage(bkgImg_shift, 0, 0, drawImage_drawWidth, 10, drawImage_from, img_height + 10 + 3 + img_height, drawImage_drawWidth, 10);
								drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
								drawImage_from = drawImage_from + drawImage_drawWidth;
							}
							//ctx2.drawImage(bkgImg_shift, 0, 0, subElementWidth2, 10, subElementRelativeFroms2[i][j], img_height + 10 + 3 + img_height, subElementWidth2, 10);
							ctx2.rect(subElementRelativeFroms2[i][j], img_height + 10 + 3 + img_height, subElementWidth2, 10);
							
							break;
						case "lBox":
							ctx2.rect(subElementRelativeFroms2[i][j], img_height + 10 + 3 + img_height, subElementWidth2, 10);
							ctx2.fillRect(subElementRelativeFroms2[i][j], 3 + img_height + 10 + 3 + img_height, subElementWidth2, 5);
							
							break;
						case "skBox":
							ctx2.rect(subElementRelativeFroms2[i][j], img_height + 10 + 3 + img_height, subElementWidth2, 10);
							ctx2.fillRect(subElementRelativeFroms2[i][j], 5 + img_height + 10 + 3 + img_height, subElementWidth2, 1);
							
							break;
						case "psBox":
							var drawImage_eleWidth = subElementWidth2;
							var drawImage_imgWidth = $(bkgImg_pshift).width();
							var drawImage_from = subElementRelativeFroms2[i][j];
							var drawImage_drawWidth;
							while(drawImage_eleWidth > 0){
								drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
								ctx2.drawImage(bkgImg_pshift, 0, 0, drawImage_drawWidth, 10, drawImage_from, img_height + 10 + 3 + img_height, drawImage_drawWidth, 10);
								drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
								drawImage_from = drawImage_from + drawImage_drawWidth;
							}
							//ctx2.drawImage(bkgImg_pshift, 0, 0, subElementWidth2, 10, subElementRelativeFroms2[i][j], img_height + 10 + 3 + img_height, subElementWidth2, 10);
							ctx2.rect(subElementRelativeFroms2[i][j], img_height + 10 + 3 + img_height, subElementWidth2, 10);
							break;
						case "eBand":
							ctx2.fillRect(subElementRelativeFroms2[i][j], 3 + img_height + 10 + 3 + img_height, subElementWidth2, 5);
							if(elementDirections2[i] == "+"){
								ctx2.rect(subElemnetRelativeTos2[i][j], 3 + img_height + 10 + 3 + img_height, extend_width, 5);
							}else{
								ctx2.rect(subElementRelativeFroms2[i][j] - extend_width, 3 + img_height + 10 + 3 + img_height, extend_width, 5);
							}
							
							break;
						case "seBox":
							var drawImage_eleWidth = subElementWidth2;
							var drawImage_imgWidth = $(bkgImg_shift).width();
							var drawImage_from = subElementRelativeFroms2[i][j];
							var drawImage_drawWidth;
							while(drawImage_eleWidth > 0){
								drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
								ctx2.drawImage(bkgImg_shift, 0, 0, drawImage_drawWidth, 10, drawImage_from, img_height + 10 + 3 + img_height, drawImage_drawWidth, 10);
								drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
								drawImage_from = drawImage_from + drawImage_drawWidth;
							}
							//ctx2.drawImage(bkgImg_shift, 0, 0, subElementWidth2, 10, subElementRelativeFroms2[i][j], img_height + 10 + 3 + img_height, subElementWidth2, 10);
							ctx2.rect(subElementRelativeFroms2[i][j], img_height + 10 + 3 + img_height, subElementWidth2, 10);
							if(elementDirections2[i] == "+"){
								ctx2.rect(subElemnetRelativeTos2[i][j], img_height + 10 + 3 + img_height, extend_width, 10);
							}else{
								ctx2.rect(subElementRelativeFroms2[i][j] - extend_width, img_height + 10 + 3 + img_height, extend_width, 10);
							}
							
							break;
						case "pseBox":
							var drawImage_eleWidth = subElementWidth2;
							var drawImage_imgWidth = $(bkgImg_pshift).width();
							var drawImage_from = subElementRelativeFroms2[i][j];
							var drawImage_drawWidth;
							while(drawImage_eleWidth > 0){
								drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
								ctx2.drawImage(bkgImg_pshift, 0, 0, drawImage_drawWidth, 10, drawImage_from, img_height + 10 + 3 + img_height, drawImage_drawWidth, 10);
								drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
								drawImage_from = drawImage_from + drawImage_drawWidth;
							}
							//ctx2.drawImage(bkgImg_pshift, 0, 0, subElementWidth2, 10, subElementRelativeFroms2[i][j], img_height + 10 + 3 + img_height, subElementWidth2, 10);
							ctx2.rect(subElementRelativeFroms2[i][j], img_height + 10 + 3 + img_height, subElementWidth2, 10);
							if(elementDirections2[i] == "+"){
								ctx2.rect(subElemnetRelativeTos2[i][j], img_height + 10 + 3 + img_height, extend_width, 10);
							}else{
								ctx2.rect(subElementRelativeFroms2[i][j] - extend_width, img_height + 10 + 3 + img_height, extend_width, 10);
							}
							
							break;
					}
					for( k = 0; k < variants2[i][j].length; k++) {
						var geneVariantLetters = variantLetters2[i][j][k].split(":");
						if(geneVariantLetters.length == 1) {
							if(geneVariantLetters[0] == "^") {
								if(elementDirections2[i] == "+"){
									ctx2.drawImage(imgStart, (variantRelativeFroms2[i][j][k] + variantRelativeTos2[i][j][k] - img_width) / 2, 10 + 3 + img_height);
								}else{
									ctx2.drawImage(imgStart2, (variantRelativeFroms2[i][j][k] + variantRelativeTos2[i][j][k] - img_width) / 2, 10 + 3 + img_height);
								}
							} else if(geneVariantLetters[0] == "#") {
								ctx2.drawImage(imgShift, (variantRelativeFroms2[i][j][k] + variantRelativeTos2[i][j][k] - img_width) / 2, 10 + 3 + img_height);
							} else if(geneVariantLetters[0] == "(") {
								if(elementDirections2[i] == "+") {
									ctx2.drawImage(imgASS, (variantRelativeFroms2[i][j][k] + variantRelativeTos2[i][j][k] - img_width) / 2, 10 + 3 + img_height);
								} else {
									ctx2.drawImage(imgDSS, (variantRelativeFroms2[i][j][k] + variantRelativeTos2[i][j][k] - img_width) / 2, 10 + 3 + img_height);
								}
							} else if(geneVariantLetters[0] == ")") {
								if(elementDirections2[i] == "+") {
									ctx2.drawImage(imgDSS, (variantRelativeFroms2[i][j][k] + variantRelativeTos2[i][j][k] - img_width) / 2, 10 + 3 + img_height);
								} else {
									ctx2.drawImage(imgASS, (variantRelativeFroms2[i][j][k] + variantRelativeTos2[i][j][k] - img_width) / 2, 10 + 3 + img_height);
								}
							}
						} else {
							if((variantLetters2[i][j][k] + "").indexOf("$") != -1) {
								if((geneVariantLetters[0] + "").indexOf("$") != -1) {
									ctx2.drawImage(imgStop2, (variantRelativeFroms2[i][j][k] + variantRelativeTos2[i][j][k] - img_width) / 2, 10 + 3 + img_height);
								} else {
									ctx2.drawImage(imgStop, (variantRelativeFroms2[i][j][k] + variantRelativeTos2[i][j][k] - img_width) / 2, 10 + 3 + img_height);
								}
							} else {
								if((geneVariantLetters[0] + "").length > (geneVariantLetters[1] + "").length || (geneVariantLetters[1] + "").indexOf("_") != -1) {
									ctx2.drawImage(imgDEL_AA, (variantRelativeFroms2[i][j][k] + variantRelativeTos2[i][j][k] - img_width) / 2, 10 + 3 + img_height);
								} else if((geneVariantLetters[0] + "").length < (geneVariantLetters[1] + "").length || (geneVariantLetters[0] + "").indexOf("_") != -1) {
									ctx2.drawImage(imgINS_AA, (variantRelativeFroms2[i][j][k] + variantRelativeTos2[i][j][k] - img_width) / 2, 10 + 3 + img_height);
								} else {
									if(variantLetters2[i][j][k].split(":")[1] !=variantLetters2[i][j][k].split(":")[0]){
										ctx2.drawImage(img_Letter_Array[variantLetters2[i][j][k].split(":")[1]], (variantRelativeFroms2[i][j][k] + variantRelativeTos2[i][j][k] - img_width) / 2, 10 + 3 + img_height);
									}
								}
							}
						}
					}
				}
			}
			
		} else {
			var packVariants = [], squishVariants = [];
			var packVariants2 = [], squishVariants2 = [];
			if((mode == "pack") && ((geneNodes.length + geneNodes2.length) <= parseInt(trackLength / 50) * 50)) {//geneNodes.length <= parseInt(trackLength / 50   此判断是假定一个gene最小占位为50，则宽为trackLength，高为50的track可放的最多gene数目
				packVariants[packVariants.length] = [];
				packVariants[0][0] = 0;
				for( i = 1; i < geneNodes.length; i++) {
					for( j = 0; j < packVariants.length; j++) {
						if(elementIds[i] != ".") {
							if((elementRelativeFroms[i] - ctx1.measureText(elementIds[i]).width - 8) > elementRelativeTos[packVariants[j][packVariants[j].length - 1]]) {
								packVariants[j][packVariants[j].length] = i;
								break;
							}
						} else {
							if((elementRelativeFroms[i] - 5) > elementRelativeTos[packVariants[j][packVariants[j].length - 1]]) {
								packVariants[j][packVariants[j].length] = i;
								break;
							}
						}
					}
					if(j == packVariants.length) {
						packVariants[packVariants.length] = [];
						packVariants[j][0] = i;
					}
				}
				/***************************************************************/
				packVariants2[packVariants2.length] = [];
				packVariants2[0][0] = 0;
				for( i = 1; i < geneNodes2.length; i++) {
					for( j = 0; j < packVariants2.length; j++) {
						if(elementIds2[i] != ".") {
							if((elementRelativeFroms2[i] - ctx1.measureText(elementIds2[i]).width - 8) > elementRelativeTos2[packVariants2[j][packVariants2[j].length - 1]]) {
								packVariants2[j][packVariants2[j].length] = i;
								break;
							}
						} else {
							if((elementRelativeFroms2[i] - 5) > elementRelativeTos2[packVariants2[j][packVariants2[j].length - 1]]) {
								packVariants2[j][packVariants2[j].length] = i;
								break;
							}
						}
					}
					if(j == packVariants2.length) {
						packVariants2[packVariants2.length] = [];
						packVariants2[j][0] = i;
					}
				}
				
				
				if(packVariants.length + packVariants2.length<= 50) {
					canvas1.height = (13 + img_height) * (packVariants.length + packVariants2.length) - 3;
					canvas1.style.height = (13 + img_height) * (packVariants.length + packVariants2.length) - 3;
					canvas2.height = (13 + img_height) * (packVariants.length + packVariants2.length) - 3;
					canvas2.style.height = (13 + img_height) *(packVariants.length + packVariants2.length) - 3;
					
					ctx1.strokeStyle = "#000";
					ctx1.fillStyle = "#000";
					ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);

					y = 10 + img_height;
					for( i = 0; i < packVariants.length; i++) {
						for( j = 0; j < packVariants[i].length; j++) {
							personalPannel.Panno.details[packVariants[i][j]] = [];
							personalPannel.Panno.details[packVariants[i][j]].id = elementIds[packVariants[i][j]];
							personalPannel.Panno.details[packVariants[i][j]].from = elementFroms[packVariants[i][j]];
							personalPannel.Panno.details[packVariants[i][j]].to = elementTos[packVariants[i][j]];
							personalPannel.Panno.details[packVariants[i][j]].left = elementRelativeFroms[packVariants[i][j]];
							personalPannel.Panno.details[packVariants[i][j]].right = elementRelativeTos[packVariants[i][j]];
							personalPannel.Panno.details[packVariants[i][j]].top = y - 10;
							personalPannel.Panno.details[packVariants[i][j]].bottom = y;
							
							if(elementIds[i] != ".") {
								ctx2.fillStyle = elementColors[packVariants[i][j]];
								ctx2.strokeStyle = elementColors[packVariants[i][j]];
								ctx2.fillText(elementIds[packVariants[i][j]], elementRelativeFroms[packVariants[i][j]] - ctx2.measureText(elementIds[packVariants[i][j]]).width - 3, y - 2);
								if(elementStatus[packVariants[i][j]]){
									ctx2.drawImage(imgStatus, elementRelativeFroms[packVariants[i][j]] - 21, y - 10 - img_height);
								}
							}

							ctx2.fillStyle = elementColors[packVariants[i][j]];
							ctx2.strokeStyle = elementColors[packVariants[i][j]];

							if(geneNodes[packVariants[i][j]].getElementsByTagName(xmlTagSubElement).length == 0) {
								elementRelativeWidth = elementRelativeTos[packVariants[i][j]] - elementRelativeFroms[packVariants[i][j]] + 1;
								drawGeneByDirectionBox(elementRelativeFroms[packVariants[i][j]], elementRelativeTos[packVariants[i][j]], elementRelativeWidth, elementDirections[packVariants[i][j]], y, ctx2);
								m = 0;
								for( k = 0; k < variants[packVariants[i][j]][m].length; k++) {
									var geneVariantLetters = variantLetters[packVariants[i][j]][m][k].split(":");
									if(geneVariantLetters.length == 1) {
										if(geneVariantLetters[0] == "^") {
											if(elementDirections[packVariants[i][j]] == "+"){
												ctx2.drawImage(imgStart, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											}else{
												ctx2.drawImage(imgStart2, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											}
										} else if(geneVariantLetters[0] == "#") {
											ctx2.drawImage(imgShift, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
										} else if(geneVariantLetters[0] == "(") {
											if(elementDirections[packVariants[i][j]] == "+") {
												ctx2.drawImage(imgASS, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											} else {
												ctx2.drawImage(imgDSS, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											}
										} else if(geneVariantLetters[0] == ")") {
											if(elementDirections[packVariants[i][j]] == "+") {
												ctx2.drawImage(imgDSS, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											} else {
												ctx2.drawImage(imgASS, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											}
										}
									} else {
										if((variantLetters[packVariants[i][j]][m][k] + "").indexOf("$") != -1) {
											if((geneVariantLetters[0] + "").indexOf("$") != -1) {
												ctx2.drawImage(imgStop2, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											} else {
												ctx2.drawImage(imgStop, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											}
										} else {
											if((geneVariantLetters[0] + "").length > (geneVariantLetters[1] + "").length || (geneVariantLetters[1] + "").indexOf("_") != -1) {
												ctx2.drawImage(imgDEL_AA, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											} else if((geneVariantLetters[0] + "").length < (geneVariantLetters[1] + "").length || (geneVariantLetters[0] + "").indexOf("_") != -1) {
												ctx2.drawImage(imgINS_AA, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											} else {
												if(variantLetters[packVariants[i][j]][k][m].split(":")[1] != variantLetters[packVariants[i][j]][k][m].split(":")[0]){
													ctx2.drawImage(img_Letter_Array[variantLetters[packVariants[i][j]][k][m].split(":")[1]], (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
												}
											}
										}
									}
								}
							} else {
								for( k = 0; k < subElements[packVariants[i][j]].length; k++) {
									subElementWidth = Math.abs(subElemnetRelativeTos[packVariants[i][j]][k] - subElementRelativeFroms[packVariants[i][j]][k] + 1);
									//save time and memory by Liran
									if(subElemnetRelativeTos[packVariants[i][j]][k]<0 || subElementRelativeFroms[packVariants[i][j]][k]>trackLength){
										continue;
									}
									//save time and memory by Liran
									var preType = " ";
									if(k > 0) {
										preType = subElementTypes[packVariants[i][j]][k - 1];
									}
									drawPersonalGeneSubElement(subElementRelativeFroms[packVariants[i][j]][k], subElemnetRelativeTos[packVariants[i][j]][k], subElementWidth, subElementTypes[packVariants[i][j]][k], elementDirections[packVariants[i][j]], y, elementColors[packVariants[i][j]], preType, ctx2);
									for( m = 0; m < variants[packVariants[i][j]][k].length; m++) {
										var geneVariantLetters = variantLetters[packVariants[i][j]][k][m].split(":");
										if(geneVariantLetters.length == 1) {
											if(geneVariantLetters[0] == "^") {
												if(elementDirections[packVariants[i][j]] == "+"){
													ctx2.drawImage(imgStart, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												}else{
													ctx2.drawImage(imgStart2, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												}	
											} else if(geneVariantLetters[0] == "#") {
												ctx2.drawImage(imgShift, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
											} else if(geneVariantLetters[0] == "(") {
												if(elementDirections[packVariants[i][j]] == "+") {
													ctx2.drawImage(imgASS, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												} else {
													ctx2.drawImage(imgDSS, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												}
											} else if(geneVariantLetters[0] == ")") {
												if(elementDirections[packVariants[i][j]] == "+") {
													ctx2.drawImage(imgDSS, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												} else {
													ctx2.drawImage(imgASS, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												}
											}
										} else {
											if((variantLetters[packVariants[i][j]][k][m] + "").indexOf("$") != -1) {
												if((geneVariantLetters[0] + "").indexOf("$") != -1) {
													ctx2.drawImage(imgStop2, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												} else {
													ctx2.drawImage(imgStop, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												}
											} else {
												if((geneVariantLetters[0] + "").length > (geneVariantLetters[1] + "").length || (geneVariantLetters[1] + "").indexOf("_") != -1) {
													ctx2.drawImage(imgDEL_AA, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												} else if((geneVariantLetters[0] + "").length < (geneVariantLetters[1] + "").length || (geneVariantLetters[0] + "").indexOf("_") != -1) {
													ctx2.drawImage(imgINS_AA, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												} else {
													if(variantLetters[packVariants[i][j]][k][m].split(":")[1] != variantLetters[packVariants[i][j]][k][m].split(":")[0]){
														ctx2.drawImage(img_Letter_Array[variantLetters[packVariants[i][j]][k][m].split(":")[1]], (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
													}
												}
											}
										}
									}
								}
							}
						}
						y = y + 13 + img_height;
					}
					/**********************************************************************************/
					for( i = 0; i < packVariants2.length; i++) {
						for( j = 0; j < packVariants2[i].length; j++) {
							var personalPannel_Panno_details_length = personalPannel.Panno.details.length;
							personalPannel.Panno.details[personalPannel_Panno_details_length] = [];
							personalPannel.Panno.details[personalPannel_Panno_details_length].id = elementIds2[packVariants2[i][j]];
							personalPannel.Panno.details[personalPannel_Panno_details_length].from = elementFroms2[packVariants2[i][j]];
							personalPannel.Panno.details[personalPannel_Panno_details_length].to = elementTos2[packVariants2[i][j]];
							personalPannel.Panno.details[personalPannel_Panno_details_length].left = elementRelativeFroms2[packVariants2[i][j]];
							personalPannel.Panno.details[personalPannel_Panno_details_length].right = elementRelativeTos2[packVariants2[i][j]];
							personalPannel.Panno.details[personalPannel_Panno_details_length].top = y - 10;
							personalPannel.Panno.details[personalPannel_Panno_details_length].bottom = y;
							
							if(elementIds2[i] != ".") {
								ctx2.fillStyle = elementColors2[packVariants2[i][j]];
								ctx2.strokeStyle = elementColors2[packVariants2[i][j]];
								ctx2.fillText(elementIds2[packVariants2[i][j]], elementRelativeFroms2[packVariants2[i][j]] - ctx2.measureText(elementIds2[packVariants2[i][j]]).width - 3, y - 2);
								if(elementStatus2[packVariants2[i][j]]){
									ctx2.drawImage(imgStatus, elementRelativeFroms2[packVariants2[i][j]] - 21, y - 10 - img_height);
								}
							}

							ctx2.fillStyle = elementColors2[packVariants2[i][j]];
							ctx2.strokeStyle = elementColors2[packVariants2[i][j]];

							if(geneNodes2[packVariants2[i][j]].getElementsByTagName(xmlTagSubElement).length == 0) {
								elementRelativeWidth2 = elementRelativeTos2[packVariants2[i][j]] - elementRelativeFroms2[packVariants2[i][j]] + 1;
								drawGeneByDirectionBox(elementRelativeFroms2[packVariants2[i][j]], elementRelativeTos2[packVariants2[i][j]], elementRelativeWidth2, elementDirections2[packVariants2[i][j]], y, ctx2);
								m = 0;
								for( k = 0; k < variants2[packVariants2[i][j]][m].length; k++) {
									var geneVariantLetters = variantLetters2[packVariants2[i][j]][m][k].split(":");
									if(geneVariantLetters.length == 1) {
										if(geneVariantLetters[0] == "^") {
											if(elementDirections2[packVariants2[i][j]] == "+"){
												ctx2.drawImage(imgStart, (variantRelativeFroms2[packVariants2[i][j]][m][k] + variantRelativeTos2[packVariants2[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											}else{
												ctx2.drawImage(imgStart2, (variantRelativeFroms2[packVariants2[i][j]][m][k] + variantRelativeTos2[packVariants2[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											}
										} else if(geneVariantLetters[0] == "#") {
											ctx2.drawImage(imgShift, (variantRelativeFroms2[packVariants2[i][j]][m][k] + variantRelativeTos2[packVariants2[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
										} else if(geneVariantLetters[0] == "(") {
											if(elementDirections2[packVariants2[i][j]] == "+") {
												ctx2.drawImage(imgASS, (variantRelativeFroms2[packVariants2[i][j]][m][k] + variantRelativeTos2[packVariants2[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											} else {
												ctx2.drawImage(imgDSS, (variantRelativeFroms2[packVariants2[i][j]][m][k] + variantRelativeTos2[packVariants2[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											}
										} else if(geneVariantLetters[0] == ")") {
											if(elementDirections2[packVariants2[i][j]] == "+") {
												ctx2.drawImage(imgDSS, (variantRelativeFroms2[packVariants2[i][j]][m][k] + variantRelativeTos2[packVariants2[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											} else {
												ctx2.drawImage(imgASS, (variantRelativeFroms2[packVariants2[i][j]][m][k] + variantRelativeTos2[packVariants2[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											}
										}
									} else {
										if((variantLetters2[packVariants2[i][j]][m][k] + "").indexOf("$") != -1) {
											if((geneVariantLetters[0] + "").indexOf("$") != -1) {
												ctx2.drawImage(imgStop2, (variantRelativeFroms2[packVariants2[i][j]][m][k] + variantRelativeTos2[packVariants2[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											} else {
												ctx2.drawImage(imgStop, (variantRelativeFroms2[packVariants2[i][j]][m][k] + variantRelativeTos2[packVariants2[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											}
										} else {
											if((geneVariantLetters[0] + "").length > (geneVariantLetters[1] + "").length || (geneVariantLetters[1] + "").indexOf("_") != -1) {
												ctx2.drawImage(imgDEL_AA, (variantRelativeFroms2[packVariants2[i][j]][m][k] + variantRelativeTos2[packVariants2[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											} else if((geneVariantLetters[0] + "").length < (geneVariantLetters[1] + "").length || (geneVariantLetters[0] + "").indexOf("_") != -1) {
												ctx2.drawImage(imgINS_AA, (variantRelativeFroms2[packVariants2[i][j]][m][k] + variantRelativeTos2[packVariants2[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											} else {
												if(variantLetters2[packVariants2[i][j]][m][k].split(":")[1] != variantLetters2[packVariants2[i][j]][m][k].split(":")[0]){
													ctx2.drawImage(img_Letter_Array[variantLetters2[packVariants2[i][j]][m][k].split(":")[1]], (variantRelativeFroms2[packVariants2[i][j]][m][k] + variantRelativeTos2[packVariants2[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
												}
											}
										}
									}
								}
							} else {
								for( k = 0; k < subElements2[packVariants2[i][j]].length; k++) {
									subElementWidth2 = Math.abs(subElemnetRelativeTos2[packVariants2[i][j]][k] - subElementRelativeFroms2[packVariants2[i][j]][k] + 1);
									//save time and memory by Liran
									if(subElemnetRelativeTos2[packVariants2[i][j]][k]<0 || subElementRelativeFroms2[packVariants2[i][j]][k]>trackLength){
										continue;
									}
									//save time and memory by Liran
									var preType = " ";
									if(k > 0) {
										preType = subElementTypes2[packVariants2[i][j]][k - 1];
									}
									drawPersonalGeneSubElement(subElementRelativeFroms2[packVariants2[i][j]][k], subElemnetRelativeTos2[packVariants2[i][j]][k], subElementWidth2, subElementTypes2[packVariants2[i][j]][k], elementDirections2[packVariants2[i][j]], y, elementColors2[packVariants2[i][j]], preType, ctx2);
									for( m = 0; m < variants2[packVariants2[i][j]][k].length; m++) {
										var geneVariantLetters = variantLetters2[packVariants2[i][j]][k][m].split(":");
										if(geneVariantLetters.length == 1) {
											if(geneVariantLetters[0] == "^") {
												if(elementDirections2[packVariants2[i][j]] == "+") {
													ctx2.drawImage(imgStart, (variantRelativeFroms2[packVariants2[i][j]][k][m] + variantRelativeTos2[packVariants2[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												}else{
													ctx2.drawImage(imgStart2, (variantRelativeFroms2[packVariants2[i][j]][k][m] + variantRelativeTos2[packVariants2[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												}
											} else if(geneVariantLetters[0] == "#") {
												ctx2.drawImage(imgShift, (variantRelativeFroms2[packVariants2[i][j]][k][m] + variantRelativeTos2[packVariants2[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
											} else if(geneVariantLetters[0] == "(") {
												if(elementDirections2[packVariants2[i][j]] == "+") {
													ctx2.drawImage(imgASS, (variantRelativeFroms2[packVariants2[i][j]][k][m] + variantRelativeTos2[packVariants2[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												} else {
													ctx2.drawImage(imgDSS, (variantRelativeFroms2[packVariants2[i][j]][k][m] + variantRelativeTos2[packVariants2[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												}
											} else if(geneVariantLetters[0] == ")") {
												if(elementDirections2[packVariants2[i][j]] == "+") {
													ctx2.drawImage(imgDSS, (variantRelativeFroms2[packVariants2[i][j]][k][m] + variantRelativeTos2[packVariants2[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												} else {
													ctx2.drawImage(imgASS, (variantRelativeFroms2[packVariants2[i][j]][k][m] + variantRelativeTos2[packVariants2[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												}
											}
										} else {
											if((variantLetters2[packVariants2[i][j]][k][m] + "").indexOf("$") != -1) {
												if((geneVariantLetters[0] + "").indexOf("$") != -1) {
													ctx2.drawImage(imgStop2, (variantRelativeFroms2[packVariants2[i][j]][k][m] + variantRelativeTos2[packVariants2[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												} else {
													ctx2.drawImage(imgStop, (variantRelativeFroms2[packVariants2[i][j]][k][m] + variantRelativeTos2[packVariants2[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												}
											} else {
												if((geneVariantLetters[0] + "").length > (geneVariantLetters[1] + "").length || (geneVariantLetters[1] + "").indexOf("_") != -1) {
													ctx2.drawImage(imgDEL_AA, (variantRelativeFroms2[packVariants2[i][j]][k][m] + variantRelativeTos2[packVariants2[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												} else if((geneVariantLetters[0] + "").length < (geneVariantLetters[1] + "").length || (geneVariantLetters[0] + "").indexOf("_") != -1) {
													ctx2.drawImage(imgINS_AA, (variantRelativeFroms2[packVariants2[i][j]][k][m] + variantRelativeTos2[packVariants2[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												} else {
													if(variantLetters2[packVariants2[i][j]][k][m].split(":")[1] != variantLetters2[packVariants2[i][j]][k][m].split(":")[0]){
														ctx2.drawImage(img_Letter_Array[variantLetters2[packVariants2[i][j]][k][m].split(":")[1]], (variantRelativeFroms2[packVariants2[i][j]][k][m] + variantRelativeTos2[packVariants2[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
													}
												}
											}
										}
									}
								}
							}
						}
						y = y + 13 + img_height;
					}
					canvas2.addEventListener("mousemove", canvasMousemoveOnPP, false);
					canvas2.addEventListener("click", canvasClickForPersonalGene, false);
				}
			}
			if(mode == "squish" || ((mode == "pack") && (packVariants.length + packVariants2.length > 50 || (geneNodes.length + geneNodes2.length) > parseInt(trackLength / 50) * 50))) {
				squishVariants[squishVariants.length] = [];
				squishVariants[0][0] = 0;
				for( i = 1; i < geneNodes.length; i++) {
					for( j = 0; j < squishVariants.length; j++) {
						if(elementRelativeFroms[i] > elementRelativeTos[squishVariants[j][squishVariants[j].length - 1]]) {
							squishVariants[j][squishVariants[j].length] = i;
							break;
						}
					}
					if(j == squishVariants.length) {
						squishVariants[squishVariants.length] = [];
						squishVariants[j][0] = i;
					}
				}
				/***************************************************************************************************/
				squishVariants2[squishVariants2.length] = [];
				squishVariants2[0][0] = 0;
				for( i = 1; i < geneNodes2.length; i++) {
					for( j = 0; j < squishVariants2.length; j++) {
						if(elementRelativeFroms2[i] > elementRelativeTos2[squishVariants2[j][squishVariants2[j].length - 1]]) {
							squishVariants2[j][squishVariants2[j].length] = i;
							break;
						}
					}
					if(j == squishVariants2.length) {
						squishVariants2[squishVariants2.length] = [];
						squishVariants2[j][0] = i;
					}
				}
				
				canvas1.height = 8 * (squishVariants.length + squishVariants2.length) - 3;
				canvas1.style.height = 8 * (squishVariants.length + squishVariants2.length) - 3;
				canvas2.height = 8 * (squishVariants.length + squishVariants2.length) - 3;
				canvas2.style.height = 8 * (squishVariants.length + squishVariants2.length) - 3;
				ctx1.strokeStyle = "#000";
				ctx1.fillStyle = "#000";
				ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);
				
				y = 0;
				for( i = 0; i < squishVariants.length; i++) {
					for( j = 0; j < squishVariants[i].length; j++) {
						ctx2.fillStyle = elementColors[squishVariants[i][j]];
						for( k = 0; k < subElements[squishVariants[i][j]].length; k++) {
							subElementWidth = Math.abs(subElemnetRelativeTos[squishVariants[i][j]][k] - subElementRelativeFroms[squishVariants[i][j]][k] + 1);
							if(subElementTypes[squishVariants[i][j]][k] == subElementTypeLineValue) {
								ctx2.fillRect(subElementRelativeFroms[squishVariants[i][j]][k], y + 2, subElementWidth, 1);
							} else {
								ctx2.fillRect(subElementRelativeFroms[squishVariants[i][j]][k], y, subElementWidth, 5);
							}
						}
					}
					y = y + 8;
				}
				
				/****************************************************************************/
				for( i = 0; i < squishVariants2.length; i++) {
					for( j = 0; j < squishVariants2[i].length; j++) {
						ctx2.fillStyle = elementColors2[squishVariants2[i][j]];
						for( k = 0; k < subElements2[squishVariants2[i][j]].length; k++) {
							subElementWidth2 = Math.abs(subElemnetRelativeTos2[squishVariants2[i][j]][k] - subElementRelativeFroms2[squishVariants2[i][j]][k] + 1);
							if(subElementTypes2[squishVariants2[i][j]][k] == subElementTypeLineValue) {
								ctx2.fillRect(subElementRelativeFroms2[squishVariants2[i][j]][k], y + 2, subElementWidth2, 1);
							} else {
								ctx2.fillRect(subElementRelativeFroms2[squishVariants2[i][j]][k], y, subElementWidth2, 5);
							}
						}
					}
					y = y + 8;
				}
			}

		}		
	}
}

function showPersonalGeneByImg_OneNode(canvas1, canvas2, geneNode, mode) {
	var geneNodes = geneNode.getElementsByTagName(xmlTagElement);
	var elementFroms = [], elementTos = [], elementIds = [], elementDirections = [], elementColors = [];
	var elementRelativeFroms = [], elementRelativeTos = [];
	var geneNodeName = geneNode.getAttribute(xmlAttributeId);
	var subElements = [], subElementRelativeFroms = [], subElemnetRelativeTos = [], subElementTypes = [];
	var variants = [], variantRelativeFroms = [], variantRelativeTos = [], variantTypes = [], variantIds = [], variantLetters = [];
	var subElementWidth, elementRelativeWidth;
	var geneNodeName_show = geneNodeName;
	var elementStatus = [];
	
	var i, j, k, m;
	var extend_width = 10;
	var img_height = 30, img_width = 21;
	//var img_A, img_B, img_C, img_D, img_E, img_F, img_G, img_H, img_I, img_J, img_K, img_L, img_M, img_N, img_O, img_P, img_Q, img_R, img_S, img_T, img_U, img_V, img_W, img_Y, img_Z;
	var img_Letter_Array = [];
	var imgDEL_AA, imgINS_AA, imgOTH_AA;
	var imgStart, imgStart2, imgStop, imgStop2, imgASS, imgDSS, imgShift, imgStatus;
	var bkgImg_shift, bkgImg_pshift;
	img_Letter_Array["A"] = document.getElementById("img_A");
	img_Letter_Array["B"] = document.getElementById("img_B");
	img_Letter_Array["C"] = document.getElementById("img_C");
	img_Letter_Array["D"] = document.getElementById("img_D");
	img_Letter_Array["E"] = document.getElementById("img_E");
	img_Letter_Array["F"] = document.getElementById("img_F");
	img_Letter_Array["G"] = document.getElementById("img_G");
	img_Letter_Array["H"] = document.getElementById("img_H");
	img_Letter_Array["I"] = document.getElementById("img_I");
	img_Letter_Array["J"] = document.getElementById("img_J");
	img_Letter_Array["K"] = document.getElementById("img_K");
	img_Letter_Array["L"] = document.getElementById("img_L");
	img_Letter_Array["M"] = document.getElementById("img_M");
	img_Letter_Array["N"] = document.getElementById("img_N");
	img_Letter_Array["O"] = document.getElementById("img_O");
	img_Letter_Array["P"] = document.getElementById("img_P");
	img_Letter_Array["Q"] = document.getElementById("img_Q");
	img_Letter_Array["R"] = document.getElementById("img_R");
	img_Letter_Array["S"] = document.getElementById("img_S");
	img_Letter_Array["T"] = document.getElementById("img_T");
	img_Letter_Array["U"] = document.getElementById("img_U");
	img_Letter_Array["V"] = document.getElementById("img_V");
	img_Letter_Array["W"] = document.getElementById("img_W");
	img_Letter_Array["Y"] = document.getElementById("img_Y");
	img_Letter_Array["Z"] = document.getElementById("img_Z");

	imgDEL_AA = document.getElementById("imgDEL_AA");
	imgINS_AA = document.getElementById("imgINS_AA");
	imgOTH_AA = document.getElementById("imgOTH_AA");
	imgStart = document.getElementById("imgStart");
	imgStart2 = document.getElementById("imgStart2");
	imgStop = document.getElementById("imgStop");
	imgStop2 = document.getElementById("imgStop2");
	imgASS = document.getElementById("imgASS");
	imgDSS = document.getElementById("imgDSS");
	imgShift = document.getElementById("imgShift");
	imgStatus = document.getElementById("imgStatus");
	bkgImg_shift = document.getElementById("bkgImg_shift");
	bkgImg_pshift = document.getElementById("bkgImg_pshift");

	var colorStyle = "#000";
	if(geneNodeName == "_refGene") {
		colorStyle = "#8B8B00";
	} else if(geneNodeName == "_ensemblGene") {
		colorStyle = "RGB(109,115,243)";
	} else if(geneNodeName == "_knownGene") {
		colorStyle = "RGB(163,17,90)";
	} else {
		colorStyle = "#000";
	}
	
	personalPannel.Panno.details = [];

	for( i = 0; i < geneNodes.length; i++) {
		elementIds[i] = geneNodes[i].getAttribute(xmlAttributeId);
		elementFroms[i] = geneNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
		elementTos[i] = geneNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
		elementDirections[i] = geneNodes[i].getElementsByTagName(xmlTagDirection)[0].childNodes[0].nodeValue;
		if(geneNodes[i].getElementsByTagName(xmlTagColor).length > 0) {
			elementColors[i] = "RGB(" + geneNodes[i].getElementsByTagName(xmlTagColor)[0].firstChild.nodeValue + ")";
		} else {
			elementColors[i] = colorStyle;
		}
		elementRelativeFroms[i] = parseInt((parseInt(elementFroms[i]) - startIndex) / searchLength * trackLength);
		elementRelativeTos[i] = parseInt((parseInt(elementTos[i]) - startIndex + 1) / searchLength * trackLength);
		
		if(geneNodes[i].getElementsByTagName(xmlTagStatus).length > 0){
			elementStatus[i] = true;
		}else{
			elementStatus[i] = false;
		}

		subElements[i] = geneNodes[i].getElementsByTagName(xmlTagSubElement);
		subElementRelativeFroms[i] = [];
		subElemnetRelativeTos[i] = [];
		subElementTypes[i] = [];

		variants[i] = [];
		variantIds[i] = [];
		variantTypes[i] = [];
		variantRelativeFroms[i] = [];
		variantRelativeTos[i] = [];
		variantLetters[i] = [];

		if(subElements[i].length == 0) {
			subElements[i][0] = " ";
			//virtual subElement Node
			subElementTypes[i][0] = subElementTypeBoxValue;
			subElementRelativeFroms[i][0] = elementRelativeFroms[i];
			subElemnetRelativeTos[i][0] = elementRelativeTos[i];

			variants[i][0] = geneNodes[i].getElementsByTagName(xmlTagVariant);
			variantIds[i][0] = [];
			variantTypes[i][0] = [];
			variantRelativeFroms[i][0] = [];
			variantRelativeTos[i][0] = [];
			variantLetters[i][0] = [];
			for( k = 0; k < variants[i][0].length; k++) {
				variantIds[i][0][k] = variants[i][0][k].getAttribute(xmlAttributeId);
				variantTypes[i][0][k] = variants[i][0][k].getAttribute(xmlAttributeType);
				variantRelativeFroms[i][0][k] = parseInt((parseInt(variants[i][0][k].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
				variantRelativeTos[i][0][k] = parseInt((parseInt(variants[i][0][k].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
				variantLetters[i][0][k] = variants[i][0][k].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
			}
		} else {
			for( j = 0; j < subElements[i].length; j++) {
				subElementTypes[i][j] = subElements[i][j].getAttribute(xmlAttributeType);
				subElementRelativeFroms[i][j] = parseInt((parseInt(subElements[i][j].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
				subElemnetRelativeTos[i][j] = parseInt((parseInt(subElements[i][j].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
				variants[i][j] = subElements[i][j].getElementsByTagName(xmlTagVariant);
				variantIds[i][j] = [];
				variantTypes[i][j] = [];
				variantRelativeFroms[i][j] = [];
				variantRelativeTos[i][j] = [];
				variantLetters[i][j] = [];
				for( k = 0; k < variants[i][j].length; k++) {
					variantIds[i][j][k] = variants[i][j][k].getAttribute(xmlAttributeId);
					variantTypes[i][j][k] = variants[i][j][k].getAttribute(xmlAttributeType);
					variantRelativeFroms[i][j][k] = parseInt((parseInt(variants[i][j][k].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
					variantRelativeTos[i][j][k] = parseInt((parseInt(variants[i][j][k].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
					variantLetters[i][j][k] = variants[i][j][k].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
				}
			}
		}
	}

	if(((/^_/).test(geneNodeName))){
		geneNodeName_show = (geneNodeName + "").replace("_","");
	}
	
	if(canvas1.getContext && canvas2.getContext) {
		var ctx1 = canvas1.getContext('2d');
		var ctx2 = canvas2.getContext('2d');
		if(geneNodes.length == 0) {
			canvas1.height = 10;
			canvas1.style.height = 10;
			canvas2.height = 10;
			canvas2.style.height = 10;
			ctx1.strokeStyle = "#000";
			ctx1.fillStyle = "#000";
			ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);
		} else {
			if(mode == "dense") {
				canvas1.height = img_height + 10;
				canvas1.style.height = img_height + 10;
				canvas2.height = img_height + 10;
				canvas2.style.height = img_height + 10;
				ctx1.strokeStyle = "#000";
				ctx1.fillStyle = "#000";
				ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);

				for( i = 0; i < subElements.length; i++) {
					ctx2.fillStyle = elementColors[i];
					for( j = 0; j < subElements[i].length; j++) {
						subElementWidth = Math.abs(subElemnetRelativeTos[i][j] - subElementRelativeFroms[i][j] + 1);
						switch(subElementTypes[i][j]) {
							case subElementTypeBoxValue:
								ctx2.fillRect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
								break;
							case subElementTypeBandValue:
								ctx2.fillRect(subElementRelativeFroms[i][j], 3 + img_height, subElementWidth, 5);
								break;
							case subElementTypeLineValue:
								ctx2.fillRect(subElementRelativeFroms[i][j], 5 + img_height, subElementWidth, 1);
								break;
							case "eBox":
								ctx2.fillRect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
								if(elementDirections[i] == "+"){
									ctx2.rect(subElemnetRelativeTos[i][j], img_height, extend_width, 10);
								}else{
									ctx2.rect(subElementRelativeFroms[i][j] - extend_width, img_height, extend_width, 10);
								}
								
								//ctx2.drawImage(imgASS, subElemnetRelativeTos[i][j] - img_width / 2, 0);
								break;
							case "sBand":
								ctx2.rect(subElementRelativeFroms[i][j], 3 + img_height, subElementWidth, 5);
								ctx2.fillRect(subElementRelativeFroms[i][j], 5 + img_height, subElementWidth, 1);
								//ctx2.drawImage(imgDSS, subElemnetRelativeFroms[i][j] - img_width / 2, 0);
								break;
							case "shBox":
								var drawImage_eleWidth = subElementWidth;
								var drawImage_imgWidth = $(bkgImg_shift).width();
								var drawImage_from = subElementRelativeFroms[i][j];
								var drawImage_drawWidth;
								while(drawImage_eleWidth > 0){
									drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
									ctx2.drawImage(bkgImg_shift, 0, 0, drawImage_drawWidth, 10, drawImage_from, img_height, drawImage_drawWidth, 10);
									drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
									drawImage_from = drawImage_from + drawImage_drawWidth;
								}
								//ctx2.drawImage(bkgImg_shift, 0, 0, subElementWidth, 10, subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
								ctx2.rect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
								if(j == 0 || (j > 0 && subElementTypes[i][j - 1] != "shBox" && subElementTypes[i][j - 1] != "seBox")) {
									ctx2.drawImage(imgShift, subElemnetRelativeFroms[i][j] - img_width / 2, 0);
								}
								break;
							case "lBox":
								ctx2.rect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
								ctx2.fillRect(subElementRelativeFroms[i][j], 3 + img_height, subElementWidth, 5);
								/*if(j == 0 || (j > 0 && subElementTypes[i][j - 1] != "lBox")) {
									ctx2.drawImage(imgStop, subElemnetRelativeFroms[i][j] - img_width / 2, 0);
								}*/
								break;
							case "skBox":
								ctx2.rect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
								ctx2.fillRect(subElementRelativeFroms[i][j], 5 + img_height, subElementWidth, 1);
								//ctx2.drawImage(imgDSS, subElemnetRelativeFroms[i][j] - img_width / 2, 0);
								break;
							case "psBox":
								var drawImage_eleWidth = subElementWidth;
								var drawImage_imgWidth = $(bkgImg_shift).width();
								var drawImage_from = subElementRelativeFroms[i][j];
								var drawImage_drawWidth;
								while(drawImage_eleWidth > 0){
									drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
									ctx2.drawImage(bkgImg_shift, 0, 0, drawImage_drawWidth, 10, drawImage_from, img_height, drawImage_drawWidth, 10);
									drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
									drawImage_from = drawImage_from + drawImage_drawWidth;
								}
								//ctx2.drawImage(bkgImg_pshift, 0, 0, subElementWidth, 10, subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
								ctx2.rect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
								break;
							case "eBand":
								ctx2.fillRect(subElementRelativeFroms[i][j], 3 + img_height, subElementWidth, 5);
								if(elementDirections[i] == "+"){
									ctx2.rect(subElemnetRelativeTos[i][j], 3 + img_height, extend_width, 5);
								}else{
									ctx2.rect(subElementRelativeFroms[i][j] - extend_width, 3 + img_height, extend_width, 5);
								}
								
								//ctx2.drawImage(imgASS, subElemnetRelativeTos[i][j] - img_width / 2, 0);
								break;
							case "seBox":
								var drawImage_eleWidth = subElementWidth;
								var drawImage_imgWidth = $(bkgImg_shift).width();
								var drawImage_from = subElementRelativeFroms[i][j];
								var drawImage_drawWidth;
								while(drawImage_eleWidth > 0){
									drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
									ctx2.drawImage(bkgImg_shift, 0, 0, drawImage_drawWidth, 10, drawImage_from, img_height, drawImage_drawWidth, 10);
									drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
									drawImage_from = drawImage_from + drawImage_drawWidth;
								}
								//ctx2.drawImage(bkgImg_shift, 0, 0, subElementWidth, 10, subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
								ctx2.rect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
								/*if(j == 0 || (j > 0 && subElementTypes[i][j - 1] != "shBox" && subElementTypes[i][j - 1] != "seBox")) {
									ctx2.drawImage(imgShift, subElemnetRelativeFroms[i][j] - img_width / 2, 0);
								}*/
								if(elementDirections[i] == "+"){
									ctx2.rect(subElemnetRelativeTos[i][j], img_height, extend_width, 10);
								}else{
									ctx2.rect(subElementRelativeFroms[i][j] - extend_width, img_height, extend_width, 10);
								}
								
								//ctx2.drawImage(imgASS, subElemnetRelativeTos[i][j] - img_width / 2, 0);
								break;
							case "pseBox":
								var drawImage_eleWidth = subElementWidth;
								var drawImage_imgWidth = $(bkgImg_shift).width();
								var drawImage_from = subElementRelativeFroms[i][j];
								var drawImage_drawWidth;
								while(drawImage_eleWidth > 0){
									drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
									ctx2.drawImage(bkgImg_shift, 0, 0, drawImage_drawWidth, 10, drawImage_from, img_height, drawImage_drawWidth, 10);
									drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
									drawImage_from = drawImage_from + drawImage_drawWidth;
								}
								//ctx2.drawImage(bkgImg_pshift, 0, 0, subElementWidth, 10, subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
								ctx2.rect(subElementRelativeFroms[i][j], img_height, subElementWidth, 10);
								if(elementDirections[i] == "+"){
									ctx2.rect(subElemnetRelativeTos[i][j], img_height, extend_width, 10);
								}else{
									ctx2.rect(subElementRelativeFroms[i][j] - extend_width, img_height, extend_width, 10);
								}
								
								//ctx2.drawImage(imgASS, subElemnetRelativeTos[i][j] - img_width / 2, 0);
								break;
						}
						for( k = 0; k < variants[i][j].length; k++) {
							var geneVariantLetters = variantLetters[i][j][k].split(":");
							if(geneVariantLetters.length == 1){
								if(geneVariantLetters[0] == "^"){
									if(elementDirections[i] == "+"){
										ctx2.drawImage(imgStart, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
									}else{
										ctx2.drawImage(imgStart2, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
									}
								}else if(geneVariantLetters[0] == "#"){
									ctx2.drawImage(imgShift, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
								}else if(geneVariantLetters[0] == "("){
									if(elementDirections[i] == "+"){
										ctx2.drawImage(imgASS, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
									}else{
										ctx2.drawImage(imgDSS, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
									}
								}else if(geneVariantLetters[0] == ")"){
									if(elementDirections[i] == "+"){
										ctx2.drawImage(imgDSS, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
									}else{
										ctx2.drawImage(imgASS, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
									}
								}
							}else{
								if((variantLetters[i][j][k]+"").indexOf("$")!= -1){
									if((geneVariantLetters[0] + "").indexOf("$")!= -1){
										ctx2.drawImage(imgStop2, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
									}else{
										ctx2.drawImage(imgStop, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
									}
								}else{
									if((geneVariantLetters[0] + "").length > (geneVariantLetters[1] + "").length || (geneVariantLetters[1] + "").indexOf("_") != -1){
										ctx2.drawImage(imgDEL_AA, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
									}else if((geneVariantLetters[0] + "").length < (geneVariantLetters[1] + "").length || (geneVariantLetters[0] + "").indexOf("_") != -1){
										ctx2.drawImage(imgINS_AA, (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
									}else{
										if(variantLetters[i][j][k].split(":")[1] != variantLetters[i][j][k].split(":")[0]){
											ctx2.drawImage(img_Letter_Array[variantLetters[i][j][k].split(":")[1]], (variantRelativeFroms[i][j][k] + variantRelativeTos[i][j][k] - img_width) / 2, 0);
										}
									}
								}
							}
						}
					}
				}
			}else{
				var packVariants = [], squishVariants = [];
				if((mode == "pack" || (mode == "full" && geneNodes.length > 50)) && (geneNodes.length <= parseInt(trackLength / 50) * 50)) {//geneNodes.length <= parseInt(trackLength / 50   此判断是假定一个gene最小占位为50，则宽为trackLength，高为50的track可放的最多gene数目
					packVariants[packVariants.length] = [];
					packVariants[0][0] = 0;
					for( i = 1; i < geneNodes.length; i++) {
						for( j = 0; j < packVariants.length; j++) {
							if(elementIds[i] != ".") {
								if((elementRelativeFroms[i] - ctx1.measureText(elementIds[i]).width - 8) > elementRelativeTos[packVariants[j][packVariants[j].length - 1]]) {
									packVariants[j][packVariants[j].length] = i;
									break;
								}
							} else {
								if((elementRelativeFroms[i] - 5) > elementRelativeTos[packVariants[j][packVariants[j].length - 1]]) {
									packVariants[j][packVariants[j].length] = i;
									break;
								}
							}
						}
						if(j == packVariants.length) {
							packVariants[packVariants.length] = [];
							packVariants[j][0] = i;
						}
					}
					if(packVariants.length <= 50) {
						canvas1.height = (13 + img_height) * packVariants.length - 3;
						canvas1.style.height = (13 + img_height) * packVariants.length - 3;
						canvas2.height = (13 + img_height) * packVariants.length - 3;
						canvas2.style.height = (13 + img_height) * packVariants.length - 3;
						
						ctx1.strokeStyle = "#000";
						ctx1.fillStyle = "#000";
						ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);

						y = 10 + img_height;
						for( i = 0; i < packVariants.length; i++) {
							for( j = 0; j < packVariants[i].length; j++) {
								personalPannel.Panno.details[packVariants[i][j]] = [];
								personalPannel.Panno.details[packVariants[i][j]].id = elementIds[packVariants[i][j]];
								personalPannel.Panno.details[packVariants[i][j]].from = elementFroms[packVariants[i][j]];
								personalPannel.Panno.details[packVariants[i][j]].to = elementTos[packVariants[i][j]];
								personalPannel.Panno.details[packVariants[i][j]].left = elementRelativeFroms[packVariants[i][j]];
								personalPannel.Panno.details[packVariants[i][j]].right = elementRelativeTos[packVariants[i][j]];
								personalPannel.Panno.details[packVariants[i][j]].top = y - 10;
								personalPannel.Panno.details[packVariants[i][j]].bottom = y;
								
								if(elementIds[i] != ".") {
									ctx2.fillStyle = elementColors[packVariants[i][j]];
									ctx2.strokeStyle = elementColors[packVariants[i][j]];
									ctx2.fillText(elementIds[packVariants[i][j]], elementRelativeFroms[packVariants[i][j]] - ctx2.measureText(elementIds[packVariants[i][j]]).width - 3, y - 2);
									if(elementStatus[packVariants[i][j]]){
										ctx2.drawImage(imgStatus, elementRelativeFroms[packVariants[i][j]] - 21, y - 10 - img_height);
									}
								}
								
								ctx2.fillStyle = elementColors[packVariants[i][j]];
								ctx2.strokeStyle = elementColors[packVariants[i][j]];

								if(geneNodes[packVariants[i][j]].getElementsByTagName(xmlTagSubElement).length == 0) {
									elementRelativeWidth = elementRelativeTos[packVariants[i][j]] - elementRelativeFroms[packVariants[i][j]] + 1;
									drawGeneByDirectionBox(elementRelativeFroms[packVariants[i][j]], elementRelativeTos[packVariants[i][j]], elementRelativeWidth, elementDirections[packVariants[i][j]], y, ctx2);									
									m = 0;
									for( k = 0; k < variants[packVariants[i][j]][m].length; k++) {
										var geneVariantLetters = variantLetters[packVariants[i][j]][m][k].split(":");
										if(geneVariantLetters.length == 1){
											if(geneVariantLetters[0] == "^"){
												if(elementDirections[packVariants[i][j]] == "+"){
													ctx2.drawImage(imgStart, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
												}else{
													ctx2.drawImage(imgStart2, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
												}
											}else if(geneVariantLetters[0] == "#"){
												ctx2.drawImage(imgShift, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
											}else if(geneVariantLetters[0] == "("){
												if(elementDirections[packVariants[i][j]] == "+"){
													ctx2.drawImage(imgASS, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
												}else{
													ctx2.drawImage(imgDSS, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
												}
											}else if(geneVariantLetters[0] == ")"){
												if(elementDirections[packVariants[i][j]] == "+"){
													ctx2.drawImage(imgDSS, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
												}else{
													ctx2.drawImage(imgASS, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
												}
											}
										}else{
											if(( variantLetters[packVariants[i][j]][m][k]+"").indexOf("$")!= -1){
												if((geneVariantLetters[0] + "").indexOf("$")!= -1){
													ctx2.drawImage(imgStop2, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
												}else{
													ctx2.drawImage(imgStop, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
												}
											}else{
												if((geneVariantLetters[0] + "").length > (geneVariantLetters[1] + "").length || (geneVariantLetters[1] + "").indexOf("_") != -1){
													ctx2.drawImage(imgDEL_AA, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
												}else if((geneVariantLetters[0] + "").length < (geneVariantLetters[1] + "").length || (geneVariantLetters[0] + "").indexOf("_") != -1){
													ctx2.drawImage(imgINS_AA, (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
												}else{
													if(variantLetters[packVariants[i][j]][k][m].split(":")[1] != variantLetters[packVariants[i][j]][k][m].split(":")[0]){
														ctx2.drawImage(img_Letter_Array[variantLetters[packVariants[i][j]][k][m].split(":")[1]], (variantRelativeFroms[packVariants[i][j]][m][k] + variantRelativeTos[packVariants[i][j]][m][k] - img_width) / 2, y - 10 - img_height);
													}
												}
											}
										}
									}
								} else {
									for( k = 0; k < subElements[packVariants[i][j]].length; k++) {
										subElementWidth = Math.abs(subElemnetRelativeTos[packVariants[i][j]][k] - subElementRelativeFroms[packVariants[i][j]][k] + 1);
										//save time and memory by Liran
										if(subElemnetRelativeTos[packVariants[i][j]][k]<0 || subElementRelativeFroms[packVariants[i][j]][k]>trackLength){
											continue;
										}
										//save time and memory by Liran
										var preType = " ";
										if(k>0){
											preType = subElementTypes[packVariants[i][j]][k-1];
										}
										drawPersonalGeneSubElement(subElementRelativeFroms[packVariants[i][j]][k], subElemnetRelativeTos[packVariants[i][j]][k], subElementWidth, subElementTypes[packVariants[i][j]][k], elementDirections[packVariants[i][j]], y, elementColors[packVariants[i][j]], preType, ctx2);
										for(m = 0; m < variants[packVariants[i][j]][k].length; m++){
											var geneVariantLetters = variantLetters[packVariants[i][j]][k][m].split(":");
											if(geneVariantLetters.length == 1){
												if(geneVariantLetters[0] == "^"){
													if(elementDirections[packVariants[i][j]] == "+"){
														ctx2.drawImage(imgStart, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
													}else{
														ctx2.drawImage(imgStart2, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
													}
												}else if(geneVariantLetters[0] == "#"){
													ctx2.drawImage(imgShift, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
												}else if(geneVariantLetters[0] == "("){
													if(elementDirections[packVariants[i][j]] == "+"){
														ctx2.drawImage(imgASS, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
													}else{
														ctx2.drawImage(imgDSS, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
													}
												}else if(geneVariantLetters[0] == ")"){
													if(elementDirections[packVariants[i][j]] == "+"){
														ctx2.drawImage(imgDSS, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
													}else{
														ctx2.drawImage(imgASS, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
													}
												}
											}else{
												if(( variantLetters[packVariants[i][j]][k][m]+"").indexOf("$")!= -1){
													if((geneVariantLetters[0] + "").indexOf("$")!= -1){
														ctx2.drawImage(imgStop2, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
													}else{
														ctx2.drawImage(imgStop, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
													}
												}else{
													if((geneVariantLetters[0] + "").length > (geneVariantLetters[1] + "").length || (geneVariantLetters[1] + "").indexOf("_") != -1){
														ctx2.drawImage(imgDEL_AA, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
													}else if((geneVariantLetters[0] + "").length < (geneVariantLetters[1] + "").length || (geneVariantLetters[0] + "").indexOf("_") != -1){
														ctx2.drawImage(imgINS_AA, (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
													}else{
														if(variantLetters[packVariants[i][j]][k][m].split(":")[1] != variantLetters[packVariants[i][j]][k][m].split(":")[0]){
															ctx2.drawImage(img_Letter_Array[variantLetters[packVariants[i][j]][k][m].split(":")[1]], (variantRelativeFroms[packVariants[i][j]][k][m] + variantRelativeTos[packVariants[i][j]][k][m] - img_width) / 2, y - 10 - img_height);
														}
													}
												}
											}
										}
									}
								}
							}
							y = y + 13 + img_height;
						}
						canvas2.addEventListener("mousemove", canvasMousemoveOnPP, false);
						canvas2.addEventListener("click", canvasClickForPersonalGene, false);

					}
				}
				if(mode == "squish" || ((mode == "pack") && (packVariants.length > 50 || geneNodes.length > parseInt(trackLength / 50) * 50))) {
					squishVariants[squishVariants.length] = [];
					squishVariants[0][0] = 0;
					for( i = 1; i < geneNodes.length; i++) {
						for( j = 0; j < squishVariants.length; j++) {
							if(elementRelativeFroms[i] > elementRelativeTos[squishVariants[j][squishVariants[j].length - 1]]) {
								squishVariants[j][squishVariants[j].length] = i;
								break;
							}
						}
						if(j == squishVariants.length) {
							squishVariants[squishVariants.length] = [];
							squishVariants[j][0] = i;
						}
					}
					canvas1.height = 8 * squishVariants.length  - 3;
					canvas1.style.height = 8 * squishVariants.length - 3;
					canvas2.height = 8 * squishVariants.length - 3;
					canvas2.style.height = 8 * squishVariants.length - 3;
					
					ctx1.strokeStyle = "#000";
					ctx1.fillStyle = "#000";
					ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);

					y = 0;
					for( i = 0; i < squishVariants.length; i++) {
						for( j = 0; j < squishVariants[i].length; j++) {
							ctx2.fillStyle = elementColors[squishVariants[i][j]];
							for( k = 0; k < subElements[squishVariants[i][j]].length; k++) {
								subElementWidth = Math.abs(subElemnetRelativeTos[squishVariants[i][j]][k] - subElementRelativeFroms[squishVariants[i][j]][k] + 1);
								if(subElementTypes[squishVariants[i][j]][k] == subElementTypeLineValue) {
									ctx2.fillRect(subElementRelativeFroms[squishVariants[i][j]][k], y + 2, subElementWidth, 1);
								} else {
									ctx2.fillRect(subElementRelativeFroms[squishVariants[i][j]][k], y, subElementWidth, 5);
								}
							}
						}
						y = y + 8;
					}
				}

			}
		}
	}
}

function drawPersonalGeneSubElement(from, to, width, type, direction, y, colorStyle, preType, ctx){
	var extend_width = 10;
	var img_width = 21;
	var img_height = 30;
	var imgStart, imgStart2, imgStop, imgASS, imgDSS, imgShift;
	var bkgImg_shift, bkgImg_pshift;
	imgStart = document.getElementById("imgStart");
	imgStart2 = document.getElementById("imgStart2");
	imgStop = document.getElementById("imgStop");
	imgASS = document.getElementById("imgASS");
	imgDSS = document.getElementById("imgDSS");
	imgShift = document.getElementById("imgShift");
	bkgImg_shift = document.getElementById("bkgImg_shift");
	bkgImg_pshift = document.getElementById("bkgImg_pshift");
	
	switch(type) {
		case subElementTypeBoxValue:
			ctx.fillRect(from, y - 10, width, 10);
			break;
		case subElementTypeBandValue:
			ctx.fillRect(from, y - 7, width, 5);
			break;
		case subElementTypeLineValue:
			drawIntro(from, to, width, y, direction, colorStyle, ctx);
			break;
		case "eBox":
			ctx.fillRect(from, y - 10, width, 10);
			if(direction == "+"){
				ctx.rect(to, y - 9.5, extend_width, 9);
			}else{
				ctx.rect(from - extend_width, y - 9.5, extend_width, 9);
			}
			
			//ctx.drawImage(imgASS, to - img_width / 2, y - img_height - 10);
			break;
		case "sBand":
			ctx.rect(from, y - 6.5, width, 4);
			ctx.fillRect(from, y - 5, width, 1);
			//ctx.drawImage(imgDSS, from - img_width / 2, y - img_height - 10);
			break;
		case "shBox":
			var drawImage_eleWidth = width;
			var drawImage_imgWidth = $(bkgImg_shift).width();
			var drawImage_from = from;
			var drawImage_drawWidth;
			while(drawImage_eleWidth > 0){
				drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
				ctx.drawImage(bkgImg_shift, 0, 0, drawImage_drawWidth, 10, drawImage_from, y - 10, drawImage_drawWidth, 10);
				drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
				drawImage_from = drawImage_from + drawImage_drawWidth;
			}
			//ctx.drawImage(bkgImg_shift, 0, 0, width, 10, from, y - 10, width, 10);
			ctx.rect(from, y - 9.5, width, 9);
			/*if(preType != "shBox" && preType != "seBox") {
				ctx.drawImage(imgShift, from - img_width / 2, y - img_height - 10);
			}*/
			break;
		case "lBox":
			ctx.rect(from, y - 9.5, width, 9);
			ctx.fillRect(from, y - 7, width, 5);
			/*if(idx == 0 || (preType != "lBox")) {
				ctx.drawImage(imgStop, from - img_width / 2, y - img_height - 10);
			}*/
			break;
		case "skBox":
			ctx.rect(from, y - 9.5, width, 9);
			ctx.fillRect(from, y - 5, width, 1);
			//ctx.drawImage(imgDSS, from - img_width / 2, y - img_height - 10);
			break;
		case "psBox":
			var drawImage_eleWidth = width;
			var drawImage_imgWidth = $(bkgImg_pshift).width();
			var drawImage_from = from;
			var drawImage_drawWidth;
			while(drawImage_eleWidth > 0){
				drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
				ctx.drawImage(bkgImg_pshift, 0, 0, drawImage_drawWidth, 10, drawImage_from, y - 10, drawImage_drawWidth, 10);
				drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
				drawImage_from = drawImage_from + drawImage_drawWidth;
			}
			//ctx.drawImage(bkgImg_pshift, 0, 0, width, 10, from, y - 10, width, 10);
			ctx.rect(from, y - 9.5, width, 9);
			break;
		case "eBand":
			ctx.fillRect(from, y - 7, width, 5);
			if(direction == "+"){
				ctx.rect(to, y - 6.5, extend_width, 4);
			}else{
				ctx.rect(from - extend_width, y - 6.5, extend_width, 4);
			}
			
			//ctx.drawImage(imgASS, to - img_width / 2, y - img_height - 10);
			break;
		case "seBox":
			var drawImage_eleWidth = width;
			var drawImage_imgWidth = $(bkgImg_shift).width();
			var drawImage_from = from;
			var drawImage_drawWidth;
			while(drawImage_eleWidth > 0){
				drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
				ctx.drawImage(bkgImg_shift, 0, 0, drawImage_drawWidth, 10, drawImage_from, y - 10, drawImage_drawWidth, 10);
				drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
				drawImage_from = drawImage_from + drawImage_drawWidth;
			}
			//ctx.drawImage(bkgImg_shift, 0, 0, width, 10, from, y - 10, width, 10);
			ctx.rect(from, y - 9.5, width, 9);
			
			/*if(preType != "shBox" && preType != "seBox") {
				ctx.drawImage(imgShift, from - img_width / 2, y - img_height - 10);
			}*/
			if(direction == "+"){
				ctx.rect(to, y - 9.5, extend_width, 9);
			}else{
				ctx.rect(from - extend_width, y - 9.5, extend_width, 9);
			}
			
			//ctx.drawImage(imgASS, to - img_width / 2, y - img_height - 10);
			break;
		case "pseBox":
			var drawImage_eleWidth = width;
			var drawImage_imgWidth = $(bkgImg_pshift).width();
			var drawImage_from = from;
			var drawImage_drawWidth;
			while(drawImage_eleWidth > 0){
				drawImage_drawWidth = drawImage_eleWidth < drawImage_imgWidth ? drawImage_eleWidth : drawImage_imgWidth;
				ctx.drawImage(bkgImg_pshift, 0, 0, drawImage_drawWidth, 10, drawImage_from, y - 10, drawImage_drawWidth, 10);
				drawImage_eleWidth = drawImage_eleWidth - drawImage_drawWidth;
				drawImage_from = drawImage_from + drawImage_drawWidth;
			}
			//ctx.drawImage(bkgImg_pshift, 0, 0, width, 10, from, y - 10, width, 10);
			ctx.rect(from, y - 9.5, width, 9);
			if(direction == "+"){
				ctx.rect(to, y - 9.5, extend_width, 9);
			}else{
				ctx.rect(from - extend_width, y - 9.5, extend_width, 9);
			}
			
			//ctx.drawImage(imgASS, to - img_width / 2, y - img_height - 10);
			break;
	}
	ctx.stroke();
}

function drawIntro(from, to, width, y, direction, colorStyle, ctx) {//y is bottom of the gene
	var directionIndex;
	ctx.fillRect(from, y - 5, width, 1);
	ctx.strokeStyle = colorStyle;
	if(direction == "+") {
	//	directionIndex = to;
	//	while(directionIndex - 2 > from) {
		//save time and memory by Liran
		directionIndex = to<(trackLength+3)?to:(trackLength+3);
		while(directionIndex - 2 > (from>-3?from:-3)) {
		//save time and memory by Liran
			ctx.beginPath();
			ctx.moveTo(directionIndex, y - 5);
			ctx.lineTo(directionIndex - 2, y - 7);
			ctx.closePath();
			ctx.stroke();

			ctx.beginPath();
			ctx.moveTo(directionIndex + 1, y - 5);
			ctx.lineTo(directionIndex - 2, y - 2);
			ctx.closePath();
			ctx.stroke();
			directionIndex = directionIndex - 5;
		}
	} else {
	//	directionIndex = from;
	//	while(directionIndex + 2 < to) {
		//save time and memory by Liran
		directionIndex = from>-3?from:-3;
		while(directionIndex + 2 < (to<(trackLength+3)?to:(trackLength+3))) {
		//save time and memory by Liran
			ctx.beginPath();
			ctx.moveTo(directionIndex, y - 5);
			ctx.lineTo(directionIndex + 2, y - 7);
			ctx.closePath();
			ctx.stroke();

			ctx.beginPath();
			ctx.moveTo(directionIndex - 1, y - 5);
			ctx.lineTo(directionIndex + 2, y - 2);
			ctx.closePath();
			ctx.stroke();
			directionIndex = directionIndex + 5;
		}
	}
}

function drawGeneByDirectionBox(from, to, width, direction, y, ctx) {
	if(direction == "+") {
		if(width > 5) {
			ctx.fillRect(from, y - 10, width - 5, 10);
			ctx.beginPath();
			ctx.moveTo(to - 4, y - 10);
			ctx.lineTo(to, y - 5);
			ctx.lineTo(to - 4, y);
			ctx.fill();
		} else {
			ctx.beginPath();
			ctx.moveTo(from, y - 10);
			ctx.lineTo(to, y - 5);
			ctx.lineTo(from, y);
			ctx.fill();
		}
	} else {
		if(width > 5) {
			ctx.fillRect(from + 5, y - 10, width - 5, 10);
			ctx.beginPath();
			ctx.moveTo(from + 5, y - 10);
			ctx.lineTo(from, y - 5);
			ctx.lineTo(from + 5, y);
			ctx.fill();
		} else {
			ctx.beginPath();
			ctx.moveTo(to, y - 10);
			ctx.lineTo(from, y - 5);
			ctx.lineTo(to, y);
			ctx.fill();
		}
	}
}

function drawEndOfGene(from, to, y, ctx) {
	if(from < 0) {
		ctx.fillStyle = "#ffffff";
		ctx.strokeStyle = "#000";
		ctx.beginPath();
		ctx.moveTo(0.5, y - 5);
		ctx.lineTo(5.5, y);
		ctx.lineTo(5.5, y - 10);
		ctx.closePath();
		ctx.fill();
		ctx.stroke();

		ctx.beginPath();
		ctx.moveTo(5.5, y - 5);
		ctx.lineTo(10.5, y);
		ctx.lineTo(10.5, y - 10);
		ctx.closePath();
		ctx.fill();
		ctx.stroke();
	}
	if(to > trackLength) {
		ctx.fillStyle = "#ffffff";
		ctx.strokeStyle = "#000";
		ctx.beginPath();
		ctx.moveTo(trackLength - 0.5, y - 5);
		ctx.lineTo(trackLength - 5.5, y);
		ctx.lineTo(trackLength - 5.5, y - 10);
		ctx.closePath();
		ctx.fill();
		ctx.stroke();

		ctx.beginPath();
		ctx.moveTo(trackLength - 5.5, y - 5);
		ctx.lineTo(trackLength - 10.5, y);
		ctx.lineTo(trackLength - 10.5, y - 10);
		ctx.closePath();
		ctx.fill();
		ctx.stroke();
	}
}

function showGene(canvas1, canvas2, geneNode, mode) {
	var geneNodes = geneNode.getElementsByTagName(xmlTagElement);
	var elementFroms = [], elementTos = [], elementIds = [], elementDirections = [], elementColors = [];
	var elementRelativeFroms = [], elementRelativeTos = [], elementRelativeWidth;
	var geneNodeName = geneNode.getAttribute(xmlAttributeId);
	var subElements = [], subElementRelativeFroms = [], subElemnetRelativeTos = [], subElementTypes = [];
	var subElementWidth;
	var i, j, k, trackItemIndex;
	var localStorageJsonObj;
	var geneNodeName_show = geneNodeName;

	var colorStyle = "#000";
	if(geneNodeName == "refGene") {
		colorStyle = "#8B8B00";
	} else if(geneNodeName == "ensemblGene") {
		colorStyle = "RGB(109,115,243)";
	} else if(geneNodeName == "knownGene") {
		colorStyle = "RGB(163,17,90)";
	} else {
		colorStyle = "#000";
	}

	for( i = 0; i < trackItems.length; i++) {
		if(geneNodeName == trackItems[i].id) {
			trackItemIndex = i;
			break;
		}
	}
	
	if(((/^_/).test(geneNodeName))){
		geneNodeName_show = (geneNodeName + "").replace("_","");
	}
	
	if(trackItemIndex< trackItems.length){
		trackItems[trackItemIndex].details = [];
	}

	if(geneNode.getElementsByTagName(xmlTagSubElement).length == 0) {
		for( i = 0; i < geneNodes.length; i++) {
			elementFroms[i] = geneNodes[i].firstChild;
			elementTos[i] = geneNodes[i].childNodes[1];
			if(geneNodes[i].getElementsByTagName(xmlTagDirection).length == 0) {
				elementDirections[i] = '.';
			} else {
				elementDirections[i] = geneNodes[i].getElementsByTagName(xmlTagDirection)[0].firstChild.nodeValue;
			}
			if(geneNodes[i].getElementsByTagName(xmlTagColor).length > 0) {
				elementColors[i] = "RGB(" + geneNodes[i].getElementsByTagName(xmlTagColor)[0].firstChild.nodeValue + ")";
			} else if(geneNodes[i].getAttribute(xmlTagVariant)=="true"){
				elementColors[i] = "#FF0000";
			}else{
				elementColors[i] = colorStyle;
			}
			elementIds[i] = geneNodes[i].getAttribute(xmlAttributeId);
			elementRelativeFroms[i] = parseInt((parseInt(elementFroms[i].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
			elementRelativeTos[i] = parseInt((parseInt(elementTos[i].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);

			if(trackItemIndex< trackItems.length){
				trackItems[trackItemIndex].details[i] = [];
				trackItems[trackItemIndex].details[i].id = elementIds[i];
				trackItems[trackItemIndex].details[i].left = elementRelativeFroms[i];
				trackItems[trackItemIndex].details[i].right = elementRelativeTos[i];
				trackItems[trackItemIndex].details[i].from = elementFroms[i].childNodes[0].nodeValue;
				trackItems[trackItemIndex].details[i].to = elementTos[i].childNodes[0].nodeValue;
			}
		}
		if(canvas1.getContext && canvas2.getContext) {
			var ctx1 = canvas1.getContext('2d');
			var ctx2 = canvas2.getContext('2d');
			if(geneNodes.length == 0) {
				canvas1.height = 10;
				canvas1.style.height = 10;
				canvas2.height = 10;
				canvas2.style.height = 10;
				ctx1.strokeStyle = "#000";
				ctx1.fillStyle = "#000";
				ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);
			} else {
				if(mode == "dense") {
					canvas1.height = 10;
					canvas1.style.height = 10;
					canvas2.height = 10;
					canvas2.style.height = 10;
					ctx1.strokeStyle = "#000";
					ctx1.fillStyle = "#000";
					ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);

					for( i = 0; i < geneNodes.length; i++) {
						ctx2.fillStyle = elementColors[i];
						elementRelativeWidth = Math.abs(parseInt(elementRelativeTos[i] - elementRelativeFroms[i] + 1));
						if(elementDirections[i] == "." || elementRelativeWidth <= 5) {
							ctx2.fillRect(elementRelativeFroms[i], 0, elementRelativeWidth, 10);
						} else {
							if(elementDirections[i] == "+") {
								ctx2.fillRect(elementRelativeFroms[i], 0, elementRelativeWidth - 5, 10);
								ctx2.beginPath();
								ctx2.moveTo(elementRelativeTos[i] - 4, 0);
								ctx2.lineTo(elementRelativeTos[i], 5);
								ctx2.lineTo(elementRelativeTos[i] - 4, 10);
								ctx2.fill();
							} else {
								ctx2.fillRect(elementRelativeFroms[i] + 5, 0, elementRelativeWidth - 5, 10);
								ctx2.beginPath();
								ctx2.moveTo(elementRelativeFroms[i] + 5, 0);
								ctx2.lineTo(elementRelativeFroms[i], 5);
								ctx2.lineTo(elementRelativeFroms[i] + 5, 10);
								ctx2.fill();
							}
						}
						if(trackItemIndex< trackItems.length){
							trackItems[trackItemIndex].details[i].top = 0;
							trackItems[trackItemIndex].details[i].bottom = 10;
						}
					}
				} else {
					var packVariants = [], squishVariants = [];
					if((mode == "pack") && (geneNodes.length <= parseInt(trackLength / 50) * 50)) {//geneNodes.length <= parseInt(trackLength / 50   此判断是假定一个gene最小占位为50，则宽为trackLength，高为50的track可放的最多gene数目
						packVariants[packVariants.length] = [];
						packVariants[0][0] = 0;
						for( i = 1; i < geneNodes.length; i++) {
							for( j = 0; j < packVariants.length; j++) {
								if(elementIds[i] != ".") {
									if((elementRelativeFroms[i] - ctx1.measureText(elementIds[i]).width - 8) > elementRelativeTos[packVariants[j][packVariants[j].length - 1]]) {
										packVariants[j][packVariants[j].length] = i;
										break;
									}
								} else {
									if((elementRelativeFroms[i] - 5) > elementRelativeTos[packVariants[j][packVariants[j].length - 1]]) {
										packVariants[j][packVariants[j].length] = i;
										break;
									}
								}
							}
							if(j == packVariants.length) {
								packVariants[packVariants.length] = [];
								packVariants[j][0] = i;
							}
						}
						if(packVariants.length <= 50) {
							canvas1.height = 13 * packVariants.length - 3;
							canvas1.style.height = 13 * packVariants.length - 3;
							canvas2.height = 13 * packVariants.length - 3;
							canvas2.style.height = 13 * packVariants.length - 3;
							
							ctx1.strokeStyle = "#000";
							ctx1.fillStyle = "#000";
							ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);

							y = 10;
							for( i = 0; i < packVariants.length; i++) {
								for( j = 0; j < packVariants[i].length; j++) {
									if(trackItemIndex< trackItems.length){
										trackItems[trackItemIndex].details[packVariants[i][j]].left = elementRelativeFroms[packVariants[i][j]] - ctx2.measureText(elementIds[packVariants[i][j]]).width - 3;
										trackItems[trackItemIndex].details[packVariants[i][j]].top = y - 10;
										trackItems[trackItemIndex].details[packVariants[i][j]].bottom = y;
									}
									if(elementIds[i] != ".") {
										ctx2.fillStyle = elementColors[packVariants[i][j]];
										ctx2.strokeStyle = elementColors[packVariants[i][j]];
										ctx2.fillText(elementIds[packVariants[i][j]], elementRelativeFroms[packVariants[i][j]] - ctx2.measureText(elementIds[packVariants[i][j]]).width - 3, y - 2);
									}
									ctx2.fillStyle = elementColors[packVariants[i][j]];
									ctx2.strokeStyle = elementColors[packVariants[i][j]];
									elementRelativeWidth = Math.abs(parseInt(elementRelativeTos[packVariants[i][j]] - elementRelativeFroms[packVariants[i][j]] + 1));
									if(elementDirections[packVariants[i][j]] == "." || elementRelativeWidth <= 5) {
										ctx2.fillRect(elementRelativeFroms[packVariants[i][j]], y - 10, elementRelativeWidth, 10);
									} else {
										if(elementDirections[packVariants[i][j]] == "+") {
											ctx2.fillRect(elementRelativeFroms[packVariants[i][j]], y - 10, elementRelativeWidth - 5, 10);
											ctx2.beginPath();
											ctx2.moveTo(elementRelativeTos[packVariants[i][j]] - 4, y - 10);
											ctx2.lineTo(elementRelativeTos[packVariants[i][j]], y - 5);
											ctx2.lineTo(elementRelativeTos[packVariants[i][j]] - 4, y);
											ctx2.fill();
										} else {
											ctx2.fillRect(elementRelativeFroms[packVariants[i][j]] + 5, y - 10, elementRelativeWidth - 5, 10);
											ctx2.beginPath();
											ctx2.moveTo(elementRelativeFroms[packVariants[i][j]] + 5, y - 10);
											ctx2.lineTo(elementRelativeFroms[packVariants[i][j]], y - 5);
											ctx2.lineTo(elementRelativeFroms[packVariants[i][j]] + 5, y);
											ctx2.fill();
										}
									}
								}
								y = y + 13;
							}
							canvas2.addEventListener("mousemove", canvasMousemove, false);
							canvas2.addEventListener("click", canvasClickForRepeat, false);
						}
					}
					if(mode == "squish" || ((mode == "pack") && (packVariants.length > 50 || geneNodes.length > parseInt(trackLength / 50) * 50))) {
						squishVariants[squishVariants.length] = [];
						squishVariants[0][0] = 0;
						for( i = 1; i < geneNodes.length; i++) {
							for( j = 0; j < squishVariants.length; j++) {
								if(elementRelativeFroms[i] > elementRelativeTos[squishVariants[j][squishVariants[j].length - 1]]) {
									squishVariants[j][squishVariants[j].length] = i;
									break;
								}
							}
							if(j == squishVariants.length) {
								squishVariants[squishVariants.length] = [];
								squishVariants[j][0] = i;
							}
						}
						canvas1.height = 8 * squishVariants.length - 3;
						canvas1.style.height = 8 * squishVariants.length - 3;
						canvas2.height = 8 * squishVariants.length - 3;
						canvas2.style.height = 8 * squishVariants.length - 3;
						
						ctx1.strokeStyle = "#000";
						ctx1.fillStyle = "#000";
						ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);
						
						y = 0;
						for( i = 0; i < squishVariants.length; i++) {
							for( j = 0; j < squishVariants[i].length; j++) {
								ctx2.fillStyle = elementColors[squishVariants[i][j]];

								if(trackItemIndex< trackItems.length){
									trackItems[trackItemIndex].details[squishVariants[i][j]].top = y;
									trackItems[trackItemIndex].details[squishVariants[i][j]].bottom = y + 5;
								}
								elementRelativeWidth = Math.abs(parseInt(elementRelativeTos[squishVariants[i][j]] - elementRelativeFroms[squishVariants[i][j]] + 1));
								if(elementDirections[squishVariants[i][j]] == "." || elementRelativeWidth <= 3) {
									ctx2.fillRect(elementRelativeFroms[squishVariants[i][j]], y, elementRelativeWidth, 5);
								} else {
									if(elementDirections[squishVariants[i][j]] == "+") {
										ctx2.fillRect(elementRelativeFroms[squishVariants[i][j]], y, elementRelativeWidth - 3, 5);
										ctx2.beginPath();
										ctx2.moveTo(elementRelativeTos[squishVariants[i][j]] - 2, y);
										ctx2.lineTo(elementRelativeTos[squishVariants[i][j]], y + 2);
										ctx2.lineTo(elementRelativeTos[squishVariants[i][j]] - 2, y + 5);
										ctx2.fill();
									} else {
										ctx2.fillRect(elementRelativeFroms[squishVariants[i][j]] + 3, y, elementRelativeWidth - 3, 5);
										ctx2.beginPath();
										ctx2.moveTo(elementRelativeFroms[squishVariants[i][j]] + 3, y);
										ctx2.lineTo(elementRelativeFroms[squishVariants[i][j]], y + 2);
										ctx2.lineTo(elementRelativeFroms[squishVariants[i][j]] + 3, y + 5);
										ctx2.fill();
									}
								}
							}
							y = y + 8;
						}
					}
				}
			}
		}
	} else {
		for( i = 0; i < geneNodes.length; i++) {
			elementFroms[i] = geneNodes[i].firstChild;
			elementTos[i] = geneNodes[i].childNodes[1];
			elementDirections[i] = elementTos[i].nextSibling.childNodes[0].nodeValue;
			if(geneNodes[i].getElementsByTagName(xmlTagColor).length > 0) {
				elementColors[i] = "RGB(" + geneNodes[i].getElementsByTagName(xmlTagColor)[0].firstChild.nodeValue + ")";
			} else {
				elementColors[i] = colorStyle;
			}
			elementIds[i] = geneNodes[i].getAttribute(xmlAttributeId);
			elementRelativeFroms[i] = parseInt((parseInt(elementFroms[i].childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
			elementRelativeTos[i] = parseInt((parseInt(elementTos[i].childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);

			subElements[i] = geneNodes[i].getElementsByTagName(xmlTagSubElement);
			subElementRelativeFroms[i] = [];
			subElemnetRelativeTos[i] = [];
			subElementTypes[i] = [];

			if(geneNodes[i].getElementsByTagName(xmlTagSubElement).length == 0) {
				subElements[i] = [];
				subElements[i][0] = " ";
				//virtual subElement Node
				subElementTypes[i][0] = subElementTypeBoxValue;
				subElementRelativeFroms[i][0] = elementRelativeFroms[i];
				subElemnetRelativeTos[i][0] = elementRelativeTos[i];
			} else {
				for( j = 0; j < subElements[i].length; j++) {
					subElementTypes[i][j] = subElements[i][j].getAttribute(xmlAttributeType);
					subElementRelativeFroms[i][j] = parseInt((parseInt(subElements[i][j].firstChild.childNodes[0].nodeValue) - startIndex) / searchLength * trackLength);
					subElemnetRelativeTos[i][j] = parseInt((parseInt(subElements[i][j].lastChild.childNodes[0].nodeValue) - startIndex + 1) / searchLength * trackLength);
				}
			}

			if(trackItemIndex< trackItems.length){
				trackItems[trackItemIndex].details[i] = [];
				trackItems[trackItemIndex].details[i].id = elementIds[i];
				trackItems[trackItemIndex].details[i].left = elementRelativeFroms[i];
				trackItems[trackItemIndex].details[i].right = elementRelativeTos[i];
				trackItems[trackItemIndex].details[i].from = elementFroms[i].childNodes[0].nodeValue;
				trackItems[trackItemIndex].details[i].to = elementTos[i].childNodes[0].nodeValue;
			}
		}

		if(canvas1.getContext && canvas2.getContext) {
			var ctx1 = canvas1.getContext('2d');
			var ctx2 = canvas2.getContext('2d');
			if(geneNodes.length == 0) {
				canvas1.height = 10;
				canvas1.style.height = 10;
				canvas2.height = 10;
				canvas2.style.height = 10;
				ctx1.strokeStyle = "#000";
				ctx1.fillStyle = "#000";
				ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);
			} else {
				if(mode == "dense") {
					canvas1.height = 10;
					canvas1.style.height = 10;
					canvas2.height = 10;
					canvas2.style.height = 10;
					ctx1.strokeStyle = "#000";
					ctx1.fillStyle = "#000";
					ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);

					for( i = 0; i < subElements.length; i++) {
						ctx2.fillStyle = elementColors[i];
						///
						for( j = 0; j < subElements[i].length; j++) {
							subElementWidth = Math.abs(subElemnetRelativeTos[i][j] - subElementRelativeFroms[i][j] + 1);
							if(subElementTypes[i][j] == subElementTypeBoxValue) {
								ctx2.fillRect(subElementRelativeFroms[i][j], 0, subElementWidth, 10);
							} else if(subElementTypes[i][j] == subElementTypeLineValue) {
								ctx2.fillRect(subElementRelativeFroms[i][j], 5, subElementWidth, 1);
							} else {
								ctx2.fillRect(subElementRelativeFroms[i][j], 3, subElementWidth, 5);
							}
						}
					}
					for( i = 0; i < geneNodes.length; i++) {
						trackItems[trackItemIndex].details[i].top = 0;
						trackItems[trackItemIndex].details[i].bottom = 10;

					}
				} else {
					var packVariants = [], squishVariants = [];
					if((mode == "pack") && (geneNodes.length <= parseInt(trackLength / 50) * 50)) {//geneNodes.length <= parseInt(trackLength / 50   此判断是假定一个gene最小占位为50，则宽为trackLength，高为50的track可放的最多gene数目
						packVariants[packVariants.length] = [];
						packVariants[0][0] = 0;
						for( i = 1; i < geneNodes.length; i++) {
							for( j = 0; j < packVariants.length; j++) {
								if(elementIds[i] != ".") {
									if((elementRelativeFroms[i] - ctx1.measureText(elementIds[i]).width - 8) > elementRelativeTos[packVariants[j][packVariants[j].length - 1]]) {
										packVariants[j][packVariants[j].length] = i;
										break;
									}
								} else {
									if((elementRelativeFroms[i] - 5) > elementRelativeTos[packVariants[j][packVariants[j].length - 1]]) {
										packVariants[j][packVariants[j].length] = i;
										break;
									}
								}
							}
							if(j == packVariants.length) {
								packVariants[packVariants.length] = [];
								packVariants[j][0] = i;
							}
						}
						if(packVariants.length <= 50) {
							canvas1.height = 13 * packVariants.length - 3;
							canvas1.style.height = 13 * packVariants.length - 3;
							canvas2.height = 13 * packVariants.length - 3;
							canvas2.style.height = 13 * packVariants.length - 3;
							
							ctx1.strokeStyle = "#000";
							ctx1.fillStyle = "#000";
							ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);

							y = 10;
							for( i = 0; i < packVariants.length; i++) {
								for( j = 0; j < packVariants[i].length; j++) {
									if(trackItemIndex< trackItems.length){
										trackItems[trackItemIndex].details[packVariants[i][j]].left = elementRelativeFroms[packVariants[i][j]] - ctx2.measureText(elementIds[packVariants[i][j]]).width - 3;
										trackItems[trackItemIndex].details[packVariants[i][j]].top = y - 10;
										trackItems[trackItemIndex].details[packVariants[i][j]].bottom = y;
									}
									if(elementIds[i] != ".") {
										ctx2.fillStyle = elementColors[packVariants[i][j]];
										ctx2.strokeStyle = elementColors[packVariants[i][j]];
										ctx2.fillText(elementIds[packVariants[i][j]], elementRelativeFroms[packVariants[i][j]] - ctx2.measureText(elementIds[packVariants[i][j]]).width - 3, y - 2);
									}
									ctx2.fillStyle = elementColors[packVariants[i][j]];
									//
									ctx2.strokeStyle = elementColors[packVariants[i][j]];

									if(geneNodes[packVariants[i][j]].getElementsByTagName(xmlTagSubElement).length == 0) {
										elementRelativeWidth = elementRelativeTos[packVariants[i][j]] - elementRelativeFroms[packVariants[i][j]] + 1;
										if(elementRelativeWidth <= 5){
											ctx2.fillRect(elementRelativeFroms[packVariants[i][j]], y - 10, elementRelativeWidth, 10);
										}else{
											if(elementDirections[packVariants[i][j]] == "+") {
												ctx2.fillRect(elementRelativeFroms[packVariants[i][j]], y - 10, elementRelativeWidth - 5, 10);
												ctx2.beginPath();
												ctx2.moveTo(elementRelativeTos[packVariants[i][j]] - 4, y - 10);
												ctx2.lineTo(elementRelativeTos[packVariants[i][j]], y - 5);
												ctx2.lineTo(elementRelativeTos[packVariants[i][j]] - 4, y);
												ctx2.fill();
											} else {
												ctx2.fillRect(elementRelativeFroms[packVariants[i][j]] + 5, y - 10, elementRelativeWidth - 5, 10);
												ctx2.beginPath();
												ctx2.moveTo(elementRelativeFroms[packVariants[i][j]] + 5, y - 10);
												ctx2.lineTo(elementRelativeFroms[packVariants[i][j]], y - 5);
												ctx2.lineTo(elementRelativeFroms[packVariants[i][j]] + 5, y);
												ctx2.fill();
											}
										}
										
									} else {
										for( k = 0; k < subElements[packVariants[i][j]].length; k++) {
											subElementWidth = Math.abs(subElemnetRelativeTos[packVariants[i][j]][k] - subElementRelativeFroms[packVariants[i][j]][k] + 1);
											//save time and memory by Liran
											if(subElemnetRelativeTos[packVariants[i][j]][k]<0 || subElementRelativeFroms[packVariants[i][j]][k]>trackLength){
												continue;
											}
											//save time and memory by Liran
											if(subElementTypes[packVariants[i][j]][k] == subElementTypeBoxValue) {
												ctx2.fillRect(subElementRelativeFroms[packVariants[i][j]][k], y - 10, subElementWidth, 10);
											} else if(subElementTypes[packVariants[i][j]][k] == subElementTypeLineValue) {
												ctx2.fillRect(subElementRelativeFroms[packVariants[i][j]][k], y - 5, subElementWidth, 1);
												ctx2.strokeStyle = elementColors[packVariants[i][j]];
												var directionIndex;
												if(elementDirections[packVariants[i][j]] == "+") {
												//	directionIndex = subElemnetRelativeTos[packVariants[i][j]][k];
												//	while(directionIndex - 2 > subElementRelativeFroms[packVariants[i][j]][k]) {
														//save time and memory by Liran
													directionIndex = subElemnetRelativeTos[packVariants[i][j]][k]<(trackLength+3)?subElemnetRelativeTos[packVariants[i][j]][k]:(trackLength+3);
													while(directionIndex - 2 > (subElementRelativeFroms[packVariants[i][j]][k]>-3?subElementRelativeFroms[packVariants[i][j]][k]:-3)) {
														//save time and memory by Liran
														ctx2.beginPath();
														ctx2.moveTo(directionIndex, y - 5);
														ctx2.lineTo(directionIndex - 2, y - 7);
														ctx2.closePath();
														ctx2.stroke();

														ctx2.beginPath();
														ctx2.moveTo(directionIndex + 1, y - 5);
														ctx2.lineTo(directionIndex - 2, y - 2);
														ctx2.closePath();
														ctx2.stroke();
														directionIndex = directionIndex - 5;
													}
												} else {
												//	directionIndex = subElementRelativeFroms[packVariants[i][j]][k];
												//	while(directionIndex + 2 < subElemnetRelativeTos[packVariants[i][j]][k]) {
														//save time and memory by Liran
													directionIndex = subElementRelativeFroms[packVariants[i][j]][k]>-3?subElementRelativeFroms[packVariants[i][j]][k]:-3;
													while(directionIndex + 2 < (subElemnetRelativeTos[packVariants[i][j]][k]<(trackLength+3)?subElemnetRelativeTos[packVariants[i][j]][k]:(trackLength+3))) {
														//save time and memory by Liran
														ctx2.beginPath();
														ctx2.moveTo(directionIndex, y - 5);
														ctx2.lineTo(directionIndex + 2, y - 7);
														ctx2.closePath();
														ctx2.stroke();

														ctx2.beginPath();
														ctx2.moveTo(directionIndex - 1, y - 5);
														ctx2.lineTo(directionIndex + 2, y - 2);
														ctx2.closePath();
														ctx2.stroke();
														directionIndex = directionIndex + 5;
													}
												}
											} else {
												ctx2.fillRect(subElementRelativeFroms[packVariants[i][j]][k], y - 7, subElementWidth, 5);
											}
										}
									}
								}
								y = y + 13;
							}
							canvas2.addEventListener("mousemove", canvasMousemove, false);
							canvas2.addEventListener("click", canvasClick, false);
						}
					}
					if(mode == "squish" || ((mode == "pack") && (packVariants.length > 50 || geneNodes.length > parseInt(trackLength / 50) * 50))) {
						squishVariants[squishVariants.length] = [];
						squishVariants[0][0] = 0;
						for( i = 1; i < geneNodes.length; i++) {
							for( j = 0; j < squishVariants.length; j++) {
								if(elementRelativeFroms[i] > elementRelativeTos[squishVariants[j][squishVariants[j].length - 1]]) {
									squishVariants[j][squishVariants[j].length] = i;
									break;
								}
							}
							if(j == squishVariants.length) {
								squishVariants[squishVariants.length] = [];
								squishVariants[j][0] = i;
							}
						}
						canvas1.height = 8 * squishVariants.length - 3;
						canvas1.style.height = 8 * squishVariants.length - 3;
						canvas2.height = 8 * squishVariants.length - 3;
						canvas2.style.height = 8 * squishVariants.length - 3;
						
						ctx1.strokeStyle = "#000";
						ctx1.fillStyle = "#000";
						ctx1.fillText(geneNodeName_show, canvas1.width - ctx1.measureText(geneNodeName_show).width, 8);

						y = 0;
						for( i = 0; i < squishVariants.length; i++) {
							for( j = 0; j < squishVariants[i].length; j++) {
								ctx2.fillStyle = elementColors[squishVariants[i][j]];
								if(trackItemIndex< trackItems.length){
									trackItems[trackItemIndex].details[squishVariants[i][j]].top = y;
									trackItems[trackItemIndex].details[squishVariants[i][j]].bottom = y + 5;
								}
								for( k = 0; k < subElements[squishVariants[i][j]].length; k++) {
									subElementWidth = Math.abs(subElemnetRelativeTos[squishVariants[i][j]][k] - subElementRelativeFroms[squishVariants[i][j]][k] + 1);
									if(subElementTypes[squishVariants[i][j]][k] == subElementTypeLineValue) {
										ctx2.fillRect(subElementRelativeFroms[squishVariants[i][j]][k], y + 2, subElementWidth, 1);
									} else {
										ctx2.fillRect(subElementRelativeFroms[squishVariants[i][j]][k], y, subElementWidth, 5);
									}
								}
							}
							y = y + 8;
						}
					}
				}
			}
		}
	}
}

function QueryString() {
	var name, value, i;
	var str = window.location.href;
	var num = str.indexOf("?")
	str = str.substr(num + 1);
	var arrtmp = str.split("&");
	for( i = 0; i < arrtmp.length; i++) {
		num = arrtmp[i].indexOf("=");
		if(num > 0) {
			name = arrtmp[i].substring(0, num);
			value = arrtmp[i].substr(num + 1);
			this[name] = value;
		}
	}
	return this;
}

function setAssemblyRequest() {
	var assemblySelect = document.getElementById("assemblySelect");
	var assemblyIndex = assemblySelect.selectedIndex;
	assemblyNum = assemblySelect.options[assemblyIndex].value;
	var url = "servlet/test.do?action=setAssembly&assembly=";
	url = url + assemblyNum;

	XMLHttpReq.onreadystatechange = null;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}

function removeAnnotations() {
	var tableNode = document.getElementById("trackListTable");
	var trNodeNum = tableNode.getElementsByTagName("tr").length;
	while(trNodeNum > 2) {
		tableNode.deleteRow(-1);
		trNodeNum--;
	}
}
function getChrLengthRequest() {
	var url = "servlet/test.do?action=getChromosomes";
	XMLHttpReq.onreadystatechange = handleGetChrLengthStateChange;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}
function handleGetChrLengthStateChange(){
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			var XMLDoc = XMLHttpReq.responseXML;
			var text = XMLHttpReq.responseText;
			var chrLengthList = getNodeText(XMLDoc.getElementsByTagName("ChromosomeList")[0]).split(",");
			var i, chrNum_temp, chrLength_temp;
			for(i =0; i< chrLengthList.length;i++){
				chrNum_temp = chrLengthList[i].split(":")[0] + "";
				chrLength_temp = chrLengthList[i].split(":")[1] + "";
				chr_Lengths[chrNum_temp] = chrLength_temp;
			}
			chrLength = chr_Lengths[chrNum];
			setSliderMax();
			var url = "./browser.html";
			if(start_user < 1 || end_user > parseInt(chr_Lengths[chrNum])) {
				if(start_user < 1) {
					start_user = 1;
				}
				if(end_user > parseInt(chr_Lengths[chrNum])) {
					end_user = parseInt(chr_Lengths[chrNum]);
				}
				searchLength_user = end_user - start_user + 1;
				if(searchLength_user< trackLength_user/10){
					if(start_user ==1){
						end_user = trackLength_user/10;
					}else{
						start_user = end_user - trackLength_user/10 + 1;
					}
				}
				searchLength_user = end_user - start_user + 1;
				startIndex = start_user - searchLength_user;
				endIndex = end_user + searchLength_user;
//				url = url + assemblyNum;
				url = url + "?Chr=";
				url = url + chrNum;
				url = url + "&Start=";
				url = url + startIndex;
				url = url + "&End=";
				url = url + endIndex;
//				url = url + "&width=";
//				url = url + "2850";
				window.location.href = url;
				return;
			}
			showuserSearchIndex(start_user, end_user);
			getAnnotationsRequest();
		}
	}
}

function getAnnotationsRequest() {
	var url = "servlet/test.do?action=getAnnotations";
	XMLHttpReq.onreadystatechange = handleGetgetAnnotationsStateChange;
	XMLHttpReq.open("GET", url, false);
	XMLHttpReq.send(null);
}

//use regular expression to trim the blank and '\' in the string
function allTrim(ui) {
	var notValid = /\s|\x2F/;
	while(notValid.test(ui)) {
		ui = ui.replace(notValid, "");
	}
	return ui;
}

function handleGetgetAnnotationsStateChange() {
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			var XMLDoc = XMLHttpReq.responseXML;
			var tracks = XMLDoc.getElementsByTagName(xmlTagAnnotationList)[0].childNodes[0].nodeValue;
			var trackList = tracks.split(/,/);
			var trackId;
			var h_idx = 0, pclns_idx = 0;
			for(var i = 0; i < trackList.length; i++) {
				if(trackList[i].split(/:/)[0] == "Pvar"){
					var temp_id_superid = trackList[i].split(/:/)[1];
					personalPannel.Pvar.id = temp_id_superid.split(/@/)[0];
					initPvar_superid = temp_id_superid.split(/@/)[1];
					//personalPannel.Pvar.id = trackList[i].split(/:/)[1];
					personalPannel.Pvar.mode = trackList[i].split(/:/)[2];
				}else if(trackList[i].split(/:/)[0] == "Pfanno"){
					personalPannel.Pfanno.id = trackList[i].split(/:/)[1];
					personalPannel.Pfanno.mode = trackList[i].split(/:/)[2];
				}else if(trackList[i].split(/:/)[0] == "Panno"){
					personalPannel.Panno.id = trackList[i].split(/:/)[1];
					personalPannel.Panno.mode = trackList[i].split(/:/)[2];
				}else if(trackList[i].split(/:/)[0] == "Pclns"){
					personalPannel.Pclns[pclns_idx] = [];
					personalPannel.Pclns[pclns_idx].id = trackList[i].split(/:/)[1];
					personalPannel.Pclns[pclns_idx].mode = trackList[i].split(/:/)[2];
					pclns_idx++;
				}else{
					trackItems[h_idx] = [];
					trackItems[h_idx].group = trackList[i].split(/:/)[0];
					trackItems[h_idx].id = trackList[i].split(/:/)[1];
					trackItems[h_idx].mode = trackList[i].split(/:/)[2];
					trackItems[h_idx].dataType = trackList[i].split(/:/)[3];
					trackItems[h_idx].isServer = 1;
					trackItems[h_idx].details = [];
				
					if(trackItems[h_idx].dataType == "VCF" || trackItems[h_idx].dataType == "GVF") {
						personalPannel.personalTrackItems.Pvars.push("_" + trackItems[h_idx].id);
					}
			//		if(trackItems[h_idx].group == "Disease"){
					if(trackItems[h_idx].dataType == "GDF"){
						personalPannel.personalTrackItems.Pclnss.push("_" + trackItems[h_idx].id);
					}
			//		if(trackItems[h_idx].group == "Regulation"){
					if(trackItems[h_idx].dataType == "GRF"){
						personalPannel.personalTrackItems.Pfannos.push("_" + trackItems[h_idx].id);
					}
			//		if(trackItems[h_idx].group == "Gene"){
					if(trackItems[h_idx].dataType == "ANNO"){
						personalPannel.personalTrackItems.Pannos.push("_" + trackItems[h_idx].id);
					}		
					if(trackItems[h_idx].mode != "hide") {
						createTrack(trackItems[h_idx].id,trackItems[h_idx].mode);
					}
					h_idx++;
				}
			}
			//personal pannel initialize: add tr&canvas
			if(personalPannel.Pvar.id){
				createPPGTrack(personalPannel.Pvar.id, personalPannel.Pvar.mode);
				
				if(personalPannel.Pfanno.id){
					createPPOtherTrack(personalPannel.Pfanno.id, personalPannel.Pfanno.mode);
				}
				if(personalPannel.Panno.id){
					createPPOtherTrack(personalPannel.Panno.id, personalPannel.Panno.mode);
				}
				for(i = 0; i < personalPannel.Pclns.length; i++){
					createPPOtherTrack(personalPannel.Pclns[i].id, personalPannel.Pclns[i].mode);
				}
				
				var PP_top = (document.body.clientHeight - $("#personalPannel").height())/2;
				var PP_left = $("#divTrack").position().left;
				$(document.getElementById("personalPannel")).css("display", "block");
				$(document.getElementById("personalPannel")).css("top", PP_top);
				$(document.getElementById("personalPannel")).css("left", PP_left);
				
				$(document.getElementById("ppCloseBtn")).unbind();
				$(document.getElementById("ppMin")).unbind();
				$(document.getElementById("ppSet")).unbind();
				$(document.getElementById("ppCloseBtn")).click(removePvar);
				$(document.getElementById("ppMin")).click(miniPersonalPannel);
				$(document.getElementById("ppSet")).click(setPersonalPannel);
			}
			//custom track initialize
			if($.cookie("customTrackList")) {
				var customTrackList = $.cookie("customTrackList").split(",");
				var j = trackItems.length, k;
				
				for( k = 0; k < customTrackList.length; k++, j++) {
					trackItems[j] = [];
					trackItems[j].id = customTrackList[k].split(/:/)[0];
					trackItems[j].mode = customTrackList[k].split(/:/)[1];
					trackItems[j].dataType = customTrackList[k].split(/:/)[2];
					trackItems[j].isServer = 0;
					
					if(trackItems[j].dataType == "VCF" || trackItems[j].dataType == "GVF") {
						personalPannel.personalTrackItems.Pvars.push("_" + trackItems[j].id);
					}
					
					/* add by Liran to enable customized tracks appear in personal track setting*/
					if(trackItems[j].dataType == "GDF"){
						personalPannel.personalTrackItems.Pclnss.push("_" + trackItems[j].id);
					}
					if(trackItems[j].dataType == "GRF"){
						personalPannel.personalTrackItems.Pfannos.push("_" + trackItems[j].id);
					}
					if(trackItems[j].dataType == "ANNO"){
						personalPannel.personalTrackItems.Pannos.push("_" + trackItems[j].id);
					}		
					/* add by Liran to enable customized tracks appear in personal track setting*/
					if(trackItems[j].mode != "hide") {
						createTrack(trackItems[j].id,trackItems[j].mode);
					}
				}
			}
			showRef();
		}
	}
}
var deleteTrackArray;
function removeTrack2(trackId) {
	return function() {
		var trackItemIndex;
		var i;

		for( i = 0; i < trackItems.length; i++) {
			if(trackItems[i].id == trackId) {
				trackItemIndex = i;
				break;
			}
		}

		if(trackItems[trackItemIndex].superid) {
			removeTrack(trackId);
			
			deleteTrackArray = trackItems.splice(trackItemIndex, 1);
			
			//删除personalPannel.personalTrackItems.Pvars中的的个人基因组记录
			for(var Pvars_i = 0; Pvars_i < personalPannel.personalTrackItems.Pvars.length; Pvars_i++) {
				if(personalPannel.personalTrackItems.Pvars[Pvars_i] == ("_" + deleteTrackArray[0].id)) {
					personalPannel.personalTrackItems.Pvars.splice(Pvars_i, 1);
					break;
				}
			}

			removesubtrackGetparamsRequest(deleteTrackArray[0].superid);

			//trackItems.splice(trackItemIndex,1);

			/*for(i=0 ; i<trackItems.length; i++){
			 if(trackItems[i].superid == deleteTrackArray[0].superid){
			 break;
			 }
			 }
			 if(i >= trackItems.length){
			 var trackItemObj=[];
			 trackItemObj.id = deleteTrackArray[0].superid;
			 trackItemObj.mode = "hide";//先设置为hide，随后在trackModeOnchange中修改
			 trackItemObj.dataType = deleteTrackArray[0].dataType;
			 trackItemObj.group = deleteTrackArray[0].group;
			 trackItemObj.isServer = deleteTrackArray[0].isServer;
			 trackItemObj.details = [];
			 trackItems.push(trackItemObj);

			 //创建super track，并且向后端发送请求获取这条数据进行展示
			 trackModeOnchange(trackItemObj.id,deleteTrackArray[0].mode);
			 }*/
		} else {
			var mode = "hide";
			
			/*
			//为了区别与track table中的tr的id，所以在原有的track的id之上加上“select”来做select元素的id
			var trackSlelectObj = document.getElementById(trackId + "select");

			for( i = 0; i < trackSlelectObj.options.length; i++) {
				if(trackSlelectObj.options[i].text == mode) {
					trackSlelectObj.options[i].selected = true;
					break;
				}
			}*/
			trackModeOnchange(trackId, mode);
		}
	};
}

function removesubtrackGetparamsRequest(superid) {
	var url = "servlet/test.do?" + "action=getParams&tracks=" + superid;
	XMLHttpReq.onreadystatechange = handleRemoveSubtrackGetparamsRequest;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}

function handleRemoveSubtrackGetparamsRequest() {
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			var XMLDoc = XMLHttpReq.responseXML;
			var paramNodes = XMLDoc.getElementsByTagName(xmlTagParameter);
			var params = "", values = "", tempvalues = "";
			for(var i = 0; i < paramNodes.length; i++) {
				if(paramNodes[i].getAttribute(xmlAttributeType) == "STRING") {
					if(paramNodes[i].childNodes[0].nodeValue) {
						params = params + paramNodes[i].getAttribute("id") + ";";
						values = values + paramNodes[i].childNodes[0].nodeValue + ";";
					}
				} else if(paramNodes[i].getAttribute(xmlAttributeType) == "CHECKBOX") {
					var options = getNodeText(paramNodes[i].getElementsByTagName(xmlTagOptions)[0]).split(";");
					var option;
					tempvalues = "";
					for(var j = 0; j < options.length; j++) {
						option = options[j].split(':');
						if(option[1] == "1") {
							tempvalues = tempvalues + option[0] + ":";
						}
					}
					if(tempvalues != "") {
						tempvalues = tempvalues.substr(0, tempvalues.length - 1);
					} else {
						tempvalues = "0";
					}
					params = params + paramNodes[i].getAttribute("id") + ";";
					values = values + tempvalues + ";";
				} else if(paramNodes[i].getAttribute(xmlAttributeType) == "VCFSAMPLE") {
					var options = getNodeText(paramNodes[i].getElementsByTagName(xmlTagOptions)[0]).split(";");
					var option;
					tempvalues = "";
					for(var j = 0; j < options.length; j++) {
						option = options[j].split(':');
						if(option[1] == "1") {
							tempvalues = tempvalues + option[0] + ":";
						}
					}
					if(tempvalues != "") {
						tempvalues = tempvalues.substr(0, tempvalues.length - 1);
					} else {
						tempvalues = "0";
					}
					params = params + paramNodes[i].getAttribute("id") + ";";
					values = values + tempvalues + ";";
				}
			}
			params = params.substr(0, params.length - 1);
			values = values.substr(0, values.length - 1);
			values = values.replace(deleteTrackArray[0].id, "0");

			var url = "servlet/test.do?" + "action=setParams&tracks=" + deleteTrackArray[0].superid + "&modes=" + deleteTrackArray[0].mode + "&params=" + params + "&values=" + values;

			XMLHttpReq2.onreadystatechange = function() {
				//do nothing
				if(XMLHttpReq2.readyState == 4) {
					if(XMLHttpReq2.status == 200) {
						var ii;
						for( ii = 0; ii < trackItems.length; ii++) {
							if(trackItems[ii].superid == deleteTrackArray[0].superid) {
								break;
							}
						}
						if(ii >= trackItems.length) {
							trackItems[trackItems.length] = [];
							trackItems[trackItems.length - 1].id = deleteTrackArray[0].superid;
							trackItems[trackItems.length - 1].mode = "hide";
							trackItems[trackItems.length - 1].dataType = deleteTrackArray[0].dataType;
							trackItems[trackItems.length - 1].group = deleteTrackArray[0].group;
							trackItems[trackItems.length - 1].isServer = deleteTrackArray[0].isServer;
							trackItems[trackItems.length - 1].details = [];
							
							/*var trackItemObj = [];
							trackItemObj.id = deleteTrackArray[0].superid;
							trackItemObj.mode = "hide";
							//先设置为hide，随后在trackModeOnchange中修改
							trackItemObj.dataType = deleteTrackArray[0].dataType;
							trackItemObj.group = deleteTrackArray[0].group;
							trackItemObj.isServer = deleteTrackArray[0].isServer;
							trackItemObj.details = [];
							trackItems[trackItems.length] = trackItemObj;*/
							//trackItems.push(trackItemObj);

							//创建super track，并且向后端发送请求获取这条数据进行展示
							trackModeOnchange(deleteTrackArray[0].superid, deleteTrackArray[0].mode);

							//将super track的id加入个人基因组数组Pvars中
							personalPannel.personalTrackItems.Pvars.push("_" + deleteTrackArray[0].superid);
						}
						trackItems_setting2();
					}
				}
			};
			XMLHttpReq2.open("GET", url, true);
			XMLHttpReq2.send(null);

		}
	}
}

var current_superid_mode;
var current_superid;
function trackModeOnchange_superid(trackSuperid, mode){
	var i;
	var old_mode_class,new_mode_class;
	if(mode == "dense"){
		old_mode_class = "packmode";
		new_mode_class = "densemode";
	}else{
		old_mode_class = "densemode";
		new_mode_class = "packmode";
	}
	for(i=0; i< trackItems.length;i++){
		if(trackItems[i].superid){
			if(trackItems[i].superid == trackSuperid){
				trackItems[i].mode = mode;
				document.getElementById(trackItems[i].id).getElementsByClassName(old_mode_class)[0].className = new_mode_class;
			}
		}
	}
	current_superid_mode = mode;
	current_superid = trackSuperid;
	url = "servlet/test.do?action=modiTracks&tracks=" + trackSuperid + "&modes=" + mode;
	XMLHttpReq.onreadystatechange = handle_trackModeOnchange_superid;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}

function handle_trackModeOnchange_superid(){
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			var XMLDoc = XMLHttpReq.responseXML;
			var variantNodes = XMLDoc.getElementsByTagName(xmlTagVariants);
			var i, j, variantNode_temp, canvasNodes;
			if(variantNodes.length == 0){
				for(i =0; i < trackItems.length;i++){
					if(trackItems[i].superid && trackItems[i].superid == current_superid){
						overScaleShow(trackItems[i].id);
					}
				}
			}else{
				for(i = 0; i < trackItems.length; i++){
					if(trackItems[i].superid && trackItems[i].superid == current_superid){
						variantNode_temp = null;
						for(j = 0; j < variantNodes.length; j++){
							if(variantNodes[j].getAttribute(xmlAttributeId) == trackItems[i].id){
								variantNode_temp = variantNodes[j];
								break;
							}
						}
						if(variantNode_temp){
							canvasNodes = document.getElementById(trackItems[i].id).getElementsByTagName("canvas");
							showVariant(canvasNodes[0], canvasNodes[1], variantNode_temp, trackItems[i].mode);
						}else{
							overScaleShow(trackItems[i].id);
						}
					}
				}
			}
		}
	}
}

//dense转pack，pack转dense
function changeTrackModeByBtn(trackId){
	return function(){
		var i;
		var trackMode;
		var trNode = document.getElementById(trackId);
		var modechangeBtnSpanObj;
		for(i=0;i<trackItems.length;i++){
			if(trackId == trackItems[i].id){
				trackMode = trackItems[i].mode;
				break;
			}
		}
		if(trackItems[i].superid){
			if(trackMode == "dense"){
				trackModeOnchange_superid(trackItems[i].superid, "pack");
			}else{
				trackModeOnchange_superid(trackItems[i].superid, "dense");
			}
		}else{
			if(trackMode=="dense"){
				trackModeOnchange(trackId,"pack");
				modechangeBtnSpanObj = trNode.getElementsByClassName("densemode")[0];
				modechangeBtnSpanObj.className = "packmode";
			}else{
				trackModeOnchange(trackId,"dense");
				modechangeBtnSpanObj = trNode.getElementsByClassName("packmode")[0];
				modechangeBtnSpanObj.className = "densemode";
			}
		}
	}
}

function createTrack(trackId, mode) {
	var trackTable = document.getElementById("tableTrack");
	var trNode = trackTable.insertRow(-1);
	trNode.id = trackId;
	var modechangeBtnSpan_str = "";
	if(mode){
		if(mode=="pack"){
			modechangeBtnSpan_str = "<span class=\"packmode\"></span>";
		}else if(mode == "dense"){
			modechangeBtnSpan_str = "<span class=\"densemode\"></span>";
		}else{
			modechangeBtnSpan_str = "";
		}
	}
	//<span class=\"setting thickbox\" title=\"setting\" alt=\"#TB_inline?height=300;width=650;inlineId=tracksettingDIV\"></span>
	trNode.innerHTML = "<td class=\"trackOperator\"><span class=\"close\"></span>"+ modechangeBtnSpan_str +"</td><td class=\"trackName\"><canvas width=\"100\" height=\"50\" style=\"background: #ffffff\"></canvas></td><td class=\"trackContent\"><canvas width=\""+ trackLength +"\" height=\"50\" class=\"canvasTrackcontent\" title=\"shift+click and drag to zoom in\"></canvas></td>";
	var canvasNodes = trNode.getElementsByTagName("canvas");
	canvasNodes[0].onmouseover = mouseOver;
	canvasNodes[0].onmouseout = mouseOut;
	canvasNodes[0].onmousedown = mouseDown;

	canvasNodes[1].onmousedown = mouseDownRightCanvas;
	
	addMousewheelEvent(canvasNodes[1], mousewheelHandler);
	
	$(canvasNodes[1]).draggable({ 
		axis: "x" ,
		cursor: "url(./image/Grabber.cur),auto" ,
		drag : function(event, ui) {
			$(".canvasTrackcontent").each(function(arrayindex, arrayele) {
				$(arrayele).css("left", ui.position.left);
			});
			dragDragHandler(ui.position.left);
		},
		stop : function(event, ui){
			dragStopHandler(ui.position.left);
		}
	});
	
	$(canvasNodes[1]).css("left" , $("#refTrack1").css("left"));

	var removeTrackIconObj = trNode.getElementsByClassName("close")[0];

	//通过闭包传递参数
	removeTrackIconObj.onclick = removeTrack2(trackId);
	
	var modechangeIconObj;
	if(mode){
		if(mode=="dense"){
			modechangeIconObj = trNode.getElementsByClassName("densemode")[0];
			modechangeIconObj.onclick = changeTrackModeByBtn(trackId);
		}else if(mode=="pack"){
			modechangeIconObj = trNode.getElementsByClassName("packmode")[0];
			modechangeIconObj.onclick = changeTrackModeByBtn(trackId);
		}
	}

	for(var i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == trackId) {
			if(trackItems[i].dataType == "VCF" || trackItems[i].dataType == "GVF") {
				$(canvasNodes[1]).dblclick(function(event) {
					var target = event.target || event.srcElement;
					var trParentNode = target.parentNode.parentNode;
					var trackId = trParentNode.getAttribute("id");
					var trTop = $(trParentNode).position().top - document.body.scrollTop;
					var trLeft = $(trParentNode).position().left;
					if(isMini_personalpannel == 1){
						// if the personal pannel is mini
						var trackTable_width = $("#divTrack").width();
						$(document.getElementById("personalPannel")).css("height", "auto");
						$(document.getElementById("personalPannel")).css("width", trackTable_width);
						$('#showWindowBtn').remove();
					}
					$(document.getElementById("personalPannel")).css("display", "block");
					$(document.getElementById("personalPannel")).css("top", trTop);
					$(document.getElementById("personalPannel")).css("left", trLeft - 1);
					document.getElementById("ppTrackTable").innerHTML = "";
					$(document.getElementById("ppCloseBtn")).unbind();
					$(document.getElementById("ppMin")).unbind();
					$(document.getElementById("ppSet")).unbind();
					$(document.getElementById("ppCloseBtn")).click(removePvar);
					$(document.getElementById("ppMin")).click(miniPersonalPannel);
					$(document.getElementById("ppSet")).click(setPersonalPannel);
					addPvarHttpRequest(trackId);
				});
			}
			break;
		}
	}

	//这个function调用的是thickbox.js中的方法
	//tb_init('a.thickbox, area.thickbox, input.thickbox, span.thickbox');
}

/************************************move the personal pannel*****************************/
$(document).ready(function() {
	$("#personalPannelTitle").bind("mousedown", modesedown_in_personalpannel_title);
});

function modesedown_in_personalpannel_title(ev){
	$(document.body).bind("mousemove", mousemove_for_personalpannel);
	$(document.body).bind("mouseup", mouseup_for_personalpannel);
	document.body.onselectstart = function() {
		return false;
	};
	$(document.body).css("-moz-user-select","none");
	$(document.body).css("-webkit-user-select","none");
}

function mouseup_for_personalpannel(ev){
	document.body.onselectstart = function() {
		return true;
	};
	$(document.body).css("-moz-user-select","auto");
	$(document.body).css("-webkit-user-select","auto");
	$(document.body).unbind("mousemove", mousemove_for_personalpannel);
	$(document.body).unbind("mouseup", mouseup_for_personalpannel);
}

function mousemove_for_personalpannel(ev){
	var mouseCoordinates = mouseCoords_in_clientWindow(ev);
	$("#personalPannel").css("top", mouseCoordinates.y);
}
function mouseCoords_in_clientWindow(ev) {
	if(ev.clientX || ev.clientY) {
		return {
			x : ev.clientX,
			y : ev.clientY
		};
	}
	return {
		x : ev.pageX - document.body.scrollLeft + document.body.clientLeft,
		y : ev.pageY - document.body.scrollTop + document.body.clientTop
	};
}
/**************************************************************************************/

function removePvar() {
	$(document.getElementById("personalPannel")).css("display", "none");
	XMLHttpReq.onreadystatechange = function() {
		personalPannel.Pvar.id="";
		personalPannel.Pvar.mode="";
	};
	XMLHttpReq.open("GET", "servlet/test.do?action=removePvar", true);
	XMLHttpReq.send(null);
	$(document.getElementById("ppCloseBtn")).unbind();
	$(document.getElementById("ppMin")).unbind();
	$(document.getElementById("ppSet")).unbind();
}
var isMini_personalpannel = 0;
function miniPersonalPannel() {
	//$(document.getElementById("personalPannel")).css("display", "none");
	var _personalPanel = $('#personalPannel');
	var _width = _personalPanel.css('width');
	var _height = _personalPanel.css('height');
	var _top = _personalPanel.css('top');
	var _left = _personalPanel.css('left');
	isMini_personalpannel = 1;
	
	_personalPanel.animate(
			{ 
            width: 0, 
            height: 0,
            top: $(window).height() / 2,
            left: $(window).width(),
      },
			{
				complete: function(){
						$('body').append('<input type="button" id="showWindowBtn"'
							+ ' style="height:31px; width:32px; border-style:none; background: url(image/mouse-out-32.png); top:'
							+ ($(window).height() / 2 - 16) + 'px; left:' + ($(window).width() - 32) 
							+ 'px; position:fixed;" />');
						showWindowBtn = $('#showWindowBtn');
						showWindowBtn.mousemove(function(e){
							showWindowBtn.css('background', 'url(image/mouse-over-32.png)');
						});
						showWindowBtn.mouseout(function(e){
							showWindowBtn.css('background', 'url(image/mouse-out-32.png)');
						});
						showWindowBtn.click(function(e){
								_left = $("#divTrack").position().left;
								showWindowBtn.remove();
								_personalPanel.css("display","block");
								_personalPanel.animate(
										{ 
												width: _width, 
												height: "auto",
												top: _top,
												left: _left,
										}
								);
								_personalPanel.css("height","auto");
								isMini_personalpannel = 0;
								//$(window).resize();
						});
						_personalPanel.css("display","none");
				}
			}
	);
}

function setPersonalPannel(){
	var overlayDIV = document.getElementById("overlayDIV");
	$(overlayDIV).css("display","block");
	var ppsLeft = (document.body.clientWidth - $("#personalPannelSetting").width())/2;
	var ppsTop = (document.body.clientHeight - $("#personalPannelSetting").height())/2;
	$("#personalPannelSetting").css("left", ppsLeft);
	$("#personalPannelSetting").css("top", ppsTop);
	$("#personalPannelSetting").css("display","block");
	document.getElementById("ppsCloseBtn").onclick = overlayHide;
	overlayDIV.onclick = overlayHide;
	var PvarTable = document.getElementById("PvarTable");
	var PfannoTable = document.getElementById("PfannoTable");
	var PannoTable = document.getElementById("PannoTable");
	var PclnsTable = document.getElementById("PclnsTable");
	var trNode, tdNode;
	var radioObj, labelObj;
	var i;
	////modified by Liran for scoring method setting
	PvarTable.innerHTML = "";
	var pattern = /<.*?>/g;
	var XMLHttpReq9 = createXMLHttpRequest();
	XMLHttpReq9.open("GET","servlet/test.do?action=getScoreMethods",false);
	XMLHttpReq9.send(null);
	var scoremethlist=XMLHttpReq9.responseText.replace(pattern,"");
	var scoremeths_temp=scoremethlist.split(",");

	for(i = 0; i<scoremeths_temp.length; i++){
		if(i % 4 == 0) {
			trNode = PvarTable.insertRow(-1);
		}
		tdNode = trNode.insertCell(-1);
		radioObj = document.createElement("input");
		radioObj.id = scoremeths_temp[i] + "radio";
		radioObj.type = "radio";
		radioObj.name = "personalVar";
		radioObj.value = scoremeths_temp[i];
		if(scoremeths_temp[i] == scoremethPvar){
			radioObj.checked = true;
		}
		tdNode.appendChild(radioObj);
		labelObj = document.createElement("label");
		labelObj.setAttribute("for", scoremeths_temp[i] + "radio");
		labelObj.innerHTML = scoremeths_temp[i];
		tdNode.appendChild(labelObj);
	
		radioObj.onclick = function(event){
			var target = event.target || event.srcElement;
			scoremethPvar = target.getAttribute("value");

			setScoreMethHttpRequest();
		};
	}
	////modified by Liran for scoring method setting
	PfannoTable.innerHTML = "";
	for(i = 0; i<personalPannel.personalTrackItems.Pfannos.length; i++){
		if(i % 4 == 0) {
			trNode = PfannoTable.insertRow(-1);
		}
		tdNode = trNode.insertCell(-1);
		radioObj = document.createElement("input");
		radioObj.id = personalPannel.personalTrackItems.Pfannos[i] + "radio";
		radioObj.type = "radio";
		radioObj.name = "personalFanno";
		radioObj.value = personalPannel.personalTrackItems.Pfannos[i];
		if(personalPannel.personalTrackItems.Pfannos[i] == personalPannel.Pfanno.id){
			radioObj.checked = true;
		}
		tdNode.appendChild(radioObj);
		labelObj = document.createElement("label");
		labelObj.setAttribute("for", personalPannel.personalTrackItems.Pfannos[i] + "radio");
		labelObj.innerHTML = personalPannel.personalTrackItems.Pfannos[i];
		tdNode.appendChild(labelObj);
		
		radioObj.onclick = function(event){
			var target = event.target || event.srcElement;
			var Pfanno_id = target.getAttribute("value").replace("_","");
			
			addPfannoHttpRequest(Pfanno_id);
		};
	}
	PannoTable.innerHTML = "";
	for(i = 0; i<personalPannel.personalTrackItems.Pannos.length; i++){
		if(i % 4 == 0) {
			trNode = PannoTable.insertRow(-1);
		}
		tdNode = trNode.insertCell(-1);
		radioObj = document.createElement("input");
		radioObj.id = personalPannel.personalTrackItems.Pannos[i] + "radio";
		radioObj.type = "radio";
		radioObj.name = "personalAnno";
		radioObj.value = personalPannel.personalTrackItems.Pannos[i];
		if(personalPannel.personalTrackItems.Pannos[i] == personalPannel.Panno.id){
			radioObj.checked = true;
		}
		tdNode.appendChild(radioObj);
		labelObj = document.createElement("label");
		labelObj.setAttribute("for", personalPannel.personalTrackItems.Pannos[i] + "radio");
		labelObj.innerHTML = personalPannel.personalTrackItems.Pannos[i];
		tdNode.appendChild(labelObj);
		
		radioObj.onclick = function(event){
			var target = event.target || event.srcElement;
			var Panno_id = target.getAttribute("value").replace("_","");
			
			addPannoHttpRequest(Panno_id);
		};
	}
	PclnsTable.innerHTML = "";
	for(i = 0; i<personalPannel.personalTrackItems.Pclnss.length; i++){
		if(i % 4 == 0) {
			trNode = PclnsTable.insertRow(-1);
		}
		tdNode = trNode.insertCell(-1);
		radioObj = document.createElement("input");
		radioObj.id = personalPannel.personalTrackItems.Pclnss[i] + "chk";
		radioObj.type = "checkbox";
		radioObj.name = "personalClns";
		radioObj.value = personalPannel.personalTrackItems.Pclnss[i];
		for(var j=0; j< personalPannel.Pclns.length;j++){
			if(personalPannel.personalTrackItems.Pclnss[i] == personalPannel.Pclns[j].id){
				radioObj.checked = true;
				break;
			}
		}
		tdNode.appendChild(radioObj);
		labelObj = document.createElement("label");
		labelObj.setAttribute("for", personalPannel.personalTrackItems.Pclnss[i] + "chk");
		labelObj.innerHTML = personalPannel.personalTrackItems.Pclnss[i];
		tdNode.appendChild(labelObj);
		
		radioObj.onclick = function(event){
			if(personalPannel.Panno.id && personalPannel.Pfanno.id){
				var target = event.target || event.srcElement;
				var Pclns_id = target.getAttribute("value").replace("_","");
				
				if($(target).attr("checked")){
					addPclnsHttpRequest(Pclns_id);
				}else{
					removePclnsHttpRequest(Pclns_id);
				}
			}else{
				$(pclns_checkboxs[i]).attr("checked", false);
				alert("Please select Pfanno&Panno first!");
			}
		};
	}
}
/******************************************clear button function************************************/
$(document).ready(function() {
	$("#PclnsSetClrBtn").bind("click",remove_all_pclns);
	$("#PfannoSetClrBtn").bind("click", remove_pfanno);
	$("#PannoSetClrBtn").bind("click", remove_panno);
});
function remove_all_pclns(){
	var pclns_checkboxs = document.getElementsByName("personalClns");
	var i;
	for(i=0;i<pclns_checkboxs.length;i++){
		if($(pclns_checkboxs[i]).attr("checked")){
			$(pclns_checkboxs[i]).attr("checked", false);
			$(pclns_checkboxs[i]).click();
			$(pclns_checkboxs[i]).attr("checked", false);
		}
	}
}

function remove_pfanno(){
	if(personalPannel.Pfanno.id){
		if(personalPannel.Pclns.length == 0){
			$(document.getElementById(personalPannel.Pfanno.id + "radio")).attr("checked",false);
			var ppTrackTable = document.getElementById("ppTrackTable");
			var trNode = document.getElementById(personalPannel.Pfanno.id);
			var rowIndex = trNode.rowIndex;
			ppTrackTable.deleteRow(rowIndex);
			var url = "servlet/test.do?action=removePfanno"
			XMLHttpReq.onreadystatechange = function(){};
			XMLHttpReq.open("GET", url, true);
			XMLHttpReq.send(null);
			personalPannel.Pfanno.id = "";
		}else{
			alert("Please delete all Phenotype tracks first!");
		}
	}
}

function remove_panno(){
	if(personalPannel.Panno.id){
		if(personalPannel.Pclns.length == 0){
			$(document.getElementById(personalPannel.Panno.id + "radio")).attr("checked", false);
			var ppTrackTable = document.getElementById("ppTrackTable");
			var trNode = document.getElementById(personalPannel.Panno.id);
			var rowIndex = trNode.rowIndex;
			ppTrackTable.deleteRow(rowIndex);
			var url = "servlet/test.do?action=removePanno"
			XMLHttpReq.onreadystatechange = function(){};
			XMLHttpReq.open("GET", url, true);
			XMLHttpReq.send(null);
			personalPannel.Panno.id = "";
		}else{
			alert("Please delete all Phenotype tracks first!");
		}
	}
}
/***************************************************************************************************/

function removeTrackByCloseBtn_in_personalpannel(trackId){
	return function(){
		if(trackId == personalPannel.Pfanno.id){
			remove_pfanno();
		}else if(trackId == personalPannel.Panno.id){
			remove_panno();
		}else{
			removePclnsHttpRequest((trackId+"").replace(/^_/,""));
		}
		var ppTop = (document.body.clientHeight - $("#personalPannel").height() - 10);
		ppTop = ppTop > 50 ? ppTop : 50;
		$("#personalPannel").animate({top:ppTop});
		$("#ppTrackTable tbody").sortable({axis:"y" ,cancel:".cannotSortable"});
	};
}

function overlayHide() {
	$("#overlayDIV").css("display","none");
	$("#personalPannelSetting").css("display","none");
}

/*************personal pannel tracks setting toggle function**********/

function pvarSetToggle() {
	$("#PvarSetContent").slideToggle();
}

function pfannoSetToggle() {
	$("#PfannoSetContent").slideToggle();
}

function pannoSetToggle() {
	$("#PannoSetContent").slideToggle();
}

function pclnsSetToggle() {
	$("#PclnsSetContent").slideToggle();
}

/***************************End***************************************/

function setScoreMethHttpRequest() {
	////added by Liran for scoring method setting
	var url = "servlet/test.do?action=setScoreMethod&scoremeth="+scoremethPvar;
	XMLHttpReq.onreadystatechange = handle_addPvar_Request;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}
function addPvarHttpRequest(trackId) {
	var url = "servlet/test.do?action=addPvar&tracks=";
	var P_id, P_track, P_mode;
	var i;
	for(i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == trackId) {
			P_id = trackId;
			P_track = trackItems[i].superid ? trackItems[i].superid : trackId;
			P_mode = trackItems[i].mode;
			break;
		}
	}
	
	if(P_mode == "hide"){
		P_mode = "dense";
	}
	
	url = url + P_track + "&modes=" + P_mode + "&id=" + P_id;

	personalPannel.Pvar.id = "_" + trackId;
	personalPannel.Pvar.mode = P_mode;

	XMLHttpReq.onreadystatechange = handle_addPvar_Request;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}
function addPvarHttpRequest2(PvarID,trackId) {//for add individual directly
	var url = "servlet/test.do?action=addPvar&tracks=";
	var P_mode;
	
	initPvar_superid = trackId;
	P_mode=personalPannel.Pvar.mode;
	if(P_mode == "hide" || P_mode == ""){
		P_mode = "dense";
	}
	
	url = url + trackId + "&modes=" + P_mode + "&id=" + PvarID;

	personalPannel.Pvar.id = "_" + PvarID;
	personalPannel.Pvar.mode = P_mode;

	XMLHttpReq.onreadystatechange = handle_addPvar_Request;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}

function handle_addPvar_Request() {
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			var XMLDoc = XMLHttpReq.responseXML;
			var personal_genome_node = XMLDoc.getElementsByTagName(xmlTagVariants)[0];
			var elementNodes = XMLDoc.getElementsByTagName(xmlTagElements);
			var i, j;
			//personal genome
			createPPGTrack(personalPannel.Pvar.id, personalPannel.Pvar.mode);
			var canvasNodes = document.getElementById(personalPannel.Pvar.id).getElementsByTagName("canvas");
			showVariantByImg(canvasNodes[0], canvasNodes[1], personal_genome_node, personalPannel.Pvar.mode);
			//Pfanno track
			if(personalPannel.Pfanno.id){
				var personal_fanno_node;
				for(i=0;i<elementNodes.length;i++){
					if(personalPannel.Pfanno.id == elementNodes[i].getAttribute("id")){
						personal_fanno_node = elementNodes[i];
						createPPOtherTrack(personalPannel.Pfanno.id, personalPannel.Pfanno.mode);
						canvasNodes = document.getElementById(personalPannel.Pfanno.id).getElementsByTagName("canvas");
						showGene(canvasNodes[0], canvasNodes[1], personal_fanno_node, personalPannel.Pfanno.mode);
						break;
					}
				}
			}
			if(personalPannel.Panno.id){
				var personal_anno_nodes = [];
				for(i=0;i<elementNodes.length;i++){
					if(elementNodes[i].getAttribute("id") == personalPannel.Panno.id){
						personal_anno_nodes.push(elementNodes[i]);
					}
				}
				createPPOtherTrack(personalPannel.Panno.id, personalPannel.Panno.mode);
				canvasNodes = document.getElementById(personalPannel.Panno.id).getElementsByTagName("canvas");
				if(personal_anno_nodes.length == 1){
					showPersonalGeneByImg_OneNode(canvasNodes[0], canvasNodes[1], personal_anno_nodes[0], personalPannel.Panno.mode);
				}else if(personal_anno_nodes.length == 2){
					showPersonalGeneByImg_TwoNode(canvasNodes[0], canvasNodes[1], personal_anno_nodes[0], personal_anno_nodes[1], personalPannel.Panno.mode);
				}
			}
			//Pclns tracks
			if(personalPannel.Pclns.length > 0){
				for(i=0;i<personalPannel.Pclns.length;i++){
					for(j=0;j<elementNodes.length;j++){
						if(elementNodes[j].getAttribute("id")==personalPannel.Pclns[i].id){
							createPPOtherTrack(personalPannel.Pclns[i].id, personalPannel.Pclns[i].mode);
							canvasNodes = document.getElementById(personalPannel.Pclns[i].id).getElementsByTagName("canvas");
							showGene(canvasNodes[0], canvasNodes[1], elementNodes[j], personalPannel.Pfanno.mode);
							break;
						}
					}
				}
			}
			
			var ppTop = (document.body.clientHeight - $("#personalPannel").height() - 10);
			ppTop = ppTop > 50 ? ppTop : 50;
			$("#personalPannel").animate({top:ppTop});
			$("#ppTrackTable tbody").sortable({axis:"y" ,cancel:".cannotSortable"});
		}
	}
}

function addPfannoHttpRequest(trackId){
	var url = "servlet/test.do?action=addPfanno&tracks=";
	var P_track, P_mode;
	var i;
	for(i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == trackId) {
			P_track = trackId;
			P_mode = trackItems[i].mode;
			break;
		}
	}
	
	if(P_mode == "hide"){
		P_mode = "dense";
	}
	
	url = url + P_track + "&modes=" + P_mode;

	personalPannel.Pfanno.id = "_" + trackId;
	personalPannel.Pfanno.mode = P_mode;

	XMLHttpReq.onreadystatechange = handle_addPfanno_Request;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}

function handle_addPfanno_Request(){
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			var XMLDoc = XMLHttpReq.responseXML;
			var elementNodes = XMLDoc.getElementsByTagName(xmlTagElements);
			var i, j;
			
			var ppTrackTable = document.getElementById("ppTrackTable");
			
			for( j = 0; j < personalPannel.personalTrackItems.Pfannos.length; j++) {
				var trNode_Pfanno = document.getElementById(personalPannel.personalTrackItems.Pfannos[j]);
				if(trNode_Pfanno) {
					i = trNode_Pfanno.rowIndex;
					ppTrackTable.deleteRow(i);
					break;
				}
			}
	
			var trNode_Pfanno = document.getElementById(personalPannel.Pfanno.id);
			if(trNode_Pfanno){
				i = trNode_Pfanno.rowIndex;
				ppTrackTable.deleteRow(i);
			}
			var trNode_Panno = document.getElementById(personalPannel.Panno.id);
			if(trNode_Panno){
				i = trNode_Panno.rowIndex;
				ppTrackTable.deleteRow(i);
			}
			for(j=0;j<personalPannel.Pclns.length;j++){
				var trNode_Pclns = document.getElementById(personalPannel.Pclns[j].id);
				if(trNode_Pclns){
					i = trNode_Pclns.rowIndex;
					ppTrackTable.deleteRow(i);
				}
			}
			
			//Pfanno track
			if(personalPannel.Pfanno.id){
				var personal_fanno_node;
				for(i=0;i<elementNodes.length;i++){
					if(personalPannel.Pfanno.id == elementNodes[i].getAttribute("id")){
						personal_fanno_node = elementNodes[i];
						createPPOtherTrack(personalPannel.Pfanno.id, personalPannel.Pfanno.mode);
						canvasNodes = document.getElementById(personalPannel.Pfanno.id).getElementsByTagName("canvas");
						showGene(canvasNodes[0], canvasNodes[1], personal_fanno_node, personalPannel.Pfanno.mode);
						break;
					}
				}
			}
			if(personalPannel.Panno.id){
				var personal_anno_nodes = [];
				for(i=0;i<elementNodes.length;i++){
					if(elementNodes[i].getAttribute("id") == personalPannel.Panno.id){
						personal_anno_nodes.push(elementNodes[i]);
					}
				}
				createPPOtherTrack(personalPannel.Panno.id, personalPannel.Panno.mode);
				canvasNodes = document.getElementById(personalPannel.Panno.id).getElementsByTagName("canvas");
				if(personal_anno_nodes.length == 1){
					showPersonalGeneByImg_OneNode(canvasNodes[0], canvasNodes[1], personal_anno_nodes[0], personalPannel.Panno.mode);
				}else if(personal_anno_nodes.length == 2){
					showPersonalGeneByImg_TwoNode(canvasNodes[0], canvasNodes[1], personal_anno_nodes[0], personal_anno_nodes[1], personalPannel.Panno.mode);
				}
			}
			//Pclns tracks
			if(personalPannel.Pclns.length > 0){
				for(i=0;i<personalPannel.Pclns.length;i++){
					for(j=0;j<elementNodes.length;j++){
						if(elementNodes[j].getAttribute("id")==personalPannel.Pclns[i].id){
							createPPOtherTrack(personalPannel.Pclns[i].id, personalPannel.Pclns[i].mode);
							canvasNodes = document.getElementById(personalPannel.Pclns[i].id).getElementsByTagName("canvas");
							showGene(canvasNodes[0], canvasNodes[1], elementNodes[j], personalPannel.Pfanno.mode);
							break;
						}
					}
				}
			}
			var ppTop = (document.body.clientHeight - $("#personalPannel").height() - 10);
			ppTop = ppTop > 50 ? ppTop : 50;
			$("#personalPannel").animate({top:ppTop});
		}
	}
}

function addPannoHttpRequest(trackId){
	var url = "servlet/test.do?action=addPanno&tracks=";
	var P_track, P_mode;
	var i;
	for(i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == trackId) {
			P_track = trackId;
			P_mode = trackItems[i].mode;
			break;
		}
	}
	
	if(P_mode == "hide"){
		P_mode = "dense";
	}
	
	url = url + P_track + "&modes=" + P_mode;

	personalPannel.Panno.id = "_" + trackId;
	personalPannel.Panno.mode = P_mode;

	XMLHttpReq.onreadystatechange = handle_addPanno_Request;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}

function handle_addPanno_Request(){
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			var XMLDoc = XMLHttpReq.responseXML;
			var elementNodes = XMLDoc.getElementsByTagName(xmlTagElements);
			var i, j;
			
			var ppTrackTable = document.getElementById("ppTrackTable");
			for( j = 0; j < personalPannel.personalTrackItems.Pannos.length; j++) {
				var trNode_Panno = document.getElementById(personalPannel.personalTrackItems.Pannos[j]);
				if(trNode_Panno) {
					i = trNode_Panno.rowIndex;
					ppTrackTable.deleteRow(i);
					break;
				}
			}
			for(j=0;j<personalPannel.Pclns.length;j++){
				var trNode_Pclns = document.getElementById(personalPannel.Pclns[j].id);
				if(trNode_Pclns){
					i = trNode_Pclns.rowIndex;
					ppTrackTable.deleteRow(i);
				}
			}
			
			if(personalPannel.Panno.id){
				var personal_anno_nodes = [];
				for(i=0;i<elementNodes.length;i++){
					if(elementNodes[i].getAttribute("id") == personalPannel.Panno.id){
						personal_anno_nodes.push(elementNodes[i]);
					}
				}
				createPPOtherTrack(personalPannel.Panno.id, personalPannel.Panno.mode);
				canvasNodes = document.getElementById(personalPannel.Panno.id).getElementsByTagName("canvas");
				if(personal_anno_nodes.length == 1){
					showPersonalGeneByImg_OneNode(canvasNodes[0], canvasNodes[1], personal_anno_nodes[0], personalPannel.Panno.mode);
				}else if(personal_anno_nodes.length == 2){
					showPersonalGeneByImg_TwoNode(canvasNodes[0], canvasNodes[1], personal_anno_nodes[0], personal_anno_nodes[1], personalPannel.Panno.mode);
				}
			}
			//Pclns tracks
			if(personalPannel.Pclns.length > 0){
				for(i=0;i<personalPannel.Pclns.length;i++){
					for(j=0;j<elementNodes.length;j++){
						if(elementNodes[j].getAttribute("id")==personalPannel.Pclns[i].id){
							createPPOtherTrack(personalPannel.Pclns[i].id, personalPannel.Pclns[i].mode);
							canvasNodes = document.getElementById(personalPannel.Pclns[i].id).getElementsByTagName("canvas");
							showGene(canvasNodes[0], canvasNodes[1], elementNodes[j], personalPannel.Pfanno.mode);
							break;
						}
					}
				}
			}
			var ppTop = (document.body.clientHeight - $("#personalPannel").height() - 10);
			ppTop = ppTop > 50 ? ppTop : 50;
			$("#personalPannel").animate({top:ppTop});
		}
	}
}


function addPclnsHttpRequest(trackId){
	var url = "servlet/test.do?action=addPclns&tracks=";
	var P_track, P_mode;
	var i;
	for(i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == trackId) {
			P_track = trackId;
			P_mode = trackItems[i].mode;
			break;
		}
	}
	
	if(P_mode == "hide"){
		P_mode = "dense";
	}
	
	url = url + P_track + "&modes=" + P_mode;
	
	personalPannel.Pclns[personalPannel.Pclns.length] = [];
	personalPannel.Pclns[personalPannel.Pclns.length - 1].id = "_" + trackId;
	personalPannel.Pclns[personalPannel.Pclns.length - 1].mode = P_mode;

	XMLHttpReq.onreadystatechange = handle_addPclns_Request;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}
function handle_addPclns_Request(){
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			var XMLDoc = XMLHttpReq.responseXML;
			var elementNode = XMLDoc.getElementsByTagName(xmlTagElements)[0];
			
			createPPOtherTrack(personalPannel.Pclns[personalPannel.Pclns.length - 1].id, personalPannel.Pclns[personalPannel.Pclns.length - 1].mode);
			canvasNodes = document.getElementById(personalPannel.Pclns[personalPannel.Pclns.length - 1].id).getElementsByTagName("canvas");
			showGene(canvasNodes[0], canvasNodes[1], elementNode, personalPannel.Pclns[personalPannel.Pclns.length - 1].mode);
			
			var ppTop = (document.body.clientHeight - $("#personalPannel").height() - 10);
			ppTop = ppTop > 50 ? ppTop : 50;
			$("#personalPannel").animate({top:ppTop});
		}
	}
}

function removePclnsHttpRequest(trackId){
	var url = "servlet/test.do?action=removePclns&tracks=" + trackId;
	for(var i=0;i<personalPannel.Pclns.length;i++){
		if(personalPannel.Pclns[i].id == "_"+trackId){
			personalPannel.Pclns.splice(i,1);
			break;
		}
	}
	
	var ppTrackTable = document.getElementById("ppTrackTable");
	var trNode = document.getElementById("_" + trackId);
	var rowIndex = trNode.rowIndex;
	ppTrackTable.deleteRow(rowIndex);
	
	XMLHttpReq.onreadystatechange = function(){};
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}
var current_personal_track, current_personal_track_group, current_personal_track_mode;
function ppTrackModeChangeRequest(trackId, mode){
	var temp_track = (trackId + "").replace(/^_/,"");
	var param_tracks,param_id,param_action;
	var url = "servlet/test.do?action=";
	var group;
	var i;
	if(trackId == personalPannel.Pvar.id){
		for(i=0; i< trackItems.length; i++){
			if(trackItems[i].id == temp_track){
				param_id = temp_track;
				param_tracks = trackItems[i].superid ? trackItems[i].superid : temp_track;
				break;
			}
		}
		if(param_id + "." == "undefined." || param_tracks + "." == "undefined."){
			param_id = temp_track;
			param_tracks = initPvar_superid;
		}
		personalPannel.Pvar.mode = mode;
		param_action = "modiPvar";
		url = url + param_action + "&tracks=" + param_tracks + "&modes=" + mode + "&id=" + param_id;
		group = "Pvar";
	}else{
		if(trackId == personalPannel.Pfanno.id){
			personalPannel.Pfanno.mode = mode;
			param_action = "modiPfanno";
			group = "Pfanno";
		}else if(trackId == personalPannel.Panno.id){
			personalPannel.Panno.mode = mode;
			param_action = "modiPanno";
			group = "Panno";
		}else{
			for(i = 0;i<personalPannel.Pclns.length;i++){
				if(personalPannel.Pclns[i].id == trackId){
					personalPannel.Pclns[i].mode = mode;
					break;
				}
			}
			param_action = "modiPclns";
			group = "Pclns";
		}
		param_tracks = temp_track;
		url = url + param_action + "&tracks=" + param_tracks + "&modes=" + mode;
	}
	current_personal_track = trackId;
	current_personal_track_group = group;
	current_personal_track_mode = mode;
	XMLHttpReq.onreadystatechange = handle_ppTrackModeChangeRequest;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}

function handle_ppTrackModeChangeRequest(){
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			var group = current_personal_track_group;
			var trackId = current_personal_track;
			var mode = current_personal_track_mode;
			var XMLDoc = XMLHttpReq.responseXML;
			var canvasNodes = document.getElementById(trackId).getElementsByTagName("canvas");
			var elementNodes = XMLDoc.getElementsByTagName(xmlTagElements);
			var i;
			if(group == "Pvar") {
				var personal_genome_node = XMLDoc.getElementsByTagName(xmlTagVariants)[0];
				showVariantByImg(canvasNodes[0], canvasNodes[1], personal_genome_node, mode);
			} else if(group == "Pfanno") {
				var personal_fanno_node;
				for( i = 0; i < elementNodes.length; i++) {
					if(personalPannel.Pfanno.id == elementNodes[i].getAttribute("id")) {
						personal_fanno_node = elementNodes[i];
						showGene(canvasNodes[0], canvasNodes[1], personal_fanno_node, mode);
						break;
					}
				}
			} else if(group == "Panno") {
				var personal_anno_nodes = [];
				for( i = 0; i < elementNodes.length; i++) {
					if(elementNodes[i].getAttribute("id") == personalPannel.Panno.id) {
						personal_anno_nodes.push(elementNodes[i]);
					}
				}
				if(personal_anno_nodes.length == 1) {
					showPersonalGeneByImg_OneNode(canvasNodes[0], canvasNodes[1], personal_anno_nodes[0], mode);
				} else if(personal_anno_nodes.length == 2) {
					showPersonalGeneByImg_TwoNode(canvasNodes[0], canvasNodes[1], personal_anno_nodes[0], personal_anno_nodes[1], mode);
				}
			} else {
				var personal_clns_node;
				for( i = 0; i < elementNodes.length; i++) {
					if(elementNodes[i].getAttribute("id") == trackId) {
						personal_clns_node = elementNodes[i];
						showGene(canvasNodes[0], canvasNodes[1], personal_clns_node, mode);
						break;
					}
				}
			}
			var ppTop = (document.body.clientHeight - $("#personalPannel").height() - 10);
			ppTop = ppTop > 50 ? ppTop : 50;
			$("#personalPannel").animate({top:ppTop});
			$("#ppTrackTable tbody").sortable({axis:"y" ,cancel:".cannotSortable"});
		}
	}
}

function changePPTrackModeByBtn(trackId){
	return function(){
		var trNode = document.getElementById(trackId);
		var modechangeBtnSpanObj;
		if(trNode.getElementsByClassName("densemode").length>0){
			ppTrackModeChangeRequest(trackId, "pack");
			modechangeBtnSpanObj = trNode.getElementsByClassName("densemode")[0];
			modechangeBtnSpanObj.className = "packmode";
		}else{
			ppTrackModeChangeRequest(trackId, "dense");
			modechangeBtnSpanObj = trNode.getElementsByClassName("packmode")[0];
			modechangeBtnSpanObj.className = "densemode";
		}
	}
}

function createPPGTrack(trackId, mode) {
	var ppTrackTable = document.getElementById("ppTrackTable");
	document.getElementById("ppTrackTable").innerHTML = "";
	var trNode = ppTrackTable.insertRow(-1);
	trNode.id = trackId;
	var modechangeBtnSpan_str = "";
	if(mode){
		if(mode=="pack"){
			modechangeBtnSpan_str = "<span class=\"packmode\"></span>";
		}else if(mode == "dense"){
			modechangeBtnSpan_str = "<span class=\"densemode\"></span>";
		}else{
			modechangeBtnSpan_str = "";
		}
	}
	trNode.innerHTML = "<td class=\"trackOperator cannotSortable\"><div style=\"width:50px;margin:0;padding:0;border:0;\">"+ modechangeBtnSpan_str +"</div></td><td class=\"trackName\"><canvas width=\"100\" height=\"50\" style=\"background: #ffffff\"></canvas></td><td class=\"trackContent cannotSortable\"><div style=\"width:940px;overflow:hidden;padding:0;margin:0;border:0;\"><canvas width=\""+ trackLength +"\" height=\"50\" class=\"canvasTrackcontent\" title=\"shift+click and drag to zoom in\"></canvas></div></td>";
	var canvasNodes = trNode.getElementsByTagName("canvas");
	canvasNodes[0].onmouseover = mouseOver;
	canvasNodes[0].onmouseout = mouseOut;

	canvasNodes[1].onmousedown =  mouseDownRightCanvasInPP;
	//$(canvasNodes[1]).bind("mousedown", mouseDownRightCanvasInPP);
	
	addMousewheelEvent(canvasNodes[1], mousewheelHandler);
	
	$(canvasNodes[1]).css("left" , $("#refTrack1").css("left"));
	
	$(canvasNodes[1]).draggable({ 
		axis: "x" ,
		cursor: "url(./image/Grabber.cur),auto" ,
		drag : function(event, ui) {
			$(".canvasTrackcontent").each(function(arrayindex, arrayele) {
				$(arrayele).css("left", ui.position.left);
			});
			dragDragHandler(ui.position.left);
		},
		stop : function(event, ui){
			dragStopHandler(ui.position.left);
		}
	});
	
	var modechangeIconObj;
	if(mode){
		if(mode=="dense"){
			modechangeIconObj = trNode.getElementsByClassName("densemode")[0];
			modechangeIconObj.onclick = changePPTrackModeByBtn(trackId);
		}else if(mode=="pack"){
			modechangeIconObj = trNode.getElementsByClassName("packmode")[0];
			modechangeIconObj.onclick = changePPTrackModeByBtn(trackId);
		}
	}
}

function createPPOtherTrack(trackId, mode) {
	var ppTrackTable = document.getElementById("ppTrackTable");
	var trNode = ppTrackTable.insertRow(-1);
	trNode.id = trackId;
	var modechangeBtnSpan_str = "";
	if(mode){
		if(mode=="pack"){
			modechangeBtnSpan_str = "<span class=\"packmode\"></span>";
		}else if(mode == "dense"){
			modechangeBtnSpan_str = "<span class=\"densemode\"></span>";
		}else{
			modechangeBtnSpan_str = "";
		}
	}
	trNode.innerHTML = "<td class=\"trackOperator cannotSortable\"><div style=\"width:50px;margin:0;padding:0;border:0;\">"+ modechangeBtnSpan_str +"<span class=\"close\"></span></div></td><td class=\"trackName\"><canvas width=\"100\" height=\"50\" style=\"background: #ffffff\"></canvas></td><td class=\"trackContent cannotSortable\"><div style=\"width:940px;overflow:hidden;padding:0;margin:0;border:0;\"><canvas width=\""+ trackLength+"\" height=\"50\" class=\"canvasTrackcontent\" title=\"shift+click and drag to zoom in\"></canvas></div></td>";
	var canvasNodes = trNode.getElementsByTagName("canvas");
	canvasNodes[0].onmouseover = mouseOver;
	canvasNodes[0].onmouseout = mouseOut;
	canvasNodes[1].onmousedown =  mouseDownRightCanvasInPP;
	//$(canvasNodes[1]).bind("mousedown", mouseDownRightCanvasInPP);
	
	addMousewheelEvent(canvasNodes[1], mousewheelHandler);
	
	$(canvasNodes[1]).css("left" , $("#refTrack1").css("left"));
	
	$(canvasNodes[1]).draggable({
		axis : "x",
		cursor : "url(./image/Grabber.cur),auto",
		drag : function(event, ui) {
			$(".canvasTrackcontent").each(function(arrayindex, arrayele) {
				$(arrayele).css("left", ui.position.left);
			});
			dragDragHandler(ui.position.left);
		},
		stop : function(event, ui){
			dragStopHandler(ui.position.left);
		}
	});
	
	var modechangeIconObj;
	if(mode){
		if(mode=="dense"){
			modechangeIconObj = trNode.getElementsByClassName("densemode")[0];
			modechangeIconObj.onclick = changePPTrackModeByBtn(trackId);
		}else if(mode=="pack"){
			modechangeIconObj = trNode.getElementsByClassName("packmode")[0];
			modechangeIconObj.onclick = changePPTrackModeByBtn(trackId);
		}
	}
	
	var removeTrackIconObj = trNode.getElementsByClassName("close")[0];
	removeTrackIconObj.onclick = removeTrackByCloseBtn_in_personalpannel(trackId);
}

function removeTrack(trackId) {
	var trackTable = document.getElementById("tableTrack");
	var trNode = document.getElementById(trackId);
	var rowIndex = trNode.rowIndex;
	trackTable.deleteRow(rowIndex);
}

var currentTrackItem;
function trackModeOnchange(track_Id, trackMode) {
	var trackItemIndex;
	for(var i = 0; i < trackItems.length; i++) {
		if(trackItems[i].id == track_Id) {
			trackItemIndex = i;
			break;
		}
	}
	if(trackItems[trackItemIndex].isServer == 0) {
		if($.cookie("customTrackList")) {
			var customTrackListCookie = $.cookie("customTrackList");
			var replaceStr = trackItems[trackItemIndex].id + ":" + trackMode;
			var replacedStr = trackItems[trackItemIndex].id + ":" + trackItems[trackItemIndex].mode;
			customTrackListCookie = customTrackListCookie.replace(replacedStr, replaceStr);
			$.cookie("customTrackList", customTrackListCookie, {
				expires : 10 / 24
			});
		} else {
			alert("Your custom track is invalid,you can refresh page then add your custom track again!");
			return;
		}
	}

	var url = "servlet/test.do?action=";
	if(trackMode == "hide") {
		trackItems[trackItemIndex].mode = "hide";
		removeTrack(trackItems[trackItemIndex].id);
		url = url + "removeTracks&tracks=" + trackItems[trackItemIndex].id;
		XMLHttpReq.onreadystatechange = null;
		trackItems_setting2();
	} else {
		if(trackItems[trackItemIndex].mode == "hide") {
			url = url + "addTracks&tracks=" + trackItems[trackItemIndex].id + "&modes=";
			createTrack(trackItems[trackItemIndex].id, trackMode);
		} else {
			url = url + "modiTracks&tracks=" + trackItems[trackItemIndex].id + "&modes=";
		}
		if(document.getElementById(loadingId)) {
			hideLoadingImage(loadingId);
		}
		loadingId = showLoadingImage(trackItems[trackItemIndex].id, "body");
		url = url + trackMode;
		trackItems[trackItemIndex].mode = trackMode;
		currentTrackItem = trackItems[trackItemIndex];
		XMLHttpReq.onreadystatechange = function() {
			handleOnchange(currentTrackItem);
		};
	}
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}

function handleOnchange(currentTrackItem) {
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			var XMLDoc = XMLHttpReq.responseXML;
			if(currentTrackItem.mode != "hide") {
				if(currentTrackItem.dataType == "BAM") {
					var readNodes = XMLDoc.getElementsByTagName(xmlTagReads);
					var readsNode;
					var readsId;
					var i;
					var refSeqTrackNode = document.getElementById(currentTrackItem.id);
					var refSeqCanvasNodes = refSeqTrackNode.getElementsByTagName("canvas");
					for( i = 0; i < readNodes.length; i++) {
						readsId = readNodes[i].getAttribute(xmlAttributeId);
						if(readsId == currentTrackItem.id) {
							readsNode = readNodes[i];
							break;
						}
					}
					if(i == readNodes.length) {
						readNodes = XMLDoc.getElementsByTagName(xmlTagValues);
						for( i = 0; i < readNodes.length; i++) {
							readsId = readNodes[i].getAttribute(xmlAttributeId);
							if(readsId == currentTrackItem.id) {
								readsNode = readNodes[i];
								break;
							}
						}
						showPositiveValueCombine(refSeqCanvasNodes[0], refSeqCanvasNodes[1], readsNode, currentTrackItem.mode, "topdown", 70, 50);
					} else {
						showRead(refSeqCanvasNodes[0], refSeqCanvasNodes[1], readsNode, currentTrackItem.mode, false);
					}
				} else if(currentTrackItem.dataType == "VCF" || currentTrackItem.dataType == "GVF") {
					var variantsNodes = XMLDoc.getElementsByTagName(xmlTagVariants);
					var variantsId;
					var variantsNode = null;
					if(variantsNodes.length > 0) {
						for(var i = 0; i < variantsNodes.length; i++) {
							variantsId = variantsNodes[i].getAttribute(xmlAttributeId);
							if(variantsId == currentTrackItem.id) {
								variantsNode = variantsNodes[i];
								break;
							}
						}
						if(variantsNode != null) {
							var trackNode = document.getElementById(currentTrackItem.id);
							var canvasNodes = trackNode.getElementsByTagName("canvas");
							showVariant(canvasNodes[0], canvasNodes[1], variantsNode, currentTrackItem.mode);
						} else {
							overScaleShow(currentTrackItem.id);
						}
					} else {
						overScaleShow(currentTrackItem.id);
					}
				} else if(currentTrackItem.dataType == "BED" || currentTrackItem.dataType == "BEDGZ" || currentTrackItem.dataType == "ANNO" || currentTrackItem.dataType == "GRF" || currentTrackItem.dataType == "GDF") {
					var elementsNodes = XMLDoc.getElementsByTagName(xmlTagElements);
					var geneNode;
					var elementsId;
					for(var i = 0; i < elementsNodes.length; i++) {
						elementsId = elementsNodes[i].getAttribute(xmlAttributeId);
						if(elementsId == currentTrackItem.id) {
							geneNode = elementsNodes[i];
							break;
						}
					}
					var refSeqTrackNode = document.getElementById(currentTrackItem.id);
					var refSeqCanvasNodes = refSeqTrackNode.getElementsByTagName("canvas");
					showGene(refSeqCanvasNodes[0], refSeqCanvasNodes[1], geneNode, currentTrackItem.mode);
				} else if(currentTrackItem.dataType == "BW" || currentTrackItem.dataType == "WIG") {
					var elementsNodes = XMLDoc.getElementsByTagName(xmlTagValues);
					var geneNode;
					var elementsId;
					for(var i = 0; i < elementsNodes.length; i++) {
						elementsId = elementsNodes[i].getAttribute(xmlAttributeId);
						if(elementsId == currentTrackItem.id) {
							geneNode = elementsNodes[i];
							break;
						}
					}
					var refSeqTrackNode = document.getElementById(currentTrackItem.id);
					var refSeqCanvasNodes = refSeqTrackNode.getElementsByTagName("canvas");
					showValueCombine(refSeqCanvasNodes[0], refSeqCanvasNodes[1], geneNode, currentTrackItem.mode, "downtop", 100, 50);
				}
			}

			hideLoadingImage(loadingId);
		}
	}
}

function showLoadingImage(id, selector) {
	// Show a loading image above the given id; return's id of div added (so it can be removed when loading is finished).
	// This code was mostly directly copied from hgHeatmap.js, except I also added the "overlay.appendTo("body");"
	// If absolute is TRUE, then we use and absolute reference for the src tag.
	var loadingId = id + "LoadingOverlay";
	// make an opaque overlay to partially hide the image
	var overlay = $("<div></div>").attr("id", loadingId).css("position", "absolute");
	var ele = $(document.getElementById(id));
	overlay.appendTo(selector);
	overlay.css("top", ele.position().top);
	var divLeft = ele.position().left;
	overlay.css("left", divLeft);
	//var width = ele.width() - 5;
	var width = $("#divTrack").width();
	var height = ele.height();
	overlay.width(width);
	overlay.height(height);
	overlay.css("background", "white");
	overlay.css("opacity", 0.55);
	// now add the overlay image itself in the center of the overlay.
	var imgWidth = 220;
	// hardwired based on width of loading.gif
	var imgLeft = (width / 2) - (imgWidth / 2);
	var imgTop = (height / 2 ) - 10;
	var src = "./image/loading.gif";
	$("<img src='" + src + "'/>").css("position", "relative").css('left', imgLeft).css('top', imgTop).appendTo(overlay);
	return loadingId;
}

function hideLoadingImage(id) {
	$(document.getElementById(id)).remove();
}

var chrSelectList = [];
function initialChrSelectList() {
	var i;
	for( i = 0; i <= 24; i++) {
		chrSelectList[i] = [];
		if(i <= 21) {
			chrSelectList[i].num = i + 1;
		} else if(i == 22) {
			chrSelectList[22].num = 'X';
		} else if(i == 23) {
			chrSelectList[23].num = 'Y';
		} else if(i == 24) {
			chrSelectList[24].num = 'M';
		}
		chrSelectList[i].selected = 0;
	}
}

function addExternalUrl() {
	var flag1 = 0, flag2 = 1;
	var chrCheckBoxInput;
	var i;
	if($("#urlInfo tr").length == 1) {
		$("<tr><td><input type=\"text\" style=\"width: 250px;background: #E5E5E5\"\/><\/td><td><input type=\"text\" name=\"chrCheckBoxInput\" readonly=\"readonly\" onclick=\"currentChrSelectInput=this;chrSelectInputClick()\" style=\"width: 150px;background: #E5E5E5\"\/><\/td><td><img alt=\"pic\" src=\"./image/delete.png\" style=\"cursor: pointer\" onclick=\"$(this).parent().parent().remove();opBeforeDelUrl( $(this).parent().parent());\"\/><\/td><\/tr>").appendTo("#urlInfo");
	} else {
		for( i = 0; i <= 24; i++) {
			if(chrSelectList[i].selected == 0) {
				flag1 = 1;
				break;
			}
		}
		chrCheckBoxInput = document.getElementsByName("chrCheckBoxInput");
		for( i = 0; i < chrCheckBoxInput.length; i++) {
			if(chrCheckBoxInput[i].value == "") {
				flag2 = 0;
				break;
			}
		}
		if(flag1 == 1 && flag2 == 1) {
			$("<tr><td><input type=\"text\" style=\"width: 250px;background: #E5E5E5\"\/><\/td><td><input type=\"text\" name=\"chrCheckBoxInput\" readonly=\"readonly\" onclick=\"currentChrSelectInput=this;chrSelectInputClick()\" style=\"width: 150px;background: #E5E5E5\"\/><\/td><td><img alt=\"pic\" src=\"./image/delete.png\" style=\"cursor: pointer\" onclick=\"$(this).parent().parent().remove();opBeforeDelUrl( $(this).parent().parent());\"\/><\/td><\/tr>").appendTo("#urlInfo");
		} else {
			if(flag1 == 0) {
				alert("there is not chromosome number to select!");
			} else {
				alert("Please select the chromosome number in the existed input box!");
			}
		}
	}
}

function opBeforeDelUrl(trNode) {
	var chrInputNode = trNode.find("input")[1];
	var result = chrInputNode.value.split(',');
	for(var i = 0; i < result.length; i++) {
		if(result[i] == 'X') {
			chrSelectList[22].selected = 0;
		} else if(result[i] == 'Y') {
			chrSelectList[23].selected = 0;
		} else if(result[i] == 'M') {
			chrSelectList[24].selected = 0;
		} else {
			chrSelectList[result[i] - 1].selected = 0;
		}
	}
}

function isMemInArray(mem, array) {
	for(var i = 0; i < array.length; i++) {
		if(mem == array[i]) {
			return true;
		}
	}
	return false;
}

var currentChrSelectInput;
function chrSelectInputClick() {
	var divNode = document.getElementById("chrSelectDiv");
	var tableNode = document.getElementById("chrSelectTable");
	var trNode, tdNode, tdInnerHTML;
	var selectedValues;
	var i, j;
	selectedValues = currentChrSelectInput.value.split(',');
	//alert(currentChrSelectInput.value);
	tableNode.innerHTML = "";
	for( i = 0, j = 0; i <= 24; i++) {
		if(chrSelectList[i].selected == 0 || isMemInArray(chrSelectList[i].num, selectedValues)) {
			if(j % 5 == 0) {
				trNode = tableNode.insertRow(-1);
			}
			tdNode = trNode.insertCell(-1);
			tdNode.align = "left";
			tdNode.style.width = 38;
			if(chrSelectList[i].selected == 1) {
				tdInnerHTML = "<input type=\"checkbox\" name=\"chk\" value=\"" + chrSelectList[i].num + "\" checked=\"true\"\/>" + chrSelectList[i].num;
			} else {
				tdInnerHTML = "<input type=\"checkbox\" name=\"chk\" value=\"" + chrSelectList[i].num + "\"\/>" + chrSelectList[i].num;
			}
			tdNode.innerHTML = tdInnerHTML;
			j++;
		}
	}
	document.getElementById("checkAll").checked = false;
	$(divNode).css("top", $(currentChrSelectInput).position().top + 20);
	$(divNode).css("left", $(currentChrSelectInput).position().left);
	$(divNode).css("display", "block");
}

function chrSelectDivOkButton() {
	var i;
	var result = [];
	var chk = document.getElementsByName("chk");
	for( i = 0; i < chk.length; i++) {
		if(chk[i].checked) {
			result.push(chk[i].value);
			if(chk[i].value == 'X') {
				chrSelectList[22].selected = 1;
			} else if(chk[i].value == 'Y') {
				chrSelectList[23].selected = 1;
			} else if(chk[i].value == 'M') {
				chrSelectList[24].selected = 1;
			} else {
				chrSelectList[chk[i].value - 1].selected = 1;
			}
		} else {
			if(chk[i].value == 'X') {
				chrSelectList[22].selected = 0;
			} else if(chk[i].value == 'Y') {
				chrSelectList[23].selected = 0;
			} else if(chk[i].value == 'M') {
				chrSelectList[24].selected = 0;
			} else {
				chrSelectList[chk[i].value - 1].selected = 0;
			}
		}
	}
	currentChrSelectInput.value = result.join(",");
	document.getElementById("chrSelectDiv").style.display = "none";
}

function chrSelectDivCancelButton() {
	document.getElementById("chrSelectDiv").style.display = "none";
}

function chrCheckAll() {
	var chrCheckList = document.getElementsByName("chk");
	if($("#checkAll").attr("checked")){
		for(var i = 0; i < chrCheckList.length; i++) {
			chrCheckList[i].checked = true;
		}
	}else{
		for(var i = 0; i < chrCheckList.length; i++) {
			chrCheckList[i].checked = false;
		}
	}
}

function clearAllInput() {
	var i;
	var urlInfoTableNode = document.getElementById("urlInfo");
	var trLength = urlInfoTableNode.getElementsByTagName("tr").length;
	document.getElementById("customTrackId").value = "";
	for( i = 0; i <= 24; i++) {
		chrSelectList[i].selected = 0;
	}
	i = trLength;
	while(i > 2) {
		urlInfoTableNode.deleteRow(-1);
		i--;
	}
	var inputNodes = urlInfoTableNode.getElementsByTagName("input");
	for( i = 0; i < inputNodes.length; i++) {
		inputNodes[i].value = "";
	}
	document.getElementById("checkAll").checked = "false";
}

function customTrackSubmit() {
	var customTrackId = document.getElementById("customTrackId").value;
	var customTrackDisplayModeNode = document.getElementById("customTrackDisplayMode");
	var customTrackDataTypeNode = document.getElementById("customTrackDataType");
	var urlInfoTableNode = document.getElementById("urlInfo");
	var customTrackDisplayMode = customTrackDisplayModeNode.options[customTrackDisplayModeNode.selectedIndex].childNodes[0].nodeValue;
	var customTrackDataType = customTrackDataTypeNode.options[customTrackDataTypeNode.selectedIndex].childNodes[0].nodeValue;
	var urlInfoInputNodes = urlInfoTableNode.getElementsByTagName("input");
	var urlInfoTableTrNodes = urlInfoTableNode.getElementsByTagName("tr");
	var urlStr, chrArray;
	var i, j, flag = 0;
	if(/^_(.|\n)*/.test(customTrackId)){
		alert("track name cannot start with \"_\"!");
		return;
	}
	for( i = 0; i < urlInfoInputNodes.length; i++) {
		if(urlInfoInputNodes[i].value == "") {
			flag = 1;
			break;
		}
	}
	if(customTrackId == "" || flag == 1) {
		alert("Please input complete information or delete useless url item!");
		return;
	}

	for( i = 0; i < trackItems.length; i++) {
		if(customTrackId == trackItems[i].id) {
			alert("Your custom track name is existed, please input another name which is not used!");
			return;
		}
	}

	var temp_trackItemsLength = trackItems.length;
	trackItems[temp_trackItemsLength] = [];
	trackItems[temp_trackItemsLength].id = customTrackId;
	trackItems[temp_trackItemsLength].mode = customTrackDisplayMode;
	trackItems[temp_trackItemsLength].dataType = customTrackDataType;
	trackItems[temp_trackItemsLength].isServer = 0;

	var cookieStr, serverParaStr, urls = "";
	if(urlInfoTableTrNodes.length == 2 && urlInfoTableTrNodes[1].getElementsByTagName("input")[1].value.split(",").length == 25) {
		urls = urlInfoTableTrNodes[1].getElementsByTagName("input")[0].value;
	} else {
		for( i = 1; i < urlInfoTableTrNodes.length; i++) {
			urlStr = urlInfoTableTrNodes[i].getElementsByTagName("input")[0].value;
			chrArray = urlInfoTableTrNodes[i].getElementsByTagName("input")[1].value.split(",");
			for( j = 0; j < chrArray.length; j++) {
				urls = urls + "chr";
				urls = urls + chrArray[j];
				urls = urls + ":";
				urls = urls + urlStr;
				urls = urls + ";";
			}
		}
	}
	serverParaStr = "action=addExternals&" + "tracks=" + customTrackId + "&modes=" + customTrackDisplayMode + "&types=" + customTrackDataType + "&links=" + urls;
	if($.cookie("customTrackList")) {
		cookieStr = $.cookie("customTrackList") + ",";
	} else {
		cookieStr = "";
	}
	cookieStr = cookieStr + customTrackId;
	cookieStr = cookieStr + ":";
	cookieStr = cookieStr + customTrackDisplayMode;
	cookieStr = cookieStr + ":";
	cookieStr = cookieStr + customTrackDataType;
	$.cookie("customTrackList", cookieStr, {
		expires : 10 / 24
	});
	

	if(trackItems[temp_trackItemsLength].mode != "hide") {
		createTrack(trackItems[temp_trackItemsLength].id, trackItems[temp_trackItemsLength].mode);
	}
	currentTrackItem = trackItems[temp_trackItemsLength];
	XMLHttpReq.onreadystatechange = function() {
		handleOnchange(currentTrackItem);
	};
	XMLHttpReq.open("GET", "servlet/test.do?" + serverParaStr, true);
	XMLHttpReq.send(null);

	tb_remove();
	trackItems_setting2();
}

$(document).ready(function(){
	$("#customtrackByconfigSubmitBtn").bind("click", customtrack_submit_byConfiguretext);
	$("#customtrackByConfigClearBtn").bind("click", customtrack_clear_byConfiguretext);
});
$(document).ready(function(){
	$("#customtrackconfigformat").tooltip();
});
var customtracks_configtext = "";
var customtracks_index_in_trackItems;
function customtrack_submit_byConfiguretext(){
	var configText = document.getElementById("uploadfile_configureText").value;
	if(configText){
		var test_configText = test_customTrackConfigureText(configText);
		if(test_configText.pass){
			customtracks_configtext = configText;
			customtracks_configtext_setHTMLandJS(configText);
			customtracks_configtext_HttpRequest(configText);
		}else{
			var errorStr = "";
			if(!test_configText.items.num){
				errorStr = errorStr + "the number of the parameters is mismatch!\n";
			}else if(! test_configText.items.format){
				errorStr = errorStr + "the format of the parameters is wrong!\n";
			}else if(! test_configText.items.tracks.tracks_set){
				errorStr = errorStr + "the format of parameter tracks is wrong!\n";
			}else if(! test_configText.items.tracks.tracks_value){
				if(test_configText.items.tracks.errorDescription != ""){
					errorStr = errorStr + test_configText.items.tracks.errorDescription;
				}else{
					errorStr = errorStr + "the value of parameter tracks is wrong!\n";
				}
			}else if(! test_configText.items.modes.modes_set){
				errorStr = errorStr + "the format of parameter modes is wrong!\n";
			}else if(! test_configText.items.modes.modes_value){
				errorStr = errorStr + "the value of parameter modes is wrong!\n";
			}else if(! test_configText.items.Types.Types_set){
				errorStr = errorStr + "the format of parameter types is wrong!\n";
			}else if(! test_configText.items.Types.Types_value){
				errorStr = errorStr + "the value of parameter types is wrong!\n";
			}else if(! test_configText.items.Links.Links_set){
				errorStr = errorStr + "the format of parameter links is wrong!\n";
			}else if(! test_configText.items.Links.Links_value){
				errorStr = errorStr + "the value of parameter links is wrong!\n";
			}
			alert(errorStr);
		}
	}else{
		alert("Please input your configure text!");
	}
}

function customtracks_configtext_HttpRequest(configtext){
	var url = "servlet/test.do?action=addExternals&" + configtext;
	XMLHttpReq.onreadystatechange = handle_customtracks_configtext_HttpRequest;
	XMLHttpReq.open("GET", url, true);
	XMLHttpReq.send(null);
}

function handle_customtracks_configtext_HttpRequest(){
	if(XMLHttpReq.readyState == 4) {
		if(XMLHttpReq.status == 200) {
			var XMLDoc = XMLHttpReq.responseXML;
			for(var i = customtracks_index_in_trackItems; i < trackItems.length; i++) {
				show_trackItem(trackItems[i], XMLDoc);
			}
		}
	}
}

function show_trackItem(currentTrackItem,XMLDoc) {
	if(currentTrackItem.mode != "hide") {
		if(currentTrackItem.dataType == "BAM") {
			var readNodes = XMLDoc.getElementsByTagName(xmlTagReads);
			var readsNode;
			var readsId;
			var i;
			var refSeqTrackNode = document.getElementById(currentTrackItem.id);
			var refSeqCanvasNodes = refSeqTrackNode.getElementsByTagName("canvas");
			for( i = 0; i < readNodes.length; i++) {
				readsId = readNodes[i].getAttribute(xmlAttributeId);
				if(readsId == currentTrackItem.id) {
					readsNode = readNodes[i];
					break;
				}
			}
			if(i == readNodes.length) {
				readNodes = XMLDoc.getElementsByTagName(xmlTagValues);
				for( i = 0; i < readNodes.length; i++) {
					readsId = readNodes[i].getAttribute(xmlAttributeId);
					if(readsId == currentTrackItem.id) {
						readsNode = readNodes[i];
						break;
					}
				}
				showPositiveValueCombine(refSeqCanvasNodes[0], refSeqCanvasNodes[1], readsNode, currentTrackItem.mode, "topdown", "normalRatio", 70, 50);
			} else {
				showRead(refSeqCanvasNodes[0], refSeqCanvasNodes[1], readsNode, currentTrackItem.mode, false);
			}
		} else if(currentTrackItem.dataType == "VCF" || currentTrackItem.dataType == "GVF") {
			var variantsNodes = XMLDoc.getElementsByTagName(xmlTagVariants);
			var variantsId;
			var variantsNode = null;
			if(variantsNodes.length > 0) {
				for(var i = 0; i < variantsNodes.length; i++) {
					variantsId = variantsNodes[i].getAttribute(xmlAttributeId);
					if(variantsId == currentTrackItem.id) {
						variantsNode = variantsNodes[i];
						break;
					}
				}
				if(variantsNode != null) {
					var trackNode = document.getElementById(currentTrackItem.id);
					var canvasNodes = trackNode.getElementsByTagName("canvas");
					showVariant(canvasNodes[0], canvasNodes[1], variantsNode, currentTrackItem.mode);
				} else {
					overScaleShow(currentTrackItem.id);
				}
			} else {
				overScaleShow(currentTrackItem.id);
			}
		} else if(currentTrackItem.dataType == "BED" || currentTrackItem.dataType == "BEDGZ" || currentTrackItem.dataType == "ANNO" || currentTrackItem.dataType == "GRF" || currentTrackItem.dataType == "GDF") {
			var elementsNodes = XMLDoc.getElementsByTagName(xmlTagElements);
			var geneNode;
			var elementsId;
			for(var i = 0; i < elementsNodes.length; i++) {
				elementsId = elementsNodes[i].getAttribute(xmlAttributeId);
				if(elementsId == currentTrackItem.id) {
					geneNode = elementsNodes[i];
					break;
				}
			}
			var refSeqTrackNode = document.getElementById(currentTrackItem.id);
			var refSeqCanvasNodes = refSeqTrackNode.getElementsByTagName("canvas");
			showGene(refSeqCanvasNodes[0], refSeqCanvasNodes[1], geneNode, currentTrackItem.mode);
		} else if(currentTrackItem.dataType == "BW" || currentTrackItem.dataType == "WIG") {
			var elementsNodes = XMLDoc.getElementsByTagName(xmlTagValues);
			var geneNode;
			var elementsId;
			for(var i = 0; i < elementsNodes.length; i++) {
				elementsId = elementsNodes[i].getAttribute(xmlAttributeId);
				if(elementsId == currentTrackItem.id) {
					geneNode = elementsNodes[i];
					break;
				}
			}
			var refSeqTrackNode = document.getElementById(currentTrackItem.id);
			var refSeqCanvasNodes = refSeqTrackNode.getElementsByTagName("canvas");
			showValueCombine(refSeqCanvasNodes[0], refSeqCanvasNodes[1], geneNode, currentTrackItem.mode, "downtop", 100, 50);
		}
	}
}

function customtracks_configtext_setHTMLandJS(configText){
	var trackItems_length = trackItems.length;
	customtracks_index_in_trackItems = trackItems.length;
	var paramsset_array = configText.split('&');
	var tracks_array, modes_array, Types_array, Links_array;
	var i;
	for(i=0; i<paramsset_array.length;i++){
		if(paramsset_array[i].indexOf("tracks")!=-1){
			tracks_array = paramsset_array[i].split("=")[1].split(",");
		}else if(paramsset_array[i].indexOf("modes")!=-1){
			modes_array = paramsset_array[i].split("=")[1].split(",");
		}else if(paramsset_array[i].indexOf("types")!=-1){
			Types_array = paramsset_array[i].split("=")[1].split(",");
		}else if(paramsset_array[i].indexOf("links")!=-1){
			Links_array = paramsset_array[i].split("=")[1].split(",");
		}
	}
	var cookieStr;
	if($.cookie("customTrackList")) {
		cookieStr = $.cookie("customTrackList") + ",";
	} else {
		cookieStr = "";
	}
	for(i=0; i<tracks_array.length; i++){
		trackItems[trackItems.length] = [];
		trackItems[trackItems.length - 1].id = tracks_array[i];
		trackItems[trackItems.length - 1].mode = modes_array[i];
		trackItems[trackItems.length - 1].dataType = Types_array[i];
		trackItems[trackItems.length - 1].isServer = 0;
		
		cookieStr = cookieStr + tracks_array[i];
		cookieStr = cookieStr + ":";
		cookieStr = cookieStr + modes_array[i];
		cookieStr = cookieStr + ":";
		cookieStr = cookieStr + Types_array[i];
		cookieStr = cookieStr + ",";
		
		if(modes_array[i] != "hide") {
			createTrack(tracks_array[i],modes_array[i]);
		}
		
		if(Types_array[i] == "VCF" || Types_array[i] == "GVF") {
			personalPannel.personalTrackItems.Pvars.push("_" + tracks_array[i]);
		}
	}
	
	cookieStr = cookieStr.replace(/,$/gi,"");
	$.cookie("customTrackList", cookieStr, {
		expires : 10 / 24
	});
	
	tb_remove();
	trackItems_setting2();
}

function test_customTrackConfigureText(configText){
	var tracks_value_reg = new RegExp("[^_]([^,])+(,[^_]([^,])+)*");
	
	var modes_value_reg = new RegExp("(hide|dense|pack)(,(hide|dense|pack))*");
	var Types_value_reg = new RegExp("(BAM|BB|GDF|BEDGZ|ANNO|BW|GVF|VCF|GRF)(,(BAM|BB|GDF|BEDGZ|ANNO|BW|GVF|VCF|GRF))*");
	var Links_value1_reg = new RegExp("(chr([1-9]|1[0-9]|2[0-2]|X|Y|M):([^;,]+);)+");
	var Links_value2_reg = new RegExp("[^,;]+");
	
	var tracks_set_reg = new RegExp("tracks=(.|\n)+");
	var modes_set_reg = new RegExp("modes=(.|\n)+");
	var Types_set_reg = new RegExp("types=(.|\n)+");
	var Links_set_reg = new RegExp("links=(.|\n)+");
	
	var config_reg = new RegExp("(.|\n)+&(.|\n)+&(.|\n)+&(.|\n)+");
	
	var testResult = {};
	testResult.pass = false;
	testResult.items = {};
	testResult.items.num = false;
	testResult.items.format = false;
	testResult.items.tracks = {};
	testResult.items.modes = {};
	testResult.items.Types = {};
	testResult.items.Links = {};
	testResult.items.tracks.tracks_set = false;
	testResult.items.modes.modes_set = false;
	testResult.items.Types.Types_set = false;
	testResult.items.Links.Links_set = false;
	testResult.items.tracks.tracks_value = false;
	testResult.items.tracks.errorDescription = "";
	testResult.items.modes.modes_value = false;
	testResult.items.Types.Types_value = false;
	testResult.items.Links.Links_value = false;
	
	var configTextObj = {};
	configTextObj.configText = configText;
	configTextObj.tracks = {};
	configTextObj.tracks.configText = "";
	configTextObj.tracks.value = "";
	configTextObj.tracks.num = 0;
	configTextObj.modes = {};
	configTextObj.modes.configText = "";
	configTextObj.modes.value = "";
	configTextObj.modes.num = 0;
	configTextObj.Types = {};
	configTextObj.Types.configText = "";
	configTextObj.Types.value = "";
	configTextObj.Types.num = 0;
	configTextObj.Links = {};
	configTextObj.Links.configText = "";
	configTextObj.Links.value = "";
	configTextObj.Links.num = 0;
	
	var configArray = configText.split("&");
	var i, j;
	for(i=0; i<configArray.length;i++){
		if(configArray[i].indexOf("tracks")!=-1){
			configTextObj.tracks.configText = configArray[i];
			configTextObj.tracks.value = configArray[i].split("=")[1];
			configTextObj.tracks.num = configTextObj.tracks.value.split(",").length;
		}else if(configArray[i].indexOf("modes")!=-1){
			configTextObj.modes.configText = configArray[i];
			configTextObj.modes.value = configArray[i].split("=")[1];
			configTextObj.modes.num = configTextObj.modes.value.split(",").length;
		}else if(configArray[i].indexOf("types")!=-1){
			configTextObj.Types.configText = configArray[i];
			configTextObj.Types.value = configArray[i].split("=")[1];
			configTextObj.Types.num = configTextObj.Types.value.split(",").length;
		}else if(configArray[i].indexOf("links")!=-1){
			configTextObj.Links.configText = configArray[i];
			configTextObj.Links.value = configArray[i].split("=")[1];
			configTextObj.Links.num = configTextObj.Links.value.split(",").length;
		}
	}
	
	testResult.items.format = config_reg.test(configText);
	
	if(configTextObj.modes.num == configTextObj.tracks.num && configTextObj.Types.num == configTextObj.tracks.num && configTextObj.Links.num == configTextObj.tracks.num) {
		testResult.items.num = true;
	}
	
	testResult.items.tracks.tracks_set = tracks_set_reg.test(configTextObj.tracks.configText);
	testResult.items.modes.modes_set = modes_set_reg.test(configTextObj.modes.configText);
	testResult.items.Types.Types_set = Types_set_reg.test(configTextObj.Types.configText);
	testResult.items.Links.Links_set = Links_set_reg.test(configTextObj.Links.configText);
	testResult.items.tracks.tracks_value = tracks_value_reg.test(configTextObj.tracks.value);
	testResult.items.modes.modes_value = modes_value_reg.test(configTextObj.modes.value);
	testResult.items.Types.Types_value = Types_value_reg.test(configTextObj.Types.value);
	
	var customtrack_nameList = configTextObj.tracks.value.split(",");
	
	for(i = 0; i < customtrack_nameList.length; i++){
		if(/^_/.test(customtrack_nameList[i])){
			testResult.items.tracks.tracks_value = false;
			testResult.items.tracks.errorDescription = "track name cannot start width \"_\"!";
			break;
		}
		for(j = 0; j < trackItems.length; j++){
			if(trackItems[j].id == customtrack_nameList[i]){
				testResult.items.tracks.tracks_value = false;
				testResult.items.tracks.errorDescription = "The track name:" + trackItems[j].id + " already exists!";
				break;
			}
		}
	}
	
	for(i = 0; i < customtrack_nameList.length - 1; i++){
		for(j = i + 1; j < customtrack_nameList.length; j++){
			if(customtrack_nameList[i] == customtrack_nameList[j]){
				testResult.items.tracks.tracks_value = false;
				testResult.items.tracks.errorDescription = "There is the same track names in the value of tracks parameter!";
				break;
			}
		}
	}
	
	var Links_value_array = configTextObj.Links.value.split(",");
	var temp_bool_value = false;
	for(i =0; i<Links_value_array.length; i++){
		if(Links_value_array[i].indexOf(";")!= -1){
			temp_bool_value = Links_value1_reg.test(Links_value_array[i]);
		}else{
			temp_bool_value = Links_value2_reg.test(Links_value_array[i]);
		}
		if(!temp_bool_value){
			break;
		}
	}
	if(i<Links_value_array.length){
		testResult.items.Links.Links_value = false;
	}else{
		testResult.items.Links.Links_value = true;
	}
	
	
	if(testResult.items.format && testResult.items.num && testResult.items.tracks.tracks_set && testResult.items.modes.modes_set && testResult.items.Links.Links_set && testResult.items.tracks.tracks_value && testResult.items.modes.modes_value && testResult.items.Types.Types_value && testResult.items.Links.Links_value) {
		testResult.pass = true;
	} else {
		testResult.pass = false;
	}
	
	return testResult;
}

function customtrack_clear_byConfiguretext(){
	document.getElementById("uploadfile_configureText").value = "";
}

/*****************************************************START: the options and visualisition cytobands************************************************************************/

/*cytoBand is the g band
 width of cytoband track in the page is 1000pixels
 height is 15px*/
function showCytoband(canvasNode, cytobandsNode) {
	var cytobandNodes = cytobandsNode.getElementsByTagName(xmlTagCytoBand);
	var cytobands = [];
	var cytobandTrackWidth = 1000, chrLen, i;
	var bandRelativeFrom, bandRelativeTo, bandRelativeWidth;
	if(cytobandNodes.length == 0) {
		return;
	}
	for( i = 0; i < cytobandNodes.length; i++) {
		cytobands[i] = [];
		cytobands[i].id = cytobandNodes[i].getAttribute(xmlAttributeId);
		cytobands[i].gieStain = cytobandNodes[i].getAttribute(xmlAttribute_gieStain);
		cytobands[i].from = cytobandNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
		cytobands[i].to = cytobandNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
	}
	chrLen = parseInt(cytobands[cytobands.length - 1].to);
	if(canvasNode.getContext) {
		var ctx = canvasNode.getContext('2d');
		ctx.clearRect(0, 0, 1000, 15);
		for( i = 0; i < cytobandNodes.length; i++) {
			bandRelativeFrom = cytobands[i].from * 1000 / chrLen;
			bandRelativeWidth = (cytobands[i].to - cytobands[i].from + 1) * 1000 / chrLen;
			if(cytobands[i].gieStain == "acen") {
				ctx.fillStyle = "#556B2F";
				if(i + 1 < cytobandNodes.length && cytobands[i + 1].gieStain == "acen") {
					ctx.beginPath();
					ctx.moveTo(bandRelativeFrom, 0);
					ctx.lineTo(bandRelativeFrom + bandRelativeWidth, 8);
					ctx.lineTo(bandRelativeFrom, 15);
					ctx.closePath();
					ctx.fill();

					ctx.fillStyle = "#000";
					ctx.beginPath();
					ctx.moveTo(bandRelativeFrom, 0);
					ctx.lineTo(bandRelativeFrom + bandRelativeWidth, 8);
					ctx.closePath();
					ctx.stroke();

					ctx.beginPath();
					ctx.moveTo(bandRelativeFrom + bandRelativeWidth, 8);
					ctx.lineTo(bandRelativeFrom, 15);
					ctx.closePath();
					ctx.stroke();
				} else {
					ctx.beginPath();
					ctx.moveTo(bandRelativeFrom, 8);
					ctx.lineTo(bandRelativeFrom + bandRelativeWidth, 0);
					ctx.lineTo(bandRelativeFrom + bandRelativeWidth, 15);
					ctx.closePath();
					ctx.fill();

					ctx.fillStyle = "#000";
					ctx.beginPath();
					ctx.moveTo(bandRelativeFrom, 8);
					ctx.lineTo(bandRelativeFrom + bandRelativeWidth, 0);
					ctx.closePath();
					ctx.stroke();

					ctx.beginPath();
					ctx.moveTo(bandRelativeFrom, 8);
					ctx.lineTo(bandRelativeFrom + bandRelativeWidth, 15);
					ctx.closePath();
					ctx.stroke();
				}
			} else {
				switch(cytobands[i].gieStain) {
					case "gvar":
						ctx.fillStyle = "#8B8B00";
						break;
					case "stalk":
						ctx.fillStyle = "#B22222";
						break;
					case "gneg":
						ctx.fillStyle = "#fff";
						break;
					case "gpos25":
						ctx.fillStyle = "#C2C2C2";
						break;
					case "gpos50":
						ctx.fillStyle = "#8A8A8A";
						break;
					case "gpos75":
						ctx.fillStyle = "#4D4D4D";
						break;
					case "gpos100":
						ctx.fillStyle = "#000";
						break;
				}
				ctx.fillRect(bandRelativeFrom, 0, bandRelativeWidth, 15);
				ctx.fillStyle = "#000";
				ctx.fillRect(bandRelativeFrom, 0, bandRelativeWidth, 1);
				ctx.fillRect(bandRelativeFrom, 14, bandRelativeWidth, 1);
				if(i == 0) {
					ctx.fillRect(bandRelativeFrom, 0, 1, 15);
				} else if(i == cytobandNodes.length - 1) {
					ctx.fillRect(cytobands[i].to / chrLen * 1000 - 1, 0, 1, 15);
				}
				if(ctx.measureText(cytobands[i].id).width < bandRelativeWidth) {
					if(cytobands[i].gieStain == "gpos100" || cytobands[i].gieStain == "gpos75") {
						ctx.fillStyle = "#fff";
					} else {
						ctx.fillStyle = "#000";
					}
					ctx.textAlign = "center";
					ctx.textBaseline = "middle";
					ctx.fillText(cytobands[i].id, bandRelativeFrom + bandRelativeWidth / 2, 8);
					ctx.textAlign = "start";
					ctx.textBaseline = "alphabetic";
					ctx.fillStyle = "#000";
				}
			}
		}
	}
}

function drawScaleboxOnCytobandsImg(temp_searchLength_user, temp_start_user){
	if($("#scaleBoxOnCytobandsImg")){
		$("#scaleBoxOnCytobandsImg").remove();
	}
	var cytobandsImg_width = $("#cytobandsCanvas").width();
	var scaleBox_width = temp_searchLength_user/ chrLength * cytobandsImg_width;
	var scaleBox_left = temp_start_user / chrLength * cytobandsImg_width + $("#cytobandsCanvas").position().left;
	var scaleBox_top = $("#cytobandsCanvas").position().top - 3;
	var scaleBox_height = $("#cytobandsCanvas").height() + 2*2;
	var scaleBox = $("<div></div>").attr("id", "scaleBoxOnCytobandsImg").css("position", "absolute");
	scaleBox.appendTo("body");
	scaleBox.css({
		"left": scaleBox_left,
		"top" : scaleBox_top,
		"width": scaleBox_width - 2,
		"height": scaleBox_height,
		"border": "solid 1px #000",
		"background": "rgba(255,255,255,0)",
		"z-index": 5
	});
}

$(document).ready(function() {
	$("#cytobandsCanvas").imgAreaSelect({
		classPrefix : "chrImgareaselect",
		handles : false,
		resizable : false,
		movable : false,
		autoHide : true,
		minHeight : $("#cytobandsCanvas").height(),
		onSelectChange : function(img, selection) {
			var cytobandsImg_width = $("#cytobandsCanvas").width();
			var tempstartIndex = Math.round(selection.x1 * chrLength / cytobandsImg_width);
			var tempendIndex = Math.round(selection.x2 * chrLength / cytobandsImg_width);
			showuserSearchIndex(tempstartIndex, tempendIndex);
			setSliderValue(tempendIndex - tempstartIndex + 1);
		},
		onSelectEnd : function(img, selection) {
			var cytobandsImg_width = $("#cytobandsCanvas").width();
			start_user = Math.round(selection.x1 * chrLength / cytobandsImg_width);
			end_user = Math.round(selection.x2 * chrLength / cytobandsImg_width);
			searchLength_user = end_user - start_user + 1;
			startIndex = start_user - searchLength_user;
			endIndex = end_user + searchLength_user;
			searchLength = endIndex - startIndex + 1;
			drawScaleboxOnCytobandsImg(searchLength_user, start_user);
			setSliderValue(searchLength_user);
			showuserSearchIndex(start_user, end_user);
			showRef();
		}
	});
});

//为了解决cytobands图片中的选定区域中不可选的问题而增加的处理，
//而在cytobands上添加的原来的处理方法实际上可以删掉
$(document).ready(function(){
	var cytobandsImg_width = $("#cytobandsCanvas").width();
	var cytobandsImg_height = $("#cytobandsCanvas").height();
	var cytobandsImg_left = $("#cytobandsCanvas").position().left - 4;
	var cytobandsImg_top = $("#cytobandsCanvas").position().top;
	var overlay_above_cytobandsImg = $("<div></div>").attr("id", "overlay_above_cytobandsImg").css("position", "absolute");
	overlay_above_cytobandsImg.appendTo("body");
	overlay_above_cytobandsImg.css({
		"left": cytobandsImg_left,
		"top": cytobandsImg_top,
		"width": cytobandsImg_width,
		"height": cytobandsImg_height,
		"z-index": 10,
		"border": "none",
		"opacity": 0
	});
	
	$("#overlay_above_cytobandsImg").imgAreaSelect({
		classPrefix : "chrImgareaselect",
		handles : false,
		resizable : false,
		movable : false,
		autoHide : true,
		minHeight : $("#overlay_above_cytobandsImg").height(),
		onSelectChange : function(img, selection) {
			var cytobandsImg_width = $("#overlay_above_cytobandsImg").width();
			var tempstartIndex = Math.round(selection.x1 * chrLength / cytobandsImg_width);
			var tempendIndex = Math.round(selection.x2 * chrLength / cytobandsImg_width);
			showuserSearchIndex(tempstartIndex, tempendIndex);
			setSliderValue(tempendIndex - tempstartIndex + 1);
		},
		onSelectEnd : function(img, selection) {
			var cytobandsImg_width = $("#overlay_above_cytobandsImg").width();
			start_user = Math.round(selection.x1 * chrLength / cytobandsImg_width);
			end_user = Math.round(selection.x2 * chrLength / cytobandsImg_width);
			searchLength_user = end_user - start_user + 1;
			startIndex = start_user - searchLength_user;
			endIndex = end_user + searchLength_user;
			searchLength = endIndex - startIndex + 1;
			drawScaleboxOnCytobandsImg(searchLength_user, start_user);
			setSliderValue(searchLength_user);
			showuserSearchIndex(start_user, end_user);
			showRef();
		}
	});
});

/*********************************************************END: the options and visualisition cytobands********************************************************************/

function initPvar(){
	var url = "servlet/test.do?action=initPvar&modes=pack&tracks=1000genome_CEU&id=NA12716";
	XMLHttpReq5.onreadystatechange = function(){};
	XMLHttpReq5.open("GET", url, false);
	XMLHttpReq5.send(null);
}

$(document).ready(function() {
	var urlParameter = QueryString();
	if((urlParameter["Chr"] + ".") == "undefined."){
		window.location.href = "browser.html?Chr=chr21&Start=33021623&End=33051544";
		return;
	}
	//initPvar();
	if((urlParameter["Chr"] + ".") != "undefined.") {
		assemblyNum = urlParameter["Assembly"];
		chrNum = urlParameter["Chr"];
		startIndex = parseInt(urlParameter["Start"]);
		endIndex = parseInt(urlParameter["End"]);
		if(startIndex > endIndex) {
			var start2end;
			start2end = endIndex;
			endIndex = startIndex;
			startIndex = start2end;
		}
		if(endIndex - startIndex + 1 < parseInt(trackLength / 10)) {
			var temp_d = parseInt(trackLength / 10) - (endIndex - startIndex + 1);
			if(temp_d % 2 == 0) {
				endIndex = endIndex + temp_d / 2;
				startIndex = startIndex - temp_d / 2;
			} else {
				endIndex = endIndex + parseInt(temp_d / 2);
				startIndex = startIndex - (parseInt(temp_d / 2) + 1)
			}
		}
		searchLength = endIndex - startIndex + 1;
		searchLength_user = Math.round(searchLength / 3);
		start_user = startIndex + searchLength_user;
		end_user = endIndex - searchLength_user;

		/*var assemblySelectNode = document.getElementById("assemblySelect");
		var i = 0;
		for(; i < assemblySelectNode.options.length; i++) {
			if(assemblyNum == assemblySelectNode.options[i].firstChild.nodeValue) {
				assemblySelectNode.selectedIndex = i;
				break;
			}
		}
		setAssemblyRequest();*/
		getChrLengthRequest();
		pageFlag = 1;
	}
});
//search input autocomplete
$(function() { 
    $("#search_field").autocomplete({ 
        source: function(request, response) { 
        	var input = request.term;
        	if(input.length >3)
        	{
        		if((input[0]=='c' || input[0]=='C')&&(input[1]=='h' || input[1]=='H')&&(input[2]=='r' || input[2]=='R'))
    			{
        			if((input[3]=='x' || input[3]=='X')||(input[3]=='y' || input[3]=='Y')||(input[3]=='m' || input[3]=='M')||(input[3]>='1'&&input[3]<='9') )
        			{
        				$("#ui-id-1").css("display", "none");
        				return;
        			}
    			}
        		
        	}
        	$("#ui-id-1").css("left", $("#search_field").position().left - 3);
        	$("#ui-id-1").css("top", $("#search_field").position().top + $("#search_field").outerHeight(true));
        	$("#ui-id-1 li").remove();
			$("#ui-id-1").append("<li><p><center><img src=\"image/ui-anim_basic_16x16.gif\"></img><br>one moment, searching......</center></p></li>");
			$("#ui-id-1").css("display", "block");
			$("#ui-id-1").mouseleave(function(e){
				$("#select_info").css("display", "none");
			});
        	$.ajax({ 
                url: "servlet/test.do?action=findGene&prefix=" + request.term , 
                type: "get",
                dataType: "xml", 
                success: function(xmlResponse) {
                	$("#wait_div").css("display", "none");
                	var hasData = false;
                	var dataNum = 0;
					response($( "Gene", xmlResponse ).map(function() {
						hasData = true;
						dataNum++;
						return{
							id: "li_" + (dataNum-1),
							value: $(this).attr("id"),
							label: $(this).attr("id"),
							desc: $( "Chromosome", this ).text() + ":" 	+ $(xmlTagFrom , this ).text() + "-" + $( xmlTagTo, this ).text()
						};
					}));
					if(!hasData)
					{
						$("#ui-id-1 li").remove();
						$("#ui-id-1").append("<li><p><center>No Result</center></p></li>");
						$("#ui-id-1").css("display", "block");

					}
					$("#ui-id-1").css("left", $("#search_field").position().left - 3);
					$("#ui-id-1").css("width", 239);
                },
                error: function(xmlResponse){
                	//here is you deal code when request returned unsuccessfully.
                	//alert('request error!');
                }
            }); 
        },
        focus: function( event, ui ) {
        	if(ui.item.value==null||ui.item.value=="")
        		return;
        	var top = $("#"+ui.item.id).position().top + $("#ui-id-1").position().top;
        	var left =  parseInt($("#ui-id-1").css("left").split("px")[0]) + parseInt($("#ui-id-1").css("width").split("px")[0]) + 5;
        	$("#select_info").css("display", "block");
			$("#select_info").css("top", top);
			$("#select_info").css("left", left);
			//$("#select_info").css("width", 200);
			//$("#select_info").css("height", 200);
			$("#select_info").css("background-color", "#fff");
			//init the showing panel with waiting image
			
			$("#select_info").html("");
			$("#select_info").append("<br><br><center><image src=\"image/ui-anim_basic_16x16.gif\"></image>");
			$("#select_info").append("<p><centet>one moment, loading......</center></p>");
			$("#select_info").mouseover(function(){
				$("#select_info").css("display", "block");
			});
			$("#select_info").mouseout(function(){
				$("#select_info").css("display", "none");
			});
			//send an ajax request to get the data to generate a picture
			$.ajax({ 
				url: "servlet/test.do?action=getGene&gene=" + ui.item.label,//here is your url 
                type:"get",
                dataType: "xml", 
                success: function(xmlResponse) {
                	$("#select_info").html("");
                	$("Gene", xmlResponse ).map(function() {
                		$("#select_info").append("<div id=\"gene_info_name\">"+$(this).attr("id")+"</div>");
                		if($(this).children().length>0)
                			$("#select_info").append("<table id=\"gene_info_table\"></table>");
                		$(this).children().each(function(){
                			var name = $(this).get(0).tagName;
                			if(name !="Chromosome" && name != xmlTagFrom && name!= xmlTagTo)
                				$("#gene_info_table").append("<tr><td align=\"left\">"+name+": </td><td align=\"left\">"+$(this).text()+"</td></tr>");
                		});
					}); 
                	//here is you deal code when request returned successfully. To use the return data to generate a picture
                },
                error: function(xmlResponse){
                	//here is you deal code when request returned unsuccessfully.
                	//alert('request error!');
                }
            });
			return false;
		},
		close: function( event, ui ) {
			$("#select_info").css("display", "none");
		},
		select: function( event, ui) {
			var temp_searchStr = ui.item.desc + "";
			chrNum = temp_searchStr.split(":")[0];
			chrLength = chr_Lengths[chrNum];
			var temp_scale = temp_searchStr.split(":")[1];
			start_user = parseInt(temp_scale.split("-")[0]);
			end_user = parseInt(temp_scale.split("-")[1]);
			normalSearchIndex();
			showRef();
			/*$.ajax({ 
                url: "servlet/test.do?action=getGene&gene=" + ui.item.label,//here is your url 
                type:"get",
                dataType: "xml", 
                success: function(xmlResponse) { 
                	//here is you deal code when request returned successfully.
                },
                error: function(xmlResponse){
                	//here is you deal code when request returned unsuccessfully.
                	alert(ui.item.desc);
                }
            });*/
		},
    })
    .data( "ui-autocomplete" )._renderItem = function( ul, item ) {
		return $( "<li id=\"" + item.id + "\">" )
			.append( "<a>" + item.label + "<br>" + "<span class=\"searchListDetail\">" +item.desc + "</span>" + "</a>" )
			.appendTo( ul );
	};
});
/*$(document).ready(function(){
	document.getElementById("track-manage-li").onclick = trackItems_setting;
});*/

function trackItems_setting2(){
	var trackGroups = [];
	trackGroups[0] = [];
	trackGroups[0].name = trackItems[0].group;
	trackGroups[0].trackList = [];
	trackGroups[0].trackList.push(0);
	var i,j,k;
	for(i = 1; i< trackItems.length; i++){
		if(trackItems[i].group){
			for(j = 0; j < trackGroups.length; j ++){
				if(trackItems[i].group == trackGroups[j].name){
					trackGroups[j].trackList.push(i);
					break;
				}
			}
			if(j == trackGroups.length){
				trackGroups[trackGroups.length] = [];
				trackGroups[j].name = trackItems[i].group;
				trackGroups[j].trackList = [];
				trackGroups[j].trackList.push(i);
			}
		}else{
			for(j = 0; j < trackGroups.length; j ++){
				if(trackGroups[j].name == "custom tracks"){
					trackGroups[j].trackList.push(i);
					break;
				}
			}
			if(j == trackGroups.length){
				trackGroups[trackGroups.length] = [];
				trackGroups[j].name = "custom tracks";
				trackGroups[j].trackList = [];
				trackGroups[j].trackList.push(i);
			}
		}
	}
	var ts_content_centerObj = document.getElementById("tracksmanageOnmainPannel");//.getElementsByTagName("center")[0]
	ts_content_centerObj.innerHTML = "";
	
	var collapse_expand_center = document.createElement("center");
	collapse_expand_center.innerHTML = "<table style=\" width:500px;text-align:center;\"><tr><td><input id=\"ts_collapseBtn\" type=\"button\" value=\"collapse all\"></td><td><input id=\"ts_expandBtn\" type=\"button\" value=\"expand all\"></td></tr></table>";
	ts_content_centerObj.appendChild(collapse_expand_center);
	$("#ts_collapseBtn").click(function(){
		var temp_group_content_nodes = $(ts_content_centerObj).find(".group_content_div");
		for(i=0;i<temp_group_content_nodes.length;i++){
			if($(temp_group_content_nodes[i]).css("display")!="none"){
				$(temp_group_content_nodes[i]).prev().click();
			}
		}
	});
	$("#ts_expandBtn").click(function(){
		var temp_group_content_nodes = $(ts_content_centerObj).find(".group_content_div");
		for(i=0;i<temp_group_content_nodes.length;i++){
			if($(temp_group_content_nodes[i]).css("display")=="none"){
				$(temp_group_content_nodes[i]).prev().click();
			}
		}
	});
	
	var group_divObj;
	var group_title_divObj, group_content_divObj;
	var group_title_name_centerObj, group_title_name_spanObj, group_title_icon_divObj, group_title_icon_spanObj;
	var group_content_tableObj;
	var trNode, tdNode;
	var checkboxObj, labelObj;
	for(i = 0; i < trackGroups.length; i++){
		group_divObj = document.createElement("div");
		group_divObj.id = trackGroups[i].name + "_group";
		group_divObj.className = "group_div";
		group_title_divObj = document.createElement("div");
		group_title_divObj.id = trackGroups[i].name + "_group_title";
		group_title_divObj.className = "group_title_div";
		group_divObj.appendChild(group_title_divObj);
		group_title_divObj.onclick = function(e){
			var title_div_target = e.target || e.srcElement;
			var group_divObj_parent =  $(title_div_target).parents(".group_div")[0];
			var temp_childs = $(group_divObj_parent).children();
			$(temp_childs[1]).slideToggle();
			if($(group_divObj_parent).find(".group_content_hiddenBtn").length > 0){
				$($(group_divObj_parent).find(".group_content_hiddenBtn")[0]).attr("class","group_content_showBtn");
			}else{
				$($(group_divObj_parent).find(".group_content_showBtn")[0]).attr("class","group_content_hiddenBtn");
			}
		};
		group_title_name_centerObj = document.createElement("center");
		group_title_divObj.appendChild(group_title_name_centerObj);
		group_title_name_spanObj = document.createElement("span");
		group_title_name_spanObj.innerHTML = trackGroups[i].name;
		group_title_name_centerObj.appendChild(group_title_name_spanObj);
		
		group_title_icon_divObj = document.createElement("div");
		group_title_name_centerObj.appendChild(group_title_icon_divObj);
		$(group_title_icon_divObj).css({
			"display":"inline-block",
			"float":"right",
			"padding-right":"3px",
		});
		group_title_icon_spanObj = document.createElement("span");
		if(i==0){
			group_title_icon_spanObj.className = "group_content_hiddenBtn";
		}else{
			group_title_icon_spanObj.className = "group_content_showBtn";
		}
		//group_title_icon_spanObj.className = "group_content_showBtn";
		group_title_icon_divObj.appendChild(group_title_icon_spanObj);
		
		group_content_divObj = document.createElement("div");
		group_content_divObj.id = trackGroups[i].name + "_group_content";
		group_divObj.appendChild(group_content_divObj);
		group_content_tableObj = document.createElement("table");
		group_content_tableObj.id = trackGroups[i].name + "_group_content_table";
		group_content_divObj.appendChild(group_content_tableObj);
		for(j = 0; j < trackGroups[i].trackList.length; j++){
			if(j%5 == 0){
				trNode = group_content_tableObj.insertRow(-1);
			}
			tdNode = trNode.insertCell(-1);
			checkboxObj = document.createElement("input");
			checkboxObj.id = trackItems[trackGroups[i].trackList[j]].id + "-chk";
			checkboxObj.type = "checkbox";
			checkboxObj.name = trackGroups[i].name + "-group";
			checkboxObj.value = trackItems[trackGroups[i].trackList[j]].id;
			if(trackItems[trackGroups[i].trackList[j]].mode != "hide"){
				checkboxObj.checked = true;
			}
			tdNode.appendChild(checkboxObj);
			labelObj = document.createElement("label");
			labelObj.setAttribute("for", trackItems[trackGroups[i].trackList[j]].id + "-chk");
			labelObj.innerHTML = trackItems[trackGroups[i].trackList[j]].id;
			tdNode.appendChild(labelObj);
			
			checkboxObj.onclick = function(event){
				var target = event.target || event.srcElement;
				var temp_trackid = target.getAttribute("value");
				var trNode2, tdNode2, temp_tdNode,temp_checkboxObj, temp_labelObj, temp_group;
				if($(target).attr("checked")){
					trackModeOnchange(temp_trackid, "dense");
				}else{
					var temp_superid = null;
					for(k = 0; k < trackItems.length; k++){
						if(trackItems[k].id == temp_trackid){
							if(trackItems[k].superid){
								temp_superid = trackItems[k].superid;
								if(trackItems[k].group){
									temp_group = trackItems[k].group;
								}else{
									temp_group = "custom tracks";
								}
							}
							break;
						}
					}
					if(temp_superid){
						var sub_track_num = 0;
						for(k = 0; k < trackItems.length; k++){
							if(trackItems[k].superid && trackItems[k].superid == temp_superid){
								sub_track_num++;
							}
						}
						tdNode2 = target.parentNode;
						trNode2 = tdNode2.parentNode;
						if(sub_track_num == 1){
							temp_tdNode = trNode2.insertCell(-1);
							temp_checkboxObj = document.createElement("input");
							temp_checkboxObj.id = temp_superid + "-chk";
							temp_checkboxObj.type = "checkbox";
							temp_checkboxObj.name = temp_group + "-group";
							temp_checkboxObj.value = temp_superid;
							temp_checkboxObj.checked = true;
							temp_tdNode.appendChild(temp_checkboxObj);
							temp_labelObj = document.createElement("label");
							temp_labelObj.setAttribute("for", temp_superid + "-chk");
							temp_labelObj.innerHTML = temp_superid;
							temp_tdNode.appendChild(temp_labelObj);
							temp_checkboxObj.onclick = function(evt){
								var target2 = evt.target || evt.srcElement;
								var temp_trackid2 = target2.getAttribute("value");
								if($(target2).attr("checked")){
									trackModeOnchange(temp_trackid2, "dense");
								}else{
									trackModeOnchange(temp_trackid2, "hide");
								}
							};
							$(tdNode2).remove();
						}else{
							if(tdNode2.cellIndex == 0){
								$(trNode2).remove();
							}else{
								$(tdNode2).remove();
							}
						}
					}
					removeTrack2(temp_trackid)();
				}
			};
		}
		group_content_divObj.className = "group_content_div";
		if(i==0){
			$(group_content_divObj).css("display","block");
		}else{
			$(group_content_divObj).css("display","none");
		}
		//$(group_content_divObj).css("display","none");
		ts_content_centerObj.appendChild(group_divObj);
	}
}

function trackItems_setting(){
	var trackGroups = [];
	trackGroups[0] = [];
	trackGroups[0].name = trackItems[0].group;
	trackGroups[0].trackList = [];
	trackGroups[0].trackList.push(0);
	var i,j,k;
	for(i = 1; i< trackItems.length; i++){
		if(trackItems[i].group){
			for(j = 0; j < trackGroups.length; j ++){
				if(trackItems[i].group == trackGroups[j].name){
					trackGroups[j].trackList.push(i);
					break;
				}
			}
			if(j == trackGroups.length){
				trackGroups[trackGroups.length] = [];
				trackGroups[j].name = trackItems[i].group;
				trackGroups[j].trackList = [];
				trackGroups[j].trackList.push(i);
			}
		}else{
			for(j = 0; j < trackGroups.length; j ++){
				if(trackGroups[j].name == "custom tracks"){
					trackGroups[j].trackList.push(i);
					break;
				}
			}
			if(j == trackGroups.length){
				trackGroups[trackGroups.length] = [];
				trackGroups[j].name = "custom tracks";
				trackGroups[j].trackList = [];
				trackGroups[j].trackList.push(i);
			}
		}
	}
	var trackItems_setting_divObj = document.getElementById("trackItems_setting");
	var ts_content_centerObj = document.getElementById("ts_content");//.getElementsByTagName("center")[0]
	ts_content_centerObj.innerHTML = "";
	var group_divObj;
	var group_title_divObj, group_content_divObj;
	var group_title_name_centerObj, group_title_name_spanObj, group_title_icon_divObj, group_title_icon_spanObj;
	var group_content_tableObj;
	var trNode, tdNode;
	var checkboxObj, labelObj;
	for(i = 0; i < trackGroups.length; i++){
		group_divObj = document.createElement("div");
		group_divObj.id = trackGroups[i].name + "_group";
		group_divObj.className = "group_div";
		group_title_divObj = document.createElement("div");
		group_title_divObj.id = trackGroups[i].name + "_group_title";
		group_title_divObj.className = "group_title_div";
		group_divObj.appendChild(group_title_divObj);
		group_title_divObj.onclick = function(e){
			var title_div_target = e.target || e.srcElement;
			var group_divObj_parent =  $(title_div_target).parents(".group_div")[0];
			var temp_childs = $(group_divObj_parent).children();
			$(temp_childs[1]).slideToggle();
			if($(group_divObj_parent).find(".group_content_hiddenBtn").length > 0){
				$($(group_divObj_parent).find(".group_content_hiddenBtn")[0]).attr("class","group_content_showBtn");
			}else{
				$($(group_divObj_parent).find(".group_content_showBtn")[0]).attr("class","group_content_hiddenBtn");
			}
		};
		group_title_name_centerObj = document.createElement("center");
		group_title_divObj.appendChild(group_title_name_centerObj);
		group_title_name_spanObj = document.createElement("span");
		group_title_name_spanObj.innerHTML = trackGroups[i].name;
		group_title_name_centerObj.appendChild(group_title_name_spanObj);
		
		group_title_icon_divObj = document.createElement("div");
		group_title_name_centerObj.appendChild(group_title_icon_divObj);
		$(group_title_icon_divObj).css({
			"display":"inline-block",
			"float":"right",
			"padding-right":"3px",
		});
		group_title_icon_spanObj = document.createElement("span");
		group_title_icon_spanObj.className = "group_content_showBtn";
		group_title_icon_divObj.appendChild(group_title_icon_spanObj);
		
		group_content_divObj = document.createElement("div");
		group_content_divObj.id = trackGroups[i].name + "_group_content";
		group_divObj.appendChild(group_content_divObj);
		group_content_tableObj = document.createElement("table");
		group_content_tableObj.id = trackGroups[i].name + "_group_content_table";
		group_content_divObj.appendChild(group_content_tableObj);
		for(j = 0; j < trackGroups[i].trackList.length; j++){
			if(j%5 == 0){
				trNode = group_content_tableObj.insertRow(-1);
			}
			tdNode = trNode.insertCell(-1);
			checkboxObj = document.createElement("input");
			checkboxObj.id = trackItems[trackGroups[i].trackList[j]].id + "-chk";
			checkboxObj.type = "checkbox";
			checkboxObj.name = trackGroups[i].name + "-group";
			checkboxObj.value = trackItems[trackGroups[i].trackList[j]].id;
			if(trackItems[trackGroups[i].trackList[j]].mode != "hide"){
				checkboxObj.checked = true;
			}
			tdNode.appendChild(checkboxObj);
			labelObj = document.createElement("label");
			labelObj.setAttribute("for", trackItems[trackGroups[i].trackList[j]].id + "-chk");
			labelObj.innerHTML = trackItems[trackGroups[i].trackList[j]].id;
			tdNode.appendChild(labelObj);
			
			checkboxObj.onclick = function(event){
				var target = event.target || event.srcElement;
				var temp_trackid = target.getAttribute("value");
				var trNode2, tdNode2, temp_tdNode,temp_checkboxObj, temp_labelObj, temp_group;
				if($(target).attr("checked")){
					trackModeOnchange(temp_trackid, "dense");
				}else{
					var temp_superid = null;
					for(k = 0; k < trackItems.length; k++){
						if(trackItems[k].id == temp_trackid){
							if(trackItems[k].superid){
								temp_superid = trackItems[k].superid;
								if(trackItems[k].group){
									temp_group = trackItems[k].group;
								}else{
									temp_group = "custom tracks";
								}
							}
							break;
						}
					}
					if(temp_superid){
						var sub_track_num = 0;
						for(k = 0; k < trackItems.length; k++){
							if(trackItems[k].superid && trackItems[k].superid == temp_superid){
								sub_track_num++;
							}
						}
						tdNode2 = target.parentNode;
						trNode2 = tdNode2.parentNode;
						if(sub_track_num == 1){
							temp_tdNode = trNode2.insertCell(-1);
							temp_checkboxObj = document.createElement("input");
							temp_checkboxObj.id = temp_superid + "-chk";
							temp_checkboxObj.type = "checkbox";
							temp_checkboxObj.name = temp_group + "-group";
							temp_checkboxObj.value = temp_superid;
							temp_checkboxObj.checked = true;
							temp_tdNode.appendChild(temp_checkboxObj);
							temp_labelObj = document.createElement("label");
							temp_labelObj.setAttribute("for", temp_superid + "-chk");
							temp_labelObj.innerHTML = temp_superid;
							temp_tdNode.appendChild(temp_labelObj);
							temp_checkboxObj.onclick = function(evt){
								var target2 = evt.target || evt.srcElement;
								var temp_trackid2 = target2.getAttribute("value");
								if($(target2).attr("checked")){
									trackModeOnchange(temp_trackid2, "dense");
								}else{
									trackModeOnchange(temp_trackid2, "hide");
								}
							};
							$(tdNode2).remove();
						}else{
							if(tdNode2.cellIndex == 0){
								$(trNode2).remove();
							}else{
								$(tdNode2).remove();
							}
						}
					}
					removeTrack2(temp_trackid)();
				}
			};
		}
		group_content_divObj.className = "group_content_div";
		$(group_content_divObj).css("display","none");
		ts_content_centerObj.appendChild(group_divObj);
	}
	var collapse_expand_center = document.createElement("center");
	collapse_expand_center.innerHTML = "<table style=\" width:500px;text-align:center;\"><tr><td><input id=\"ts_collapseBtn\" type=\"button\" value=\"collapse all\"></td><td><input id=\"ts_expandBtn\" type=\"button\" value=\"expand all\"></td></tr></table>";
	ts_content_centerObj.appendChild(collapse_expand_center);
	$("#ts_collapseBtn").click(function(){
		var temp_group_content_nodes = $(ts_content_centerObj).find(".group_content_div");
		for(i=0;i<temp_group_content_nodes.length;i++){
			if($(temp_group_content_nodes[i]).css("display")!="none"){
				$(temp_group_content_nodes[i]).prev().click();
			}
		}
	});
	$("#ts_expandBtn").click(function(){
		var temp_group_content_nodes = $(ts_content_centerObj).find(".group_content_div");
		for(i=0;i<temp_group_content_nodes.length;i++){
			if($(temp_group_content_nodes[i]).css("display")=="none"){
				$(temp_group_content_nodes[i]).prev().click();
			}
		}
	});
	var overlayDIV = document.getElementById("overlayDIV");
	$(overlayDIV).css("display","block");
	var tsLeft = (document.body.clientWidth - $("#trackItems_setting").width())/2;
	var tsTop = (document.body.clientHeight - $("#trackItems_setting").height())/2;
	$("#trackItems_setting").css("left", tsLeft);
	$("#trackItems_setting").css("top", tsTop);
	$("#trackItems_setting").css("display","block");
	document.getElementById("ts_closeBtn").onclick = tracksettingpannel_close;
	overlayDIV.onclick = tracksettingpannel_close;
}
function tracksettingpannel_close(){
	$("#overlayDIV").css("display","none");
	$("#trackItems_setting").css("display","none");
}
function trackItems_setting3(){//individualItems setting
	var pattern = /<.*?>/g;
	XMLHttpReq6.open("GET","servlet/test.do?action=getIndividuals",false);
	XMLHttpReq6.send(null);
	var indslist=XMLHttpReq6.responseText.replace(pattern,"");
	var inds_temp=indslist.split(",");
	var individuals=[];//includes personal TRACKs, each unit has trackname, samples, samples is also array
	for(var idx=0;idx<inds_temp.length;idx++){
		var ind_temp=inds_temp[idx].split(":");
		individuals[individuals.length] = {};
		individuals[individuals.length-1].track = ind_temp[0];
		individuals[individuals.length-1].samples = ind_temp[1].split(";");
	}
	var trackItems_setting_divObj = document.getElementById("indsetWindow");
	var ts_content_centerObj = document.getElementById("IW_content");//.getElementsByTagName("center")[0]
	ts_content_centerObj.innerHTML = "";
	var group_divObj;
	var group_title_divObj, group_content_divObj;
	var group_title_name_centerObj, group_title_name_spanObj, group_title_icon_divObj, group_title_icon_spanObj;
	var group_content_tableObj;
	var trNode, tdNode;
	var radioboxObj, labelObj;
	var i,j,k;
	for(i = 0; i < individuals.length; i++){
		group_divObj = document.createElement("div");
		group_divObj.id = individuals[i].track + "_group";
		group_divObj.className = "group_div";
		group_title_divObj = document.createElement("div");
		group_title_divObj.id = individuals[i].track + "_group_title";
		group_title_divObj.className = "group_title_div";
		group_divObj.appendChild(group_title_divObj);
		group_title_divObj.onclick = function(e){
			var title_div_target = e.target || e.srcElement;
			var group_divObj_parent =  $(title_div_target).parents(".group_div")[0];
			var temp_childs = $(group_divObj_parent).children();
			$(temp_childs[1]).slideToggle();
			if($(group_divObj_parent).find(".group_content_hiddenBtn").length > 0){
				$($(group_divObj_parent).find(".group_content_hiddenBtn")[0]).attr("class","group_content_showBtn");
			}else{
				$($(group_divObj_parent).find(".group_content_showBtn")[0]).attr("class","group_content_hiddenBtn");
			}
		};
		group_title_name_centerObj = document.createElement("center");
		group_title_divObj.appendChild(group_title_name_centerObj);
		group_title_name_spanObj = document.createElement("span");
		group_title_name_spanObj.innerHTML = individuals[i].track;
		group_title_name_centerObj.appendChild(group_title_name_spanObj);
		
		group_title_icon_divObj = document.createElement("div");
		group_title_name_centerObj.appendChild(group_title_icon_divObj);
		$(group_title_icon_divObj).css({
			"display":"inline-block",
			"float":"right",
			"padding-right":"3px",
		});
		group_title_icon_spanObj = document.createElement("span");
		group_title_icon_spanObj.className = "group_content_showBtn";
		group_title_icon_divObj.appendChild(group_title_icon_spanObj);
		
		group_content_divObj = document.createElement("div");
		group_content_divObj.id = individuals[i].track + "_group_content";
		group_divObj.appendChild(group_content_divObj);
		group_content_tableObj = document.createElement("table");
		group_content_tableObj.id = individuals[i].track + "_group_content_table";
		group_content_divObj.appendChild(group_content_tableObj);
		for(j = 0; j < individuals[i].samples.length; j++){
			if(j%9 == 0){
				trNode = group_content_tableObj.insertRow(-1);
			}
			tdNode = trNode.insertCell(-1);
			radioboxObj = document.createElement("input");
			radioboxObj.id = individuals[i].samples[j] + "-chk";
			radioboxObj.type = "radio";
			radioboxObj.name = "individualselection";
			radioboxObj.value = individuals[i].samples[j]+'@'+individuals[i].track;
			if('_' + individuals[i].samples[j] == personalPannel.Pvar.id){
				radioboxObj.checked = true;
			}
			tdNode.appendChild(radioboxObj);
			labelObj = document.createElement("label");
			labelObj.setAttribute("for", individuals[i].samples[j] + "-chk");
			labelObj.innerHTML = individuals[i].samples[j];
			tdNode.appendChild(labelObj);
			
			radioboxObj.onclick = function(event){
				var target = event.target || event.srcElement;
				var Pvar_id = target.getAttribute("value").split('@');
				var trParentNode = document.getElementById("divTrack");
				var trTop = $(trParentNode).position().top - document.body.scrollTop;
				var trLeft = $(trParentNode).position().left;
				if(isMini_personalpannel == 1){
					// if the personal pannel is mini
					var trackTable_width = $("#divTrack").width();
					$(document.getElementById("personalPannel")).css("height", "auto");
					$(document.getElementById("personalPannel")).css("width", trackTable_width);
					$('#showWindowBtn').remove();
				}
				$(document.getElementById("personalPannel")).css("display", "block");
				$(document.getElementById("personalPannel")).css("top", trTop);
				$(document.getElementById("personalPannel")).css("left", trLeft - 1);
				document.getElementById("ppTrackTable").innerHTML = "";
				$(document.getElementById("ppCloseBtn")).unbind();
				$(document.getElementById("ppMin")).unbind();
				$(document.getElementById("ppSet")).unbind();
				$(document.getElementById("ppCloseBtn")).click(removePvar);
				$(document.getElementById("ppMin")).click(miniPersonalPannel);
				$(document.getElementById("ppSet")).click(setPersonalPannel);
				addPvarHttpRequest2(Pvar_id[0],Pvar_id[1]); 
				//to be test whether need closet
			};
		}
		group_content_divObj.className = "group_content_div";
		$(group_content_divObj).css("display","none");
		ts_content_centerObj.appendChild(group_divObj);
	}
}

$(document).ready(function(){
	document.getElementById("helpWindow-li").onclick = helpWindow_alert;
	document.getElementById("indsetWindow-li").onclick = indsetWindow_alert;
	document.getElementById("browseJump").onclick = browseJumpWindow_alert;
	document.getElementById("browseJumpIcon2").onclick = browseJumpWindow_alert;
});

function helpWindow_alert(){
	var overlayDIV = document.getElementById("helpWindow_overlayDIV");
	$(overlayDIV).css("display","block");
	var tsLeft = (document.body.clientWidth - $("#helpWindow").width())/2;
	var tsTop = (document.body.clientHeight - $("#helpWindow").height())/2;
	$("#helpWindow").css("left", tsLeft);
	$("#helpWindow").css("top", tsTop);
	$("#helpWindow").css("display","block");
	document.getElementById("HW_closeBtn").onclick = helpWindow_close;
	overlayDIV.onclick = helpWindow_close;
}
function indsetWindow_alert(){
	var overlayDIV = document.getElementById("indsetWindow_overlayDIV");
	$(overlayDIV).css("display","block");
	var tsLeft = (document.body.clientWidth - $("#indsetWindow").width())/2;
	var tsTop = (document.body.clientHeight - $("#indsetWindow").height())/2;
	$("#indsetWindow").css("left", tsLeft);
	$("#indsetWindow").css("top", tsTop);
	$("#indsetWindow").css("display","block");
	document.getElementById("IW_closeBtn").onclick = indsetWindow_close;
	overlayDIV.onclick = indsetWindow_close;
	trackItems_setting3();
}
function browseJumpWindow_alert(){
	var overlayDIV = document.getElementById("browseJumpWindow_overlayDIV");
	$(overlayDIV).css("display","block");
	var tsLeft = (document.body.clientWidth - $("#browseJumpWindow").width())/2;
	var tsTop = (document.body.clientHeight - $("#browseJumpWindow").height())/2;
	$("#browseJumpWindow").css("left", tsLeft);
	$("#browseJumpWindow").css("top", tsTop);
	$("#browseJumpWindow").css("display","block");
	document.getElementById("BJW_closeBtn").onclick = browseJumpWindow_close;
	overlayDIV.onclick = browseJumpWindow_close;
	loadChrBand();
	//to be set reactions
}

function helpWindow_close(){
	$("#helpWindow_overlayDIV").css("display","none");
	$("#helpWindow").css("display","none");
}
function indsetWindow_close(){
	$("#indsetWindow_overlayDIV").css("display","none");
	$("#indsetWindow").css("display","none");
}
function browseJumpWindow_close(){
	$("#browseJumpWindow_overlayDIV").css("display","none");
	$("#browseJumpWindow").css("display","none");
	var ff = document.getElementById("file_field");
	ff.outerHTML=ff.outerHTML;
	document.getElementById("upload_success").innerHTML="";
	control_scanning=0;
}

$(document).ready(function(){
	$(document.getElementsByClassName("custom-track-li")[0]).bind("click", init_removeExternals_pannel);
	$("#removeexternals_li").bind("click", init_removeExternals_pannel);
	$("#remove_externals_all").bind("click", removeExternals_allselect);
	$("#remove_externals_btn").bind("click", removeExternals);
});

function init_removeExternals_pannel(){
	var i, external_num;
	var externals_container_divObj = document.getElementById("externals_container");
	externals_container_divObj.innerHTML = "";
	var externals_tableObj = document.createElement("table");
	externals_container_divObj.appendChild(externals_tableObj);
	var checkboxObj, labelObj, trNode, tdNode;
	external_num = 0;
	for(i = 0; i < trackItems.length; i++){
		if(trackItems[i].isServer == 0){
			if(external_num%3 == 0){
				trNode = externals_tableObj.insertRow(-1);
			}
			tdNode = trNode.insertCell(-1);
			$(tdNode).attr("style","width: 217px;");
			checkboxObj = document.createElement("input");
			checkboxObj.type = "checkbox";
			checkboxObj.id = trackItems[i].id + "externals" + "chk";
			checkboxObj.value = trackItems[i].id;
			checkboxObj.name = "remove_externals_chk";
			tdNode.appendChild(checkboxObj);
			labelObj = document.createElement("label");
			labelObj.setAttribute("for", trackItems[i].id + "externals" + "chk");
			$(labelObj).attr("style","font-size: 12px;");
			labelObj.innerHTML = trackItems[i].id;
			tdNode.appendChild(labelObj);
			external_num++;
		}
	}
	document.getElementById("remove_externals_all").checked = false;
}

function removeExternals_allselect(event){
	var target = event.target || event.srcElement;
	var chrCheckList = document.getElementsByName("remove_externals_chk");
	if($(target).attr("checked")){
		for(var i = 0; i < chrCheckList.length; i++) {
			chrCheckList[i].checked = true;
		}
	}else{
		for(var i = 0; i < chrCheckList.length; i++) {
			chrCheckList[i].checked = false;
		}
	}
}

function removeExternals(){
	var cookieStr;
	if($.cookie("customTrackList")){
		cookieStr = $.cookie("customTrackList");
	}else{
		return;
	}
	var customtrackList = [];
	var customtrackCookieList = cookieStr.split(",");
	for(var i = 0; i < customtrackCookieList.length; i++){
		customtrackList[i] = [];
		customtrackList[i].id = customtrackCookieList[i].split(":")[0];
		customtrackList[i].mode = customtrackCookieList[i].split(":")[1];
		customtrackList[i].dataType = customtrackCookieList[i].split(":")[2];
	}
	var externalCheckList = document.getElementsByName("remove_externals_chk");
	var external_trackids_str = "";
	for(var i=0; i < externalCheckList.length; i++){
		if($(externalCheckList[i]).attr("checked")){
			external_trackids_str = external_trackids_str + $(externalCheckList[i]).attr("value") + ",";
			for(var j = 0; j < trackItems.length; j++){
				if(trackItems[j].isServer == 0 && trackItems[j].id == $(externalCheckList[i]).attr("value")){
					$("#" + trackItems[j].id).remove();
					trackItems.splice(j, 1);
					j--;
				}
			}
			for(var k = 0; k < customtrackList.length; k++){
				if(customtrackList[k].id == $(externalCheckList[i]).attr("value")){
					customtrackList.splice(k, 1);
					k--;
				}
			}
		}
	}
	if(customtrackList.length == 0){
		$.cookie("customTrackList", null);
	}else{
		cookieStr = "";
		for(var i = 0; i < customtrackList.length; i++){
			cookieStr = cookieStr + customtrackList[i].id + ":" + customtrackList[i].mode + ":" + customtrackList[i].dataType + ",";
		}
		cookieStr = cookieStr.replace(/,$/gi,"");
		$.cookie("customTrackList", cookieStr);
	}
	if(external_trackids_str!=""){
		external_trackids_str = external_trackids_str.replace(/,$/gi,"");
		var url = "servlet/test.do?action=removeExternals&tracks=" + external_trackids_str;
		XMLHttpReq.onreadystatechange = null;
		XMLHttpReq.open("GET", url, false);
		XMLHttpReq.send(null);
	}
	$("#removeexternals_li").click();
	trackItems_setting2();
}

$(function() {
	$("#tabContentDiv").tabs();
	$("#slider").slider({
		min : 0,
		max : 30
	});
	$("#slider").on("slide", function(event, ui) {
		var sliderMaxValue = parseInt($( "#slider" ).slider( "option", "max" ));
		var baseLength = trackLength_user / 10;
		var newScale = baseLength * Math.pow(2, sliderMaxValue - ui.value);
		//取10000000作为width 和 left的限定值是因为left和width在css中比这个值稍微大一点就会失去作用，所以加了一下限制
		//而且在变化这么大的倍数下，再大的变化已经看不出来了
		var width = Math.round(searchLength_user / newScale * trackLength) < 10000000 ? Math.round(searchLength_user / newScale * trackLength) : 10000000;
		var left = Math.round((trackLength_user - width)/2);
		$(".canvasTrackcontent").each(function(arrayindex, arrayele) {
			$(arrayele).width(width);
			$(arrayele).css("left", left);
		});
		if(newScale > chrLength){
			newScale = chrLength;
		}
		var temp_start_user = start_user + (searchLength_user - newScale)/2;
		if(temp_start_user + newScale - 1 > chrLength){
			temp_start_user = chrLength - newScale + 1;
		}
		if(temp_start_user < 1){
			temp_start_user = 1;
		}
		drawScaleboxOnCytobandsImg(newScale, temp_start_user);
		showuserSearchIndex(temp_start_user, temp_start_user + newScale - 1);
	});
	$("#slider").on("slidestop", function(event, ui){
		var sliderMaxValue = parseInt($( "#slider" ).slider( "option", "max" ));
		var baseLength = trackLength_user / 10;
		var newScale = baseLength * Math.pow(2, sliderMaxValue - ui.value);
		var temp_start_user;
		if(newScale > chrLength){
			newScale = chrLength;
		}
		temp_start_user = Math.round(start_user + (searchLength_user - newScale)/2);
		if(temp_start_user < 1){
			temp_start_user = 1;
		}
		if(temp_start_user + newScale - 1 > chrLength){
			temp_start_user = chrLength - newScale + 1;
		}
		start_user = temp_start_user;
		searchLength_user = newScale;
		end_user = start_user + searchLength_user - 1;
		startIndex = start_user - searchLength_user;
		endIndex = end_user + searchLength_user;
		searchLength = searchLength_user * 3;
		
		showRef();
	});
});

function setSliderMax(){
	var baseLength = trackLength_user / 10;
	var sliderMaxValue = Math.ceil(Math.log(chrLength / baseLength) / Math.log(2));
	$("#slider").slider("option", "max", sliderMaxValue);
}

function setSliderValue(scale){
	var baseLength = trackLength_user / 10;
	var sliderMaxValue = $("#slider").slider("option", "max");
	var sliderValue = Math.log(scale / baseLength) / Math.log(2);
	$("#slider").slider("value", sliderMaxValue - sliderValue);
}

function addMousewheelEvent(DomObj, handler){
	if(DomObj.addEventListener) {
		DomObj.addEventListener("mousewheel", handler, false);
		DomObj.addEventListener("DOMMouseScroll", handler, false);
	} else {
		DomObj.attachEvent("onmousewheel", handler);
	}
}
var mousewheelzoomScale, mousewheelzoomStart, mousewheelFlag;
function mousewheelHandler(e){
	if(zKeyState){
		var baseLength = trackLength_user / 10;
		var e = window.event || e;
		var eventPosition = mouseCoords(e);
		//不用target原因是：在personal pannel中的定位与human中的定位不同
		//var target = e.target || e.srcElement;
		var canvasPositionLeft = $("#refTrack1").position().left;
		var canvasCssLeft = parseInt($("#refTrack1").css("left"));
		var canvasWidth = $("#refTrack1").width();
		var zoomNum = 2;
		var mouseOffsetOnCanvas = eventPosition.x - canvasPositionLeft;
		var mouseOffsetOntrackcontentTD = mouseOffsetOnCanvas + canvasCssLeft;
		var mouseLeftScale = mousewheelzoomScale * mouseOffsetOntrackcontentTD / trackLength_user;
		var mouseRightScale = mousewheelzoomScale - mouseLeftScale;

		if((e.wheelDelta || -e.detail) > 0) {
			if(mousewheelzoomScale > baseLength) {
				mousewheelzoomStart = Math.round(mousewheelzoomStart + mouseLeftScale / 2);
				mousewheelzoomScale = Math.round(mousewheelzoomScale / 2);
				if(canvasWidth * zoomNum < 10000000 && mouseOffsetOntrackcontentTD - mouseOffsetOnCanvas * zoomNum < 10000000) {//这个条件判断同样是防止大数值对于css left 和 width 的失效
					$(".canvasTrackcontent").each(function(arrayindex, arrayele) {
						$(arrayele).width(canvasWidth * zoomNum);
						$(arrayele).css("left", mouseOffsetOntrackcontentTD - mouseOffsetOnCanvas * zoomNum);
					});
				}
			}
		} else {
			if(mousewheelzoomScale < chrLength) {
				mousewheelzoomStart = Math.round(mousewheelzoomStart - mouseLeftScale);
				mousewheelzoomScale = Math.round(mousewheelzoomScale * 2);
				$(".canvasTrackcontent").each(function(arrayindex, arrayele) {
					$(arrayele).width(canvasWidth / zoomNum);
					$(arrayele).css("left", mouseOffsetOntrackcontentTD - mouseOffsetOnCanvas / zoomNum);
				});
			}
		}
		if(mousewheelzoomScale > chrLength) {
			mousewheelzoomScale = chrLength;
		}
		if(mousewheelzoomScale < baseLength) {
			mousewheelzoomScale = baseLength;
		}
		if(mousewheelzoomStart < 1) {
			mousewheelzoomStart = 1;
		}
		if(mousewheelzoomStart + mousewheelzoomScale - 1 > chrLength) {
			mousewheelzoomStart = chrLength - mousewheelzoomScale + 1;
		}
		searchLength_user = mousewheelzoomScale;
		start_user = mousewheelzoomStart;
		setSliderValue(mousewheelzoomScale);
		drawScaleboxOnCytobandsImg(mousewheelzoomScale, mousewheelzoomStart);
		showuserSearchIndex(start_user, start_user + searchLength_user - 1);

		//prevent default event handler of the browser
		if(e && e.preventDefault){
			e.preventDefault();
		}else{
			window.event.returnValue = false;
		}
		return false;
	}
}

$(document).ready(function(){
	addMousewheelEvent(document.getElementById("refTrack1"), mousewheelHandler);
});

function normalSearchIndex(){
	searchLength_user = end_user - start_user + 1;
	if(searchLength_user < trackLength_user / 10) {
		var temp_d = parseInt(trackLength_user/10) - searchLength_user;
		if(temp_d % 2 == 0) {
			start_user = start_user - temp_d / 2;
			end_user = end_user + temp_d / 2;
		} else {
			start_user = start_user - (parseInt(temp_d / 2) + 1);
			end_user = end_user + parseInt(temp_d / 2);
		}
	}
	if(start_user < 1){
		start_user = 1;
	}
	if(end_user > chrLength){
		end_user = chrLength;
	}
	searchLength_user = end_user - start_user + 1;
	if(searchLength_user < trackLength_user / 10){
		if(start_user == 1){
			end_user = parseInt(trackLength_user / 10);
		}else{
			start_user = end_user - parseInt(trackLength_user / 10) + 1;
		}
	}
	searchLength_user = end_user - start_user + 1;
	startIndex = start_user - searchLength_user;
	endIndex = end_user + searchLength_user;
	searchLength = searchLength_user * 3;
}

$(document).ready(function(){
	$("#zoomin").bind("click", sliderZoominBtn);
	$("#zoomout").bind("click", sliderZoomoutBtn);
});

function sliderZoominBtn(){
	var sliderMaxValue = $("#slider").slider("option", "max");
	var sliderValue = parseInt($( "#slider" ).slider( "value" ));
	if(sliderValue < sliderMaxValue){
		$( "#slider" ).slider( "value" , sliderValue + 1);
		
		var baseLength = trackLength_user / 10;
		var newScale = baseLength * Math.pow(2, sliderMaxValue - sliderValue - 1);
		var temp_start_user;
		if(newScale > chrLength) {
			newScale = chrLength;
		}
		temp_start_user = Math.round(start_user + (searchLength_user - newScale) / 2);
		if(temp_start_user < 1) {
			temp_start_user = 1;
		}
		if(temp_start_user + newScale - 1 > chrLength) {
			temp_start_user = chrLength - newScale + 1;
		}
		start_user = temp_start_user;
		searchLength_user = newScale;
		end_user = start_user + searchLength_user - 1;
		startIndex = start_user - searchLength_user;
		endIndex = end_user + searchLength_user;
		searchLength = searchLength_user * 3;
		
		drawScaleboxOnCytobandsImg(searchLength_user, start_user);
		showuserSearchIndex(start_user, end_user);
	
		showRef();
	}
}

function sliderZoomoutBtn(){
	var sliderMaxValue = $("#slider").slider("option", "max");
	var sliderValue = parseInt($( "#slider" ).slider( "value" ));
	if(sliderValue > 0){
		$( "#slider" ).slider( "value" , sliderValue - 1);
		
		var baseLength = trackLength_user / 10;
		var newScale = baseLength * Math.pow(2, sliderMaxValue - sliderValue + 1);
		var temp_start_user;
		if(newScale > chrLength) {
			newScale = chrLength;
		}
		temp_start_user = Math.round(start_user + (searchLength_user - newScale) / 2);
		if(temp_start_user < 1) {
			temp_start_user = 1;
		}
		if(temp_start_user + newScale - 1 > chrLength) {
			temp_start_user = chrLength - newScale + 1;
		}
		start_user = temp_start_user;
		searchLength_user = newScale;
		end_user = start_user + searchLength_user - 1;
		startIndex = start_user - searchLength_user;
		endIndex = end_user + searchLength_user;
		searchLength = searchLength_user * 3;
		
		drawScaleboxOnCytobandsImg(searchLength_user, start_user);
		showuserSearchIndex(start_user, end_user);
	
		showRef();
	}
}

function dragStopHandler(canvasCSSLeft){
	var dragDistance = canvasCSSLeft + trackLength_user;
	var dragScale = Math.round(dragDistance * searchLength_user / trackLength_user);
	var temp_start_user = start_user - dragScale;
	if(temp_start_user < 1 && startIndex <= 1){
		$(".canvasTrackcontent").each(function(arrayindex, arrayele) {
			$(arrayele).animate({"left":(startIndex - 1) / searchLength * trackLength});
		});
	}else if(temp_start_user + searchLength_user - 1 > chrLength && endIndex >= chrLength){
		$(".canvasTrackcontent").each(function(arrayindex, arrayele) {
			$(arrayele).animate({"left":trackLength_user - (chrLength - startIndex) / searchLength * trackLength});
		});
	}else{
		start_user = temp_start_user;
		end_user = start_user + searchLength_user - 1;
		startIndex = start_user - searchLength_user;
		endIndex = end_user + searchLength_user;
		showRef();
	}
}

function dragDragHandler(canvasCSSLeft){
	var dragDistance = canvasCSSLeft + trackLength_user;
	var dragScale = Math.round(dragDistance * searchLength_user / trackLength_user);
	var temp_start_user = start_user - dragScale;
	if(temp_start_user < 1){
		temp_start_user = 1;
	}else if(temp_start_user + searchLength_user - 1 > chrLength){
		temp_start_user = chrLength - searchLength_user + 1;
	}
	showuserSearchIndex(temp_start_user, temp_start_user + searchLength_user - 1);
	drawScaleboxOnCytobandsImg(searchLength_user, temp_start_user);
}

function showuserSearchIndex(start,end){
	var length = end - start + 1;
	$("#start_user_label").html(addCommas(start));
	$("#end_user_label").html(addCommas(end));
	$("#searchLength_user_label").html(addCommas(length) + "bp");
}

$(function(){
	$(window).resize(function(){
		var tracktableLeft = $("#divTrack").position().left;
		$(document.getElementById("personalPannel")).css("left", tracktableLeft);
		$('#showWindowBtn').css("left", $(window).width() - 32);
	});
})

function loadChrBand(){
	document.getElementById("BJW_genome").innerHTML="";
	document.getElementById("BJW_genelist").innerHTML="";
	document.getElementById("upload_success").innerHTML="Loading Individual Genome...";
	var xmlAttribute_gieStain = "gS";
	var xmlTagScore="Score";
	var pattern = /<.*?>/g;
	var animaTime=200;

	var l =570;
	var radius=160;
	var dis_chr2band=20;
	var chrthick=35;
	var bandthick=20;
	var dis_lable2chr=15;

	var font_size=18;
	var font_size_text=font_size+"px Candara";
	var font_size2=16;
	var font_size2_text=font_size2+"px Candara";

	var colorlist = {
		gpos100 : "rgb(0,0,0)",
		gpos    : "rgb(0,0,0)",
		gpos75  : "rgb(130,130,130)",
		gpos66  : "rgb(160,160,160)",
		gpos50  : "rgb(200,200,200)",
		gpos33  : "rgb(210,210,210)",
		gpos25  : "rgb(200,200,200)",
		gvar    : "rgb(220,220,220)",
		gneg    : "rgb(255,255,255)",
		acen    : "rgb(217,47,39)",
		stalk   : "rgb(100,127,164)",
		
		chr1  : "rgb(153,102,0)",
		chr2  : "rgb(102,102,0)",
		chr3  : "rgb(153,153,30)",
		chr4  : "rgb(204,0,0)",
		chr5  : "rgb(255,0,0)",
		chr6  : "rgb(255,0,204)",
		chr7  : "rgb(255,204,204)",
		chr8  : "rgb(255,153,0)",
		chr9  : "rgb(255,204,0)",
		chr10 : "rgb(255,255,0)",
		chr11 : "rgb(204,255,0)",
		chr12 : "rgb(0,255,0)",
		chr13 : "rgb(53,128,0)",
		chr14 : "rgb(0,0,204)",
		chr15 : "rgb(102,153,255)",
		chr16 : "rgb(153,204,255)",
		chr17 : "rgb(0,255,255)",
		chr18 : "rgb(204,255,255)",
		chr19 : "rgb(153,0,204)",
		chr20 : "rgb(204,51,255)",
		chr21 : "rgb(204,153,255)",
		chr22 : "rgb(102,102,102)",
		chr23 : "rgb(153,153,153)",
		chrX  : "rgb(153,153,153)",
		chr24 : "rgb(204,204,204)",
		chrY  : "rgb(204,204,204)",
		chrM  : "rgb(204,204,153)",
		chr0  : "rgb(204,204,153)"
	}

	XMLHttpReq7.open("GET","servlet/test.do?action=getChromosomes",false);
	XMLHttpReq7.send(null);
	var chrlist=XMLHttpReq7.responseText.replace(pattern,"");
	var chrs_temp=chrlist.split(",");
	var chrs=[];
	var chrs_map=[];
	var total=0;
	for(var idx=0;idx<chrs_temp.length;idx++){
		var chrtemp=chrs_temp[idx].split(":");
		chrs[idx]={};
		chrs[idx].name=chrtemp[0];
		chrs[idx].lengthh=parseInt(chrtemp[1]);
		chrs[idx].from=total+1;
		chrs[idx].to=total+parseInt(chrtemp[1]);
		chrs[idx].bands=[];
		total=total+parseInt(chrtemp[1]);
		chrs_map[chrtemp[0]]=idx;
	}

	for (var idx=0;idx<chrs.length;idx++){
		if(chrs[idx].lengthh<total/180){
			chrs[idx].to=chrs[idx].from+total/180;
			total=total+total/180-chrs[idx].lengthh;
		}
	}
	for (var idx=0;idx<chrs.length;idx++){
		chrs[idx].from=chrs[idx].from+total/180*idx;
		chrs[idx].to=chrs[idx].to+total/180*idx;
	}
	total=total+total/180*chrs.length;

	XMLHttpReq7.open("GET","servlet/test.do?action=getAllCytobands",false);
	XMLHttpReq7.send(null);
	//********switchable with later similar code to save initializing time
	var cytobandsNode = XMLHttpReq7.responseXML.getElementsByTagName(xmlTagCytobands)[0];
	var cytobandNodes = cytobandsNode.getElementsByTagName(xmlTagCytoBand);
	for( i = 0; i < cytobandNodes.length; i++) {
		var idx = chrs_map[cytobandNodes[i].getElementsByTagName(xmlTagChrNum)[0].childNodes[0].nodeValue];
		var curi = chrs[idx].bands.length;
		chrs[idx].bands[curi] = {};
		chrs[idx].bands[curi].id = cytobandNodes[i].getAttribute(xmlAttributeId);
		chrs[idx].bands[curi].gieStain = cytobandNodes[i].getAttribute(xmlAttribute_gieStain);
		chrs[idx].bands[curi].from = parseInt(cytobandNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue);
		chrs[idx].bands[curi].to = parseInt(cytobandNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue);
		chrs[idx].bands[curi].score = -1;
		if(cytobandNodes[i].getElementsByTagName(xmlTagScore).length > 0){
			chrs[idx].bands[curi].score = parseFloat(cytobandNodes[i].getElementsByTagName(xmlTagScore)[0].childNodes[0].nodeValue);
		}
	}
	chrs[chrs_map["chrM"]].bands[0].from=total-chrs[chrs_map["chrM"]].to+1;
	chrs[chrs_map["chrM"]].bands[0].to=total-chrs[chrs_map["chrM"]].from+1;
	//#####################

	var R = Raphael("BJW_genome", l+30, l+30);
	var G = Raphael("BJW_genelist",150,l);

	var attr = {
		fill: "#333",
		stroke: "#666",
		"stroke-width": 0,
		"stroke-linecap": "round",
		"fill-opacity": 0.7
	}
		
	var genome = [];
	var chrlables=[];
	var bands = [];
	var bandslables = [];
	var bandsscore = [];
	var bandsscore_inter = [];

	for(var idx=0;idx<chrs.length;idx++){
		bands[idx]=[];
		bandslables[idx]=[];
		bandsscore[idx] = [];
		bandsscore_inter[idx] = [];
		//*****Can be canceled to reduce object numbers and initializing time***
		for(var i=0;i<chrs[idx].bands.length;i++){
			attr["fill"]=colorlist[chrs[idx].bands[i].gieStain];
			attr["fill-opacity"]=0.9;
			attr["stroke-width"]=0;
			R.path(drawPath(chrs[idx].bands[i].from+chrs[idx].from,chrs[idx].bands[i].to+chrs[idx].from,total,radius,chrthick)).attr(attr);
			bandsscore_inter[idx][i]=R.path(drawPath(chrs[idx].bands[i].from+chrs[idx].from,chrs[idx].bands[i].to+chrs[idx].from,total,radius+chrthick+5,5)).attr({fill: cytoscore2color(chrs[idx].bands[i].score),"fill-opacity":0.9,"stroke-width":0});
		}
		//#############################
		attr["fill"]=colorlist[chrs[idx].name];
		attr["fill-opacity"]=0.6;
		attr["stroke-width"]=0;
		chrlables[idx]=drawText(chrs[idx].from,chrs[idx].to,total,radius,dis_lable2chr,chrs[idx].name.replace("chr","").replace("M","MT"),font_size);
		genome[idx]=R.path(drawPath(chrs[idx].from,chrs[idx].to,total,radius,chrthick)).attr(attr);
	}
	document.getElementById("upload_success").innerHTML="";

	var axis_set = R.set();
	var axis=R.path("M"+(l+20)+",0 L"+(l+20)+","+l).attr({stroke:"#000","stroke-width":1});
	var cali=[];
	var t_bot,t_top,t_first;
	t_top=R.text(l-10,l*0.008,"").attr({font: font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill: "#000"});
	t_bot=R.text(l-10,0.985*l,"").attr({font: font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill: "#000"});
	t_first=R.text(l-10,l/10,"").attr({font: font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill: "#000"});
	axis_set.push(axis,t_bot,t_top,t_first);
	for(var ca=0;ca<=100;ca++){
		if(ca%10==0){
			cali[ca]=R.path("M"+l+","+(ca/100*l)+" L"+(l+20)+","+(ca/100*l)).attr({stroke:"#000","stroke-width":1});
		}
		else{
			cali[ca]=R.path("M"+(l+10)+","+(ca/100*l)+" L"+(l+20)+","+(ca/100*l)).attr({stroke:"#000","stroke-width":1});
		}
		axis_set.push(cali[ca]);
	}
	//axis_set.hide();
	var gene_set = G.set();
	var loc_set= R.set();

	var go_text = R.text(l/2,l/2,"GO").attr({font: "32px Candara",opacity:1}).attr({fill: "#222"});
	var go_button = R.circle(l/2,l/2,chrthick+5).attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	(function (go_button){
		go_button[0].style.cursor = "pointer";
		go_text[0].style.cursor = "pointer";
		go_button[0].onmouseover = function(){
			go_button.animate({fill:"#999"},animaTime);
		};
		go_button[0].onclick = function(){
			browseJumpWindow_close();
			jump();
		};
		go_button[0].onmouseout = function(){
			go_button.animate({fill:"#666"},animaTime);
		};
	})(go_button);

	var current=null;
	var click=null;
	var burrent=null;
	var blick=null;
	var gurrent=null;
	var glick=null;

	var scan_text = R.text(280,l-5,"Scan Functional Variants for Individual").attr({font: "16px Candara",opacity:1}).attr({fill: "#000"});
	var error_warn=R.text(0,30,"").attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});

	var scang_text = R.text(100,l+18,"Whole genome").attr({font: "14px Candara",opacity:1}).attr({fill: "#222"});
	var scang_button = R.rect(20,l+6,160,24,3).attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	(function (scang_button){
		scang_button[0].style.cursor = "pointer";
		scang_text[0].style.cursor = "pointer";
		scang_button[0].onmouseover = function(){
			scang_button.animate({fill:"#999"},animaTime);
		};
		scang_button[0].onclick = function(){
			error_warn.remove();
			if(personalPannel.Pvar.id){
				getSingleCytoAsync(0,0,2);
			}
			else {
				error_warn=R.text(0,30,"Please load personal variants").attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});
			}
		};
		scang_button[0].onmouseout = function(){
			scang_button.animate({fill:"#666"},animaTime);
		};
	})(scang_button);

	var scanc_text = R.text(280,l+18,"Current chromosome").attr({font: "14px Candara",opacity:1}).attr({fill: "#222"});
	var scanc_button = R.rect(200,l+6,160,24,3).attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	(function (scanc_button){
		scanc_button[0].style.cursor = "pointer";
		scanc_text[0].style.cursor = "pointer";
		scanc_button[0].onmouseover = function(){
			scanc_button.animate({fill:"#999"},animaTime);
		};
		scanc_button[0].onclick = function(){
			error_warn.remove();
			if(click!=null && personalPannel.Pvar.id){
				getSingleCytoAsync(click,0,1);
			}
			else {
				error_warn=R.text(0,30,"Please load personal variants and select chromosome").attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});
			}
		};
		scanc_button[0].onmouseout = function(){
			scanc_button.animate({fill:"#666"},animaTime);
		};
	})(scanc_button);

	var scanb_text = R.text(460,l+18,"Current cytoband").attr({font: "14px Candara",opacity:1}).attr({fill: "#222"});
	var scanb_button = R.rect(380,l+6,160,24,3).attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	(function (scanb_button){
		scanb_button[0].style.cursor = "pointer";
		scanb_text[0].style.cursor = "pointer";
		scanb_button[0].onmouseover = function(){
			scanb_button.animate({fill:"#999"},animaTime);
		};
		scanb_button[0].onclick = function(){
			error_warn.remove();
			if(click!=null && blick!=null && personalPannel.Pvar.id){
				getSingleCytoAsync(click,blick,0);
			}
			else {
				error_warn=R.text(0,30,"Please load personal variants and select cytoband").attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});
			}
		};
		scanb_button[0].onmouseout = function(){
			scanb_button.animate({fill:"#666"},animaTime);
		};
	})(scanb_button);


	for (var chrom in genome){
		genome[chrom].color=Raphael.getColor();
		(function (chr, chrom) {
			var ccolor=colorlist[chrs[chrom].name];
			chr[0].style.cursor = "pointer";
			chr[0].onmouseover = function () {
				chr.animate({fill: chr.color, stroke: "#ccc"}, animaTime);
				/* switchable with previous similar code to save initiating time
				if(chrs[chrom].bands.length==0){
					XMLHttpReq7.open("GET","servlet/test.do?action=getCytobands&chr="+chrs[chrom].name,false);
					XMLHttpReq7.send(null);
					var cytobandsNode = XMLHttpReq7.responseXML.getElementsByTagName(xmlTagCytobands)[0];
					var cytobandNodes = cytobandsNode.getElementsByTagName(xmlTagCytoBand);
					if(cytobandNodes.length <= 1) {
						chrs[chrom].bands[0] = {};
						chrs[chrom].bands[0].id=chrs[chrom].name;
						chrs[chrom].bands[0].gieStain="";
						chrs[chrom].bands[0].from=total-chrs[chrom].to+1;
						chrs[chrom].bands[0].to=total-chrs[chrom].from+1;
						//To be test.
					}
					for( i = 0; i < cytobandNodes.length; i++) {
						chrs[chrom].bands[i] = {};
						chrs[chrom].bands[i].id = cytobandNodes[i].getAttribute(xmlAttributeId);
						chrs[chrom].bands[i].gieStain = cytobandNodes[i].getAttribute(xmlAttribute_gieStain);
						chrs[chrom].bands[i].from = parseInt(cytobandNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue);
						chrs[chrom].bands[i].to = parseInt(cytobandNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue);
					}
				}
				*/
				if(current){
					chrlables[current].animate({fill: "#000",font: font_size_text}, animaTime);
					for(var i=0; i < chrs[current].bands.length; i++) {
						bands[current][i].animate({fill: "#FFF", stroke: "#FFF"}, animaTime);
						bands[current][i].hide();
						bandsscore[current][i].hide();
					}
				}
				if(click){
					chrlables[click].animate({fill: "#000",font:font_size_text}, animaTime);
					for(var i=0; i < chrs[click].bands.length; i++) {
						bands[click][i].animate({fill: "#FFF", stroke: "#FFF"}, animaTime);
						bands[click][i].hide();
						bandsscore[click][i].hide();
					}
				}
				chrlables[chrom].animate({fill: chr.color,font:font_size_text}, animaTime);
				for(var i=0; i < chrs[chrom].bands.length; i++) {
					if(bands[chrom][i]==null){
						var bandfrom=chrs[chrom].bands[i].from+chrs[chrom].lengthh/180*i;
						var bandto=chrs[chrom].bands[i].to+chrs[chrom].lengthh/180*i;
						var chromlen;
						if(chrs[chrom].bands.length <=1){
							chromlen=chrs[chrom].lengthh;
						}
						else{
							chromlen=chrs[chrom].lengthh+chrs[chrom].lengthh/180*chrs[chrom].bands.length;
						}
						attr["fill-opacity"]=0;
						bands[chrom][i]=R.path(drawPath(bandfrom,bandto,chromlen,radius+chrthick+dis_chr2band,bandthick)).attr(attr);
						bandsscore[chrom][i]=R.path(drawPath(bandfrom,bandto,chromlen,radius+chrthick+dis_chr2band+bandthick+5,5)).attr({fill:cytoscore2color(chrs[chrom].bands[i].score),stroke:"#FFF","stroke-width":0,"fill-opacity":1});
						bandslables[chrom][i]=drawText(bandfrom,bandto,chromlen,radius+chrthick+dis_chr2band+bandthick,(-dis_lable2chr-10),chrs[chrom].bands[i].id,font_size2);
						bandslables[chrom][i].hide();
					}
					var bcolor=colorlist[chrs[chrom].bands[i].gieStain];
					bands[chrom][i].show();
					bands[chrom][i].animate({fill: bcolor, stroke: "#666","fill-opacity":1, "stroke-width":1}, animaTime);
					bandsscore[chrom][i].show();
				}
				R.safari();
				current = chrom;
			}; 
			chr[0].onmouseout = function () {
				chr.animate({fill: ccolor, stroke: "#666"}, animaTime);
				if(current && current != click){
					chrlables[current].animate({fill: "#000",font:font_size_text}, animaTime);
					for(var i=0; i < chrs[current].bands.length; i++) {
						bands[current][i].animate({fill: "#FFF", stroke: "#FFF"}, animaTime);
						bands[current][i].hide();
						bandsscore[current][i].hide();
					}
				}
				if(click && current != click){
					chrlables[click].animate({fill: genome[click].color,font:font_size_text}, animaTime);
					for(var i=0; i < chrs[click].bands.length; i++) {
						var bcolor=colorlist[chrs[click].bands[i].gieStain];
						bands[click][i].show();
						bands[click][i].animate({fill: bcolor, stroke: "#666"}, animaTime);
						bandsscore[click][i].show();
					}
				}
				R.safari();
			}; 
			chr[0].onclick = function() {
				if(click && click != chrom){
					burrent=null;
					blick=null;
					gurrent=null;
					glick=null;
					for (var i=0; i<bands[click].length; i++){
						bands[click][i].color=colorlist[chrs[click].bands[i].gieStain];
						(function (bd, i) {
							bd[0].style.cursor = "default";
							bd[0].onmouseover = null;
							bd[0].onclick = null;
							bd[0].onmouseout = null;
						})(bands[click][i],i);
					}
							t_top.hide();
							t_bot.hide();
							t_first.hide();
						//	axis_set.hide();
							gene_set.hide();
							gene_set.clear();
							loc_set.hide();
							loc_set.clear();
				}

				click = chrom;
				var inputsf=1;
				var inputet=chrs[click].lengthh;
				document.getElementById("search_field").value=chrs[click].name+":"+inputsf.toLocaleString().replace(/\.0+$/,"")+"-"+inputet.toLocaleString().replace(/\.0+$/,"");

				for (var i=0; i<bands[click].length; i++){
					bands[click][i].color=Raphael.getColor();
					(function (bd, i) {
						var bcolor=colorlist[chrs[click].bands[i].gieStain];
						bd[0].style.cursor = "pointer";
						bd[0].onmouseover = function () {
							bd.animate({fill: bd.color, stroke: "#ccc"}, animaTime);
							bandslables[click][i].show();
							if(burrent){
							}
							if(blick){
							}
							R.safari();
							burrent = i;
						}; 
						bd[0].onmouseout = function () {
							bd.animate({fill: bcolor, stroke: "#666"}, animaTime);
							bandslables[click][i].hide();
							if(burrent && burrent != blick){
							}
							if(blick && burrent != blick){
							}
							R.safari();
						}; 
						bd[0].onclick = function() {
							gurrent=null;
							glick=null;
							t_top.hide();
							t_bot.hide();
							t_first.hide();
							//axis_set.hide();
							gene_set.hide();
							gene_set.clear();
							loc_set.hide();
							loc_set.clear();
							blick = i;
							var inputsf=chrs[click].bands[blick].from;
							var inputet=chrs[click].bands[blick].to;
							document.getElementById("search_field").value=chrs[click].name+":"+inputsf.toLocaleString().replace(/\.0+$/,"")+"-"+inputet.toLocaleString().replace(/\.0+$/,"");
							

							t_top=R.text(l-10,l*0.015,chrs[click].name+" : "+chrs[click].bands[blick].id+" : "+chrs[click].bands[blick].from).attr({font: font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill: "#000"});
							t_bot=R.text(l-10,0.985*l,chrs[click].bands[blick].to).attr({font: font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill: "#000"});
							t_first=R.text(l-10,l/10,(chrs[click].bands[blick].to-chrs[click].bands[blick].from+1)/10).attr({font: font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill: "#000"});
							//axis_set.show();
							var urll="servlet/test.do?action=overlapGene&chr="+chrs[click].name+"&start="+chrs[click].bands[blick].from+"&end="+chrs[click].bands[blick].to;
							XMLHttpReq7.open("GET","servlet/test.do?action=overlapGene&chr="+chrs[click].name+"&start="+chrs[click].bands[blick].from+"&end="+chrs[click].bands[blick].to,false);
							XMLHttpReq7.send(null);
							var ogenesNode = XMLHttpReq7.responseXML.getElementsByTagName("Genes")[0];
							var ogeneNodes = ogenesNode.getElementsByTagName("Gene");
							var ogenes=[];
							var ogeneso=[];
							var genelistlength=ogeneNodes.length*20+10;
							G.remove();
							G = Raphael("BJW_genelist",150,genelistlength);

							for(var gidx=0;gidx<ogeneNodes.length;gidx++){
								ogenes[gidx] = {};
								ogenes[gidx].id = ogeneNodes[gidx].getAttribute(xmlAttributeId);
								ogenes[gidx].from = parseInt(ogeneNodes[gidx].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue);
								ogenes[gidx].to = parseInt(ogeneNodes[gidx].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue);
								if(ogeneNodes[gidx].getElementsByTagName(xmlTagScore).length>0){
									ogenes[gidx].score = parseFloat(ogeneNodes[gidx].getElementsByTagName(xmlTagScore)[0].childNodes[0].nodeValue);
								}
								else{
									ogenes[gidx].score = -1;
								}

								ogeneso[gidx]= {};
								ogeneso[gidx].text=G.text(10,(gidx*20+10),ogenes[gidx].id).attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});
								if(ogenes[gidx].score>=100){
									ogenes[gidx].score=0;
								} else{
									if(ogenes[gidx].score<=0){
										ogenes[gidx].score=255;
									} else{
										ogenes[gidx].score=200-ogenes[gidx].score*2;
									}
								}
								var rgbparam="rgb(255,"+ogenes[gidx].score+","+ogenes[gidx].score+")";
								
								ogeneso[gidx].flag=G.rect(0,(gidx*20),8,20).attr({fill:Raphael.getRGB(rgbparam).hex,"stroke-width":0});
								var gup=ogenes[gidx].from-chrs[click].bands[blick].from;
								var gdown=ogenes[gidx].to-chrs[click].bands[blick].from;
								var bandlen=chrs[click].bands[blick].to-chrs[click].bands[blick].from;
								if(gdown-gup<bandlen/100){
									gdown=gdown+bandlen/200;
									gup=gup-bandlen/200;
								}
								if(gup<0){
									gup=0;
								}
								if(gdown>bandlen){
									gdown=bandlen;
								}
								ogeneso[gidx].loc=R.path("M"+(l+20)+","+l*gup/bandlen+" L"+(l+20)+","+l*gdown/bandlen).attr({stroke:"#F00","stroke-width":8});
								ogeneso[gidx].loc.hide();
								gene_set.push(ogeneso[gidx].text);
								gene_set.push(ogeneso[gidx].flag);
								loc_set.push(ogeneso[gidx].loc);
								(function (gen, gidx) {
									gen[0].style.cursor = "pointer";
									gen[0].onmouseover = function () {
										gen.animate({fill: "#F00"}, animaTime);
										ogeneso[gidx].loc.show();
										if(gurrent!=null && gidx != gurrent){
											ogeneso[gurrent].text.animate({fill: "#000"}, animaTime);
											ogeneso[gurrent].loc.hide();
										}
										if(glick!=null && gidx !=glick){
											ogeneso[glick].text.animate({fill: "#000"}, animaTime);
											ogeneso[glick].loc.hide();
										}
										R.safari();
										G.safari();
										gurrent = gidx;
									};
									gen[0].onmouseout = function () {
										gen.animate({fill: "#000"}, animaTime);
										ogeneso[gidx].loc.hide();
										if(glick!=null){
											ogeneso[glick].text.animate({fill: "#F00"}, animaTime);
											ogeneso[glick].loc.show();
										}
										R.safari();
										G.safari();
									}; 
									gen[0].onclick = function (){
										//	document.getElementById("divv").innerHTML=chrs[click].bands[blick].id;
										glick = gidx;
										var inputsf=ogenes[gidx].from;
										var inputet=ogenes[gidx].to;
										document.getElementById("search_field").value=chrs[click].name+":"+inputsf.toLocaleString().replace(/\.0+$/,"")+"-"+inputet.toLocaleString().replace(/\.0+$/,"");

									};
								})(ogeneso[gidx].text,gidx);
							}
							gene_set.show();
						};
					})(bands[click][i],i);
				}
			};
		})(genome[chrom],chrom);
	}

	function getSingleCytoAsync(c,b,mode) {
		control_scanning=1;
		var sremove = spinner();
		var burrentt = R.text(70,30,"Scanning "+chrs[c].name+" : "+chrs[c].bands[b].id+" ...").attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});
		var XMLHttpReq9 = createXMLHttpRequest();
		XMLHttpReq9.open("GET","servlet/test.do?action=getCytoband&chr="+chrs[c].name+"&id="+chrs[c].bands[b].id,true);
		XMLHttpReq9.onreadystatechange = returnedCytoScore;
		XMLHttpReq9.send(null);
		function returnedCytoScore(){
			if(XMLHttpReq9.readyState==4){
				if(XMLHttpReq9.status==200){
					var score = parseFloat(XMLHttpReq9.responseXML.getElementsByTagName(xmlTagScore)[0].childNodes[0].nodeValue);
					bandsscore_inter[c][b]=R.path(drawPath(chrs[c].bands[b].from+chrs[c].from,chrs[c].bands[b].to+chrs[c].from,total,radius+chrthick+5,5)).attr({fill: cytoscore2color(score),"fill-opacity":0.9,"stroke-width":0});
					sremove();
					burrentt.remove();
					if(control_scanning==1&&click!=null&&blick!=null&&click==c&&blick==b){
						bands[c][b][0].onclick();
					}
					if(control_scanning==1 ){
						if(mode==2 && bands[c][b]==null){
							var bandfrom=chrs[c].bands[b].from+chrs[c].lengthh/180*b;
							var bandto=chrs[c].bands[b].to+chrs[c].lengthh/180*b;
							var chromlen;
							if(chrs[c].bands.length <=1){
								chromlen=chrs[c].lengthh;
							}
							else{
								chromlen=chrs[c].lengthh+chrs[c].lengthh/180*chrs[c].bands.length;
							}
							attr["fill-opacity"]=0;
							bands[c][b]=R.path(drawPath(bandfrom,bandto,chromlen,radius+chrthick+dis_chr2band,bandthick)).attr(attr);
							bandsscore[c][b]=R.path(drawPath(bandfrom,bandto,chromlen,radius+chrthick+dis_chr2band+bandthick+5,5)).attr({fill:"#FFF",stroke:"#FFF","stroke-width":0,"fill-opacity":1});
							bandslables[c][b]=drawText(bandfrom,bandto,chromlen,radius+chrthick+dis_chr2band+bandthick,(-dis_lable2chr-10),chrs[c].bands[b].id,font_size2);
							bandslables[c][b].hide();
							bandsscore[c][b].hide();
							bands[c][b].hide();
						}
						bandsscore[c][b].animate({fill: cytoscore2color(score)}, animaTime);
					}
					if(control_scanning==1&&((mode==1 && b<chrs[c].bands.length-1)||(mode==2 && (c<chrs.length-1 || b<chrs[c].bands.length-1)))){
						if(b<chrs[c].bands.length-1){
							b++;
						}
						else{
							c++;
							b=0;
						}
						getSingleCytoAsync(c,b,mode);
					}
				}
				else{
					sremove();
					burrentt.remove();
				}
			}
		}
	}
	function cytoscore2color(score){
		if(score>=100){
			score=0;
		} else{
			if(score<=0){
				score=255;
			} else{
				score=200-score*2;
			}
		}
		var rgbparam="rgb(255,"+score+","+score+")";
		return Raphael.getRGB(rgbparam).hex;
	}

	function drawPath(from, to, total, r, thick) {
		var alpha = from/total*360;
		var a = (90-alpha)*Math.PI/180;
		var beta = to/total*360;
		var b = (90-beta)*Math.PI/180;
		var a1x = l/2+r*Math.cos(a);
		var a1y = l/2-r*Math.sin(a);
		var a2x = l/2+(r+thick)*Math.cos(a);
		var a2y = l/2-(r+thick)*Math.sin(a);
		var b1x = l/2+r*Math.cos(b);
		var b1y = l/2-r*Math.sin(b);
		var b2x = l/2+(r+thick)*Math.cos(b);
		var b2y = l/2-(r+thick)*Math.sin(b);
		var path;
		if (to-from+1 >= total/2){
			path = "M"+a1x+","+a1y+" A"+r+","+r+" 0 1,1 "+(b1x-0.0001)+","+b1y+" L"+b2x+","+b2y+" A"+(r+thick)+","+(r+thick)+" 0 1,0 "+(a2x-0.0001)+","+a2y+" Z";
		}
		else {
			path = "M"+a1x+","+a1y+" A"+r+","+r+" 0 0,1 "+b1x+","+b1y+" L"+b2x+","+b2y+" A"+(r+thick)+","+(r+thick)+" 0 0,0 "+a2x+","+a2y+" Z";
		}
		return path;
	}
	function drawText(from, to, total, r, dis, text, fontsize) {
		var alpha = (from+to)/2/total*360;
		var a = (90 -alpha)*Math.PI/180;
		var ax = l/2+(r-dis)*Math.cos(a);
		var ay = l/2-(r-dis)*Math.sin(a);
		if(to-from<=total/180 && text == "MT"){
			return R.text(ax,ay,text).attr({font: (fontsize-8)+"px Candara",opacity:1}).attr({fill: "#000"});
		}
		else{
			return R.text(ax,ay,text).attr({font: fontsize+"px Candara",opacity:1}).attr({fill: "#000"});
		}
	}
	function spinner(){
		 var sectorsCount = 10,
			 color = "#000",
			 width = 5,
			 r1 = 10,
			 r2 = 20,
			 cx = r2 + width,
			 cy = r2 + width,
			 sectors = [],
			 opacity = [],
			 beta = 2 * Math.PI / sectorsCount,
			 pathParams = {stroke: color, "stroke-width": width, "stroke-linecap": "round"};
		 Raphael.getColor.reset();
		 for (var i = 0; i < sectorsCount; i++) {
			 var alpha = beta * i - Math.PI / 2,
				 cos = Math.cos(alpha),
				 sin = Math.sin(alpha);
			 opacity[i] = 1 / sectorsCount * i;
			 sectors[i] = R.path([["M", cx + r1 * cos, cy + r1 * sin], ["L", cx + r2 * cos, cy + r2 * sin]]).attr(pathParams);
			 if (color == "rainbow") {
				 sectors[i].attr("stroke", Raphael.getColor());
			 }
		 }
		 var tick;
		 (function ticker() {
		  opacity.unshift(opacity.pop());
		  for (var i = 0; i < sectorsCount; i++) {
		  sectors[i].attr("opacity", opacity[i]);
		  }
		  R.safari();
		  tick = setTimeout(ticker, 1000 / sectorsCount);
		  })();
		 return function () {
			 clearTimeout(tick);
			 for (var i = 0; i < sectorsCount; i++) {
		 	 sectors[i].hide();
		 	 }
			 //R.remove();
		 }; 
	}

}
function BJW_getStat() {
	if(personalPannel.Pvar.id)
		window.open("servlet/test.do?action=getStat");
	else
		document.getElementById("upload_success").innerHTML="Please load personal variants";
}
function BJW_upStat() {
	var fileObj = document.getElementById("file_field").files[0];
	var form = new FormData();
	form.append("file",fileObj);
	form.append("enctype","multipart/form-data");
	XMLHttpReq8.open("POST","servlet/test.do?action=upStat",true);
	XMLHttpReq8.onreadystatechange = uploadComplete;
	XMLHttpReq8.send(form);
	function uploadComplete(){
		if(XMLHttpReq8.readyState==4){
			if(XMLHttpReq8.status==200){
				document.getElementById("upload_success").innerHTML="Upload Complete";
			}
			else{
				document.getElementById("upload_success").innerHTML="Upload Failed";
			}
		}
	}
}
/*为解决搜索框与personal gene detail box出现冲突而添加的代码，从根本上解决之后不需要这段代码
$(document).ready(function() {
	$("#search_field").click(function(){
		$("#search_field").focus();
		$("body").css("user-select","auto");
		$("body").css("-moz-user-select","auto");
		$("body").css("-webkit-user-select","auto");
		$("body").css("-ms-user-select","auto");
		
		$("#search_field").css("user-select","auto");
		$("#search_field").css("-moz-user-select","auto");
		$("#search_field").css("-webkit-user-select","auto");
		$("#search_field").css("-ms-user-select","auto");
	});
	$(document).click(function(evt){
		evt = evt || window.event;
		var eventTarget = evt.target || evt.srcElement;
		if(eventTarget.id != "search_field"){
			$("#search_field").blur();
		}
	});
});*/
