#!/bin/bash

sbt "~run -Dhttp.port=22222 $*"
