#!/bin/bash

# Fun Project Launcher Script
# 
# This script automatically detects the current branch and chooses the appropriate way to run the application:
# - On 'main' branch: Downloads pre-built binaries from GitHub Releases (no authentication required)
# - On other branches: Builds locally using Maven (for development)
#
# Benefits of using GitHub Releases:
# - No authentication required (public releases)
# - Faster startup (no build time)
# - Includes all dependencies and plugins in a single download
# - Works offline after first download

jarfile=target/fun-project.jar
libFolder=target/lib/*
pluginFolder=target/plugins/*

# Initialize optional parameters with defaults
signature="--sign"
keep_alive="-k"
end_time="-e 1830"
dashboard="--dash"

# Parse optional arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --signature|--sign)
      signature="--sign"
      shift # past argument
      ;;
    --keep-alive|-k)
      keep_alive="-k"
      shift # past argument
      ;;
    --dashboard|--dash)
      dashboard="--dash"
      shift # past argument
      ;;
    --end-time|-e)
      end_time="-e $2"
      shift # past argument
      shift # past value
      ;;
    *)    # unknown option
      shift # past argument
      ;;
  esac
done

# Function to run the application
run_application() {
    echo "Running fun-project.jar"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Starting application with args: $signature $keep_alive $dashboard $end_time" >> runtime.log
    java -cp "$libFolder:$pluginFolder:$jarfile" com.aldrineeinsteen.fun.Main $signature $keep_alive $dashboard $end_time 2>&1 | tee -a runtime.log
    exit_code=${PIPESTATUS[0]}
    if [ $exit_code -ne 0 ]; then
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] Application exited with error code: $exit_code" >> runtime.log
    fi
    return $exit_code
}

# Function to build locally
build_locally() {
    echo "Building project locally with Maven..."
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Building project locally with Maven" >> runtime.log
    ./mvnw clean install 2>&1 | tee -a runtime.log
    build_exit_code=${PIPESTATUS[0]}
    if [ $build_exit_code -ne 0 ]; then
        echo "Error: Build failed with exit code: $build_exit_code"
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: Build failed with exit code: $build_exit_code" >> runtime.log
        exit $build_exit_code
    fi

    if [[ -f "$jarfile" ]]; then
        run_application
    else
        echo "Error: Failed to build jar file locally"
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: Failed to build jar file locally" >> runtime.log
        exit 1
    fi
}

# Function to download from GitHub Releases
download_from_github_releases() {
    echo "Attempting to download latest release from GitHub Releases..."
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Attempting to download from GitHub Releases" >> runtime.log
    
    # Create target directories
    mkdir -p target/lib target/plugins
    
    # GitHub repository details
    REPO="aldrineeinsteen/fun-project"
    
    # Check if curl is available
    if ! command -v curl &> /dev/null; then
        echo "curl is required but not installed. Falling back to local build."
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: curl not found, falling back to local build" >> runtime.log
        build_locally
        return
    fi
    
    # Get the latest release information
    echo "Fetching latest release information..."
    RELEASE_INFO=$(curl -s "https://api.github.com/repos/$REPO/releases/latest" 2>> runtime.log)
    
    if echo "$RELEASE_INFO" | grep -q "Not Found"; then
        echo "No releases found. Falling back to local build."
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] WARNING: No releases found, falling back to local build" >> runtime.log
        build_locally
        return
    fi
    
    # Extract download URL for the distribution zip
    DISTRIBUTION_URL=$(echo "$RELEASE_INFO" | grep -o '"browser_download_url": "[^"]*fun-project-distribution.zip"' | cut -d'"' -f4)
    
    if [[ -z "$DISTRIBUTION_URL" ]]; then
        echo "Distribution package not found in latest release. Falling back to local build."
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] WARNING: Distribution package not found, falling back to local build" >> runtime.log
        build_locally
        return
    fi
    
    echo "Downloading distribution package from: $DISTRIBUTION_URL"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Downloading from: $DISTRIBUTION_URL" >> runtime.log
    
    # Download and extract the distribution package
    if curl -L -o target/fun-project-distribution.zip "$DISTRIBUTION_URL" 2>> runtime.log; then
        echo "Download successful. Extracting..."
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] Download successful, extracting..." >> runtime.log
        
        # Extract the distribution
        cd target
        if command -v unzip &> /dev/null; then
            unzip -q fun-project-distribution.zip 2>> ../runtime.log
            rm fun-project-distribution.zip
            cd ..
            
            # Verify the main jar exists
            if [[ -f "$jarfile" ]]; then
                echo "Successfully downloaded and extracted from GitHub Releases"
                echo "[$(date '+%Y-%m-%d %H:%M:%S')] Successfully extracted from GitHub Releases" >> runtime.log
                run_application
            else
                echo "Main jar not found in downloaded package. Falling back to local build."
                echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: Main jar not found after extraction, falling back to local build" >> runtime.log
                build_locally
            fi
        else
            echo "unzip is required but not installed. Falling back to local build."
            echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: unzip not found, falling back to local build" >> runtime.log
            cd ..
            build_locally
        fi
    else
        echo "Failed to download from GitHub Releases. Falling back to local build."
        echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR: Download failed, falling back to local build" >> runtime.log
        build_locally
    fi
}

# Detect current branch
current_branch=$(git branch --show-current 2>/dev/null || echo "unknown")

echo "[$(date '+%Y-%m-%d %H:%M:%S')] ========== Fun Project Startup ==========" >> runtime.log
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Current branch: $current_branch" >> runtime.log

if [[ -f "$jarfile" ]]; then
    echo "Found existing jar file"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] Found existing jar file" >> runtime.log
    run_application
elif [[ "$current_branch" == "main" ]]; then
    echo "On main branch - attempting to use GitHub Releases"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] On main branch - attempting GitHub Releases" >> runtime.log
    download_from_github_releases
else
    echo "On development branch ($current_branch) - building locally"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] On development branch ($current_branch) - building locally" >> runtime.log
    build_locally
fi