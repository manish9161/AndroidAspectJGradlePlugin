package me.leolin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project
/**
 * @author leolin
 */
class GradleAndroidAspectJPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def variants = getVariants(project)
        ensureAspectJrtDependencyIsSatisfied(project)
        project.afterEvaluate {
            variants.all { variant ->
                doWeave(variant, project)
            }
        }
    }

    private void ensureAspectJrtDependencyIsSatisfied(Project project) {
        project.repositories {
            mavenCentral()
        }

        def aspectjVersion = '1.8.5';
        project.dependencies {
            compile "org.aspectj:aspectjrt:$aspectjVersion"
        }
    }

    private boolean hasRetroLambda(Project project) {
        project.plugins.hasPlugin('me.tatarka.retrolambda')
    }

    private getVariants(Project project) {
        def isAppProject = project.plugins.withType(AppPlugin)
        def isLibProject = project.plugins.withType(LibraryPlugin)
        if (isAppProject) {
            return project.android.applicationVariants
        } else if (isLibProject) {
            return project.android.libraryVariants
        } else {
            throw new IllegalStateException("Must be android project or android-library project.")
        }
    }

    private void doWeave(variant, Project project) {
        def buildTypeName = variant.name.capitalize()
        def aopTask = project.task("compile${buildTypeName}AspectJ") {
            doLast {
                String[] args = [
                        "-showWeaveInfo",
                        "-1.5",
                        "-inpath", javaCompile.destinationDir.toString(),
                        "-aspectpath", javaCompile.classpath.asPath,
                        "-d", javaCompile.destinationDir.toString(),
                        "-classpath", javaCompile.classpath.asPath,
                        "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)
                ]

                MessageHandler handler = new MessageHandler(true);
                log.quiet("Start aspectJ work...")
                log.quiet("Full ajc build args: ${Arrays.toString(args as String[])}\n\n")
                new Main().run(args, handler);
                for (IMessage message : handler.getMessages(null, true)) {
                    switch (message.getKind()) {
                        case IMessage.ABORT:
                        case IMessage.ERROR:
                        case IMessage.FAIL:
                            if (message.thrown != null) {
                                log.error(message.message, message.thrown)
                            } else {
                                log.error(message.message)
                            }
                            break;
                        case IMessage.WARNING:
                            if (message.thrown != null) {
                                log.warn(message.message, message.thrown)
                            } else {
                                log.warn(message.message)
                            }
                            break;
                        case IMessage.INFO:
                        case IMessage.DEBUG:
                            break;
                    }
                }
            }
        }

        if (hasRetroLambda(project)) {
            project.tasks["compileRetrolambda$buildTypeName"].finalizedBy(aopTask)
        } else {
            variant.javaCompile.finalizedBy(aopTask)
        }
    }
}
