#!/bin/bash

echo "======== START THE GAMESERVER, GAME ROOMS AND SOME PLAYERS ========="
echo "Spawning GameServer"
gnome-terminal --tab --title="Server" -x ./AutomatedScripts/GameServer.sh
sleep 2s
echo "TESTCASE1: 2 players start the game, 2 other join in and finish the game"
gnome-terminal --tab --title="Player1" -x ./AutomatedScripts/Player1.sh
gnome-terminal --tab --title="Player2" -x ./AutomatedScripts/Player2.sh
gnome-terminal --tab --title="Player3" -x ./AutomatedScripts/Player3.sh
gnome-terminal --tab --title="Player4" -x ./AutomatedScripts/Player4.sh

sleep 60s
echo "TESTCASE2: 2 players start the game, 3rd player join in, 1st and 2nd players quit, 2nd re-join and finish the game"
gnome-terminal --tab --title="Player5" -x ./AutomatedScripts/Player5.sh
gnome-terminal --tab --title="Player6" -x ./AutomatedScripts/Player6.sh
gnome-terminal --tab --title="Player7" -x ./AutomatedScripts/Player7.sh

sleep 45s
echo "TESTCASE3: (Stress test) First 3 players spawn 3 games (1 wait in the waiting queue)"
echo "2 players will join the 1st game, 1 player will join the 2nd game."
echo "The 2nd game will finish first and let the 3rd creator creates a game" 
echo "1 player will join the new game and finish it"

gnome-terminal --tab --title="Player8" -x ./AutomatedScripts/Player8.sh
gnome-terminal --tab --title="Player9" -x ./AutomatedScripts/Player9.sh
gnome-terminal --tab --title="Player10" -x ./AutomatedScripts/Player10.sh
gnome-terminal --tab --title="Player11" -x ./AutomatedScripts/Player11.sh
gnome-terminal --tab --title="Player14" -x ./AutomatedScripts/Player14.sh
gnome-terminal --tab --title="Player12" -x ./AutomatedScripts/Player12.sh
gnome-terminal --tab --title="Player13" -x ./AutomatedScripts/Player13.sh
echo "======== FINISHED INSTANTIATING THE GAME SERVER AND PLAYER1 ========"
sleep 60s
