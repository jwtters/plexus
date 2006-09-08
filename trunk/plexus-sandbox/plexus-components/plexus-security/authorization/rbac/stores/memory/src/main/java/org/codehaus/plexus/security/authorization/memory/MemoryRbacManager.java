package org.codehaus.plexus.security.authorization.memory;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.security.rbac.Operation;
import org.codehaus.plexus.security.rbac.Permission;
import org.codehaus.plexus.security.rbac.RBACManager;
import org.codehaus.plexus.security.rbac.RbacObjectNotFoundException;
import org.codehaus.plexus.security.rbac.RbacStoreException;
import org.codehaus.plexus.security.rbac.Resource;
import org.codehaus.plexus.security.rbac.Role;
import org.codehaus.plexus.security.rbac.Roles;
import org.codehaus.plexus.security.rbac.UserAssignment;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * MemoryRbacManager: a very quick and dirty implementation of a rbac store
 *
 * WARNING: not for actual usage, its not sound - jesse
 *
 * @author Jesse McConnell <jmcconnell@apache.org>
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id:$
 *
 * @plexus.component
 *   role="org.codehaus.plexus.security.rbac.RBACManager"
 *   role-hint="memory"
 */
public class MemoryRbacManager
    implements RBACManager, Initializable
{
    RBACManager store = null;

    public void initialize()
        throws InitializationException
    {
//        RBACModelXpp3Reader reader = new RBACModelXpp3Reader();
//
//        try
//        {
//            store = reader.read( new FileReader( getBasedir() + File.separator + "rbac-store-model.xml" ) );
//        }
//        catch ( Exception e )
//        {
//            throw new InitializationException( "error loading file rbac-store-model.xml", e );
//        }
    }


    public void store()
        throws Exception
    {
//        RBACModelXpp3Writer writer = new RBACModelXpp3Writer();
//
//        writer.write( new FileWriter( getBasedir() + File.separator + "rbac-store-model.xml"), store);
    }



    public static String getBasedir()
    {
        String basedir = System.getProperty( "basedir" );

        if ( basedir == null )
        {
            basedir = new File( "" ).getAbsolutePath();
        }

        return basedir;
    }


    // ----------------------------------------------------------------------
    // Role methods
    // ----------------------------------------------------------------------
    public Role addRole( Role role )
        throws RbacStoreException
    {
        store.addRole( role );
        return role;
    }

    public Role getRole( int roleId )
        throws RbacStoreException
    {
        for (Iterator i = store.getAllRoles().iterator(); i.hasNext(); )
        {
            Role role = (Role)i.next();

            if ( role.getId() == roleId )
            {
                return role;
            }
        }

        throw new RbacStoreException( "unable to locate role: " + roleId );
    }

    public List getAllRoles()
        throws RbacStoreException
    {
        List gatheredRoles = new ArrayList();

        for (Iterator i = store.getAllRoles().iterator(); i.hasNext(); )
        {
            Role role = (Role) i.next();

            gatherRoles( gatheredRoles, role );
        }

        return gatheredRoles;
    }

    private void gatherRoles( List gatheredRoles, Role role )
    {
        if ( role.hasChildRoles() )
        {
            for ( Iterator i = role.getChildRoles().iterator(); i.hasNext(); )
            {
                Role r = (Role)i.next();

                gatheredRoles.add( r );

                gatherRoles( gatheredRoles, r );
            }
        }
        else
        {
            gatheredRoles.add( role );
        }
    }

    public List getAssignableRoles()
        throws RbacStoreException
    {
        List assignableRoles = new ArrayList();

        for ( Iterator i = store.getAllRoles().iterator(); i.hasNext(); )
        {
            Role role = (Role)i.next();

            if ( role.isAssignable() )
            {
                assignableRoles.add( role );
            }
        }

        return assignableRoles;
    }

    public void removeRole( int roleId )
        throws RbacStoreException, RbacObjectNotFoundException
    {
        // just removing top lvl roles atm.
        if ( getRole( roleId) != null )
        {
            store.removeRole( getRole( roleId ) );
        }
    }

    // ----------------------------------------------------------------------
    // Permission methods
    // ----------------------------------------------------------------------
    public void addPermission( int roleId, Permission permission )
        throws RbacStoreException
    {
        Role role = getRole( roleId );

        role.addPermission( permission );
    }

    public List getPermissions( int roleId )
        throws RbacStoreException
    {
        return getRole( roleId ).getPermissions();
    }


    public Operation addOperation( Operation operation )
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Permission addPermission( Permission permission )
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Resource addResource( Resource resource )
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public UserAssignment addUserAssignment( UserAssignment userAssignment )
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Operation createOperation( String name, String description )
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Permission createPermission( String name, String description )
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Permission createPermission( String name, String description, String operation, String resource )
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Resource createResource( String identifier )
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Role createRole( String name, String description )
    {
        // TODO Auto-generated method stub
        return null;
    }


    public UserAssignment createUserAssignment( Object principal )
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Operation getOperation( int operationId )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public List getOperations()
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Permission getPermission( int permissionId )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public List getPermissions()
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Resource getResource( int resourceId )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public List getResources()
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public List getRoles()
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public UserAssignment getUserAssignment( Object principal )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public List getUserAssignments()
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public void removeOperation( Operation operation )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        
    }


    public void removePermission( Permission permission )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        
    }


    public void removeResource( Resource resource )
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        
    }


    public void removeRole( Role role )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        
    }


    public void removeUserAssignment( UserAssignment userAssignment )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        
    }


    public void setOperations( List operation )
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        
    }


    public void setPermissions( List permissions )
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        
    }


    public void setResources( List resources )
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        
    }


    public void setRoles( List roles )
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        
    }


    public void setUserAssignments( List assignments )
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        
    }


    public Operation updateOperation( Operation operation )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Permission updatePermission( Permission permission )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Resource updateResource( Resource resource )
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Role updateRole( Role role )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public UserAssignment updateUserAssignment( UserAssignment userAssignment )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Set getAssignedPermissions( Object principal )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public UserAssignment createUserAssignment( String principal )
    {
        // TODO Auto-generated method stub
        return null;
    }


    public List getAllOperations()
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public List getAllPermissions()
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public List getAllResources()
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public List getAllUserAssignments()
        throws RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public UserAssignment getUserAssignment( String principal )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Roles getAssignedRoles( Object principal )
        throws RbacObjectNotFoundException, RbacStoreException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
