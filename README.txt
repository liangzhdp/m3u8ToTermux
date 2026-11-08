构建：推送到 GitHub，Actions 自动编译；或用 Android Studio 打开后 Build APK。

功能
- 注册为 http/https 链接处理者（Linksheet 才会把它当浏览器列出）。
- 链接是音频（m3u8/m3u/m4a/mp3/aac/flac/ogg/opus/wav/pls）-> 交给 Termux 的 mpv 只播声音。
- 其他链接 -> 自动转给 Chrome/Brave/Firefox 等浏览器（MainActivity.BROWSERS 可改）。
- 通知栏有「暂停/继续」「停止」按钮；播完或停止后通知自动消失。
- 再点新链接会先停掉旧的 mpv。

Termux 里（一次性）
  mkdir -p ~/.termux
  echo "allow-external-apps=true" >> ~/.termux/termux.properties
  termux-reload-settings        # 然后完全关闭 Termux 再重开
  pkg install mpv procps        # procps 提供 pkill / pgrep

权限
  首次点链接会弹出：在 Termux 中执行命令、发送通知。
  如果灰掉：应用信息页右上角 ⋮ > 允许受限制的设置。
