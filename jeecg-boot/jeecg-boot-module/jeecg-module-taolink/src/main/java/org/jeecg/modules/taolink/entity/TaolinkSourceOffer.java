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
@Schema(description = "来源商品（1688/淘宝）")
@TableName("taolink_source_offer")
public class TaolinkSourceOffer extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "关联店铺ID（可选，1688货源可跨店共享）")
    private String shopId;

    @Schema(description = "来源平台", example = "1688")
    private String platform;

    @Schema(description = "来源商品ID（num_iid）")
    private String numIid;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "详情页URL")
    private String detailUrl;

    @Schema(description = "卖家昵称")
    private String sellerNick;

    @Schema(description = "最低价（分）", example = "0")
    private Integer priceMin;

    @Schema(description = "最高价（分）", example = "0")
    private Integer priceMax;

    @Schema(description = "起批量（1688）", example = "0")
    private Integer minNum;

    @Schema(description = "发货地/所在地")
    private String location;

    @Schema(description = "风险等级", example = "low")
    private String riskLevel;

    @Schema(description = "原始回包JSON（字符串）")
    private String rawJson;

    @Schema(description = "采集时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date fetchedAt;

    // Getters and Setters
    public String getShopId() {
        return shopId;
    }

    public TaolinkSourceOffer setShopId(String shopId) {
        this.shopId = shopId;
        return this;
    }

    public String getPlatform() {
        return platform;
    }

    public TaolinkSourceOffer setPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public String getNumIid() {
        return numIid;
    }

    public TaolinkSourceOffer setNumIid(String numIid) {
        this.numIid = numIid;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public TaolinkSourceOffer setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public TaolinkSourceOffer setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
        return this;
    }

    public String getSellerNick() {
        return sellerNick;
    }

    public TaolinkSourceOffer setSellerNick(String sellerNick) {
        this.sellerNick = sellerNick;
        return this;
    }

    public Integer getPriceMin() {
        return priceMin;
    }

    public TaolinkSourceOffer setPriceMin(Integer priceMin) {
        this.priceMin = priceMin;
        return this;
    }

    public Integer getPriceMax() {
        return priceMax;
    }

    public TaolinkSourceOffer setPriceMax(Integer priceMax) {
        this.priceMax = priceMax;
        return this;
    }

    public Integer getMinNum() {
        return minNum;
    }

    public TaolinkSourceOffer setMinNum(Integer minNum) {
        this.minNum = minNum;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public TaolinkSourceOffer setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public TaolinkSourceOffer setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
        return this;
    }

    public String getRawJson() {
        return rawJson;
    }

    public TaolinkSourceOffer setRawJson(String rawJson) {
        this.rawJson = rawJson;
        return this;
    }

    public Date getFetchedAt() {
        return fetchedAt;
    }

    public TaolinkSourceOffer setFetchedAt(Date fetchedAt) {
        this.fetchedAt = fetchedAt;
        return this;
    }
}

