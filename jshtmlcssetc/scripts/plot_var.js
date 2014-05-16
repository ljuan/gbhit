/**
 * @author Yafeng Hao, Liran Juan
 */

function init_individual_vars(){
	variants = [];
	var vsNode = req3.responseXML.getElementsByTagName(xmlTagVariants)[0];
	var vNodes = vsNode.getElementsByTagName(xmlTagVariant);
	for(var i = 0 ; i < vNodes.length ; i++){
		variants[i] = {};
		variants[i].chr = current_chr;
		variants[i].id = vNodes[i].getAttribute(xmlAttributeId);
		variants[i].genotype = vNodes[i].getAttribute(xmlAttributeGenotype);
		variants[i].type = vNodes[i].getAttribute(xmlAttributeType);
		variants[i].from = parseInt(vNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue);
		variants[i].to = parseInt(vNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue);
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
	var axis = R.path("M"+R_left+","+R_top+" L"+R_left+","+(R_height-R_bottom)).attr({stroke:"#000","stroke-width":1});
	var cali = [];
	var t_bot, t_top;
	t_top = R.text(0,R_top-10,current_chr+":"+current_start).attr({font: font_size2_text ,opacity:1,"text-anchor":"start"}).attr({fill: "#000"});
	t_bot = R.text(0,R_height-R_bottom+15,current_chr+":"+current_end).attr({font: font_size2_text ,opacity:1,"text-anchor":"start"}).attr({fill: "#000"});
	axis_set.push(axis, t_bot, t_top);
	ca = 0;
	axis_set.push(cali[ca]);
	for(var ca = 0 ; ca <= 100 ; ca++){
		if(ca%10 == 0){
			cali[ca] = R.path("M"+(R_left-20)+","+(R_top+ca/100*l)+" L"+R_left+","+(R_top+ca/100*l)).attr({stroke:"#000","stroke-width":1});
		}else{
			cali[ca] = R.path("M"+(R_left-10)+","+(R_top+ca/100*l)+" L"+R_left+","+(R_top+ca/100*l)).attr({stroke:"#000","stroke-width":1});
		}
		axis_set.push(cali[ca]);
	}
	axis_set.push(cali[ca]);
	//axis_set.hide();
	
	if(individuals[current_selected_individual] != undefined){
		var color = "#FFF";
		if(individuals[individuals[current_selected_individual].fid].affected == "1"){
			color = "#000";
		}
		R.rect(R_left+w/2-40,5,20,20,0).attr({fill:color,stroke:"#000","stroke-width":1});

		color = "#FFF";
		if(individuals[individuals[current_selected_individual].mid].affected == "1"){
			color = "#000";
		}
		R.ellipse(R_left+w/2+30,15,10,10).attr({fill:color,stroke:"#000","stroke-width":1});

		color = "#FFF";
		if(individuals[current_selected_individual].affected == "1"){
			color = "#000";
		}
		if(individuals[current_selected_individual].sex == "1"){
			R.rect(R_left+w/2-10,R_top-25,20,20,0).attr({fill:color,stroke:"#000","stroke-width":1});
		} else {
			R.ellipse(R_left+w/2,R_top-15,10,10).attr({fill:color,stroke:"#000","stroke-width":1});
		}

		R.path("M"+(R_left+w/2-20)+",15 L"+(R_left+w/2+20)+",15").attr({stroke:"#000","stroke-width":1});
		R.path("M"+(R_left+w/2)+",15 L"+(R_left+w/2)+","+(R_top-25)).attr({stroke:"#000","stroke-width":1});

		R.text(R_left+w/2-42,20,individuals[current_selected_individual].fid).attr({font:font_size2_text,"text-anchor":"end"});
		R.text(R_left+w/2+42,20,individuals[current_selected_individual].mid).attr({font:font_size2_text,"text-anchor":"start"});
		R.text(R_left+w/2+12,R_top-10,current_selected_individual).attr({font:font_size2_text,"text-anchor":"start"});
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
						if(vs_id == individuals[current_selected_individual].fid){
							variants[vPointer].paternal = "N";
							var vartable = document.getElementById("varlist_table");
							if(vartable != undefined){
								vartable.rows[vPointer+1].cells[5].innerHTML="N";
							}
						}else if(vs_id == individuals[current_selected_individual].mid){
							variants[vPointer].maternal = "N";
							var vartable = document.getElementById("varlist_table");
							if(vartable != undefined){
								vartable.rows[vPointer+1].cells[6].innerHTML="N";
							}
						}else if(vs_id == current_selected_individual){
							change_variant_color(vPointer,"#F00");
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
					if(vPointer<variants.length){
				//		alert(from+" "+to+" "+variants[vPointer].from+" "+variants[vPointer].to+" "+id+" "+variants[vPointer].id);
					}
					if(from <= variants[vPointer].from && to >= variants[vPointer].to && id == variants[vPointer].id){
						if(vs_id == individuals[current_selected_individual].fid){
							variants[vPointer].paternal = "N";
							var vartable = document.getElementById("varlist_table");
							if(vartable != undefined){
								vartable.rows[vPointer+1].cells[5].innerHTML="N";
							}
						}else if(vs_id == individuals[current_selected_individual].mid){
							variants[vPointer].maternal = "Y";
						}else if(vs_id == current_selected_individual){
							change_variant_color(vPointer,"#0F0");
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
						if(vs_id == individuals[current_selected_individual].fid){
							variants[vPointer].paternal = "Y";
						}else if(vs_id == individuals[current_selected_individual].mid){
							variants[vPointer].maternal = "N";
							var vartable = document.getElementById("varlist_table");
							if(vartable != undefined){
								vartable.rows[vPointer+1].cells[6].innerHTML="N";
							}
						}else if(vs_id == current_selected_individual){
							change_variant_color(vPointer,"#00F");
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

			var sets4 = individuals[current_selected_individual].fid
					+":"+individuals[current_selected_individual].mid
					+","+current_selected_individual;
			var sets5 = individuals[current_selected_individual].fid
					+","+individuals[current_selected_individual].mid
					+":"+current_selected_individual;
			var sets6 = individuals[current_selected_individual].mid
					+","+individuals[current_selected_individual].fid
					+":"+current_selected_individual;
		
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
		if(variants[i].id == current_selected_variant){
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
	var w = R_width - R_left;
	var font_height = 15;
	var text_length = 100;
	var bin_size = 10;
	var max_bar_length = 100;
	var font_size2_text = "12px \"Trebuchet MS\", Arial, sans-serif";
	var pixel_per_variant = 2;

	if(variants[0] != undefined && variants[0].genotype.indexOf("|")>=0){
		R.rect(R_left+w/2-15,R_top,10,l,5).attr({fill:"#F77",stroke:"#F77"});
		R.rect(R_left+w/2+5,R_top,10,l,5).attr({fill:"#77F",stroke:"#77F"});
	} else {
		R.rect(R_left+w/2-5,R_top,10,l,5).attr({fill:"#777",stroke:"#777"});
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
			var splitter = "|";
			if(variants[i].genotype.indexOf("/")>0){
				splitter = "/";
			}
			var genotypes = variants[i].genotype.split(splitter);
			if(genotypes[0] == undefined || genotypes[0] == "0"){
				variants[i].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:"#FFF",stroke:"#000"});
			}else{
				variants[i].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:"#000",stroke:"#000"});
				variants[i].lines[0] = R.path("M"+(R_left+text_length+2)+","+name_pos+" L"+(R_left+text_length+20)+","+name_pos+" L"+(R_left+w/2-52)+","+natual_pos+" L"+(R_left+w/2-33)+","+natual_pos).attr({stroke:"#000",opacity:1});

				variants[i].name[0] = R.text(R_left+text_length,name_pos,variants[i].id).attr({font:font_size2_text,opacity:1,"text-anchor":"end"});

				(function(idx){
					variants[idx].name[0][0].style.cursor = "pointer";
					variants[idx].name[0][0].onmouseover = function(){
						variants[idx].name[0].animate({fill:"#777"},200);
						R.safari();
					};
					variants[idx].name[0][0].onmouseout = function(){
						variants[idx].name[0].animate({fill:"#000"},200);
						R.safari();
					};
					variants[idx].name[0][0].onclick = function(){
						select_a_variant(idx);
						R.safari();
					};
				})(i);

			}
			if(genotypes[1] == undefined || genotypes[1] == "0"){
				variants[i].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:"#FFF",stroke:"#000"});
			}else{
				variants[i].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:"#000",stroke:"#000"});
				variants[i].lines[1] = R.path("M"+(R_left+w-text_length-2)+","+name_pos+" L"+(R_left+w-text_length-20)+","+name_pos+" L"+(R_left+w/2+52)+","+natual_pos+" L"+(R_left+w/2+33)+","+natual_pos).attr({stroke:"#000",opacity:1});

				variants[i].name[1] = R.text(R_left+w-text_length,name_pos,variants[i].id).attr({font:font_size2_text,opacity:1,"text-anchor":"start"});

				(function(idx){
					variants[idx].name[1][0].style.cursor = "pointer";
					variants[idx].name[1][0].onmouseover = function(){
						variants[idx].name[1].animate({fill:"#777"},200);
						R.safari();
					};
					variants[idx].name[1][0].onmouseout = function(){
						variants[idx].name[1].animate({fill:"#000"},200);
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
			var splitter = "|";
			if(variants[i].genotype.indexOf("/")>0){
				splitter = "/";
			}
			var genotypes = variants[i].genotype.split(splitter);
			if(genotypes[0] == undefined || genotypes[0] == "0"){
				variants[i].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:"#FFF",stroke:"#000"});
			}else{
				variants[i].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:"#000",stroke:"#000"});
			}
			if(genotypes[1] == undefined || genotypes[1] == "0"){
				variants[i].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:"#FFF",stroke:"#000"});
			}else{
				variants[i].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:"#000",stroke:"#000"});
			}
		}
	} else {
		var bins = [];
		bins[0] = [];
		bins[1] = [];
		for(var i = 0 ; i < variants.length ; i++){
			var natual_pos = ((variants[i].to+variants[i].from)/2 - current_start)/(current_end - current_start + 1)*(l/bin_size);
			var splitter = "|";
			if(variants[i].genotype.indexOf("/")>0){
				splitter = "/";
			}
			var genotypes = variants[i].genotype.split(splitter);
			if(genotypes[0] != undefined && genotypes[0] != "0"){
				if(bins[0][Math.floor(natual_pos)] == undefined){
					bins[0][Math.floor(natual_pos)] = 1;
				}else{
					bins[0][Math.floor(natual_pos)] ++;
				}
			}
			if(genotypes[1] != undefined && genotypes[1] != "0"){
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
			R.rect(R_left+w/2-40-bins[0][i]*pixel_per_variant,i*bin_size+R_top,bins[0][i]*pixel_per_variant,bin_size,0).attr({fill:"#777",stroke:"#777"});
		}
		for(var i = 0 ; i < bins[1].length ; i++){
			if(bins[1][i] == undefined){
				bins[1][i] = 0;
			}
			R.rect(R_left+w/2+40,i*bin_size+R_top,bins[1][i]*pixel_per_variant,bin_size,0).attr({fill:"#777",stroke:"#777"});
		}
	}
}

