# petclinic-plugins

# For main project where you want to use the plugins:
Add classpath("com.epam.petclinic.plugin:petclinic-plugins:0.0.1") to your script dependencies in build.gradle
For java plugin add - apply plugin: 'petclinic-java' (remove version, sourceCompatibility ?) - it adds java, checkstyle,
pmd and findbugs plugins
For code coverage add - apply plugin: 'petclinic-code-coverage' and jacocoVersion=0.7.8(to gradle.properties)
For using idea plugin add - apply plugin: 'petclinic-idea', it applies configurations for idea for
checkstyle, formatter, pmd, findbugs plugins(BUT in addition: CheckStyle-IDEA, PMDPlugin, FindBugs-IDEA should be installed on ide)
For use document generation plugin for rest docs - apply plugin: 'petclinic-ascii-doctor'

# Publishing and using plugins for petclinic-plugins
For using plugins for main projects(petclinic-clinic, petclinic-orders) -  gradle.build should be adjusted
(see example https://github.com/shalauniou/petclinic-clinic/commit/b98feeb4338ac8ef3dcb7609314fd5046775bd9d)
It will be done after agreements later.

>Configuration

Versions of used and released plugins see in gradle.properties
Change github owner(later this repo will be unified)
Add new token on github and copy it to your user home build.gradle file in petclinicReleaseToken property

>Managing

For managing artifactory jars, login to https://jitpack.io/ with github creds, you'll see builds and releases there.
Also you could delete releases from github: git push origin :refs/tags/<tag version>

>Publishing has to stages:
Make sure, githubRelease released code that in your github repository(commit it before release)
1. publish initial version locally and to artifactory
2. resolve 'to-do' sections in build.gradle and publish once again

>How to publishing(releasing) artifacts:
- from jitpack(github) to local: gradle publishToMavenLocal (optional - you don't need it now)
- from local to local: gradle publishToMavenLocal -PdevelopPlugins
- from local to github: gradle githubRelease -PdevelopPlugins (optional - you don't need it now)
- from github to github: gradle githubRelease
