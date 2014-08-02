/**
 * @author Yafeng Hao, Liran Juan
 */

function plan_pedigree() {
	if (req2.readyState == 4){
		if (req2.status == 200){
			var XMLDoc = req2.responseXML;
			var Pedigrees = XMLDoc.getElementsByTagName("Member");
			for(var i = 0;i < Pedigrees.length;i++){
				var id = Pedigrees[i].getAttribute(xmlAttributeId);
				individuals[id] = {};
				individuals[id].id = id;
				individuals[id].family = Pedigrees[i].getElementsByTagName("Family")[0].firstChild.nodeValue;
				individuals[id].fid = Pedigrees[i].getElementsByTagName("Father")[0].firstChild.nodeValue;
				individuals[id].mid = Pedigrees[i].getElementsByTagName("Mother")[0].firstChild.nodeValue;
				individuals[id].sex = Pedigrees[i].getElementsByTagName("Sex")[0].firstChild.nodeValue;
				individuals[id].affected = Pedigrees[i].getElementsByTagName("Affected")[0].firstChild.nodeValue;
				individuals[id].ifs = Pedigrees[i].getAttribute("ifSample");
				individuals[id].selected = false;
				if(families[id] == undefined){
					families[id] = {};
				}
				families[id].id = id;
				families[id].fid = individuals[id].fid;
				families[id].mid = individuals[id].mid;
				if(individuals[id].fid != "0"){
					add_id_familist(id,individuals[id].fid);
					families[individuals[id].fid].sp = individuals[id].mid;
					families[individuals[id].fid].rmo = id;
				}
				if(individuals[id].mid != "0"){
					add_id_familist(id,individuals[id].mid);
					families[individuals[id].mid].sp = individuals[id].fid;
					families[individuals[id].mid].rmo = id;
				}
			}
			families_meta_heightt = 0;
			families_meta_widthh = 0;
			families.roots = {};
			for(ind_key in families){
				if(ind_key != "min_level" && ind_key != "max_level" && ind_key!= "level" && ind_key!="roots"){
					
					if(families[ind_key].fid == "0" 
					&& families[ind_key].mid == "0"
					&& families[ind_key].sp != undefined
					&& families[ind_key].sp != "0" 
					&& families[families[ind_key].sp].fid == "0"
					&& families[families[ind_key].sp].mid == "0"
					&& families.roots[ind_key] == undefined
					&& families.roots[families[ind_key].sp] == undefined){

						families.roots[ind_key] = {};
						families.roots[ind_key].levels = [];
						families.roots[ind_key].leftt = 0;
						families.roots[ind_key].rightt = 0;
						families.roots[ind_key].height = 0;

						search_offspring(ind_key,ind_key,0,1);
						assign_coord(ind_key,ind_key,0,0);
						correct_coord(ind_key);

						families_meta_heightt += families.roots[ind_key].height + 1;
						if(families_meta_widthh < families.roots[ind_key].rightt - families.roots[ind_key].leftt){
							families_meta_widthh = families.roots[ind_key].rightt - families.roots[ind_key].leftt;
						}
					}
				}
			}
			plot_families();
			list_individuals();
		}
	}
}
function correct_coord(root){
	var family = individuals[root].family;
	var pointers = [];
	var height = families.roots[root].levels.length;
	families.roots[root].height = height;
	for(var i=0;i<height;i++){
		for(var j=1;j<families.roots[root].levels[i].length;j++){
			var id2 = families.roots[root].levels[i][j];
			var id1 = families.roots[root].levels[i][j-1];
			var step = 2;
			if(id2 == "0"){
				continue;
			} else if(id1 == "0"){
				id1 = families.roots[root].levels[i][j-2];
				step+=2;
			}
			if(families[id2].x[root]<families[id1].x[root]+step){
				move(id2,families[id1].x[root]+step-families[id2].x[root]);
			}
		}
	}
	for(var i=0;i<height;i++){
		var id1 = families.roots[root].levels[i][0];
		var id2 = families.roots[root].levels[i][families.roots[root].levels[i].length-1];
		var plus_temp = 0;
		if(id2 == "0"){
			id2 = families.roots[root].levels[i][families.roots[root].levels[i].length-2];
			plus_temp = 2;
		} 
		if(families.roots[root].leftt > families[id1].x[root]){
			families.roots[root].leftt = families[id1].x[root];
		}
		if(families.roots[root].rightt < families[id2].x[root]+plus_temp){
			families.roots[root].rightt = families[id2].x[root]+plus_temp;
		}
	}

	function move(id,offset){
		if(families[id].fid != "0"){
			if(families[families[id].fid].lmo == id){
				move_1(families[id].fid,offset);
			} else {
				move_1(families[id].fid,offset/2);
			}
		}
		if(families[id].mid != "0"){
			if(families[families[id].mid].lmo == id){
				move_1(families[id].mid,offset);
			} else {
				move_1(families[id].mid,offset/2);
			}
		}
		move_2(id,offset);

		function move_1(id,offset){//move parents
			if(families[id].x[root] == undefined){
				return;
			}
			families[id].x[root] += offset;
//			if(families[id].sp != undefined && families[id].sp != "0"){
//				families[families[id].sp].x[root] += offset;
//			}
	
			if(families[id].fid != "0"){
				if(families[families[id].fid].lmo == id){
					move_1(families[id].fid,offset);
				} else {
					move_1(families[id].fid,offset/2);
				}
			}
			if(families[id].mid != "0"){
				if(families[families[id].mid].lmo == id){
					move_1(families[id].mid,offset);
				} else {
					move_1(families[id].mid,offset/2);
				}
			}
	
			if(families[id].rb != undefined){
				move_2(families[id].rb,offset);
			}
		}
		function move_2(id,offset){//move offsprings
			if(families[id].x[root] == undefined){
				return;
			}

			families[id].x[root] += offset;
			if(families[id].sp != undefined && families[id].sp != "0"){
				families[families[id].sp].x[root] += offset;
			}
	
			if(families[id].lmo != undefined){
				move_2(families[id].lmo,offset);
			}
			if(families[id].rb != undefined){
				move_2(families[id].rb,offset);
			}
		}
	}
}
function assign_coord(root,id,level,offset){
	if(families[id].y == undefined){
		families[id].y = {};
	}
	families[id].y[root] = level;

	if(families[id].x == undefined){
		families[id].x = {};
	}
	families[id].x[root] = offset;

	if(families[id].lmo != undefined){
		assign_coord(root,families[id].lmo,level+1,
		offset+2-families[id].offs);
	} 

	if(families[id].sp != undefined){
		offset+=2;
		if(families[id].sp != "0"){
			if(families[families[id].sp].x == undefined){
				families[families[id].sp].x = {};
			}
			if(families[families[id].sp].y == undefined){
				families[families[id].sp].y = {};
			}
			families[families[id].sp].x[root] = offset;
			families[families[id].sp].y[root] = level;
		}
	}

	if(families[id].rb != undefined){
		assign_coord(root,families[id].rb,level,offset+2);
	}
}
function search_offspring(root,id,level,brothers){
	if(families.roots[root].levels[level] == undefined){
		families.roots[root].levels[level] = [];
	}
	var curlength = families.roots[root].levels[level].length;
	families.roots[root].levels[level][curlength] = id;
	brothers++;
	if(families[id].lmo != undefined){
		families[id].offs = search_offspring(root,families[id].lmo,level+1,0);
	} else{
		families[id].offs = 0;
	}

	if(families[id].sp != undefined){
		brothers++;
		curlength = families.roots[root].levels[level].length;
		families.roots[root].levels[level][curlength] = families[id].sp;
	}
	if(families[id].rb != undefined){
		brothers = search_offspring(root,families[id].rb,level,brothers);
	} else if(families[id].sp != undefined){
		brothers--;
	}
	return brothers;
}
function add_id_familist(id,pid){//add lmo (left most offspring)/rb (right brother);
	if(families[pid] == undefined){
		families[pid] = {};
		families[pid].lmo = id;
	} else if (families[pid].lmo == undefined){
		families[pid].lmo = id;
	} else {
		var family_list_temp = families[pid].lmo;
		while(families[family_list_temp].rb!=undefined && families[family_list_temp].rb!=id){
			family_list_temp = families[family_list_temp].rb;
		}
		families[family_list_temp].rb=id;
	}
}

