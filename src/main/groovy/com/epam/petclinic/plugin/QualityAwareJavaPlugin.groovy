package com.epam.petclinic.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.compile.JavaCompile

/**
* The QualityAwareJavaPlugin adds configuration that is common to all Java projects.
*
* Date: 5/4/2017
*
* @author Stanislau Halauniou
*/
public class QualityAwareJavaPlugin implements Plugin<Project> {

    private static final String CODE_QUALITY_DIR = 'code-quality'
    private static final String JAVA_PLUGIN_ID = "java"
    private static final String CHECKSTYLE_PLUGIN_ID = "checkstyle"

    @Override
    void apply(Project project) {
        final String JAVA_VERSION = "1.8"
        project.plugins.apply(JAVA_PLUGIN_ID)

        project.tasks.withType(JavaCompile) {
            sourceCompatibility = JAVA_VERSION
            targetCompatibility = JAVA_VERSION
            options.encoding = 'UTF-8'
            options.compilerArgs = [
                    '-Xlint:deprecation',
                    '-Xlint:finally',
                    '-Xlint:overrides',
                    '-Xlint:path',
                    '-Xlint:processing',
                    '-Xlint:rawtypes',
                    '-Xlint:varargs',
                    '-Xlint:unchecked'
            ]
        }

        configureCheckStyle(project)
    }

    private void configureCheckStyle(Project project) {
        project.plugins.apply(CHECKSTYLE_PLUGIN_ID)

        project.checkstyle {
            toolVersion = "7.1"
            config = getToolResource(project, "checkstyle/checkstyle-rules.xml")
            configProperties.suppressionsFile = getToolPath(project, "checkstyle/checkstyle-suppressions.xml")
            ignoreFailures = true
        }

        project.tasks.withType(Checkstyle) {
            reports {
                html.stylesheet(getToolResource(project, 'checkstyle/checkstyle-noframes-severity-sorted.xsl'))
            }
        }
    }


    private static TextResource getToolResource(Project project, String relativeReference) {
        return project.rootProject.resources.text.fromFile(
                getToolPath(project, relativeReference)
        )
    }

    private static String getToolPath(Project project, String relativeReference) {
        return "${project.rootDir}/${CODE_QUALITY_DIR}/${relativeReference}"
    }

}
