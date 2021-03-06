package com.davidkoudela.crucible.timer;

import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Description: {@link AdvancedLdapTimerTrigger} is an auxiliary timer service triggering LDAP tasks.
 *              It provides missing Crucible timer service functionality.
 * Copyright (C) 2015 David Koudela
 *
 * @author dkoudela
 * @since 2015-03-13
 */
@org.springframework.stereotype.Service("advancedLdapTrigger")
public class AdvancedLdapTimerTrigger {
    private LinkedList<Timer> timerLinkedList;

    public AdvancedLdapTimerTrigger() {
        this.timerLinkedList = new LinkedList<Timer>();
    }

    public int createTimer(TimerTask timerTask, long periodInSeconds) {
        Timer timer = new Timer(true);
        if (periodInSeconds < 1)
            periodInSeconds = 1;
        timer.scheduleAtFixedRate(timerTask, 0, periodInSeconds * 1000);
        int index = this.timerLinkedList.size();
        this.timerLinkedList.add(timer);
        System.out.println("Timer index created: " + index);
        return index;
    }

    public void deleteTimer(int index) {
        try {
            Timer timer = this.timerLinkedList.remove(index);
            timer.cancel();
            System.out.println("Timer index canceled: " + index);
        } catch (Exception e) {
            System.out.println("Timer index not found: " + index);
        } catch (Throwable throwable) {
            System.out.println("Timer index canceling failed: " + index + " Exception: " + throwable);
        }
    }

    public int activeTimers() {
        return this.timerLinkedList.size();
    }

    public void runNow(TimerTask timerTask) {
        Timer timer = new Timer(true);
        timer.schedule(timerTask, new Date());
    }
}
