// PostToolUse hook: cheap static checks for the AGENTS.md hard rules on the file just edited.
// Silent when clean. On a violation it exits 2 so Claude sees the message and fixes it right away,
// instead of finding out later from a full detekt/Gradle run.
import { readFileSync } from "node:fs";

let input = "";
for await (const chunk of process.stdin) input += chunk;

let filePath;
try {
  filePath = JSON.parse(input)?.tool_input?.file_path;
} catch {
  process.exit(0);
}
if (!filePath) process.exit(0);

const path = filePath.replace(/\\/g, "/");
let src;
try {
  src = readFileSync(filePath, "utf8");
} catch {
  process.exit(0);
}

const issues = [];
const lines = src.split(/\r?\n/);
const hit = (re, msg) => {
  lines.forEach((l, i) => {
    const t = l.trim();
    const isComment = t.startsWith("//") || t.startsWith("*") || t.startsWith("/*");
    if (re.test(l) && !isComment) issues.push(`${i + 1}: ${msg}`);
  });
};

if (path.endsWith(".kt") && path.includes("/src/main/")) {
  hit(/\bcollectAsState\(/, "use collectAsStateWithLifecycle(), not collectAsState()");
  hit(/\bTODO\(/, "no TODO() — handle every branch (Loading | Success | Error | MessageSuccess)");

  if (/@HiltViewModel/.test(src)) {
    hit(/^import android\.content\.Context\b/, "never inject Context into a ViewModel");
    hit(/private val \w+: \w+(Repository|Service|Api)\b/, "ViewModels call use cases only, not repositories/services/APIs");
  }

  // Hardcoded UI text, ignoring the preview section at the bottom of the file.
  const previewStart = lines.findIndex((l) => /@(ThemePreviews|FontScalePreviews|Preview)\b/.test(l));
  lines.forEach((l, i) => {
    if (previewStart !== -1 && i >= previewStart) return;
    const m = l.match(/\bText\(\s*(?:text\s*=\s*)?"([^"]*)"/);
    // Only flag real words; "$count" / "${a}%" templates are fine.
    if (m && /[A-Za-z]{2,}/.test(m[1].replace(/\$\{[^}]*\}|\$\w+/g, ""))) {
      issues.push(`${i + 1}: use stringResource(R.string.…) instead of a hardcoded Text string`);
    }
  });
}

if (path.endsWith(".gradle.kts") && !path.endsWith("settings.gradle.kts")) {
  hit(/(implementation|api|ksp|kapt|testImplementation|androidTestImplementation|debugImplementation)\(\s*"[^"]+:[^"]+:[^"]+"/,
    "hardcoded dependency version — add it to gradle/libs.versions.toml and use libs.*");
  if (/^.*\/features\//.test(path) || path.endsWith("features/build.gradle.kts")) {
    hit(/project\(":features/, "feature-to-feature dependency is forbidden — share via :core / :core-ui");
  }
}

if (issues.length) {
  console.error(`Convention check (${filePath}):\n` + issues.map((s) => `  line ${s}`).join("\n"));
  process.exit(2);
}
