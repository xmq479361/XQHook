package xmq.hook.gradle.core

import xmq.hook.gradle.util.CodeUtil
import javassist.CannotCompileException
import javassist.expr.MethodCall
import xmq.hook.gradle.util.Logger
import xmq.hooks.IMethodCallInfo
import xmq.hooks.IMethodInvoker

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author xmqyeah* @CreateDate 2021/12/12 20:45
 */
class HookManager {

    static void execute(Set<HookerImpl> hookers, MethodCall call, HashMap<String, String> keyPairs) {
        if (null == call) return
        def hookEntry = new HookEntry(keyPairs, call)
        def statement = call.className + "." + call.methodName + call.signature
        Logger.i("\t\t==check: " + statement)
        hookers.each { hooker ->
//            Logger.i("--: " + call.method)
            if (hooker.match(statement)) {
                Logger.e("\t\t\t==execute: " + hooker.regex + " = " + statement)
                hooker.methodCall.execute(hookEntry)
//            } else {
//                Logger.i("\t\t\t----not match: " + hooker.regex + " = " + statement)
            }
        }
        hookEntry.executed()
    }

    static class HookEntry implements IMethodInvoker {
        void executed() {
            if (!hasConfiguration) return
            StringBuffer fullStatement = new StringBuffer()
            if (beforeStatement.length() > 0) {
                fullStatement.append(beforeStatement)
            }
            if (null != sourceExecStatement) { // 插入源代码执行部分
                fullStatement.append(sourceExecStatement)
            }
            if (afterStatement.length() > 0) {
                fullStatement.append(afterStatement)
            }
            replaceFor("{ " + fullStatement+" }")
        }
        StringBuffer beforeStatement = new StringBuffer()
        StringBuffer afterStatement = new StringBuffer()
        String sourceExecStatement
        boolean hasConfiguration = false
        MethodCall call
        HashMap<String, String> keyPairs
        HookEntry(HashMap<String, String> keyPairs, MethodCall call) {
            this.call = call
            if (CodeUtil.isReturnVoid(call.method)) {
                sourceExecStatement = "\$proceed(\$\$);"
            } else {
                sourceExecStatement = "\$_ = \$proceed(\$\$);"
            }
            keyPairs.put("\\\$\\{fileName}", call.fileName)
            keyPairs.put("\\\$\\{line}", call.lineNumber.toString())
            keyPairs.put("\\\$\\{className}", call.className)
            keyPairs.put("\\\$\\{methodName}", call.methodName)
            this.keyPairs = keyPairs
        }

        @Override
        String returnType() {
            call.method.returnType.name
        }

        @Override
        String[] params(){
            String[] params = new String[call.method.parameterTypes.length]
            call.method.parameterTypes.eachWithIndex {ct, index->
                params[index] = ct.name
            }
            return params
        }

        String getPair(String key) {
            def keys = "\\\$\\{"+key+"}"
            return keyPairs.get(keys)
        }

        @Override
        String methodName() {
            return getPair("methodName")
        }

        @Override
        String className() {
            return getPair("className")
        }

        @Override
        String classSimpleName() {
            className().substring(className().lastIndexOf("."))
        }

        @Override
        int invokeLineNo() {
            return getPair("line").toInteger()
        }

        @Override
        String invokeFileName() {
            return getPair("fileName")
        }

        @Override
        String invokeClzName() {
            return getPair("invokeClz")
        }

        @Override
        String invokeClzSimpleName() {
            return getPair("invokeClzSimpleName")
        }

        @Override
        String invokeMethodName() {
            return getPair("invokeMethodName")
        }

        @Override
        IMethodCallInfo callInfo() {
            return this
        }

        @Override
        String paramInStatement(int index){
            return "\$$index"
        }
        @Override
        void insertBefore(String statement) {
            Logger.d("insertBefore: " + statement)
            beforeStatement.append(statement)
            hasConfiguration = true
        }

        @Override
        void insertAfter(String statement) {
            Logger.d("insertAfter: " + statement)
            afterStatement.append(statement)
            hasConfiguration = true
        }

        @Override
        void replace(String statement) {
            Logger.d("replace: " + statement+" ,"+call.method.returnType.name+", "+CodeUtil.getReturnValue(call.method))
            if (!CodeUtil.isReturnVoid(call.method)) {
                sourceExecStatement = "\$_ = ${CodeUtil.getReturnValue(call.method)};"// 给定默认返回值
            }
            afterStatement.insert(0,  statement)
            hasConfiguration = true
        }

        @Override
        void replaceWithReturn(String statement) {
            Logger.d("replaceWithReturn: " + statement+" ,"+call.method.returnType.name+", "+CodeUtil.getReturnValue(call.method))
            if (CodeUtil.isReturnVoid(call.method)) {
                beforeStatement.insert(0, statement)
                sourceExecStatement = null
            } else {
                sourceExecStatement = "\$_ = $statement "
            }
            hasConfiguration = true
        }

        @Override
        void replaceReturn(String returnStatement) {
            if (CodeUtil.isReturnVoid(call.method)) {
                sourceExecStatement = null
                beforeStatement.insert(0, statement)
            } else {
                // 替换其中的返回值处理
                def srcNew = ""
                def returnIndex = sourceExecStatement.indexOf("\$_")
                def returnSetIndex = sourceExecStatement.indexOf(returnIndex, "=") + 1
                if (returnIndex > 0) { // 脚本前面仍可能有其它脚本处理。 需要保留
                    srcNew = sourceExecStatement.substring(0, returnIndex - 1)
                }
                srcNew += "\$_ = $statement " + sourceExecStatement.substring(returnSetIndex + 1)
                Logger.d("replaceReturn: $statement [$returnIndex: $returnSetIndex], $sourceExecStatement => $srcNew")
                sourceExecStatement = srcNew
            }
            hasConfiguration = true
        }

        @Override
        void replaceParameter(int index, String statement) {
            Logger.d("replaceParameter: " +index+","+ statement)
            beforeStatement.insert(0, "\$" + index + " =  " + statement)
            hasConfiguration = true
        }

        final static Pattern pattern = Pattern.compile("\\\$\\{([a-zA-Z]+)}")
        void replaceFor(String statement) {
            def matcher = pattern.matcher(statement)
            Logger.d("------replaceFor: $statement, ${matcher.find()}")
            if (matcher.find()){
                def iterator = keyPairs.entrySet().iterator()
                while (iterator.hasNext()) {
                    def entry =  iterator.next()
                    statement = statement.replaceAll(entry.key, entry.value)
                }
            }
            Logger.d("------replaceFor: of $statement")
            try {
                call.replace(statement)
            } catch (CannotCompileException e) {
                e.printStackTrace()
            }
        }
    }
}
