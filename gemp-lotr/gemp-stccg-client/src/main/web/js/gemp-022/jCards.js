var cardCache = {};
var cardScale = 357 / 497;

var packBlueprints = {
    "(S)FotR - Starter": "/gemp-lotr/images/boosters/fotr_starter_selection.png",
    "(S)MoM - Starter": "/gemp-lotr/images/boosters/mom_starter_selection.png",
    "(S)RotEL - Starter": "/gemp-lotr/images/boosters/rotel_starter_selection.png",

    "(S)TTT - Starter": "/gemp-lotr/images/boosters/ttt_starter_selection.png",
    "(S)BoHD - Starter": "/gemp-lotr/images/boosters/bohd_starter_selection.png",
    "(S)EoF - Starter": "/gemp-lotr/images/boosters/eof_starter_selection.png",

    "(S)RotK - Starter": "/gemp-lotr/images/boosters/rotk_starter_selection.png",
    "(S)SoG - Starter": "/gemp-lotr/images/boosters/sog_starter_selection.png",
    "(S)MD - Starter": "/gemp-lotr/images/boosters/md_starter_selection.png",

    "(S)SH - Starter": "/gemp-lotr/images/boosters/sh_starter_selection.png",
    "(S)BR - Starter": "/gemp-lotr/images/boosters/br_starter_selection.png",
    "(S)BL - Starter": "/gemp-lotr/images/boosters/bl_starter_selection.png",
    
    "(S)HU - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)RoS - Starter": "/gemp-lotr/images/boosters/starter_selection.png",

    "(S)FotR - Tengwar": "/gemp-lotr/images/boosters/fotr_tengwar_selection.png",
    "(S)TTT - Tengwar": "/gemp-lotr/images/boosters/ttt_tengwar_selection.png",
    "(S)RotK - Tengwar": "/gemp-lotr/images/boosters/rotk_tengwar_selection.png",
    "(S)SH - Tengwar": "/gemp-lotr/images/boosters/sh_tengwar_selection.png",
    "(S)Tengwar": "/gemp-lotr/images/boosters/tengwar_selection.png",

    "(S)All Decipher Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)Movie Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)TS Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",

    "FotR - League Starter": "/gemp-lotr/images/boosters/fotr_league_starter.png",
    "Random FotR Foil Common": "/gemp-lotr/images/boosters/random_foil.png",
    "Random FotR Foil Uncommon": "/gemp-lotr/images/boosters/random_foil.png",

    "FotR - Gandalf Starter": "/gemp-lotr/images/boosters/fotr_gandalf_starter.png",
    "FotR - Aragorn Starter": "/gemp-lotr/images/boosters/fotr_aragorn_starter.png",
    "FotR - Booster": "/gemp-lotr/images/boosters/fotr_booster.png",

    "MoM - Gandalf Starter": "/gemp-lotr/images/boosters/mom_gandalf_starter.png",
    "MoM - Gimli Starter": "/gemp-lotr/images/boosters/mom_gimli_starter.png",
    "MoM - Booster": "/gemp-lotr/images/boosters/mom_booster.png",

    "RotEL - Boromir Starter": "/gemp-lotr/images/boosters/rotel_boromir_starter.png",
    "RotEL - Legolas Starter": "/gemp-lotr/images/boosters/rotel_legolas_starter.png",
    "RotEL - Booster": "/gemp-lotr/images/boosters/rotel_booster.png",

    "TTT - Aragorn Starter": "/gemp-lotr/images/boosters/ttt_aragorn_starter.png",
    "TTT - Theoden Starter": "/gemp-lotr/images/boosters/ttt_theoden_starter.png",
    "TTT - Booster": "/gemp-lotr/images/boosters/ttt_booster.png",

    "BoHD - Eowyn Starter": "/gemp-lotr/images/boosters/bohd_eowyn_starter.png",
    "BoHD - Legolas Starter": "/gemp-lotr/images/boosters/bohd_legolas_starter.png",
    "BoHD - Booster": "/gemp-lotr/images/boosters/bohd_booster.png",

    "EoF - Faramir Starter": "/gemp-lotr/images/boosters/eof_faramir_starter.png",
    "EoF - Witch-king Starter": "/gemp-lotr/images/boosters/eof_witch_king_starter.png",
    "EoF - Booster": "/gemp-lotr/images/boosters/eof_booster.png",

    "RotK - Aragorn Starter": "/gemp-lotr/images/boosters/rotk_aragorn_starter.png",
    "RotK - Eomer Starter": "/gemp-lotr/images/boosters/rotk_eomer_starter.png",
    "RotK - Booster": "/gemp-lotr/images/boosters/rotk_booster.png",

    "SoG - Merry Starter": "/gemp-lotr/images/boosters/sog_merry_starter.png",
    "SoG - Pippin Starter": "/gemp-lotr/images/boosters/sog_pippin_starter.png",
    "SoG - Booster": "/gemp-lotr/images/boosters/sog_booster.png",

    "MD - Frodo Starter": "/gemp-lotr/images/boosters/md_frodo_starter.png",
    "MD - Sam Starter": "/gemp-lotr/images/boosters/md_sam_starter.png",
    "MD - Booster": "/gemp-lotr/images/boosters/md_booster.png",

    "SH - Aragorn Starter": "/gemp-lotr/images/boosters/sh_aragorn_starter.png",
    "SH - Eowyn Starter": "/gemp-lotr/images/boosters/sh_eowyn_starter.png",
    "SH - Gandalf Starter": "/gemp-lotr/images/boosters/sh_gandalf_starter.png",
    "SH - Legolas Starter": "/gemp-lotr/images/boosters/sh_legolas_starter.png",
    "SH - Booster": "/gemp-lotr/images/boosters/sh_booster.png",

    "BR - Mouth Starter": "/gemp-lotr/images/boosters/br_mouth_starter.png",
    "BR - Saruman Starter": "/gemp-lotr/images/boosters/br_saruman_starter.png",
    "BR - Booster": "/gemp-lotr/images/boosters/br_booster.png",

    "BL - Arwen Starter": "/gemp-lotr/images/boosters/bl_arwen_starter.png",
    "BL - Boromir Starter": "/gemp-lotr/images/boosters/bl_boromir_starter.png",
    "BL - Booster": "/gemp-lotr/images/boosters/bl_booster.png",

    "HU - Aragorn Starter": "/gemp-lotr/images/boosters/hu_aragorn_starter.png",
    "HU - Mauhur Starter": "/gemp-lotr/images/boosters/hu_mauhur_starter.png",
    "HU - Booster": "/gemp-lotr/images/boosters/hu_booster.png",

    "RoS - Uruk Rampage Starter": "/gemp-lotr/images/boosters/ros_uruk_rampage_starter.png",
    "RoS - Evil Man Starter": "/gemp-lotr/images/boosters/ros_evil_man_starter.png",
    "RoS - Booster": "/gemp-lotr/images/boosters/ros_booster.png",

    "TaD - Faramir Starter": "/gemp-lotr/images/boosters/eof_faramir_starter.png",
    "TaD - Witch-king Starter": "/gemp-lotr/images/boosters/eof_witch_king_starter.png",
    "TaD - Booster": "/gemp-lotr/images/boosters/tad_booster.png",

    "REF - Booster": "/gemp-lotr/images/boosters/ref_booster.png",

    "Special-01": "/gemp-lotr/images/boosters/special-01.png",
    "Special-02": "/gemp-lotr/images/boosters/special-02.png",
    "Special-03": "/gemp-lotr/images/boosters/special-03.png",
    "Special-04": "/gemp-lotr/images/boosters/special-04.png",
    "Special-05": "/gemp-lotr/images/boosters/special-05.png",
    "Special-06": "/gemp-lotr/images/boosters/special-06.png",
    "Special-07": "/gemp-lotr/images/boosters/special-07.png",
    "Special-08": "/gemp-lotr/images/boosters/special-08.png",
    "Special-09": "/gemp-lotr/images/boosters/special-09.png",

    "(S)Special-1-3": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Special-4-6": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Special-7-9": "/gemp-lotr/images/boosters/starter_selection.png",

    "TSSealedS1D1": "/gemp-lotr/images/boosters/TSS1D1.png",
    "TSSealedS1D2": "/gemp-lotr/images/boosters/TSS1D2.png",
    "TSSealedS1D3": "/gemp-lotr/images/boosters/TSS1D3.png",
    "TSSealedS2D1": "/gemp-lotr/images/boosters/TSS2D1.png",
    "TSSealedS2D2": "/gemp-lotr/images/boosters/TSS2D2.png",
    "TSSealedS2D3": "/gemp-lotr/images/boosters/TSS2D3.png",
    "TSSealedS3D1": "/gemp-lotr/images/boosters/TSS3D1.png",
    "TSSealedS3D2": "/gemp-lotr/images/boosters/TSS3D2.png",
    "TSSealedS3D3": "/gemp-lotr/images/boosters/TSS3D3.png",

    "(S)TSSealed-S1": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)TSSealed-S2": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)TSSealed-S3": "/gemp-lotr/images/boosters/starter_selection.png",

    "Expanded": "/gemp-lotr/images/boosters/expanded.png",
    "Wraith": "/gemp-lotr/images/boosters/wraith.png",
    "AgesEnd": "/gemp-lotr/images/boosters/ages_end.png",
    
    "(S)FotR Block Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)TTT Block Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)RotK Block Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)SoG/MD Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)WotR Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)HU Block Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)Expanded Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",
    "(S)BR/HU/RoS Choice - Booster": "/gemp-lotr/images/boosters/booster_selection.png",

    "(S)Movie Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Reflections Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Hobbits Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Expanded Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Evil Characters Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)FotR Block Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)TTT Block Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)RotK Block Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)TS Special Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)Movie Special Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)WotR Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "(S)HU Block Choice - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    
    "(S)PC Promo Art Selection": "/gemp-lotr/images/boosters/pc_promo_selection.png",
    "(S)Masterwork Selection": "/gemp-lotr/images/boosters/masterwork_selection.png",
    
    
    "(S)SotP - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    
    "SotP - Tales & Weather Starter": "/gemp-lotr/images/boosters/V1-starter-tales_weather.png",
    "SotP - Aragorn Signet & Twilight Starter": "/gemp-lotr/images/boosters/V1-starter-aragorn_twilight.png",
    "SotP - Gandalf's Allies & Tentacles Starter": "/gemp-lotr/images/boosters/V1-starter-gandalf_tentacles.png",
    "SotP - Frodo Signet & Sauron Starter": "/gemp-lotr/images/boosters/V1-starter-frodo_sauron.png",
    
    "(R)SotP - Rare": "/gemp-lotr/images/boosters/V1-random-rare.png",
    "(R)SotP - Uncommon": "/gemp-lotr/images/boosters/V1-random-uncommon.png",
    "(R)SotP - Common": "/gemp-lotr/images/boosters/V1-random-common.png",
    
    "(S)SARU - Starter": "/gemp-lotr/images/boosters/starter_selection.png",
    "SARU - Rohan & Dunland Starter": "/gemp-lotr/images/boosters/saru-rohan-dunland.png",
    "SARU - Dwarf & Evil Men Starter": "/gemp-lotr/images/boosters/saru-dwarf-men.png",
    "SARU - Gondor & Isengard Orc Starter": "/gemp-lotr/images/boosters/saru-gondor-isenorc.png",
    "SARU - Pipeweed & Isengard Uruk Starter": "/gemp-lotr/images/boosters/saru-pipeweed-isenuruks.png",
    "SARU - Smeagol & Urukhai Starter": "/gemp-lotr/images/boosters/saru-smeagol-uruks.png",
    
    "Event Chase Booster": "/gemp-lotr/images/boosters/event_award_booster.png",
    "Tournament Random Chase Card Selector": "/gemp-lotr/images/boosters/tournament_event_award.png",
    "Countdown to the King Collection": "/gemp-lotr/images/boosters/countdown_collection.png",
    "The Nine Riders Collection": "/gemp-lotr/images/boosters/nine_riders_collection.png",
    "(S)Alt Image Promo Selection": "/gemp-lotr/images/boosters/promo_selection.png",
    
    "Random Common Foil": "/gemp-lotr/images/boosters/random_foil.png",
    "Random Uncommon Foil": "/gemp-lotr/images/boosters/random_foil.png",
    "Random Rare Foil": "/gemp-lotr/images/boosters/random_foil.png",
    "Random Ultra Rare Foil": "/gemp-lotr/images/boosters/random_foil.png"
};

