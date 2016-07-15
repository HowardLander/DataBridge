#!/bin/bash
# pick a java
JAVA=/usr/bin/java
NAME=databridge
HOSTNAME=`echo $HOSTNAME | cut -d "." -f 1`

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

batch_pid_file=${DATABRIDGE_RUN_DIR}/batch.${HOSTNAME}.pid

batch_stdout_file=${DATABRIDGE_LOG_DIR}/batch.${HOSTNAME}.log

batch_stderr_file=${DATABRIDGE_LOG_DIR}/batch.${HOSTNAME}.log

get_batch_pid() {
   cat ${batch_pid_file}
}

log() {
    printf '%s\n' "$@" >> debug.log
}

is_batch_running_orig() {
    [ -f "$batch_pid_file" ] && ps `get_batch_pid` > /dev/null 2>&1
}

test_pid() {
    ps_out=`ps -ef | grep $1 | grep -v 'grep'`
    #result=$(echo $ps_out | grep "$1")
    if [[ !  -z  $ps_out ]];then
        return 0
    else
        return 1
    fi
}

is_batch_running() {
    [ -f "$batch_pid_file" ] && test_pid `get_batch_pid` > /dev/null 2>&1
}

is_running() {
    is_batch_running
}
# Find the config file
DATABRIDGE_CONFIG_FILE=${DATABRIDGE_CONFIG_DIR}/DataBridge.conf

# include the classpath files for the various servers.
if [ -f ${DATABRIDGE_CONFIG_DIR}/batchEngineClasspath.txt ]
then
   rawBatchFile=${DATABRIDGE_CONFIG_DIR}/batchEngineClasspath.txt
   rawBatchPath=$(head -1 $rawBatchFile)
   BATCH_ENGINE_CLASS_PATH=${rawBatchPath}:$DATABRIDGE_SYSTEM_JARS/*:$DATABRIDGE_CONTRIBUTED_JARS/*
else
   echo "Can't find batchEngineClasspath.txt" 
   echo "Try mvn dependency:build-classpath -Dmdep.outputFile=batchEngineClasspath.txt"
   echo "in engines/batch"
fi

case "$1" in
    start)
    if is_batch_running; then
        echo "Batch engine already running"
    else
        echo "Starting batch engine"
        cd "$DATABRIDGE_BIN"
        echo "Batch engine startup at "`date -u` >> ${batch_stdout_file}
        $JAVA -cp ${BATCH_ENGINE_CLASS_PATH} org.renci.databridge.engines.batch.BatchEngine ${DATABRIDGE_CONFIG_DIR}/DataBridge.conf >> "$batch_stdout_file" 2>> "$batch_stderr_file" &
        echo $! > "$batch_pid_file"
        if ! is_batch_running; then
            echo "Unable to start batch engine, see $batch_stdout_file and $batch_stderr_file"
            exit 1
        fi
    fi
    ;;
    stop)
    # Stop the batch engine
    if is_batch_running; then
        echo -n "Stopping batch engine.."
        kill `get_batch_pid`
        for i in {1..10}
        do
            if ! is_batch_running; then
                break
            fi

            echo -n "."
            sleep 1
        done
        echo

        if is_batch_running; then
            echo "Not stopped; may still be shutting down or shutdown may have failed"
            exit 1
        else
            echo "Stopped"
            if [ -f "$batch_pid_file" ]; then
                rm -f "$batch_pid_file"
            fi
        fi
    else
        echo "Batch engine not running"
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
        echo "Batch engine running"
    else
        echo "Batch engine stopped"
    fi
    ;;
    *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
    ;;
esac

exit 0
