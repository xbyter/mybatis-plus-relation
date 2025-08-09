package app.extensions;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Data;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 所属一对一关系, 出入参同HasOne, 仅做语义上区分. 比如订单商品表(order_id)要关联出相应的订单信息(id)
 * @param <CE> CompositeEntity 订单产品
 * @param <RE> RelationEntity 订单
 */
public class BelongsTo<CE, RE> extends HasOne<CE, RE> {

  public BelongsTo(BiConsumer<CE, RE> setter, BaseMapper<RE> relationMapper, SFunction<CE, ?> selfKey, SFunction<RE, ?> relationKey) {
    super(setter, relationMapper, selfKey, relationKey);
  }
}
