

```markdown
# CI Pipeline — Full Reference

Every keyword in `.github/workflows/ci.yml` explained.

## Top-level keys

| Keyword | Value | Meaning |
|---|---|---|
| `name` | `Java QA CI` | Display name shown in the GitHub Actions tab. |
| `on` | *(object)* | Defines **when** this workflow runs (the trigger events). |

## `on` — Trigger events

| Keyword | Value | Meaning |
|---|---|---|
| `push` | `{ branches: [main, develop] }` | Run this workflow whenever code is **pushed** to `main` or `develop`. |
| `pull_request` | `{ branches: [main] }` | Run this workflow whenever a **pull request** targets `main`. |
| `branches` | `[main, develop]` / `[main]` | Array of branch name filters; only matching branches trigger the event. |

## `jobs` — Job definitions

| Keyword | Value | Meaning |
|---|---|---|
| `jobs` | *(object)* | Container for all jobs in this workflow. |
| `test` | *(object)* | A single job with the id `test`. The id is arbitrary and used in logs and status checks. |
| `runs-on` | `ubuntu-latest` | The job runs on GitHub's latest Ubuntu virtual machine (runner). |
| `steps` | *(list)* | Ordered list of steps the job executes sequentially. |

## `steps` — Step by step

### Step 1 — Checkout

```yaml
- uses: actions/checkout@v4
```

| Keyword | Value | Meaning |
|---|---|---|
| `uses` | `actions/checkout@v4` | Runs a **reusable action** from the GitHub marketplace. |
| `actions/checkout` | — | Clones the repository into the runner's workspace so subsequent steps can access the code. |
| `@v4` | — | Pins the action to **major version 4** (receives patches but no breaking changes). |

### Step 2 — Setup Java

```yaml
- uses: actions/setup-java@v4
  with: { java-version: '21', distribution: 'temurin' }
```

| Keyword | Value | Meaning |
|---|---|---|
| `uses` | `actions/setup-java@v4` | Installs a JDK and sets `JAVA_HOME` on the runner. |
| `with` | *(object)* | Input parameters passed to the action. |
| `java-version` | `'21'` | Install **JDK 21**. Must match `sourceCompatibility` in build.gradle. |
| `distribution` | `'temurin'` | Use the **Eclipse Temurin** (Adoptium) JDK distribution. |

### Step 3 — Make Gradle wrapper executable

```yaml
- run: chmod +x gradlew
```

| Keyword | Value | Meaning |
|---|---|---|
| `run` | `chmod +x gradlew` | Execute a **shell command** directly. |
| `chmod` | — | Linux command to **change file permissions**. |
| `+x` | — | Adds the e**x**ecute permission. |
| gradlew | — | The **Gradle wrapper** script; git can strip the execute bit, so this restores it. |

### Step 4 — Run API tests

```yaml
- run: ./gradlew apiTests
```

| Keyword | Value | Meaning |
|---|---|---|
| gradlew | — | Invokes Gradle via the checked-in wrapper (ensures the correct Gradle version). |
| `apiTests` | — | A custom Gradle **task** that runs the API test suite. |

### Step 5 — Run UI tests

```yaml
- run: ./gradlew uiTests
```

| Keyword | Value | Meaning |
|---|---|---|
| `uiTests` | — | A custom Gradle **task** that runs the WebDriver/UI test suite. |

### Step 6 — Generate Allure report

```yaml
- if: always()
  run: ./gradlew allureReport
```

| Keyword | Value | Meaning |
|---|---|---|
| `if` | `always()` | A **condition expression**. `always()` means this step runs **regardless** of whether previous steps passed or failed. |
| `allureReport` | — | Gradle task that generates an HTML report from Allure test results. |

### Step 7 — Upload artifact

```yaml
- uses: actions/upload-artifact@v4
  if: always()
  with: { name: allure-report, path: build/reports/allure-report, retention-days: 14 }
```

| Keyword | Value | Meaning |
|---|---|---|
| `uses` | `actions/upload-artifact@v4` | Uploads files from the runner so they can be **downloaded** from the Actions UI. |
| `if` | `always()` | Upload even if earlier steps failed. |
| `name` | `allure-report` | The **display name** of the artifact in the GitHub UI. |
| `path` | allure-report | The **directory** on the runner to upload. |
| `retention-days` | `14` | GitHub **deletes** the artifact after 14 days to save storage. |
