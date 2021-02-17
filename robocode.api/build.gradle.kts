import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.type

plugins {
    id("net.sf.robocode.java-conventions")
    `java-library`
    `maven-publish`
}

description = "Robocode API"

/* TODO
<artifactId>maven-deploy-plugin</artifactId>
https://docs.gradle.org/current/userguide/publishing_setup.html
https://docs.gradle.org/current/userguide/java_library_distribution_plugin.html
*/

tasks.jar {
    manifest {
        attributes(mapOf("Main-Class" to "robocode.Robocode"))
    }
    archiveFileName.set("robocode.jar")
    dependsOn("javadoc")
}

tasks.javadoc {
    source = sourceSets["main"].java
    exclude("robocode/exception/**")
    exclude("robocode/robocodeGL/**")
    exclude("gl4java/**")
    exclude("net/sf/robocode/**")

    options.windowTitle = "Robocode ${project.version} API"
    var op = options as StandardJavadocDocletOptions
    op.docTitle = "<h1>Robocode ${project.version} API</h1>"
    op.docFilesSubDirs(true)
    op.use(false)
    op.author(true)
    op.isNoTimestamp = false
    op.bottom("Copyright &#169; 2021 <a href=\"http://robocode.sf.net\">Robocode</a>. All Rights Reserved.")
    op.links = listOf("https://docs.oracle.com/javase/8/docs/api/")
    op.charSet("UTF-8")
    op.source("1.8")
    op.excludeDocFilesSubDir("robocode.exception", "net.sf.robocode", "gl4java", "robocode.robocodeGL")
    op.addStringsOption("exclude", ":").value = listOf("gl4java", "robocode.exception", "net.sf.robocode", "robocode.robocodeGL")
}