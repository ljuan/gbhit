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

				if(individuals[id].family != "--"){
					if(families[individuals[id].family] == undefined){
						families[individuals[id].family] = {};
					}
					if(families[individuals[id].family][id] == undefined){
						families[individuals[id].family][id] = {};
					}
					families[individuals[id].family][id].id = id;
					families[individuals[id].family][id].fid = individuals[id].fid;
					families[individuals[id].family][id].mid = individuals[id].mid;
					if(individuals[id].fid != "0"){
						add_id_familist(id,individuals[id].fid);
						families[individuals[id].family][individuals[id].fid].sp = individuals[id].mid;
						families[individuals[id].family][individuals[id].fid].rmo = id;
					}
					if(individuals[id].mid != "0"){
						add_id_familist(id,individuals[id].mid);
						families[individuals[id].family][individuals[id].mid].sp = individuals[id].fid;
						families[individuals[id].family][individuals[id].mid].rmo = id;
					}
				}
			}
			families.meta_heightt = 0;
			families.meta_widthh = 0;
			for(var family_key in families){
//				families[family_key].min_level = 0;
//				families[family_key].max_level = 0;
//				families[family_key].level =  {};
				families[family_key].roots = {};
				for(ind_key in families[family_key]){
					if(ind_key != "min_level" && ind_key != "max_level" && ind_key!= "level" && ind_key!="roots"){
//						search_level(families[family_key][ind_key].id,0);
					
						if(families[family_key][ind_key].fid == "0" 
						&& families[family_key][ind_key].mid == "0"
						&& families[family_key][ind_key].sp != undefined
						&& families[family_key][ind_key].sp != "0" 
						&& families[family_key][families[family_key][ind_key].sp].fid == "0"
						&& families[family_key][families[family_key][ind_key].sp].mid == "0"
						&& families[family_key].roots[ind_key] == undefined
						&& families[family_key].roots[families[family_key][ind_key].sp] == undefined){


							families[family_key].roots[ind_key] = {};
							families[family_key].roots[ind_key].levels = [];
							families[family_key].roots[ind_key].leftt = 0;
							families[family_key].roots[ind_key].rightt = 0;
							families[family_key].roots[ind_key].height = 0;

							search_offspring(ind_key,ind_key,0,1);
							assign_coord(ind_key,ind_key,0,0);
							correct_coord(ind_key);

							families.meta_heightt += families[family_key].roots[ind_key].height + 1;
							if(families.meta_widthh < families[family_key].roots[ind_key].rightt - families[family_key].roots[ind_key].leftt){
								families.meta_widthh = families[family_key].roots[ind_key].rightt - families[family_key].roots[ind_key].leftt;
							}
						}
					}
				}
			}
			plot_families();
			list_individuals();
			///document.getElementById("divv").innerHTML="<xmp>"+req.responseText+"</xmp>";
		} else {
			//document.getElementById("divv").innerHTML=req.responseText;
		}
	}
}
function correct_coord(root){
	var family = individuals[root].family;
	var pointers = [];
	var height = families[family].roots[root].levels.length;
	families[family].roots[root].height = height;
	for(var i=0;i<height;i++){
		for(var j=1;j<families[family].roots[root].levels[i].length;j++){
			var id2 = families[family].roots[root].levels[i][j];
			var id1 = families[family].roots[root].levels[i][j-1];
			var step = 2;
			if(id2 == "0"){
				continue;
			} else if(id1 == "0"){
				id1 = families[family].roots[root].levels[i][j-2];
				step+=2;
			}
			if(families[family][id2].x[root]<families[family][id1].x[root]+step){
				move(id2,families[family][id1].x[root]+step-families[family][id2].x[root]);
			}
		}
	}
	for(var i=0;i<height;i++){
		var id1 = families[family].roots[root].levels[i][0];
		var id2 = families[family].roots[root].levels[i][families[family].roots[root].levels[i].length-1];
		if(families[family].roots[root].leftt > families[family][id1].x[root]){
			families[family].roots[root].leftt = families[family][id1].x[root];
		}
		if(families[family].roots[root].rightt < families[family][id2].x[root]){
			families[family].roots[root].rightt = families[family][id2].x[root];
		}
	}

	function move(id,offset){
		if(families[family][id].fid != "0"){
			if(families[family][families[family][id].fid].lmo == id){
				move_1(families[family][id].fid,offset);
			} else {
				move_1(families[family][id].fid,offset/2);
			}
		}
		if(families[family][id].mid != "0"){
			if(families[family][families[family][id].mid].lmo == id){
				move_1(families[family][id].mid,offset);
			} else {
				move_1(families[family][id].mid,offset/2);
			}
		}
		move_2(id,offset);

		function move_1(id,offset){//move parents
			if(families[family][id].x[root] == undefined){
				return;
			}
			families[family][id].x[root] += offset;
			if(families[family][id].sp != undefined && families[family][id].sp == "0"){
				families[family][families[family][id].sp].x[root] += offset;
			}
	
			if(families[family][id].fid != "0"){
				if(families[family][families[family][id].fid].lmo == id){
					move_1(families[family][id].fid,offset);
				} else {
					move_1(families[family][id].fid,offset/2);
				}
			}
			if(families[family][id].mid != "0"){
				if(families[family][families[family][id].mid].lmo == id){
					move_1(families[family][id].mid,offset);
				} else {
					move_1(families[family][id].mid,offset/2);
				}
			}
	
			if(families[family][id].rb != undefined){
				move_2(families[family][id].rb,offset);
			}
		}
		function move_2(id,offset){//move offsprings
			if(families[family][id].x[root] == undefined){
				return;
			}

			families[family][id].x[root] += offset;
			if(families[family][id].sp != undefined){
				families[family][families[family][id].sp].x[root] += offset;
			}
	
			if(families[family][id].lmo != undefined){
				move_2(families[family][id].lmo,offset);
			}
			if(families[family][id].rb != undefined){
				move_2(families[family][id].rb,offset);
			}
		}
	}
}
function assign_coord(root,id,level,offset){
	if(families[individuals[id].family][id].y == undefined){
		families[individuals[id].family][id].y = {};
	}
	families[individuals[id].family][id].y[root] = level;

	if(families[individuals[id].family][id].x == undefined){
		families[individuals[id].family][id].x = {};
	}
	families[individuals[id].family][id].x[root] = offset;

	if(families[individuals[id].family][id].lmo != undefined){
		assign_coord(root,families[individuals[id].family][id].lmo,level+1,
		offset+2-families[individuals[id].family][id].offs);
	} 

	if(families[individuals[id].family][id].sp != undefined){
		offset+=2;
		if(families[individuals[id].family][id].sp != "0"){
			if(families[individuals[id].family][families[individuals[id].family][id].sp].x == undefined){
				families[individuals[id].family][families[individuals[id].family][id].sp].x = {};
			}
			if(families[individuals[id].family][families[individuals[id].family][id].sp].y == undefined){
				families[individuals[id].family][families[individuals[id].family][id].sp].y = {};
			}
			families[individuals[id].family][families[individuals[id].family][id].sp].x[root] = offset;
			families[individuals[id].family][families[individuals[id].family][id].sp].y[root] = level;
		}
	}

	if(families[individuals[id].family][id].rb != undefined){
		assign_coord(root,families[individuals[id].family][id].rb,level,offset+2);
	}
}
function search_offspring(root,id,level,brothers){
	if(families[individuals[id].family].roots[root].levels[level] == undefined){
		families[individuals[id].family].roots[root].levels[level] = [];
	}
	var curlength = families[individuals[id].family].roots[root].levels[level].length;
	families[individuals[id].family].roots[root].levels[level][curlength] = id;
	brothers++;
	if(families[individuals[id].family][id].lmo != undefined){
		families[individuals[id].family][id].offs = search_offspring(root,families[individuals[id].family][id].lmo,level+1,0);
	} else{
		families[individuals[id].family][id].offs = 0;
	}

	if(families[individuals[id].family][id].sp != undefined){
		brothers++;
		curlength = families[individuals[id].family].roots[root].levels[level].length;
		families[individuals[id].family].roots[root].levels[level][curlength] = families[individuals[id].family][id].sp;
	}
	if(families[individuals[id].family][id].rb != undefined){
		brothers = search_offspring(root,families[individuals[id].family][id].rb,level,brothers);
	} else if(families[individuals[id].family][id].sp != undefined){
		brothers--;
	}
	return brothers;
}
/* global tree levels
function search_level(id,level){
	if(families[individuals[id].family][id].level != undefined){
		return;
	}

	families[individuals[id].family][id].level = level;

	if(families[individuals[id].family].level[level.toString()] == undefined){
		families[individuals[id].family].level[level.toString()] = 0;
	}
	families[individuals[id].family].level[level.toString()]++; 

	if(families[individuals[id].family].min_level > level){
		families[individuals[id].family].min_level = level;
	}
	if(families[individuals[id].family].max_level < level){
		families[individuals[id].family].max_level = level;
	}

	if(families[individuals[id].family][id].lmo != undefined){
		search_level(families[individuals[id].family][id].lmo,level+1);
	}
	if(families[individuals[id].family][id].rb != undefined){
		search_level(families[individuals[id].family][id].rb,level);
	}
	if(families[individuals[id].family][id].fid != "0"){
		search_level(families[individuals[id].family][id].fid,level-1);
	}
	if(families[individuals[id].family][id].mid != "0"){
		search_level(families[individuals[id].family][id].mid,level-1);
	}
	if(families[individuals[id].family][id].sp != undefined && families[individuals[id].family][id].sp != "0"){
		search_level(families[individuals[id].family][id].sp,level);
	}
}
*/
function add_id_familist(id,pid){//add lmo (left most offspring)/rb (right brother);
	if(families[individuals[id].family][pid] == undefined){
		families[individuals[id].family][pid] = {};
		families[individuals[id].family][pid].lmo = id;
	} else if (families[individuals[id].family][pid].lmo == undefined){
		families[individuals[id].family][pid].lmo = id;
	} else {
		var family_list_temp = families[individuals[id].family][pid].lmo;
		while(families[individuals[id].family][family_list_temp].rb!=undefined && families[individuals[id].family][family_list_temp].rb!=id){
			family_list_temp = families[individuals[id].family][family_list_temp].rb;
		}
		families[individuals[id].family][family_list_temp].rb=id;
	}
}

