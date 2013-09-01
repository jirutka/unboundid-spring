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

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.schema.Schema;
import com.unboundid.ldif.LDIFException;
import com.unboundid.ldif.LDIFReader;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * FactoryBean that creates an in-memory LDAP server suitable for integration
 * tests.
 *
 * @author Jakub Jirutka <jakub@jirutka.cz>
 */
public class InMemoryDirectoryServerFactoryBean
        implements FactoryBean<InMemoryDirectoryServer>, DisposableBean, InitializingBean {

    public static final String
            BIND_DN = "cn=Directory Manager",
            BIND_PASSWORD = "password";

    private List<String> baseDNs;
    private List<Resource> schemaFiles = emptyList();
    private List<Resource> ldifFiles = emptyList();
    private boolean loadDefaultSchemas = true;

    private InMemoryDirectoryServer server;


    public InMemoryDirectoryServer getObject() throws LDAPException, LDIFException, IOException {
        List<Schema> schemas = new ArrayList<>(schemaFiles.size() + 1);
        String[] baseDNsArray = baseDNs.toArray(new String[0]);
        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(baseDNsArray);

        config.addAdditionalBindCredentials(BIND_DN, BIND_PASSWORD);

        if (loadDefaultSchemas) {
            schemas.add(Schema.getDefaultStandardSchema());
        }
        for (Resource resource : schemaFiles) {
            schemas.add(Schema.getSchema(resource.getFile()));
        }
        Schema mergedSchema = Schema.mergeSchemas(schemas.toArray(new Schema[schemas.size()]));
        config.setSchema(mergedSchema);

        server = new InMemoryDirectoryServer(config);

        for (Resource resource : ldifFiles) {
            server.importFromLDIF(false, new LDIFReader(resource.getFile()));
        }
        server.startListening();

        return server;
    }

    public Class<InMemoryDirectoryServer> getObjectType() {
        return InMemoryDirectoryServer.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void destroy() {
        if (server != null) server.shutDown(true);
    }

    public void afterPropertiesSet() {
        Assert.notEmpty(baseDNs, "At least one base DN must be provided.");
    }


    //////// Accessors ////////

    /**
     * @param baseDNs The set of base DNs to use for the server.
     */
    public void setBaseDNs(List<String> baseDNs) {
        Assert.notEmpty(baseDNs, "At least one base DN must be provided");
        this.baseDNs = baseDNs;
    }

    /**
     * @param baseDN The base DN to use for the server.
     */
    public void setBaseDN(String baseDN) {
        this.baseDNs = Arrays.asList(baseDN);
    }

    /**
     * @param schemaFiles The resources of the LDIF files containing the schema
     *                    information to be imported to the server.
     *
     * @see com.unboundid.ldap.sdk.schema.Schema#getSchema(java.io.File...)
     * @see com.unboundid.ldap.listener.InMemoryDirectoryServerConfig#setSchema(com.unboundid.ldap.sdk.schema.Schema)
     */
    public void setSchemaFiles(List<Resource> schemaFiles) {
        this.schemaFiles = schemaFiles;
    }

    /**
     * @param schema The resource of the LDIF file containing the schema
     *               information to be imported to the server.
     *
     * @see com.unboundid.ldap.sdk.schema.Schema#getSchema(java.io.File...)
     * @see com.unboundid.ldap.listener.InMemoryDirectoryServerConfig#setSchema(com.unboundid.ldap.sdk.schema.Schema)
     */
    public void setSchemaFile(Resource schema) {
        this.schemaFiles = Arrays.asList(schema);
    }

    /**
     * @param ldifFiles The resources to the LDIF files from which the entries
     *                  will be imported to the server.
     *
     * @see com.unboundid.ldap.listener.InMemoryDirectoryServer#importFromLDIF(boolean, com.unboundid.ldif.LDIFReader)
     */
    public void setLdifFiles(List<Resource> ldifFiles) {
        this.ldifFiles = ldifFiles;
    }

    /**
     * @param ldifFile The resource to the LDIF file from which the entries
     *                 will be imported to the server.
     *
     * @see com.unboundid.ldap.listener.InMemoryDirectoryServer#importFromLDIF(boolean, com.unboundid.ldif.LDIFReader)
     */
    public void setLdifFile(Resource ldifFile) {
        this.ldifFiles = Arrays.asList(ldifFile);
    }

    /**
     * @param loadDefaultSchemas <tt>true</tt> if default standard schemas
     *                           should be loaded (default), or not.
     *
     * @see com.unboundid.ldap.sdk.schema.Schema#getDefaultStandardSchema()
     */
    public void setLoadDefaultSchemas(boolean loadDefaultSchemas) {
        this.loadDefaultSchemas = loadDefaultSchemas;
    }
}
