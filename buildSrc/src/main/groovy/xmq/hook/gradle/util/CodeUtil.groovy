package xmq.hook.gradle.util

import javassist.CtMethod

/**
 * @author xmqyeah
 * @CreateDate 2021/12/17 22:55
 */
 class CodeUtil {
    static boolean isReturnVoid(CtMethod method){
        return isReturnVoid(method.returnType.name)
    }
     static boolean isReturnVoid(String classPkgName){
         return classPkgName == "void"
     }
     static String getReturnValue(CtMethod method){
         return getReturnValue(method.returnType.name)
     }
    static String getReturnValue(String classPkgName) {
        String className = classPkgName.replace("/", ".")
        switch (className) {
            case String.name:
                return "\"\""
            case Long.name:
            case "long":
                return "0L"
            case Character.name:
            case "char":
                return "\' \'"
            case Double.name:
            case "double":
            case Float.name:
            case "float":
                return "0.0"
            case Boolean.name:
            case "boolean":
                return "false"
            case Byte.name:
            case "byte":
            case Integer.name:
            case "int":
                return "0"
        }
        return "null"
    }
}
