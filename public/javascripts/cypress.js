/**
 * The Cypress Project
 * Created by ry on 4/6/15.
 */

function newExp() {

    console.log("new experiment");
    $(".exp_preview_new")
        .attr("contenteditable", "true")
        .addClass("exp_new_editing")
        .attr("onkeypress", "newExpKeyPrH(event)");
}

function newExpKeyPrH(evt) {
    var x = evt.which || evt.keyCode;
    if(x == 13) {
        $(".exp_preview_new")
            .attr("contenteditable", "false")
            .removeClass("exp_new_editing")
            .removeAttr("onkeypress", "")
            .removeClass("exp_preview_new");

        //$("#new_text").removeAttr("id");

        $("#experiments").find("#display")
            .prepend(
                "<div class='exp_preview exp_preview_new' onclick='newExp()'>"+
                    "<span id='new_text' contenteditable='inherit'>New</span>"+
                "</div>"
        )
    }
}
