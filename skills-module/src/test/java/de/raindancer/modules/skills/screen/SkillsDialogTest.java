package de.raindancer.modules.skills.screen;

import de.raindancer.core.ui.dialog.DialogAnswers;
import de.raindancer.core.ui.dialog.DialogPage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SkillsDialogTest {

    private final List<DialogPage> shown = new ArrayList<>();
    private final Player viewer = mock(Player.class);

    @BeforeEach
    void standIn() {
        when(viewer.isOnline()).thenReturn(true);
        DialogPage.runtime(new DialogPage.Runtime() {
            @Override
            public void show(Player player, DialogPage page) {
                shown.add(page);
            }

            @Override
            public void onPlayerThread(Player player, Runnable task) {
                task.run();
            }
        });
    }

    @AfterEach
    void game() {
        DialogPage.runtime(null);
    }

    private static String plain(net.kyori.adventure.text.Component text) {
        return PlainTextComponentSerializer.plainText().serialize(text);
    }

    @Test
    @DisplayName("the adventurer's level and points on top, every skill a button saying its level, a click opens its tree")
    void everySkillAButton() {
        AtomicInteger opened = new AtomicInteger();
        DialogPage page = SkillsDialog.page(viewer, 12, 0.4, 3, List.of(
                new SkillsDialog.Row("<white>Mining", 5, 0.5, 3, 3, 1, false, opened::incrementAndGet),
                new SkillsDialog.Row("<white>Fishing", 2, 0.1, 3, 2, 0, false, opened::incrementAndGet)),
                List.of("Each level is a skill point."));

        assertThat(plain(page.title())).contains("Your skills");
        assertThat(page.plainText()).contains("Adventurer level 12").contains("3 skill points to spend");
        assertThat(page.pressable().stream().map(button -> plain(button.label())).toList())
                .containsExactly("Mining · 5", "Fishing · 2", "How this works");
        assertThat(plain(page.button("Mining").tooltip())).contains("Mastery 1 of 3").contains("Click to open its tree");

        page.button("Fishing").press(viewer, DialogAnswers.none());
        assertThat(opened).hasValue(1);
    }

    @Test
    @DisplayName("a fully mastered skill says so")
    void mastered() {
        DialogPage page = SkillsDialog.page(viewer, 40, 1, 0, List.of(
                new SkillsDialog.Row("<white>Mining", 30, 1, 0, 3, 3, true, () -> { })), List.of());

        assertThat(plain(page.button("Mining").tooltip())).contains("Mastered");
        assertThat(page.plainText()).doesNotContain("to spend");
    }
}
