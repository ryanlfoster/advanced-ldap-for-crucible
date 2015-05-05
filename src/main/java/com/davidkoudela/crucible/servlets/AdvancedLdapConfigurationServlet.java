package com.davidkoudela.crucible.servlets;

import com.atlassian.fisheye.plugin.web.helpers.VelocityHelper;
import com.davidkoudela.crucible.config.AdvancedLdapPluginConfiguration;
import com.davidkoudela.crucible.config.HibernateAdvancedLdapPluginConfigurationDAO;
import com.davidkoudela.crucible.tasks.AdvancedLdapSynchronizationManager;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: Configuration servlet class reacting on HTTP GET and POST requests.
 * Copyright (C) 2015 David Koudela
 *
 * @author dkoudela
 * @since 2015-03-24
 */
public class AdvancedLdapConfigurationServlet extends HttpServlet {
    private final VelocityHelper velocityHelper;
    private HibernateAdvancedLdapPluginConfigurationDAO hibernateAdvancedLdapPluginConfigurationDAO;
    private AdvancedLdapSynchronizationManager advancedLdapSynchronizationManager;

    @org.springframework.beans.factory.annotation.Autowired
    public AdvancedLdapConfigurationServlet(VelocityHelper velocityHelper,
                                            HibernateAdvancedLdapPluginConfigurationDAO hibernateAdvancedLdapPluginConfigurationDAO,
                                            AdvancedLdapSynchronizationManager advancedLdapSynchronizationManager) {
        this.velocityHelper = velocityHelper;
        this.hibernateAdvancedLdapPluginConfigurationDAO = hibernateAdvancedLdapPluginConfigurationDAO;
        this.advancedLdapSynchronizationManager = advancedLdapSynchronizationManager;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AdvancedLdapPluginConfiguration advancedLdapPluginConfiguration = this.hibernateAdvancedLdapPluginConfigurationDAO.get();

        Map<String,Object> params = new HashMap<String,Object>();
        req.setAttribute("decorator", "atl.admin");
        params.put("advancedLdapPluginConfiguration", advancedLdapPluginConfiguration);
        resp.setContentType("text/html");
        if (req.getPathInfo().contains("/advancedLdapConfigurationServletView.do")) {
            velocityHelper.renderVelocityTemplate("templates/configureView.vm", params, resp.getWriter());
        } else if (req.getPathInfo().contains("/advancedLdapConfigurationServletTest.do")) {
            velocityHelper.renderVelocityTemplate("templates/authTest.vm", params, resp.getWriter());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Map<String,Object> params = new HashMap<String,Object>();
        req.setAttribute("decorator", "atl.admin");
        resp.setContentType("text/html");
        AdvancedLdapPluginConfiguration advancedLdapPluginConfiguration = this.hibernateAdvancedLdapPluginConfigurationDAO.get();
        if (req.getPathInfo().contains("/advancedLdapConfigurationServletView.do")) {
            setParameters(advancedLdapPluginConfiguration, req);

            try {
                this.hibernateAdvancedLdapPluginConfigurationDAO.store(advancedLdapPluginConfiguration, true);
                this.advancedLdapSynchronizationManager.updateTimer();
            } catch (Exception e) {
                System.out.println("AdvancedLdapConfigurationServlet.doPost: hibernateAdvancedLdapPluginConfigurationDAO.store failed: " + e);
            }
            params.put("advancedLdapPluginConfiguration", advancedLdapPluginConfiguration);
            velocityHelper.renderVelocityTemplate("templates/configureView.vm", params, resp.getWriter());
        } else if (req.getPathInfo().contains("/advancedLdapConfigurationServletRemove.do")) {
            try {
                this.hibernateAdvancedLdapPluginConfigurationDAO.remove(advancedLdapPluginConfiguration.getId());
                this.advancedLdapSynchronizationManager.updateTimer();
            } catch (Exception e) {
                System.out.println("AdvancedLdapConfigurationServlet.doPost: hibernateAdvancedLdapPluginConfigurationDAO.remove failed: " + e);
            }
            advancedLdapPluginConfiguration = this.hibernateAdvancedLdapPluginConfigurationDAO.get();
            params.put("advancedLdapPluginConfiguration", advancedLdapPluginConfiguration);
            velocityHelper.renderVelocityTemplate("templates/configureView.vm", params, resp.getWriter());
        } else if (req.getPathInfo().contains("/advancedLdapConfigurationServletTest.do")) {
            velocityHelper.renderVelocityTemplate("templates/authTest.vm", params, resp.getWriter());
        } else if (req.getPathInfo().contains("/advancedLdapConfigurationServletTestResults.do")) {
            velocityHelper.renderVelocityTemplate("templates/authResult.vm", params, resp.getWriter());
        } else {
            params.put("advancedLdapPluginConfiguration", advancedLdapPluginConfiguration);
            velocityHelper.renderVelocityTemplate("templates/configureEdit.vm", params, resp.getWriter());
        }
    }

    private void setParameters(AdvancedLdapPluginConfiguration advancedLdapPluginConfiguration, HttpServletRequest request) {
        advancedLdapPluginConfiguration.setConnectTimeoutMillis(Integer.parseInt(StringUtils.defaultIfEmpty(request.getParameter("ldap.serverTimeout"), "10000")));
        advancedLdapPluginConfiguration.setResponseTimeoutMillis(Integer.parseInt(StringUtils.defaultIfEmpty(request.getParameter("ldap.serverTimeout"), "10000")));
        advancedLdapPluginConfiguration.setLDAPPageSize(Integer.parseInt(StringUtils.defaultIfEmpty(request.getParameter("ldap.pageSize"), "1000")));
        advancedLdapPluginConfiguration.setLDAPSyncPeriod(Integer.parseInt(StringUtils.defaultIfEmpty(request.getParameter("ldap.resyncPeriod"), "3600")));
        advancedLdapPluginConfiguration.setLDAPUrl(StringUtils.defaultIfEmpty(request.getParameter("ldap.url"), ""));
        advancedLdapPluginConfiguration.setLDAPBindDN(StringUtils.defaultIfEmpty(request.getParameter("ldap.initialDn"), ""));
        advancedLdapPluginConfiguration.setLDAPBindPassword(StringUtils.defaultIfEmpty(request.getParameter("ldap.initialSecret"), ""));
        advancedLdapPluginConfiguration.setLDAPBaseDN(StringUtils.defaultIfEmpty(request.getParameter("ldap.baseDn"), ""));
        advancedLdapPluginConfiguration.setUserFilterKey(StringUtils.defaultIfEmpty(request.getParameter("ldap.filter"), ""));
        advancedLdapPluginConfiguration.setDisplayNameAttributeKey(StringUtils.defaultIfEmpty(request.getParameter("ldap.displaynameAttr"), ""));
        advancedLdapPluginConfiguration.setEmailAttributeKey(StringUtils.defaultIfEmpty(request.getParameter("ldap.emailAttr"), ""));
        advancedLdapPluginConfiguration.setUIDAttributeKey(StringUtils.defaultIfEmpty(request.getParameter("ldap.uidAttr"), ""));

        advancedLdapPluginConfiguration.setUserGroupNamesKey(StringUtils.defaultIfEmpty(request.getParameter("ldap.userGroupNamesAttr"), ""));
        advancedLdapPluginConfiguration.setGroupFilterKey(StringUtils.defaultIfEmpty(request.getParameter("ldap.groupFilter"), ""));
        advancedLdapPluginConfiguration.setGIDAttributeKey(StringUtils.defaultIfEmpty(request.getParameter("ldap.gidAttr"), ""));
        advancedLdapPluginConfiguration.setGroupDisplayNameKey(StringUtils.defaultIfEmpty(request.getParameter("ldap.groupDisplaynameAttr"), ""));
        advancedLdapPluginConfiguration.setUserNamesKey(StringUtils.defaultIfEmpty(request.getParameter("ldap.groupUsernameAttr"), ""));
    }

}