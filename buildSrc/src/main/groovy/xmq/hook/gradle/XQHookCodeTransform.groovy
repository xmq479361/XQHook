package xmq.hook.gradle

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import xmq.hook.gradle.core.HookManager

//import xmq.hook.gradle.core.HookManager
import xmq.hook.gradle.core.HookerImpl
import xmq.hook.gradle.ext.HookExtension
import xmq.hook.gradle.util.CodeUtil
import xmq.hook.gradle.util.JarZipUtil
import javassist.*
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import xmq.hook.gradle.util.Logger

/**
 * Hook代码transform
 * @author xmqyeah*
 * @CreateDate 2021/11/28 15:14
 */
class XQHookCodeTransform extends Transform {

    @Override
    String getName() {
        return "xqhook"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getReferencedScopes() {
//        return super.getReferencedScopes()
        Set<QualifiedContent.Scope> sets = new HashSet<QualifiedContent.Scope>()
        sets.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES)
//        sets.add(QualifiedContent.Scope.PROVIDED_ONLY)
        return sets
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        try {
            Logger.e("CodeHook transform>>>> : " + getName())
            transformInvocation.outputProvider.deleteAll()
            def mClassPool = new ClassPool(ClassPool.getDefault());
            // 添加android.jar目录
            mClassPool.appendClassPath(AndroidJarPath())
            def dirMap = new HashMap<String, String>()
            def jarMap = new HashMap<String, String>()
            transformInvocation.inputs.each { input ->
                input.directoryInputs.each { dirInput ->
                    def destDir = transformInvocation.outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes,
                            Format.DIRECTORY)
                    dirMap.put(dirInput.getFile().getAbsolutePath(), destDir.getAbsolutePath());
                    mClassPool.appendClassPath(dirInput.getFile().getAbsolutePath());
                }
                input.jarInputs.each { jarInput ->
                    // 重命名输出文件
                    def jarName = jarInput.getName();
                    def md5Name = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
                    if (jarName.endsWith(".jar")) {
                        jarName = jarName.substring(0, jarName.length() - 4);
                    }
                    //生成输出路径
                    def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name,
                            jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                    jarMap.put(jarInput.getFile().getAbsolutePath(), dest.getAbsolutePath());
                    mClassPool.appendClassPath(new JarClassPath(jarInput.getFile().getAbsolutePath()))
                }
            }
            for (Map.Entry<String, String> item : dirMap.entrySet()) {
                Logger.d("perform_directory : " + item.getKey());
                injectDir(item.getKey(), item.getValue(), mClassPool);
            }
            for (Map.Entry<String, String> item : jarMap.entrySet()) {
                Logger.d("perform_jar : " + item.getKey());
                injectJar(item.getKey(), item.getValue(), mClassPool);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Logger.e("CodeHook completed!...");

    }

    void injectDir(String sourcePath, String destPath, ClassPool pool) {
        def srcDir = new File(sourcePath)
        def destDir = new File(destPath)
        injectDir(sourcePath, srcDir, destDir, pool)
    }

    void injectDir(String basePath, File srcDir, File destDir, ClassPool pool) {
        srcDir.listFiles().findAll { file ->
            if (file != null && file.isDirectory()) {
                true
            } else file.name.endsWith(".class")
        }.each {
//            def destFile = new File(destDir, it.name)
            if (it.isDirectory()) {
                injectDir(basePath, it, destDir, pool)
            } else {
                injectSingleFile(basePath, it, destDir, pool)
            }
        }
    }

    void injectSingleFile(String basePath, File sourceFile, File destFile, ClassPool pool) {
        def classFullName = sourceFile.path.replace(basePath + "\\", "")
                .replace(".class", "")
                .replace(".kt", "")
                .replaceAll("/", ".").replaceAll("\\\\", ".")

        if (classFullName.endsWith(".BuildConfig") || sourceFile.name.startsWith("R\$") || classFullName == "xmq.track.base.MockLog") {
            Logger.d("injectSingleFile not($classFullName): ${sourceFile.path} => ${destFile.path}")
            return
        }
        def classCt = pool.get(classFullName)
        Logger.d("injectSingleFile($classFullName): ${sourceFile.path} => ${destFile.path}")
        modify(HookExtension.hookers, classCt, pool)
        classCt.writeFile(destFile.path)
        classCt.detach()
    }

    static void injectJar(String sourcePath, String destPath, ClassPool pool) {
        def index = sourcePath.indexOf("\\build\\")
        def srcJar = new File(sourcePath)
        pool.appendClassPath(sourcePath)
        File jarFile = new File(destPath)
        if (index == -1){ // 这里暂不处理外部依赖库。
            FileUtils.copyFile(srcJar, jarFile)
        } else if (sourcePath.endsWith(".jar")) {
            // jar包解压后的保存路径
            def jarZipDir = jarFile.parent + "/" + jarFile.name.replace('.jar', '')
            Logger.d("injectJar($index): ${sourcePath} => ${destPath}")
            // 解压jar包, 返回jar包中所有class的完整类名的集合（带.class后缀）
            def classNameList = JarZipUtil.unzipJar(sourcePath, jarZipDir)
            // 删除原来的jar包
            jarFile.delete()
            for (String className : classNameList) {
//                Logger.d("injectJar className($className)")
                if (className.endsWith(".class")
                        && !className.contains('R$')
                        && !className.contains('R.class')
                        && !className.contains("BuildConfig.class")
                        && className != "xmq.track.base.MockLog.class") {
                    className = className.substring(0, className.length() - 6)


                    def classCt = pool.get(className)
                    Logger.d("injectJar clz($className)")
                    modify(HookExtension.globalHookers, classCt, pool)
                    classCt.writeFile(jarZipDir)
                    classCt.detach()
                }
            }
            // 从新打包jar
            JarZipUtil.zipJar(jarZipDir, destPath)
            // 删除目录
            FileUtils.deleteDirectory(new File(jarZipDir))
        }
    }

    private static void modify(Set<HookerImpl> hookers, CtClass c, ClassPool mClassPool) {
        if (c.isFrozen()) {
            c.defrost()
        }
        Logger.d("find class[${c.name}===hooks: ${hookers.size()}")
        def methods = c.getDeclaredMethods()
        HashMap<String, String> keyPairs = new HashMap<String, String>()
        for (def method : methods) {
            keyPairs.put("\\\$\\{invokeClz}", c.name)
            keyPairs.put("\\\$\\{invokeClzSimpleName}", c.simpleName)
            keyPairs.put("\\\$\\{invokeMethodName}", method.name)
            Logger.d("\tmethod: ${method.name}, ${method.longName}, ${method.getDeclaringClass().getName()}")
            method.instrument(new ExprEditor() {
                void edit(MethodCall m) throws CannotCompileException {
                    Logger.d("\t\tedit: ${m.className}#${m.methodName}, ${m.signature}, ${m.fileName}:${m.lineNumber}," +
                            "${m.metaClass}, ${m.method.modifiers} ")
                    HookManager.execute(hookers, m, keyPairs)
                }
            })
        }
    }

    static String AndroidJarPath() {
        return new File("D:\\dev\\Android\\sdk\\platforms\\android-30\\android.jar").getPath()
    }
}
