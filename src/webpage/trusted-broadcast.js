(() => {
    let msg = document.getElementById('msg');
    let btn = document.getElementById("broadcast");
    btn.addEventListener('click', () => {
        let txt = msg.value
        let req = new XMLHttpRequest()
        req.open('GET', '/trusted-broadcast?msg=' + encodeURIComponent(txt))
        req.send();
    });
})();
