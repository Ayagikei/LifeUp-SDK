# reward

Source: lifeup-wiki `docs/zh-cn/guide/api.md` (may lag).

**方法名：**reward

**说明：**直接提供奖励，可定制奖励理由

**示例：**

- 获得 1 点金币，获得原因为「学习API调用」。且原因会在金币详情页面展示：

  [lifeup://api/reward?type=coin&content=学习API调用&number=1](lifeup://api/reward?type=coin&content=学习API调用&number=1)

- 获得 300 点「学识、创造力」经验值，获得原因为「学习API调用」。且原因会在经验值详情页面展示：

  [lifeup://api/reward?type=exp&content=学习API调用&number=300&skills=2&skills=6](lifeup://api/reward?type=exp&content=学习API调用&number=300&skills=2&skills=6)

- 获得 1 个模糊匹配「金币」商品，获得原因为「学习API调用」。且原因会在仓库历史页面展示：

  [lifeup://api/reward?type=item&content=学习API调用&number=1&item_name=金币](lifeup://api/reward?type=item&content=学习API调用&number=1&item_name=金币)

| 参数      | 含义             | 取值                                   | 示例        | 是否必须 | 备注                                                         |
| --------- | ---------------- | -------------------------------------- | ----------- | -------- | ------------------------------------------------------------ |
| type      | 奖励类型         | 目前仅支持：<br/>coin<br/>exp<br/>item | coin        | 是       | coin - 金币<br/>exp - 经验值<br/>item - 商品                 |
| content   | 奖励原因         | 任意文本                               | 学习API调用 | 是       | -                                                            |
| skills    | 技能（属性）     | 大于 0 的数字数组                      | 1           | 否       | 仅当 type 为 exp 时可用<br/>支持数组（如&skills=1&skills=2&skills=3）<br/>获取方式请查看上文 「基础知识 - 人升数据 ID」 |
| number    | 奖励数量         | 大于 0 的数字                          | 1           | 是       | 如果是金币，取值最大为999999<br/>如果是经验值，取值最大为99999<br/>如果是商品，取值最大为99 |
| item_id   | 商品id           | 大于 0 的数字                          | 1           | 否*      | 仅当 type 为 item 时可用                                     |
| item_name | 商品名称         | 任意文本                               | 金币        | 否*      | 仅当 type 为 item 时可用，模糊匹配                           |
| silent    | 是否要禁用UI提示 | true 或者 false                        | false       | 否       | 默认为 false                                                 |

<br/>
