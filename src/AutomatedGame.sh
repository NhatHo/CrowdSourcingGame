#!/bin/bash

echo "======== START THE GAMESERVER, GAME ROOMS AND SOME PLAYERS ========="

gnome-terminal --tab --title="Server" -x ./AutomatedScripts/GameServer.sh
sleep 2s
gnome-terminal --tab --title="Player1" -x ./AutomatedScripts/Player1.sh
gnome-terminal --tab --title="Player2" -x ./AutomatedScripts/Player2.sh
gnome-terminal --tab --title="Player3" -x ./AutomatedScripts/Player3.sh
sleep 3s
gnome-terminal --tab --title="Player4" -x ./AutomatedScripts/Player4.sh
gnome-terminal --tab --title="Player5" -x ./AutomatedScripts/Player5.sh
gnome-terminal --tab --title="Player6" -x ./AutomatedScripts/Player6.sh
echo "======== FINISHED INSTANTIATING THE GAME SERVER AND PLAYER1 ========"
