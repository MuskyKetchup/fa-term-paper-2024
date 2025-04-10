#!/bin/sh
REPORT_NAME=report-image
podman build ./doc/misc/ --tag $REPORT_NAME
podman run -it -v  ./doc/report/:/root/ -w=/root/ $REPORT_NAME quarto render; rm ./.bash_history
