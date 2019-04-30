#!/bin/bash
set -e

java -cp script-deployer.jar -Dloader.path="lib" org.springframework.boot.loader.PropertiesLauncher