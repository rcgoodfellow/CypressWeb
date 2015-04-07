/**
 * The Cypress Project
 * Created by ry on 4/6/15.
 */

var currentExp = "";

var codeWindows = {};

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
                   "New"+
                "</div>"
        )

        $.post("newExp", {name: expname},
            function(data){
                console.log("newExp response - " + data)
            }
        );
    }
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
            if(!codeWindows[name]) {
                openCodeWindow(name);
            }
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

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i=0; i<ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1);
        if (c.indexOf(name) == 0) return c.substring(name.length,c.length);
    }
    return "";
}

function openCodeWindow(expname) {
    var url  = "/code?"
        + "name=" + expname;

    var winFeatures =
        "menubar=no, location=no, status=no, resizable=yes," +
        "width=600, height=900";

    var win = window.open(url, expname + " :: code", winFeatures);
    codeWindows[expname] = win;
    win.onbeforeunload = function() {
        delete codeWindows[expname];
    }
}
