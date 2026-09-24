#!/bin/sh
# Local fallback: install Gradle 8.10.2 or use the GitHub Actions workflow.
echo "Este projeto usa Gradle 8.10.2. No GitHub Actions, o Gradle e configurado automaticamente."
echo "Para compilar localmente, instale Gradle 8.10.2 e execute: gradle build"
exit 1
