#!/bin/bash

echo "-------- SETTING UP THE ENVIRONMENT ---------"

javac BufferManager.java
javac GameController.java
javac GameDB.java
javac GameQueue.java
javac GameServer.java
javac Merger.java
javac Player.java
javac PlayerAutomated.java
javac UserInterface.java
javac WordList.java

pwd > config.txt

chmod 744 -R ../src/*

echo "-------- SUCCESSFULLY SETTING UP THE ENVIRONMENT ---------"
