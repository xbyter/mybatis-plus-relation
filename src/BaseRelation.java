package app.extensions;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Getter;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 *
 * @param <CE> CompositeEntity
 * @param <RE> RelationEntity
 */
public abstract class BaseRelation<CE, RE> {
  @Getter
  private final List<BaseRelation<RE, ?>> relationList = new ArrayList<>();


  private Consumer<LambdaQueryWrapper<RE>> queryWrapperConsumer = null;

  public abstract BaseMapper<RE> getRelationMapper();

  public abstract SFunction<CE, ?> getSelfKey();

  public abstract SFunction<RE, ?> getRelationKey();

  protected List<RE> getRelationDataList(List<CE> compositeList) {
    //收集键
    List<Object> selfKeyValueList = compositeList.stream().map(getSelfKey()).distinct().collect(Collectors.toList());
    if (CollectionUtils.isEmpty(selfKeyValueList)) {
      return new ArrayList<>();
    }

    //查询关联表
    List<RE> result = new ArrayList<>();
    //关联键分批查询数量
    int batchSize = 500;
    for (int i = 0; i < selfKeyValueList.size(); i += batchSize) {
      // 截取当前批次
      List<Object> batchKeys = selfKeyValueList.subList(i, Math.min(i + batchSize, selfKeyValueList.size()));

      // 构建当前批次的查询条件
      LambdaQueryWrapper<RE> queryWrapper = new LambdaQueryWrapper<>();
      queryWrapper.in(getRelationKey(), batchKeys);

      // 附加额外查询条件
      if (queryWrapperConsumer != null) {
        queryWrapperConsumer.accept(queryWrapper);
      }

      // 执行查询并累加结果
      result.addAll(this.getRelationMapper().selectList(queryWrapper));
    }
    return result;
  }

  protected Map<Object, List<RE>> toRelationDataMap(List<RE> relationDataList) {
    if (CollectionUtils.isEmpty(relationDataList)) {
      return Collections.emptyMap();
    }
    return relationDataList.stream()
            .collect(Collectors.groupingBy(getRelationKey()));
  }


  /**
   *
   * @param <RE2> 新的关联Entity, 要在原来的RE(Relation Entity)中设置新关联数据, 所以原来的RE的位置换到了CE(Composite Entity)位置,
   */
  public  <RE2> BaseRelation<CE, RE> addRelation(BaseRelation<RE, RE2> relation) {
    relationList.add(relation);
    return this;
  }

  public BaseRelation<CE, RE> setQueryWrapperConsumer(Consumer<LambdaQueryWrapper<RE>> queryWrapperConsumer) {
    this.queryWrapperConsumer = queryWrapperConsumer;
    return this;
  }

  public void fillCompositeData(List<CE> compositeList) {
    if (CollectionUtils.isEmpty(compositeList)) {
      return;
    }

    //查询关联数据
    List<RE> relationDataList = getRelationDataList(compositeList);
    Map<Object, List<RE>> subMap = toRelationDataMap(relationDataList);
    if (CollectionUtils.isEmpty(subMap)) {
      return;
    }

    //填充关联数据
    compositeList.forEach(mainEntity -> {
      Object key = getSelfKey().apply(mainEntity);
      List<RE> groupedSubList = subMap.getOrDefault(key, Collections.emptyList());
      fillData(mainEntity, groupedSubList);
    });

    //子关联关系处理
    getRelationList().forEach(childRelation -> {
      childRelation.fillCompositeData(relationDataList);
    });
  }

  //如果是一对一的关系，则只填充一个值. 一对多的关系，则填充所有
  public abstract void fillData(CE mainEntity, List<RE> groupedSubList);
}