function plot_families(){
	document.getElementById("pedplot").innerHTML="";
	var height_unit = 72;
	var width_unit = 36;
	var r = 10;
	var baseline = height_unit/2;
	var height = height_unit*families.meta_heightt;
	var width = width_unit*(families.meta_widthh+3);
	var widthlimit = document.body.clientWidth*0.9*0.45<720?document.body.clientWidth*0.9*0.45:720;
	if(width < widthlimit){
		width = widthlimit;
	}

	var P = Raphael("pedplot",width,height);
	var offset = 0;
	for (var family_key in families){
		for(var root in families[family_key].roots){
			offset = 0;
			if((families[family_key].roots[root].rightt-families[family_key].roots[root].leftt)*width_unit < widthlimit){
				offset = (widthlimit - (families[family_key].roots[root].rightt-families[family_key].roots[root].leftt)*width_unit)/2 - families[family_key].roots[root].leftt*width_unit;
			}else{
				offset = (2 - families[family_key].roots[root].leftt) * width_unit;
			}
			plot_ped(root,root);
			baseline += height_unit*(families[family_key].roots[root].height+1);
		}
	}

	function plot_ped(root,id){
		plot_individual(root,id);
		plot_relations(root,id);
		if(families[individuals[id].family][id].sp != undefined){
			if(families[individuals[id].family][id].sp != "0"){
				plot_individual(root,families[individuals[id].family][id].sp);
			} else {
				plot_fake_sp(root,id);
			}
		}
		if(families[individuals[id].family][id].lmo != undefined){
			plot_ped(root,families[individuals[id].family][id].lmo);
		}
		if(families[individuals[id].family][id].rb != undefined){
			plot_ped(root,families[individuals[id].family][id].rb);
		}
	}
	
	function plot_relations(root,id){
		if(individuals[id].fid != "0" || individuals[id].mid != "0"){
			P.path("M"+(offset+families[individuals[id].family][id].x[root]*width_unit)
					+" "+(baseline+families[individuals[id].family][id].y[root]*height_unit-r)
					+"L"+(offset+families[individuals[id].family][id].x[root]*width_unit)
					+" "+(baseline+families[individuals[id].family][id].y[root]*height_unit-height_unit/2));
		}
		if(families[individuals[id].family][id].sp != undefined){
			P.path("M"+(offset+families[individuals[id].family][id].x[root]*width_unit+r)
					+" "+(baseline+families[individuals[id].family][id].y[root]*height_unit)
					+"L"+(offset+(families[individuals[id].family][id].x[root]+2)*width_unit-r)
					+" "+(baseline+families[individuals[id].family][id].y[root]*height_unit));

		}
		if(families[individuals[id].family][id].lmo != undefined){
			P.path("M"+(offset+(families[individuals[id].family][id].x[root]+1)*width_unit)
					+" "+(baseline+families[individuals[id].family][id].y[root]*height_unit)
					+"L"+(offset+(families[individuals[id].family][id].x[root]+1)*width_unit)
					+" "+(baseline+families[individuals[id].family][id].y[root]*height_unit+height_unit/2));
			var lmo = families[individuals[id].family][id].lmo;
			var rmo = families[individuals[id].family][id].rmo;
			if(lmo != rmo){
				P.path("M"+(offset+families[individuals[id].family][lmo].x[root]*width_unit)
						+" "+(baseline+families[individuals[id].family][id].y[root]*height_unit+height_unit/2)
						+"L"+(offset+families[individuals[id].family][rmo].x[root]*width_unit)
						+" "+(baseline+families[individuals[id].family][id].y[root]*height_unit+height_unit/2));
			}
		}
	}

	function plot_individual(root,id){
		if(families[individuals[id].family][id].obj == undefined){
			families[individuals[id].family][id].obj = {};
		}
		if(individuals[id].sex == "1"){
			families[individuals[id].family][id].obj[root] = P.rect(offset+families[individuals[id].family][id].x[root]*width_unit-r,baseline+families[individuals[id].family][id].y[root]*height_unit-r,2*r,2*r,0);
		} else {
			families[individuals[id].family][id].obj[root] = P.ellipse(offset+families[individuals[id].family][id].x[root]*width_unit,baseline+families[individuals[id].family][id].y[root]*height_unit,r,r);
		}

		var color = "#FFF";
		if(individuals[id].affected == "1"){
			color = "#000";
		}

		families[individuals[id].family][id].obj[root].attr({fill:color,"fill-opacity":0.9,stroke:"#000"});

		if(families[individuals[id].family][id].idobj == undefined){
			families[individuals[id].family][id].idobj = {};
		}

		families[individuals[id].family][id].idobj[root] = P.text(offset+families[individuals[id].family][id].x[root]*width_unit-2,baseline+families[individuals[id].family][id].y[root]*height_unit-r-6,id)
		families[individuals[id].family][id].idobj[root].attr({fill:"#000",font:"11px \"Trebuchet MS\", Arial, sans-serif","text-anchor":"end"});

		if(id != "0" && individuals[id].ifs == "true"){
			(function(root,id){
				families[individuals[id].family][id].obj[root][0].style.cursor = "pointer";
				families[individuals[id].family][id].idobj[root][0].style.cursor = "pointer";
				families[individuals[id].family][id].obj[root][0].onmouseover = function(){
					families[individuals[id].family][id].obj[root].animate({fill:"#999"},200);
					P.safari();
				};
				families[individuals[id].family][id].obj[root][0].onmouseout = function(){
					families[individuals[id].family][id].obj[root].animate({fill:color},200);
					P.safari();
				};
				families[individuals[id].family][id].obj[root][0].onclick = function(){
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
			temp = P.ellipse(offset+(families[individuals[id].family][id].x[root]+2)*width_unit,baseline+families[individuals[id].family][id].y[root]*height_unit,r,r);
		} else {
			temp = P.rect(offset+(families[individuals[id].family][id].x[root]+2)*width_unit-r,baseline+families[individuals[id].family][id].y[root]*height_unit-r,2*r,2*r,0);
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
			for(var temp_root in families[individuals[id].family][id].idobj){
				families[individuals[id].family][id].idobj[temp_root].attr({fill:"#000"});
			}
			individuals[id].selected = false;
			csi = "--";
		} else {
			if(individuals[csi] != undefined){
				for(var temp_root in families[individuals[csi].family][csi].idobj){
					families[individuals[csi].family][csi].idobj[temp_root].attr({fill:"#000"});
				}
				individuals[csi].selected = false;
			}
			for(var temp_root in families[individuals[id].family][id].idobj){
				families[individuals[id].family][id].idobj[temp_root].attr({fill:"#F00"});
			}
			individuals[id].selected = true;
			csi = id;
		}
	}
}
