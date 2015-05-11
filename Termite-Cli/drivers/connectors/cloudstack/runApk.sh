#!/bin/bash

sshpass -p$1 ssh -o StrictHostKeyChecking=no root@$2 "~/runApk.sh $3"
