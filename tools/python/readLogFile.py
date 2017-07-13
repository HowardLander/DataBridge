#!/usr/bin/env python
import sys
import subprocess
import signal

def printMsg(msgDict):
     print msgDict['name']
     print '\t','type: ',msgDict['type']
     print '\t','subtype: ',msgDict['subtype']
     for key,value in msgDict.items():
        if ((key != 'name') and (key != 'type') and (key != 'subtype')):
           print '\t',key,': ',value
     print ''

logString = sys.argv[1]
logFile = sys.argv[2]
targetMsg = ""
if (len(sys.argv) > 3):
   targetMsg = sys.argv[3]

output = subprocess.check_output(['zgrep', logString, logFile]) 
lines = output.split("\n")

for thisLine in lines:
  splitLine = thisLine.split("{")
  if (len(splitLine) > 1):
     thisMsg = splitLine[1]
     thisMsg = thisMsg[:-1]
  #  print thisMsg
     splitMsg = thisMsg.split(", ")
     msgDict = {}
     for thisSplitMsg in splitMsg:
  #     print thisSplitMsg
        keyValuePair = thisSplitMsg.split("=")
        msgDict[keyValuePair[0]] = keyValuePair[1]
     if ((targetMsg == "") or ((msgDict['name'].find(targetMsg)) != -1)):
        printMsg(msgDict)
