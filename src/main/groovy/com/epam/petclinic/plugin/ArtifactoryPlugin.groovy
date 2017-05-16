package com.epam.petclinic.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

/**
 * The ArtifactoryPlugin deploy artifacts and build information to Artifactory.
 *
 * Date: 5/4/2017
 *
 * @author Stanislau Halauniou
 */
class ArtifactoryPlugin implements Plugin<Project> {
    private final String MAVEN_PUBLISH_PLUGIN = 'maven-publish'
    private final String GITHUB_PUBLISH_PLUGIN = 'co.riiid.gradle'

    @Override
    void apply(Project project) {
        project.plugins.apply(MAVEN_PUBLISH_PLUGIN)
        project.plugins.apply(GITHUB_PUBLISH_PLUGIN)
        configureMavenPublication(project)
    }

    void configureMavenPublication(Project project) {
        project.task([type: Jar], 'sourcesJar') {
            from(project.sourceSets.main.allSource)
            classifier('sources')
        }

        configureDocTask(project)

        project.publishing.publications {
            mavenJava(MavenPublication) {
                artifactId(project.archivesBaseName)
                groupId('com.epam.petclinic')
                version("${project.productVersion}")
                from(project.components.java)
                artifact(project.javadocJar)
                artifact(project.sourcesJar)
            }
        }

        project.github {
            owner = "${project.githubOwner}"
            repo = "${project.archivesBaseName}"
            tagName = "${project.productVersion}"
            targetCommitish = 'master'
            token = "${project.petclinicReleaseToken}"
            body = "${project.releaseNote}"
        }
    }

    void configureDocTask(Project project) {
        project.task([type: Jar, dependsOn: project.groovydoc], 'javadocJar') {
            from(project.groovydoc.destinationDir)
            classifier('javadoc')
        }
    }
}
