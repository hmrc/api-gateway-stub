#!/bin/bash

sbt "~run -Dhttp.port=9763 $*"
