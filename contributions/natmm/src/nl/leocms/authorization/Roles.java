/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is LeoCMS.
 *
 * The Initial Developer of the Original Code is
 * 'De Gemeente Leeuwarden' (The dutch municipality Leeuwarden).
 *
 * See license.txt in the root of the LeoCMS directory for the full license.
 */
package nl.leocms.authorization;

/**
 * @author Edwin van der Elst
 * Date :Oct 7, 2003
 * 
 */
public interface Roles {
   public static int LEZER = 0; // DENY all rights
   public static int SCHRIJVER = 1;
   public static int REDACTEUR = 2;
   public static int EINDREDACTEUR = 3;
   public static int WEBMASTER = 100; // ALLES

}
