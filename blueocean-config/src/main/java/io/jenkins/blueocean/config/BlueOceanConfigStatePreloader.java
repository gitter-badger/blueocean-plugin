package io.jenkins.blueocean.config;

import hudson.Extension;
import hudson.security.AuthorizationStrategy;
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import hudson.security.SecurityRealm;
import io.jenkins.blueocean.commons.BlueOceanConfigProperties;
import io.jenkins.blueocean.commons.PageStatePreloader;
import jenkins.model.Jenkins;
import net.sf.json.util.JSONBuilder;

import java.io.StringWriter;
import java.util.logging.Logger;

/**
 * @author Vivek Pandey
 */
@Extension
public class BlueOceanConfigStatePreloader extends PageStatePreloader {

    private static final Logger LOGGER = Logger.getLogger(BlueOceanConfigStatePreloader.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStatePropertyPath() {
        return "config";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStateJson() {
        StringWriter writer = new StringWriter();
        Jenkins jenkins = Jenkins.getInstance();
        String version = Jenkins.getVersion() != null ? Jenkins.getVersion().toString() : Jenkins.VERSION;

        AuthorizationStrategy authorizationStrategy = jenkins.getAuthorizationStrategy();
        boolean allowAnonymousRead = true;
        if(authorizationStrategy instanceof FullControlOnceLoggedInAuthorizationStrategy){
            allowAnonymousRead = ((FullControlOnceLoggedInAuthorizationStrategy) authorizationStrategy).isAllowAnonymousRead();
        }

        new JSONBuilder(writer)
            .object()
                .key("version").value(getBlueOceanPluginVersion())
                .key("jenkinsConfig")
                .object()
                    .key("version").value(version)
                    .key("security")
                    .object()
                        .key("enabled").value(jenkins.isUseSecurity())
                        .key("loginUrl").value(jenkins.getSecurityRealm() == SecurityRealm.NO_AUTHENTICATION ? null : jenkins.getSecurityRealm().getLoginUrl())
                        .key("authorizationStrategy").object()
                            .key("allowAnonymousRead").value(allowAnonymousRead)
                            .endObject()
                        .key("enableJWT").value(BlueOceanConfigProperties.BLUEOCEAN_FEATURE_JWT_AUTHENTICATION)
                        .endObject()
                    .endObject()
                .endObject();

        return writer.toString();
    }

    /** gives Blueocean plugin version. blueocean-web being core module is looked at to determine the version */
    private String getBlueOceanPluginVersion(){
        return Jenkins.getInstance().getPlugin("blueocean-web").getWrapper().getVersion();
    }
}
