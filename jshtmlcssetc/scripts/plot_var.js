/**
 * @author Yafeng Hao, Liran Juan
 */
var colD = "#d73027"; //denovo mut color
var colD_hover = "#f46d43"; //denovo mut color hover
var colP = "#4575b4"; //paternal var color
var colP_hover = "#74add1"; //paternal var color hover
var colM = "#1a9850"; //maternal var color
var colM_hover = "#66bd63"; //maternal var color
var colO = "#000000"; //black
var colO_hover = "#636363"; //black hover
var colO_hover2 = "#bdbdbd"; //black hover
var colInter = "#542788";//intersection color
var colInter_hover = "#998ec3";//intersection color hover
var colDiff = "#542788";//difference color
var colDiff_hover = "#998ec3";//difference color hover

///////////////
var restrictX;
var restrictY;
var gdf_list;
var gdf_icon;
///////////////

var functional_vnum = 0;
var all_lds ;
function init_individual_vars(){
	variants = [];
	variants_byid = {};
	functional_v = {};
	functional_vPointer = [];
	genes = [];
	symbols = [];
	css = 0;
	cst = 0;
	cssObj = null;
	cstObj = null;

	all_lds = R.set();

	symbols[0] = "ALL";
	functional_vnum = 0;
	var symbol_map = {}
	var sPointer = 1;
	var vsNode = req3.responseXML.getElementsByTagName(xmlTagVariants)[0];
	var vNodes = vsNode.getElementsByTagName(xmlTagVariant);
	for(var i = 0 ; i < vNodes.length ; i++){
		variants[i] = {};
		variants[i].chr = current_chr;
		variants[i].id = vNodes[i].getAttribute(xmlAttributeId);
		variants[i].dd = vNodes[i].getAttribute("dd");

		variants[i].genotype = vNodes[i].getAttribute(xmlAttributeGenotype);
		var splitter = "|";
		if(variants[i].genotype.indexOf("/")>0){
			splitter = "/";
		}
		variants[i].genotypes = variants[i].genotype.split(splitter);

		if(i > 0 && variants[i-1].genotypes[0]*variants[i-1].genotypes[1] > 1 
		&& variants[i].genotypes[0]*variants[i].genotypes[1] > 1
		&& (variants[i].dd == undefined && variants[i-1].dd == undefined
		|| variants[i].dd != undefined && variants[i-1].dd != undefined 
		&& variants[i].dd == variants[i-1].dd)){

			variants[i-1].genotype = variants[i-1].genotype + "(" + variants[i-1].genotypes[0] + ")";
			variants[i].genotype = variants[i].genotype + "(" + variants[i].genotypes[1] + ")";
			variants[i-1].genotypes[1] = 0;
			variants[i].genotypes[0] = 0;
		}
		variants[i].type = vNodes[i].getAttribute(xmlAttributeType);

		if(variants[i].id == undefined || variants[i].id == "." || variants[i].id ==""){
			variants[i].id = "Unnamed "+variants[i].type;
		}
		variants[i].from = parseInt(vNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue);
		variants[i].to = parseInt(vNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue);
		variants[i].selected = false;

		if(vNodes[i].getElementsByTagName(xmlTagLetter).length > 0){
			variants[i].letter = vNodes[i].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
		} else {
			variants[i].letter = "--";
		}

		variants_byid[current_chr+":"+variants[i].from+":"+variants[i].to+":"+(variants[i].letter=="--"?"-":variants[i].letter)] = i;
		variants[i].paternal = "Y";
		variants[i].maternal = "Y";
		variants[i].functional = "--";
	}

	var esNodes = req3.responseXML.getElementsByTagName(xmlTagElements);
	var enode_start = 0;

	function match_functional_variants(vPointer, from, to, letter, id){
		var letter_trans = letter;

		if(letter == "(" || letter == ")"){
			letter_trans = "ASS/DSS loss";
		}else if (letter == "#"){
			letter_trans = "Frame shifting";
		}else if (letter == "^"){
			letter_trans = "Initiator loss";
		}else{
			var texts = letter.split(":");
			if(texts[0].indexOf("$") >= 0){
				letter_trans = "Stop loss";
			}else if(texts[1].indexOf("$") >= 0){
				letter_trans = "Stop gain";
			}else {
				letter_trans = texts[0] + "->" + texts[1];
			}
		}

		while(vPointer > 0 && variants[vPointer].to > to){
			vPointer--;
		}
		while(vPointer < variants.length-1 && variants[vPointer].from < from){
			vPointer++;
		}
		if(from <= variants[vPointer].from && to >= variants[vPointer].to && id == variants[vPointer].id){
			if(functional_v[from+":"+to+":"+letter] == undefined){
				functional_v[from+":"+to+":"+letter] = vPointer;
				functional_vnum++;
				var fvidx = 0; 
				if(variants[vPointer].functional == "--"){
					//variants[vPointer] has function for the first time
					variants[vPointer].functional = letter_trans;
					variants[vPointer].functional_v = [];
					functional_vPointer[functional_vPointer.length] = vPointer;
				}else{
					//variants[vPointer] may play different roles in different allele/transcript
					variants[vPointer].functional += ","+letter_trans;
					fvidx = variants[vPointer].functional_v.length;
				}
				variants[vPointer].functional_v[fvidx] = {};
				variants[vPointer].functional_v[fvidx].from = from;
				variants[vPointer].functional_v[fvidx].to = to;
				variants[vPointer].functional_v[fvidx].letter = letter;
			}
		}else if(from <= variants[vPointer].from && to >= variants[vPointer].to){
			while(vPointer > 0 && from <= variants[vPointer-1].from && to >= variants[vPointer-1].to){
				vPointer--;
			}
			while(vPointer < variants.length && from <= variants[vPointer].from && to >= variants[vPointer].to){
				if(id == variants[vPointer].id){
					if(functional_v[from+":"+to+":"+letter] == undefined){
						functional_v[from+":"+to+":"+letter] = vPointer;
						functional_vnum++;
						var fvidx = 0; 
						if(variants[vPointer].functional == "--"){
							//variants[vPointer] has function for the first time
							variants[vPointer].functional = letter_trans;
							variants[vPointer].functional_v = [];
							functional_vPointer[functional_vPointer.length] = vPointer;
						}else{
							//variants[vPointer] may play different roles in different allele/transcript
							variants[vPointer].functional += ","+letter_trans;
							fvidx = variants[vPointer].functional_v.length;
						}
						variants[vPointer].functional_v[fvidx] = {};
						variants[vPointer].functional_v[fvidx].from = from;
						variants[vPointer].functional_v[fvidx].to = to;
						variants[vPointer].functional_v[fvidx].letter = letter;
					}
					return vPointer;
				} else {
					vPointer++;
				}
			}
			return vPointer-1;
		}
		return vPointer;
	}

	for(var i = 0 ; i < esNodes.length ; i++){
		var veNodes = esNodes[i].getElementsByTagName(xmlTagVariant);
		var vPointer = 0;
		for(var j = 0 ; j < veNodes.length ; j++){
			var from = veNodes[j].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
			var to = veNodes[j].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
			var letter = veNodes[j].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
			var id = veNodes[j].getAttribute(xmlAttributeId);
			var type = veNodes[j].getAttribute(xmlAttributeType);

			if(id == "."){
				id = "Unnamed "+type;
			}
			if(from.indexOf(";")<0 && to.indexOf(";")<0){
				match_functional_variants(vPointer, from, to, letter, id);
			} else {
				var froms = from.split(";");
				var tos = to.split(";");
				if(type == "DEL" && tos[1] - froms[0] < 50){
					match_functional_variants(vPointer, froms[0], tos[1], letter, id);
				}else{
					for(var junc_var = 0 ; junc_var < froms.length ; junc_var++){
						if(froms[junc_var]>=current_start && tos[junc_var]<=current_end){
							vPointer = match_functional_variants(vPointer, froms[junc_var], tos[junc_var], letter, id);
						}
					}
				}
			}
		}

		var eNodes = esNodes[i].getElementsByTagName(xmlTagElement);
		for(var j = 0 ; j < eNodes.length ; j++){
			if(i == 0){
				genes[j] = {};
				genes[j].id = eNodes[j].getAttribute(xmlAttributeId);
				genes[j].symbol = eNodes[j].getAttribute("Symbol");
				genes[j].from = eNodes[j].getElementsByTagName(xmlTagFrom)[0].firstChild.nodeValue;
				genes[j].to = eNodes[j].getElementsByTagName(xmlTagTo)[0].firstChild.nodeValue;
				genes[j].strand = eNodes[j].getElementsByTagName(xmlTagDirection)[0].firstChild.nodeValue;
				var subNodes = eNodes[j].getElementsByTagName(xmlTagSubElement);
				if(subNodes.length > 0){
					genes[j].subs = [];
					for(var k = 0 ; k < subNodes.length ; k++){
						genes[j].subs[k] = {};
						genes[j].subs[k].type = subNodes[k].getAttribute(xmlAttributeType);
						genes[j].subs[k].from = subNodes[k].getElementsByTagName(xmlTagFrom)[0].firstChild.nodeValue;
						genes[j].subs[k].to = subNodes[k].getElementsByTagName(xmlTagTo)[0].firstChild.nodeValue;
					}
				}

				var tr_len = 1;
				if(symbol_map[genes[j].symbol] == undefined){
					symbol_map[genes[j].symbol] = sPointer;
					symbols[sPointer] = [];
					symbols[sPointer][0] = genes[j].symbol;
					sPointer++;
				} else {
					tr_len = symbols[symbol_map[genes[j].symbol]].length;
				}
				symbols[symbol_map[genes[j].symbol]][tr_len] = j;
			}
			else if(esNodes[i].getAttribute(xmlAttributeId)=="_OMIM"){
				gdf_list[j] = {};
				gdf_list[j].symbol= eNodes[j].getAttribute("Symbol");
				gdf_list[j].from = eNodes[j].getElementsByTagName(xmlTagFrom)[0].firstChild.nodeValue;
				gdf_list[j].to = eNodes[j].getElementsByTagName(xmlTagTo)[0].firstChild.nodeValue;
				gdf_list[j].id = eNodes[j].getAttribute(xmlAttributeId);
				gdf_list[j].source = eNodes[j].getElementsByTagName("Source")[0].firstChild.nodeValue;
				/*var sPointerrr = symbol_map[eNodes[j].getAttribute("Symbol")];
				var slength = symbols[sPointerrr].length;
				var newindex = j + enode_start;
				symbols[sPointerrr][slength] = newindex;
				genes[j+enode_start] = {};
				genes[j+enode_start].from = eNodes[j].getElementsByTagName(xmlTagFrom)[0].firstChild.nodeValue;
				genes[j+enode_start].to = eNodes[j].getElementsByTagName(xmlTagTo)[0].firstChild.nodeValue;
				genes[j+enode_start].id = eNodes[j].getAttribute(xmlAttributeId);
				genes[j+enode_start].omim = 1;*/
			}
			var vNodes = eNodes[j].getElementsByTagName(xmlTagVariant);
			if(vNodes.length > 0){
				if(genes[j].vars == undefined){
					genes[j].vars = {};
				}
				for(var k = 0 ; k < vNodes.length ; k++){
					var from = vNodes[k].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
					var to = vNodes[k].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
					var letter = vNodes[k].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
					if(from.indexOf(";")>0 || to.indexOf(";")>0){
						var froms = from.split(";");
						var tos = to.split(";");
						if(functional_v[froms[0] + ":" + tos[0] + ":" + letter] != undefined){
							from = froms[0];
							to = tos[0];
						} else if (functional_v[froms[1] + ":" + tos[1] + ":" + letter] != undefined){
							from = froms[1];
							to = tos[1];
						} else if (functional_v[froms[0] + ":" + tos[1] + ":" + letter] != undefined){
							from = froms[0];
							to = tos[1];
						}
					}
					if(variants[functional_v[from + ":" + to + ":" + letter]] != undefined){
						for(var fvidx = 0 ; fvidx < variants[functional_v[from+":"+to+":"+letter]].functional_v.length ; fvidx++){
							if(from == variants[functional_v[from+":"+to+":"+letter]].functional_v[fvidx].from 
							&& to == variants[functional_v[from+":"+to+":"+letter]].functional_v[fvidx].to 
							&& letter == variants[functional_v[from+":"+to+":"+letter]].functional_v[fvidx].letter){
								genes[j].vars[from+":"+to+":"+letter] = fvidx;
							}
						}
					}else{
//						alert(from + ":" + to + ":" + letter);
					}
				}
			}
		}
	}
	functional_vPointer.sort(sortNumber);
}

