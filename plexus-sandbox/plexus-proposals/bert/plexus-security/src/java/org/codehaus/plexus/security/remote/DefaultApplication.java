package org.codehaus.plexus.security.remote;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.codehaus.plexus.security.PlexusSession;
import org.codehaus.plexus.security.SecurityService;
import org.codehaus.plexus.security.authentication.AuthenticationException;
import org.codehaus.plexus.security.request.RequestInterceptor;
import org.codehaus.plexus.security.request.SecurityRequestInterceptorDelegate;
import org.codehaus.plexus.security.request.proxy.ProxyServiceManagerDelegate;
import org.codehaus.plexus.lifecycle.avalon.RestrictedServiceManagerDelegate;

/**
 * <p>Configuration format is as follows:
 * 
 * <pre><![CDATA[
 * 	<name>myAppName</name>
 * 	<version>myAppVersion</version>
 * 	<build>myAppBuild</build>
 * 	<registeredTo>myAppRegisteredTo</registeredTo>
 *		<allow-components>
 *			<role>myRole</role>
 *			<role>anotherRole</role>
 *  	</allow-components>
 * ]]>
 * </pre>
 * 
 * <p>Requires:
 * <ul>
 * 	<li>SecurityService</li>
 * </ul>
 * </p>
 * 
  * <p>Created on 20/08/2003</p>
  *
  * @author <a href="mailto:bert@tuaworks.co.nz">Bert van Brakel</a>
  * @revision $Revision$
  */
public class DefaultApplication
    implements Application, Serviceable, Initializable, Disposable, Configurable
{
    private String appName;
    private String appVersion;
    private String appBuild;
    private String appRegisteredTo;

    /** The security service to authenticate against */
    private SecurityService security;

    /** The source ServiceManager */
    private ServiceManager service;

    /** Wraps the RestrictedServiceManager to build proxied components which
     * begin and end a request on each method invocation */
    private ProxyServiceManagerDelegate proxyServiceManager;

	/** The request interceptor to pass to the proxyServiceManager to enable proxied 
	 * components to notify the system of request demarcation */
	private RequestInterceptor requestInterceptor;
    /** Configuration passed in by the container */
    private Configuration config;
    
    //private Map session = new ThreadSafeMap();
    
    /**
     * @see org.codehaus.plexus.security.remote.Application#authenticate(java.lang.Object)
     */
    public ApplicationSession authenticate(Object token) throws AuthenticationException
    {
    	
    	//@todo possibly keep track of the ApplicationSession so we return the same one
    	//even if the client re authenticates.
    	PlexusSession sess = security.authenticate(token);
    	DefaultApplicationSession appSession = new DefaultApplicationSession(sess,proxyServiceManager,requestInterceptor);
        return appSession;
    }

    /**
     * @see org.codehaus.plexus.security.remote.Application#getBuild()
     */
    public String getBuild()
    {
        return appBuild;
    }

    /**
     * @see org.codehaus.plexus.security.remote.Application#getName()
     */
    public String getName()
    {
        return appName;
    }

    /**
     * @see org.codehaus.plexus.security.remote.Application#getVersion()
     */
    public String getVersion()
    {
        return appVersion;
    }

    /**
     * @see org.codehaus.plexus.security.remote.Application#getRegisteredTo()
     */
    public String getRegisteredTo()
    {
        return appRegisteredTo;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager service) throws ServiceException
    {
        this.service = service;
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception
    {
        security = (SecurityService) service.lookup(SecurityService.ROLE);
        //configure the service manager which restricts access to only a  specified 
        //list of components
		RestrictedServiceManagerDelegate resServiceManager = new RestrictedServiceManagerDelegate();
        resServiceManager.service(service);
        resServiceManager.configure(config);
		//now build a proxy serviceManager which exposes only these restricted 
		//components
        proxyServiceManager = new ProxyServiceManagerDelegate();
        proxyServiceManager.service(resServiceManager);
        
        //setup the request interceptor to delegate to the securityService the
        //task of handling request demarcation
        requestInterceptor = new SecurityRequestInterceptorDelegate( security );
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose()
    {
        if (security != null)
        {
            service.release(security);
        }
        service = null;
        security = null;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException
    {
        this.config = config;
        appBuild = config.getChild("build").getValue();
        appVersion = config.getChild("version").getValue();
        appName = config.getChild("name").getValue();
        appRegisteredTo = config.getChild("registeredTo").getValue();
    }
    
    

}
