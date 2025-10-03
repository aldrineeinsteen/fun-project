# Maven Central Publishing Setup

This document provides step-by-step instructions for setting up Maven Central publishing for the Fun Project.

## Prerequisites

### 1. Sonatype OSSRH Account
1. Create an account at [Sonatype OSSRH](https://s01.oss.sonatype.org/)
2. Create a [JIRA ticket](https://issues.sonatype.org/secure/CreateIssue.jspa?issuetype=21&pid=10134) to claim your group ID
3. Wait for approval (usually takes 1-2 business days)

### 2. GPG Key Setup
Generate a GPG key for signing artifacts:

```bash
# Generate GPG key
gpg --gen-key

# List keys to get the key ID
gpg --list-secret-keys --keyid-format LONG

# Export public key to key servers
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID

# Export private key for GitHub secrets (base64 encoded)
gpg --export-secret-keys YOUR_KEY_ID | base64 > private-key.txt
```

### 3. GitHub Secrets Configuration

Add the following secrets to your GitHub repository:

#### Required Secrets:
- `OSSRH_USERNAME`: Your Sonatype OSSRH username
- `OSSRH_TOKEN`: Your Sonatype OSSRH password/token
- `GPG_PRIVATE_KEY`: Base64 encoded GPG private key (content of private-key.txt)
- `GPG_PASSPHRASE`: Passphrase for your GPG key

#### Environment Configuration:
1. Go to Settings → Environments in your GitHub repository
2. Create a new environment named `maven-central`
3. Add the secrets above to this environment
4. Optionally, add deployment protection rules

### 4. Update Workflow Configuration

In `.github/workflows/maven-central.yml`, update:
- `GPG_KEY_ID`: Replace with your actual GPG key ID

## Publishing Process

### Automatic Publishing (Recommended)
The Maven Central workflow is triggered automatically when a GitHub release is published:

1. Push changes to `main` branch
2. GitHub Release workflow creates a release
3. Maven Central workflow is automatically triggered
4. Artifacts are published to Maven Central

### Manual Publishing
You can manually trigger the Maven Central workflow:

1. Go to Actions → Maven Central Release
2. Click "Run workflow"
3. Enter the version to publish
4. Optionally enable "dry run" for testing

### Dry Run Testing
Before publishing to Maven Central, test the process:

1. Run the workflow with "dry run" enabled
2. Check that all artifacts are generated correctly
3. Verify GPG signing works
4. Ensure POM compliance with Maven Central requirements

## Workflow Features

### Optimized GitHub Release
- **Parallel jobs**: Prepare and perform phases run separately
- **Caching**: Maven dependencies cached between runs
- **Artifact management**: Comprehensive distribution packages
- **Release notes**: Auto-generated with changelog
- **Checksums**: SHA256 verification files included
- **Cross-platform**: ZIP (Windows) and TAR.GZ (Unix) distributions

### Maven Central Publishing
- **Validation**: Pre-flight checks for Maven Central requirements
- **GPG Signing**: Automatic artifact signing
- **Staging**: Uses Nexus staging plugin for controlled releases
- **Dry Run**: Test publishing without actual deployment
- **Environment Protection**: Secure secret management

## Distribution Packages

The release process creates several distribution formats:

### GitHub Releases
- `fun-project.jar` - Main application JAR
- `fun-project-VERSION-distribution.zip` - Complete Windows distribution
- `fun-project-VERSION-distribution.tar.gz` - Complete Unix/Linux distribution
- `*.sha256` files - Integrity verification checksums

### Maven Central
- Main JAR with dependencies managed separately
- Sources JAR (`-sources.jar`)
- Javadoc JAR (`-javadoc.jar`)
- GPG signatures (`.asc` files)
- POM with complete metadata

## Troubleshooting

### Common Issues

#### GPG Signing Fails
- Verify GPG key is correctly imported
- Check GPG_PASSPHRASE secret is correct
- Ensure key hasn't expired

#### Maven Central Validation Fails
- Check POM has all required elements (name, description, url, licenses, developers, scm)
- Verify group ID matches your approved Sonatype claim
- Ensure version is not a SNAPSHOT for releases

#### Nexus Staging Issues
- Check OSSRH credentials are valid
- Verify repository is in correct staging state
- Review Nexus repository activity logs

### Useful Commands

```bash
# Test GPG signing locally
mvn clean verify -Pgpg-sign

# Test Maven Central profile
mvn clean deploy -Pmaven-central -DskipTests=true

# Check POM compliance
mvn help:effective-pom

# Validate project structure
mvn validate
```

## Maven Central URLs

After successful publishing, artifacts will be available at:

- **Search**: https://search.maven.org/search?q=g:com.aldrineeinsteen
- **Repository**: https://repo1.maven.org/maven2/com/aldrineeinsteen/fun-project/
- **Central Portal**: https://central.sonatype.org/

## Security Best Practices

1. **Secrets Management**: Use GitHub environments for production secrets
2. **GPG Key Security**: Store private keys securely, use strong passphrases
3. **Access Control**: Limit repository access and review permissions regularly
4. **Audit Trail**: Monitor deployment logs and Maven Central activity
5. **Key Rotation**: Regularly rotate GPG keys and update OSSRH credentials

## Support

For issues with:
- **OSSRH Account**: Contact Sonatype support
- **GPG Setup**: Check GPG documentation
- **GitHub Actions**: Review workflow logs and GitHub documentation
- **Maven Central**: Review Sonatype OSSRH documentation