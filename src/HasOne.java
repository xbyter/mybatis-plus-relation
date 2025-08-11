package app.extensions;

import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 一对一关联关系, 比如一个订单(id)有一个地址(order_id)
 * @param <CE> CompositeEntity 订单
 * @param <RE> RelationEntity 订单地址
 */
@Getter
@Slf4j
public class HasOne<CE, RE> extends BaseRelation<CE, RE> {

  protected BiConsumer<CE, RE> setter;

  protected BaseMapper<RE> relationMapper;

  protected SFunction<CE, ?> selfKey;

  protected SFunction<RE, ?> relationKey;

  public HasOne(BiConsumer<CE, RE> setter, BaseMapper<RE> relationMapper, SFunction<CE, ?> selfKey, SFunction<RE, ?> relationKey) {
    this.setter = setter;
    this.relationMapper = relationMapper;
    this.selfKey = selfKey;
    this.relationKey = relationKey;
  }

  //如果是一对一的关系，则只填充一个值. 一对多的关系，则填充所有
  @Override
  public void fillData(CE mainEntity, List<RE> groupedSubList) {
    if (groupedSubList != null && !groupedSubList.isEmpty()) {
      //查出的数量大于1个，说明有脏数据记录警告日志
      if (groupedSubList.size() > 1) {
        log.warn("HasOne relation found more than one result. SelfKey: {}, Count: {}. Using the first one.",
                getSelfKey().apply(mainEntity), groupedSubList.size());
      }
      getSetter().accept(mainEntity, groupedSubList.get(0));
    }
  }
}
