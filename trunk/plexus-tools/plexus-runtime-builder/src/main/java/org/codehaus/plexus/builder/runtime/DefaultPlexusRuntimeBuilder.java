package org.codehaus.plexus.builder.runtime;

/*
 * Copyright (c) 2004, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.MavenMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.repository.RepositoryUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.velocity.VelocityComponent;
import org.codehaus.plexus.builder.AbstractBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="jason@zenplex.com">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @component
 */
public class DefaultPlexusRuntimeBuilder
    extends AbstractBuilder
    implements PlexusRuntimeBuilder
{
    private static String JSW = "jsw";

    private final static String CLASSWORLDS_TEMPLATE = "org/codehaus/plexus/builder/templates/classworlds.vm";

    private final static String UNIX_LAUNCHER_TEMPLATE = "org/codehaus/plexus/builder/templates/plexus.vm";

    private final static String WINDOWS_LAUNCHER_TEMPLATE = "org/codehaus/plexus/builder/templates/plexus-bat.vm";

    private final static String APPS_DIRECTORY = "apps";

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private String baseDirectory;

    private String applicationName;

    private String plexusConfiguration;

    private String configurationsDirectory;

    private String configurationPropertiesFile;

    private Properties configurationProperties;

    private File appLibDir;

    private File binDir;

    private File confDir;

    private File coreDir;

    private File bootDir;

    private File logsDir;

    private File tempDir;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private final static String[][] bootArtifacts =
        {
            {"classworlds", "classworlds"}
        };

    private Set artifacts;

    private Set plexusArtifacts;

    // ----------------------------------------------------------------------
    // Accessors
    // ----------------------------------------------------------------------

    public void setBaseDirectory( String baseDirectory )
    {
        this.baseDirectory = baseDirectory;
    }

    public void setApplicationName( String applicationName )
    {
        this.applicationName = applicationName;
    }

    public void setPlexusConfiguration( String plexusConfiguration )
    {
        this.plexusConfiguration = plexusConfiguration;
    }

    public void setConfigurationsDirectory( String configurationsDirectory )
    {
        this.configurationsDirectory = configurationsDirectory;
    }

    public void setConfigurationPropertiesFile( String configurationPropertiesFile )
    {
        this.configurationPropertiesFile = configurationPropertiesFile;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void build()
        throws PlexusRuntimeBuilderException
    {
        try
        {
            checkApplicationConfiguration();

            checkBaseDirectory();

            checkLocalRepository();

            createDirectoryStructure();

            createClassworldsConfiguration();

            createLauncherScripts();

            findArtifacts();

            copyBootDependencies();

            copyPlexusDependencies();

            copyApplicationDependencies();

            processMainConfiguration();

            processConfigurations();

            javaServiceWrapper();

            packageJavaRuntime();

            executable( baseDirectory + "/bin/plexus.sh" );
        }
        catch ( PlexusRuntimeBuilderException ex )
        {
            throw ex;
        }
        catch ( IOException ex )
        {
            throw new PlexusRuntimeBuilderException( "Exception while building the runtime.", ex );
        }
    }



    private void checkApplicationConfiguration()
        throws PlexusRuntimeBuilderException
    {
        if ( applicationName == null || applicationName.trim().length() == 0 )
        {
            throw new PlexusRuntimeBuilderException( "The application name must be set." );
        }
    }

    private void checkBaseDirectory()
        throws PlexusRuntimeBuilderException
    {
        if ( baseDirectory == null )
        {
            throw new PlexusRuntimeBuilderException( "The basedir must be specified." );
        }

        File f = new File( baseDirectory );

        mkdir( f );

        getLogger().info( "Building runtime in " + f.getAbsolutePath() );
    }

    private void checkLocalRepository()
        throws PlexusRuntimeBuilderException
    {
        if ( localRepository == null )
        {
            throw new PlexusRuntimeBuilderException( "The local Maven repository must be specified." );
        }
    }

    private void createDirectoryStructure()
    {
        appLibDir = new File( baseDirectory, APPS_DIRECTORY + "/" + applicationName + "/lib" );

        binDir = new File( baseDirectory, "bin" );

        confDir = new File( baseDirectory, "conf" );

        coreDir = new File( baseDirectory, "core" );

        bootDir = new File( baseDirectory, "core/boot" );

        logsDir = new File( baseDirectory, "logs" );

        tempDir = new File( baseDirectory, "temp" );

        mkdir( appLibDir );

        mkdir( binDir );

        mkdir( confDir );

        mkdir( bootDir );

        mkdir( logsDir );

        mkdir( tempDir );
    }

    /**
     * 1. Find all artifacts
     * 2. Find the plexus artifact
     * 3. Find all plexus artifacts
     */
    private void findArtifacts()
        throws PlexusRuntimeBuilderException
    {
        artifacts = findArtifacts( project, getRemoteRepositories(), getLocalRepository() );

        Artifact artifact = resolve( project.getGroupId(), project.getArtifactId(), project.getVersion(), project.getType() );

        artifacts.add( artifact );

        Artifact plexusArtifact = null;

        // find the plexus artifact
        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            artifact = (Artifact) it.next();

            String groupId = artifact.getGroupId();

            String artifactId = artifact.getArtifactId();

            String type = artifact.getType();

            if ( groupId.equals( "plexus" ) && artifactId.equals( "plexus" ) && type.equals( "jar" ) )
            {
                plexusArtifact = artifact;

                break;
            }
        }

        if ( plexusArtifact == null )
        {
            throw new PlexusRuntimeBuilderException( "Could not find plexus JAR in the dependency list." );
        }

        Artifact plexusPom = resolve( plexusArtifact.getGroupId(), plexusArtifact.getArtifactId(), plexusArtifact.getVersion(), "pom" );

        if ( plexusPom == null )
        {
            throw new PlexusRuntimeBuilderException( "Cannot find pom for: " + plexusArtifact.getId() );
        }

        MavenProject plexus = buildProject( plexusPom.getFile() );

        plexusArtifacts = findArtifacts( plexus, getRemoteRepositories(), getLocalRepository() );

        plexusArtifacts.add( plexusArtifact );

        Artifact appserver = new DefaultArtifact( "plexus", "plexus-appserver", "1.0-alpha-1-SNAPSHOT", "jar" );

        appserver.setPath( "/home/jvanzyl/maven-repo-local/plexus/jars/plexus-appserver-1.0-alpha-1-SNAPSHOT.jar" );

        plexusArtifacts.add( appserver );
    }

    private void copyBootDependencies()
        throws IOException
    {
        for ( Iterator it = artifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = (Artifact) it.next();

            if ( !isBootArtifact( artifact ) )
            {
                continue;
            }

            copyArtifact( artifact, bootDir );
        }
    }

    private void copyPlexusDependencies()
        throws IOException
    {
        for ( Iterator i = plexusArtifacts.iterator(); i.hasNext(); )
        {
            Artifact artifact = (Artifact) i.next();

            if ( isBootArtifact( artifact ) )
            {
                continue;
            }

            copyArtifact( artifact, coreDir );
        }
    }

    private void copyApplicationDependencies()
        throws PlexusRuntimeBuilderException, IOException
    {
        Iterator it = artifacts.iterator();

        Artifact artifact = new DefaultArtifact( project.getGroupId(), project.getArtifactId(), project.getVersion(), project.getType() );

        try
        {
            artifact = artifactResolver.resolve( artifact, getRemoteRepositories(), getLocalRepository() );
        }
        catch ( ArtifactResolutionException ex )
        {
            throw new PlexusRuntimeBuilderException( "Error while resolving the project artifact.", ex );
        }

        copyArtifact( artifact, appLibDir );

        while ( it.hasNext() )
        {
            artifact = (Artifact) it.next();

            if ( isBootArtifact( artifact ) || isPlexusArtifact( artifact ) )
            {
                continue;
            }

            copyArtifact( artifact, appLibDir );
        }
    }

    private void createClassworldsConfiguration()
        throws PlexusRuntimeBuilderException, IOException
    {
        mergeTemplate( CLASSWORLDS_TEMPLATE, new File( confDir, "classworlds.conf" ) );
    }

    private void createLauncherScripts()
        throws PlexusRuntimeBuilderException, IOException
    {
        mergeTemplate( UNIX_LAUNCHER_TEMPLATE, new File( binDir, "plexus.sh" ) );

        mergeTemplate( WINDOWS_LAUNCHER_TEMPLATE, new File( binDir, "plexus.bat" ) );
    }

    private Set findArtifacts( MavenProject project, Set repositories, ArtifactRepository localRepository )
        throws PlexusRuntimeBuilderException
    {
        ArtifactResolutionResult result;

        MavenMetadataSource metadata = new MavenMetadataSource( repositories, localRepository, artifactResolver );

        try
        {
            result = artifactResolver.resolveTransitively( project.getArtifacts(), repositories, localRepository, metadata );
        }
        catch ( ArtifactResolutionException ex )
        {
            throw new PlexusRuntimeBuilderException( "Exception while getting artifacts for " + project.getId() + ".", ex );
        }

        return new HashSet( result.getArtifacts().values() );
    }

    private void processMainConfiguration()
        throws PlexusRuntimeBuilderException, IOException
    {
        if ( plexusConfiguration == null )
        {
            throw new PlexusRuntimeBuilderException( "The plexus configuration file must be set." );
        }

        File in = new File( plexusConfiguration );

        if ( !in.exists() )
        {
            throw new PlexusRuntimeBuilderException( "The specified plexus configuration file " + "'" + in + "'" + " doesn't exist." );
        }

        File out = new File( confDir, "plexus.conf" );

        filterCopy( in, out, getConfigurationProperties() );
    }

    private void processConfigurations()
        throws PlexusRuntimeBuilderException, IOException
    {
        if ( configurationsDirectory == null )
        {
            return;
        }

        if ( configurationPropertiesFile == null )
        {
            throw new PlexusRuntimeBuilderException( "The configuration properties file must be set." );
        }

        DirectoryScanner scanner = new DirectoryScanner();

        scanner.setBasedir( configurationsDirectory );

        scanner.setExcludes( new String[]{configurationPropertiesFile, "**/CVS/**"} );

        scanner.scan();

        String[] files = scanner.getIncludedFiles();

        for ( int i = 0; i < files.length; i++ )
        {
            String file = files[i];

            File in = new File( configurationsDirectory, file );

            File out = new File( confDir, files[i] );

            filterCopy( in, out, getConfigurationProperties() );
        }
    }

    private Properties getConfigurationProperties()
        throws PlexusRuntimeBuilderException
    {
        if ( configurationProperties == null )
        {
            Properties p = new Properties();

            File input = new File( configurationPropertiesFile );

            try
            {
                p.load( new FileInputStream( input ) );
            }
            catch ( IOException ex )
            {
                throw new PlexusRuntimeBuilderException( "Exception while reading configuration file: " + input.getPath(), ex );
            }

            configurationProperties = new Properties();

            for ( Enumeration e = p.propertyNames(); e.hasMoreElements(); )
            {
                String name = (String) e.nextElement();

                configurationProperties.setProperty( name, p.getProperty( name ) );
            }
        }

        return configurationProperties;
    }

    private void javaServiceWrapper()
        throws PlexusRuntimeBuilderException, IOException
    {
        ClassLoader cl = getClass().getClassLoader();

        String[] linux = new String[]
        {
            "linux/libwrapper.so|x",
            "linux/run.sh|x",
            "linux/wrapper|x"
        };

        String[] windows = new String[]
        {
            "windows/InstallService.bat",
            "windows/UninstallService.bat",
            "windows/Wrapper.dll",
            "windows/Wrapper.exe",
            "windows/run.bat"
        };

        InputStream is = cl.getResourceAsStream( JSW + "/wrapper.jar" );

        OutputStream os = new FileOutputStream( new File( coreDir + "/wrapper.jar" ) );

        IOUtil.copy( is, os );

        filterCopy( cl.getResourceAsStream( JSW + "/wrapper.conf" ),
                    new File( confDir, "wrapper.conf" ),
                    getConfigurationProperties() );

        copyResources( "bin/linux", cl, linux );

        copyResources( "bin/windows", cl, windows );
    }

    protected void copyResources( String directory, ClassLoader cl, String[] resources )
        throws IOException
    {
        InputStream is;

        OutputStream os;

        File file = new File( baseDirectory, directory );

        file.mkdirs();

        for ( int i = 0; i < resources.length; i++ )
        {
            String[] s = StringUtils.split( resources[i], "|" );

            String resource = s[0];

            is = cl.getResourceAsStream( JSW + "/" + resource );

            if ( is == null )
            {
                continue;
            }

            File target = new File( binDir, resource );

            os = new FileOutputStream( target );

            IOUtil.copy( is, os );

            if ( s.length == 2 )
            {
                String instructions = s[1];

                if ( instructions.indexOf( "x" ) >= 0 )
                {
                    executable( target.getPath() );
                }
            }
        }
    }

    private void packageJavaRuntime()
        throws IOException
    {
    }

    private void copyArtifact( Artifact artifact, File destination )
        throws IOException
    {
        String dest = destination.getAbsolutePath().substring( baseDirectory.length() + 1 );

        getLogger().info( "Adding " + artifact.getId() + " to " + dest );

        FileUtils.copyFileToDirectory( artifact.getFile(), destination );

        artifacts.remove( artifact.getId() );
    }

    private MavenProject buildProject( File file )
        throws PlexusRuntimeBuilderException
    {
        try
        {
            return projectBuilder.build( file );
        }
        catch ( ProjectBuildingException ex )
        {
            throw new PlexusRuntimeBuilderException( "Error while building project: " + ex );
        }
    }

    private boolean isBootArtifact( Artifact artifact )
    {
        for ( int i = 0; i < bootArtifacts.length; i++ )
        {
            String groupId = artifact.getGroupId();

            String artifactId = artifact.getArtifactId();

            if ( bootArtifacts[i][0].equals( groupId ) &&
                bootArtifacts[i][1].equals( artifactId ) )
            {
                return true;
            }
        }

        return false;
    }

    private boolean isPlexusArtifact( Artifact artifact )
    {
        for ( Iterator it = plexusArtifacts.iterator(); it.hasNext(); )
        {
            Artifact a = (Artifact) it.next();

            if ( a.getGroupId().equals( artifact.getGroupId() ) &&
                a.getArtifactId().equals( artifact.getArtifactId() ) )
            {
                return true;
            }
        }

        return false;
    }
}
