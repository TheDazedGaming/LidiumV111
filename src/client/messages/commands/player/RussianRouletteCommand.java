/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client.messages.commands.player;

import client.MapleCharacter;
import client.MapleClient;
import client.messages.Command;
import server.Randomizer;
import tools.packet.CWvsContext;

/**
 *
 * @author Alex
 */
public class RussianRouletteCommand extends Command{
    
    {
        setDescription("Roll the dice for a winner!");
    }

    @Override
    public void execute(MapleClient c, String[] splitted){
        if (splitted.length < 2) {
            c.getPlayer().dropMessage(5, "@roll <number>");
        } else {
        c.getPlayer().getMap().broadcastMessage(CWvsContext.serverNotice(5, "[Russian Roulette] Number: " + Randomizer.rand(1, Integer.parseInt(splitted[1]))));
    }
}
}