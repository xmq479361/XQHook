package xmq.hooks;

/**
 * 方法调用信息
 */
interface IMethodCallInfo {
    /**
     * 方法返回值;  包括: void,int,double,float,byte,char,long,
     *
     * @return
     */
    String returnType();

    String[] params();

    String methodName();

    String className();

    String classSimpleName();

    String invokeFileName();

    String invokeClzName();

    String invokeClzSimpleName();

    String invokeMethodName();

    int invokeLineNo();
}