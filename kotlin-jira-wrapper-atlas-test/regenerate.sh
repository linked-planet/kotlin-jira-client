#!/bin/bash

set -e

SCRIPT_DIR="$(
  cd "$(dirname "$0")" || exit
  pwd -P
)"

get_version_from_maven() {
    mvn help:evaluate -Dexpression=project.version -q -DforceStdout | tail -n 1
}

FOLDER_NAME=$(basename "$SCRIPT_DIR")
ARTIFACT_ID="kotlin-jira-wrapper-atlas-test"
VERSION=$(get_version_from_maven)

pushd "$PWD" || exit
cd "$SCRIPT_DIR" && cd "../" || exit

echo "---------------------------------------------------"
echo "Renaming existing project to $FOLDER_NAME-swap ..."
echo "---------------------------------------------------"
echo
mv "$FOLDER_NAME" "$FOLDER_NAME-swap"


echo "---------------------------------------------------"
echo "Regenerating project files ..."
echo "---------------------------------------------------"
echo
mvn archetype:generate -B -U \
    "-DarchetypeGroupId=com.linked-planet.maven.archetype" \
    "-DarchetypeArtifactId=atlassian-plugin-kotlin" \
    "-DarchetypeVersion=3.1.4-SNAPSHOT" \
    "-DatlassianApp=confluence" \
    "-DgroupId=com.linked-planet.plugin.confluence" \
    "-DartifactId=kotlin-jira-wrapper-atlas-test" \
    "-Dpackage=com.linkedplanet.plugin.confluence.jirawrapper.test" \
    "-DnameHumanReadable=kotlin-jira-wrapper-atlas-test" \
    "-Ddescription=Description of New Plugin" \
    "-DorganizationNameHumanReadable=linked-planet GmbH" \
    "-DorganizationUrl=https://linked-planet.com" \
    "-DinceptionYear=2021" \
    "-DdeveloperConnection=scm:git:https://github.com/linked-planet/new-plugin.git" \
    "-DgenerateGithubActions=false" \
    "-DgenerateBitbucketPipelines=false" \
    "-DgenerateDockerEnvironment=false" \
    "-DgenerateStubs=false"


echo "---------------------------------------------------"
echo "Post-process generated files ..."
echo "---------------------------------------------------"
# must not overwrite the script itself
rm $ARTIFACT_ID/regenerate.sh
# inject current project version into regenerated files
if test -f "$ARTIFACT_ID/runConfigurations/package___deploy.xml"; then
  sed -i -E "s/(.*)-[0-9]+\.[0-9]+\.[0-9]+.*\.jar/\1-$VERSION.jar/g" $ARTIFACT_ID/runConfigurations/package___deploy.xml
fi
sed -i -E "0,/<version>[0-9]+\.[0-9]+\.[0-9]+(.*)<\/version>/s/<version>[0-9]+\.[0-9]+\.[0-9]+(.*)<\/version>/<version>$VERSION<\/version>/" $ARTIFACT_ID/pom.xml


echo "---------------------------------------------------"
echo "Copy files into swap ..."
echo "---------------------------------------------------"
echo
cp -a "$ARTIFACT_ID/." "$FOLDER_NAME-swap/"


echo "---------------------------------------------------"
echo "Clean up ..."
echo "---------------------------------------------------"
echo
rm -r "$ARTIFACT_ID"
mv "$FOLDER_NAME-swap" "$FOLDER_NAME"

popd || exit
