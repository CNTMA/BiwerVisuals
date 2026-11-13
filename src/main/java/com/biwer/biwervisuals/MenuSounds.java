package com.biwer.biwervisuals;

import java.io.BufferedInputStream;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

final class MenuSounds {
    private static final java.util.concurrent.ExecutorService AUDIO = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "Biwer menu audio");
        thread.setDaemon(true);
        return thread;
    });

    static void play(String name) {
        AUDIO.execute(() -> {
            try (var resource = MenuSounds.class.getResourceAsStream("/assets/biwervisuals/sounds/ui/" + name + ".wav")) {
                if (resource == null) return;
                try (var audio = AudioSystem.getAudioInputStream(new BufferedInputStream(resource))) {
                    Clip clip = AudioSystem.getClip();
                    clip.addLineListener(event -> {
                        if (event.getType() == LineEvent.Type.STOP) clip.close();
                    });
                    clip.open(audio);
                    clip.start();
                }
            } catch (Exception exception) {
                org.slf4j.LoggerFactory.getLogger(MenuSounds.class).debug("Menu sound unavailable", exception);
            }
        });
    }
}
