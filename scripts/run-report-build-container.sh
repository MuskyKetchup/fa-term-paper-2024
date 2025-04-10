#!/bin/sh
# REPORT_NAME=report-image
REPORT_NAME=ghcr.io/quarto-dev/quarto:1.7.19
# podman build ./doc/misc/ --tag $REPORT_NAME
podman run -it -e TYPST_FONT_PATHS=./ -v ./doc/report/:/root/ -w=/root/ $REPORT_NAME quarto render; rm ./.bash_history
