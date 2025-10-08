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
signature="--signature"
keep_alive="--keep-alive"  # Default value for keep-alive
end_time=""

# Parse optional arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --signature)
      signature="--signature"
      shift # past argument
      ;;
    --keep-alive)
      keep_alive="--keep-alive"
      shift # past argument
      ;;
    --end-time|-e)
      end_time="--end-time $2"
      shift # past argument
      shift # past value
      ;;
    --resume-next-day|-r)
      resume_next_day="--resume-next-day"
      shift # past argument
      ;;
    *)    # unknown option
      shift # past argument
      ;;
  esac
done

# Function to run the application
run_application() {
    echo "Running fun-project.jar"
    java -cp "$libFolder:$pluginFolder:$jarfile" com.aldrineeinsteen.fun.Main $signature $keep_alive $end_time $resume_next_day
}

# Function to build locally
build_locally() {
    echo "Building project locally with Maven..."
    ./mvnw clean install
    if [[ -f "$jarfile" ]]; then
        run_application
    else
        echo "Error: Failed to build jar file locally"
        exit 1
    fi
}

# Function to download from GitHub Releases
download_from_github_releases() {
    echo "Attempting to download latest release from GitHub Releases..."
    
    # Create target directories
    mkdir -p target/lib target/plugins
    
    # GitHub repository details
    REPO="aldrineeinsteen/fun-project"
    
    # Check if curl is available
    if ! command -v curl &> /dev/null; then
        echo "curl is required but not installed. Falling back to local build."
        build_locally
        return
    fi
    
    # Get the latest release information
    echo "Fetching latest release information..."
    RELEASE_INFO=$(curl -s "https://api.github.com/repos/$REPO/releases/latest")
    
    if echo "$RELEASE_INFO" | grep -q "Not Found"; then
        echo "No releases found. Falling back to local build."
        build_locally
        return
    fi
    
    # Extract download URL for the distribution zip
    DISTRIBUTION_URL=$(echo "$RELEASE_INFO" | grep -o '"browser_download_url": "[^"]*fun-project-distribution.zip"' | cut -d'"' -f4)
    
    if [[ -z "$DISTRIBUTION_URL" ]]; then
        echo "Distribution package not found in latest release. Falling back to local build."
        build_locally
        return
    fi
    
    echo "Downloading distribution package from: $DISTRIBUTION_URL"
    
    # Download and extract the distribution package
    if curl -L -o target/fun-project-distribution.zip "$DISTRIBUTION_URL"; then
        echo "Download successful. Extracting..."
        
        # Extract the distribution
        cd target
        if command -v unzip &> /dev/null; then
            unzip -q fun-project-distribution.zip
            rm fun-project-distribution.zip
            cd ..
            
            # Verify the main jar exists
            if [[ -f "$jarfile" ]]; then
                echo "Successfully downloaded and extracted from GitHub Releases"
                run_application
            else
                echo "Main jar not found in downloaded package. Falling back to local build."
                build_locally
            fi
        else
            echo "unzip is required but not installed. Falling back to local build."
            cd ..
            build_locally
        fi
    else
        echo "Failed to download from GitHub Releases. Falling back to local build."
        build_locally
    fi
}

# Detect current branch
current_branch=$(git branch --show-current 2>/dev/null || echo "unknown")

if [[ -f "$jarfile" ]]; then
    echo "Found existing jar file"
    run_application
elif [[ "$current_branch" == "main" ]]; then
    echo "On main branch - attempting to use GitHub Releases"
    download_from_github_releases
else
    echo "On development branch ($current_branch) - building locally"
    build_locally
fi