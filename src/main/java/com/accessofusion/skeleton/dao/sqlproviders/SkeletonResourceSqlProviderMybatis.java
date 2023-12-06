/*
 * *
 *  * (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 *
 */

/**
 * (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 */
package com.accessofusion.skeleton.dao.sqlproviders;

import org.apache.ibatis.jdbc.SQL;

public class SkeletonResourceSqlProviderMybatis extends PagingAndSortingSqlProviderMybatis {

  @Override
  protected SQL getBaseSqlForSelect() {
    return new SQL().SELECT("id, name, active")
        .FROM("skeletonResource");
  }

}
