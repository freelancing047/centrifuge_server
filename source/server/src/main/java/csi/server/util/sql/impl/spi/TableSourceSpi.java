/** 
 *  Copyright (c) 2008 Centrifuge Systems, Inc. 
 *  All rights reserved. 
 *   
 *  This software is the confidential and proprietary information of 
 *  Centrifuge Systems, Inc. ("Confidential Information").  You shall 
 *  not disclose such Confidential Information and shall use it only
 *  in accordance with the terms of the license agreement you entered 
 *  into with Centrifuge Systems.
 *
 **/
package csi.server.util.sql.impl.spi;

import csi.server.util.sql.TableSource;


/**
 * @author Centrifuge Systems, Inc.
 *
 */
public interface TableSourceSpi extends TableSource, HasAlias, HasAliasedSQLRepresentation {

}
