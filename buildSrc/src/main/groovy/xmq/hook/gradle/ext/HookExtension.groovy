package xmq.hook.gradle.ext

import xmq.hook.gradle.core.HookerImpl
import org.gradle.api.Project
import xmq.hook.gradle.util.Logger

/**
 * @author xmqyeah* @CreateDate 2021/12/10 0:13
 */
class HookExtension {
    static Set<HookerImpl> hookers = new HashSet<>()
    static Set<HookerImpl> globalHookers = new HashSet<>()
    /**
     * 对应当前module下执行的Hook
     * @param regex
     * @param methodCall
     */
    void addHooker(String regex, HookerImpl.IMethodCall methodCall) {
        def hooker = new HookerImpl(regex, methodCall)
        if (hookers.contains(hooker)) {
            Logger.e("addHooker failed: exsits: "+regex)
        }
        hookers.add(hooker)
    }
    void addGlobalHooker(String regex, HookerImpl.IMethodCall methodCall) {
        def hooker = new HookerImpl(regex, methodCall)
        if (hookers.contains(hooker)) {
            Logger.e("addHooker failed: exsits: "+regex)
        }
        hookers.add(hooker)
        if (globalHookers.contains(hooker)) {
            Logger.e("addGlobalHooker failed: exsits: "+regex)
        }
        globalHookers.add(hooker)
    }
}
