/**
 * The Cypress Project
 * Created by ry on 4/7/15.
 */

var editor = {};
var out_count = 0;

function code_onload() {

    var code = document.getElementById("code");
    CodeMirror.commands.save = mirror_eval;

    editor =
        CodeMirror(code, {
            value: '//experiment code goes here',
            mode: "text/x-scala",
            matchBrackets: true,
            lineNumbers: true,
            theme: "lesser-dark",
            vimMode: true
        });

}

function mirror_eval() {

    console.log("Code Push!");

    var source = editor.getValue();

    $.post("eval", {source: source, exp: expname},
        function(data) {
            var cs = $("#console");
            cs.append(
                "<div class='codeout'>"+
                    "<table><tr>"+
                        "<td><div class='ccl'>[" + out_count++ + "]:</div></td>"+
                        "<td><div class='cc'>"+data+"</div></td>" +
                    "</tr></table>"+
                "</div>");
            cs.scrollTop(cs.prop("scrollHeight"));
            console.log(data);
            //window.updateModel(expname);
        }
    );

}
