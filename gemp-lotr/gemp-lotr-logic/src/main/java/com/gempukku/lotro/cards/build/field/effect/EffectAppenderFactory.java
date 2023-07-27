package com.gempukku.lotro.cards.build.field.effect;

import com.gempukku.lotro.cards.build.CardGenerationEnvironment;
import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.cards.build.field.FieldUtils;
import com.gempukku.lotro.cards.build.field.effect.appender.*;
import com.gempukku.lotro.cards.build.field.effect.appender.lotronly.*;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class EffectAppenderFactory {
    private final Map<String, EffectAppenderProducer> effectAppenderProducers = new HashMap<>();

    public EffectAppenderFactory() {
        effectAppenderProducers.put("addburdens", new AddBurdens());
        effectAppenderProducers.put("addkeyword", new AddKeyword());
        effectAppenderProducers.put("addmodifier", new AddModifier());
        effectAppenderProducers.put("addthreats", new AddThreats());
        effectAppenderProducers.put("addtokens", new AddTokens());
        effectAppenderProducers.put("addtrigger", new AddTrigger());
        effectAppenderProducers.put("addtwilight", new AddTwilight());
        effectAppenderProducers.put("allycanparticipateinarcheryfireandskirmishes", new AllyCanParticipateInArcheryFireAndSkirmishes());
        effectAppenderProducers.put("allycanparticipateinskirmishes", new AllyCanParticipateInSkirmishes());
        effectAppenderProducers.put("appendcardidstowhileinzone", new AppendCardIdsToWhileInZone());
        effectAppenderProducers.put("assignfpcharactertoskirmish", new AssignFpCharacterToSkirmish());
        effectAppenderProducers.put("cancelallassignments", new CancelAllAssignments());
        effectAppenderProducers.put("cancelevent", new CancelEvent());
        effectAppenderProducers.put("cancelskirmish", new CancelSkirmish());
        effectAppenderProducers.put("cantbeassignedtoskirmish", new CantBeAssignedToSkirmish());
        effectAppenderProducers.put("cantbeoverwhelmedmultiplier", new CantBeOverwhelmedMultiplier());
        effectAppenderProducers.put("canttakemorewoundsthan", new CantTakeMoreWoundsThan());
        effectAppenderProducers.put("canttakewounds", new CantTakeWounds());
        effectAppenderProducers.put("choice", new Choice());
        effectAppenderProducers.put("chooseactivecards", new ChooseActiveCards());
        effectAppenderProducers.put("chooseakeyword", new ChooseAKeyword());
        effectAppenderProducers.put("chooseandheal", new ChooseAndHeal());
        effectAppenderProducers.put("chooseanumber", new ChooseANumber());
        effectAppenderProducers.put("choosearace", new ChooseARace());
        effectAppenderProducers.put("chooseandaddtwilight", new ChooseAndAddTwilight());
        effectAppenderProducers.put("chooseandremovetokens", new ChooseAndRemoveTokens());
        effectAppenderProducers.put("chooseandremovetwilight", new ChooseAndRemoveTwilight());
        effectAppenderProducers.put("choosecardsfromdiscard", new ChooseCardsFromDiscard());
        effectAppenderProducers.put("choosecardsfromdrawdeck", new ChooseCardsFromDrawDeck());
        effectAppenderProducers.put("choosehowmanyburdenstospot", new ChooseHowManyBurdensToSpot());
        effectAppenderProducers.put("choosehowmanytospot", new ChooseHowManyToSpot());
        effectAppenderProducers.put("chooseopponent", new ChooseOpponent());
        effectAppenderProducers.put("chooseyesorno", new ChooseYesOrNo());
        effectAppenderProducers.put("conditional", new ConditionalEffect());
        effectAppenderProducers.put("corruptringbearer", new CorruptRingBearer());
        effectAppenderProducers.put("costtoeffect", new CostToEffect());
//        effectAppenderProducers.put("cycle", new TribblePowerCycle());
        effectAppenderProducers.put("discard", new DiscardFromPlay());
        effectAppenderProducers.put("discardbottomcardsfromdeck", new DiscardBottomCardFromDeck());
        effectAppenderProducers.put("discardcardatrandomfromhand", new DiscardCardAtRandomFromHand());
        effectAppenderProducers.put("discardcardsfromdrawdeck", new DiscardCardsFromDrawDeck());
        effectAppenderProducers.put("discardfromhand", new DiscardFromHand());
        effectAppenderProducers.put("discardstackedcards", new DiscardStackedCards());
        effectAppenderProducers.put("discardtopcardsfromdeck", new DiscardTopCardFromDeck());
        effectAppenderProducers.put("doesnotaddtoarcherytotal", new DoesNotAddToArcheryTotal());
        effectAppenderProducers.put("drawcards", new DrawCards());
        effectAppenderProducers.put("endphase", new EndPhase());
//        effectAppenderProducers.put("exchange", new TribblePowerExchange());
        effectAppenderProducers.put("exert", new Exert());
        effectAppenderProducers.put("exhaust", new Exhaust());
        effectAppenderProducers.put("filtercardsinmemory", new FilterCardsInMemory());
        effectAppenderProducers.put("foreachplayer", new ForEachPlayer());
        effectAppenderProducers.put("foreachshadowplayer", new ForEachShadowPlayer());
        effectAppenderProducers.put("getcardsfromtopofdeck", new GetCardsFromTopOfDeck());
        effectAppenderProducers.put("heal", new Heal());
        effectAppenderProducers.put("incrementperphaselimit", new IncrementPerPhaseLimit());
        effectAppenderProducers.put("incrementperturnlimit", new IncrementPerTurnLimit());
        effectAppenderProducers.put("kill", new Kill());
        effectAppenderProducers.put("liberatesite", new LiberateSite());
        effectAppenderProducers.put("lookatdrawdeck", new LookAtDrawDeck());
        effectAppenderProducers.put("lookathand", new LookAtHand());
        effectAppenderProducers.put("lookatrandomcardsfromhand", new LookAtRandomCardsFromHand());
        effectAppenderProducers.put("lookattopcardsofdrawdeck", new LookAtTopCardsOfDrawDeck());
        effectAppenderProducers.put("makeselfringbearer", new MakeSelfRingBearer());
        effectAppenderProducers.put("memorize", new MemorizeActive());
        effectAppenderProducers.put("memorizenumber", new MemorizeNumber());
        effectAppenderProducers.put("memorizestacked", new MemorizeStacked());
        effectAppenderProducers.put("modifyarcherytotal", new ModifyArcheryTotal());
        effectAppenderProducers.put("modifysitenumber", new ModifySiteNumber());
        effectAppenderProducers.put("modifystrength", new ModifyStrength());
        effectAppenderProducers.put("negatewound", new NegateWound());
        effectAppenderProducers.put("optional", new Optional());
        effectAppenderProducers.put("placenowoundforexert", new PlaceNoWoundForExert());
        effectAppenderProducers.put("play", new PlayCardFromHand());
        effectAppenderProducers.put("playcardfromdiscard", new PlayCardFromDiscard());
        effectAppenderProducers.put("playcardfromdrawdeck", new PlayCardFromDrawDeck());
        effectAppenderProducers.put("playcardfromstacked", new PlayCardFromStacked());
        effectAppenderProducers.put("playnextsite", new PlayNextSite());
        effectAppenderProducers.put("playsite", new PlaySite());
        effectAppenderProducers.put("preventable", new PreventableAppenderProducer());
        effectAppenderProducers.put("preventdiscard", new PreventCardEffectAppender());
        effectAppenderProducers.put("preventexert", new PreventExert());
        effectAppenderProducers.put("preventheal", new PreventCardEffectAppender());
        effectAppenderProducers.put("preventwound", new PreventWound());
        effectAppenderProducers.put("putcardsfromdeckintohand", new PutCardsFromDeckIntoHand());
        effectAppenderProducers.put("putcardsfromdeckontopofdeck", new PutCardsFromDeckOnTopOfDeck());
        effectAppenderProducers.put("putcardsfromdeckonbottomofdeck", new PutCardsFromDeckOnBottomOfDeck());
        effectAppenderProducers.put("putcardsfromdiscardintohand", new PutCardsFromDiscardIntoHand());
        effectAppenderProducers.put("putcardsfromdiscardonbottomofdeck", new PutCardsFromDiscardOnBottomOfDeck());
        effectAppenderProducers.put("putcardsfromdiscardontopofdeck", new PutCardsFromDiscardOnTopOfDeck());
        effectAppenderProducers.put("putcardsfromhandonbottomofdeck", new PutCardsFromHandOnBottomOfDeck());
        effectAppenderProducers.put("putcardsfromhandontopofdeck", new PutCardsFromHandOnTopOfDeck());
        effectAppenderProducers.put("putcardsfromplayonbottomofdeck", new PutCardsFromPlayOnBottomOfDeck());
        effectAppenderProducers.put("putonring", new PutOnRing());
        effectAppenderProducers.put("putplayedeventintohand", new PutPlayedEventIntoHand());
        effectAppenderProducers.put("putplayedeventonbottomofdrawdeck", new PutPlayedEventOnBottomOfDrawDeck());
        effectAppenderProducers.put("putstackedcardsintohand", new PutStackedCardsIntoHand());
        effectAppenderProducers.put("reconcilehand", new ReconcileHand());
        effectAppenderProducers.put("reducearcherytotal", new ReduceArcheryTotal());
        effectAppenderProducers.put("reinforcetokens", new ReinforceTokens());
        effectAppenderProducers.put("removeburdens", new RemoveBurdens());
        effectAppenderProducers.put("removecardsindiscardfromgame", new RemoveCardsInDiscardFromGame());
        effectAppenderProducers.put("removefromthegame", new RemoveFromTheGame());
        effectAppenderProducers.put("removekeyword", new RemoveKeyword());
        effectAppenderProducers.put("removetext", new RemoveText());
        effectAppenderProducers.put("removethreats", new RemoveThreats());
        effectAppenderProducers.put("removetokens", new RemoveTokens());
        effectAppenderProducers.put("removetwilight", new RemoveTwilight());
        effectAppenderProducers.put("reordertopcardsofdrawdeck", new ReorderTopCardsOfDrawDeck());
        effectAppenderProducers.put("repeat", new Repeat());
        effectAppenderProducers.put("replaceinassignment", new ReplaceInAssignment());
        effectAppenderProducers.put("replaceinskirmish", new ReplaceInSkirmish());
//        effectAppenderProducers.put("replay", new TribblePowerReplay());
        effectAppenderProducers.put("resetwhileinzonedata", new ResetWhileInZoneData());
        effectAppenderProducers.put("returntohand", new ReturnToHand());
        effectAppenderProducers.put("revealbottomcardsofdrawdeck", new RevealBottomCardsOfDrawDeck());
        effectAppenderProducers.put("revealcards", new RevealCards());
        effectAppenderProducers.put("revealcardsfromhand", new RevealCardsFromHand());
        effectAppenderProducers.put("revealhand", new RevealHand());
        effectAppenderProducers.put("revealrandomcardsfromhand", new RevealRandomCardsFromHand());
        effectAppenderProducers.put("revealtopcardsofdrawdeck", new RevealTopCardsOfDrawDeck());
        effectAppenderProducers.put("reverseplayerorder", new ReversePlayerOrder());
        effectAppenderProducers.put("shadowcanthaveinitiative", new ShadowCantHaveInitiative());
        effectAppenderProducers.put("shufflecardsfromdiscardintodrawdeck", new ShuffleCardsFromDiscardIntoDrawDeck());
        effectAppenderProducers.put("shufflecardsfromhandintodrawdeck", new ShuffleCardsFromHandIntoDrawDeck());
        effectAppenderProducers.put("shufflecardsfromplayintodrawdeck", new ShuffleCardsFromPlayIntoDrawDeck());
        effectAppenderProducers.put("shufflehandintodrawdeck", new ShuffleHandIntoDrawDeck());
        effectAppenderProducers.put("sideplayercantplayphaseeventsorusephasespecialabilities", new SidePlayerCantPlayPhaseEventsOrUsePhaseSpecialAbilities());
        effectAppenderProducers.put("spot", new Spot());
        effectAppenderProducers.put("stackcards", new StackCardsFromPlay());
        effectAppenderProducers.put("stackcardsfromdeck", new StackCardsFromDeck());
        effectAppenderProducers.put("stackcardsfromdiscard", new StackCardsFromDiscard());
        effectAppenderProducers.put("stackcardsfromhand", new StackCardsFromHand());
        effectAppenderProducers.put("stackplayedevent", new StackPlayedEvent());
        effectAppenderProducers.put("stacktopcardsofdrawdeck", new StackTopCardsOfDrawDeck());
        effectAppenderProducers.put("storewhileinzone", new StoreWhileInZone());
        effectAppenderProducers.put("takecontrolofsite", new TakeControlOfSite());
        effectAppenderProducers.put("takeoffring", new TakeOffRing());
        effectAppenderProducers.put("transfer", new Transfer());
        effectAppenderProducers.put("transferfromdiscard", new TransferFromDiscard());
        effectAppenderProducers.put("transfertosupport", new TransferToSupport());
        effectAppenderProducers.put("wound", new Wound());
    }

    public EffectAppender getEffectAppender(JSONObject effectObject, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        final String type = FieldUtils.getString(effectObject.get("type"), "type");
        final EffectAppenderProducer effectAppenderProducer = effectAppenderProducers.get(type.toLowerCase());
        if (effectAppenderProducer == null)
            throw new InvalidCardDefinitionException("Unable to find effect of type: " + type);
        return effectAppenderProducer.createEffectAppender(effectObject, environment);
    }

    public EffectAppender[] getEffectAppenders(JSONObject[] effectArray, CardGenerationEnvironment environment) throws InvalidCardDefinitionException {
        EffectAppender[] result = new EffectAppender[effectArray.length];
        for (int i = 0; i < result.length; i++) {
            final String type = FieldUtils.getString(effectArray[i].get("type"), "type");
            final EffectAppenderProducer effectAppenderProducer = effectAppenderProducers.get(type.toLowerCase());
            if (effectAppenderProducer == null)
                throw new InvalidCardDefinitionException("Unable to find effect of type: " + type);
            result[i] = effectAppenderProducer.createEffectAppender(effectArray[i], environment);
        }
        return result;
    }
}
