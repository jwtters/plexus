/**
 *
 * Copyright 2007
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.codehaus.plexus.license;

import java.io.IOException;
import java.io.File;

/**
 * @author John Tolentino
 */
public class XmlLicenseChecker
    extends AbstractLicenseChecker
{
    public XmlLicenseChecker( File startLicenseFile, File endLicenseFile )
    {
        super( startLicenseFile, endLicenseFile );
    }

    public String removeCommentCharacters( File sourceFile )
        throws IOException
    {
        String processed = Utils.fileToString( sourceFile );

        processed = processed.replaceAll( "<!--+", "" );
        processed = processed.replaceAll( "--+>", "" );
        processed = processed.replaceAll( "\\s", "" );
        return processed;
    }
}