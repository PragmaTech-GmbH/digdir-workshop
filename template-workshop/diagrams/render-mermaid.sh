#!/bin/bash

# Create the generated directory if it doesn't exist
mkdir -p generated

# Install mermaid-cli if not already installed
if ! command -v mmdc &> /dev/null; then
    echo "Installing @mermaid-js/mermaid-cli..."
    npm install -g @mermaid-js/mermaid-cli
fi

# Process each Mermaid file
for mermaid_file in *.mmd; do
    # Extract the filename without path and extension
    filename=$(basename "$mermaid_file" .mmd)
    output_file="generated/${filename}.png"

    echo "Rendering $mermaid_file to $output_file..."
    mmdc -i "$mermaid_file" -o "$output_file" -b transparent
done

echo "All Mermaid diagrams have been rendered to the generated directory."
