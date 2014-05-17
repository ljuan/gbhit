/**
 * @author Yafeng Hao, Liran Juan
 */
var colD = "#F00"; //denovo mut color
var colD_hover = "#F77"; //denovo mut color hover
var colP = "#0F0"; //paternal var color
var colP_hover = "#7F7"; //paternal var color hover
var colM = "#00F"; //maternal var color
var colM_hover = "#77F"; //maternal var color
var colO = "#000"; //black
var colO_hover = "#777"; //black hover

function init_individual_vars(){
	variants = [];
	var vsNode = req3.responseXML.getElementsByTagName(xmlTagVariants)[0];
	var vNodes = vsNode.getElementsByTagName(xmlTagVariant);
	for(var i = 0 ; i < vNodes.length ; i++){
		variants[i] = {};
		variants[i].chr = current_chr;
		variants[i].id = vNodes[i].getAttribute(xmlAttributeId);

		variants[i].genotype = vNodes[i].getAttribute(xmlAttributeGenotype);
		var splitter = "|";
		if(variants[i].genotype.indexOf("/")>0){
			splitter = "/";
		}
		variants[i].genotypes = variants[i].genotype.split(splitter);

		variants[i].type = vNodes[i].getAttribute(xmlAttributeType);
		variants[i].from = parseInt(vNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue);
		variants[i].to = parseInt(vNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue);
		variants[i].selected = false;
		if(vNodes[i].getElementsByTagName(xmlTagLetter).length > 0){
			variants[i].letter = vNodes[i].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
		} else {
			variants[i].letter = "--";
		}
		variants[i].paternal = "Y";
		variants[i].maternal = "Y";
		variants[i].functional = "--";
	}

	var esNodes = req3.responseXML.getElementsByTagName(xmlTagElements);
	for(var i = 0 ; i < esNodes.length ; i++){
		var veNodes = esNodes[i].getElementsByTagName(xmlTagVariant);
		var vPointer = 0;
		for(var j = 0 ; j < veNodes.length ; j++){
			var from = veNodes[j].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue;
			var to = veNodes[j].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue;
			var letter = veNodes[j].getElementsByTagName(xmlTagLetter)[0].childNodes[0].nodeValue;
			var id = veNodes[j].getAttribute(xmlAttributeId);
			while(variants[vPointer].from < from){
				vPointer++;
			}
			if(from <= variants[vPointer].from && to >= variants[vPointer].to && id == variants[vPointer].id){
				variants[vPointer].functional = letter;
			}
		}
	}
}

function show_axis(){
	var l = R_height - R_top - R_bottom;
	var w = R_width - R_left;
	var font_size2_text = "14px \"Trebuchet MS\", Arial, sans-serif";
	var axis_set = R.set();
	var axis = R.path("M"+R_left+","+R_top+" L"+R_left+","+(R_height-R_bottom)).attr({stroke:colO,"stroke-width":1});
	var cali = [];
	var t_bot, t_top;
	t_top = R.text(0,R_top-10,current_chr+":"+current_start).attr({font: font_size2_text ,opacity:1,"text-anchor":"start"}).attr({fill: colO});
	t_bot = R.text(0,R_height-R_bottom+15,current_chr+":"+current_end).attr({font: font_size2_text ,opacity:1,"text-anchor":"start"}).attr({fill: colO});
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
	
	if(individuals[csi] != undefined){
		var color = "#FFF";
		if(individuals[individuals[csi].fid].affected == "1"){
			color = colO;
		}
		R.rect(R_left+w/2-40,5,20,20,0).attr({fill:color,stroke:colO,"stroke-width":1});

		color = "#FFF";
		if(individuals[individuals[csi].mid].affected == "1"){
			color = colO;
		}
		R.ellipse(R_left+w/2+30,15,10,10).attr({fill:color,stroke:colO,"stroke-width":1});

		color = "#FFF";
		if(individuals[csi].affected == "1"){
			color = colO;
		}
		if(individuals[csi].sex == "1"){
			R.rect(R_left+w/2-10,R_top-25,20,20,0).attr({fill:color,stroke:colO,"stroke-width":1});
		} else {
			R.ellipse(R_left+w/2,R_top-15,10,10).attr({fill:color,stroke:colO,"stroke-width":1});
		}

		R.path("M"+(R_left+w/2-20)+",15 L"+(R_left+w/2+20)+",15").attr({stroke:colO,"stroke-width":1});
		R.path("M"+(R_left+w/2)+",15 L"+(R_left+w/2)+","+(R_top-25)).attr({stroke:colO,"stroke-width":1});

		R.text(R_left+w/2-42,20,individuals[csi].fid).attr({font:font_size2_text,"text-anchor":"end"});
		R.text(R_left+w/2+42,20,individuals[csi].mid).attr({font:font_size2_text,"text-anchor":"start"});
		R.text(R_left+w/2+12,R_top-10,csi).attr({font:font_size2_text,"text-anchor":"start"});
	}
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
							var vartable = document.getElementById("varlist_table");
							if(vartable != undefined){
								vartable.rows[vPointer+1].cells[5].innerHTML="N";
							}
						}else if(vs_id == individuals[csi].mid){
							variants[vPointer].maternal = "N";
							var vartable = document.getElementById("varlist_table");
							if(vartable != undefined){
								vartable.rows[vPointer+1].cells[6].innerHTML="N";
							}
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
							var vartable = document.getElementById("varlist_table");
							if(vartable != undefined){
								vartable.rows[vPointer+1].cells[5].innerHTML="N";
							}
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
							var vartable = document.getElementById("varlist_table");
							if(vartable != undefined){
								vartable.rows[vPointer+1].cells[6].innerHTML="N";
							}
						}else if(vs_id == csi){
							change_variant_color(vPointer,colM);
						}
					}
				}
			}
		}
	}
}

