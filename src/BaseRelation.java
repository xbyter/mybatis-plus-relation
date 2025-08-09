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
    List<Object> selfKeyValueList = compositeList.stream().map(getSelfKey()).distinct().collect(Collectors.toList());
    if (CollectionUtils.isEmpty(selfKeyValueList)) {
      return new ArrayList<>();
    }

    LambdaQueryWrapper<RE> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.in(getRelationKey(), selfKeyValueList);
    //附加查询条件
    if (queryWrapperConsumer != null) {
      queryWrapperConsumer.accept(queryWrapper);
    }

    return this.getRelationMapper().selectList(queryWrapper);
  }

  protected Map<Object, List<RE>> toRelationDataMap(List<RE> relationDataList) {
    if (CollectionUtils.isEmpty(relationDataList)) {
      return null;
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