function plot_families(){
	document.getElementById("pedplot").innerHTML="";
	var height_unit = 72;
	var width_unit = 36;
	var r = 10;
	var baseline = height_unit/2;
	var height = height_unit*families_meta_heightt;
	var width = width_unit*(families_meta_widthh+3);
	var widthlimit = document.body.clientWidth*0.9*0.45<720?document.body.clientWidth*0.9*0.45:720;
	var mark_lablesize = 9;
	var mark_labletext = mark_lablesize + "px Candara";
	if(width < widthlimit){
		width = widthlimit;
	}

	var P = Raphael("pedplot",width,height);
	var offset = 0;
	for(var root in families.roots){
		offset = 0;
		if((families.roots[root].rightt-families.roots[root].leftt)*width_unit < widthlimit){
			offset = (widthlimit - (families.roots[root].rightt-families.roots[root].leftt)*width_unit)/2 - families.roots[root].leftt*width_unit;
		}else{
			offset = (2 - families.roots[root].leftt) * width_unit;
		}
		plot_ped(root,root);
		baseline += height_unit*(families.roots[root].height+1);
	}

	function plot_ped(root,id){
		plot_individual(root,id);
		plot_relations(root,id);
		if(families[id].sp != undefined){
			if(families[id].sp != "0"){
				plot_individual(root,families[id].sp);
			} else {
				plot_fake_sp(root,id);
			}
		}
		if(families[id].lmo != undefined){
			plot_ped(root,families[id].lmo);
		}
		if(families[id].rb != undefined){
			plot_ped(root,families[id].rb);
		}
	}
	
	function plot_relations(root,id){
		if(individuals[id].fid != "0" || individuals[id].mid != "0"){
			P.path("M"+(offset+families[id].x[root]*width_unit)
					+" "+(baseline+families[id].y[root]*height_unit-r)
					+"L"+(offset+families[id].x[root]*width_unit)
					+" "+(baseline+families[id].y[root]*height_unit-height_unit/2));
		}
		if(families[id].sp != undefined){
			P.path("M"+(offset+families[id].x[root]*width_unit+r)
					+" "+(baseline+families[id].y[root]*height_unit)
					+"L"+(offset+(families[id].x[root]+2)*width_unit-r)
					+" "+(baseline+families[id].y[root]*height_unit));

		}
		if(families[id].lmo != undefined){
			P.path("M"+(offset+(families[id].x[root]+1)*width_unit)
					+" "+(baseline+families[id].y[root]*height_unit)
					+"L"+(offset+(families[id].x[root]+1)*width_unit)
					+" "+(baseline+families[id].y[root]*height_unit+height_unit/2));
			var lmo = families[id].lmo;
			var rmo = families[id].rmo;
			if(lmo != rmo){
				P.path("M"+(offset+families[lmo].x[root]*width_unit)
						+" "+(baseline+families[id].y[root]*height_unit+height_unit/2)
						+"L"+(offset+families[rmo].x[root]*width_unit)
						+" "+(baseline+families[id].y[root]*height_unit+height_unit/2));
			}
		}
	}

	function plot_individual(root,id){
		if(families[id].obj == undefined){
			families[id].obj = {};
		}
		if(individuals[id].sex == "1"){
			families[id].obj[root] = P.rect(offset+families[id].x[root]*width_unit-r,baseline+families[id].y[root]*height_unit-r,2*r,2*r,0);
		} else {
			families[id].obj[root] = P.ellipse(offset+families[id].x[root]*width_unit,baseline+families[id].y[root]*height_unit,r,r);
		}

		var color = "#FFF";
		if(individuals[id].affected == "1"){
			color = "#000";
		}

		families[id].obj[root].attr({fill:color,"fill-opacity":0.9,stroke:"#000"});

		if(families[id].idobj == undefined){
			families[id].idobj = {};
		}

		families[id].idobj[root] = P.text(offset+families[id].x[root]*width_unit-2,baseline+families[id].y[root]*height_unit-r-6,id)
		families[id].idobj[root].attr({fill:"#000",font:"11px \"Trebuchet MS\", Arial, sans-serif","text-anchor":"end"});

		if(id != "0" && individuals[id].ifs == "true"){
			////////////////////////////////
			if(families[id].markobj == undefined){
				families[id].markobj = {};
			}
			families[id].markobj[root] = P.text(offset+families[id].x[root]*width_unit+19,baseline+families[id].y[root]*height_unit-r+20,"");
			families[id].markobj[root].attr({fill:"#F00",font: mark_labletext,"font-weight":"bold"});
			families[id].markobj[root].hide();
			/////////////////////////////////
			(function(root,id){
				families[id].obj[root][0].style.cursor = "pointer";
				families[id].idobj[root][0].style.cursor = "pointer";
				families[id].obj[root][0].onmouseover = function(){
					families[id].obj[root].animate({fill:"#999"},200);
					P.safari();
				};
				families[id].obj[root][0].onmouseout = function(){
					families[id].obj[root].animate({fill:color},200);
					P.safari();
				};
				families[id].obj[root][0].onclick = function(){
					select_a_individual(id);

					var radioObj = document.getElementById(id+"__indlist_select");
					if(radioObj != undefined){
						if(radioObj.checked){
							radioObj.checked = false;
						} else {
							radioObj.checked = true;
						}
					}

					P.safari();
				};
	
			})(root,id);
		}
	}

	function plot_fake_sp(root,id){
		var temp;
		if(individuals[id].sex == "1"){
			temp = P.ellipse(offset+(families[id].x[root]+2)*width_unit,baseline+families[id].y[root]*height_unit,r,r);
		} else {
			temp = P.rect(offset+(families[id].x[root]+2)*width_unit-r,baseline+families[id].y[root]*height_unit-r,2*r,2*r,0);
		}
		temp.attr({fill:"#FFF","fill-opacity":0.9,stroke:"#000","stroke-dasharray":"--"});
	}
}

