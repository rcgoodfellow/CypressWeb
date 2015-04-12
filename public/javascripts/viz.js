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

var baseG = new THREE.Group(),
    compG = new THREE.Group();

function showViz() {

    container = document.getElementById("design_content");
    scene = new THREE.Scene();
    camera =
        new THREE.PerspectiveCamera(
            75,
            container.offsetWidth / container.offsetHeight,
            0.1,
            1000
        );

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

    /*
    var box = new THREE.Mesh(
        new THREE.PlaneGeometry(10, 10),
        new THREE.MeshBasicMaterial({color: 0x004477})
    );
    scene.add(box);
    */

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
        new THREE.PlaneGeometry(10,10),
        new THREE.MeshBasicMaterial({color: 0x004477})
    );
    x.position.x = c.xy.x;
    x.position.y = c.xy.y;
    x.info = c;
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
    var ixs = raycaster.intersectObjects(compG.children);

    if(ixs.length > 0) {
        selected = ixs[0];
        if(selected.object === baseplane) return;
        console.log(selected.object.info.name);
        console.log(selected);

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
                selected.object.position.x = ix.point.x;
                selected.object.position.y = ix.point.y;
                render();
            }
        }
    }

}
