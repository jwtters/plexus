package org.codehaus.plexus.registry;

/*
 * Copyright 2007, Brett Porter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.DefaultConfigurationBuilder;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the registry component using
 * <a href="http://jakarta.apache.org/commons/configuration">Commons Configuration</a>. The use of Commons Configuration
 * enables a variety of sources to be used, including XML files, properties, JNDI, JDBC, etc.
 * <p/>
 * The component can be configured using the {@link #properties} configuration item, the content of which should take
 * the format of an input to the Commons Configuration
 * <a href="http://jakarta.apache.org/commons/configuration/howto_configurationbuilder.html">configuration
 * builder</a>.
 *
 * @plexus.component role-hint="commons-configuration"
 */
public class CommonsConfigurationRegistry
    extends AbstractLogEnabled
    implements Registry, Initializable
{
    /**
     * The combined configuration instance that houses the registry.
     */
    private Configuration configuration;

    /**
     * The configuration properties for the registry. This should take the format of an input to the Commons
     * Configuration
     * <a href="http://jakarta.apache.org/commons/configuration/howto_configurationbuilder.html">configuration
     * builder</a>.
     *
     * @plexus.configuration
     */
    private PlexusConfiguration properties;

    public String dump()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "Configuration Dump." );
        for ( Iterator i = configuration.getKeys(); i.hasNext(); )
        {
            String key = (String) i.next();
            Object value = configuration.getProperty( key );
            buffer.append( "\n\"" ).append( key ).append( "\" = \"" ).append( value ).append( "\"" );
        }
        return buffer.toString();
    }

    public boolean isEmpty()
    {
        return configuration.isEmpty();
    }

    public Registry getSubRegistry( String key )
    {
        // TODO! ungross this
        CommonsConfigurationRegistry registry = new CommonsConfigurationRegistry();
        registry.configuration = this.configuration.subset( key );
        return registry;
    }

    public List getList( String key )
    {
        return configuration.getList( key );
    }

    public String getString( String key )
    {
        return configuration.getString( key );
    }

    public String getString( String key, String defaultValue )
    {
        return configuration.getString( key, defaultValue );
    }

    public int getInt( String key )
    {
        return configuration.getInt( key );
    }

    public int getInt( String key, int defaultValue )
    {
        return configuration.getInt( key, defaultValue );
    }

    public boolean getBoolean( String key )
    {
        return configuration.getBoolean( key );
    }

    public void addConfigurationFromResource( String resource )
        throws RegistryException
    {
        CombinedConfiguration configuration = (CombinedConfiguration) this.configuration;
        if ( resource.endsWith( ".properties" ) )
        {
            try
            {
                getLogger().debug( "Loading properties configuration from classloader resource: " + resource );
                configuration.addConfiguration( new PropertiesConfiguration( resource ) );
            }
            catch ( ConfigurationException e )
            {
                throw new RegistryException(
                    "Unable to add configuration from resource '" + resource + "': " + e.getMessage(), e );
            }
        }
        else if ( resource.endsWith( ".xml" ) )
        {
            try
            {
                getLogger().debug( "Loading XML configuration from classloader resource: " + resource );
                configuration.addConfiguration( new XMLConfiguration( resource ) );
            }
            catch ( ConfigurationException e )
            {
                throw new RegistryException(
                    "Unable to add configuration from resource '" + resource + "': " + e.getMessage(), e );
            }
        }
        else
        {
            throw new RegistryException(
                "Unable to add configuration from resource '" + resource + "': unrecognised type" );
        }
    }

    public void addConfigurationFromFile( File file )
        throws RegistryException
    {
        CombinedConfiguration configuration = (CombinedConfiguration) this.configuration;
        if ( file.getName().endsWith( ".properties" ) )
        {
            try
            {
                getLogger().debug( "Loading properties configuration from file: " + file );
                configuration.addConfiguration( new PropertiesConfiguration( file ) );
            }
            catch ( ConfigurationException e )
            {
                throw new RegistryException(
                    "Unable to add configuration from file '" + file.getName() + "': " + e.getMessage(), e );
            }
        }
        else if ( file.getName().endsWith( ".xml" ) )
        {
            try
            {
                getLogger().debug( "Loading XML configuration from file: " + file );
                configuration.addConfiguration( new XMLConfiguration( file ) );
            }
            catch ( ConfigurationException e )
            {
                throw new RegistryException(
                    "Unable to add configuration from file '" + file.getName() + "': " + e.getMessage(), e );
            }
        }
        else
        {
            throw new RegistryException(
                "Unable to add configuration from file '" + file.getName() + "': unrecognised type" );
        }
    }

    public void initialize()
        throws InitializationException
    {
        try
        {
            CombinedConfiguration configuration;
            if ( properties != null )
            {
                // TODO: changing this component to have a different configurator might be more appropriate?
                StringWriter w = new StringWriter();
                printConfiguration( properties, new PrintWriter( w ) );

                DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
                getLogger().debug( "Loading configuration into commons-configuration: " + w.toString() );
                builder.load( new StringReader( w.toString() ) );
                configuration = builder.getConfiguration( false );
            }
            else
            {
                getLogger().debug( "Creating a default configuration - no configuration was provided" );
                configuration = new CombinedConfiguration();
            }

            configuration.addConfiguration( new SystemConfiguration() );

            this.configuration = configuration;
        }
        catch ( ConfigurationException e )
        {
            throw new InitializationException( e.getMessage(), e );
        }
        catch ( PlexusConfigurationException e )
        {
            throw new InitializationException( e.getMessage(), e );
        }
    }

    private static void printConfiguration( PlexusConfiguration configuration, PrintWriter writer )
        throws PlexusConfigurationException
    {
        writer.print( "<" + configuration.getName() );
        String[] attrs = configuration.getAttributeNames();
        for ( int i = 0; i < attrs.length; i++ )
        {
            writer.print( " " + attrs[i] + "=\"" + configuration.getAttribute( attrs[i] ) + "\"" );
        }
        writer.print( ">" );
        if ( configuration.getChildCount() > 0 )
        {
            for ( int i = 0; i < configuration.getChildCount(); i++ )
            {
                writer.println();
                printConfiguration( configuration.getChild( i ), writer );
            }
        }
        else
        {
            writer.print( configuration.getValue() );
        }
        writer.println( "</" + configuration.getName() + ">" );
    }

    public void setProperties( PlexusConfiguration properties )
    {
        this.properties = properties;
    }
}