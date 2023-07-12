package com.gempukku.lotro.game.timing;

import com.gempukku.lotro.common.Filterable;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.filters.Filters;
import com.gempukku.lotro.cards.PhysicalCard;
import com.gempukku.lotro.game.DefaultGame;
import com.gempukku.lotro.game.effects.*;
import com.gempukku.lotro.game.timing.results.*;

public class TriggerConditions {
    public static boolean losesInitiative(EffectResult effectResult, Side side) {
        if (effectResult.getType() == EffectResult.Type.INITIATIVE_CHANGE) {
            InitiativeChangeResult initiativeChangeResult = (InitiativeChangeResult) effectResult;
            if (initiativeChangeResult.getSide() != side)
                return true;
        }
        return false;
    }

    public static boolean takenControlOfASite(EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.TAKE_CONTROL_OF_SITE) {
            TakeControlOfSiteResult takeResult = (TakeControlOfSiteResult) effectResult;
            return takeResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean startOfPhase(DefaultGame game, EffectResult effectResult, Phase phase) {
        return (effectResult.getType() == EffectResult.Type.START_OF_PHASE
                && game.getGameState().getCurrentPhase() == phase);
    }

    public static boolean endOfPhase(DefaultGame game, EffectResult effectResult, Phase phase) {
        return (effectResult.getType() == EffectResult.Type.END_OF_PHASE
                && (game.getGameState().getCurrentPhase() == phase || phase == null));
    }

    public static boolean startOfTurn(DefaultGame game, EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.START_OF_TURN;
    }

    public static boolean endOfTurn(DefaultGame game, EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.END_OF_TURN;
    }

    public static boolean reconciles(DefaultGame game, EffectResult effectResult, String playerId) {
        return effectResult.getType() == EffectResult.Type.RECONCILE && (playerId == null || ((ReconcileResult) effectResult).getPlayerId().equals(playerId));
    }

    public static boolean winsSkirmish(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.CHARACTER_WON_SKIRMISH) {
            CharacterWonSkirmishResult wonResult = (CharacterWonSkirmishResult) effectResult;
            return Filters.and(filters).accepts(game, wonResult.getWinner());
        }
        return false;
    }

    public static boolean winsSkirmishInvolving(DefaultGame game, EffectResult effectResult, Filterable winnerFilter, Filterable involvingFilter) {
        if (effectResult.getType() == EffectResult.Type.CHARACTER_WON_SKIRMISH) {
            CharacterWonSkirmishResult wonResult = (CharacterWonSkirmishResult) effectResult;
            return Filters.and(winnerFilter).accepts(game, wonResult.getWinner())
                    && Filters.filter(wonResult.getInvolving(), game, involvingFilter).size() > 0;
        }
        return false;
    }

