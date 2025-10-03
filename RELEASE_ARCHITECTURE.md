# Release Process Architecture

This document explains how the release workflows are designed to be independent and resilient.

## Workflow Independence

### Primary Release Process (GitHub)
**Workflow**: `.github/workflows/maven-publish.yml`
- ✅ **Triggered by**: Push to `main` branch
- ✅ **Publishes to**: GitHub Packages + GitHub Releases
- ✅ **Creates**: Binary distributions, release notes, checksums
- ✅ **Status**: Always runs when code is pushed to main

### Secondary Publishing (Maven Central)
**Workflow**: `.github/workflows/maven-central.yml`
- 🔄 **Triggered by**: Manual dispatch OR after GitHub release
- 🔄 **Publishes to**: Maven Central (Sonatype OSSRH)
- 🔄 **Creates**: Maven artifacts with GPG signatures
- 🔄 **Status**: Optional, can fail without affecting main release

## Failure Isolation

### If GitHub Release Fails
- ❌ No GitHub Release created
- ❌ No binaries available for download
- ❌ No GitHub Packages published
- ❌ **Impact**: Complete release failure

### If Maven Central Publishing Fails
- ✅ GitHub Release still exists and works
- ✅ Binaries still available for download
- ✅ GitHub Packages still accessible
- ✅ **Impact**: Only Maven Central unavailable

## Recovery Scenarios

### Recovering from Maven Central Failure

1. **Re-run the workflow**:
   - Go to Actions → Maven Central Release
   - Click "Run workflow"
   - Enter the same version number
   - The workflow will retry Maven Central publishing

2. **Debug with dry-run**:
   - Run the workflow with "dry run" enabled
   - Check for configuration issues
   - Fix problems and re-run

3. **Manual publishing** (if needed):
   ```bash
   # Checkout the release tag
   git checkout v1.2.9
   
   # Publish to Maven Central
   mvn clean deploy -Pmaven-central -DskipTests=true
   ```

## Configuration Status

### Required for GitHub Release (✅ Ready)
- GitHub repository secrets: `PAT`
- GitHub Actions enabled
- Maven settings.xml configured

### Required for Maven Central (⚠️ Setup Needed)
- Sonatype OSSRH account: `OSSRH_USERNAME`, `OSSRH_TOKEN`
- GPG signing keys: `GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`
- Group ID approved by Sonatype
- Optional: GitHub environment `maven-central`

## Best Practices

1. **Always test GitHub release first** - This is the primary distribution method
2. **Use Maven Central dry-run** - Test before actual publishing
3. **Monitor both workflows** - They run independently
4. **Document failures** - Maven Central issues don't affect main release
5. **Re-run safely** - Maven Central workflow can be repeated

## Benefits of This Architecture

- 🛡️ **Resilient**: Main release works even if Maven Central fails
- 🔄 **Recoverable**: Maven Central can be retried without affecting anything else
- 🚀 **Fast**: Users get GitHub releases immediately
- 🌐 **Comprehensive**: Eventually available in both GitHub and Maven ecosystems
- 🔧 **Maintainable**: Each workflow has a single responsibility

## Workflow Dependencies

```
Push to main
     ↓
GitHub Release Workflow (maven-publish.yml)
     ↓
✅ GitHub Release Created
     ↓
[Optional] Maven Central Workflow (maven-central.yml)
     ↓
🔄 Maven Central Published (or fails independently)
```

The arrow from GitHub Release to Maven Central is **optional** and **non-blocking**.