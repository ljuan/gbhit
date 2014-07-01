function loadChrBand(){
	document.getElementById("brd_genome").innerHTML="";
	document.getElementById("brd_genelist").innerHTML="";
	document.getElementById("upload_success").innerHTML="Loading Individual Genome...";
	var xmlAttribute_gieStain = "gS";
	var xmlTagScore="Score";
	var pattern = /<.*?>/g;
	var animaTime=100;

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
	//#####################

	var R = Raphael("brd_genome", l+30, l+30);
	var G = Raphael("brd_genelist",150,l);

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
						chromlen=chrs[chrom].lengthh+chrs[chrom].lengthh/180*chrs[chrom].bands.length;
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
}
