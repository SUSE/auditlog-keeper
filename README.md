
# Auditlog keeper


## How to build

The buildsystem is ant + ivy. Running ant will try to resolve the dependencies
from the internet. If retrieving them fail, the dependencies are searched
in ext/, so if you are building offline (eg. rpm), symlink or copy the jars there.



