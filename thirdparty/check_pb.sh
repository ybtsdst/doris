#!/bin/bash


if [ $# -ne 1 ]; then
    echo "Usage: $0 <directory>"
    exit 1
fi

LIB_DIR="$1"

if [ ! -d "$LIB_DIR" ]; then
    echo "Directory $LIB_DIR does not exist."
    exit 1
fi

echo "Scanning directory: $LIB_DIR for static libraries linking protobuf..."

for lib in "$LIB_DIR"/*.a; do
    [ -e "$lib" ] || continue
    count=$(nm -C "$lib" 2>/dev/null | grep -c 'google::protobuf')
    if [ "$count" -gt 0 ]; then
        echo "Library: $lib"
        echo "  Found $count protobuf symbols:"
        nm -C "$lib" | grep 'google::protobuf' | head -20
        echo "  ... (truncated if more)"
        echo "----------------------------------------"
    fi
done
