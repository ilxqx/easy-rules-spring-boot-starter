# 🔨 引入依赖

```xml
<dependency>
    <groupId>com.ilxqx</groupId>
    <artifactId>easy-rules-spring-boot-starter</artifactId>
    <version>4.2.0</version>
</dependency>
```

# 🏷 使用方法

使用前，`application.yml` 配置中需要增加：

```yaml
easy:
  rules:
    enabled: true
    rules-engine-type: plain # 指定规则引擎的实现，plain是本starter新增的一个简单执行引擎
```

必要重要的注解：
`@RuleGroup` 用于定义该类是一个Spring管理的规则`Bean`，而且属于那个规则组。
`@Rule` 用于定义规则的元信息，比如规则名称、描述等。

```java
@RuleGroup(name = "orderValidation")
@Rule(name = "commodityStockValidation", description = "商品库存验证", priority = 3)
@RequiredArgsConstructor
public class CommodityStockValidationRule extends CommodityStockRule {

    // 引入自己的相关处理Service
    // 这里以商品Service为例
    private final CommodityService commodityService;

    /**
     * 这里是条件评估，如果返回 true 则执行规则
     * 
     * @param order 订单对象 这里就是调用触发规则的地方传入的事实对象
     * @return bool
     */
    @Condition
    public boolean evaluate(@Fact(EasyRulesTemplate.DEFAULT_FACT_NAME) Order order) {
        return OrderUtil.isCustomerOrder(order) && WarehouseType.OWN.equals(order.getOrderDetail().getWarehouseType()) && (
            (
                !OrderUtil.isInBulkOrder(order) && OrderEnv.CREATION.equals(order.getEnv())
            ) || (
                OrderUtil.isInBulkOrder(order) && (
                    OrderEnv.PICKUP_COMPLETION.equals(order.getEnv()) ||
                        OrderEnv.AUDIT_COMPLETION.equals(order.getEnv())
                )
            )
        );
    }

    /**
     * 执行验证逻辑 也就是执行规则
     *
     * @param order 订单对象 这个也是从调用处传入的事实对象
     */
    @Action
    public void validate(@Fact(EasyRulesTemplate.DEFAULT_FACT_NAME) Order order) {
        // 验证库存
        List<Commodity> commodities = this.fetchOrderCommodities(order, commodityService);
        for (OrderCommodity orderCommodity : order.getOrderCommodities()) {
            Commodity cm = commodities.stream().filter(commodity -> StrUtil.equals(commodity.getId(), orderCommodity.getCommodityId()))
                .findFirst().orElseThrow(() -> new UpdateNotAllowedException("商品「" + orderCommodity.getCommodityName() + "」缺失！"));
            if (orderCommodity.getCommodityQuantity().compareTo(OrderUtil.convertUnitStock(cm, orderCommodity.getCommodityUnit())) > 0) {
                throw new UpdateNotAllowedException("商品「" + orderCommodity.getCommodityName() + "」库存不足！");
            }
        }
    }
}
```

有了规则组的定义，那就可以触发规则了。触发方法如下：

```java
@Service
@RequiredArgsConstructor
public class DemoService {
    // 引入 easyRulesTemplate
    private final EasyRulesTemplate easyRulesTemplate;
    
    public void executeRule(Order order) {
        // 触发指定的规则组
        this.easyRulesTemplate.fire("orderValidation", order);
    }
}
```

这里说一下，`fire` 方法有几个重载，但都对事实对象的传递过程的简化。
