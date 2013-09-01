Spring Factory Beans for UnboundID LDAP SDK
===========================================

[Spring](http://www.springsource.org/spring-framework) Factory Beans for [UnboundID LDAP SDK](https://www.unboundid.com/products/ldapsdk/) – a fast, powerful, user-friendly, and free Java API for communicating with LDAP directory servers. UnboundID SDK also provides an in-memory LDAP server written in Java, excellent choice for integration tests.

Why to use the UnboundID in a Spring-based application instead of Spring LDAP anyway? Well, Spring LDAP has one major issue – it’s a layer on top of JNDI that has a poor performance and horrible API. See [this question](http://stackoverflow.com/a/3895885/2217862) on StackOverflow.


Usage
-----

```xml
<bean id="ldapConnection" class="cz.jirutka.spring.unboundid.LdapConnectionFactoryBean"
      p:url="ldaps://example.org:636"
      p:bindDN="cn=Directory Manager"
      p:password="top-secret"
      p:sslTrustAll="true" />
```

```xml
<bean id="inMemoryLdap" class="cz.jirutka.spring.unboundid.InMemoryDirectoryServerFactoryBean"
      p:baseDN="ou=People,dc=example,dc=org"
      p:schemaFile="classpath:example.schema"
      p:ldifFile="classpath:data.ldif"
      p:loadDefaultSchemas="false" />
```

Most of properties are optional.


Maven
-----

Released versions are available in The Central Repository. Just add this artifact to your project:

```xml
<dependency>
    <groupId>cz.jirutka.spring</groupId>
    <artifactId>unboundid-spring</artifactId>
    <version>1.0</version>
</dependency>
```

However if you want to use the last snapshot version, you have to add the Sonatype OSS repository:

```xml
<repository>
    <id>sonatype-snapshots</id>
    <name>Sonatype repository for deploying snapshots</name>
    <url>https://oss.sonatype.org/content/repositories/snapshots</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```


TODO
----

* tests


License
-------

This project is licensed under [MIT License](http://opensource.org/licenses/MIT).
