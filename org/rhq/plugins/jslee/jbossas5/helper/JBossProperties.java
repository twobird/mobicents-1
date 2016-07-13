/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.rhq.plugins.jslee.jbossas5.helper;

/**
 * The properties that are used by the JBossAS micro-kernel during bootstrap (see http://wiki.jboss.org/wiki/Wiki.jsp?page=JBossProperties).
 *
 * @author Ian Springer
 */
public abstract class JBossProperties {
    public static final String PARTITION_NAME = "jboss.partition.name";
    public static final String PARTITION_UDP_GROUP = "jboss.partition.udpGroup";
    public static final String NATIVE_LOAD = "jboss.native.load";
    public static final String NATIVE_DIR = "jboss.native.dir";
    public static final String BOOT_LIBRARY_LIST = "jboss.boot.library.list";
    public static final String SERVER_TYPE = "jboss.server.type";
    public static final String SERVER_ROOT_DEPLOYMENT_FILENAME = "jboss.server.root.deployment.filename";

    /**
     * The base directory of the jboss distribution - default '$JBOSS_HOME'
     */
    public static final String HOME_DIR = "jboss.home.dir";

    public static final String HOME_URL = "jboss.home.url";
    public static final String LIB_URL = "jboss.lib.url";
    public static final String PATCH_URL = "jboss.patch.url";

    /**
     * The configuration name of the server - default 'default' for AS, or 'production' for EAP or SOA
     */
    public static final String SERVER_NAME = "jboss.server.name";

    /**
     * The directory where server configurations exist - default '${jboss.home.dir}/server'
     */
    public static final String SERVER_BASE_DIR = "jboss.server.base.dir";

    /**
     * The directory for the current configuration - default '${jboss.server.base.dir}/${jboss.server.name}'
     */
    public static final String SERVER_HOME_DIR = "jboss.server.home.dir";

    public static final String SERVER_LOG_DIR = "jboss.server.log.dir";
    public static final String SERVER_TEMP_DIR = "jboss.server.temp.dir";
    public static final String SERVER_DATA_DIR = "jboss.server.data.dir";
    public static final String SERVER_BASE_URL = "jboss.server.base.url";
    public static final String SERVER_HOME_URL = "jboss.server.home.url";
    public static final String SERVER_CONFIG_URL = "jboss.server.config.url";
    public static final String SERVER_LIB_URL = "jboss.server.lib.url";
    public static final String BIND_ADDRESS = "jboss.bind.address";
    public static final String SERVER_EXIT_ON_SHUTDOWN = "jboss.server.exitonshutdown";
    public static final String SERVER_BLOCKING_SHUTDOWN = "jboss.server.blockingshutdown";
    public static final String SERVER_REQUIRE_JBOSS_URL_STREAM_HANDLER_FACTORY = "jboss.server.requirejbossurlstreamhandlerfactory";
    public static final String PLATFORM_MBEANSERVER = "jboss.platform.mbeanserver";

    public static final String LOGGING_LOGGER_PLUGIN_CLASS = "org.jboss.logging.Logger.pluginClass";

    /* JGroups props */
    public static final String JGROUPS_BIND_ADDRESS = "jboss.bind.address";
    public static final String JGROUPS_BIND_ADDR = "jgroups.bind_addr";
    public static final String JGROUPS_UDP_MCAST_ADDR = "jgroups.udp.mcast_addr";

    private JBossProperties() {
    }
}