package app.extensions;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Data;

import java.util.List;
import java.util.function.BiConsumer;

/**
 *
 * @param <CE> CompositeEntity
 * @param <RE> RelationEntity
 */
@Data
public class OneToMany<CE, RE> extends BaseRelation<CE, RE> {

  private BiConsumer<CE, List<RE>> setter;

  private BaseMapper<RE> relationMapper;

  private SFunction<CE, ?> selfKey;

  private SFunction<RE, ?> relationKey;

  public OneToMany(BiConsumer<CE, List<RE>> setter, BaseMapper<RE> relationMapper, SFunction<CE, ?> selfKey, SFunction<RE, ?> relationKey) {
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
