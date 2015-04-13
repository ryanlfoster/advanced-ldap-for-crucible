package com.davidkoudela.crucible.admin;

import com.atlassian.crucible.spi.data.UserData;
import com.cenqua.fisheye.user.UserManager;
import com.davidkoudela.crucible.config.AdvancedLdapPluginConfiguration;
import com.davidkoudela.crucible.config.HibernateAdvancedLdapPluginConfigurationDAO;
import com.davidkoudela.crucible.ldap.connect.AdvancedLdapConnector;
import com.davidkoudela.crucible.ldap.connect.AdvancedLdapSearchFilterFactory;
import com.davidkoudela.crucible.ldap.model.AdvancedLdapGroup;
import com.davidkoudela.crucible.ldap.model.AdvancedLdapPerson;
import com.davidkoudela.crucible.ldap.model.AdvancedLdapPersonBuilder;
import com.unboundid.ldap.sdk.*;

import java.util.List;

/**
 * Description: Implementation of {@link AdvancedLdapUserManager} providing managed LDAP users with their groups.
 * Copyright (C) 2015 David Koudela
 *
 * @author dkoudela
 * @since 2015-03-13
 */
@org.springframework.stereotype.Service("advancedLdapUserManager")
public class AdvancedLdapUserManagerImpl implements AdvancedLdapUserManager {
    private UserManager userManager;
    private HibernateAdvancedLdapPluginConfigurationDAO hibernateAdvancedLdapPluginConfigurationDAO;

    @org.springframework.beans.factory.annotation.Autowired
    public AdvancedLdapUserManagerImpl(UserManager userManager, HibernateAdvancedLdapPluginConfigurationDAO hibernateAdvancedLdapPluginConfigurationDAO) {
        this.userManager = userManager;
        this.hibernateAdvancedLdapPluginConfigurationDAO = hibernateAdvancedLdapPluginConfigurationDAO;
        System.out.println("**************************** AdvancedLdapUserManagerImpl START ****************************");
    }

    @Override
    public void loadUser(UserData userData) {
        SearchRequest searchRequest = null;
        AdvancedLdapPluginConfiguration advancedLdapPluginConfiguration = hibernateAdvancedLdapPluginConfigurationDAO.get();

        try {
            Filter filter = AdvancedLdapSearchFilterFactory.getSearchFilterForUser(advancedLdapPluginConfiguration.getUserFilterKey(), userData.getUserName());
            searchRequest = new SearchRequest(advancedLdapPluginConfiguration.getLDAPBaseDN(), SearchScope.SUB, filter);
        } catch (Exception e) {
            System.out.println("Search Request creation failed for filter: " + advancedLdapPluginConfiguration.getUserFilterKey() + " Exception: " + e);
            return;
        }

        AdvancedLdapConnector advancedLdapConnector = new AdvancedLdapConnector();
        AdvancedLdapPersonBuilder advancedLdapPersonBuilder = new AdvancedLdapPersonBuilder(advancedLdapPluginConfiguration, true);
        advancedLdapConnector.ldapPagedSearch(advancedLdapPluginConfiguration, searchRequest, advancedLdapPersonBuilder);
        List<AdvancedLdapPerson> persons = advancedLdapPersonBuilder.getPersons();

        if (1 != persons.size()) {
            System.out.println("AdvancedLdapUserManagerImpl: person search returned "+ persons.size() + " entries");
            return;
        }
        AdvancedLdapPerson advancedLdapPerson = persons.get(0);

        for (AdvancedLdapGroup advancedLdapGroup : advancedLdapPerson.getGroupList()) {
            String GID = advancedLdapGroup.getNormalizedGID();
            System.out.println("AdvancedLdapUserManagerImpl: GID: " + GID);
            try {
                if (!this.userManager.builtInGroupExists(GID)) {
                    System.out.println("AdvancedLdapUserManagerImpl: GID added: " + GID);
                    this.userManager.addBuiltInGroup(GID);
                }
                if (!this.userManager.isUserInGroup(GID, advancedLdapPerson.getUid())) {
                    this.userManager.addUserToBuiltInGroup(GID, advancedLdapPerson.getUid());
                }
            } catch (Exception e) {
                System.out.println("AdvancedLdapUserManagerImpl: group: " + GID + " failed: " + e);
            } catch(Throwable e) {
                System.out.println("AdvancedLdapUserManagerImpl: group: " + GID + " failed unexpected: " + e);
            }
        }

        System.out.println("AdvancedLdapUserManagerImpl.loadUser END");
    }
}
