package com.accessofusion.skeleton.dao;

import com.accessofusion.skeleton.dao.sqlproviders.SkeletonResourceSqlProviderMybatis;
import com.accessofusion.skeleton.datamodel.SkeletonResourceEntity;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.domain.Pageable;

@Mapper
public interface SkeletonResourceDAOMybatis {

  @Select({
      "SELECT id, name, active",
      "FROM skeletonResource",
      "WHERE id = #{id}"
  })
  SkeletonResourceEntity findById(String id);
  
  @Insert({
      "INSERT INTO skeletonResource(id, name, active)",
      "VALUES(#{id}, #{name}, #{active})"
  })
  @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
  int insert(SkeletonResourceEntity skeletonResource);

  @Update({
      "UPDATE skeletonResource",
      "SET name=#{name}, active=#{active}",
      "WHERE id = #{id}"
  })
  int update(SkeletonResourceEntity skeletonResource);

  @SelectProvider(type = SkeletonResourceSqlProviderMybatis.class,
      method = "fetchAllWithPagingAndOrdering"
  )
  List<SkeletonResourceEntity> findAll(Pageable pageable);

  @Select("SELECT COUNT(id) FROM skeletonResource")
  Long count();

  @Select({
      "SELECT count(1)",
      "FROM skeletonResource",
      "WHERE id=#{id}"
  })
  boolean existsById(String id);

  /**
   * Returns the id of the skeletonResource with the name specified.
   */
  @Select({
      "SELECT id",
      "FROM skeletonResource",
      "WHERE name=#{name}"
  })
  String findIdByName(String name);
}