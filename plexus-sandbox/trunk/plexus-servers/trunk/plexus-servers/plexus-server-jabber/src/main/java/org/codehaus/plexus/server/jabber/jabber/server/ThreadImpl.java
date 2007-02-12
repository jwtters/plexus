/*
 * BSD License http://open-im.org/bsd-license.html
 * Copyright (c) 2003, OpenIM Project http://open-im.org
 * All rights reserved.
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenIM project. For more
 * information on the OpenIM project, please see
 * http://open-im.org/
 */
package org.codehaus.plexus.server.jabber.jabber.server;


import org.codehaus.plexus.server.jabber.DefaultSessionProcessor;
import org.codehaus.plexus.server.jabber.data.jabber.MessagePacket;
import org.codehaus.plexus.server.jabber.session.IMSession;

/**
 * @author AlAg
 * @version 1.0
 * @avalon.component version="1.0" name="server.Thread" lifestyle="singleton"
 * @avalon.service type="org.codehaus.plexus.server.jabber.jabber.server.Thread"
 */
public class ThreadImpl
    extends DefaultSessionProcessor
    implements Thread
{


    public void processText( final IMSession session,
                             final Object context )
        throws Exception
    {
        ( (MessagePacket) context ).setThread( session.getXmlPullParser().getText().trim() );
    }

}

