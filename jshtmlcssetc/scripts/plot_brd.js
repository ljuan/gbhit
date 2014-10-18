function loadChrBand(){
	document.getElementById("brd_genome").innerHTML="";
	document.getElementById("brd_genelist").innerHTML="";
//	document.getElementById("upload_success").innerHTML="Loading Individual Genome...";
	var xmlAttribute_gieStain = "gS";
	var xmlTagScore="Score";
	var pattern = /<.*?>/g;
	var animaTime=100;

	var setscore_req = createXMLHttpRequest();
	setscore_req.open("GET","servlet/test.do?action=setScoreMethod&scoremeth=Family",false);
	setscore_req.send(null);

	var h = 800;

	var l =520;
	var radius=150;
	var dis_chr2band=15;
	var chrthick=25;
	var bandthick=12;
	var dis_lable2chr=10;

	var font_size=12;
	var font_size_text=font_size+"px Trebuchet MS, Arial, sans-serif";
	var font_size2=16;
	var font_size2_text=font_size2+"px Trebuchet MS, Arial, sans-serif";

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
	
	for (var idx = 0 ; idx < chrs.length ; idx++){
		if(chrs[idx].lengthh < total/200){
			chrs[idx].to = chrs[idx].from + total/200;
			total = total + total/200 - chrs[idx].lengthh;
		}
	}
	for (var idx = 0 ; idx < chrs.length ; idx++){
		chrs[idx].from = chrs[idx].from + total/200*idx;
		chrs[idx].to = chrs[idx].to + total/200*idx;
	}
	total = total + total/200*chrs.length;


	XMLHttpReq7.open("GET","servlet/test.do?action=getAllCytobands",false);
	XMLHttpReq7.send(null);
	//********switchable with later similar code to save initializing time
	var cytobandsNode = XMLHttpReq7.responseXML.getElementsByTagName(xmlTagCytobands)[0];
	var cytobandNodes = cytobandsNode.getElementsByTagName(xmlTagCytoBand);
	var lastid = "0";
	for( i = 0; i < cytobandNodes.length; i++) {
		var idx = chrs_map[cytobandNodes[i].getElementsByTagName(xmlTagChrNum)[0].childNodes[0].nodeValue];
		var curi = chrs[idx].bands.length;
		chrs[idx].bands[curi] = {};
		chrs[idx].bands[curi].id = cytobandNodes[i].getAttribute(xmlAttributeId);
		chrs[idx].bands[curi].gieStain = cytobandNodes[i].getAttribute(xmlAttribute_gieStain);

		chrs[idx].bands[curi].from = parseInt(cytobandNodes[i].getElementsByTagName(xmlTagFrom)[0].childNodes[0].nodeValue);
		chrs[idx].bands[curi].to = parseInt(cytobandNodes[i].getElementsByTagName(xmlTagTo)[0].childNodes[0].nodeValue);
		if(lastid.charAt(0) == "p" && chrs[idx].bands[curi].id.charAt(0) == "q"){
			chrs[idx].centromere = chrs[idx].bands[curi].from;
		}
		lastid = chrs[idx].bands[curi].id;
		chrs[idx].bands[curi].score = -1;
		if(cytobandNodes[i].getElementsByTagName(xmlTagScore).length > 0){
			chrs[idx].bands[curi].score = parseFloat(cytobandNodes[i].getElementsByTagName(xmlTagScore)[0].childNodes[0].nodeValue);
		}
	}
	//#####################

	var B = Raphael("brd_genome", 570, h+90);
	var G = Raphael("brd_genelist",150,h-20);
	
//	Rbrd_sremove = spinner(B);
	
	var attr = {
		fill: "#333",
		stroke: "#666",
		"stroke-width": 0,
		"stroke-linecap": "round",
		"fill-opacity": 0.8
	}
		
//	var genome = [];
	var chrlables=[];
	var genome_rad = [];
//	var bands = [];
	var bandslables = [];
	var bandsscore = [];
	var bands_rad = [];
	var bandsscore_inter = [];

	var chrx = 50;
	var bandx = 300;
	var chrw = 50;
	var bandw = 50;
	var axisx = 550;
	var interval = 80;

	for(var idx=0;idx<chrs.length;idx++){
		bands[idx] = [];
		bandslables[idx] = [];
		bandsscore[idx] = [];
		bands_rad[idx] = [];
		bandsscore_inter[idx] = [];
//		attr["fill"] = colorlist["acen"];
//		attr["fill-opacity"] = 0.9;
//		B.rect(80,h*(chrs[idx].from+chrs[idx].centromere)/total,60,2,0).attr(attr);
		attr["fill"] = colorlist[chrs[idx].name];
		attr["stroke-width"] = 0;
		attr["fill-opacity"] = 0.8;
		genome[idx]=B.rect(chrx,interval+h*chrs[idx].from/total,chrw,h*(chrs[idx].to-chrs[idx].from)/total,3).attr(attr);
		attr["fill"] = "0-#bbb-#eee";
		attr["stroke-width"] = 0;
		attr["fill-opacity"] = 0.2;
		genome_rad[idx]=B.path("M"+(chrx+chrw+10)+","+(interval+h*chrs[idx].from/total)+" L"+(bandx-10)+","+interval+" L"+(bandx-10)+","+(interval+h)+" L"+(chrx+chrw+10)+","+(interval+h*chrs[idx].to/total)+"Z").attr(attr);
		genome_rad[idx].hide();
		chrlables[idx]=B.text(chrx-20,interval+h*(chrs[idx].from+chrs[idx].to)/2/total,chrs[idx].name.replace("chr","").replace("M","MT")).attr({font:font_size2_text,opacity:1}).attr({fill:"#000"});
		/*for(var i = 0 ; i < chrs[idx].bands.length ; i++){
			bands[idx][i]=drawCyto(chrs[idx].bands[i].from, chrs[idx].bands[i].to, chrs[idx].centromere, chrs[idx].lengthh, h, bandw, bandx, interval, chrs[idx].bands[i].gieStain);
			bands[idx][i].hide();

			attr["fill"] = "0-#bbb-#eee";
			attr["stroke-width"] = 0;
			attr["fill-opacity"] = 0.2;
			bands_rad[idx][i]=B.path("M"+(bandx+bandw+20)+","+(interval+h*chrs[idx].bands[i].from/chrs[idx].lengthh)+" L"+(axisx-10)+","+interval+" L"+(axisx-10)+","+(interval+h)+" L"+(bandx+bandw+20)+","+(interval+h*chrs[idx].bands[i].to/chrs[idx].lengthh)+"Z").attr(attr);
			bands_rad[idx][i].hide();

			bandslables[idx][i]=B.text(bandx-20,interval+h*(chrs[idx].bands[i].from+chrs[idx].bands[i].to)/2/chrs[idx].lengthh,chrs[idx].bands[i].id).attr({font:font_size_text}).attr({fill:"#000"});
			bandslables[idx][i].hide();
			if(chrs[idx].bands[i].scoreobj == undefined){
				chrs[idx].bands[i].scoreobj = {};
			}
			bandsscore[idx][i]=drawScore(chrs[idx].bands[i].from, chrs[idx].bands[i].to, chrs[idx].lengthh, h, bandx+bandw+5, interval);
			bandsscore[idx][i].hide();
			chrs[idx].bands[i].scoreobj[1] = bandsscore[idx][i];
		}*/
	}

	var axis_set = B.set();
	var axis = B.path("M"+axisx+","+interval+" L"+axisx+","+(interval+h)).attr({stroke:"#000","stroke-width":1});
	var cali = [];
	var t_bot,t_top,t_first;
	t_top = B.text(axisx-30,interval+5,"").attr({font: font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill: "#000"});
	t_bot = B.text(axisx-30,h+interval-5,"").attr({font: font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill: "#000"});
	t_first = B.text(axisx-30,interval+h/10,"").attr({font: font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill: "#000"});
	axis_set.push(axis,t_bot,t_top,t_first);
	for(var ca = 0 ; ca <= 100 ; ca++){
		if(ca%10 == 0){
			cali[ca] = B.path("M"+(axisx-20)+","+(ca/100*h+interval)+" L"+axisx+","+(ca/100*h+interval)).attr({stroke:"#000","stroke-width":1});
		} else {
			cali[ca] = B.path("M"+(axisx-10)+","+(ca/100*h+interval)+" L"+axisx+","+(ca/100*h+interval)).attr({stroke:"#000","stroke-width":1});
		}
	}

	geneflag_set = G.set();
	var gene_set = G.set();
	var loc_set= B.set();

	var stop_text = B.text(300,20,"Stop").attr({font: "16px Trebuchet MS, Arial, sans-serif",opacity:1}).attr({fill: "#222"});
	//var stop_button = B.rect(270,20,60,20,3).attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	var stop_button = B.ellipse(300,20,25,12).attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	//	B.circle(l/2,l/2,chrthick+5).attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	//var burrentt = B.text(70,20,"").attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});
	stop_text.hide();
	stop_button.hide();
	(function (stop_button){
		stop_button[0].style.cursor = "pointer";
		stop_text[0].style.cursor = "pointer";
		stop_button[0].onmouseover = function(){
			stop_button.animate({fill:"#999"},animaTime);
		};
		stop_button[0].onclick = function(){
			if(control_scanning==1){
				control_scanning=0;
			stop_button.hide();
				stop_text.hide();
			}
		};
		stop_button[0].onmouseout = function(){
			stop_button.animate({fill:"#666"},animaTime);
		};
	})(stop_button);
	
	var current=null;
	var click=null;
	var burrent=null;
	var blick=null;
	var gurrent=null;
	var glick=null;

	var scan_text0 = B.text(120,15,"Scan").attr({font: "16px Trebuchet MS, Arial, sans-serif",opacity:1}).attr({fill: "#000"});
	var scan_text1 = B.text(370,15,"mutation and compound heterozygous variants").attr({font: "16px Trebuchet MS, Arial, sans-serif",opacity:1}).attr({fill: "#000"});
	var scan_text2 = B.text(170,15,"de novo").attr({font: "italic 16px Trebuchet MS, Arial, sans-serif",opacity:1}).attr({fill: "#000"});
	var error_warn=B.text(0,30,"").attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});
	var scan_text = B.set();
	scan_text.push(scan_text0,scan_text1,scan_text2);
	btn_set = B.set();
	btn_set.push(scan_text0,scan_text1,scan_text2);

	var scang_text1 = B.text(chrx+25,47,"Whole").attr({font: "14px Trebuchet MS, Arial, sans-serif",opacity:1}).attr({fill: "#222"});
	var scang_text = B.text(chrx+25,62,"genome").attr({font: "14px Trebuchet MS, Arial, sans-serif",opacity:1}).attr({fill: "#222"});
	var scang_button = B.rect(chrx-20,37,chrw+40,37,3).attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	//var scang_arrow = B.path("M"+ (chrx+25) +" "+ 80 +",L"+ (chrx+35) +" "+ 74 +",L"+ (chrx+15) +" "+ 74 +"z").attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	btn_set.push(scang_text,scang_button,scang_text1);
	(function (scang_button){
		scang_button[0].style.cursor = "pointer";
		scang_text[0].style.cursor = "pointer";
		scang_button[0].onmouseover = function(){
			scang_button.animate({fill:"#999"},animaTime);
			//scang_arrow.animate({fill:"#999"},animaTime);
		};
		scang_button[0].onclick = function(){
		error_warn.remove();
			if(csi!=undefined && csi!="--"){
				scan_text.hide();
				stop_text.show();
				stop_button.show();
				getSingleCytoAsync(0,0,2);
			}
			else {
				error_warn=B.text(0,30,"Please load personal variants").attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});
			}
		};
		scang_button[0].onmouseout = function(){
			scang_button.animate({fill:"#666"},animaTime);
			//scang_arrow.animate({fill:"#666"},animaTime);
			error_warn.hide();
		};
	})(scang_button);

	var scanc_text1 = B.text(chrx+275,47,"Selected").attr({font: "14px Trebuchet MS, Arial, sans-serif",opacity:1}).attr({fill: "#222"});
	var scanc_text = B.text(chrx+275,62,"chromosome").attr({font: "14px Trebuchet MS, Arial, sans-serif",opacity:1}).attr({fill: "#222"});
	var scanc_button = B.rect(chrx+230,37,chrw+40,37,3).attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	//var scanc_arrow = B.path("M"+ (chrx+275) +" "+ 80 +",L"+ (chrx+285) +" "+ 74 +",L"+ (chrx+265) +" "+ 74 +"z").attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	btn_set.push(scanc_text,scanc_button,scanc_text1);
	(function (scanc_button){
		scanc_button[0].style.cursor = "pointer";
		scanc_text[0].style.cursor = "pointer";
		scanc_button[0].onmouseover = function(){
			scanc_button.animate({fill:"#999"},animaTime);
			//scanc_arrow.animate({fill:"#999"},animaTime);
		};
		scanc_button[0].onclick = function(){
			error_warn.remove();
			if(click!=null && csi!=undefined && csi!="--"){
				scan_text.hide();
				stop_text.show();
				stop_button.show();
				getSingleCytoAsync(click,0,1);
			}
			else {
				error_warn=B.text(0,30,"Please load personal variants and select chromosome").attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});
			}
		};
		scanc_button[0].onmouseout = function(){
			scanc_button.animate({fill:"#666"},animaTime);
			//scanc_arrow.animate({fill:"#666"},animaTime);
			error_warn.hide();
		};
	})(scanc_button);

	var scanb_text1 = B.text(chrx+475,47,"Selected").attr({font: "14px Trebuchet MS, Arial, sans-serif",opacity:1}).attr({fill: "#222"});
	var scanb_text = B.text(chrx+475,62,"cytoband").attr({font: "14px Trebuchet MS, Arial, sans-serif",opacity:1}).attr({fill: "#222"});
	var scanb_button = B.rect(chrx+430,37,chrw+40,37,3).attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	//var scanb_arrow = B.path("M"+ (chrx+475) +" "+ 80 +",L"+ (chrx+485) +" "+ 74 +",L"+ (chrx+465) +" "+ 74 +"z").attr({fill:"#666",stroke:"#999",opacity:0.5,"stroke-width":0});
	btn_set.push(scanb_text1,scanb_text,scanb_button);
	(function (scanb_button){
		scanb_button[0].style.cursor = "pointer";
		scanb_text[0].style.cursor = "pointer";
		scanb_button[0].onmouseover = function(){
			scanb_button.animate({fill:"#999"},animaTime);
			//scanb_arrow.animate({fill:"#999"},animaTime);
		};
		scanb_button[0].onclick = function(){
			error_warn.remove();
			if(click!=null && blick!=null && csi!=undefined && csi!="--"){
				scan_text.hide();
				stop_text.show();
				stop_button.show();
				getSingleCytoAsync(click,blick,0);
			}
			else {
				error_warn=B.text(0,30,"Please load personal variants and select cytoband").attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});
			}
		};
		scanb_button[0].onmouseout = function(){
			scanb_button.animate({fill:"#666"},animaTime);
			//scanb_arrow.animate({fill:"#666"},animaTime);
			error_warn.hide();
		};
	})(scanb_button);
	
	if(individuals[csi] != undefined){
		if(individuals[csi].fid == "0" ||  individuals[csi].mid == "0"){
			btn_set.hide();
		}else{
			btn_set.show();
		}
	}else{
		btn_set.hide();
	}

	for (var chrom in genome){
		genome[chrom].color=Raphael.getColor();
		(function (chr, chrom) {
			var ccolor=colorlist[chrs[chrom].name];
			chr[0].style.cursor = "pointer";
			chr[0].onmouseover = function () {
				chr.animate({fill: chr.color, stroke: "#ccc"}, animaTime);
				if(bands[chrom].length < chrs[chrom].bands.length){
					for(var i = 0 ; i < chrs[chrom].bands.length ; i++){
						if(bands[chrom][i]==undefined){
							bands[chrom][i]=drawCyto(chrs[chrom].bands[i].from, chrs[chrom].bands[i].to, chrs[chrom].centromere, chrs[chrom].lengthh, h, bandw, bandx, interval, chrs[chrom].bands[i].gieStain);
							bands[chrom][i].hide();
						}
						attr["fill"] = "0-#bbb-#eee";
						attr["stroke-width"] = 0;
						attr["fill-opacity"] = 0.2;
						bands_rad[chrom][i]=B.path("M"+(bandx+bandw+14)+","+(interval+h*chrs[chrom].bands[i].from/chrs[chrom].lengthh)+" L"+(axisx-10)+","+interval+" L"+(axisx-10)+","+(interval+h)+" L"+(bandx+bandw+14)+","+(interval+h*chrs[chrom].bands[i].to/chrs[chrom].lengthh)+"Z").attr(attr);
						bands_rad[chrom][i].hide();
						if(bandslables[chrom][i]==undefined){
							bandslables[chrom][i]=B.text(bandx-20,interval+h*(chrs[chrom].bands[i].from+chrs[chrom].bands[i].to)/2/chrs[chrom].lengthh,chrs[chrom].bands[i].id).attr({font:font_size_text}).attr({fill:"#000"});
							bandslables[chrom][i].hide();
						}
						if(chrs[chrom].bands[i].scoreobj == undefined){
							chrs[chrom].bands[i].scoreobj = {};
						}
						if(bandsscore[chrom][i]==undefined){
							bandsscore[chrom][i]=drawScore(chrs[chrom].bands[i].from, chrs[chrom].bands[i].to, chrs[chrom].lengthh, h, bandx+bandw+3, interval);
							bandsscore[chrom][i].hide();
							chrs[chrom].bands[i].scoreobj[1] = bandsscore[chrom][i];
						}
					}
				}
				if(current){
					chrlables[current].animate({fill: "#000",font: font_size_text}, animaTime);
					genome_rad[current].hide();
					for(var i=0; i < chrs[current].bands.length; i++) {
					bands[current][i].animate({fill: "#FFF", stroke: "#FFF"}, animaTime);
					bands[current][i].hide();
					bandsscore[current][i].hide();
					}
				}
				if(click&&click!=chrom){
					chrlables[click].animate({fill: "#000",font:font_size_text}, animaTime);
					genome_rad[click].hide();
					for(var i=0; i < chrs[click].bands.length; i++) {
						bands[click][i].animate({fill: "#FFF", stroke: "#FFF"}, animaTime);
						bands[click][i].hide();
						bandsscore[click][i].hide();
					}
					if(blick!=null){
						bandslables[click][blick].hide();
						bands_rad[click][blick].hide();
						gene_set.hide();
						t_top.hide();
						t_bot.hide();
						t_first.hide();
					}
				}
				chrlables[chrom].animate({fill: chr.color,font:font_size_text}, animaTime);
				genome_rad[chrom].show();
				for(var i=0; i < chrs[chrom].bands.length; i++) {
					var bcolor=colorlist[chrs[chrom].bands[i].gieStain];
					bands[chrom][i].show();
					bands[chrom][i].animate({fill: bcolor, stroke: "#666","fill-opacity":1, "stroke-width":1}, animaTime);
					bandsscore[chrom][i].show();
				}
				B.safari();
				current = chrom;
			}; 
			chr[0].onmouseout = function () {
				chr.animate({fill: ccolor, stroke: "#666"}, animaTime);
				if(current && current != click){
					chrlables[current].animate({fill: "#000",font:font_size_text}, animaTime);
					genome_rad[current].hide();
					for(var i=0; i < chrs[current].bands.length; i++) {
						bands[current][i].animate({fill: "#FFF", stroke: "#FFF"}, animaTime);
						bands[current][i].hide();
						bandsscore[current][i].hide();
					}
				}
				if(click && current != click){
					chrlables[click].animate({fill: genome[click].color,font:font_size_text}, animaTime);
					genome_rad[click].show();
					for(var i=0; i < chrs[click].bands.length; i++) {
						var bcolor=colorlist[chrs[click].bands[i].gieStain];
						bands[click][i].show();
						bands[click][i].animate({fill: bcolor, stroke: "#666"}, animaTime);
						bandsscore[click][i].show();
					}
					if(blick!=null){
						bandslables[click][blick].show();
						bands_rad[click][blick].show();
						gene_set.show();
						t_top.show();
						t_bot.show();
						t_first.show();
					}
				}
				B.safari();
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
							bandslables[click][i].hide();
							bands_rad[click][i].hide();
						})(bands[click][i],i);
					}
					G.remove();
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

				for (var i=0; i<bands[click].length; i++){
					bands[click][i].color=Raphael.getColor();
					(function (bd, i) {
						var bcolor=colorlist[chrs[click].bands[i].gieStain];
						bd[0].style.cursor = "pointer";
						bd[0].onmouseover = function () {
							if(burrent!=null){
								bandslables[click][burrent].hide();
								bands_rad[click][burrent].hide();
							}
							if(blick!=null&&blick!=i){
								bandslables[click][blick].hide();
								bands_rad[click][blick].hide();
								gene_set.hide();
								t_top.hide();
								t_bot.hide();
								t_first.hide();
							}
							bd.animate({fill: bd.color, stroke: "#ccc"}, animaTime);
							bandslables[click][i].show();
							bands_rad[click][i].show();
							B.safari();
							burrent = i;
						}; 
						bd[0].onmouseout = function () {
							bd.animate({fill: bcolor, stroke: "#666"}, animaTime);
							if(burrent!=null && burrent != blick){
								bandslables[click][burrent].hide();
								bands_rad[click][burrent].hide();
							}
							if(blick!=null && burrent != blick){
								bandslables[click][blick].show();
								bands_rad[click][blick].show();
								gene_set.show();
								t_top.show();
								t_bot.show();
								t_first.show();
							}
							B.safari();
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
							
							t_top = B.text(axisx-30,interval+5,chrs[click].bands[blick].from).attr({font: font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill: "#000"});
							t_bot = B.text(axisx-30,h+interval-5,chrs[click].bands[blick].to).attr({font: font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill: "#000"});
							t_first = B.text(axisx-30,interval+h/10,(chrs[click].bands[blick].to-chrs[click].bands[blick].from+1)/10).attr({font: font_size2_text,opacity:1,"text-anchor":"end"}).attr({fill: "#000"});
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
							G = Raphael("brd_genelist",150,genelistlength+20);

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
								ogeneso[gidx].text=G.text(10,(gidx*20+interval-60),ogenes[gidx].id).attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});
								ogeneso[gidx].flag=G.rect(0,(gidx*20+interval-70),8,20).attr({fill:score2flag(ogenes[gidx].score).color,"stroke-width":0});
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
								ogeneso[gidx].loc=B.path("M"+axisx+","+(interval+h*gup/bandlen)+" L"+axisx+","+(interval+h*gdown/bandlen)).attr({stroke:"#F00","stroke-width":8});
								ogeneso[gidx].loc.hide();
								gene_set.push(ogeneso[gidx].text);
								gene_set.push(ogeneso[gidx].flag);
								loc_set.push(ogeneso[gidx].loc);
								geneflag_set.push(ogeneso[gidx].flag);

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
										B.safari();
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
										B.safari();
										G.safari();
									}; 
									gen[0].onclick = function (){
										//	document.getElementById("divv").innerHTML=chrs[click].bands[blick].id;
										glick = gidx;
										var inputsf=ogenes[gidx].from;
										var inputet=ogenes[gidx].to;
										document.getElementById("search_field").value=ogenes[gidx].id;
										document.getElementById("chrSelect").value=chrs[click].name;
										document.getElementById("startInput").value=inputsf.toString();
										document.getElementById("endInput").value=inputet.toString();
										if(individuals[csi] != null){
											setTabb("brwview");
											jump();
											control_scanning = 0;
										}
											//chrs[click].name+":"+inputsf.toLocaleString().replace(/\.0+$/,"")+"-"+inputet.toLocaleString().replace(/\.0+$/,"");

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
		var sremove = spinner(B);
		var burrentt = B.text(70,20,"Scanning "+chrs[c].name+" : "+chrs[c].bands[b].id+" ...").attr({font:font_size2_text, opacity:1, "text-anchor":"start"}).attr({fill:"#000"});
		var XMLHttpReq9 = createXMLHttpRequest();
		XMLHttpReq9.open("GET","servlet/test.do?action=getCytoband&chr="+chrs[c].name+"&id="+chrs[c].bands[b].id,true);
		XMLHttpReq9.onreadystatechange = returnedCytoScore;
		XMLHttpReq9.send(null);
		function returnedCytoScore(){
			if(XMLHttpReq9.readyState==4){
				if(XMLHttpReq9.status==200){
					var score = parseFloat(XMLHttpReq9.responseXML.getElementsByTagName(xmlTagScore)[0].childNodes[0].nodeValue);
					//bandsscore_inter[c][b]=B.path(drawPath(chrs[c].bands[b].from+chrs[c].from,chrs[c].bands[b].to+chrs[c].from,total,radius+chrthick+5,5)).attr({fill: cytoscore2color(score),"fill-opacity":0.9,"stroke-width":0});
					//bandsscore_inter[c][b] = drawScore_inter(chrs[c].bands[b].from+chrs[c].from,chrs[c].bands[b].to+chrs[c].from,total,radius+chrthick,dis_chr2band,score);
					if(chrs[c].bands[b].scoreobj == undefined){
						chrs[c].bands[b].scoreobj = {};
					}
					if(chrs[c].bands[b].scoreobj[0]==undefined){
						var from = chrs[c].bands[b].from + chrs[c].from;
						var to = chrs[c].bands[b].to + chrs[c].from;
						chrs[c].bands[b].scoreobj[0] = B.rect(chrx+chrw+2, interval + h*from/total, 6, h*(to-from)/total, 0).attr({fill: score2flag(score).color,"stroke-width":0,"fill-opacity":score2flag(score).opacity});
						//drawScore_inter(chrs[c].bands[b].from+chrs[c].from,chrs[c].bands[b].to+chrs[c].from,total,radius+chrthick,dis_chr2band,score);
					}
					chrs[c].bands[b].scoreobj[0].animate({fill: score2flag(score).color, "fill-opacity":score2flag(score).opacity},animaTime);
					//chrs[c].bands[b].scoreobj[0].animate({transform: score2flag(score).interRscale}, 500, "elastic");
					//chrs[c].bands[b].scoreobj[0].hide();
					sremove();
					burrentt.remove();
					if(control_scanning==0){
						scan_text.show();

					}
					if(control_scanning==1&&click!=null&&blick!=null&&click==c&&blick==b){
						bands[c][b][0].onclick();
					}
					if(control_scanning==1 ){
						var bandfrom=chrs[c].bands[b].from+chrs[c].lengthh/180*b;
						var bandto=chrs[c].bands[b].to+chrs[c].lengthh/180*b;
						var chromlen;
						if(chrs[c].bands.length <=1){
							chromlen=chrs[c].lengthh;
						}
						else{
							chromlen=chrs[c].lengthh+chrs[c].lengthh/180*chrs[c].bands.length;
						}
						if(mode==2 && bands[c][b]==null){
							attr["fill-opacity"]=0;
							//bands[c][b]=B.path(drawPath(bandfrom,bandto,chromlen,radius+chrthick+dis_chr2band,bandthick)).attr(attr);
							bands[c][b]=drawCyto(chrs[c].bands[b].from, chrs[c].bands[b].to, chrs[c].centromere, chrs[c].lengthh, h, bandw, bandx, interval, chrs[c].bands[b].gieStain);
							if(chrs[c].bands[b].scoreobj[1]==undefined){
							//	chrs[c].bands[b].scoreobj[1] = B.path(drawPath(bandfrom,bandto,chromlen,radius+chrthick+dis_chr2band+bandthick+4,score2flag(score).normalthick)).attr({fill: score2flag(score).color,stroke:"#FFF","stroke-width":0,"fill-opacity":score2flag(score).opacity});
								chrs[c].bands[b].scoreobj[1] = drawScore(chrs[c].bands[b].from, chrs[c].bands[b].to, chrs[c].lengthh, h, bandx+bandw+3, interval);
							}
							bandsscore[c][b] = chrs[c].bands[b].scoreobj[1];
							//bandslables[c][b]=drawText(bandfrom,bandto,chromlen,radius+chrthick+dis_chr2band+bandthick,(-dis_lable2chr-10),chrs[c].bands[b].id,font_size2);
							bandslables[c][b]=B.text(bandx-20,interval+h*(chrs[c].bands[b].from+chrs[c].bands[b].to)/2/chrs[c].lengthh,chrs[c].bands[b].id).attr({font:font_size_text}).attr({fill:"#000"});
							bandslables[c][b].hide();
							bandsscore[c][b].hide();
							bands[c][b].hide();
						}/*else{
							bandsscore[c][b].remove();
							bandsscore[c][b] = B.path(drawPath(bandfrom,bandto,chromlen,radius+chrthick+dis_chr2band+bandthick+4,score2flag(score).outthick)).attr({fill: score2flag(score).color,stroke:"#FFF","stroke-width":0,"fill-opacity":score2flag(score).opacity});
							chrs[c].bands[b].scoreobj[1] = bandsscore[c][b];
							bandsscore[c][b].hide();
						}*/
						chrs[c].bands[b].scoreobj[1].animate({fill: score2flag(score).color}, animaTime);
						//chrs[c].bands[b].scoreobj[1].animate({transform: score2flag(score).outthickscale}, animaTime,"elastic");
						//chrs[c].bands[b].scoreobj[1].animate({path:drawPath(bandfrom,bandto,chromlen,radius+chrthick+dis_chr2band+bandthick+4,score2flag(score).outthick)}, animaTime);
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
					}else{
						stop_button.hide();
						stop_text.hide();
						scan_text.show();
					}
				}
				else{
					sremove();
					burrentt.remove();
					scan_text.show();
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

	function drawChr(chr, length, centromere, B_left, B_top){
		var interval = 20;
		var height = 16;
		var len = length/2000000;
		var cen = centromere/2000000;
		var x = B_left;
		var y = B_top + chr*(interval+height);
		var path;
		if(cen > 0){
			path = "M"+x+","+y
			+" A"+(height*0.7)+","+(height*0.7)+" 0 0,0 "+x+","+(y+height)
			+" L"+(x+cen)+","+(y+height)+" L"+(x+cen+10)+","+y
			+" L"+(x+len+10)+","+y
			+" A"+(height*0.7)+","+(height*0.7)+" 0 0,1 "+(x+len+10)+","+(y+height)
			+" L"+(x+cen+10)+","+(y+height)+" L"+(x+cen)+","+y
			+"Z";
		}else{
			path = "M"+x+","+y+" A"+(height*0.6)+","+(height*0.6)+" 0 0,0 "+x+","+(y+height)+" L"+(x+len)+","+(y+height)+" A"+(height*0.6)+","+(height*0.6)+" 0 0,0 "+(x+len)+","+y+"Z";
		}
		return path;
	}
	function drawCyto(from,to,centromere,length,h,w,B_left,B_top,gS){
		var corner = 10;
		var x1 = B_left;
		var x2 = B_left+corner;
		var x3 = B_left+w-corner;
		var x4 = B_left+w
		var y1 = from/length*h+B_top;
		var y2 = from/length*h+B_top+corner;
		var y3 = to/length*h+B_top-corner;
		var y4 = to/length*h+B_top;
		attr["fill"]=colorlist[gS];
		attr["fill-opacity"]=0.9;
		attr["stroke-width"]=1;
		attr["stroke"]="#666";
		if(from == 1 || from == centromere){
			return B.path("M"+x1+","+y2
					+" A"+corner+","+corner+" 0 0,1 "+x2+","+y1
					+" L"+x3+","+y1
					+" A"+corner+","+corner+" 0 0,1 "+x4+","+y2
					+" L"+x4+","+y4
					+" L"+x1+","+y4+"Z").attr(attr);
		}else if(to == length || to == centromere-1){
			return B.path("M"+x1+","+y1
					+" L"+x4+","+y1
					+" L"+x4+","+y3
					+" A"+corner+","+corner+" 0 0,1 "+x3+","+y4
					+" L"+x2+","+y4
					+" A"+corner+","+corner+" 0 0,1 "+x1+","+y3+"Z").attr(attr);
		}else{
			return B.rect(x1,y1,w,y4-y1,0).attr(attr);
		}
	}
	function drawScore(from,to,length,h,B_left,B_top){
		var w = 8;
		var x1 = B_left;
		var y1 = from/length*h+B_top;
		var y4 = to/length*h+B_top;
		return B.rect(x1,y1,w,y4-y1,0).attr({fill:"#FFF","stroke-width":0});
	}

	/*
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
//		var ay = (l/2-(r-dis)*Math.sin(a))/2;
		if(to-from<=total/180 && text == "MT"){
			return B.text(ax,ay,text).attr({font: (fontsize-8)+"px Trebuchet MS, Arial, sans-serif",opacity:1}).attr({fill: "#000"});
		}
		else{
			return B.text(ax,ay,text).attr({font: fontsize+"px Trebuchet MS, Arial, sans-serif",opacity:1}).attr({fill: "#000"});
		}
	}*/
	///////////////////////////////////////////////////////////////////////////////
	
	function getNumBybinary(n){
		var count = 0;  
        while (n > 0){  
            if(n % 2 ==0){
				count++;
			}
            n = Math.floor(n/2);  
        } 
		return count;
	}

	function score2flag(score){
		var score_attr = {};
		score_attr.interR = 2
		score_attr.listR = 3;
		score_attr.normalthick = 3;
		var num0 = getNumBybinary(score);
		if(score == 9){
			score_attr.color = colP;
			score_attr.opacity = 1;
		}else if(num0 == 2){
			score_attr.color = colDiff;
			score_attr.opacity = 1;
		}else if(num0 == 3){
			score_attr.color = colD;
			score_attr.opacity = 1;
		}else{
			score_attr.color = "#FFF";
			score_attr.opacity = 0;
		}
		/*
		switch(score){
			case 9:
				score_attr.color = "#00F";
				score_attr.opacity = 1;
				score_attr.interRscale = "s2.0 2.0 ";
				score_attr.listRscale = "s3.0 3.0 ";
				score_attr.outthickscale = "s2.0 ";
				score_attr.outthick = 9;
				//score_attr.listR = 9;
			break;
			case 17:
				score_attr.color = cytoscore2color(score);
				score_attr.opacity = 1;
				score_attr.interRscale = "s1.0 1.0 ";
				score_attr.listRscale = "s1.0 1.0 ";
				score_attr.outthickscale = "s1.0 ";
				score_attr.outthick = 3;
				//score_attr.listR = 3;
			break;
			case 49:
				score_attr.color = cytoscore2color(score);
				score_attr.opacity = 1;
				score_attr.interRscale = "s1.5 1.5 ";
				score_attr.listRscale = "s2.0 2.0 ";
				score_attr.outthickscale = "s1.5 ";
				score_attr.outthick = 6;
				//score_attr.listR = 6;
			break;
			case 113:
				score_attr.color = cytoscore2color(score);
				score_attr.opacity = 1;
				score_attr.interRscale = "s2.0 2.0 ";
				score_attr.listRscale = "s3.0 3.0 ";
				score_attr.outthickscale = "s2.0 ";
				score_attr.outthick = 9;
				//score_attr.listR = 9;
			break;
			default:
				score_attr.color = "#FFF";
				score_attr.opacity = 0;
				score_attr.interRscale = "";
				score_attr.listRscale = "";
				score_attr.outthickscale = "";
				score_attr.outthick = 0;
				//score_attr.listR = 0;
		}*/
		return score_attr;
	}

	/*
	function drawScore_inter(from, to, total, r, dis_chr2band, score) {
		var alpha = (from+to)/2/total*360;
		var a = (90 -alpha)*Math.PI/180;
		var ax = l/2+(r+(dis_chr2band/2))*Math.cos(a);
		var ay = l/2-(r+(dis_chr2band/2))*Math.sin(a);
		var flag_temp = score2flag(score);
		if(flag_temp!=null){
			return	B.circle(ax,ay,flag_temp.interR).attr({fill: flag_temp.color,"stroke-width":0,"fill-opacity":score2flag(score).opacity});
		}else{
			return null;
		}
	}*/
	genome[20][0].onmouseover();
	genome[20][0].onclick();
	bands[20][9][0].onmouseover();
	bands[20][9][0].onclick();
}

