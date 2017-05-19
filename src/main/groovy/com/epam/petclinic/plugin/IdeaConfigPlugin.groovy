package com.epam.petclinic.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.XmlProvider

/**
 * Responsible for integration with Intellij IDEA.
 *
 * Date: 5/11/2017
 *
 * @author Stanislau Halauniou
 */
public class IdeaConfigPlugin implements Plugin<Project> {

    private static final String CODE_QUALITY_DIR = 'code-quality'
    private static final String IDEA_PLUGIN_ID = 'idea'
    private static final String COMPONENT = 'component'
    private static final String COPYRIGHT_TEXT = '''COPYRIGHT:     Copyright © 2017 Petclinic'''


    @Override
    void apply(Project project) {
        project.plugins.apply(IDEA_PLUGIN_ID)
        configureIdeaProject(project)
    }

    private void configureIdeaProject(Project project) {
        final String VCS_DIRECTORY_MAPPINGS = 'VcsDirectoryMappings'
        final String COPYRIGHT_MANAGER = 'CopyrightManager'
        final String COPYRIGHT_PROFILE_NAME = 'Copyright Petclinic'

        project.idea.project {
            languageLevel = 'JDK_1_8'

            ipr {
                //change idea task to overwrite only module path's
                beforeMerged { prj ->
                    prj.modulePaths.clear()
                }
                withXml { provider ->
                    // set up Git VCS support
                    def vcsMapping = provider.node.component.find {
                        it.@name == VCS_DIRECTORY_MAPPINGS
                    }.mapping

                    vcsMapping.@vcs = 'Git'
                    vcsMapping.@directory = '$PROJECT_DIR$'

                    //these settings belong to IDEA only, not to addons
                    appendIdeaSettings('idea/code-format-settings.xml', provider)

                    //addon-related IDEA settings
                    appendIdeaSettings('idea/checkstyle-settings.xml', provider)
                    appendIdeaSettings('idea/findbugs-settings.xml', provider)

                    //PMD IDEA addon does not "understand" project-related paths and require
                    // absolute path of the config file
                    appendPmdSettings(provider, project.projectDir.absolutePath)

                    Node copyrightComponent = provider.node.component.find { it.@name == COPYRIGHT_MANAGER } ?:
                            provider.node.appendNode(COMPONENT, [name: COPYRIGHT_MANAGER])

                    copyrightComponent.replaceNode {
                        component(name: COPYRIGHT_MANAGER, 'default': COPYRIGHT_PROFILE_NAME) {
                            copyright() {
                                option(name: 'notice', value: COPYRIGHT_TEXT)
                                option(name: 'keyword', value: 'Copyright')
                                option(name: 'allowReplaceKeyword', value: '')
                                option(name: 'myName', value: COPYRIGHT_PROFILE_NAME)
                                option(name: 'myLocal', value: 'true')
                            }
                        }
                    }
                }
            }
        }
    }

    private void appendIdeaSettings(String fileName, XmlProvider xmlProvider) {
        Node settingsNode = new XmlParser().parse(
                this.getClass().getClassLoader()
                        .getResourceAsStream(fileName)
        )
        xmlProvider.node.append(settingsNode)
    }

    private void appendPmdSettings(XmlProvider xmlProvider, String projectDirReformatted) {
        Node settingsNode = new XmlParser().parse(
                this.getClass().getClassLoader()
                        .getResourceAsStream('idea/pmd-settings.xml')
        )

        //pmd requires system-specific separators
        String separator = File.separator
        String pmdCommonRulesPath = "${projectDirReformatted}${separator}${CODE_QUALITY_DIR}${separator}pmd" +
                "${separator}pmd-rules-general.xml"


        //projectDir cannot be pre-defined, so I insert it manually
        Node customizedNode = new XmlParser().parseText("""
            <option name=\"customRuleSets\">
              <list>
                <option value=\"${pmdCommonRulesPath}\"/>
              </list>
            </option>
        """)

        settingsNode.children().add(customizedNode)

        xmlProvider.node.append(settingsNode)
    }
}