function show_axis(){
	var l = R_height - R_top - R_bottom;
	var w = R_width - R_left - R_right;
	var font_size2_text = "14px \"Trebuchet MS\", Arial, sans-serif";
	var axis_set = R.set();
	var axis = R.path("M"+R_left+","+R_top+" L"+R_left+","+(R_height-R_bottom)).attr({stroke:colO,"stroke-width":1});
	var cali = [];
	var t_bot, t_top;
	t_top = R.text(0,R_top-10,current_chr+":"+current_start).attr({font: font_size2_text ,opacity:1,"text-anchor":"start"}).attr({fill: colO});
	t_bot = R.text(0,R_height-R_bottom+10,current_chr+":"+current_end).attr({font: font_size2_text ,opacity:1,"text-anchor":"start"}).attr({fill: colO});
	axis_set.push(axis, t_bot, t_top);
	ca = 0;
	axis_set.push(cali[ca]);
	for(var ca = 0 ; ca <= 100 ; ca++){
		if(ca%10 == 0){
			cali[ca] = R.path("M"+(R_left-20)+","+(R_top+ca/100*l)+" L"+R_left+","+(R_top+ca/100*l)).attr({stroke:colO,"stroke-width":1});
		}else{
			cali[ca] = R.path("M"+(R_left-10)+","+(R_top+ca/100*l)+" L"+R_left+","+(R_top+ca/100*l)).attr({stroke:colO,"stroke-width":1});
		}
		axis_set.push(cali[ca]);
	}
	axis_set.push(cali[ca]);
	//axis_set.hide();
	
	var brwplotCanvas = $(document.getElementById("brwplot"));
	//var touch_x = restrictX - brwplotCanvas.position().left;
	//var touch_y = restrictY - brwplotCanvas.position().top;
	var markline = R.path("M"+(R_left-35)+","+R_top+" L"+(R_left+R_width)+","+R_top).attr({"stroke-dasharray": "-",stroke:"#000","stroke-width":1});
	var marklable = R.text(R_left+1, R_top-8, "").attr({font:"8px  \"Trebuchet MS\", Arial, sans-serif"});
	
	var start_y, dis_y;
	var searcharea = R.rect(R_left-30, R_top,R_width+35,1).attr({fill:"#ffe4b5", "stroke-width":0, "fill-opacity":0.4});
	searcharea.hide();
	var	start_line;
	var toucharea = R.rect(R_left-25, R_top - 1, 50, R_height - R_bottom - R_top + 1, 0).attr({stroke: "#000", fill: "#000", opacity: 0, cursor: "move"});
	toucharea.drag(dragmove, dragstart, dragend);
	function dragstart(x,y) {
		start_y = y - brwplotCanvas.position().top;
		searcharea.animate({transform: "t0 "+(start_y-R_top)});
		searcharea.show();
		start_line = R.path("M"+(R_left-35)+","+start_y+" L"+(R_left+R_width)+","+start_y).attr({"stroke-dasharray": "-",stroke:"#000","stroke-width":1});
	}
	function dragmove(dx,dy) {
		if(dy>0){
			if(dy + start_y < R_height - R_bottom){
				searcharea.animate({height: dy});
				dis_y = dy;
			}else{
				searcharea.animate({height: (R_height - R_bottom - start_y)});
				dis_y = R_height - R_bottom - start_y;
			}			
		}else if(dy<0){
			if(dy + start_y > R_top){
				searcharea.animate({transform: "t0 "+(start_y-R_top+dy)});
				searcharea.animate({height: (0-dy)});
				dis_y = dy;
			}else{
				searcharea.animate({transform: "t0 0"});
				searcharea.animate({height: (start_y - R_top)});
				dis_y = R_top - start_y;
			}
		}
	}
	function dragend() {
		start_line.remove();
		searcharea.hide();
		var searchLength = current_end - current_start;
		var index1 = Math.round(searchLength * (start_y-R_top)/(R_height-R_bottom-R_top) + current_start);;
		var index2 = Math.round(searchLength * (start_y-R_top+dis_y)/(R_height-R_bottom-R_top) + current_start);;
		if(dis_y>0){
			load_family_genome(current_chr, index1, index2);
		}else{
			load_family_genome(current_chr, index2, index1);
		}
	}



	markline.hide();
	marklable.hide();
	toucharea.mouseover(function(){
		//alert("ok "+ restrictX + " " + restrictY + " " + brwplotCanvas.position().left);
		var touch_x = restrictX - brwplotCanvas.position().left;
		var touch_y = restrictY - brwplotCanvas.position().top;
		var searchLength = current_end - current_start;
		var refSeqIndex = Math.round(searchLength * (touch_y-R_top)/(R_height-R_bottom-R_top) + current_start);

		markline.stop().animate({transform:"t"+(R_left-25)+" "+(touch_y-R_top)},0);
		marklable.stop().animate({transform:"t"+(R_left+1)+" "+(touch_y-R_top)},0);
		marklable.attr({text:refSeqIndex});
		markline.show();
		marklable.show();
	});

	toucharea.mousemove(function(){
		var touch_x = restrictX - brwplotCanvas.position().left;
		var touch_y = restrictY - brwplotCanvas.position().top;
		var searchLength = current_end - current_start;
		var refSeqIndex = Math.round(searchLength * (touch_y-R_top)/(R_height-R_bottom-R_top) + current_start);
		markline.stop().animate({transform:"t"+(R_left-25)+" "+(touch_y-R_top)},0);
		marklable.stop().animate({transform:"t"+(R_left+1)+" "+(touch_y-R_top)},0);
		marklable.attr({text:refSeqIndex});
		markline.show();
		marklable.show();
	});

	toucharea.mouseout(function(){
		markline.hide();
		marklable.hide();
	});

	if(individuals[csi] != undefined){
		var color = colO;
		if(individuals[csi].fid != "0" && individuals[csi].mid != "0"){
			color = "#FFF";
			if(individuals[individuals[csi].fid].affected == "1"){
				color = colO;
			}
			R.rect(R_left+w/2+20,5,20,20,0).attr({fill:color,stroke:colO,"stroke-width":1});
	
			color = "#FFF";
			if(individuals[individuals[csi].mid].affected == "1"){
				color = colO;
			}
			R.ellipse(R_left+w/2-30,15,10,10).attr({fill:color,stroke:colO,"stroke-width":1});
			R.path("M"+(R_left+w/2-20)+",15 L"+(R_left+w/2+20)+",15").attr({stroke:colO,"stroke-width":1});
			R.path("M"+(R_left+w/2)+",15 L"+(R_left+w/2)+","+(R_top-25)).attr({stroke:colO,"stroke-width":1});
	
			R.text(R_left+w/2+42,15,individuals[csi].fid).attr({font:font_size2_text,fill:colP,"text-anchor":"start"});
			R.text(R_left+w/2-42,15,individuals[csi].mid).attr({font:font_size2_text,fill:colM,"text-anchor":"end"});
			color = colD;
		}
		R.text(R_left+w/2+12,R_top-15,csi).attr({font:font_size2_text,fill:color,"text-anchor":"start"});

		color = "#FFF";
		if(individuals[csi].affected == "1"){
			color = colO;
		}
		var indObj = null;
		if(individuals[csi].sex == "1"){
			indObj = R.rect(R_left+w/2-10,R_top-25,20,20,0).attr({fill:color,stroke:colO,"stroke-width":1});
		} else {
			indObj = R.ellipse(R_left+w/2,R_top-15,10,10).attr({fill:color,stroke:colO,"stroke-width":1});
		}
		(function(indObj){
			indObj[0].style.cursor = "pointer";
			indObj[0].onmouseover = function(){
				indObj.animate({fill:"#999"},200);
				R.safari();
			};
			indObj[0].onmouseout = function(){
				indObj.animate({fill:color},200);
				R.safari();
			};
			indObj[0].onclick = function(){
				if(document.getElementById("SD_window").style.display == "none"){
					call_shared_different();
				}else{
					close_shared_different();
				}
				R.safari();
			};
		})(indObj);
	}
}
////////////////////////////////////////////////////////////////////

