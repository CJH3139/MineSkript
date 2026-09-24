package com.mineskript.common;

import com.mineskript.common.elements.conditions.CondCompareEqual;
import com.mineskript.common.elements.conditions.CondEndsWith;
import com.mineskript.common.elements.conditions.CondIgnoringCase;
import com.mineskript.common.elements.conditions.CondIsAtLeast;
import com.mineskript.common.elements.conditions.CondIsAtMost;
import com.mineskript.common.elements.conditions.CondIsBetween;
import com.mineskript.common.elements.conditions.CondIsGreaterThan;
import com.mineskript.common.elements.conditions.CondIsIn;
import com.mineskript.common.elements.conditions.CondIsLessThan;
import com.mineskript.common.elements.conditions.CondIsSet;
import com.mineskript.common.elements.conditions.CondListContains;
import com.mineskript.common.elements.conditions.CondStartsWith;
import com.mineskript.common.elements.conditions.CondTextContains;
import com.mineskript.common.elements.effects.EffAdd;
import com.mineskript.common.elements.effects.EffContinue;
import com.mineskript.common.elements.effects.EffDelete;
import com.mineskript.common.elements.effects.EffExitLoop;
import com.mineskript.common.elements.effects.EffIncrease;
import com.mineskript.common.elements.effects.EffReduce;
import com.mineskript.common.elements.effects.EffRemove;
import com.mineskript.common.elements.effects.EffReset;
import com.mineskript.common.elements.effects.EffSet;
import com.mineskript.common.elements.effects.EffStop;
import com.mineskript.common.elements.effects.EffStopAllScripts;
import com.mineskript.common.elements.effects.EffStopScript;
import com.mineskript.common.elements.effects.EffWait;
import com.mineskript.common.elements.events.CommonEvents;
import com.mineskript.common.elements.events.EventValues;
import com.mineskript.common.elements.expressions.ExprAbsoluteValue;
import com.mineskript.common.elements.expressions.ExprCeiling;
import com.mineskript.common.elements.expressions.ExprEventValue;
import com.mineskript.common.elements.expressions.ExprFirstCharacters;
import com.mineskript.common.elements.expressions.ExprFloor;
import com.mineskript.common.elements.expressions.ExprJoin;
import com.mineskript.common.elements.expressions.ExprLastCharacters;
import com.mineskript.common.elements.expressions.ExprLength;
import com.mineskript.common.elements.expressions.ExprListSize;
import com.mineskript.common.elements.expressions.ExprLoopIndex;
import com.mineskript.common.elements.expressions.ExprLoopIteration;
import com.mineskript.common.elements.expressions.ExprLoopValue;
import com.mineskript.common.elements.expressions.ExprLowercase;
import com.mineskript.common.elements.expressions.ExprMaximum;
import com.mineskript.common.elements.expressions.ExprMinimum;
import com.mineskript.common.elements.expressions.ExprParsedAsBoolean;
import com.mineskript.common.elements.expressions.ExprParsedAsInteger;
import com.mineskript.common.elements.expressions.ExprParsedAsNumber;
import com.mineskript.common.elements.expressions.ExprRandomInteger;
import com.mineskript.common.elements.expressions.ExprRandomNumber;
import com.mineskript.common.elements.expressions.ExprReplace;
import com.mineskript.common.elements.expressions.ExprRound;
import com.mineskript.common.elements.expressions.ExprRoundedToPlaces;
import com.mineskript.common.elements.expressions.ExprSplit;
import com.mineskript.common.elements.expressions.ExprSquareRoot;
import com.mineskript.common.elements.expressions.ExprSubstring;
import com.mineskript.common.elements.expressions.ExprUppercase;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.SyntaxRegistry;

public final class CommonModule implements SyntaxModule {
    @Override
    public String name() {
        return "common";
    }

    @Override
    public void register(SyntaxRegistry registry) {
        EventValues.register(registry);
        CommonEvents.register(registry);

        ExprEventValue.register(registry);
        ExprLoopValue.register(registry);
        ExprLoopIteration.register(registry);
        ExprLoopIndex.register(registry);
        ExprListSize.register(registry);
        ExprUppercase.register(registry);
        ExprLowercase.register(registry);
        ExprLength.register(registry);
        ExprSubstring.register(registry);
        ExprReplace.register(registry);
        ExprJoin.register(registry);
        ExprFirstCharacters.register(registry);
        ExprLastCharacters.register(registry);
        ExprSplit.register(registry);
        ExprParsedAsNumber.register(registry);
        ExprParsedAsInteger.register(registry);
        ExprParsedAsBoolean.register(registry);
        ExprRound.register(registry);
        ExprFloor.register(registry);
        ExprCeiling.register(registry);
        ExprAbsoluteValue.register(registry);
        ExprSquareRoot.register(registry);
        ExprRoundedToPlaces.register(registry);
        ExprMinimum.register(registry);
        ExprMaximum.register(registry);
        ExprRandomNumber.register(registry);
        ExprRandomInteger.register(registry);

        CondIsSet.register(registry);
        CondIsIn.register(registry);
        CondListContains.register(registry);
        CondTextContains.register(registry);
        CondStartsWith.register(registry);
        CondEndsWith.register(registry);
        CondIgnoringCase.register(registry);
        CondCompareEqual.register(registry);
        CondIsAtLeast.register(registry);
        CondIsAtMost.register(registry);
        CondIsGreaterThan.register(registry);
        CondIsLessThan.register(registry);
        CondIsBetween.register(registry);

        EffWait.register(registry);
        EffExitLoop.register(registry);
        EffContinue.register(registry);
        EffStopAllScripts.register(registry);
        EffStopScript.register(registry);
        EffStop.register(registry);
        EffSet.register(registry);
        EffAdd.register(registry);
        EffIncrease.register(registry);
        EffRemove.register(registry);
        EffReduce.register(registry);
        EffDelete.register(registry);
        EffReset.register(registry);
    }
}
