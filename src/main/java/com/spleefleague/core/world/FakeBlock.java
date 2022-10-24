/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.spleefleague.core.world;

import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;

/**
 * @author NickM13
 */
public record FakeBlock(BlockData blockData) {

    public Sound getStepSound() {
        return blockData.getSoundGroup().getStepSound();
    }

    public Sound getBreakSound() {
        return blockData.getSoundGroup().getBreakSound();
    }

    public Sound getPlaceSound() {
        return blockData.getSoundGroup().getPlaceSound();
    }

    public Sound getHitSound() {
        return blockData.getSoundGroup().getHitSound();
    }

    public Sound getFallSound() {
        return blockData.getSoundGroup().getFallSound();
    }

}
