package de.uniks.vs.jalica.engine.skillmanagement;

import de.uniks.vs.jalica.engine.AlicaEngine;

import java.util.HashMap;

public class SkillModule {

    protected HashMap<String, ISkill> skills = new HashMap<>();

    public HashMap<String, ISkill> getSkills() {
        return skills;
    }
    public ISkill get(String id) {
        return skills.get(id);
    }
}
