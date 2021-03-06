/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.sets.tempestremastered;

import java.util.UUID;
import mage.abilities.Ability;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.TapTargetEffect;
import mage.cards.CardImpl;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.Rarity;
import mage.filter.common.FilterCreaturePermanent;
import mage.game.Game;
import mage.game.permanent.Permanent;
import mage.target.common.TargetCreaturePermanent;

/**
 *
 * @author fireshoes
 */
public class Deadshot extends CardImpl {
    
    private static final FilterCreaturePermanent filter = new FilterCreaturePermanent("another target creature");

    public Deadshot(UUID ownerId) {
        super(ownerId, 129, "Deadshot", Rarity.UNCOMMON, new CardType[]{CardType.SORCERY}, "{3}{R}");
        this.expansionSetCode = "TPR";

        // Tap target creature.
        this.getSpellAbility().addEffect(new TapTargetEffect());
        this.getSpellAbility().addTarget(new TargetCreaturePermanent());
        
        // It deals damage equal to its power to another target creature.
        this.getSpellAbility().addEffect(new DeadshotDamageEffect());
        this.getSpellAbility().addTarget(new DeadshotTargetCreaturePermanent(filter));
    }

    public Deadshot(final Deadshot card) {
        super(card);
    }

    @Override
    public Deadshot copy() {
        return new Deadshot(this);
    }
}

class DeadshotDamageEffect extends OneShotEffect {

    public DeadshotDamageEffect() {
        super(Outcome.Damage);
        this.staticText = "Target creature deals damage equal to its power to another target creature";
    }

    public DeadshotDamageEffect(final DeadshotDamageEffect effect) {
        super(effect);
    }

    @Override
    public DeadshotDamageEffect copy() {
        return new DeadshotDamageEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Permanent ownCreature = game.getPermanent(source.getFirstTarget());
        if (ownCreature != null) {
            int damage = ownCreature.getPower().getValue();
            Permanent targetCreature = game.getPermanent(source.getTargets().get(1).getFirstTarget());
            if (targetCreature != null) {
                targetCreature.damage(damage, ownCreature.getId(), game, false, true);
                return true;
            }
        }
        return false;
    }
}

class DeadshotTargetCreaturePermanent extends TargetCreaturePermanent {

    public DeadshotTargetCreaturePermanent(FilterCreaturePermanent filter) {
        super(filter);
    }

    @Override
    public boolean canTarget(UUID id, Ability source, Game game) {
        if (source.getTargets().getFirstTarget().equals(id)) {
            return false;
        }
        return super.canTarget(id, source, game);
    }

    @Override
    public boolean canTarget(UUID controllerId, UUID id, Ability source, Game game) {
        if (source.getTargets().getFirstTarget().equals(id)) {
            return false;
        }
        return super.canTarget(controllerId, id, source, game);
    }

}