    public static boolean losesSkirmish(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.CHARACTER_LOST_SKIRMISH) {
            CharacterLostSkirmishResult wonResult = (CharacterLostSkirmishResult) effectResult;
            return Filters.and(filters).accepts(game, wonResult.getLoser());
        }
        return false;
    }

    public static boolean losesSkirmishInvolving(DefaultGame game, EffectResult effectResult, Filterable loserFilter, Filterable involvingFilter) {
        if (effectResult.getType() == EffectResult.Type.CHARACTER_LOST_SKIRMISH) {
            CharacterLostSkirmishResult wonResult = (CharacterLostSkirmishResult) effectResult;
            return Filters.and(loserFilter).accepts(game, wonResult.getLoser())
                    && Filters.filter(wonResult.getInvolving(), game, involvingFilter).size() > 0;
        }
        return false;
    }

    public static boolean sidePlayerAddedBurden(DefaultGame game, EffectResult effectResult, Side side) {
        if (effectResult.getType() == EffectResult.Type.ADD_BURDEN) {
            AddBurdenResult burdenResult = (AddBurdenResult) effectResult;
            String fpPlayer = game.getGameState().getCurrentPlayerId();
            if ((side == Side.FREE_PEOPLE && fpPlayer.equals(burdenResult.getPerformingPlayer()))
                    || (side == Side.SHADOW && !fpPlayer.equals(burdenResult.getPerformingPlayer())))
                return true;
            else
                return false;
        }
        return false;
    }

    public static boolean addedBurden(DefaultGame game, EffectResult effectResult, Filterable... sourceFilters) {
        if (effectResult.getType() == EffectResult.Type.ADD_BURDEN) {
            AddBurdenResult burdenResult = (AddBurdenResult) effectResult;
            return (Filters.and(sourceFilters).accepts(game, burdenResult.getSource()));
        }
        return false;
    }

    public static boolean removedBurden(DefaultGame game, EffectResult effectResult, Filterable... sourceFilters) {
        if (effectResult.getType() == EffectResult.Type.REMOVE_BURDEN) {
            RemoveBurdenResult burdenResult = (RemoveBurdenResult) effectResult;
            return (Filters.and(sourceFilters).accepts(game, burdenResult.getSource()));
        }
        return false;
    }

    public static boolean addedThreat(DefaultGame game, EffectResult effectResult, Filterable... sourceFilters) {
        if (effectResult.getType() == EffectResult.Type.ADD_THREAT) {
            AddThreatResult threatResult = (AddThreatResult) effectResult;
            return (Filters.and(sourceFilters).accepts(game, threatResult.getSource()));
        }
        return false;
    }

    public static boolean assignedAgainst(DefaultGame game, EffectResult effectResult, Side side, Filterable againstFilter, Filterable... assignedFilters) {
        if (effectResult.getType() == EffectResult.Type.ASSIGNED_AGAINST) {
            AssignAgainstResult assignmentResult = (AssignAgainstResult) effectResult;
            if (side != null) {
                if (assignmentResult.getPlayerId().equals(game.getGameState().getCurrentPlayerId())) {
                    if (side == Side.SHADOW)
                        return false;
                } else {
                    if (side == Side.FREE_PEOPLE)
                        return false;
                }
            }

            return Filters.and(assignedFilters).accepts(game, assignmentResult.getAssignedCard())
                    && Filters.filter(assignmentResult.getAgainst(), game, againstFilter).size() > 0;
        }
        return false;
    }

    public static boolean assignedToSkirmish(DefaultGame game, EffectResult effectResult, Side side, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.ASSIGNED_TO_SKIRMISH) {
            AssignedToSkirmishResult assignResult = (AssignedToSkirmishResult) effectResult;
            if (side != null) {
                if (assignResult.getPlayerId().equals(game.getGameState().getCurrentPlayerId())) {
                    if (side == Side.SHADOW)
                        return false;
                } else {
                    if (side == Side.FREE_PEOPLE)
                        return false;
                }
            }

            return Filters.and(filters).accepts(game, ((AssignedToSkirmishResult) effectResult).getAssignedCard());
        }
        return false;
    }

    public static boolean forEachCardDrawnOrPutIntoHandByOpponent(DefaultGame game, EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND) {
            DrawCardOrPutIntoHandResult drawResult = (DrawCardOrPutIntoHandResult) effectResult;
            return !drawResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean forEachCardDrawnOrPutIntoHand(DefaultGame game, EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND) {
            DrawCardOrPutIntoHandResult drawResult = (DrawCardOrPutIntoHandResult) effectResult;
            return drawResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean forEachCardDrawn(DefaultGame game, EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.DRAW_CARD_OR_PUT_INTO_HAND) {
            DrawCardOrPutIntoHandResult drawResult = (DrawCardOrPutIntoHandResult) effectResult;
            return drawResult.isDraw() && drawResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean forEachDiscardedFromPlay(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_PLAY)
            return Filters.and(filters).accepts(game, ((DiscardCardsFromPlayResult) effectResult).getDiscardedCard());
        return false;
    }

    public static boolean forEachDiscardedFromHand(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_HAND)
            return Filters.and(filters).accepts(game, ((DiscardCardFromHandResult) effectResult).getDiscardedCard());
        return false;
    }

    public static boolean forEachDiscardedFromDeck(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_DECK)
            return Filters.and(filters).accepts(game, ((DiscardCardFromDeckResult) effectResult).getDiscardedCard());
        return false;
    }
    
    public static boolean forEachDiscardedFromHandBy(DefaultGame game, EffectResult effectResult, Filterable discardedBy, Filterable... discarded) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_DISCARDED_FROM_HAND) {
            DiscardCardFromHandResult discardResult = (DiscardCardFromHandResult) effectResult;
            if (discardResult.getSource() != null 
                    && Filters.and(discardedBy).accepts(game, discardResult.getSource()))
                return Filters.and(discarded).accepts(game, discardResult.getDiscardedCard());
        }
        return false;
    }

    public static boolean forEachWounded(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_WOUNDED)
            return Filters.and(filters).accepts(game, ((WoundResult) effectResult).getWoundedCard());
        return false;
    }

    public static boolean forEachHealed(DefaultGame game, EffectResult effectResult, String player, Filterable... filters) {
        if (effectResult.getType() != EffectResult.Type.FOR_EACH_HEALED)
            return false;

        HealResult healResult = (HealResult)effectResult;
        if (player == null || player.equals(healResult.getPerformingPlayer()))
            return Filters.and(filters).accepts(game, ((HealResult) effectResult).getHealedCard());
        else
            return false;

    }

    public static boolean forEachExerted(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_EXERTED)
            return Filters.and(filters).accepts(game, ((ExertResult) effectResult).getExertedCard());
        return false;
    }

    public static boolean forEachExertedBy(DefaultGame game, EffectResult effectResult, Filterable exertedBy, Filterable... exerted) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_EXERTED) {
            ExertResult exertResult = (ExertResult) effectResult;
            if (exertResult.getAction().getActionSource() != null
                    && Filters.and(exertedBy).accepts(game, exertResult.getAction().getActionSource()))
                return Filters.and(exerted).accepts(game, exertResult.getExertedCard());
        }
        return false;
    }

    public static boolean isTakingControlOfSite(Effect effect, DefaultGame game, Filterable... sourceFilters) {
        if (effect.getType() == Effect.Type.BEFORE_TAKE_CONTROL_OF_A_SITE) {
            Preventable takeControlOfASiteEffect = (Preventable) effect;
            return !takeControlOfASiteEffect.isPrevented(game) && Filters.and(sourceFilters).accepts(game, effect.getSource());
        }
        return false;
    }

    public static boolean revealedCardsFromTopOfDeck(EffectResult effectResult, String playerId) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_REVEALED_FROM_TOP_OF_DECK) {
            RevealCardFromTopOfDeckResult revealCardFromTopOfDeckResult = (RevealCardFromTopOfDeckResult) effectResult;
            return revealCardFromTopOfDeckResult.getPlayerId().equals(playerId);
        }
        return false;
    }

    public static boolean isAddingBurden(Effect effect, DefaultGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_ADD_BURDENS) {
            Preventable addBurdenEffect = (Preventable) effect;
            return !addBurdenEffect.isPrevented(game) && Filters.and(filters).accepts(game, effect.getSource());
        }
        return false;
    }

    public static boolean isAddingTwilight(Effect effect, DefaultGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_ADD_TWILIGHT) {
            Preventable addTwilightEffect = (Preventable) effect;
            return !addTwilightEffect.isPrevented(game) && effect.getSource() != null && Filters.and(filters).accepts(game, effect.getSource());
        }
        return false;
    }

    public static boolean isGettingHealed(Effect effect, DefaultGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_HEALED) {
            PreventableCardEffect healEffect = (PreventableCardEffect) effect;
            return Filters.filter(healEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingKilled(Effect effect, DefaultGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_KILLED) {
            KillEffect killEffect = (KillEffect) effect;
            return Filters.filter(killEffect.getCharactersToBeKilled(), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isDrawingACard(Effect effect, DefaultGame game, String playerId) {
        if (effect.getType() == Effect.Type.BEFORE_DRAW_CARD) {
            DrawOneCardEffect drawEffect = (DrawOneCardEffect) effect;
            if (effect.getPerformingPlayer().equals(playerId) && drawEffect.canDrawCard(game))
                return true;
        }
        return false;
    }

    public static boolean forEachReturnedToHand(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_RETURNED_TO_HAND) {
            ReturnCardsToHandResult result = (ReturnCardsToHandResult) effectResult;
            return Filters.and(filters).accepts(game, result.getReturnedCard());
        }
        return false;
    }

    public static boolean forEachKilled(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_KILLED) {
            ForEachKilledResult killResult = (ForEachKilledResult) effectResult;
            return Filters.and(filters).accepts(game, killResult.getKilledCard());
        }
        return false;
    }

    public static boolean forEachKilledBy(DefaultGame game, EffectResult effectResult, Filterable killedBy, Filterable... killed) {
        return forEachKilledInASkirmish(game, effectResult, killedBy, killed);
    }

    public static boolean forEachKilledInASkirmish(DefaultGame game, EffectResult effectResult, Filterable killedBy, Filterable... killed) {
        if (effectResult.getType() == EffectResult.Type.FOR_EACH_KILLED
                && game.getGameState().getCurrentPhase() == Phase.SKIRMISH
                && Filters.countActive(game, Filters.inSkirmish, killedBy)>0) {
            ForEachKilledResult killResult = (ForEachKilledResult) effectResult;

            return Filters.and(killed).accepts(game, killResult.getKilledCard());
        }
        return false;
    }

    public static boolean isGettingWoundedBy(Effect effect, DefaultGame game, Filterable sourceFilter, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_WOUND) {
            WoundCharactersEffect woundEffect = (WoundCharactersEffect) effect;
            if (woundEffect.getSources() != null && Filters.filter(woundEffect.getSources(), game, sourceFilter).size() > 0)
                return Filters.filter(woundEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingExerted(Effect effect, DefaultGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_EXERT) {
            PreventableCardEffect woundEffect = (PreventableCardEffect) effect;
            return Filters.filter(woundEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingWounded(Effect effect, DefaultGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_WOUND) {
            PreventableCardEffect woundEffect = (PreventableCardEffect) effect;
            return Filters.filter(woundEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingDiscardedBy(Effect effect, DefaultGame game, Filterable sourceFilter, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_DISCARD_FROM_PLAY) {
            PreventableCardEffect preventableEffect = (PreventableCardEffect) effect;
            if (effect.getSource() != null && Filters.and(sourceFilter).accepts(game, effect.getSource()))
                return Filters.filter(preventableEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingDiscardedByOpponent(Effect effect, DefaultGame game, String playerId, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_DISCARD_FROM_PLAY) {
            PreventableCardEffect preventableEffect = (PreventableCardEffect) effect;
            if (effect.getSource() != null && !effect.getPerformingPlayer().equals(playerId))
                return Filters.filter(preventableEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean isGettingDiscarded(Effect effect, DefaultGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_DISCARD_FROM_PLAY) {
            PreventableCardEffect discardEffect = (PreventableCardEffect) effect;
            return Filters.filter(discardEffect.getAffectedCardsMinusPrevented(game), game, filters).size() > 0;
        }
        return false;
    }

    public static boolean activated(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.ACTIVATE) {
            PhysicalCard source = ((ActivateCardResult) effectResult).getSource();
            return Filters.and(filters).accepts(game, source);
        }
        return false;
    }

    public static boolean played(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            PhysicalCard playedCard = ((PlayCardResult) effectResult).getPlayedCard();
            return Filters.and(filters).accepts(game, playedCard);
        }
        return false;
    }

    public static boolean playedFromZone(DefaultGame game, EffectResult effectResult, Zone zone, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            final PlayCardResult playResult = (PlayCardResult) effectResult;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return (playResult.getPlayedFrom() == zone && Filters.and(filters).accepts(game, playedCard));
        }
        return false;
    }

    public static boolean playedFromStacked(DefaultGame game, EffectResult effectResult, Filterable stackedOnFilter, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            final PlayCardResult playResult = (PlayCardResult) effectResult;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return (playResult.getPlayedFrom() == Zone.STACKED
                    && Filters.and(stackedOnFilter).accepts(game, playResult.getAttachedOrStackedPlayedFrom())
                    && Filters.and(filters).accepts(game, playedCard));
        }
        return false;
    }

    public static boolean playedOn(DefaultGame game, EffectResult effectResult, Filterable targetFilter, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.PLAY) {
            final PlayCardResult playResult = (PlayCardResult) effectResult;
            final PhysicalCard attachedTo = playResult.getAttachedTo();
            if (attachedTo == null)
                return false;
            PhysicalCard playedCard = playResult.getPlayedCard();
            return Filters.and(filters).accepts(game, playedCard)
                    && Filters.and(targetFilter).accepts(game, attachedTo);
        }
        return false;
    }

    public static boolean movesTo(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.WHEN_MOVE_TO
                && Filters.and(filters).accepts(game, game.getGameState().getCurrentSite())) {
            return true;
        }
        return false;
    }

    public static boolean isMovingTo(Effect effect, DefaultGame game, Filterable... filters) {
        if (effect.getType() == Effect.Type.BEFORE_MOVE_TO
                && Filters.and(filters).accepts(game, game.getGameState().getCurrentSite()))
            return true;

        return false;
    }

    public static boolean movesFrom(DefaultGame game, EffectResult effectResult, Filterable... filters) {
        if (effectResult.getType() == EffectResult.Type.WHEN_MOVE_FROM
                && Filters.and(filters).accepts(game, ((WhenMoveFromResult) effectResult).getSite())) {
            return true;
        }
        return false;
    }

    public static boolean moves(DefaultGame game, EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.WHEN_FELLOWSHIP_MOVES;
    }

    public static boolean transferredCard(DefaultGame game, EffectResult effectResult, Filterable transferredCard, Filterable transferredFrom, Filterable transferredTo) {
        if (effectResult.getType() == EffectResult.Type.CARD_TRANSFERRED) {
            CardTransferredResult transferResult = (CardTransferredResult) effectResult;
            return (Filters.and(transferredCard).accepts(game, transferResult.getTransferredCard())
                    && (transferredFrom == null || (transferResult.getTransferredFrom() != null && Filters.and(transferredFrom).accepts(game, transferResult.getTransferredFrom())))
                    && (transferredTo == null || (transferResult.getTransferredTo() != null && Filters.and(transferredTo).accepts(game, transferResult.getTransferredTo()))));
        }
        return false;
    }

    public static boolean skirmishCancelled(DefaultGame game, EffectResult effectResult, Filterable ... fpCharacterFilter) {
        if (effectResult.getType() == EffectResult.Type.SKIRMISH_CANCELLED) {
            SkirmishCancelledResult cancelledResult = (SkirmishCancelledResult) effectResult;
            return Filters.and(fpCharacterFilter).accepts(game, cancelledResult.getFpCharacter());
        }
        return false;
    }

    public static boolean freePlayerStartedAssigning(DefaultGame game, EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.FREE_PEOPLE_PLAYER_STARTS_ASSIGNING;
    }

    public static boolean freePlayerDecidedIfMoving(DefaultGame game, EffectResult effectResult) {
        return effectResult.getType() == EffectResult.Type.FREE_PEOPLE_PLAYER_DECIDED_IF_MOVING;
    }

    public static boolean freePlayerDecidedToMove(DefaultGame game, EffectResult effectResult) {
        if(effectResult.getType() == EffectResult.Type.FREE_PEOPLE_PLAYER_DECIDED_IF_MOVING) {
            var result = (FreePlayerMoveDecisionResult)effectResult;
            return result.IsMoving();
        }

        return false;
    }

    public static boolean freePlayerDecidedToStay(DefaultGame game, EffectResult effectResult) {
        if(effectResult.getType() == EffectResult.Type.FREE_PEOPLE_PLAYER_DECIDED_IF_MOVING) {
            var result = (FreePlayerMoveDecisionResult)effectResult;
            return result.IsStaying();
        }

        return false;
    }

}
