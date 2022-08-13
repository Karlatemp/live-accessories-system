(function () {
    let bdy = document.getElementById("maindisplay");

    let msgs = [];

    function pushMsg(title, msg, options) {
        let comp = document.createElement('div');

        let hd = comp.appendChild(document.createElement('div'));
        hd.className = 'hd';
        hd.textContent = title;

        let mg = comp.appendChild(document.createElement('div'));
        mg.className = 'mg';
        mg.textContent = msg;

        // comp.style.transform = 'translateX(-100%)';
        comp.style.opacity = '0';

        if (options && options.className) {
            comp.className = 'danmuku ' + options.className;
        } else {
            comp.className = 'danmuku';
        }

        bdy.appendChild(comp);
        comp.scrollIntoView({
            behavior: 'smooth'
        })

        setTimeout(() => {
            // comp.style.transform = 'none';
            comp.style.opacity = '1';
        });
        msgs.push(comp);
    }

    setInterval(() => {
        if (msgs.length > 30) {
            msgs.shift().remove();
        }
    }, 1000);


    let evtTarget = new EventTarget();

    evtTarget.addEventListener('push', (evt) => {
        // console.log(evt.detail);
        pushMsg(evt.detail.name, evt.detail.msg, evt.detail);
    })

    /**
     * @type {WebSocket}
     */
    let connection;
    let reconnecting = true;

    /**
     * (this: WebSocket, ev: MessageEvent) => any
     * @param evt {MessageEvent}
     */
    function onWSMsg(evt) {
        let data = evt.data;
        // console.log(data, data instanceof String, typeof data)
        if (typeof data == 'string') {
            let jsonx = JSON.parse(data)
            console.log(jsonx)
            evtTarget.dispatchEvent(new CustomEvent(jsonx.type, {detail: jsonx}));
        }
    }

    function onwsready() {
        pushMsg("System", "Reconnected Danmu System");
        reconnecting = false;
    }

    function ondisconnect() {
        setTimeout(reconnect, 1000);
    }

    function reconnect() {
        if (!reconnecting) {
            pushMsg("System", "Reconnecting.......");
        }
        reconnecting = true;
        try {
            connection = new WebSocket("ws://" + location.host + '/msgc');
            connection.onopen = onwsready;
            connection.onclose = ondisconnect;
            connection.onmessage = onWSMsg;
        } catch (e) {
            console.log(e);
            ondisconnect()
        }
    }

    reconnect();
    // let connection = new WebSocket("ws://" + location.host + '/asdawef');

    // region Timer Clock
    (() => {
        let clock = document.getElementById('timer-clock');
        let settings = dnsettings['timer-clock'];
        if (!settings.enabled) {
            clock.remove();
            return
        }
        clock.parentElement.style.background = settings.bg;

        function padding(v) {
            v = String(v);
            if (v.length == 1) return '0' + v;
            return v;
        }

        setInterval(() => {
            let date = new Date();
            clock.textContent =
                padding(date.getHours()) + ":" +
                padding(date.getMinutes()) + ":" +
                padding(date.getSeconds());
        }, 500);
    })();
    // endregion
})();