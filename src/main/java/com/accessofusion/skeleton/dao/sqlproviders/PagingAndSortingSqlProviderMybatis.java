/**
 * (C) 2019 Accesso Technology Group, plc. All Rights Reserved.
 */
package com.accessofusion.skeleton.dao.sqlproviders;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * SQL provider to customize queries requiring paging and sorting
 */
public abstract class PagingAndSortingSqlProviderMybatis {

  private static final String ASC = "ASC";
  private static final String DESC = "DESC";

  public String fetchAllWithPagingAndOrdering(@Param("pageable") Pageable pageable) {
    SQL sql = getBaseSqlForSelect();
    addSorting(sql, pageable.getSort());
    return addLimits(sql.toString(), pageable);
  }

  protected String addLimits(String sql, Pageable rowBounds) {
    StringBuilder sqlBuilder = new StringBuilder(sql);
    if (rowBounds.getOffset() == 0) {
      sqlBuilder.append(" LIMIT ");
      sqlBuilder.append(rowBounds.getPageSize());
    } else {
      sqlBuilder.append(" LIMIT ");
      sqlBuilder.append(rowBounds.getOffset());
      sqlBuilder.append(",");
      sqlBuilder.append(rowBounds.getPageSize());
    }
    return sqlBuilder.toString();
  }

  /**
   * @param sql SQL Object to append the sort criteria
   * @param sort fields to sort by
   */
  protected void addSorting(SQL sql, Sort sort) {
    if (null != sort && sort.isSorted()) {
      sort.forEach(sortField ->
          {
            sql.ORDER_BY(
                getSortCriteria(sortField.getProperty(), sortField.getDirection().isAscending()));
          }
      );
    }
  }

  /**
   * Gets the order by criteria if provided
   *
   * @param sortByField field name to sort by
   * @param asc if true sort is ascending, descending otherwise
   * @return the order by criteria
   */
  private String getSortCriteria(String sortByField, boolean asc) {
    return sortByField + " " + (asc ? ASC : DESC);
  }

  /**
   * Gets the default query to retrieve records
   *
   * @return base SQL for all the records queries
   */
  protected abstract SQL getBaseSqlForSelect();

}
