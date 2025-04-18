#!/bin/sh
# REPORT_NAME=report-image
REPORT_NAME=ghcr.io/quarto-dev/quarto:1.7.19
# podman build ./doc/misc/ --tag $REPORT_NAME
cd $(dirname $0)/../
podman run -it -e TYPST_FONT_PATHS=/root/doc/report -v ./:/root/ -w=/root/doc/report $REPORT_NAME quarto render; 