var Card = Class.extend({
    blueprintId: null,
    foil: null,
    tengwar: null,
    hasWiki: null,
    horizontal: null,
    locationIndex: null,
    zone: null,
    cardId: null,
    owner: null,
    siteNumber: 1,
    attachedCards: null,
    errata: null,

    init: function (blueprintId, zone, cardId, owner, imageUrl, locationIndex, upsideDown) {
        this.blueprintId = blueprintId;
        this.imageUrl = imageUrl;
        this.upsideDown = upsideDown;


        var imageBlueprint = blueprintId;
        var len = imageBlueprint.length;
        this.foil = imageBlueprint.substring(len - 1, len) == "*";
        if (this.foil)
            imageBlueprint = imageBlueprint.substring(0, len - 1);

        var bareBlueprint = imageBlueprint;
        len = bareBlueprint.length;
        this.tengwar = bareBlueprint.substring(len - 1, len) == "T";
        if (this.tengwar)
            bareBlueprint = bareBlueprint.substring(0, len - 1);

        this.hasWiki = packBlueprints[imageBlueprint] == null;

        this.zone = zone;
        this.cardId = cardId;
        this.owner = owner;
        if (locationIndex !== undefined) {
            this.locationIndex = parseInt(locationIndex);
        }
        this.attachedCards = new Array();
        if (imageBlueprint == "rules") {
            this.imageUrl = "/gemp-lotr/images/rules.png";
        } else {
            if (cardCache[imageBlueprint] != null) {
                var cardFromCache = cardCache[imageBlueprint];
                this.horizontal = cardFromCache.horizontal;
//                this.imageUrl = cardFromCache.imageUrl;
                this.errata = cardFromCache.errata;
            } else {
//                this.imageUrl = this.getUrlByBlueprintId(bareBlueprint);
                this.horizontal = this.isHorizontal(bareBlueprint);

                var separator = bareBlueprint.indexOf("_");
                var setNo = parseInt(bareBlueprint.substr(0, separator));
                var cardNo = parseInt(bareBlueprint.substr(separator + 1));

                this.errata = this.getErrata(setNo, cardNo) != null;
                cardCache[imageBlueprint] = {
                    imageUrl: this.imageUrl,
                    horizontal: this.horizontal,
                    errata: this.errata
                };
            }
        }
    },

    isTengwar: function () {
        return this.tengwar;
    },

    isFoil: function () {
        return this.foil;
    },

    isUpsideDown: function () {
        return this.upsideDown;
    },

    hasErrata: function () {
        var separator = this.blueprintId.indexOf("_");
        var setNo = parseInt(this.blueprintId.substr(0, separator));
        
        if(setNo >= 50 && setNo <= 89)
            return true;
        
        return this.errata;
    },

    isPack: function () {
        return packBlueprints[this.blueprintId] != null;
    },

    isHorizontal: function (blueprintId) {
        return false;
    },

    getUrlByBlueprintId: function (blueprintId, ignoreErrata) {
        if (packBlueprints[blueprintId] != null)
            return packBlueprints[blueprintId];

        var separator = blueprintId.indexOf("_");
        var setNo = parseInt(blueprintId.substr(0, separator));
        var cardNo = parseInt(blueprintId.substr(separator + 1));

        var errata = this.getErrata(setNo, cardNo);
        if (errata != null && (ignoreErrata === undefined || !ignoreErrata))
            return errata;

        var mainLocation = this.getMainLocation(setNo, cardNo);

        var cardStr;

        if (this.isMasterworks(setNo, cardNo))
            cardStr = this.formatSetNo(setNo) + "O0" + (cardNo - this.getMasterworksOffset(setNo));
        else
            cardStr = this.formatCardNo(setNo, cardNo);

        return mainLocation + "LOTR" + cardStr + (this.isTengwar() ? "T" : "") + ".jpg";
    },

    getWikiLink: function () {
        var imageUrl = this.getUrlByBlueprintId(this.blueprintId, true);
        var afterLastSlash = imageUrl.lastIndexOf("/") + 1;
        var countAfterLastSlash = imageUrl.length - 4 - afterLastSlash;
        return "http://wiki.lotrtcgpc.net/wiki/" + imageUrl.substr(afterLastSlash, countAfterLastSlash);
    },

    hasWikiInfo: function () {
        return this.hasWiki;
    },

    formatSetNo: function (setNo) {
        var setNoStr;
        if (setNo < 10)
            setNoStr = "0" + setNo;
        else
            setNoStr = setNo;
        return setNoStr;
    },

    formatCardNo: function (setNo, cardNo) {
        var setNoStr = this.formatSetNo(setNo);

        var cardStr;
        if (cardNo < 10)
            cardStr = setNoStr + "00" + cardNo;
        else if (cardNo < 100)
            cardStr = setNoStr + "0" + cardNo;
        else
            cardStr = setNoStr + "" + cardNo;

        return cardStr;
    },

    getMainLocation: function (setNo, cardNo) {
        return "https://i.lotrtcgpc.net/decipher/";
    },

    getMasterworksOffset: function (setNo) {
        if (setNo == 17)
            return 148;
        if (setNo == 18)
            return 140;
        return 194;
    },

    isMasterworks: function (setNo, cardNo) {
        if (setNo == 12)
            return cardNo > 194;
        if (setNo == 13)
            return cardNo > 194;
        if (setNo == 15)
            return cardNo > 194 && cardNo < 204;
        if (setNo == 17)
            return cardNo > 148;
        if (setNo == 18)
            return cardNo > 140;
        return false;
    },

    remadeErratas: {
        "0": [7],
        "1": [3, 12, 43, 46, 55, 109, 113, 138, 162, 211, 235, 263, 309, 318, 331, 338, 343, 360],
        "3": [48, 110],
        "4": [63, 236, 237, 352],
        "6": [39, 46, 85],
        "7": [10, 14, 66, 114, 133, 134, 135, 182, 284, 285, 289, 302, 357],
        "8": [20, 33, 69],
        "17": [15, 87, 96, 118],
        "18": [8, 12, 20, 25, 35, 48, 50, 55, 77, 78, 79, 80, 82, 94, 97, 133]
    },

    getErrata: function (setNo, cardNo) {
        if (this.remadeErratas["" + setNo] != null && $.inArray(cardNo, this.remadeErratas["" + setNo]) != -1)
            return "/gemp-lotr/images/erratas/LOTR" + this.formatCardNo(setNo, cardNo) + ".jpg";
        return null;
    },

    getHeightForColumnWidth:function (columnWidth) {
        if (this.horizontal)
            return columnWidth;
        else
            return Math.floor(columnWidth / cardScale);
    },

    getHeightForWidth: function (width) {
        if (this.horizontal)
            return Math.floor(width * cardScale);
        else
            return Math.floor(width / cardScale);
    },

    getWidthForHeight: function (height) {
        if (this.horizontal)
            return Math.floor(height / cardScale);
        else
            return Math.floor(height * cardScale);
    },

    getWidthForMaxDimension: function (maxDimension) {
        if (this.horizontal)
            return maxDimension;
        else
            return Math.floor(maxDimension * cardScale);
    },

    displayCardInfo: function (container) {
        that = this;
        container.html("");
        container.html("<div style='scroll: auto'></div>");
        container.append(createFullCardDiv(that.imageUrl, that.foil, that.horizontal, that.isPack()));
//        if (that.hasWikiInfo())
//            container.append("<div><a href='" + that.getWikiLink() + "' target='_blank'>Wiki</a></div>");

        var horSpace = 30;
        var vertSpace = 65;
        var dialogWidth;
        var dialogHeight;

        if (that.horizontal) {
            dialogWidth = 500 + horSpace;
            dialogHeight = 360 + vertSpace;
        } else {
            dialogWidth = 360 + horSpace;
            dialogHeight = 500 + vertSpace;
        }

        container.dialog(
            {width:Math.min(dialogWidth, $(window).width()), height:Math.min(dialogHeight, $(window).height())}
        );
        container.dialog("open");
    }

});

