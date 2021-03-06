#!/bin/bash
# pick a java
JAVA=/usr/bin/java
NAME=databridge

# Gets the directory of the script
SCRIPT_HOME="${BASH_SOURCE[0]}";
if ([ -h "${SCRIPT_HOME}" ]) then
  while([ -h "${SCRIPT_HOME}" ]) do SCRIPT_HOME=`readlink "${SCRIPT_HOME}"`; done
fi
pushd . > /dev/null
cd `dirname ${SCRIPT_HOME}` > /dev/null
SCRIPT_HOME=`pwd`;
popd  > /dev/null

# DATABRIDGE_HOME is the next directory above this script
DATABRIDGE_HOME="${SCRIPT_HOME%/*}"

# The Databridge bin directory
DATABRIDGE_BIN=${DATABRIDGE_HOME}/bin/

# System jars have to be here
DATABRIDGE_SYSTEM_JARS=${DATABRIDGE_HOME}/bin/system

# Contributed jars have to be here
DATABRIDGE_CONTRIBUTED_JARS=${DATABRIDGE_HOME}/bin/contrib

# Get the DATABRIDGE config dir
DATABRIDGE_CONFIG_DIR=${DATABRIDGE_HOME}/config

# Get the DATABRIDGE run dir
DATABRIDGE_RUN_DIR=${DATABRIDGE_HOME}/run

# Get the DATABRIDGE log dir
DATABRIDGE_LOG_DIR=${DATABRIDGE_HOME}/log

batch_worker_pid_file=${DATABRIDGE_RUN_DIR}/batchWorker.pid

batch_worker_stdout_file=${DATABRIDGE_LOG_DIR}/batchWorker.log

batch_worker_stderr_file=${DATABRIDGE_LOG_DIR}/batchWorker.err

get_batch_pid() {
  sed -n "${1}{p;q;}"  ${batch_worker_pid_file}
}

is_batch_running() {
    [ -f "$batch_worker_pid_file" ] && ps `get_batch_pid $1` > /dev/null 2>&1
}


is_running() {
    # For now we just check for the first worker.
    is_batch_running 0
}
# Find the config file
DATABRIDGE_CONFIG_FILE=${DATABRIDGE_CONFIG_DIR}/DataBridge.conf

# include the classpath files for the various servers.
if [ -f ${DATABRIDGE_CONFIG_DIR}/batchEngineWorkerClasspath.txt ]
then
   rawBatchFile=${DATABRIDGE_CONFIG_DIR}/batchEngineWorkerClasspath.txt
   rawBatchPath=$(head -1 $rawBatchFile)
   BATCH_WORKER_CLASS_PATH=${rawBatchPath}:$DATABRIDGE_SYSTEM_JARS/*:$DATABRIDGE_CONTRIBUTED_JARS/*
else
   echo "Can't find batchEngineWorkerClasspath.txt" 
   echo "Try mvn dependency:build-classpath -Dmdep.outputFile=batchEngineWorkerClasspath.txt"
   echo "in engines/batch"
fi

case "$1" in
    start)
    if is_batch_running; then
        echo "Batch workers already running"
    else
        echo "Starting $2 batch worker(s)"
        cd "$DATABRIDGE_BIN"
        COUNTER=0
        OFFSET=1
        if [ -f "$batch_worker_pid_file" ]; then
           /bin/rm $batch_worker_pid_file
        fi
        while [[ $COUNTER -lt $2 ]]; do
            echo "Batch worker startup at "`date -u` >> ${batch_worker_stdout_file}.$COUNTER
            $JAVA -cp ${BATCH_WORKER_CLASS_PATH} org.renci.databridge.engines.batch.BatchWorker ${DATABRIDGE_CONFIG_DIR}/DataBridge.conf >> "$batch_worker_stdout_file.$COUNTER" 2>> "$batch_worker_stderr_file.$COUNTER" &
            echo $! >> "$batch_worker_pid_file"
            if ! is_batch_running $OFFSET; then
                echo "Unable to start worker, see $batch_worker_stdout_file.$COUNTER and $batch_worker_stderr_file.$COUNTER"
                exit 1
            fi
            let OFFSET=OFFSET+1
            let COUNTER=COUNTER+1
        done
    fi
    ;;
    stop)
    # Stop the batch engine
    if is_batch_running 1; then
        nWorkers=`wc -l $batch_worker_pid_file | awk '{print $1}'`
        echo "Stopping $nWorkers batch worker(s)..."
        COUNTER=0
        OFFSET=1
        while [[ $COUNTER -lt $nWorkers ]]; do
            echo -n "killing worker process `get_batch_pid $OFFSET`"
            kill `get_batch_pid $OFFSET`
            for i in {1..10}
            do
                if ! is_batch_running $OFFSET; then
                    echo
                    break
                fi
    
                echo -n "."
                sleep 1
            done

            if is_batch_running $OFFSET; then
                echo "Not stopped; may still be shutting down or shutdown may have failed"
                exit 1
            else
                if [[ $OFFSET -eq ${nWorkers} ]]; then
                    echo "Stopped"
                    if [ -f "$batch_worker_pid_file" ]; then
                        rm -f "$batch_worker_pid_file"
                    fi
                fi
            fi
            let OFFSET=OFFSET+1
            let COUNTER=COUNTER+1
        done
    else
        echo "Batch worker not running"
    fi

    ;;
    restart)
    $0 stop
    if is_running; then
        echo "Unable to stop, will not attempt to start"
        exit 1
    fi
    $0 start
    ;;
    status)
    if is_batch_running; then
        nWorkers=`wc -l $batch_worker_pid_file | awk '{print $1}'`
        echo "$nWorkers batch worker(s) running"
    else
        echo "Batch worker stopped"
    fi
    ;;
    *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac

exit 0
