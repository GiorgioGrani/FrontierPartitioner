#!/bin/bash
CPLEX_BIN_DIR=/opt/ibm/ILOG/CPLEX_Studio1271/cplex/bin/x86-64_linux
INPUT_DIR=/home/giorgiograni/Downloads/BolandCode/ip/instances/AP
JAR=/home/giorgiograni/IdeaProjects/FrontierPartitioner/out/artifacts/FrontierPartitioner_jar/FrontierPartitioner.jar
OUTPUTFOLDER=/home/giorgiograni/IdeaProjects/FrontierPartitioner/output2305
#!CONF_DIR=conf
#!LOG_DIR=log
output_path=report.csv
for d in $INPUT_DIR/*; do
    for e in $d/*; do
		  fromname=${d##*/}
		  toname=${e##*/} 
          echo  "Processing  $fromname...$toname"
          java   -Djava.library.path=$CPLEX_BIN_DIR -jar $JAR ap $e $toname $OUTPUTFOLDER false 8  
	done
done
