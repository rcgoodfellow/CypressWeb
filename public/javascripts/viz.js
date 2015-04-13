/**
 * The Cypress Project
 * Created by ry on 4/9/15.
 */

var scene = {},
    camera = {},
    container = {},
    renderer = {},
    selected = {},
    raycaster = new THREE.Raycaster(),
    mouse = new THREE.Vector2(),
    baseplane = {};

var ObjKind = {
    Computer: 0,
    Interface: 1
};

var baseG = new THREE.Group(),
    compG = new THREE.Group();

function showViz() {

    container = document.getElementById("design_content");
    scene = new THREE.Scene();
    var width = container.offsetWidth,
        height = container.offsetHeight;
    camera =
        new THREE.OrthographicCamera(
            width / - 2,
            width / 2,
            height / 2,
            height / - 2,
            1,
            1000 );

    renderer = new THREE.WebGLRenderer();
    renderer.setSize(container.offsetWidth, container.offsetHeight);
    renderer.setClearColor(0x747474);
    container.appendChild(renderer.domElement);

    baseplane = new THREE.Mesh(
        new THREE.PlaneGeometry(5000, 5000),
        new THREE.LineBasicMaterial({color: 0x222222})
    );
    baseplane.position.z = -1;
    scene.add(baseplane);

    baseG.add(compG);
    scene.add(baseG);

    camera.position.z = 100;

    render();
}

function clearVizTree() {
    compG.children = [];
}

function drawComputer(c) {
    var x = new THREE.Mesh(
        new THREE.PlaneGeometry(30,30),
        new THREE.MeshBasicMaterial({color: 0x004477})
    );
    x.info = c;
    x.position.x = c.xy.x;
    x.position.y = c.xy.y;
    x.kind = ObjKind.Computer;

    for(var i=0; i<c.interfaces.length; i++) {
        var iface = c.interfaces[i];
        var y = new THREE.Mesh(
            new THREE.PlaneGeometry(7,7),
            new THREE.MeshBasicMaterial({color:0xCCCCCC})
        );
        y.position.x = 15*Math.cos(iface.r.theta);
        y.position.y = 15*Math.sin(iface.r.theta);
        y.position.z = 1;
        y.info = iface;
        y.kind = ObjKind.Interface;
        y.comp = x;
        y.clamp = function() {

            var clmp = function(x, a) {
                var v = x;
                if(x>a) v = a;
                else if(x<(-a)) v = -a;
                return v;
            };

            //this.position.x = clmp(this.position.x, 15);
            //this.position.y = clmp(this.position.y, 15);

            var arg = Math.atan2(this.position.y, this.position.x);
            if(arg > -Math.PI/4 && arg <= Math.PI/4) {
                this.position.x = 15;
                this.position.y = clmp(this.position.y, 15);
            }
            else if(arg > Math.PI/4 && arg <= 3*Math.PI/4) {
                this.position.y = 15;
                this.position.x = clmp(this.position.x, 15);
            }
            else if(arg > 3*Math.PI/4 && arg <= 5*Math.PI/4) {
                this.position.x = -15;
                this.position.y = clmp(this.position.y, 15);
            }
            else {
                this.position.y = -15;
                this.position.x = clmp(this.position.x, 15);
            }

        };
        x.add(y);
    }

    compG.add(x);
    render();
}

function render() {
    renderer.render(scene, camera);
}

function updateCoords(event) {
    mouse.x = (event.layerX / container.offsetWidth) * 2 - 1;
    mouse.y = -(event.layerY / container.offsetHeight) * 2 + 1;
}

function updateObjectXY(type, name, x, y) {

    $.post("updateXY", {type: type, name: name, x: x, y: y, exp: expname})

}

function viz_mousedown(event) {

    updateCoords(event);

    raycaster.setFromCamera(mouse, camera);
    var ixs = raycaster.intersectObjects(compG.children, true);

    if(ixs.length > 0) {
        selected = ixs[0];
        if(selected.object === baseplane) return;
        console.log(selected.object.info.name);
        console.log(selected);

        var off = {x: 0, y:0};
        if(selected.object.kind === ObjKind.Interface) {
            off.x = selected.object.comp.position.x;
            off.y = selected.object.comp.position.y;
        }


        container.onmouseup = function(upE) {
            updateCoords(upE);
            raycaster.setFromCamera(mouse, camera);
            var ixs = raycaster.intersectObject(baseplane);

            updateObjectXY("computer", selected.object.info.name,
                ixs[0].point.x, ixs[0].point.y);

            container.onmousemove = null;
            container.onmouseup = null;
        };

        container.onmousemove = function(moveE) {
            updateCoords(moveE);
            raycaster.setFromCamera(mouse, camera);
            var ixs = raycaster.intersectObject(baseplane);


            if(ixs.length > 0) {
                var ix = ixs[0];
                selected.object.position.x = ix.point.x - off.x;
                selected.object.position.y = ix.point.y - off.y;
                if(selected.object.clamp) selected.object.clamp();
                render();
            }
        }
    }

}
