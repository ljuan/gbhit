var tracksImgareaselect;
var ppTracksImgareaselect;
var shiftKeyState = false;
var zKeyState = false;
var testFlag=0;//Liran
$(document).ready(function() {
	tracksImgareaselect = $(document.getElementById("divTrack")).imgAreaSelect({
		classPrefix : "divTrackImgareaselect",
		handles : false,
		resizable : false,
		movable : false,
		autoHide : true,
		instance : true,
		disable : true,
		minHeight : 100,
		onSelectChange : function(img, selection) {
			var baseX = $(document.getElementById("divTrack")).width() - trackLength_user;
			searchLength_user = end_user - start_user + 1;
		//	if(selection.x1 >= baseX) {
			if(selection.x1 >= baseX && testFlag!=1) {//Liran
				var tempStart = start_user + parseInt(searchLength_user / trackLength_user * (selection.x1 - baseX));
				var tempEnd = start_user + parseInt(searchLength_user / trackLength_user * (selection.x2 - baseX));
				showuserSearchIndex(tempStart, tempEnd);
				setSliderValue(tempEnd - tempStart + 1);
				drawScaleboxOnCytobandsImg(tempEnd - tempStart + 1, tempStart);
			}
			testFlag=0;//Liran
		},
		onSelectEnd : function(img, selection) {
			var baseX = $(document.getElementById("divTrack")).width() - trackLength_user;
			var tempStr = chrNum + ":" + start_user + "-" + end_user;
			searchLength_user = end_user - start_user + 1;

			if(selection.x1 >= baseX && selection.x2 < baseX + trackLength_user + 1) {
				var tempStart = start_user + parseInt(searchLength_user / trackLength_user * (selection.x1 - baseX));
				var tempEnd = start_user + parseInt(searchLength_user / trackLength_user * (selection.x2 - baseX));
				//document.getElementById("searchInput").value = chrNum + ":" + tempStart + "-" + tempEnd;
				start_user = tempStart;
				end_user = tempEnd;
				searchLength_user = end_user - start_user + 1;
				startIndex = start_user - searchLength_user;
				endIndex = end_user + searchLength_user;
				searchLength = searchLength_user * 3;
				setSliderValue(searchLength_user);
				drawScaleboxOnCytobandsImg(searchLength_user, start_user);
				showuserSearchIndex(start_user, end_user);
				testFlag=1;//Liran
				showRef();
			} else {
				//document.getElementById("searchInput").value = tempStr;
				setSliderValue(searchLength_user);
				drawScaleboxOnCytobandsImg(searchLength_user, start_user);
				showuserSearchIndex(start_user, end_user);
			}
			
			shiftKeyState = false;
			var trackTable = document.getElementById("tableTrack");
			var trackContentNodes = trackTable.getElementsByClassName("trackContent");
			for(var i = 0; i < trackContentNodes.length; i++) {
				$(trackContentNodes[i].childNodes[0]).css("cursor", "url(./image/Grabber.cur),auto");
			}
			$("#ppContent .canvasTrackcontent").each(function(arrayindex, arrayele) {
				$(arrayele).css("cursor", "url(./image/Grabber.cur),auto");
			});
		}
	});
	
	ppTracksImgareaselect = $("#ppContent").imgAreaSelect({
		classPrefix : "divTrackImgareaselect",
		handles : false,
		resizable : false,
		movable : false,
		autoHide : true,
		instance : true,
		disable : true,
		minHeight : 100,
		onSelectChange : function(img, selection) {
			var baseX = $(document.getElementById("ppContent")).width() - trackLength_user;
			searchLength_user = end_user - start_user + 1;
		//	if(selection.x1 >= baseX) {
			if(selection.x1 >= baseX && testFlag!=1) {//Liran
				var tempStart = start_user + parseInt(searchLength_user / trackLength_user * (selection.x1 - baseX));
				var tempEnd = start_user + parseInt(searchLength_user / trackLength_user * (selection.x2 - baseX));
				showuserSearchIndex(tempStart, tempEnd);
				setSliderValue(tempEnd - tempStart + 1);
				drawScaleboxOnCytobandsImg(tempEnd - tempStart + 1, tempStart);
			}
			testFlag=0;//Liran
		},
		onSelectEnd : function(img, selection) {
			var baseX = $(document.getElementById("ppContent")).width() - trackLength_user;
			var tempStr = chrNum + ":" + start_user + "-" + end_user;
			searchLength_user = end_user - start_user + 1;

			if(selection.x1 >= baseX && selection.x2 < baseX + trackLength_user + 1) {
				var tempStart = start_user + parseInt(searchLength_user / trackLength_user * (selection.x1 - baseX));
				var tempEnd = start_user + parseInt(searchLength_user / trackLength_user * (selection.x2 - baseX));
				start_user = tempStart;
				end_user = tempEnd;
				searchLength_user = end_user - start_user + 1;
				startIndex = start_user - searchLength_user;
				endIndex = end_user + searchLength_user;
				searchLength = searchLength_user * 3;
				setSliderValue(searchLength_user);
				drawScaleboxOnCytobandsImg(searchLength_user, start_user);
				showuserSearchIndex(start_user, end_user);
				testFlag=1;//Liran
				showRef();
			} else {
				setSliderValue(searchLength_user);
				drawScaleboxOnCytobandsImg(searchLength_user, start_user);
				showuserSearchIndex(start_user, end_user);
			}
			
			shiftKeyState = false;
			var trackTable = document.getElementById("tableTrack");
			var trackContentNodes = trackTable.getElementsByClassName("trackContent");
			for(var i = 0; i < trackContentNodes.length; i++) {
				$(trackContentNodes[i].childNodes[0]).css("cursor", "url(./image/Grabber.cur),auto");
			}
			$("#ppContent .canvasTrackcontent").each(function(arrayindex, arrayele) {
				$(arrayele).css("cursor", "url(./image/Grabber.cur),auto");
			});
		}
	});
	
	var trackTable = document.getElementById("tableTrack");
	var trackContentNodes = trackTable.getElementsByClassName("trackContent");
	for(var i = 0; i < trackContentNodes.length; i++) {
		trackContentNodes[i].childNodes[0].onmousedown = mouseDownRightCanvas;
	}
	
	document.onkeydown = keyDown;
	document.onkeyup = keyUp;
	//document.onmouseup = mouseUp;   //the detail information of the event "onmouseup" about dragToZoomin in the file "trackMove.js"
});
function keyDown(evt) {
	evt = evt ? evt : ((window.event) ? window.event : "");
	var key = evt.keyCode ? evt.keyCode : evt.which;
	if(key == 16) {
		shiftKeyState = true;
		var trackTable = document.getElementById("tableTrack");
		var trackContentNodes = trackTable.getElementsByClassName("trackContent");
		for(var i = 0; i < trackContentNodes.length; i++) {
			$(trackContentNodes[i].childNodes[0]).css("cursor", "crosshair");
		}
		$("#ppContent .canvasTrackcontent").each(function(arrayindex, arrayele) {
			$(arrayele).css("cursor", "crosshair");
		});
		var verticalLine = $(document.getElementById("verticalLine"));
		var indexSpan = $(document.getElementById("indexSpan"));
		verticalLine.css("display", "none");
		indexSpan.css("display", "none");
	}
	if(key == 90){
		zKeyState = true;
		mousewheelzoomScale = searchLength_user;
		mousewheelzoomStart = start_user;
	}
}

