import ChatBoxUI from "../../js/gemp-022/chat.js";
import GempHallUI from "../../js/gemp-022/hallUi.js";
import { formatPrice } from "../../js/gemp-022/common.js";

var chat;
var hall;

$(document).ready(function () {

	$("#main").tabs();
	
	let chat = new ChatBoxUI("Game Hall", $("#chat"), "/gemp-stccg-server", true, null, false, null, true);
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

		$("#main").on("tabsload", function(event, ui) {
			let selected_tab_num = $( "#main" ).tabs( "option", "active" );
			switch(selected_tab_num) {
				case 0:
					// Game hall
					break;
				case 1:
					// Help
					$("#helpMain").tabs().addClass( "ui-tabs-vertical ui-helper-clearfix" );
					$("#help-tabs li").removeClass("ui-corner-top").addClass("ui-corner-left");
					break;
				case 2:
					// Events
					$("#eventsMain").tabs().addClass("ui-tabs-vertical ui-helper-clearfix");
					$("#event-tabs > ol > li").removeClass("ui-corner-top").addClass("ui-corner-left");
					
					// TODO: import these bad boys
					let leagueUI = new LeagueResultsUI("/gemp-stccg-server");
					
					let tourneyUI = new TournamentResultsUI("/gemp-stccg-server");
		
					$(".loadFinishedTournaments").button().click(
						function() {
							tourneyUI.loadHistoryTournaments();
						}
					);
					break;
				case 3:
					// Server info
					$("#infoMain").tabs().addClass("ui-tabs-vertical ui-helper-clearfix");
					$("#info-tabs > ol > li").removeClass("ui-corner-top").addClass("ui-corner-left");
					// TODO: Import StatsUI
					var ui = new StatsUI("/gemp-stccg-server", $("#statsParameters"), $("#stats"));
					$(".getStats").click();
					break;
				case 4:
					// My account
					$("#accountMain").tabs().addClass("ui-tabs-vertical ui-helper-clearfix");
					$("#account-tabs > ol > li").removeClass("ui-corner-top").addClass("ui-corner-left");
		
					$("#accountName").html(chat.userName);
					$("#pocketDiv").html(formatPrice(hall.pocketValue));
					break;
				case 5:
					// Admin
					$("#adminMain").tabs().addClass("ui-tabs-vertical ui-helper-clearfix");
					$("#admin-tabs > ol > li").removeClass("ui-corner-top").addClass("ui-corner-left");
					
					$("#landingTab").parent().hide();
					$("#landing").hide();
					
					if(!hall.userInfo.type.includes("a"))
					{
						$("#generalTab").parent().hide();
						$("#banTab").parent().hide();
					}
					
					if(!hall.userInfo.type.includes("l"))
					{
						$("#leagueTab").parent().hide();
					}
					break;
				default:
					console.error("main.tabs(): Unknown selected tab number" + selected_tab_num);
			}
		});
});