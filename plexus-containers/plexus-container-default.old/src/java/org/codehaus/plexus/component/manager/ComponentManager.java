package org.codehaus.plexus.component.manager;


import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.lifecycle.LifecycleHandler;
import org.codehaus.plexus.logging.Logger;

/**
 * Manages a component manager.
 * Determines when a component is shutdown, and when it's started up. Each
 * manager deals with only one component class, though may handle multiple
 * instances of this class.
 *
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public interface ComponentManager
{
    ComponentManager copy();

    static String ROLE = ComponentManager.class.getName();

    String getId();

    void setup( Logger logger, ClassLoader cl, LifecycleHandler h, ComponentDescriptor d )
        throws Exception;

    void initialize()
        throws Exception;

    int getConnections();

    ComponentDescriptor getComponentDescriptor();

    LifecycleHandler getLifecycleHandler();

    void dispose();

    void release( Object component );

    void suspend( Object component );

    void resume( Object component );

    Object getComponent()
        throws Exception;
}