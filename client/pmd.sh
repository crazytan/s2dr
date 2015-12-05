#!/usr/bin/env bash
pmd pmd -d src -f html -R java-basic,java-design,java-sunsecure > codeAnalysis.html
