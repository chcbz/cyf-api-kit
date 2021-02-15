package cn.jia.point.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GiftExample extends PointGift {

    private Integer clientStrictFlag;

    private Long createTimeStart;

    private Long createTimeEnd;

    private Long updateTimeStart;

    private Long updateTimeEnd;
}