#!/bin/bash

# Temporary clone GitHub repo
git clone --mirror https://github.com/Mooad/vid-processor.git temp_dir

# Change to the temporary directory
cd temp_dir

# Set CodeCommit as the push URL
git remote set-url --push origin https://git-codecommit.eu-west-3.amazonaws.com/v1/repos/vid-processor

# Push the changes
git push --mirror

# Clean up
cd ..
rm -rf temp_dir
arn:aws:lambda:eu-west-3:553035198032:layer:git:6
