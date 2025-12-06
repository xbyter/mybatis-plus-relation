# mybatis-plus-relation
mybatis-plus关联查询解决方案, 代码实现简单, 已线上验证.

无需写SQL就可以实现关联子查询(支持嵌套, 避免N+1查询, 每个关联只查询一次).

## 使用示例
订单一对一关联地址, 一堆多关联订单商品. 订单商品一对多关联折扣.
### 订单和订单商品ComplisteEntity类定义
OrderComposite继承OrderEntity并扩展其关联属性. 查询的时候也建个OrderCompositeMapper方便查询关联. 
```java
@Data
public class OrderComposite extends OrderEntity {

    private OrderAddressEntity orderAddress;               // 一对一：订单收货地址
    private List<OrderProductComposite> orderProducts;     // 一对多：订单商品列表
}

@Data
public class OrderProductComposite extends OrderProductEntity {

    private List<OrderProductDiscountEntity> orderProductDiscounts;     // 一对多：订单商品折扣
}
```

### 订单和订单商品组合类Mapper
如果要多次使用关联查询的话就要重复多次建立关联关系, 建议把关联关系定义放在单独的类里或Mapper里. 这样可以集中管理和重复利用

```java
public interface OrderCompositeMapper extends BaseMapper<OrderComposite> {

	default HasMany<OrderComposite, OrderProductComposite> withOrderProducts() {
      return new HasMany<>(
              OrderComposite::setOrderProducts,
              SpringContextUtils.getBean(OrderProductMapper.class),
              OrderComposite::getOrderId,
              OrderProductComposite::getOrderId);
	}

	default HasOne<OrderComposite, OrderAddressEntity> withOrderAddress() {
	      return new HasOne<>(
	              OrderComposite::setOrderAddress,
	              SpringContextUtils.getBean(OrderAddressMapper.class),
	              OrderComposite::getOrderId,
	              OrderAddressEntity::getOrderId);
	}
}

public interface OrderProductCompositeMapper extends BaseMapper<OrderProductComposite> {

	default HasMany<OrderProductComposite, OrderProductDiscountEntity> withOrderProductDiscounts() {
      return new HasMany<>(
              OrderProductComposite::setOrderProductDiscounts,
              SpringContextUtils.getBean(OrderProductDiscountMapper.class),
              OrderProductComposite::getOrderProductId,
              OrderProductDiscountEntity::getOrderProductId);
	}
}
```

### 代码
```java
    LambdaQueryWrapper<OrderComposite> wrapper = new LambdaQueryWrapper<>();
    wrapper.in(OrderComposite::getOrderId, Arrays.asList(2023029, 2022919, 2023053));
    List<OrderComposite> orderComposites = orderCompositeMapper.selectList(wrapper);

	
	RelationManager<OrderComposite> relationManager = new RelationManager<>();
    //关联订单地址
	relationManager.addRelation(orderCompositeMapper.withOrderAddress());
    //关联订单商品
    relationManager.addRelation(orderCompositeMapper.withOrderProducts().addRelation(
        //关联订单商品折扣
        orderProductCompositeMapper.withOrderProductDiscounts()
    ));
	/**
	//也可以扩展查询条件
	relationManager.addRelation(orderCompositeMapper.withOrderProducts().setQueryWrapperConsumer(
		queryWrapper -> queryWrapper.like(OrderProductComposite::getProductName, "查询值")
	));
	**/

	relationManager.fillCompositeData(orderComposites);
```

###  填充后的结果结构示例
```java
List<OrderComposite> orderComposites = 
[
  {
    "orderId": 2023029,
    "orderNo": "ORD2023029001",
    "createTime": "2023-05-10T14:30:00",
    "orderAddress": {
      "addressId": 101,
      "orderId": 2023029,
      "receiverName": "张三",
      "fullAddress": "北京市海淀区..."
    },
    "orderProducts": [
      {
        "orderProductId": 5001,
        "orderId": 2023029,
        "productName": "智能手机",
        "price": 2999.00,
        "orderProductDiscounts": [
          { "discountId": 680, "orderProductId": 5001, "discountAmount": 200.00 }
        ]
      },
      {
        "orderProductId": 5002,
        "orderId": 2023029,
        "productName": "无线耳机",
        "price": 399.00,
        "orderProductDiscounts": [
          { "discountId": 50, "orderProductId": 5002, "discountAmount": 20.00 }
        ]
      }
    ]
  },
  {
    "orderId": 2022919,
    "orderNo": "ORD2022919002",
    "createTime": "2022-12-01T09:15:00",
    "orderAddress": {
      "addressId": 98,
      "orderId": 2022919,
      "receiverName": "李四",
      "fullAddress": "上海市浦东新区..."
    },
    "orderProducts": [
      {
        "orderProductId": 4888,
        "orderId": 2022919,
        "productName": "平板电脑",
        "price": 2499.00,
        "orderProductDiscounts": []
      }
    ]
  },
  {
    "orderId": 2023053,
    "orderNo": "ORD2023053003",
    "createTime": "2023-06-18T20:00:00",
    "orderAddress": null,  // 可能无地址
    "orderProducts": []    // 可能无商品（或被查询条件过滤）
  }
]
```