function show_vars(){
	if(req3.readyState == 4) {
		if(req3.status == 200) {

			init_individual_vars();
			list_variants();
			plot_variants();
			plot_genes();

			var sets4 = individuals[csi].fid
					+":"+individuals[csi].mid
					+","+csi;
			var sets5 = individuals[csi].fid
					+","+individuals[csi].mid
					+":"+csi;
			var sets6 = individuals[csi].mid
					+","+individuals[csi].fid
					+":"+csi;
		
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
		"<th>Paternal</th>"+
		"<th>Maternal</th>"+
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
		temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = variants[i].paternal;
		temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = variants[i].maternal;
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
			var color = colO;
			if(variants[idx].paternal == "N" && variants[idx].maternal == "N"){
				color = colD;
			}else if(variants[idx].paternal == "N"){
				color = colP;
			}else if(variants[idx].maternal == "N"){
				color = colM;
			}
			select_a_variant(idx,color);
		};
	}
}

function plot_variants(){
	var l = R_height - R_top - R_bottom;
	var w = R_width - R_left;
	var font_height = 15;
	var text_length = 100;
	var bin_size = 10;
	var max_bar_length = 100;
	var font_size2_text = "12px \"Trebuchet MS\", Arial, sans-serif";
	var pixel_per_variant = 2;

	if(variants[0] != undefined && variants[0].genotype.indexOf("|")>=0){
		R.rect(R_left+w/2-15,R_top,10,l,5).attr({fill:colD_hover,stroke:colD_hover});
		R.rect(R_left+w/2+5,R_top,10,l,5).attr({fill:colM_hover,stroke:colM_hover});
	} else {
		R.rect(R_left+w/2-5,R_top,10,l,5).attr({fill:colO_hover,stroke:colO_hover});
	}

	if(variants.length < l/font_height){
		var current_pos = 0;
		for(var i = 0 ; i < variants.length ; i++){
			var natual_pos = ((variants[i].to+variants[i].from)/2 - current_start)/(current_end - current_start + 1)*l;
			var name_pos = natual_pos;
			natual_pos += R_top;

			if(name_pos < i*font_height){
				name_pos = i*font_height;
			} else if(name_pos > l - (variants.length - i + 1)*font_height){
				name_pos = l - (variants.length - i + 1)*font_height;
			} else if(name_pos < current_pos + font_height){
				name_pos = current_pos + font_height;
			}
			current_pos = name_pos;
			name_pos += R_top;

			variants[i].point = [];
			variants[i].name = [];
			variants[i].lines = [];
			if(variants[i].genotypes[0] == undefined || variants[i].genotypes[0] == "0"){
				variants[i].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:"#FFF",stroke:colO});
			}else{
				variants[i].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:colO,stroke:colO});
				variants[i].lines[0] = R.path("M"+(R_left+text_length+2)+","+name_pos+" L"+(R_left+text_length+20)+","+name_pos+" L"+(R_left+w/2-52)+","+natual_pos+" L"+(R_left+w/2-33)+","+natual_pos).attr({stroke:colO,opacity:1});

				variants[i].name[0] = R.text(R_left+text_length,name_pos,variants[i].id).attr({font:font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill:colO,"font-weight":"normal"});

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
						select_a_variant(idx,colO);
						R.safari();
					};
				})(i);

			}
			if(variants[i].genotypes[1] == undefined || variants[i].genotypes[1] == "0"){
				variants[i].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:"#FFF",stroke:colO});
			}else{
				variants[i].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:colO,stroke:colO});
				variants[i].lines[1] = R.path("M"+(R_left+w-text_length-2)+","+name_pos+" L"+(R_left+w-text_length-20)+","+name_pos+" L"+(R_left+w/2+52)+","+natual_pos+" L"+(R_left+w/2+33)+","+natual_pos).attr({stroke:colO,opacity:1});

				variants[i].name[1] = R.text(R_left+w-text_length,name_pos,variants[i].id).attr({font:font_size2_text,opacity:1,"text-anchor":"start"}).attr({fill:colO});

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
						select_a_variant(idx,colO);
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

