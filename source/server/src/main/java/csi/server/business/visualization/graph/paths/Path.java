/*
 *
 * Copyright (c) 2004-2008 Arizona State University.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ARIZONA STATE UNIVERSITY ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL ARIZONA STATE UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package csi.server.business.visualization.graph.paths;

import java.util.ArrayList;
import java.util.List;

import prefuse.data.Node;

/**
 * @author <a href='mailto:Yan.Qi@asu.edu'>Yan Qi</a>
 * @version $Revision: 673 $
 * @latest $Date: 2009-02-05 01:19:18 -0700 (Thu, 05 Feb 2009) $
 */
public class Path implements BaseElementWithWeight {
   private List<Node> vertices = new ArrayList<Node>();
   private double weight = -1;
   private String id;

   public Path() {
   }

   public Path(List<Node> vertices, double weight) {
      this.vertices = vertices;
      this.weight = weight;
   }

   public double get_weight() {
      return weight;
   }

   public void set_weight(double weight) {
      this.weight = weight;
   }

   public List<Node> get_vertices() {
      return vertices;
   }

   /*
    * (non-Javadoc)
    *
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object right) {
      boolean result = false;

      if (right instanceof Path) {
         Path r_path = (Path) right;

         result = vertices.equals(r_path.vertices);
      }
      return result;
   }

   /*
    * (non-Javadoc)
    *
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      return vertices.hashCode();
   }

   public String toString() {
      return new StringBuilder(vertices.toString()).append(":").append(weight).toString();
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getId() {
      return this.id;
   }
}
