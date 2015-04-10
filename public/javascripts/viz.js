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

    camera.position.z = 100;

    render();
}

function render() {
    renderer.render(scene, camera);
}

function updateCoords(event) {
    mouse.x = (event.layerX / container.offsetWidth) * 2 - 1;
    mouse.y = -(event.layerY / container.offsetHeight) * 2 + 1;
}


function viz_mousedown(event) {

    updateCoords(event);

    raycaster.setFromCamera(mouse, camera);
    var ixs = raycaster.intersectObjects(scene.children);

    if(ixs.length > 0) {
        selected = ixs[0];
        if(selected.object === baseplane) return;
        console.log(selected);

        container.onmousemove = function(moveE) {

            container.onmouseup = function() {
                container.onmousemove = null;
                container.onmouseup = null;
            };

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
