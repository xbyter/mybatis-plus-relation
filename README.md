# mybatis-plus-relation
无需写SQL就可以实现关联子查询(支持嵌套, 每个关联查询只查询一次), 类似Laravel的with方法.

## 使用示例

```java
    //OrderComposite继承OrderEntity并扩展其关联属性. 查询的时候最好也建个OrderCompositeMapper, 其他Composite一样
	OrderComposite orderComposite = new OrderComposite();
    orderComposite.setOrderId(2023029L);

    OrderComposite orderComposite2 = new OrderComposite();
    orderComposite2.setOrderId(2022919L);


    OrderComposite orderComposite3 = new OrderComposite();
    orderComposite3.setOrderId(2023053L);


    List<OrderComposite> orderComposites = Arrays.asList(orderComposite, orderComposite2, orderComposite3);

    HasOne<OrderComposite, OrderAddressEntity> orderAddressRelation = new HasOne<>(
            OrderComposite::setOrderAddress,
            orderAddressMapper,
            OrderComposite::getOrderId,
            OrderAddressEntity::getOrderId);

    HasMany<OrderComposite, OrderProductComposite> orderProductsRelation = new HasMany<>(
            OrderComposite::setOrderProducts,
            orderProductCompositeMapper,
            OrderComposite::getOrderId,
            OrderProductComposite::getOrderId);
	
	//也可以扩展查询条件
	orderProductsRelation.setQueryWrapperConsumer(
		queryWrapper -> queryWrapper.like(OrderProductComposite::getProductName, "查询值")
	);
	
	//关联orderProducts的子表
	orderProductsRelation.addRelation(
		new HasMany<>(
            OrderProductComposite::setOrderProductDiscounts,
            orderProductDiscountMapper,
            OrderProductComposite::getOrderProductId,
            OrderProductDiscountEntity::getOrderProductId
		)
	);
	
	RelationManager<OrderComposite> relationManager = new RelationManager<>();
	relationManager.addRelation(orderAddressRelation);
	relationManager.addRelation(orderProductsRelation);
	relationManager.fillCompositeData(orderComposites);
```

## 使用建议
示例里面如果要多次使用关联查询的话就要重复多次建立关联关系, 建议把关联关系放在Mapper里, 比如

```java
public interface OrderCompositeMapper extend BaseMapper<OrderComposite> {

   default HasMany<OrderComposite, OrderProductComposite> withOrderProducts() {
      return new HasMany<>(
              OrderComposite::setOrderProducts,
              SpringContextUtils.getBean(OrderProductCompositeMapper.class),
              OrderComposite::getOrderId,
              OrderProductComposite::getOrderId);
   }
}

//使用示例
RelationManager<OrderComposite> relationManager = new RelationManager<>();
relationManager.addRelation(orderCompositeMapper.withOrderProducts());
relationManager.fillCompositeData(orderComposites);

```