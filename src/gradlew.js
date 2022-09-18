const fs = require("fs");
const path = require("path");
const { spawn } = require("child_process");

if (process.env.JAVA_HOME) {
  const jreShJava = path.join(process.env.JAVA_HOME, "jre", "sh", "java");
  const stats = fs.existsSync(jreShJava) && fs.statSync(jreShJava);
  if (stats && stats.isFile()) {
    process.env.JAVACMD = jreShJava;
  } else {
    process.env.JAVACMD = path.join(process.env.JAVA_HOME, "bin", "java");
  }
} else {
  process.env.JAVACMD = "java";
}

process.env.APP_HOME = path.dirname(__dirname);
process.env.APP_NAME = "Gradle";
process.env.APP_BASE_NAME = "gradlew";
process.env.CLASSPATH = path.join(
  process.env.APP_HOME,
  "src",
  "build",
  "gradle",
  "gradle-wrapper.jar"
);

const jvmOpts = ["-Xmx64m", "-Xms64m"];
const opts = [
  ...jvmOpts,

  `-Dorg.gradle.appname=${process.env.APP_BASE_NAME}`,

  "-classpath",
  `${process.env.CLASSPATH}`,

  "org.gradle.wrapper.GradleWrapperMain",

  ...process.argv.slice(2),
];

spawn(process.env.JAVACMD, opts, {
  stdio: "inherit",
});
