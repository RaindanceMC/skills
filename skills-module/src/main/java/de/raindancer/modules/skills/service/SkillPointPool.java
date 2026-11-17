package de.raindancer.modules.skills.service;

import de.raindancer.core.ui.tree.SkillPoints;
import de.raindancer.modules.skills.model.Hero;
import de.raindancer.modules.skills.rules.SkillRule;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/** The adventurer's skill points as Core's shared pool, so other plugins can sell unlocks for them too. */
public final class SkillPointPool implements SkillPoints.Pool {

    private final Function<UUID, Optional<Hero>> heroes;
    private final SkillRule rule;

    public SkillPointPool(Function<UUID, Optional<Hero>> heroes, SkillRule rule) {
        this.heroes = heroes;
        this.rule = rule;
    }

    private Hero hero(UUID player) {
        return heroes.apply(player).orElseGet(() -> Hero.fresh(player));
    }

    @Override
    public int level(UUID player) {
        return hero(player).level();
    }

    @Override
    public int total(UUID player) {
        return rule.points(hero(player));
    }

    @Override
    public int spent(UUID player) {
        return rule.spent(hero(player));
    }
}
