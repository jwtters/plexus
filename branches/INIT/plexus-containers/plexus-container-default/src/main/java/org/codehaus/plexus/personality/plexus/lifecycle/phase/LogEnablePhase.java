package org.codehaus.plexus.personality.plexus.lifecycle.phase;

import org.codehaus.plexus.component.manager.ComponentManager;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.lifecycle.phase.AbstractPhase;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

public class LogEnablePhase
    extends AbstractPhase
{
    public void execute( Object object, ComponentManager componentManager )
        throws Exception
    {
        LoggerManager loggerManager;
        ComponentDescriptor descriptor;
        Logger logger;

        if ( object instanceof LogEnabled )
        {
            loggerManager = (LoggerManager) componentManager.getContainer().lookup( LoggerManager.ROLE );

            descriptor = componentManager.getComponentDescriptor();
            logger = loggerManager.getLoggerForComponent( descriptor.getRole(), descriptor.getRoleHint() );

            ( (LogEnabled) object ).enableLogging( logger );
        }
    }
}
