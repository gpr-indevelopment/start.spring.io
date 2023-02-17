#!/bin/sh
strace -T -f -e trace=file -p 1 2>&1 | grep "/tmp/" > strace-log.txt