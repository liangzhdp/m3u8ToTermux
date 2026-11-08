构建：用 Android Studio 打开本文件夹 -> Build > Build APK(s)，安装 app-debug.apk。

一、Termux 里（只需一次）
  mkdir -p ~/.termux
  echo "allow-external-apps=true" >> ~/.termux/termux.properties
  termux-reload-settings
  （然后完全关闭 Termux 再重开）

二、本 App 权限
  设置 > 应用 > mpv 音频播放 > 权限 > 「其他权限」里允许
  「在 Termux 环境中运行命令」(Run commands in Termux environment)。
  侧载 App 若灰掉：应用信息页右上角 ⋮ > 允许受限制的设置。

三、Linksheet
  拦截 m3u8 链接后，在选择列表里选「mpv 音频播放」。

停止播放：在 Termux 执行  pkill mpv
