package org.jeecg.modules.taolink.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description = "工单")
@TableName("taolink_ticket")
public class TaolinkTicket extends JeecgEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private String orderId;

    @Schema(description = "工单类型", example = "ship_urge")
    private String type;

    @Schema(description = "优先级", example = "p1")
    private String priority;

    @Schema(description = "状态", example = "open")
    private String status;

    @Schema(description = "会话快照JSON（字符串）")
    private String conversationSnapshotJson;

    @Schema(description = "AI建议JSON（字符串）")
    private String aiSuggestionJson;
}

