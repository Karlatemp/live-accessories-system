# Live Accessories System

只是个供个人使用的直播配件系统

--------


## 启动

运行 `src/main/kotlin/Exchange.kt`

## 文件路径

> - `./src/webpriv` 私人资源
> - `./src/test` 一些工具(玩具)

> `http://localhost:12421/static/exchange.html`
>
> 弹幕显示页, 塞进 OBS Studio 即可


> `http://localhost:12421/static/trusted-broadcast.html`
>
> 广播一条信息到所有渲染页 (DEBUG)

> `http://localhost:12421/trusted-broadcast?msg=MSG`
>
> 广播一条信息到所有渲染页

> `http://localhost:12421/static/*` -> `./src/webpage`

> `http://localhost:12421/webpriv/*` -> `./src/webpriv`

## 信息协议

```json5
{
  "type": "push",
  "name": "发送者名字",
  "msg": "发了一条弹幕",
  // 渲染的扩展类名, 可选
  "className": "sc sc-1",
}
```

