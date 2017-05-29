package com.epam.petclinic.plugin.util

import org.gradle.api.Project

/**
 * Code quality util.
 *
 * Date: 5/25/2017
 *
 * @author Stanislau Halauniou
 */
final class CodeQualityUtil {

    private static final String ERROR_CHECKSTYLE_NODE_NAME = "error"
    private static final String ERROR_FINDBUGS_NODE_NAME = "BugInstance"
    private static final String ERROR_PMD_NODE_NAME = "violation"
    private static final String ERROR_PRIORITY = "1"

    private CodeQualityUtil() {
        throw new AssertionError("${CodeQualityUtil.class.name} is utility class. Creating an instance is prohibited")
    }

    /**
     * Returns list of errors from checkstyle reports.
     *
     * @param project project
     * @param sourceSetName main or test usually
     * @return list of errors
     */
    public static List getCheckstyleReportErrors(Project project, String sourceSetName) {
        return getReportErrors(new File("${project.checkstyle.reportsDir}${File.separator}${sourceSetName}.xml"),
                { it, reportErrors ->
                    if (ERROR_CHECKSTYLE_NODE_NAME == it.name() && ERROR_CHECKSTYLE_NODE_NAME == it.@severity) {
                        reportErrors << "Checkstyle ${ERROR_CHECKSTYLE_NODE_NAME}: " +
                                "${it.@message} at ${it.parent().@name}"
                    }
                })
    }

    /**
     * Returns list of errors from findbugs reports.
     *
     * @param project project
     * @param sourceSetName main or test usually
     * @return list of errors
     */
    public static List getFindbugsReportErrors(Project project, String sourceSetName) {
        return getReportErrors(new File("${project.findbugs.reportsDir}${File.separator}${sourceSetName}.xml"),
                { it, reportErrors ->
                    if (ERROR_FINDBUGS_NODE_NAME == it.name() && ERROR_PRIORITY == it.@priority) {
                        reportErrors << "Findbugs ${ERROR_FINDBUGS_NODE_NAME}: ${it.LongMessage}"
                    }
                })
    }

    /**
     * Returns list of errors from pmd reports.
     *
     * @param project project
     * @param sourceSetName main or test usually
     * @return list of errors
     */
    public static List getPmdReportErrors(Project project, String sourceSetName) {
        return getReportErrors(new File("${project.pmd.reportsDir}${File.separator}${sourceSetName}.xml"),
                { it, reportErrors ->
                    if (ERROR_PMD_NODE_NAME == it.name() && ERROR_PRIORITY == it.@priority) {
                        reportErrors << "Pmd ${ERROR_PMD_NODE_NAME}: ${it.@ruleset} (${it.@rule})"
                    }
                })
    }

    /**
     * Returns list of errors.
     *
     * @param file file from reports to parse
     * @param closure add error if node is error
     * @return list of errors
     */
    private static List getReportErrors(File file, Closure closure) {
        List reportErrors = []
        if (file.exists()) {
            Node rootNode = new XmlParser().parse(file)
            rootNode.depthFirst().each {
                closure(it, reportErrors)
            }
        }
        return reportErrors
    }
}
