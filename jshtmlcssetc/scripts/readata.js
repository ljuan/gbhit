function createXMLHttpRequest() {
	var xmlHttp = false;
	if (window.XMLHttpRequest){
		xmlHttp = new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		try{
			xmlHttp = new ActiveXObject("Msxm12.XMLHTTP");
		}
		catch(e1){
			try{
				xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
			}
			catch(e2){
				
			}
		}
	}
	return xmlHttp;
}
var req = createXMLHttpRequest();

var pattern = /<.*?>/g;
req.open("GET","servlet/test.do?action=getAnnotations",false);
req.send(null);
var annolist=req.responseText.replace(pattern,"");
var annos_temp=annolist.split(",");
var individuals=[];
for(var idx=0;idx<annos_temp.length;idx++){
	var anno_temp=annos_temp[idx].split(":");
//	if(anno_temp[3] == "VCF" || anno_temp[3] == "GVF"){
	if(anno_temp[0] == xmlGroupIG){
		individuals[individuals.length] = {};
		individuals[individuals.length-1].track = anno_temp[1];
		individuals[individuals.length-1].samples = [];
		req.open("GET","servlet/test.do?action=getParams&tracks="+individuals[individuals.length-1].track,false);
		req.send(null);
		var individualsNode = req.responseXML.getElementsByTagName(xmlTagParameters);
		if(individualsNode.length > 0){
			var individualNodes = individualsNode[0].getElementsByTagName(xmlTagParameter);
			for (var i=0;i<individualNodes.length;i++){
				if(individualNodes[i].getAttribute(xmlAttributeId) == xmlParamSample){
					var sampleslist=individualNodes[i].getElementsByTagName(xmlTagOptions)[0].childNodes;
					var sampleTemps="";
					for(var si=0;si<sampleslist.length;si++){
						sampleTemps=sampleTemps+sampleslist[si].nodeValue;
					}
					var samplesTemp=sampleTemps.split(";");
					for(var si=0;si<samplesTemp.length;si++){
						individuals[individuals.length-1].samples[si]=samplesTemp[si].split(":")[0];
					}
				}
			}
		}
		if(individuals[individuals.length-1].samples.length == 0){
			individuals[individuals.length-1].samples[0]=individuals[individuals.length-1].track;
		}
	}
}


req.open("GET","servlet/test.do?action=getChromosomes",false);
req.send(null);
var chrlist=req.responseText.replace(pattern,"");
var chrs_temp=chrlist.split(",");
var chrs=[];
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
	req.open("GET","servlet/test.do?action=getCytobands&chr="+chrs[idx].name,false);
	req.send(null);
	var cytobandsNode = req.responseXML.getElementsByTagName(xmlTagCytobands)[0];
	var cytobandNodes = cytobandsNode.getElementsByTagName(xmlTagCytoband);
	if(cytobandNodes.length == 0) {
		chrs[idx].bands[0] = {};
		chrs[idx].bands[0].id=chrs[idx].name;
		chrs[idx].bands[0].gieStain="";
		chrs[idx].bands[0].from=total-chrs[idx].to+1;
		chrs[idx].bands[0].to=total-chrs[idx].from+1;
		//To be test.
	}
	for( i = 0; i < cytobandNodes.length; i++) {
		chrs[idx].bands[i] = {};
		chrs[idx].bands[i].id = cytobandNodes[i].getAttribute(xmlAttributeId);
		chrs[idx].bands[i].gieStain = cytobandNodes[i].getAttribute(xmlAttribute_gieStain);
		chrs[idx].bands[i].from = parseInt(cytobandNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue);
		chrs[idx].bands[i].to = parseInt(cytobandNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue);
	}
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
