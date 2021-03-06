package com.davidkoudela.crucible.tasks;

import com.davidkoudela.crucible.admin.AdvancedLdapUserManager;

import java.util.Date;
import java.util.TimerTask;

/**
 * Description: {@link AdvancedLdapSynchronizationTask} is an LDAP synchronization task
 *              for syncing LDAP users and groups.
 * Copyright (C) 2015 David Koudela
 *
 * @author dkoudela
 * @since 2015-04-27
 */
public class AdvancedLdapSynchronizationTask extends TimerTask {
    private AdvancedLdapUserManager advancedLdapUserManager;

    public AdvancedLdapSynchronizationTask(AdvancedLdapUserManager advancedLdapUserManager) {
        this.advancedLdapUserManager = advancedLdapUserManager;
    }

    @Override
    public void run() {
        System.out.println("Timer task started at:"+new Date());
        this.advancedLdapUserManager.loadGroups();
        System.out.println("Timer task ended at:" + new Date());
    }
}
