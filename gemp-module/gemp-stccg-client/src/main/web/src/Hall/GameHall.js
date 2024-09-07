var chat;
	var hall;

	$(document).ready(function () {

		$("#main").tabs();
		//Hiding the Users tab until that feature is ready (CL - removed this in the STCCG code)
//		$("#tabs > ul :nth-child(5)").hide();
		
		chat = new ChatBoxUI("Game Hall", $("#chat"), "/gemp-stccg-server", true, null, false, null, true);
		chat.showTimestamps = true;
		
		hall = new GempHallUI("/gemp-stccg-server", chat);


		var infoDialog = $("<div></div>")
				.dialog({
					autoOpen:false,
					closeOnEscape:true,
					resizable:false,
					title:"Card information",
					closeText: ""
				});

		$("body").click(
			function (event) {
				var tar = $(event.target);

				if (tar.hasClass("cardHint")) {
					var ids = tar.attr("value").split(",");
					
					infoDialog.html("");
					infoDialog.html("<div style='scroll: auto'></div>");
					var floatCardDiv = $("<div style='float: left; display:flex; gap: 5px;'></div>");
					
					var horiz = false;
					for(var i = 0; i < ids.length; i++) {
						var blueprintId = ids[i];
						var card = new Card(blueprintId, "SPECIAL", "hint", ""); // TODO - Card missing imageUrl and locationIndex
						horiz = horiz || card.horizontal;
						floatCardDiv.append(createFullCardDiv(card.imageUrl, card.foil, card.horizontal));
					}
					
					infoDialog.append(floatCardDiv);

					var windowWidth = $(window).width();
					var windowHeight = $(window).height();

					var horSpace = 30;
					var vertSpace = 45;
					var height = 505;
					var width = 340;

					infoDialog.dialog({
						title:"Card information",
						
					});
					if (horiz) {
						// 500x360
						infoDialog.dialog({width:Math.min((height + 10) * ids.length, windowWidth), 
						                  height:Math.min((width + 90), windowHeight)});
					} else {
						// 360x500
						infoDialog.dialog({width:Math.min((width + 30) * ids.length, windowWidth), 
						                  height:Math.min((height + 45), windowHeight)});
					}
					infoDialog.dialog("open");

					event.stopPropagation();
					return false;
				} else if (tar.hasClass("prizeHint")) {
					var prizeDescription = tar.attr("value");

					infoDialog.text(prizeDescription);

					infoDialog.dialog({title:"Prizes details", width:300, height: 150});
					infoDialog.dialog("open");

					event.stopPropagation();
					return false;
				}

				return true;
			});
			
	});