function mousePosition(ev){
	var scrollLeft = document.documentElement.scrollLeft || document.body.scrollLeft;
	var scrollTop = document.documentElement.scrollTop || document.body.scrollTop;
	return {
		x:ev.clientX + scrollLeft - document.documentElement.clientLeft,
		y:ev.clientY + scrollTop - document.documentElement.clientTop
	};
}
function mouseMove(ev){
	ev = ev || window.event;
	var mousePos = mousePosition(ev);
	restrictX = mousePos.x;
	restrictY = mousePos.y;
}
document.onmousemove = mouseMove;
document.onclick = mouseMove;


////////////////////////////////////////////////////////////////////
function show_navigator(){
	var r1 = 10;
	var r2 = 15;
	var xp = R_width + 70;
	var xm = R_width + 70;
	var y = 100;
	var delta = 4;
	var inner_l = 12;
	var inner_w = 4;

	var plus = R.set();
	plus.push(R.ellipse(xp,y-40,r2,r2).attr({fill:colO_hover,stroke:colO_hover}));
	plus.push(R.ellipse(xp,y-40,r1,r1).attr({fill:"#FFF",stroke:colO_hover}));
	plus.push(R.rect(xp-inner_w/2, y-40-inner_l/2,inner_w,inner_l,0).attr({fill:colO_hover,"stroke-width":0}));
	plus.push(R.rect(xp-inner_l/2, y-40-inner_w/2,inner_l,inner_w,0).attr({fill:colO_hover,"stroke-width":0}));
	plus.push(R.path("M"+(xp+r1-delta)+","+(y-40+r1)+
					" L"+(xp+r1-delta+r2)+","+(y-40+r1+r2)+
					" L"+(xp+r1+r2)+","+(y-40+r1-delta+r2)+
					" L"+(xp+r1)+","+(y-40+r1-delta)+"Z").attr({fill:colO_hover,"stroke-width":0}));

	(function (obj){
		for(var i = 0 ; i < obj.length ; i++){
			obj[i][0].style.cursor = "pointer";
			obj[i][0].onmouseover = function(){
				obj[1].animate({fill:colO_hover2},100);
				R.safari();
			};
			obj[i][0].onmouseout = function(){
				obj[1].animate({fill:"#FFF"},100);
				R.safari();
			};
			obj[i][0].onclick = function(){
				load_family_genome(current_chr,Math.floor(current_start+(current_end-current_start)/4),Math.ceil(current_end-(current_end-current_start)/4));
				R.safari();
			};
		}
	})(plus);

	var minus = R.set();
	minus.push(R.ellipse(xm,y,r2,r2).attr({fill:colO_hover,stroke:colO_hover}));
	minus.push(R.ellipse(xm,y,r1,r1).attr({fill:"#FFF",stroke:colO_hover}));
	minus.push(R.rect(xm-inner_l/2,y-inner_w/2,inner_l,inner_w,0).attr({fill:colO_hover,"stroke-width":0}));
	minus.push(R.path("M"+(xm+r1-delta)+","+(y+r1)+
					" L"+(xm+r1-delta+r2)+","+(y+r1+r2)+
					" L"+(xm+r1+r2)+","+(y+r1-delta+r2)+
					" L"+(xm+r1)+","+(y+r1-delta)+"Z").attr({fill:colO_hover,"stroke-width":0}));

	(function (obj){
		for(var i = 0 ; i < obj.length ; i++){
			obj[i][0].style.cursor = "pointer";
			obj[i][0].onmouseover = function(){
				obj[1].animate({fill:colO_hover2},100);
				R.safari();
			};
			obj[i][0].onmouseout = function(){
				obj[1].animate({fill:"#FFF"},100);
				R.safari();
			};
			obj[i][0].onclick = function(){
				load_family_genome(current_chr,Math.floor(current_start-(current_end-current_start)/2),Math.ceil(current_end+(current_end-current_start)/2));
				R.safari();
			};
		}
	})(minus);

	var upper = R.set();
	upper.push(R.path("M"+xm+","+(y+r2*2)+
				" L"+(xm-r1)+","+(y+r2*3)+
				" L"+(xm+r1)+","+(y+r2*3)+"Z").attr({fill:colO_hover,"stroke-width":0}));
	upper.push(R.path("M"+xm+","+(y+r2*3-delta)+
				" L"+(xm-r1)+","+(y+r2*4-delta)+
				" L"+(xm+r1)+","+(y+r2*4-delta)+"Z").attr({fill:colO_hover,"stroke-width":0}));
	(function (obj){
		for(var i = 0 ; i < obj.length ; i++){
			obj[i][0].style.cursor = "pointer";
			obj[i][0].onmouseover = function(){
				obj[0].animate({fill:colO_hover2},100);
				obj[1].animate({fill:colO_hover2},100);
				R.safari();
			};
			obj[i][0].onmouseout = function(){
				obj[0].animate({fill:colO_hover},100);
				obj[1].animate({fill:colO_hover},100);
				R.safari();
			};
			obj[i][0].onclick = function(){
				load_family_genome(current_chr,2*current_start-current_end,current_start);
				R.safari();
			};
		}
	})(upper);

	var up = R.path("M"+xm+","+(y+5+r2*4)+
				" L"+(xm-r1)+","+(y+5+r2*5)+
				" L"+(xm+r1)+","+(y+5+r2*5)+"Z").attr({fill:colO_hover,"stroke-width":0});
	(function (obj){
		obj[0].style.cursor = "pointer";
		obj[0].onmouseover = function(){
			obj.animate({fill:colO_hover2},100);
			R.safari();
		};
		obj[0].onmouseout = function(){
			obj.animate({fill:colO_hover},100);
			R.safari();
		};
		obj[0].onclick = function(){
			load_family_genome(current_chr,Math.floor(current_start-(current_end-current_start)/2),Math.ceil(current_end-(current_end-current_start)/2));
			R.safari();
		};
	})(up);

	var down = R.path("M"+xm+","+(y+15+r2*6)+
				" L"+(xm-r1)+","+(y+15+r2*5)+
				" L"+(xm+r1)+","+(y+15+r2*5)+"Z").attr({fill:colO_hover,"stroke-width":0});
	(function (obj){
		obj[0].style.cursor = "pointer";
		obj[0].onmouseover = function(){
			obj.animate({fill:colO_hover2},100);
			R.safari();
		};
		obj[0].onmouseout = function(){
			obj.animate({fill:colO_hover},100);
			R.safari();
		};
		obj[0].onclick = function(){
			load_family_genome(current_chr,Math.floor(current_start+(current_end-current_start)/2),Math.ceil(current_end+(current_end-current_start)/2));
			R.safari();
		};
	})(down);

	var downer = R.set();
	downer.push(R.path("M"+xm+","+(y+25+r2*7)+
				" L"+(xm-r1)+","+(y+25+r2*6)+
				" L"+(xm+r1)+","+(y+25+r2*6)+"Z").attr({fill:colO_hover,"stroke-width":0}));
	downer.push(R.path("M"+xm+","+(y+25+r2*8-delta)+
				" L"+(xm-r1)+","+(y+25+r2*7-delta)+
				" L"+(xm+r1)+","+(y+25+r2*7-delta)+"Z").attr({fill:colO_hover,"stroke-width":0}));
	(function (obj){
		for(var i = 0 ; i < obj.length ; i++){
			obj[i][0].style.cursor = "pointer";
			obj[i][0].onmouseover = function(){
				obj[0].animate({fill:colO_hover2},100);
				obj[1].animate({fill:colO_hover2},100);
				R.safari();
			};
			obj[i][0].onmouseout = function(){
				obj[0].animate({fill:colO_hover},100);
				obj[1].animate({fill:colO_hover},100);
				R.safari();
			};
			obj[i][0].onclick = function(){
				load_family_genome(current_chr,current_end,2*current_end-current_start);
				R.safari();
			};
		}
	})(downer);
}

function show_denovo_muts(){
	if(req4.readyState == 4) {
		if(req4.status == 200) {
			var vsNodes = req4.responseXML.getElementsByTagName(xmlTagVariants);
			for(var i = 0 ; i < vsNodes.length ; i++){
				var vNodes = vsNodes[i].getElementsByTagName(xmlTagVariant);
				var vs_id = vsNodes[i].getAttribute(xmlAttributeId);
				var vPointer = 0;
				for(var j = 0 ; j < vNodes.length ; j++){
					var from = vNodes[j].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
					var to = vNodes[j].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
					var id = vNodes[j].getAttribute(xmlAttributeId);
					while(variants[vPointer].from < from){
						vPointer++;
					}
					if(from <= variants[vPointer].from && to >= variants[vPointer].to && id == variants[vPointer].id){
						if(vs_id == individuals[csi].fid){
							variants[vPointer].paternal = "N";
//							var vartable = document.getElementById("varlist_table");
//							if(vartable != undefined){
//								vartable.rows[vPointer+1].cells[5].innerHTML="N";
//							}
						}else if(vs_id == individuals[csi].mid){
							variants[vPointer].maternal = "N";
//							var vartable = document.getElementById("varlist_table");
//							if(vartable != undefined){
//								vartable.rows[vPointer+1].cells[6].innerHTML="N";
//							}
						}else if(vs_id == csi){
							change_variant_color(vPointer,colD);
						}
					}
				}
			}
		}
	}
}