function createCardDiv(image, text, foil, tokens, noBorder, errata) {
    return createCardDiv(image, text, foil, tokens, noBorder, errata, false);
}

function createCardDiv(image, text, foil, tokens, noBorder, errata, upsideDown, cardId) {
    if (cardId == null) {
        if (upsideDown)
            var imgClass = "card_img upside-down";
        else
            var imgClass = "card_img";
    } else {
        if (upsideDown)
            var imgClass = "card_img upside-down card_img_" + cardId;
        else
            var imgClass = "card_img card_img_" + cardId;
    }


    var cardDiv = $("<div class='card'><img class='" + imgClass + "' src='" + image + "' width='100%' height='100%'>" + ((text != null) ? text : "") + "</div>");

    if (errata) {
        var errataDiv = $("<div class='errataOverlay'><img src='/gemp-lotr/images/errata-vertical.png' width='100%' height='100%'></div>");
        cardDiv.append(errataDiv);
    }

    var foilPresentation = getFoilPresentation();

    if (foil && foilPresentation !== 'none') {
        var foilImage = (foilPresentation === 'animated') ? "foil.gif" : "holo.jpg";
        var foilDiv = $("<div class='foilOverlay'><img src='/gemp-lotr/images/" + foilImage + "' width='100%' height='100%'></div>");
        cardDiv.append(foilDiv);
    }

    if (tokens === undefined || tokens) {
        var overlayDiv = $("<div class='tokenOverlay'></div>");
        cardDiv.append(overlayDiv);
    }
    var borderDiv = $("<div class='borderOverlay'><img class='actionArea' src='/gemp-lotr/images/pixel.png' width='100%' height='100%'></div>");
    if (noBorder)
        borderDiv.addClass("noBorder");
    cardDiv.append(borderDiv);

    return cardDiv;
}

