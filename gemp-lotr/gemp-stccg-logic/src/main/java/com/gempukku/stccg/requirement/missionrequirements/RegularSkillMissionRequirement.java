package com.gempukku.stccg.requirement.missionrequirements;

import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.RegularSkill;

import java.util.Collection;

public class RegularSkillMissionRequirement implements MissionRequirement {

    private final RegularSkill _skill;
    private final int _count;
    public RegularSkillMissionRequirement(RegularSkill skill) {
        _skill = skill;
        _count = 1;
    }
    public RegularSkillMissionRequirement(RegularSkill skill, int count) {
        _skill = skill;
        _count = count;
    }
    @Override
    public boolean canBeMetBy(PersonnelCard personnel) {
        Integer skillCount = personnel.getSkillLevel(_skill);
        return skillCount >= _count;
    }

    @Override
    public boolean canBeMetBy(Collection<PersonnelCard> personnel) {
        int totalSkillLevel = 0;
        for (PersonnelCard card : personnel) {
            totalSkillLevel += card.getSkillLevel(_skill);
        }
        return totalSkillLevel >= _count;
    }

}