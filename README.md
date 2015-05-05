# Cosmetic
Android 换肤解决方案

用最少的代码改动量在已有的项目中加入换肤功能。

  下载最新的apktool:http://code.google.com/p/android-apktool/wiki/Install

	1.用apktool把apk反编译出资源文件（apktool d <apk路径>）
	反编译文件在apktool所在目录
	
	2.把新皮肤所需要替换的资源替换。

	3.在assets文件中使用下面文本替换：
	#文件编码格式为UTF-8
	#皮肤包版本 数据类型int 根据实际目标版本修改，
	#与反编译应用中的Manifest中versionCode保持一致
	targetVersion=1
	
	4.可在assets中增加皮肤包的icon，此项可选。

	#第5步骤可以舍弃，验证id已无需public.xml
	#5.\res\values下的public.xml复制到assets中，
	#在打包成皮肤包后可用于与当前项目R.java验证id的匹配，
	#使用提供的验证工具验证
	
	6.建议把资源里的smali文件（代码文件）删除。
	
	7.再用apktool重新打包成apk（apktool b <反编译资源路劲>）。

  无需签名，新打包的apk根据要求修改名称和后缀就可以用来换肤使用了。

# Tools

新增皮肤包效验工具，用于确定新皮肤包与主程序apk包中R文件所有id可以一一对应。

Cmd运行编译好的Cusmetictool.jar或双击使用Cusmetictool.bat脚本（Cusmetictool.jar与脚本同一目录）

此工具提供两种比对ids的方法，一个是R.java文件和皮肤包比对，一个是apk与皮肤包比对

可在控制台里使用Cusmetictool命令查看使用方法