function keyUp(evt) {
	evt = evt ? evt : ((window.event) ? window.event : "");
	var key = evt.keyCode ? evt.keyCode : evt.which;
	if(key == 16) {
		shiftKeyState = false;
		var trackTable = document.getElementById("tableTrack");
		var trackContentNodes = trackTable.getElementsByClassName("trackContent");
		for(var i = 0; i < trackContentNodes.length; i++) {
			$(trackContentNodes[i].childNodes[0]).css("cursor", "url(./image/Grabber.cur),auto");
		}
		$("#ppContent .canvasTrackcontent").each(function(arrayindex, arrayele) {
			$(arrayele).css("cursor", "url(./image/Grabber.cur),auto");
		});
	}
	if(key == 90){
		zKeyState = false;
		if(start_user - searchLength_user != startIndex || searchLength != searchLength_user * 3){
			end_user = start_user + searchLength_user - 1;
			startIndex = start_user - searchLength_user;
			endIndex = end_user + searchLength_user;
			searchLength = searchLength_user * 3;
			showRef();
		}
	}
}

function mouseOverRightCanvas() {
	if(shiftKeyState) {
		tracksImgareaselect.setOptions({
			disable : false
		});
	}
}

function mouseDownRightCanvas() {
	if(shiftKeyState) {
		tracksImgareaselect.setOptions({
			disable : false,
			minHeight : $(document.getElementById("divTrack")).height()
		});
		$(".canvasTrackcontent").draggable("destroy");
	}
}

function mouseDownRightCanvasInPP(){
	if(shiftKeyState) {
		ppTracksImgareaselect.setOptions({
			disable : false,
			minHeight : $(document.getElementById("ppContent")).height()
		});
		$(".canvasTrackcontent").draggable("destroy");
	}
}
