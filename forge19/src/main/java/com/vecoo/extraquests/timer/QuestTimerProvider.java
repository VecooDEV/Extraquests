package com.vecoo.extraquests.timer;

import com.vecoo.extralib.gson.UtilGson;
import com.vecoo.extralib.world.UtilWorld;
import com.vecoo.extraquests.ExtraQuests;
import com.vecoo.extraquests.util.Utils;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class QuestTimerProvider {
    private final String filePath;
    private final ArrayList<QuestTimer> questTimers;

    public QuestTimerProvider(String filePath, MinecraftServer server) {
        this.questTimers = new ArrayList<>();
        this.filePath = UtilWorld.worldDirectory(filePath, server);
    }

    public List<QuestTimer> getQuestTimers() {
        return this.questTimers;
    }

    public void addQuestTimer(QuestTimer questTimer) {
        this.questTimers.add(questTimer);
        ExtraQuests.getInstance().getTimerProvider().startTimer(questTimer);
        write();
    }

    public void removeQuestTimer(QuestTimer questTimer) {
        this.questTimers.remove(questTimer);
        ExtraQuests.getInstance().getTimerProvider().removeTimer(questTimer);
        write();
    }

    private void write() {
        UtilGson.writeFileAsync(filePath, "timers.json", UtilGson.newGson().toJson(this)).join();
    }

    public void init() {
        CompletableFuture<Boolean> future = UtilGson.readFileAsync(filePath, "timers.json", el -> {
            for (QuestTimer questTimer : UtilGson.newGson().fromJson(el, QuestTimerProvider.class).getQuestTimers()) {
                if (questTimer.getEndTime() > new Date().getTime()) {
                    this.questTimers.add(questTimer);
                    ExtraQuests.getInstance().getTimerProvider().startTimer(questTimer);
                } else {
                    Utils.timerExpired(questTimer);
                }
            }
        });
        if (!future.join()) {
            write();
        }
    }
}