package org.codehaus.plexus.security.ui.web.util;

/*
 * Copyright 2001-2006 The Codehaus.
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

import org.codehaus.plexus.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CookieUtils 
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public class CookieUtils
{
    public static void invalidateCookie( HttpServletRequest request, HttpServletResponse response, String name )
    {
        Cookie cookie = getCookie( request, name );
        if ( cookie != null )
        {
            setCookie( response, name, null, 0, cookie.getPath() );
        }
    }

    public static Cookie setCookie( HttpServletResponse response, String name, String value, int maxAge, String path )
    {
        Cookie cookie = new Cookie( name, value );
        cookie.setMaxAge( maxAge );
        cookie.setPath( path );
        response.addCookie( cookie );

        return cookie;
    }

    public static Cookie getCookie( HttpServletRequest request, String name )
    {
        Cookie cookies[] = request.getCookies();

        if ( cookies == null || StringUtils.isEmpty( name ) )
        {
            return null;
        }

        for ( int i = 0; i < cookies.length; i++ )
        {
            if ( StringUtils.equals( name, cookies[i].getName() ) )
            {
                return cookies[i];
            }
        }

        return null;
    }
}