function getFoilPresentation() {
    var result = $.cookie("foilPresentation");
    if (result === null)
        result = "static";
    if (result === "true")
        result = "animated";
    if (result === "false")
        result = "static";
    return result;
}

function createFullCardDiv(image, foil, horizontal, noBorder) {

    if (horizontal) orientation = "Horizontal";
    else orientation = "Vertical";

    if (noBorder) var borderClass = "noBorderOverlay";
    else var borderClass = "borderOverlay";

    var cardDiv = $("<div class='fullCardDiv" + orientation + "'></div>");
    cardDiv.append($("<div class='fullCardWrapper'>" +
        "<img class='fullCardImg" + orientation + "' src='" + image + "'></div>"));
    cardDiv.append($("<div class='" + borderClass + orientation + "'>" +
        "<img class='actionArea' src='/gemp-lotr/images/pixel.png' width='100%' height='100%'></div>"));

    if (foil && getFoilPresentation() !== 'none') {
        var foilImage = (getFoilPresentation() === 'animated') ? "foil.gif" : "holo.jpg";
        cardDiv.append($("div class='foilOverlay" + orientation + "'>" +
            "<img src='/gemp-lotr/images/" + foilImage + "' width='100%' height='100%'></div>"));
    }

    return cardDiv;
}

function createSimpleCardDiv(image) {
    var cardDiv = $("<div class='card'><img src='" + image + "' width='100%' height='100%'></div>");

    return cardDiv;
}

function getCardDivFromId(cardId) {
    return $(".card:cardId(" + cardId + ")");
}