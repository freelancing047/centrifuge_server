/* VirtualMachineError.java
   Copyright (C) 1998, 1999, 2001 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

As a special exception, if you link this library with other files to
produce an executable, this library does not by itself cause the
resulting executable to be covered by the GNU General Public License.
This exception does not however invalidate any other reasons why the
executable file might be covered by the GNU General Public License. */

package csi.server.common.exception;

/* Written using "Java Class Libraries", 2nd edition, ISBN 0-201-31002-3
 * "The Java Language Specification", ISBN 0-201-63451-1
 * plus online API docs for JDK 1.2 beta from http://www.javasoft.com.
 * Status:  Believed complete and correct.
 */

/**
 * A <code>VirtualMachineError</code> or its subclasses are thrown to indicate
 * there is something wrong with the Java Virtual Machine or that it does not
 * have the resources needed for it to continue execution.
 *
 * @since JDK 1.0
 *
 * @author Brian Jones
 * @author Tom Tromey <tromey@cygnus.com>
 * @date October 1, 1998
 */
public abstract class AbstractVirtualMachineError extends Error {
   static final long serialVersionUID = 4161983926571568670L;

   /**
    * Create an error without a message.
    */
   public AbstractVirtualMachineError() {
      super();
   }

   /**
    * Create an error with a message.
    */
   public AbstractVirtualMachineError(String s) {
      super(s);
   }
}
