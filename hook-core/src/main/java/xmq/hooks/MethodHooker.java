package xmq.hooks;

/**
 * 注册
 * @author xmqyeah
 * @CreateDate 2021/12/10 0:17
 */
public abstract class MethodHooker extends IMatcher.MethodPatternMatcher implements IMethodInvoker {
    public MethodHooker(String regex) {
        super(regex);
    }

}
