/*
 * The MIT License
 *
 * Copyright 2013 Jakub Jirutka <jakub@jirutka.cz>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cz.jirutka.spring.unboundid;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPURL;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustAllTrustManager;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.security.GeneralSecurityException;

import static org.springframework.util.StringUtils.hasText;

/**
 * FactoryBean that creates an UnboundID {@code LDAPConnection}.
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class LdapConnectionFactoryBean implements FactoryBean<LDAPConnection>, DisposableBean, InitializingBean {

    private static final int DEFAULT_PORT = 389;
    private static final int DEFAULT_SSL_PORT = 636;

    private String host;
    private int port = -1;
    private String bindDN;
    private String password;
    private boolean ssl = false;
    private boolean sslTrustAll = false;
    private TrustManager sslTrustManager;

    private LDAPConnection connection;


    public LDAPConnection getObject() throws GeneralSecurityException, LDAPException {
        SocketFactory socketFactory;

        if (ssl && sslTrustAll) {
            sslTrustManager = new TrustAllTrustManager();
        }

        if (sslTrustManager != null) {
            socketFactory = new SSLUtil(sslTrustManager).createSSLSocketFactory();
        } else if (ssl) {
            socketFactory = SSLSocketFactory.getDefault();
        } else {
            socketFactory = SocketFactory.getDefault();
        }

        connection = new LDAPConnection(socketFactory, host, port);

        if (hasText(bindDN) && hasText(password)) {
            connection.bind(bindDN, password);
        }

        return connection;
    }

    public Class<LDAPConnection> getObjectType() {
        return LDAPConnection.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() {
        Assert.hasText(host, "host or URL must be provided");

        if (port < 1) {
            port = ssl ? DEFAULT_SSL_PORT : DEFAULT_PORT;
        }
    }

    public void destroy() {
        if (connection != null) connection.close();
    }


    //////// Accessors ////////

    /**
     * Specifies the LDAP server URL with scheme, host and optionally port.
     *
     * @param url The URL of the LDAP server. It must not be null or empty.
     * @throws LDAPException If the provided URL string cannot be parsed as an
     *                       LDAP URL.
     */
    public void setUrl(String url) throws LDAPException {
        LDAPURL ldapUrl = new LDAPURL(url);

        this.host = ldapUrl.getHost();
        this.port = ldapUrl.getPort();
        this.ssl = "ldaps".equals(ldapUrl.getScheme());
    }

    /**
     * @param host The address of the server to which the connection should be
     *             established. It must not be null or empty.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @param port The port number of the server to which the connection should
     *             be established. It should be a value between 1 and 65535.
     *             The default port is 389 or 636 for TLS/SSL.
     */
    public void setPort(int port) {
        Assert.isTrue(port > 0 && port < 65536, "port must be between 1 and 65535");
        this.port = port;
    }

    /**
     * @param ssl Connect via TLS/SSL? Default is {@code false}.
     */
    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    /**
     * Note: When bindDN and password is not provided, then anonymous binding
     * will be used.
     *
     * @param bindDN The DN to use to authenticate to the directory server.
     */
    public void setBindDN(String bindDN) {
        this.bindDN = bindDN;
    }

    /**
     * Note: When bindDN and password is not provided, then anonymous binding
     * will be used.
     *
     * @param password The password to use to authenticate to the directory
     *                 server.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * This enables SSL trust manager which will blindly trust any
     * certificate that is presented to it.
     *
     * <p>DO NOT USE USE THIS IN PRODUCTION!</p>
     *
     * @see TrustAllTrustManager
     *
     * @param sslTrustAll Trust blindly any certificate?
     */
    public void setSslTrustAll(boolean sslTrustAll) {
        this.sslTrustAll = sslTrustAll;
    }

    /**
     * @param sslTrustManager The trust manager to use to determine whether to
     *                        trust server certificates presented to the client.
     *                        It may be {@code null} if the default set of trust
     *                        managers should be used.
     */
    public void setSslTrustManager(TrustManager sslTrustManager) {
        this.sslTrustManager = sslTrustManager;
    }
}