function show_maternal_vars(){
	if(req5.readyState == 4) {
		if(req5.status == 200) {
			R_sremove();

			var vsNodes = req5.responseXML.getElementsByTagName(xmlTagVariants);
			for(var i = 0 ; i < vsNodes.length ; i++){
				var vNodes = vsNodes[i].getElementsByTagName(xmlTagVariant);
				var vs_id = vsNodes[i].getAttribute(xmlAttributeId);
				var vPointer = 0;
				for(var j = 0 ; j < vNodes.length ; j++){
					var from = vNodes[j].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
					var to = vNodes[j].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
					var id = vNodes[j].getAttribute(xmlAttributeId);
					while(variants[vPointer].from < from){
						vPointer++;
					}
					if(from <= variants[vPointer].from && to >= variants[vPointer].to && id == variants[vPointer].id){
						if(vs_id == individuals[csi].fid){
							variants[vPointer].paternal = "N";
//							var vartable = document.getElementById("varlist_table");
//							if(vartable != undefined){
//								vartable.rows[vPointer+1].cells[5].innerHTML="N";
//							}
						}else if(vs_id == individuals[csi].mid){
							variants[vPointer].maternal = "Y";
						}else if(vs_id == csi){
							change_variant_color(vPointer,colP);
						}
					}
				}
			}
		}
	}
}

function show_paternal_vars(){
	if(req6.readyState == 4) {
		if(req6.status == 200) {
			var vsNodes = req6.responseXML.getElementsByTagName(xmlTagVariants);
			for(var i = 0 ; i < vsNodes.length ; i++){
				var vNodes = vsNodes[i].getElementsByTagName(xmlTagVariant);
				var vs_id = vsNodes[i].getAttribute(xmlAttributeId);
				var vPointer = 0;
				for(var j = 0 ; j < vNodes.length ; j++){
					var from = vNodes[j].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
					var to = vNodes[j].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
					var id = vNodes[j].getAttribute(xmlAttributeId);
					while(variants[vPointer].from < from){
						vPointer++;
					}
					if(from <= variants[vPointer].from && to >= variants[vPointer].to && id == variants[vPointer].id){
						if(vs_id == individuals[csi].fid){
							variants[vPointer].paternal = "Y";
						}else if(vs_id == individuals[csi].mid){
							variants[vPointer].maternal = "N";
//							var vartable = document.getElementById("varlist_table");
//							if(vartable != undefined){
//								vartable.rows[vPointer+1].cells[6].innerHTML="N";
//							}
						}else if(vs_id == csi){
							change_variant_color(vPointer,colM);
						}
					}
				}
			}
		}
	}
}


function show_colorful_vars(){
	if(req5.readyState == 4) {
		if(req5.status == 200) {
			var vsNodes = req5.responseXML.getElementsByTagName(xmlTagVariants);
			//alert(req5.responseText);
			for(var i = 0 ; i < vsNodes.length ; i++){
				var vNodes = vsNodes[i].getElementsByTagName(xmlTagVariant);
				var vs_id = vsNodes[i].getAttribute(xmlAttributeId);
				var vPointer = 0;
				for(var j = 0 ; j < vNodes.length ; j++){
					var from = vNodes[j].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
					var to = vNodes[j].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
					var id = vNodes[j].getAttribute(xmlAttributeId);
					var type = vNodes[j].getAttribute(xmlAttributeType);

					if(id == "."){
						id = "Unnamed "+type;
					}
					while(variants[vPointer].from < from){
						vPointer++;
					}
					if(from <= variants[vPointer].from && to >= variants[vPointer].to && id == variants[vPointer].id){
						if(vsNodes.length == 1 && vs_id == "Intersection"){
							change_variant_color(vPointer,colInter);
						}else if(vsNodes.length == 1 && vs_id == "Difference"){
							change_variant_color(vPointer,colDiff);
						}else if(vs_id == individuals[csi].fid){
							variants[vPointer].paternal = "Y";
							variants[vPointer].maternal = "N";
							change_variant_color(vPointer,colP);
						}else if(vs_id == individuals[csi].mid){
							variants[vPointer].paternal = "N"
							variants[vPointer].maternal = "Y";
							change_variant_color(vPointer,colM);
						}else if(vs_id == csi){
							variants[vPointer].paternal = "N";
							variants[vPointer].maternal = "N";
							change_variant_color(vPointer,colD);
						}
					}
				}
			}
			R_sremove();
		}
	}
}
function clear_colorful_vars(){
	for(var i = 0 ; i < variants.length ; i++){
		change_variant_color(i,colO);
	}
}
function highlight_vars(){
	var len = 0;
	for(id in compared_individuals){
		if(id == csi){
			delete compared_individuals[id];
		}else{
			len++;
		}
	}
	clear_colorful_vars();
	close_shared_different();
	if((compare_method == "getDifference" || compare_method == "getIntersection") 
		&& len > 0 && len <= 9 && individuals[csi] != undefined){
		var sets5 = "";
		for(var id in compared_individuals){
			sets5 += id + ":";
		}
		if(compare_method == "getDifference"){
			sets5 = sets5.substring(0,sets5.length-1) + ",";
		}
		sets5 += csi;
		var form5 = new FormData();
		form5.append("sets",sets5);
		form5.append("enctype","multipart/form-data");
		req5.onreadystatechange = show_colorful_vars;
		querry = "action="+compare_method+"&tracks="+trackname+"&chr="+current_chr+"&start="+current_start+"&end="+current_end;
		req5.open("POST","servlet/test.do?"+querry,true);
		req5.send(form5);
	}else{
		R_sremove();
	}
}
function trioAnalysis(){
	if(compare_method == "trioAnalysis" 
		&& individuals[csi].fid != "0" && individuals[csi].mid != "0"){
		clear_colorful_vars();
		req5.onreadystatechange = show_colorful_vars;
		querry = "action=trioAnalysis&tracks="+trackname+"&chr="+current_chr+"&start="+current_start+"&end="+current_end+"&id="+csi;
		req5.open("GET","servlet/test.do?"+querry,true);
		req5.send();
	}else{
		R_sremove();
	}
}
function show_vars(){
	if(req3.readyState == 4) {
		if(req3.status == 200) {
			gdf_list = [];	
			init_individual_vars();
			list_variants();
			plot_variants();
			plot_genes();
			
			/*
			var sets4 = individuals[csi].fid
					+":"+individuals[csi].mid
					+","+csi;
			var sets5 = individuals[csi].fid
					+","+individuals[csi].mid
					+":"+csi;
			var sets6 = individuals[csi].mid
					+","+individuals[csi].fid
					+":"+csi;
			*/
			if(variants.length < (R_height - R_top - R_bottom)/15){
				req7.onreadystatechange = show_ld_relationships;
				querry = "action=getLD&tracks="+trackname+"&start="+current_start+"&end="+current_end+"&id="+csi;
				req7.open("GET","servlet/test.do?"+querry,true);
				req7.send();
			}

			R_sremove();
			if(compare_method == "trioAnalysis"){
				R_sremove = spinner(R);
				trioAnalysis();
			}else if(compare_method == "getIntersection" || compare_method == "getDifference"){
				R_sremove = spinner(R);
				highlight_vars();
			}

			/*
			var form5 = new FormData();
			form5.append("sets",sets5);
			form5.append("enctype","multipart/form-data");
			req5.onreadystatechange = show_maternal_vars;
			querry = "action=getDifference&tracks="+trackname+"&chr="+current_chr+"&start="+current_start+"&end="+current_end;
			req5.open("POST","servlet/test.do?"+querry,true);
			req5.send(form5);
		
			var form6 = new FormData();
			form6.append("sets",sets6);
			req6.onreadystatechange = show_paternal_vars;
			querry = "action=getDifference&tracks="+trackname+"&chr="+current_chr+"&start="+current_start+"&end="+current_end;
			req6.open("POST","servlet/test.do?"+querry,true);
			req6.send(form6);

			var form4 = new FormData();
			form4.append("sets",sets4);
			form4.append("enctype","multipart/form-data");
			req4.onreadystatechange = show_denovo_muts;
			querry = "action=getDifference&tracks="+trackname+"&chr="+current_chr+"&start="+current_start+"&end="+current_end;
			req4.open("POST","servlet/test.do?"+querry,true);
			req4.send(form4);
			*/
		}
	}
}
function show_ld_relationships(){
	if(req7.readyState == 4) {
		if(req7.status == 200) {
			var pattern = /<.*?>/g;
			var lds = req7.responseText.replace(pattern,"").split(",");
			var l = R_height - R_top - R_bottom;
			var w = R_width - R_left - R_right;
			for(var i = 0 ; i < lds.length ; i++){
				var ld = lds[i].split(";");
				if(variants_byid[ld[0]] != undefined && variants_byid[ld[1]] != undefined){
					var var1_pos = ((variants[variants_byid[ld[0]]].to+variants[variants_byid[ld[0]]].from)/2 - current_start)/(current_end - current_start + 1)*l+R_top;
					var var2_pos = ((variants[variants_byid[ld[1]]].to+variants[variants_byid[ld[1]]].from)/2 - current_start)/(current_end - current_start + 1)*l+R_top;
					var x = R_left+w/2-30;
					var direction = 0;
					if(ld[2] == 1){
						x = R_left+w/2+30;
						direction = 1;
					}
					var obj_temp = R.path("M"+x+","+var1_pos+" A"+(var2_pos-var1_pos)+","+(var2_pos-var1_pos)+" 0 0,"+direction+" "+x+","+var2_pos).attr({stroke:Raphael.getRGB("rgb(255,"+(1-ld[3])*255+","+(1-ld[3])*255+")").hex});
					if(variants[variants_byid[ld[0]]].lds == undefined){
						variants[variants_byid[ld[0]]].lds = R.set();
					}
					if(variants[variants_byid[ld[1]]].lds == undefined){
						variants[variants_byid[ld[1]]].lds = R.set();
					}
					variants[variants_byid[ld[0]]].lds.push(obj_temp);
					variants[variants_byid[ld[1]]].lds.push(obj_temp);
//					obj_temp.hide(); // for hide all lds in start, and show lds when select an individual
					all_lds.push(obj_temp); //for show all lds in start, and hide inconnected lds when select an individual
				}
			}
		}
	}
}
function list_variants(){
	document.getElementById("varlist").innerHTML= "";
	var temp = document.createElement("table");
	document.getElementById("varlist").appendChild(temp);
	temp.className = "listt_table";
	temp.id = "varlist_table";
	var temp_tr = temp.insertRow(-1);
	temp_tr.innerHTML = 
		"<th>ID</th>"+
		"<th>Type</th>"+
		"<th>Pos</th>"+
		"<th>Letter</th>"+
		"<th>Genotype</th>"+
//		"<th>Paternal</th>"+
//		"<th>Maternal</th>"+
		"<th>Function</th>"+
		"<th>Checked</th>";
	for(var i=0 ; i<variants.length ; i++){ 
		temp_tr = temp.insertRow(-1);
		temp_tr.style.background = "#CCC";
		var temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = variants[i].id;
		temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = variants[i].type;
		temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = variants[i].chr+":"+variants[i].from+"-"+variants[i].to;
		temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = variants[i].letter;
		temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = variants[i].genotype;
//		temp_td = temp_tr.insertCell(-1);
//		temp_td.innerHTML = variants[i].paternal;
//		temp_td = temp_tr.insertCell(-1);
//		temp_td.innerHTML = variants[i].maternal;
		temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = variants[i].functional;
		temp_td = temp_tr.insertCell(-1);

		var radioObj = document.createElement("input");
		temp_td.appendChild(radioObj);
		radioObj.type = "radio";
		radioObj.name = "varlist_select";
		radioObj.id = i + "__varlist_select";
		if(variants[i].id == csv){
			radioObj.checked = true;
		}
		radioObj.onclick = function(event){
			var target = event.target || event.srcElement;
			var idx = target.getAttribute("id").split("__")[0];
			select_a_variant(idx);
		};
	}
}

