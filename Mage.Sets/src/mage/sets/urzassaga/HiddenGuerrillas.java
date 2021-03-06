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
package mage.sets.urzassaga;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.TriggeredAbility;
import mage.abilities.common.SpellCastOpponentTriggeredAbility;
import mage.abilities.condition.common.SourceMatchesFilterCondition;
import mage.abilities.decorator.ConditionalTriggeredAbility;
import mage.abilities.effects.common.continuous.BecomesCreatureSourceEffect;
import mage.abilities.keyword.TrampleAbility;
import mage.cards.CardImpl;
import mage.constants.CardType;
import mage.constants.Duration;
import mage.constants.Rarity;
import mage.filter.common.FilterArtifactSpell;
import mage.filter.common.FilterEnchantmentPermanent;
import mage.filter.predicate.mageobject.CardTypePredicate;
import mage.game.permanent.token.Token;

/**
 *
 * @author LoneFox

 */
public class HiddenGuerrillas extends CardImpl {

    public HiddenGuerrillas(UUID ownerId) {
        super(ownerId, 261, "Hidden Guerrillas", Rarity.UNCOMMON, new CardType[]{CardType.ENCHANTMENT}, "{G}");
        this.expansionSetCode = "USG";

        // When an opponent casts an artifact spell, if Hidden Guerrillas is an enchantment, Hidden Guerrillas becomes a 5/3 Soldier creature with trample.
        TriggeredAbility ability = new SpellCastOpponentTriggeredAbility(new BecomesCreatureSourceEffect(new HiddenGuerrillasSoldier(), "", Duration.WhileOnBattlefield, true),
            new FilterArtifactSpell(), false);
        this.addAbility(new ConditionalTriggeredAbility(ability, new SourceMatchesFilterCondition(new FilterEnchantmentPermanent()),
            "When an opponent casts an artifact spell, if {this} is an enchantment, {this} becomes a 5/3 Soldier creature with trample."));
     }

    public HiddenGuerrillas(final HiddenGuerrillas card) {
        super(card);
    }

    @Override
    public HiddenGuerrillas copy() {
        return new HiddenGuerrillas(this);
    }
}

class HiddenGuerrillasSoldier extends Token {

    public HiddenGuerrillasSoldier() {
        super("Soldier", "a 5/3 Soldier creature with trample");
        cardType.add(CardType.CREATURE);
        subtype.add("Soldier");
        power = new MageInt(5);
        toughness = new MageInt(3);
        this.addAbility(TrampleAbility.getInstance());
    }
}