function list_individuals(){
	document.getElementById("indlist").innerHTML="";
	var temp = document.createElement("table");
	document.getElementById("indlist").appendChild(temp);
	temp.className = "listt_table";
	temp.id = "indlist_table";
	var temp_tr = temp.insertRow(-1);
	temp_tr.innerHTML = 
		"<th>Family</th>"+
		"<th>Id</th>"+
		"<th>Father</th>"+
		"<th>Mother</th>"+
		"<th>Gender</th>"+
		"<th>Affected</th>"+
		"<th>Checked</th>";
	for(var id in individuals){
		var father = "--";
		var mother = "--";
		var gender = "Female";
		var affected = "Unaffected";

		if(individuals[id].fid != "0"){
			father = individuals[id].fid;
		}
		if(individuals[id].mid != "0"){
			mother = individuals[id].mid;
		}
		if(individuals[id].sex == "1"){
			gender = "Male";
		}
		if(individuals[id].affected == "1"){
			affected = "Affected";
		} 
		temp_tr = temp.insertRow(-1);
		temp_tr.style.background = "#CCC";
		var temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = individuals[id].family;
		temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = id;
		temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = father;
		temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = mother;
		temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = gender;
		temp_td = temp_tr.insertCell(-1);
		temp_td.innerHTML = affected;
		temp_td = temp_tr.insertCell(-1);

		if(individuals[id].ifs == "true"){
			var radioObj = document.createElement("input");
			temp_td.appendChild(radioObj);
			radioObj.type = "radio";
			radioObj.name = "indlist_select";
			radioObj.id = id + "__indlist_select";
			if(id == csi){
				radioObj.checked = true;
			}
			radioObj.onclick = function(event){
				var target = event.target || event.srcElement;
				var id = target.getAttribute("id").split("__")[0];
				select_a_individual(id);
			};
		} else {
			temp_td.innerHTML = "--";
		}
	}
}
function select_a_individual(id){
	if(individuals[id] != undefined){
		if(individuals[id].selected){
			for(var temp_root in families[id].idobj){
				families[id].idobj[temp_root].attr({fill:"#000"});
			}
			individuals[id].selected = false;
			csi = "--";
		} else {
			if(individuals[csi] != undefined){
				for(var temp_root in families[csi].idobj){
					families[csi].idobj[temp_root].attr({fill:"#000"});
				}
				individuals[csi].selected = false;
			}
			for(var temp_root in families[id].idobj){
				families[id].idobj[temp_root].attr({fill:"#F00"});
			}
			individuals[id].selected = true;
			csi = id;
		}
		////////////////////////////////
		if(control_scanning!=undefined){
			if(control_scanning==1){
				control_scanning=0;
			}
		}
		for(var family_member in families){
			for(var temp_root in families[family_member].markobj){
				if(family_member != "roots" && individuals[family_member].ifs == "true" && families[family_member].markobj[temp_root] != undefined){
					families[family_member].markobj[temp_root].hide();
				}
			}
		}
		for(var chr_num in chrs){
			for(var band_num in chrs[chr_num].bands){
				for(var objid in chrs[chr_num].bands[band_num].scoreobj){
					if(chrs[chr_num].bands[band_num].scoreobj[objid]!=undefined){
						chrs[chr_num].bands[band_num].scoreobj[objid].attr({fill:"#FFF"});
					}
				}
			}
		}
		if(individuals[id].selected){
			setTabb("brwview");
			load_family_genome(current_chr,current_start,current_end);
		}
		///////////////////////////////
	}
}