function plot_variants(){
	var l = R_height - R_top - R_bottom;
	var w = R_width - R_left - R_right;
	var font_height = 15;
	var text_length = 100;
	var bin_size = 10;
	var max_bar_length = 100;
	var font_size2_text = "12px \"Trebuchet MS\", Arial, sans-serif";
	var pixel_per_variant = 1;

	if(variants[0] != undefined && variants[0].genotype.indexOf("|")>=0){
		R.rect(R_left+w/2-15,R_top,10,l,5).attr({fill:colO_hover,stroke:colO_hover});
		R.rect(R_left+w/2+5,R_top,10,l,5).attr({fill:colO_hover,stroke:colO_hover});
	} else {
		R.rect(R_left+w/2-5,R_top,10,l,5).attr({fill:colO_hover,stroke:colO_hover});
	}

	if(variants.length < l/font_height){
		var current_pos = 0 - font_height;
		for(var i = 0 ; i < variants.length ; i++){
			var natual_pos = ((variants[i].to+variants[i].from)/2 - current_start)/(current_end - current_start + 1)*l;
			var name_pos = natual_pos;
			natual_pos += R_top;

			if(name_pos < current_pos + font_height){
				name_pos = current_pos + font_height;
			} else if(name_pos < i*font_height){
				name_pos = i*font_height;
			} else if(name_pos > l - (variants.length - i - 1)*font_height){
				name_pos = l - (variants.length - i - 1)*font_height;
			}
			current_pos = name_pos;
			name_pos += R_top;

			variants[i].point = [];
			variants[i].name = [];
			variants[i].lines = [];

			var name_text = variants[i].id;

			if(variants[i].id.indexOf("Unnamed") == 0  && variants[i].dd && variants[i].dd.indexOf("rs") == 0){
				name_text = variants[i].dd + "*";
			}

			if(variants[i].genotypes[0] == undefined || variants[i].genotypes[0] == "0"){
				variants[i].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:"#FFF",stroke:colO});
			}else{
				variants[i].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:colO,stroke:colO});
				variants[i].lines[0] = R.path("M"+(R_left+text_length+2)+","+name_pos+" L"+(R_left+text_length+20)+","+name_pos+" L"+(R_left+w/2-52)+","+natual_pos+" L"+(R_left+w/2-33)+","+natual_pos).attr({stroke:colO,opacity:1});

				variants[i].name[0] = R.text(R_left+text_length,name_pos,name_text).attr({font:font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill:colO,"font-weight":"normal"});

				(function(idx){
					variants[idx].name[0][0].style.cursor = "pointer";
					variants[idx].name[0][0].onmouseover = function(){
						variants[idx].name[0].animate({fill:colO_hover},100);
						R.safari();
					};
					variants[idx].name[0][0].onmouseout = function(){
						variants[idx].name[0].animate({fill:colO},100);
						R.safari();
					};
					variants[idx].name[0][0].onclick = function(){
						select_a_variant(idx);
						R.safari();
					};
				})(i);

			}
			if(variants[i].genotypes[1] == undefined || variants[i].genotypes[1] == "0"){
				variants[i].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:"#FFF",stroke:colO});
			}else{
				variants[i].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:colO,stroke:colO});
				variants[i].lines[1] = R.path("M"+(R_left+w-text_length-2)+","+name_pos+" L"+(R_left+w-text_length-20)+","+name_pos+" L"+(R_left+w/2+52)+","+natual_pos+" L"+(R_left+w/2+33)+","+natual_pos).attr({stroke:colO,opacity:1});

				variants[i].name[1] = R.text(R_left+w-text_length,name_pos,name_text).attr({font:font_size2_text,opacity:1,"text-anchor":"start"}).attr({fill:colO});

				(function(idx){
					variants[idx].name[1][0].style.cursor = "pointer";
					variants[idx].name[1][0].onmouseover = function(){
						variants[idx].name[1].animate({fill:colO_hover},100);
						R.safari();
					};
					variants[idx].name[1][0].onmouseout = function(){
						variants[idx].name[1].animate({fill:colO},100);
						R.safari();
					};
					variants[idx].name[1][0].onclick = function(){
						select_a_variant(idx);
						R.safari();
					};
				})(i);

			}
		}
	} else if (variants.length < l/3){
		for(var i = 0 ; i < variants.length ; i++){
			var natual_pos = ((variants[i].to+variants[i].from)/2 - current_start)/(current_end - current_start + 1)*l;
			natual_pos += R_top;

			variants[i].point = [];

			if(variants[i].genotypes[0] == undefined || variants[i].genotypes[0] == "0"){
				variants[i].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:"#FFF",stroke:colO});
			}else{
				variants[i].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:colO,stroke:colO});
			}
			if(variants[i].genotypes[1] == undefined || variants[i].genotypes[1] == "0"){
				variants[i].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:"#FFF",stroke:colO});
			}else{
				variants[i].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:colO,stroke:colO});
			}
		}
	} else {
		var bins = [];
		bins[0] = [];
		bins[1] = [];
		for(var i = 0 ; i < variants.length ; i++){
			var natual_pos = ((variants[i].to+variants[i].from)/2 - current_start)/(current_end - current_start + 1)*(l/bin_size);
			if(variants[i].genotypes[0] != undefined && variants[i].genotypes[0] != "0"){
				if(bins[0][Math.floor(natual_pos)] == undefined){
					bins[0][Math.floor(natual_pos)] = 1;
				}else{
					bins[0][Math.floor(natual_pos)] ++;
				}
			}
			if(variants[i].genotypes[1] != undefined && variants[i].genotypes[1] != "0"){
				if(bins[1][Math.floor(natual_pos)] == undefined){
					bins[1][Math.floor(natual_pos)] = 1;
				}else{
					bins[1][Math.floor(natual_pos)] ++;
				}
			}
		}
		for(var i = 0 ; i < bins[0].length ; i++){
			if(bins[0][i] == undefined){
				bins[0][i] = 0;
			}
			R.rect(R_left+w/2-40-bins[0][i]*pixel_per_variant,i*bin_size+R_top,bins[0][i]*pixel_per_variant,bin_size,0).attr({fill:colO_hover,stroke:colO_hover});
		}
		for(var i = 0 ; i < bins[1].length ; i++){
			if(bins[1][i] == undefined){
				bins[1][i] = 0;
			}
			R.rect(R_left+w/2+40,i*bin_size+R_top,bins[1][i]*pixel_per_variant,bin_size,0).attr({fill:colO_hover,stroke:colO_hover});
		}
	}
}

function show_same_collectionvar(){
	if(reqV.readyState == 4) {
		if(reqV.status == 200){
			var xmlDoc=null;
			var xmlString = reqV.responseText;
			if(!window.DOMParser && window.ActiveXObject){
				var xmlDomVersions = ['MSXML.2.DOMDocument.6.0','MSXML.2.DOMDocument.3.0','Microsoft.XMLDOM'];
				for(var i=0;i<xmlDomVersions.length;i++){
					try{
						xmlDoc = new ActiveXObject(xmlDomVersions[i]);
						xmlDoc.async = false;
						xmlDoc.loadXML(xmlString); 
						break;
					}catch(e){
					}
				}
			}
			else if(window.DOMParser && document.implementation && document.implementation.createDocument){
				try{
					domParser = new  DOMParser();
					xmlDoc = domParser.parseFromString(xmlString, 'text/xml');
				}catch(e){
				}
			}
			/*
			var var_id;
			for(var i in individuals){
				if(individuals[i].selected){
					var_id=i;
					break;
				}
			}
			*/
			if(csi!=undefined){	
				for(var family_member in families){
					if(family_member != "roots" && individuals[family_member].ifs == "true" && families[family_member].markobj != undefined){
						for(var temp_root in families[family_member].markobj){
							families[family_member].markobj[temp_root].hide();
						}
					}
				}
				if(xmlDoc.getElementsByTagName("IndividualOrder")[0]!=null && xmlDoc.getElementsByTagName("IndividualOrder")[0].firstChild!=null){			
				//	alert("OK, the value is " + xmlDoc.getElementsByTagName("IndividualOrder")[0].firstChild.nodeValue);
					var collectionlist = xmlDoc.getElementsByTagName("IndividualOrder")[0].firstChild.nodeValue.split(",");
					if(collectionlist!=null){
						for(var j in collectionlist){
							var id_info = collectionlist[j].split(":");
							if(individuals[id_info[0]].ifs == "true"){
								for(var temp_root in families[id_info[0]].markobj){
									if(families[id].markobj[temp_root] != undefined){
										families[id].markobj[temp_root].attr({text:id_info[2]});
										families[id].markobj[temp_root].show();
									}
								}														
							}
						}
					}
				}
				else{
//					alert("Nothing!");
				}
			}
		}
	}
}

