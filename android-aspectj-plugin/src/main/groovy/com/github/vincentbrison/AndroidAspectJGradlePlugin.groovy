package com.github.vincentbrison

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidAspectJGradlePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def extension = project.extensions.create('aspectj', AndroidAspectJGradlePluginExtension);
        def variants = getVariants(project)
        ensureAspectJrtDependencyIsSatisfied(project)
        project.afterEvaluate {
            variants.all { variant ->
                doWeave(variant, project, extension);
            }
        }
    }

    private void ensureAspectJrtDependencyIsSatisfied(Project project) {
        project.repositories {
            mavenCentral()
        }

        project.dependencies {
            compile 'org.aspectj:aspectjrt:1.8.9'
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

    private void doWeave(variant, Project project, AndroidAspectJGradlePluginExtension extension) {
        def File logFile = prepareLogger(project);
        def buildTypeName = variant.name.capitalize()
        def aopTask = project.task("compile${buildTypeName}AspectJ") {
            doLast {
                def args = [
                        "-showWeaveInfo",
                        "-1.5",
                        "-inpath", variant.javaCompile.destinationDir.toString(),
                        "-d", variant.javaCompile.destinationDir.toString(),
                        "-classpath", variant.javaCompile.classpath.asPath,
                        "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)
                ]
                if (extension.includeAspectPath) {
                    args << "-aspectpath" << variant.javaCompile.classpath.asPath;
                }
                logFile << "Full ajc build args: ${Arrays.toString(args as String[])}\n\n";
                MessageHandler handler = new MessageHandler(true);
                new Main().run(args as String[], handler);
                logMessages(logFile, handler);
            }
        }

        if (hasRetroLambda(project)) {
            project.tasks["compileRetrolambda$buildTypeName"].finalizedBy(aopTask)
        } else {
            variant.javaCompile.finalizedBy(aopTask)
        }
    }

    private MessageHandler logMessages(File logFile, MessageHandler handler) {
        for (IMessage message : handler.getMessages(null, true)) {
            switch (message.getKind()) {
                case IMessage.ABORT:
                case IMessage.ERROR:
                case IMessage.FAIL:
                    log("[error]", logFile, message);
                    break;
                case IMessage.WARNING:
                    log("[warning]", logFile, message);
                    break;
                case IMessage.INFO:
                case IMessage.DEBUG:
                    log("[info]", logFile, message);
                    break;
            }
        }
        println "AspectJ logs available in : $logFile.absolutePath";
    }

    private void log(String prefix, File logFile, IMessage message) {
        logFile << prefix << message?.message;
        if (message?.thrown != null) {
            logFile << message.thrown;
        }
        logFile << '\n'
    }

    private prepareLogger(Project project) {
        def logFilePath = project.buildDir.absolutePath + File.separator + 'ajc-details.log';
        File lf = new File(logFilePath);
        if (lf.exists()) {
            lf.delete();
        }
        return lf;
    }
}
