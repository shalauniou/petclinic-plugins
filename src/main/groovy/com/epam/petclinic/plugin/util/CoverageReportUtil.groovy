package com.epam.petclinic.plugin.util

import org.gradle.api.Project
import org.jacoco.core.analysis.Analyzer
import org.jacoco.core.analysis.CoverageBuilder
import org.jacoco.core.analysis.IBundleCoverage
import org.jacoco.core.tools.ExecFileLoader
import org.jacoco.report.DirectorySourceFileLocator
import org.jacoco.report.FileMultiReportOutput
import org.jacoco.report.IReportVisitor
import org.jacoco.report.html.HTMLFormatter
import org.jacoco.report.xml.XMLFormatter

import java.nio.charset.StandardCharsets

/**
 * Coverage Report Utils for {@link com.epam.petclinic.plugin.CodeCoveragePlugin}.
 *
 * Date: 5/10/2017
 *
 * @author Stanislau Halauniou
 */
final class CoverageReportUtil {
    private static final String JACOCO_TEST_PATH = "jacoco/jacocoTest.exec"
    private static final int TAB_WIDTH = 4
    private static String title

    private static File executionDataFile
    private static File classesDirectory
    private static File sourceDirectory
    private static File reportDirectory

    private static ExecFileLoader execFileLoader

    private CoverageReportUtil() {
        throw new AssertionError("${CoverageReportUtil.class.name}" +
                ' is utility class. Creating an instance is prohibited')
    }

    /**
     * Generates the coverage report.
     *
     * @param project project to which need generate report
     *
     * @throws IOException
     */
    public static void generate(Project project) throws IOException {
        title = project.projectDir.getName()
        executionDataFile = new File(project.buildDir, JACOCO_TEST_PATH)
        classesDirectory = project.sourceSets.main.output.classesDir
        sourceDirectory = project.sourceSets.main.java.getSrcDirs().getAt(0)
        reportDirectory = new File(project.buildDir, 'reports/coverage')
        if (!reportDirectory.exists()) {
            reportDirectory.mkdirs()
        }

        // Read the jacoco.exec file.
        loadExecutionData()

        // Run the structure analyzer on a single class folder to build up the coverage model.
        final IBundleCoverage bundleCoverage = analyzeStructure()

        createReport(bundleCoverage)

    }

    private static void createReport(final IBundleCoverage bundleCoverage) throws IOException {
        // Create a concrete report visitor based on some supplied
        // configuration. In this case we use the defaults
        XMLFormatter xmlFormatter = new XMLFormatter()
        IReportVisitor visitor = xmlFormatter
                .createVisitor(new FileOutputStream(reportDirectory.path + '/coverage-report.xml'))

        // Initialize the report with all of the execution and session
        // information. At this point the report doesn't know about the
        // structure of the report being created
        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
                execFileLoader.getExecutionDataStore().getContents())

        // Populate the report structure with the bundle coverage information.
        // Call visitGroup if you need groups in your report.
        visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(
                sourceDirectory, StandardCharsets.UTF_8.toString(), TAB_WIDTH))

        // Signal end of structure information to allow report to write all
        // information out
        visitor.visitEnd()

        // Same for html
        HTMLFormatter htmlFormatter = new HTMLFormatter()
        visitor = htmlFormatter
                .createVisitor(new FileMultiReportOutput(reportDirectory))
        visitor.visitInfo(execFileLoader.getSessionInfoStore().getInfos(),
                execFileLoader.getExecutionDataStore().getContents())
        visitor.visitBundle(bundleCoverage, new DirectorySourceFileLocator(
                sourceDirectory, StandardCharsets.UTF_8.toString(), TAB_WIDTH))
        visitor.visitEnd()

    }

    private static void loadExecutionData() throws IOException {
        execFileLoader = new ExecFileLoader()
        if (executionDataFile.exists()) {
            execFileLoader.load(executionDataFile)
        }
    }

    private static IBundleCoverage analyzeStructure() throws IOException {
        final CoverageBuilder coverageBuilder = new CoverageBuilder()
        final Analyzer analyzer = new Analyzer(
                execFileLoader.getExecutionDataStore(), coverageBuilder)

        analyzer.analyzeAll(classesDirectory)

        return coverageBuilder.getBundle(title)
    }
}
