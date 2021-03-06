package com.davidkoudela.crucible.config;

/**
 * Description: {@link HibernateAdvancedLdapPluginConfigurationDAO} represents an interface for Data Access Object class
 *              for {@link AdvancedLdapPluginConfiguration}.
 * Copyright (C) 2015 David Koudela
 *
 * @author dkoudela
 * @since 2015-03-31
 */
public interface HibernateAdvancedLdapPluginConfigurationDAO {
    public abstract void store(AdvancedLdapPluginConfiguration advancedLdapPluginConfiguration, boolean isUpdate) throws Exception;
    public abstract AdvancedLdapPluginConfiguration get();
    public abstract void remove(int id);
}
