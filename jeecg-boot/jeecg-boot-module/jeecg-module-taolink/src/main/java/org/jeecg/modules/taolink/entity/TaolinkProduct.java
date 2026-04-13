package org.jeecg.modules.taolink.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "内部商品SPU")
@TableName("taolink_product")
public class TaolinkProduct extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "内部商品名")
    private String name;

    @Schema(description = "所属店铺ID（关联 taolink_shop）")
    private String shopId;

    @Schema(description = "类目ID（内部类目树节点）")
    private String categoryId;

    @Schema(description = "业务状态", example = "draft")
    private String status;

    @Schema(description = "上下架状态", example = "draft")
    private String listingStatus;

    @Schema(description = "上架时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date listedAt;

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date delistedAt;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getListingStatus() {
        return listingStatus;
    }

    public void setListingStatus(String listingStatus) {
        this.listingStatus = listingStatus;
    }

    public Date getListedAt() {
        return listedAt;
    }

    public void setListedAt(Date listedAt) {
        this.listedAt = listedAt;
    }

    public Date getDelistedAt() {
        return delistedAt;
    }

    public void setDelistedAt(Date delistedAt) {
        this.delistedAt = delistedAt;
    }
}

