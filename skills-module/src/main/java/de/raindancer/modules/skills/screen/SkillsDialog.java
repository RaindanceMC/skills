package de.raindancer.modules.skills.screen;

import de.raindancer.core.ui.dialog.DialogPage;
import de.raindancer.core.ui.dialog.DialogReturn;
import de.raindancer.core.ui.menu.Menu;
import de.raindancer.core.ui.tree.SkillView;
import de.raindancer.core.ui.tree.Skills;
import de.raindancer.modules.skills.SkillsServices;
import de.raindancer.modules.skills.model.Hero;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * {@code /skills} as the game's own window, like /hub: the adventurer's level and points on top, then every skill —
 * this module's and any other plugin's, fishing's or the enchants' abilities — as a button with its level and mastery.
 * A skill's tree stays a chest page; its Back brings this window back.
 */
public final class SkillsDialog implements ISkillsScreen {

    static final String HEADING = "<bold><gradient:#86EFAC:#7DD3FC>Your skills</gradient></bold>";
    private static final int BAR = 10;

    /** One skill as the window shows it. */
    record Row(String title, int level, double progress, int pointsLeft, int branches, int mastered,
               boolean fullyMastered, Runnable open) {
    }

    private final SkillsServices services;
    private final Player viewer;

    public SkillsDialog(SkillsServices services, Player viewer) {
        this.services = services;
        this.viewer = viewer;
    }

    /** The parent for a tree opened from here, or straight from a command: Back shows this window. */
    public static Menu returning(SkillsServices services, Player viewer) {
        return new DialogReturn(viewer, services.brand(), "Your skills", () -> new SkillsDialog(services, viewer).open());
    }

    @Override
    public void open() {
        UUID me = viewer.getUniqueId();
        Hero hero = services.heroes().of(me).orElseGet(() -> Hero.fresh(me));
        Menu here = returning(services, viewer);
        List<Row> rows = new ArrayList<>();
        for (SkillView skill : Skills.all()) {
            rows.add(new Row(skill.title(), skill.level(me), skill.progress(me), skill.pointsLeft(me), skill.branches(),
                    skill.mastered(me), skill.fullyMastered(me), () -> skill.open(viewer, here)));
        }
        List<String> help = List.of(
                "Adventurer experience comes from playing: mining, digging, chopping, farming, fishing, crafting,"
                        + " smelting, cooking, brewing, building, trading, enchanting, taming, exploring, travelling,"
                        + " bosses and raids.",
                "Each level is a skill point for any tree, up to " + services.perks().cap() + ". You have "
                        + services.perks().points(hero) + ".");
        page(viewer, hero.level(), services.progression().progress(hero.xp()), services.perks().left(hero), rows, help)
                .show(viewer);
    }

    static DialogPage page(Player viewer, int level, double progress, int pointsLeft, List<Row> rows,
                           List<String> help) {
        DialogPage page = DialogPage.titled(HEADING).columns(2)
                .text("<gray>Adventurer level <white>" + level + "\n" + bar(progress)
                        + (pointsLeft > 0 ? "\n<green>" + pointsLeft + (pointsLeft == 1 ? " skill point" : " skill points")
                        + " to spend" : ""));
        for (Row row : rows) {
            StringBuilder tooltip = new StringBuilder("<gray>Level <white>" + row.level() + "\n" + bar(row.progress()));
            if (row.branches() > 0) {
                tooltip.append("\n").append(row.fullyMastered() ? "<gold>Mastered"
                        : "<gray>Mastery <white>" + row.mastered() + "<gray> of " + row.branches());
            }
            if (row.pointsLeft() > 0) {
                tooltip.append("\n<green>").append(row.pointsLeft())
                        .append(row.pointsLeft() == 1 ? " point" : " points").append(" to spend");
            }
            tooltip.append("\n\n<dark_gray>» <white>Click to open its tree");
            page.button((row.fullyMastered() ? "<gold>" : "") + row.title() + " <dark_gray>· <white>" + row.level(),
                    tooltip.toString(), answers -> row.open().run());
        }
        page.button("<gray>How this works", "<gray>Where experience comes from and what a point buys."
                + "\n\n<dark_gray>» <white>Click to read it", answers -> DialogPage.titled(HEADING, "How this works")
                .text("<gray>" + String.join("\n\n", help)).back(() -> page.show(viewer)).show(viewer));
        return page;
    }

    static String bar(double progress) {
        int filled = (int) Math.floor(Math.max(0, Math.min(1, progress)) * BAR);
        return "<green>" + "▰".repeat(filled) + "<dark_gray>" + "▰".repeat(BAR - filled);
    }

    @Override
    public String describe() {
        return "every skill, with the player's level, points and mastery in each";
    }
}