function select_a_variant(idx){
	if(variants[idx] != undefined){
		////////////////////insert request of searchIndividual
		/*alert(variants[idx].chr + ":"  + variants[idx].from + ":" + variants[idx].to + ":" + variants[idx].letter);
		var varString;
		if(variants[idx].letter=="--"){
			varString = variants[idx].chr + ":"  + variants[idx].from + ":" + variants[idx].to + ":-";
		}else{
			varString = variants[idx].chr + ":"  + variants[idx].from + ":" + variants[idx].to + ":" + variants[idx].letter;
		}
		var formV = new FormData();
		formV.append("variants",varString);
		formV.append("enctype","multipart/form-data");
		reqV.onreadystatechange = show_same_collectionvar;
		querry = "action=searchIndividual&tracks="+trackname;
		reqV.open("POST","servlet/test.do?"+querry,true);
		reqV.send(formV);
		
		*//////////////////////////////

		var radioObj = document.getElementById(idx+"__varlist_select")
		if(variants[idx].selected){
			for(var family_member in families){
				if(family_member != "roots" && individuals[family_member].ifs == "true" && families[family_member].markobj != undefined){
					for(var temp_root in families[family_member].markobj){
						families[family_member].markobj[temp_root].hide();
					}
				}
			}
			variants[idx].selected = false;
			radioObj.checked = false;
			if(variants[idx].name != undefined){
				if(variants[idx].genotypes[0] != undefined && variants[idx].genotypes[0] != "0"){
					variants[idx].name[0].attr({"font-weight":"normal"});
					variants[idx].point[0].attr({"stroke-width":1});
					variants[idx].lines[0].attr({"stroke-width":1});
				}
				if(variants[idx].genotypes[1] != undefined && variants[idx].genotypes[1] != "0"){
					variants[idx].name[1].attr({"font-weight":"normal"});
					variants[idx].point[1].attr({"stroke-width":1});
					variants[idx].lines[1].attr({"stroke-width":1});
				}
				if(variants[idx].functional_v != undefined){
					for(var fvidx = 0 ; fvidx < variants[idx].functional_v.length ; fvidx++){
						variants[idx].functional_v[fvidx].obj.attr({"font-weight":"normal","stroke-width":1});
					}
				}
				all_lds.show();
				if(variants[idx].lds != undefined){
					variants[idx].lds.hide();
				}
			}
			csv = -1;
		} else {
			var varString;
			if(variants[idx].letter=="--"){
				varString = variants[idx].chr + ":"  + variants[idx].from + ":" + variants[idx].to + ":-";
			}else{
				varString = variants[idx].chr + ":"  + variants[idx].from + ":" + variants[idx].to + ":" + variants[idx].letter;
			}
			var formV = new FormData();
			formV.append("variants",varString);
			formV.append("enctype","multipart/form-data");
			reqV.onreadystatechange = show_same_collectionvar;
			querry = "action=searchIndividual&tracks="+trackname;
			reqV.open("POST","servlet/test.do?"+querry,true);
			reqV.send(formV);
			if(csv>=0 && variants[csv] != undefined){
				variants[csv].selected = false;
			
				if(variants[csv].name != undefined){
					if(variants[csv].genotypes[0] != undefined && variants[csv].genotypes[0] != "0"){
						variants[csv].name[0].attr({"font-weight":"normal"});
						variants[csv].point[0].attr({"stroke-width":1});
						variants[csv].lines[0].attr({"stroke-width":1});
					}
					if(variants[csv].genotypes[1] != undefined && variants[csv].genotypes[1] != "0"){
						variants[csv].name[1].attr({"font-weight":"normal"});
						variants[csv].point[1].attr({"stroke-width":1});
						variants[csv].lines[1].attr({"stroke-width":1});
					}
					if(variants[csv].functional_v != undefined){
						for(var fvidx = 0 ; fvidx < variants[csv].functional_v.length ; fvidx++){
							variants[csv].functional_v[fvidx].obj.attr({"font-weight":"normal","stroke-width":1});
						}
					}
					if(variants[csv].lds != undefined){
						variants[csv].lds.hide();
					}
				}
			}
			variants[idx].selected = true;
			radioObj.checked = true;
			if(variants[idx].name != undefined){
				if(variants[idx].genotypes[0] != undefined && variants[idx].genotypes[0] != "0"){
					variants[idx].name[0].attr({"font-weight":"bolder"});
					variants[idx].point[0].attr({"stroke-width":2});
					variants[idx].lines[0].attr({"stroke-width":2});
				}
				if(variants[idx].genotypes[1] != undefined && variants[idx].genotypes[1] != "0"){
					variants[idx].name[1].attr({"font-weight":"bolder"});
					variants[idx].point[1].attr({"stroke-width":2});
					variants[idx].lines[1].attr({"stroke-width":2});
				}
				if(variants[idx].functional_v != undefined){
					for(var fvidx = 0 ; fvidx < variants[idx].functional_v.length ; fvidx++){
						variants[idx].functional_v[fvidx].obj.attr({"font-weight":"bolder","stroke-width":2});
					}
				}
				all_lds.hide();
				if(variants[idx].lds != undefined){
					variants[idx].lds.show();
				}
			}
			csv = idx;
		}
	}
}

