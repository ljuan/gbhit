$(function() { 
    $("#search_field").autocomplete({ 
        source: function(request, response) { 
        	var input = request.term;
        		
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
			$("#select_info").css("background-color", "#ffffff");
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
			document.getElementById("chrSelect").value = temp_searchStr.split(":")[0];
			var temp_scale = temp_searchStr.split(":")[1];
			document.getElementById("startInput").value = parseInt(temp_scale.split("-")[0]);
			document.getElementById("endInput").value  = parseInt(temp_scale.split("-")[1]);
			jump();
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
