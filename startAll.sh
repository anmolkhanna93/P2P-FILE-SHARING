#!/usr/bin/env bash

rm -f *log
rm -rf peer_1001
rm -rf peer_1003
rm -rf peer_1004
rm -rf peer_1005
rm -rf peer_1006

dt=$(date '+%d-%m-%Y_%H:%M:%S')
mkdir -p ../WorkingCode/${dt}/
cp -R . ../WorkingCode/${dt}/

mvn clean install
mvn package

mvn exec:java -f pom.xml  -Dexec.mainClass="org.ufl.cise.cn.peerProcess" -Dexec.args="1001" &
mvn exec:java -f pom.xml  -Dexec.mainClass="org.ufl.cise.cn.peerProcess" -Dexec.args="1002" &
mvn exec:java -f pom.xml  -Dexec.mainClass="org.ufl.cise.cn.peerProcess" -Dexec.args="1003" &
mvn exec:java -f pom.xml  -Dexec.mainClass="org.ufl.cise.cn.peerProcess" -Dexec.args="1004" &
mvn exec:java -f pom.xml  -Dexec.mainClass="org.ufl.cise.cn.peerProcess" -Dexec.args="1005" &
mvn exec:java -f pom.xml  -Dexec.mainClass="org.ufl.cise.cn.peerProcess" -Dexec.args="1006" 

find . -name ImageFile.jpg -type f
