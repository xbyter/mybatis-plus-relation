package app.extensions;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @param <CE> CompositeEntity
 */
@Getter
public class RelationManager<CE> {

  private final List<BaseRelation<CE, ?>> relationList = new ArrayList<>();

  public <RE> RelationManager<CE> addRelation(BaseRelation<CE, RE> relation) {
    this.relationList.add(relation);
    return this;
  }

  public void fillCompositeData(CE composite) {
    if (composite == null) {
      return;
    }
    fillCompositeData(Collections.singletonList(composite));
  }

  public void fillCompositeData(List<CE> compositeList) {
    for (BaseRelation<CE, ?> relation : relationList) {
      relation.fillCompositeData(compositeList);
    }
  }
}
