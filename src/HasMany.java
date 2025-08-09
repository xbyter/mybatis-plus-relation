package app.extensions;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 一对多, 比如一个订单(id)有多个订单产品(order_id)
 * @param <CE> CompositeEntity 订单
 * @param <RE> RelationEntity 订单产品
 */
@Data
public class HasMany<CE, RE> extends BaseRelation<CE, RE> {

  protected BiConsumer<CE, List<RE>> setter;

  protected BaseMapper<RE> relationMapper;

  protected SFunction<CE, ?> selfKey;

  protected SFunction<RE, ?> relationKey;

  public HasMany(BiConsumer<CE, List<RE>> setter, BaseMapper<RE> relationMapper, SFunction<CE, ?> selfKey, SFunction<RE, ?> relationKey) {
    this.setter = setter;
    this.relationMapper = relationMapper;
    this.selfKey = selfKey;
    this.relationKey = relationKey;
  }


  //如果是一对一的关系，则只填充一个值. 一对多的关系，则填充所有
  public void fillData(CE mainEntity, List<RE> groupedSubList) {
    getSetter().accept(mainEntity, groupedSubList);
  }
}
