package xmq.hook.gradle.util;

import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Created by hp on 2016/4/13.
 */
class JarZipUtil {

    /**
     * 将该jar包解压到指定目录
     * @param jarPath jar包的绝对路径
     * @param destDirPath jar包解压后的保存路径
     * @return 返回该jar包中包含的所有class的完整类名类名集合，其中一条数据如：com.aitski.hotpatch.Xxxx.class
     */
    static List unzipJar(String jarPath, String destDirPath) {
        def list = new ArrayList()
        if (jarPath.endsWith('.jar')) {
            def jarFile = new JarFile(jarPath)
            def jarEntrys = jarFile.entries()
            while (jarEntrys.hasMoreElements()) {
                def jarEntry = jarEntrys.nextElement()
                if (jarEntry.directory) {
                    continue
                }
                def entryName = jarEntry.getName()
                if (entryName.endsWith('.class')) {
                    def className = entryName.replace('\\', '.').replace('/', '.')
                    list.add(className)
                }
                def outFile = new File(destDirPath + "/" + entryName)
                outFile.getParentFile().mkdirs()
                def fileOutputStream = null
                def inputStream = null
                try {
                    inputStream = jarFile.getInputStream(jarEntry)
                    fileOutputStream = new FileOutputStream(outFile)
                    fileOutputStream << inputStream
                    fileOutputStream.close()
                    inputStream.close()
                } finally {
                    if (null != inputStream) {
                        inputStream.close()
                    }
                    if (null != fileOutputStream) {
                        fileOutputStream.close()
                    }
                }
            }
            jarFile.close()
        }
        return list
    }

    /**
     * 重新打包jar
     * @param packagePath 将这个目录下的所有文件打包成jar
     * @param destPath 打包好的jar包的绝对路径
     */
    static void zipJar(String packagePath, String destPath) {
        def outputStream = null
        try {
            outputStream = new JarOutputStream(new FileOutputStream(destPath))
            new File(packagePath).eachFileRecurse { File f ->
                def entryName = f.getAbsolutePath().substring(packagePath.length() + 1).replaceAll("\\\\", "/")
                if (!f.directory) {
                    outputStream.putNextEntry(new ZipEntry(entryName))
                    def inputStream = null
                    try {
                        inputStream = new FileInputStream(f)
                        outputStream << inputStream
                    } finally {
                        if (null != inputStream) {
                            inputStream.close()
                        }
                    }
                }
            }
        } finally {
            if (null != outputStream) {
                outputStream.close()
            }
        }
    }
}