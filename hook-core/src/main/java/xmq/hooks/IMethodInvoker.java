package xmq.hooks;

/**
 * 方法调用处理接口
 * @author xmqyeah
 * @CreateDate 2021/12/9 23:45
 */
public interface IMethodInvoker extends IMethodCallInfo {

    /**
     * 方法调用信息
     * @return
     */
    IMethodCallInfo callInfo();

    /**
     * 获取参数在statement代码块中的描述key
     * @param index
     * @return
     */
    String paramInStatement(int index);

    /**
     * 插入执行语句{statement}到方法之前
     * @param statement 执行代码字符串语义化。 如: "System.out.println("\"Hello!\"");"
     */
    void insertBefore(String statement);

    /**
     * 插入执行语句{statement}到方法之后
     * @param statement 执行代码字符串语义化。 如: "System.out.println("\"Hello!\"");"
     */
    void insertAfter(String statement);

    /**
     * 替换执行的方法。 此时与${replaceWithReturn}对应。替换后原方法的执行将给予默认的返回值: {CodeUtil.getReturnValue()}
     * @param statement 执行代码字符串语义化。 如: "System.out.println("\"Hello!\"");"
     */
    void replace(String statement);

    /**
     * 替换执行的方法(包括返回值)。 此时与${replaceWithReturn}对应。此方法将会用替换后执行结果作为原方法的返回值。
     * 所以替换方法间必须保证返回值是相同的。
     * @param statement 执行代码字符串语义化。 如: "System.out.println("\"Hello!\"");"
     */
    void replaceWithReturn(String statement);

    /**
     * 替换执行的方法返回值。
     * @param returnStatement 返回值字符串语义化。 如返回字符串: "\"Hello!\"";"
     */
    void replaceReturn(String returnStatement);
    /**
     * 替换方法参数
     * @param index 参数索引位置
     * @param statement 执行代码字符串语义化。 如: "System.out.println("\"Hello!\"");"
     */
    void replaceParameter(int index, String statement);

}
