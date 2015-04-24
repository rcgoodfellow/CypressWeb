/**
 * The Cypress Project
 * Created by ry on 4/6/15.
 */

var currentExp = "";
var codeWindows = {};
var exp_view = {};
var sock = new WebSocket("wss://" + window.location.host + "/ws");

sock.onmessage = function(msg) {
    console.log("seckmess:");
    exp_view = JSON.parse(msg.data);
    console.log(exp_view);
    drawModel();
};

function newExp() {
    $(".exp_preview_new")
        .attr("contenteditable", "true")
        .addClass("exp_new_editing")
        .attr("onkeypress", "newExpKeyPrH(event)");
}

function newExpKeyPrH(evt) {
    var x = evt.which || evt.keyCode;
    if(x == 13) {
        var node = $(".exp_preview_new");
        var expname = node[0].innerHTML.trim();
        expname = expname.replace("<br>", "");

        node.attr("contenteditable", "false")
            .removeClass("exp_new_editing")
            .removeAttr("onkeypress", "")
            .removeClass("exp_preview_new")
            .attr("onclick", "goDesign('"+expname+"')");

        $("#experiments").find("#display")
            .prepend(
                "<div class='exp_preview exp_preview_new' onclick='newExp()'>"+
                  "New" +
                "</div>");

        $.post("newExp", {name: expname},
            function(data){
                console.log("newExp response")
                console.log(data)
            }
        );
    }
}

function loadControllers() {
    $(".controllers").append(
        "<div class='controller'>" +
            '<svg width="20" height="20"><polygon points="0,0 20,0 10,20" style="fill: #cc5750" ></polygon></svg>'+
            '<span class="cname">Mr. Anderson</span>'+
        '</div>')
}

function goDesign(name) {
    $(".navlink_selected").removeClass("navlink_selected");
    $("#nav_design")
        .addClass("navlink_selected")
        .html("Design - " + name);

    console.log("goDesign - " + name);
    currentExp = name;

    if(name === "") return;

    $.get("designer", {name: name},
        function(data) {
            $("#main_content").html(data);
            loadControllers();
            if(!codeWindows[name]) {
                openCodeWindow(name);
            }

            $.get("expdata", {name: name, view: "default"},
                function(data) {
                    exp_view = data;
                    //$("#design_content").html(data.toString())
                    showViz();
                    console.log(exp_view);
                    drawModel();
                }
            );

        }
    );
}

function expView() {
    $(".navlink_selected").removeClass("navlink_selected");
    $("#nav_exp").addClass("navlink_selected");

    $.get("experiments",
        function(data) {
            $("#main_content").html(data);
        }
    );
}

function updateModelData(name) {
    console.log("updating model data for "+  name);
    $.get("expdata", {name: name, view: "default"},
        function(data) {
            exp_view = data;
            console.log("model data:");
            console.log(data);
            drawModel();
            if(!codeWindows[name]) {
                openCodeWindow(name);
            }
        }
    );
}

function drawModel() {
    clearVizTree();

    if(typeof exp_view.substrates !== 'undefined') {
        for(var i=0; i<exp_view.substrates.length; i++) {
            var s = exp_view.substrates[i];
            drawSubstrate(s);
        }
    }

    if(typeof exp_view.computers !== 'undefined') {
        for (var i = 0; i < exp_view.computers.length; i++) {
            var c = exp_view.computers[i];
            drawComputer(c);
        }
    }
}

function openCodeWindow(expname) {
    var url  = "/code?"
        + "name=" + expname;

    var winFeatures =
        "menubar=no, location=no, status=no, resizable=yes," +
        "width=700, height=400";

    var win = window.open(url, expname + " :: code", winFeatures);
    win.updateModel = updateModelData;
    codeWindows[expname] = win;
    win.onbeforeunload = function() {
        delete codeWindows[expname];
    }
}
