#!/bin/bash

password=$1
server=$2
apkPath=$3
apkName=$4

sshpass -p$password scp -o StrictHostKeyChecking=no $apkPath root@$server:~
sshpass -p$password ssh -o StrictHostKeyChecking=no root@$server "~/installApk.sh $apkName" 
