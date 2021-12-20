# Gradle XQHook 插件Demo

## 本例通过Gradle Transform API + Javassists实现动态注入或修改代码。
#### 优点:

1. 集成简单.
2. 功能强大。支持正则匹配 + 插入代码(执行前后) + 替换方法执行 + 修改参数.

#### 集成步骤(仓库配置自定义):
#### 1).根目录引入插件仓库配置 build.gradle
```groovy
allprojects {
    repositories {
        //
        maven { // 引入依赖仓库
            url file("$rootDir/../repos")
        }
    }
}
```
#### 2).在application module引入插件即可(可动态修改依赖库代码)。
##### app下build.gradle 文件
```groovy
plugins {
    id "xqhook"
}
```

#### 3).在build.gradle中配置hook点
```groovy
/** 配置hook点 */
hookConfig {
    // TODO 添加局部module配置(当前module下处理). addHooker(regex, )
    addHooker("*.MainActivity.test\\(*\\)*") {
        // 替换方法执行的第一个参数
        it.replaceParameter(1, "\"replace Parameter from \" + ${it.paramInStatement(1)} ;")
    }
    addHooker("*.MainActivity.test3\\(*\\)*") {
        // 替换方法执行并采用替换后脚本作为返回值
        it.replaceWithReturn("System.out.println(\"Test3 body\");")
    }
    // TODO 添加全局module插件处理(这里是将android系统日志打印API替换)
    addGlobalHooker("android.util.Log.*\\(*\\)") {
        // 替换系统日志打印api, 为当前api.
        it.replace("xmq.track.base.MockLog.d(\$\$);")
        // 在tag前追加打印所在类. 如: MainActivity-TAG
        it.replaceParameter(1, "getClass().getSimpleName()+ \"-\" + ${it.paramInStatement(1)};")
        // 在message前追加方法调用的类文件和行号. 如: (MainActivity.java:24)
        it.replaceParameter(2, "\"(${it.invokeFileName()}:${it.invokeLineNo()}) \" + ${it.paramInStatement(2)};")
    }
    // TODO 下面例子演示,根据参数信息. 动态插入.
    addGlobalHooker("xmq.track.base.MockLog.*\\(*\\)V") {
//        Logger.i("hook:::: "+Arrays.toString(it.params()))
        // 这里识别无tag情况。（只有一个message || 第二个参数就不是String)
        if (it.params().length == 1 || it.params()[1] != String.name) {
            // 增加第一个参数tag为获取类名称
            it.replaceWithReturn("xmq.track.base.MockLog.${it.methodName()}(getClass().getSimpleName(), \$\$);")
            // message添加代码执行行信息
            it.replaceParameter(1, "\"(${it.invokeFileName()}:${it.invokeLineNo()}) \" + ${it.paramInStatement(1)};")
        } else {
            // 第一个参数为tag, 这里添加获取类名称
            it.replaceParameter(1, "getClass().getSimpleName() + \"-\" + ${it.paramInStatement(1)};")
            // message添加代码执行行信息
            it.replaceParameter(2, "\"(${it.invokeFileName()}:${it.invokeLineNo()}) \" + ${it.paramInStatement(2)};")
        }
    }
}
```



