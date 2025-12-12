#!/bin/bash

export PATH="/opt/homebrew/opt/openjdk@21/bin:/opt/homebrew/bin:$PATH"
echo "Starting Reservo Client..."
cd client
mvn javafx:run

