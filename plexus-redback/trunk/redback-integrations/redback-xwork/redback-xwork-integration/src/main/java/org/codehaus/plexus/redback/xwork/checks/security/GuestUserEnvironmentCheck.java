package org.codehaus.plexus.redback.xwork.checks.security;

/*
 * Copyright 2005-2006 The Codehaus.
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

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.redback.rbac.profile.RoleProfileException;
import org.codehaus.plexus.redback.rbac.profile.RoleProfileManager;
import org.codehaus.plexus.redback.policy.UserSecurityPolicy;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.Role;
import org.codehaus.plexus.redback.rbac.UserAssignment;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.system.check.EnvironmentCheck;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;

import java.util.List;

/**
 * RequiredRolesEnvironmentCheck:
 *
 * @author: Jesse McConnell <jesse@codehaus.org>
 * @version: $ID:$
 * @plexus.component role="org.codehaus.plexus.redback.system.check.EnvironmentCheck"
 * role-hint="guest-user-check"
 */
public class GuestUserEnvironmentCheck
    extends AbstractLogEnabled
    implements EnvironmentCheck
{

    /**
     * @plexus.requirement role-hint=default
     */
    private RoleProfileManager roleProfileManager;

    /**
     * @plexus.requirement
     */
    private RBACManager rbacManager;

    /**
     * @plexus.requirement
     */
    private SecuritySystem securitySystem;

    /**
     * boolean detailing if this environment check has been executed
     */
    private boolean checked = false;

    /**
     * @param violations
     */
    public void validateEnvironment( List violations )
    {
        if ( !checked )
        {
            UserManager userManager = securitySystem.getUserManager();
            UserSecurityPolicy policy = securitySystem.getPolicy();

            User guest;

            try
            {
                guest = userManager.findUser( "guest" );
            }
            catch ( UserNotFoundException ne )
            {
                policy.setEnabled( false );

                guest = userManager.createUser( "guest", "Guest", "" );
                guest.setPermanent( true );
                guest = userManager.addUser( guest );

            }
            finally
            {
                policy.setEnabled( true );
            }

            try
            {
                Role guestRole = roleProfileManager.getRole( "guest" );
                UserAssignment ua = rbacManager.createUserAssignment( guest.getPrincipal().toString() );
                ua.addRoleName( guestRole );
                ua.setPermanent( true );
                rbacManager.saveUserAssignment( ua );

            }
            catch ( RoleProfileException rpe )
            {
                violations.add( "unable to initialize guest user properly: " + rpe.getMessage() );
            }
            catch ( RbacManagerException e )
            {
                violations.add( "unable to initialize guest user properly: " + e.getMessage() );
                getLogger().warn( "System error:", e );
            }

            checked = true;
        }
    }
}