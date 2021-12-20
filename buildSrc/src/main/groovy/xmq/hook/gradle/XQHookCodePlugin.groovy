package xmq.hook.gradle

import com.android.build.gradle.AppExtension
import xmq.hook.gradle.ext.HookExtension
import org.gradle.api.Project
import org.gradle.api.Plugin
import xmq.hook.gradle.util.Logger

class XQHookCodePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        Logger.init(project)
        Logger.e(">>>XQHookCodePlugin apply: ${project.name}")
        if (project == project.rootProject) {
            return
        }
        def android = project.extensions.getByType(AppExtension)
        project.extensions.create("hookConfig", HookExtension)
//        android.registerTransform(new CodeLineInsertAsmTransform())
        android.registerTransform(new XQHookCodeTransform())
    }
}