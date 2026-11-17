package de.raindancer.modules.skills.service;

import de.raindancer.core.ui.tree.SkillPoints;
import de.raindancer.modules.skills.model.Hero;
import de.raindancer.modules.skills.model.SkillCatalog;
import de.raindancer.modules.skills.rules.SkillRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SkillPointPoolTest {

    private final UUID me = UUID.randomUUID();

    @AfterEach
    void clear() {
        SkillPoints.clear();
    }

    @Test
    @DisplayName("Core's pool shows the hero's level and points, and what another plugin spent comes off what is left")
    void sharedPool() {
        Hero hero = new Hero(me, 12, 0, Map.of("defense:toughness", 2), 0);
        SkillRule rule = new SkillRule(SkillCatalog.all(), () -> 100, () -> Duration.ofDays(1),
                SkillPoints::spentElsewhere);
        SkillPoints.provide(null, new SkillPointPool(id -> Optional.of(hero), rule));
        SkillPoints.spender(null, "enchants", id -> 3);

        assertThat(SkillPoints.level(me)).isEqualTo(12);
        assertThat(SkillPoints.total(me)).isEqualTo(12);
        assertThat(SkillPoints.left(me)).isEqualTo(12 - 2 - 3);
        assertThat(rule.left(hero)).as("skills' own count agrees").isEqualTo(SkillPoints.left(me));
    }

    @Test
    @DisplayName("an unknown player is a fresh hero")
    void unknown() {
        SkillRule rule = new SkillRule(SkillCatalog.all(), () -> 100, () -> Duration.ofDays(1));
        SkillPoints.provide(null, new SkillPointPool(id -> Optional.empty(), rule));
        assertThat(SkillPoints.left(me)).isEqualTo(rule.points(Hero.fresh(me)));
    }
}