function change_variant_color(vPointer,color){
	var l = R_height - R_top - R_bottom;
	var w = R_width - R_left - R_right;
	if(variants[vPointer].name != undefined){

		var color2 = colO_hover;
		if(color == colP){
			color2 = colP_hover;
		}else if(color == colD){
			color2 = colD_hover;
		}else if(color == colM){
			color2 = colM_hover;
		}else if(color == colInter){
			color2 = colInter_hover;
		}else if(color == colDiff){
			color2 = colDiff_hover;
		}

		if(variants[vPointer].genotypes[0] != undefined && variants[vPointer].genotypes[0] != "0"){
			variants[vPointer].name[0].attr({fill:color}).toFront();

			(function(idx,color){
				variants[idx].name[0][0].style.cursor = "pointer";
				variants[idx].name[0][0].onmouseover = function(){
					variants[idx].name[0].animate({fill:color2},100);
					R.safari();
				};
				variants[idx].name[0][0].onmouseout = function(){
					variants[idx].name[0].animate({fill:color},100);
					R.safari();
				};
				variants[idx].name[0][0].onclick = function(){
					select_a_variant(idx);
					R.safari();
				};
			})(vPointer,color);
		}
		if(variants[vPointer].genotypes[1] != undefined && variants[vPointer].genotypes[1] != "0"){
			variants[vPointer].name[1].attr({fill:color}).toFront();

			(function(idx,color){
				variants[idx].name[1][0].style.cursor = "pointer";
				variants[idx].name[1][0].onmouseover = function(){
					variants[idx].name[1].animate({fill:color2},100);
					R.safari();
				};
				variants[idx].name[1][0].onmouseout = function(){
					variants[idx].name[1].animate({fill:color},100);
					R.safari();
				};
				variants[idx].name[1][0].onclick = function(){
					select_a_variant(idx);
					R.safari();
				};
			})(vPointer,color);
		}
	}
	if(variants[vPointer].point != undefined && variants.length < l/3){
		if(variants[vPointer].genotypes[0] != undefined && variants[vPointer].genotypes[0] != "0"){
			variants[vPointer].point[0].attr({fill:color,stroke:color}).toFront();
		}
		if(variants[vPointer].genotypes[1] != undefined && variants[vPointer].genotypes[1] != "0"){
			variants[vPointer].point[1].attr({fill:color,stroke:color}).toFront();
		}
	} else if(color != colO){
		var natual_pos = ((variants[vPointer].to+variants[vPointer].from)/2 - current_start)/(current_end - current_start + 1)*l + R_top;
		variants[vPointer].point = [];
		if(variants[vPointer].genotypes[0] != undefined && variants[vPointer].genotypes[0] != "0"){
			variants[vPointer].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:color,stroke:color});
		}
		if(variants[vPointer].genotypes[1] != undefined && variants[vPointer].genotypes[1] != "0"){
			variants[vPointer].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:color,stroke:color});
		}
	} else if(variants.length >= l/3){
		if(variants[vPointer].point != undefined){
			if(variants[vPointer].point[0] != undefined){
				variants[vPointer].point[0].remove();
			}
			if(variants[vPointer].point[1] != undefined){
				variants[vPointer].point[1].remove();
			}
			delete variants[vPointer].point;
		}
	}
	if(variants[vPointer].lines != undefined){
		if(variants[vPointer].genotypes[0] != undefined && variants[vPointer].genotypes[0] != "0"){
			variants[vPointer].lines[0].attr({stroke:color}).toFront();
		}
		if(variants[vPointer].genotypes[1] != undefined && variants[vPointer].genotypes[1] != "0"){
			variants[vPointer].lines[1].attr({stroke:color}).toFront();
		}
	}
}
function plot_genes(){
	var l = R_height - R_top - R_bottom;
	var w = R_width - R_left - R_right;
	var font_height = 15;
	var box_width = 10;
	var band_width = 6;
	var deslinelength = 80;
	var brwplotCanvas = $(document.getElementById("brwplot"));	

	var vari = 0;
	var plotted_amino_var = {};
	var current_pos = 0 - font_height;
	for(var i = 0 ; i < functional_vPointer.length ; i++){
		for(var fvidx = 0 ; fvidx < variants[functional_vPointer[i]].functional_v.length ; fvidx++){
			var var_top = map_coord(l,variants[functional_vPointer[i]].functional_v[fvidx].from);
			var var_bot = map_coord(l,variants[functional_vPointer[i]].functional_v[fvidx].to);
			if(var_top == l || var_bot == 0){
				continue;
			}
			var natual_pos = (var_top+var_bot)/2;
			var name_pos = natual_pos;
			natual_pos += R_top;
			if(name_pos < current_pos + font_height){
				name_pos = current_pos + font_height;
			} else if(name_pos < vari*font_height){
				name_pos = vari*font_height;
			} else if(name_pos > l - (functional_vnum - vari - 1)*font_height){
				name_pos = l - (functional_vnum - vari - 1)*font_height;
			}
			vari++;

			current_pos = name_pos;
			name_pos += R_top;
			variants[functional_vPointer[i]].functional_v[fvidx].obj = draw_var(natual_pos,name_pos,l,box_width,variants[functional_vPointer[i]].functional_v[fvidx].letter);
		}
	}
	for(var i = 0 ; i < genes.length ; i++){
		genes[i].obj = R.set();
		
		if(genes[i].subs == undefined){
			var main = draw_box(genes[i].from,genes[i].to,l,box_width);
			genes[i].obj.push(main);
//			var direction = draw_triangle(genes[i].from,genes[i].to,l,genes[i].strand,box_width);
//			if(direction != null){
//				genes[i].obj.push(direction);
//			}
		} else {
			for(var j = 0 ; j < genes[i].subs.length ; j++){
				var temp = null;
				if(genes[i].subs[j].type == "X"){
					temp = draw_box(genes[i].subs[j].from,genes[i].subs[j].to,l,box_width);
				} else if(genes[i].subs[j].type == "shBox"){
					temp = draw_box(genes[i].subs[j].from,genes[i].subs[j].to,l,box_width);
				} else if(genes[i].subs[j].type == "eBox"){
					temp = draw_box(genes[i].subs[j].from,genes[i].subs[j].to,l,box_width);
				} else if(genes[i].subs[j].type == "skBox"){
					temp = draw_box(genes[i].subs[j].from,genes[i].subs[j].to,l,box_width);
				} else if(genes[i].subs[j].type == "lBox"){
					temp = draw_box(genes[i].subs[j].from,genes[i].subs[j].to,l,box_width);
				} else if(genes[i].subs[j].type == "psBox"){
					temp = draw_box(genes[i].subs[j].from,genes[i].subs[j].to,l,box_width);
				} else if(genes[i].subs[j].type == "seBox"){
					temp = draw_box(genes[i].subs[j].from,genes[i].subs[j].to,l,box_width);
				} else if(genes[i].subs[j].type == "pseBox"){
					temp = draw_box(genes[i].subs[j].from,genes[i].subs[j].to,l,box_width);
				} else if(genes[i].subs[j].type == "D"){
					temp = draw_band(genes[i].subs[j].from,genes[i].subs[j].to,l,band_width,box_width);
				} else if(genes[i].subs[j].type == "eBand"){
					temp = draw_band(genes[i].subs[j].from,genes[i].subs[j].to,l,band_width,box_width);
				} else if(genes[i].subs[j].type == "sBand"){
					temp = draw_band(genes[i].subs[j].from,genes[i].subs[j].to,l,band_width,box_width);
				} else if(genes[i].subs[j].type == "L"){
					temp = draw_line(genes[i].subs[j].from,genes[i].subs[j].to,l,genes[i].strand,box_width);
				}
				if(temp != null){
					genes[i].obj.push(temp);
				}
			}
		}
		if(genes[i].vars != undefined){
			for(var fv in genes[i].vars){
				genes[i].obj.push(variants[functional_v[fv]].functional_v[genes[i].vars[fv]].obj);
			}
		}
	}
	if(genes.length > 0){
		var font_size2_text = "14px \"Trebuchet MS\", Arial, sans-serif";
		draw_arrow(R_width-R_right+10+box_width/2,0,-1);
		draw_arrow(R_width-R_right+10+box_width/2,0,1);
		draw_arrow(R_width-R_right+10+box_width/2,1,-1);
		draw_arrow(R_width-R_right+10+box_width/2,1,1);
		cssObj = R.text(R_width-R_right+10+box_width/2,R_top-8-20,"ALL").attr({font:font_size2_text});
		cstObj = R.text(R_width-R_right+10+box_width/2,R_top-8,"ALL").attr({font:font_size2_text});
	}
	
	var r1 = 15;
	var r2 = 25;
	var xm = R_width + 70;
	var y = (R_height-R_bottom-R_top)/2;
	//var gdf_icon = R.set();
	var font_size2_text = "15px \"Trebuchet MS\", Arial, sans-serif";
	var showflag = 0;
	//gdf_icon = R.path("M16,1.466C7.973,1.466,1.466,7.973,1.466,16c0,8.027,6.507,14.534,14.534,14.534c8.027,0,14.534-6.507,14.534-14.534C30.534,7.973,24.027,1.466,16,1.466z M17.328,24.371h-2.707v-2.596h2.707V24.371zM17.328,19.003v0.858h-2.707v-1.057c0-3.19,3.63-3.696,3.63-5.963c0-1.034-0.924-1.826-2.134-1.826c-1.254,0-2.354,0.924-2.354,0.924l-1.541-1.915c0,0,1.519-1.584,4.137-1.584c2.487,0,4.796,1.54,4.796,4.136C21.156,16.208,17.328,16.627,17.328,19.003z").attr({fill: colO_hover, stroke: "none",cursor:"pointer"});	
	gdf_icon = R.path("M26.711,14.086L16.914,4.29c-0.778-0.778-2.051-0.778-2.829,0L4.29,14.086c-0.778,0.778-0.778,2.05,0,2.829l9.796,9.796c0.778,0.777,2.051,0.777,2.829,0l9.797-9.797C27.488,16.136,27.488,14.864,26.711,14.086zM14.702,8.981c0.22-0.238,0.501-0.357,0.844-0.357s0.624,0.118,0.844,0.353c0.221,0.235,0.33,0.531,0.33,0.885c0,0.306-0.101,1.333-0.303,3.082c-0.201,1.749-0.379,3.439-0.531,5.072H15.17c-0.135-1.633-0.301-3.323-0.5-5.072c-0.198-1.749-0.298-2.776-0.298-3.082C14.372,9.513,14.482,9.22,14.702,8.981zM16.431,21.799c-0.247,0.241-0.542,0.362-0.885,0.362s-0.638-0.121-0.885-0.362c-0.248-0.241-0.372-0.533-0.372-0.876s0.124-0.638,0.372-0.885c0.247-0.248,0.542-0.372,0.885-0.372s0.638,0.124,0.885,0.372c0.248,0.247,0.372,0.542,0.372,0.885S16.679,21.558,16.431,21.799z").attr({fill: colO_hover, stroke: "none",cursor:"pointer"});	
	gdf_icon.stop().animate({transform: "t"+(xm-65)+" "+y+" s2.0"});	
	
	/*gdf_icon.push(R.ellipse(xm,y,r2,r2).attr({fill:colO_hover,stroke:colO_hover}));
	gdf_icon.push(R.ellipse(xm,y,r1,r1).attr({fill:"#FFF",stroke:colO_hover}));
	gdf_icon.push(R.text(xm,y,"?").attr({font:font_size2_text,cursor:"pointer","font-weight":"bolder"}));
	*/
	gdf_icon.hide();
	if(gdf_list.length > 0){
		gdf_icon.show();
		gdf_icon.mouseover(function(){
			gdf_icon.animate({fill:colO_hover2},200);
		});
		gdf_icon.mouseout(function(){
			gdf_icon.animate({fill:colO_hover},200);
		});
		gdf_icon.mousedown(function(){
			var t=document.getElementById("gdftablebody");
			t.innerHTML = "";
			for(var i=0; i<(gdf_list.length+1); i++){
				var row=document.createElement("tr");
				row.id = "row"+i;
				var cell3=document.createElement("td");
				var cell2=document.createElement("td");
				var cell1=document.createElement("td");
				if(i==0){
					cell3.appendChild(document.createTextNode("Source"));
					cell2.appendChild(document.createTextNode("Symbol"));
					cell1.appendChild(document.createTextNode("Id"));
				}else{
					cell3.id="gdfsource"+i;
					cell3.appendChild(document.createTextNode(gdf_list[i-1].source));
					cell2.appendChild(document.createTextNode(gdf_list[i-1].symbol));
					cell1.appendChild(document.createTextNode(gdf_list[i-1].id));
				}
				row.appendChild(cell1);
				row.appendChild(cell2);
				row.appendChild(cell3);
				document.getElementById("gdftablebody").appendChild(row);
			}
			var rows=document.getElementById("gdf_list").rows;  
			document.getElementById("gdf_list").style.fontSize="13px";  
			if(rows.length>1){  
				for(var i=1;i<rows.length;i++){  
					(function(i){  
						var obj=rows[i];
						var patternOMIM = /^.*,.*(\d{6}).*$/;
						if(patternOMIM.test(gdf_list[i-1].id)){
							$(document.getElementById("gdfsource"+i)).css("cursor","pointer"); 
							document.getElementById("gdfsource"+i).style.textDecoration="underline";
							var OMIMentry = RegExp.$1;
							obj.cells[2].onclick = function(){
								window.open("http://omim.org/entry/"+OMIMentry);
							};
							obj.cells[2].onmouseover = function(){
								obj.cells[2].style.color="#FF0";
							};
							obj.cells[2].onmouseout = function(){
								obj.cells[2].style.color="FFF";
							};
						}
					})(i);  
				}  
			} 
			$(document.getElementById("gdflist")).css("position", "absolute");
			$(document.getElementById("gdflist")).css("top", brwplotCanvas.position().top+y+28);
			$(document.getElementById("gdflist")).css("left", brwplotCanvas.position().left+xm-270);
			if(showflag==0){
				showflag = 1;
				$(document.getElementById("gdflist")).css("display", "block");
			}else{
				showflag = 0;
				$(document.getElementById("gdflist")).css("display", "none");
			}
		});
	}
	
	function mousedownOutsideRepeatTooltip(evt){
		evt = evt || window.event;
		var eventTarget = evt.target || evt.srcElement;
		var flag=0;
		while(eventTarget){
			if(eventTarget==document.getElementById("gdflist")){
				flag=1;
				break;
			}else{
				eventTarget = eventTarget.parentNode;
			}
		}
		if(flag==0) {
			document.getElementById("gdflist").style.display = "none";
		}
	}
	function change_symbol(x,sh,direction){
		var font_size2_text = "14px \"Trebuchet MS\", Arial, sans-serif";
		if(cssObj == null){
			return;
		}
		cssObj.remove();
		cstObj.remove();
		if(css == 0 && direction == -1){
			css = symbols.length - 1;
		} else if(css == symbols.length -1 && direction == 1){
			css = 0;
		} else {
			css += direction;
		}
		if(css == 0){
			for(var i = 1 ; i < symbols.length ; i++){
				for(var j = 1 ; j < symbols[i].length ; j++){
					genes[symbols[i][j]].obj.show();
				}
			}
			cssObj = R.text(x,R_top-sh-20,"ALL").attr({font:font_size2_text});
		} else {
			for(var i = 1 ; i < symbols.length ; i++){
				for(var j = 1 ; j < symbols[i].length ; j++){
					genes[symbols[i][j]].obj.hide();
				}
			}
			if(css > 0 && css < symbols.length){
				for(var j = 1 ; j < symbols[css].length ; j++){
					genes[symbols[css][j]].obj.show();
				}
			}
			cssObj = R.text(x,R_top-sh-20,symbols[css][0]).attr({font:font_size2_text});
		}

		cst = 0;
		cstObj = R.text(x,R_top-sh,"ALL").attr({font:font_size2_text});
	}
	function change_transcript(x,sh,direction){
		var font_size2_text = "14px \"Trebuchet MS\", Arial, sans-serif";
		if(cssObj == null || cstObj == null || css == 0){
			return;
		}
		cstObj.remove();
		if(cst == 0 && direction == -1){
			cst = symbols[css].length - 1;
		} else if(cst == symbols[css].length -1 && direction == 1){
			cst = 0;
		} else {
			cst += direction;
		}
		if(cst == 0){
			for(var i = 1 ; i < symbols.length ; i++){
				for(var j = 1 ; j < symbols[i].length ; j++){
					genes[symbols[i][j]].obj.hide();
				}
			}
			if(css > 0 && css < symbols.length){
				for(var j = 1 ; j < symbols[css].length ; j++){
					genes[symbols[css][j]].obj.show();
				}
			}
			cstObj = R.text(x,R_top-sh,"ALL").attr({font:font_size2_text});
		} else {
			for(var i = 1 ; i < symbols.length ; i++){
				for(var j = 1 ; j < symbols[i].length ; j++){
					genes[symbols[i][j]].obj.hide();
				}
			}
			if(css > 0 && css < symbols.length 
			&& cst > 0 && cst < symbols[css].length){
				genes[symbols[css][cst]].obj.show();
			}
			var strand = "";
			if(genes[symbols[css][cst]].strand == "+"){
				strand = ">";
			} else if(genes[symbols[css][cst]].strand == "-"){
				strand = "<";
			}
			cstObj = R.text(x,R_top-sh,genes[symbols[css][cst]].id+strand).attr({font:font_size2_text});
		}
	}
	function draw_arrow(h1,level,direction){
		var name_length = 100;
		var h = h1 + name_length/2*direction;
		var v = R_top - 20*level;
		var sh = 8;//short height
//		var arrow = R.path("M"+h+","+v+
//					" L"+(h+1*sh*direction)+","+v+
//					" L"+(h+1*sh*direction)+","+(v+sh/2)+
//					" L"+(h+2.5*sh*direction)+","+(v-sh/2)+
//					" L"+(h+1*sh*direction)+","+(v-sh*1.5)+
//					" L"+(h+1*sh*direction)+","+(v-sh)+
//					" L"+h+","+(v-sh)+"Z").attr({fill:colO_hover,"stroke-width":0});					
		if(direction==1){
			var arrow = R.path("M16,1.466C7.973,1.466,1.466,7.973,1.466,16c0,8.027,6.507,14.534,14.534,14.534c8.027,0,14.534-6.507,14.534-14.534C30.534,7.973,24.027,1.466,16,1.466zM13.665,25.725l-3.536-3.539l6.187-6.187l-6.187-6.187l3.536-3.536l9.724,9.723L13.665,25.725z");
		}else {
			var arrow = R.path("M16,30.534c8.027,0,14.534-6.507,14.534-14.534c0-8.027-6.507-14.534-14.534-14.534C7.973,1.466,1.466,7.973,1.466,16C1.466,24.027,7.973,30.534,16,30.534zM18.335,6.276l3.536,3.538l-6.187,6.187l6.187,6.187l-3.536,3.537l-9.723-9.724L18.335,6.276z");
		}
		arrow.attr({fill:colO_hover,stroke:"none"})
		arrow.stop().animate({transform: "t"+ (h-15) +" "+ (v-25) +" s0.7"});
		//			R.path("M"+h+","+v+
		//			" L"+(h+2*sh*direction)+","+(v-sh)+
		//			" L"+h+","+(v-2*sh)+"Z").attr({fill:colO_hover,"stroke-width":0});
	
		(function (obj){
			obj[0].style.cursor = "pointer";
			obj[0].onmouseover = function() {
				obj.attr({fill:colO_hover2});
				R.safari();
			};
			obj[0].onmouseout = function() {
				obj.attr({fill:colO_hover});
				R.safari();
			};
			obj[0].onclick = function() {
				if(level == 0){
					change_transcript(h1,sh,direction);
				}else if(level == 1){
					change_symbol(h1,sh,direction);
				}
			};
		})(arrow);
	}
	function draw_var(natual_pos,name_pos,l,box_width,letter){
		var var_length = 10;
		var var_height = 5;
		var horizontal_offset = R_width-R_right+15+box_width;
		var horizontal_line_offset = horizontal_offset+var_height+var_length;
		var horizontal_text_offset = horizontal_line_offset+50;
		var font_size2_text = "12px \"Trebuchet MS\", Arial, sans-serif";

		var temp = R.set();
		temp.push(R.path("M"+horizontal_offset+","+natual_pos+
			" L"+(horizontal_offset+var_height)+","+(natual_pos+var_height/2)+
			" L"+(horizontal_offset+var_height+var_length)+","+(natual_pos+var_height/2)+
			" L"+(horizontal_offset+var_height+var_length)+","+(natual_pos-var_height/2)+
			" L"+(horizontal_offset+var_height)+","+(natual_pos-var_height/2)+"Z").attr({fill:colO,"stroke-width":0}));

		temp.push(R.path("M"+(horizontal_line_offset)+","+natual_pos
			+" L"+(horizontal_line_offset+10)+","+natual_pos
			+" L"+(horizontal_text_offset-10)+","+name_pos
			+" L"+(horizontal_text_offset)+","+name_pos).attr({stroke:colO,opacity:1}));

		if(letter == "(" || letter == ")"){
			letter = "ASS/DSS loss";
		}else if (letter == "#"){
			letter = "Frame shifting";
		}else if (letter == "^"){
			letter = "Initiator loss";
		}else{
			var texts = letter.split(":");
			if(texts[0].indexOf("$") >= 0){
				letter = "Stop loss";
			}else if(texts[1].indexOf("$") >= 0){
				letter = "Stop gain";
			}else {
				letter = texts[0] + "->" + texts[1];
			}
		}

		temp.push(R.text(horizontal_text_offset+2,name_pos,letter).attr({font:font_size2_text,"text-anchor":"start"}));
		return temp;
	}

	function draw_box(from,to,l,box_width){
		var sub_top = map_coord(l,parseInt(from));
		var sub_bot = map_coord(l,parseInt(to)+1);
		if(sub_top == l || sub_bot == 0){
			return null;
		}
		return R.rect(R_width-R_right+10,R_top+sub_top,box_width,sub_bot-sub_top,0).attr({fill:colO,stroke:colO});
	}
	function draw_triangle(from,to,l,strand,box_width){
		var sub_top = map_coord(l,parseInt(from));
		var sub_bot = map_coord(l,parseInt(to)+1);
		if(strand == "+"){
			return R.path("M"+(R_width-R_right+10)+","+(R_top+sub_bot)+" L"+(R_width-R_right+10+box_width)+","+(R_top+sub_bot)+" L"+(R_width-R_right+10+box_width/2)+","+(R_top+sub_bot+box_width)+" Z").attr({fill:colO});
		} else if (strand == "-"){
			return R.path("M"+(R_width-R_right+10)+","+(R_top+sub_top)+" L"+(R_width-R_right+10+box_width)+","+(R_top+sub_top)+" L"+(R_width-R_right+10+box_width/2)+","+(R_top+sub_top-box_width)+" Z").attr({fill:colO});
		} else {
			return null;
		}
	}
	function draw_band(from,to,l,band_width,box_width){
		var sub_top = map_coord(l,parseInt(from));
		var sub_bot = map_coord(l,parseInt(to)+1);
		var slimmer = (box_width-band_width)/2;
		if(sub_top == l || sub_bot == 0){
			return null;
		}
		return R.rect(R_width-R_right+10+slimmer,R_top+sub_top,band_width,sub_bot-sub_top,0).attr({fill:colO,stroke:colO});
	}
	function draw_line(from,to,l,strand,box_width){
		var sub_top = map_coord(l,parseInt(from));
		var sub_bot = map_coord(l,parseInt(to)+1);
		var mid_point_x = R_width-R_right+10+box_width/2;
		if(sub_top == l || sub_bot == 0){
			return null;
		}
		if(strand == "+"){
		//	mid_point_x = R_width-R_right+10+box_width;
			mid_point_x = R_width-R_right+10+box_width/2;
		} else if(strand == "-"){
		//	mid_point_x = R_width-R_right+10;
			mid_point_x = R_width-R_right+10+box_width/2;
		}
		return R.path("M"+(R_width-R_right+10+box_width/2)+","+(R_top+sub_top)+" L"+mid_point_x+","+(R_top+(sub_bot+sub_top)/2)+" L"+(R_width-R_right+10+box_width/2)+","+(R_top+sub_bot));
	}
	function map_coord(l,coord){
		if (coord < current_start){
			return 0;
		} else if (coord > current_end){
			return l;
		} else {
			return (coord-current_start)/(current_end-current_start+1)*l;
		}
	}
}
