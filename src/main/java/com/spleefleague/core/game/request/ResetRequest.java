package com.spleefleague.core.game.request;

import com.spleefleague.core.game.battle.Battle;
import com.spleefleague.core.player.CorePlayer;

import javax.annotation.Nullable;

/**
 * @author NickM13
 * @since 4/27/2020
 */
public class ResetRequest extends BattleRequest {

    public ResetRequest(Battle<?> battle) {
        super(battle, "reset", false, 1D);
    }

    @Override
    public boolean attemptStartRequest(CorePlayer cp, int total, @Nullable String requestValue) {
        chatName = "reset the field";
        scoreboardName = "Reset Field";
        return true;
    }

    @Override
    protected void meetsRequirement() {
        battle.reset();
    }

}
