/*
* Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
*
* Redistribution and use in source and binary forms, with or without modification, are
* permitted provided that the following conditions are met:
*
*    1. Redistributions of source code must retain the above copyright notice, this list of
*       conditions and the following disclaimer.
*
*    2. Redistributions in binary form must reproduce the above copyright notice, this list
*       of conditions and the following disclaimer in the documentation and/or other materials
*       provided with the distribution.
*
* THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
* FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
* ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* The views and conclusions contained in the software and documentation are those of the
* authors and should not be interpreted as representing official policies, either expressed
* or implied, of BetaSteward_at_googlemail.com.
*/

package mage.abilities.effects.common;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.Mode;
import mage.abilities.effects.OneShotEffect;
import mage.constants.Outcome;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.Target;
import mage.target.targetpointer.FirstTargetPointer;
import mage.util.CardUtil;

/**
 *
 * @author BetaSteward_at_googlemail.com
 */
public class DestroyTargetEffect extends OneShotEffect<DestroyTargetEffect> {

    protected boolean noRegen;

    public DestroyTargetEffect() {
        this(false);
    }

    public DestroyTargetEffect(String ruleText) {
        this(false);
        staticText = ruleText;
    }

    public DestroyTargetEffect(boolean noRegen) {
        super(Outcome.DestroyPermanent);
        this.noRegen = noRegen;
    }

    public DestroyTargetEffect(final DestroyTargetEffect effect) {
        super(effect);
        this.noRegen = effect.noRegen;
    }

    @Override
    public DestroyTargetEffect copy() {
        return new DestroyTargetEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        int affectedTargets = 0;
        if (source.getTargets().size() > 1 && targetPointer instanceof FirstTargetPointer) { // for Rain of Thorns
            for (Target target : source.getTargets()) {
                for (UUID permanentId : target.getTargets()) {
                    Permanent permanent = game.getPermanent(permanentId);
                    if (permanent != null) {
                        permanent.destroy(source.getId(), game, noRegen);
                        affectedTargets++;
                    }
                }
            }
        }
        else if (targetPointer.getTargets(game, source).size() > 0) {
            for (UUID permanentId : targetPointer.getTargets(game, source)) {
                Permanent permanent = game.getPermanent(permanentId);
                if (permanent != null) {
                    permanent.destroy(source.getId(), game, noRegen);
                    affectedTargets++;
                }
            }
        }
        return affectedTargets > 0;
    }

    @Override
    public String getText(Mode mode) {
        if (staticText != null && !staticText.isEmpty()) {
            return staticText;
        }
        StringBuilder sb = new StringBuilder();
        if (mode.getTargets().size() == 0) {
            sb.append("destroy that creature"); //TODO add possibility to specify text with targetPointer usage
        } else if (mode.getTargets().get(0).getNumberOfTargets() == 1) {
            sb.append("Destroy target ").append(mode.getTargets().get(0).getTargetName());
        } else {
            sb.append("Destroy ").append(CardUtil.numberToText(mode.getTargets().get(0).getNumberOfTargets())).append(" target ").append(mode.getTargets().get(0).getTargetName());
        }
        if (noRegen) {
            sb.append(". It can't be regenerated");
        }
        return sb.toString();
    }

}
