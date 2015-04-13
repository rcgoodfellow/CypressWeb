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
    Interface: 1,
    Actuator: 2,
    Sensor: 3,
    Substrate: 4
};

var baseG = new THREE.Group(),
    compG = new THREE.Group(),
    subsG = new THREE.Group(),
    cnxG = new THREE.Group();

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

    renderer = new THREE.WebGLRenderer({antialiasing: true});
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
    baseG.add(subsG);
    baseG.add(cnxG);
    scene.add(baseG);

    camera.position.z = 100;

    render();
}

function clearVizTree() {
    compG.children = [];
    subsG.children = [];
}

function getPath(n) {
    if(typeof n.kind === 'undefined') return [];

    var p = [{kind: n.kind, name: n.info.name}];
    if(typeof n.parent !== 'undefined' &&
       typeof n.parent.kind !== 'undefined') {
        p = getPath(n.parent).concat(p)
    }
    return p;
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
        y.position.x = iface.xy.x;
        y.position.y = iface.xy.y;
        y.position.z = 1;
        y.info = iface;
        y.kind = ObjKind.Interface;
        y.clamp = function() {

            var clmp = function(x, a) {
                var v = x;
                if(x>a) v = a;
                else if(x<(-a)) v = -a;
                return v;
            };

            var arg = Math.atan2(this.position.y, this.position.x);
            if (arg < 0) arg += 2*Math.PI;

            if((arg >= 0 && arg <= Math.PI/4) || (arg >= 7*Math.PI/4)) {
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

        y.terms = [];
        y.updateTerms = function() {
            for(var i=0; i<this.terms.length; i++) {
                scene.updateMatrixWorld();
                this.parent.updateMatrixWorld();
                this.terms[i].v.setFromMatrixPosition(this.matrixWorld);
                this.terms[i].v.z = 0;
                this.terms[i].g.verticesNeedUpdate = true;
                render();
            }

        };

        x.add(y);
    }

    compG.add(x);
    render();

    for(var i=0; i<c.interfaces.length; i++) {

        var iface = c.interfaces[i];

        for(var j=0; j<iface.substrates.length; j++) {
            var sname = iface.substrates[i];
            var s = findSubstrate(sname);
            addConnection(y, s);
        }

    }
}

function findSubstrate(name) {

    for(var i=0; i<subsG.children.length; i++) {
        var s = subsG.children[i];
        if(s.info.name === name) return s
    }
    return null;
}

function addConnection(a, b) {

    scene.updateMatrixWorld();

    var geom = new THREE.Geometry();
    geom.dynamic = true;

    var ac = new THREE.Vector3();
    a.terms.push({g: geom, v: ac});
    a.updateTerms();

    /*
    if(typeof a.parent !== 'undefined') {
        a.parent.updateMatrixWorld();
    }
    ac.setFromMatrixPosition(a.matrixWorld);
    */

    var bc = new THREE.Vector3();
    b.terms.push({g: geom, v: bc});
    b.updateTerms();

    /*
    if(typeof b.parent !== 'undefined') {
        b.parent.updateMatrixWorld();
    }
    bc.setFromMatrixPosition(b.matrixWorld);
    bc.z = 0;
    */

    geom.vertices.push(ac, bc);

    var line = new THREE.Line(
        geom,
        new THREE.LineBasicMaterial({color: 0xDDDDDD})
    );

    cnxG.add(line);

    render();

}

function drawSubstrate(s) {
    var radius = 20,
        segments = 64,
        color = 0xCCCCCC;

    var x = new THREE.Mesh(
        new THREE.CircleGeometry(radius, segments),
        new THREE.MeshBasicMaterial({color: color})
    );

    x.position.x = s.xy.x;
    x.position.y = s.xy.y;
    x.position.z = 1;
    x.info = s;
    x.kind = ObjKind.Substrate;
    x.terms = [];

    x.updateTerms = function() {
        for(var i=0; i<this.terms.length; i++) {
            scene.updateMatrixWorld();
            this.terms[i].v.setFromMatrixPosition(this.matrixWorld);
            this.terms[i].v.z = 0;
            this.terms[i].g.verticesNeedUpdate = true;
        }
        render();
    };

    subsG.add(x);
    render();
}

function render() {
    renderer.render(scene, camera);
}

function updateCoords(event) {
    mouse.x = (event.layerX / container.offsetWidth) * 2 - 1;
    mouse.y = -(event.layerY / container.offsetHeight) * 2 + 1;
}

function updateObjectXY(path, x, y) {

    $.ajax({
        type: "POST",
        url: "/updateXY",
        data: JSON.stringify({path: path, x: x, y: y, exp: expname}),
        contentType: "application/json",
        dataType: "json"
    });

}

function obj_updateTerms(o) {
    if(typeof o.updateTerms !== 'undefined') {
        o.updateTerms()
    }

    if(typeof o.children !== 'undefined') {
        if(typeof o.children.length !== 'undefined') {
            for(var i=0; i<o.children.length; i++) {
                obj_updateTerms(o.children[i])
            }
        }
    }

}

function viz_mousedown(event) {

    updateCoords(event);

    raycaster.setFromCamera(mouse, camera);
    var ixs = raycaster.intersectObjects(baseG.children, true);

    if(ixs.length > 0) {
        selected = ixs[0];
        if(selected.object === baseplane) return;
        //console.log(selected.object.info.name);
        //console.log(selected);

        var off = {x: 0, y:0};
        if(selected.object.kind === ObjKind.Interface) {
            off.x = selected.object.parent.position.x;
            off.y = selected.object.parent.position.y;
        }

        container.onmouseup = function() {

            var obj = selected.object;
            var pth = getPath(obj);
            updateObjectXY(pth, obj.position.x, obj.position.y);

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
                /*
                if(typeof selected.object.updateTerms !== 'undefined') {
                    selected.object.updateTerms();
                }
                */
                obj_updateTerms(selected.object);
                render();
            }
        }
    }

}
