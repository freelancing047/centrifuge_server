/*
 * Copyright 1995-1996 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package csi.server.common.util.uploader.zip;

/*
 * This interface defines the constants that are used by the classes
 * which manipulate ZIP files.
 *
 * @author      David Connelly
 */
public class CsiZipConstants {
   /*
    * Header signatures
    */
   public static final long LOCSIG = 0x04034b50L; // "PK\003\004"
   public static final long EXTSIG = 0x08074b50L; // "PK\007\008"
   public static final long CENSIG = 0x02014b50L; // "PK\001\002"
   public static final long ENDSIG = 0x06054b50L; // "PK\005\006"

   /**
    * Compression method. This method doesn't compress at all.
    */
   public static final int STORED = 0;

   /**
    * Compression method. This method uses the Deflater.
    */
   public static final int DEFLATED = 8;

   /**
    * From "interface DeflaterConstants"
    */
   public static final int STORED_BLOCK = 0;
   public static final int STATIC_TREES = 1;
   public static final int DYN_TREES = 2;
}
