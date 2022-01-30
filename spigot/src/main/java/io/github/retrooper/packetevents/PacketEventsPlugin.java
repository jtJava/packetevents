/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2021 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.retrooper.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.particle.Particle;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientChatMessage;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.utils.SpigotDataHelper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PacketEventsPlugin extends JavaPlugin {
    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        //Register your listeners
        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getSettings().debug(false).bStats(true);
        SimplePacketListenerAbstract listener = new SimplePacketListenerAbstract() {
            @Override
            public void onPacketPlayReceive(PacketPlayReceiveEvent event) {
                User user = event.getUser();
                switch (event.getPacketType()) {
                    case CHAT_MESSAGE:
                        WrapperPlayClientChatMessage chatMessage = new WrapperPlayClientChatMessage(event);
                        if (chatMessage.getMessage().equalsIgnoreCase("plzparticles")) {
                            Particle particle = new Particle(ParticleTypes.ANGRY_VILLAGER);
                            Vector3d position = SpigotDataHelper
                                    .fromBukkitLocation(((Player) event.getPlayer()).getLocation())
                                    .getPosition().add(0, 2, 0);
                            WrapperPlayServerParticle particlePacket
                                    = new WrapperPlayServerParticle(particle, true, position,
                                    new Vector3f(0.4f, 0.4f, 0.4f), 0, 25);
                            user.sendPacket(particlePacket);
                        }
                        break;
                    case PLAYER_FLYING:
                    case PLAYER_POSITION:
                    case PLAYER_POSITION_AND_ROTATION:
                    case PLAYER_ROTATION:
                        WrapperPlayClientPlayerFlying flying = new WrapperPlayClientPlayerFlying(event);
                        Location location = flying.getLocation();
                        break;
                    case PLAYER_DIGGING:
                        WrapperPlayClientPlayerDigging digging = new WrapperPlayClientPlayerDigging(event);
                        DiggingAction action = digging.getAction();
                        break;
                    case PLAYER_BLOCK_PLACEMENT:
                        WrapperPlayClientPlayerBlockPlacement blockPlacement = new WrapperPlayClientPlayerBlockPlacement(event);
                        BlockFace face = blockPlacement.getFace();
                        Vector3i bp = blockPlacement.getBlockPosition();
                        //user.sendMessage("Face: " + face + ", bp: " + bp);
                        break;
                }
            }

            @Override
            public void onPacketPlaySend(PacketPlaySendEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.JOIN_GAME) {
                    if (event.getPlayer() instanceof Player) {
                        Player player = (Player) event.getPlayer();
                        player.sendMessage("Hello " + player.getName());
                    } else {
                        event.getUser().sendMessage("player null, but hey!");
                    }
                }
            }
        };
        //net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles w1;
        //PacketEvents.getAPI().getEventManager().registerListener(listener);
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }
}