function select_a_variant(idx,color){
	if(variants[idx] != undefined){
		var radioObj = document.getElementById(idx+"__varlist_select")
		if(variants[idx].selected){
			change_variant_color(idx,color);
			variants[idx].selected = false;
			radioObj.checked = false;
			if(variants[idx].name != undefined){
				if(variants[idx].genotypes[0] != undefined && variants[idx].genotypes[0] != "0"){
					variants[idx].name[0].attr({"font-weight":"normal"});
				}
				if(variants[idx].genotypes[1] != undefined && variants[idx].genotypes[1] != "0"){
					variants[idx].name[1].attr({"font-weight":"normal"});
				}
			}
			csv = -1;
		} else {
			if(csv>=0 && variants[csv] != undefined){
				var color2 = colO;
				if(variants[csv].paternal == "N" && variants[csv].maternal == "N"){
					color2 = colD;
				}else if(variants[csv].paternal == "N"){
					color2 = colP;
				}else if(variants[csv].maternal == "N"){
					color2 = colM;
				}
				change_variant_color(csv,color2);
				variants[csv].selected = false;
			
				if(variants[csv].name != undefined){
					if(variants[csv].genotypes[0] != undefined && variants[csv].genotypes[0] != "0"){
						variants[csv].name[0].attr({"font-weight":"normal"});
					}
					if(variants[csv].genotypes[1] != undefined && variants[csv].genotypes[1] != "0"){
						variants[csv].name[1].attr({"font-weight":"normal"});
					}
				}
			}
			change_variant_color(idx,color);
			variants[idx].selected = true;
			radioObj.checked = true;
			if(variants[idx].name != undefined){
				if(variants[idx].genotypes[0] != undefined && variants[idx].genotypes[0] != "0"){
					variants[idx].name[0].attr({"font-weight":"bolder"});
				}
				if(variants[idx].genotypes[1] != undefined && variants[idx].genotypes[1] != "0"){
					variants[idx].name[1].attr({"font-weight":"bolder"});
				}
			}
			csv = idx;
		}
	}
}

function change_variant_color(vPointer,color){
	if(variants[vPointer].name != undefined){

		var color2 = colO_hover;
		if(color == colP){
			color2 = colP_hover;
		}else if(color == colD){
			color2 = colD_hover;
		}else if(color == colM){
			color2 = colM_hover;
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
					select_a_variant(idx,color);
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
					select_a_variant(idx,color);
					R.safari();
				};
			})(vPointer,color);
		}
	}
	if(variants[vPointer].point != undefined){
		if(variants[vPointer].genotypes[0] != undefined && variants[vPointer].genotypes[0] != "0"){
			variants[vPointer].point[0].attr({fill:color,stroke:color}).toFront();
		}
		if(variants[vPointer].genotypes[1] != undefined && variants[vPointer].genotypes[1] != "0"){
			variants[vPointer].point[1].attr({fill:color,stroke:color}).toFront();
		}
	} else {
		var l = R_height - R_top - R_bottom;
		var w = R_width - R_left;
		var natual_pos = ((variants[vPointer].to+variants[vPointer].from)/2 - current_start)/(current_end - current_start + 1)*l + R_top;
		variants[vPointer].point = [];
		if(variants[vPointer].genotypes[0] == undefined || variants[vPointer].genotypes[0] == "0"){
			variants[vPointer].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:color,stroke:color});
		}
		if(variants[vPointer].genotypes[1] == undefined || variants[vPointer].genotypes[1] == "0"){
			variants[vPointer].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:color,stroke:color});
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
}