function select_a_variant(idx){
}
function change_variant_color(vPointer,color){
	var splitter = "|";
	if(variants[vPointer].genotype.indexOf("/")>0){
		splitter = "/";
	}
	var genotypes = variants[vPointer].genotype.split(splitter);
	if(variants[vPointer].name != undefined){
		var color2 = "#777";
		if(color == "#0F0"){
			color2 = "#7F7";
		}else if(color == "#F00"){
			color2 = "#F77";
		}else if(color == "#00F"){
			color2 = "#77F";
		}

		if(genotypes[0] != undefined && genotypes[0] != "0"){
			variants[vPointer].name[0].attr({fill:color}).toFront();

			(function(idx,color){
				variants[idx].name[0][0].style.cursor = "pointer";
				variants[idx].name[0][0].onmouseover = function(){
					variants[idx].name[0].animate({fill:color2},200);
					R.safari();
				};
				variants[idx].name[0][0].onmouseout = function(){
					variants[idx].name[0].animate({fill:color},200);
					R.safari();
				};
				variants[idx].name[0][0].onclick = function(){
					select_a_variant(idx);
					R.safari();
				};
			})(vPointer,color);
		}
		if(genotypes[1] != undefined && genotypes[1] != "0"){
			variants[vPointer].name[1].attr({fill:color}).toFront();

			(function(idx,color){
				variants[idx].name[1][0].style.cursor = "pointer";
				variants[idx].name[1][0].onmouseover = function(){
					variants[idx].name[1].animate({fill:color2},200);
					R.safari();
				};
				variants[idx].name[1][0].onmouseout = function(){
					variants[idx].name[1].animate({fill:color},200);
					R.safari();
				};
				variants[idx].name[1][0].onclick = function(){
					select_a_variant(idx);
					R.safari();
				};
			})(vPointer,color);
		}
	}
	if(variants[vPointer].point != undefined){
		if(genotypes[0] != undefined && genotypes[0] != "0"){
			variants[vPointer].point[0].attr({fill:color,stroke:color}).toFront();
		}
		if(genotypes[1] != undefined && genotypes[1] != "0"){
			variants[vPointer].point[1].attr({fill:color,stroke:color}).toFront();
		}
	} else {
		var l = R_height - R_top - R_bottom;
		var w = R_width - R_left;
		var natual_pos = ((variants[vPointer].to+variants[vPointer].from)/2 - current_start)/(current_end - current_start + 1)*l + R_top;
		variants[vPointer].point = [];
		if(genotypes[0] == undefined || genotypes[0] == "0"){
			variants[vPointer].point[0] = R.ellipse(R_left+w/2-30,natual_pos,2,2).attr({fill:color,stroke:color});
		}
		if(genotypes[1] == undefined || genotypes[1] == "0"){
			variants[vPointer].point[1] = R.ellipse(R_left+w/2+30,natual_pos,2,2).attr({fill:color,stroke:color});
		}
	}
	if(variants[vPointer].lines != undefined){
		if(genotypes[0] != undefined && genotypes[0] != "0"){
			variants[vPointer].lines[0].attr({stroke:color}).toFront();
		}
		if(genotypes[1] != undefined && genotypes[1] != "0"){
			variants[vPointer].lines[1].attr({stroke:color}).toFront();
		}
	}
}
