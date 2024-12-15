package com.bitsflea.events;

import io.nuls.contract.sdk.Address;
import io.nuls.contract.sdk.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 添加商品类型事件
 */
@Data
@AllArgsConstructor
public class AddCategoryEvent implements Event {
    /**
     * 操作者
     */
    private Address operator;
    /**
     * 类型id
     */
    private Integer id;
    /**
     * 显示名称
     */
    private String view;
    /**
     * 父类
     */
    private Integer parent;
}
