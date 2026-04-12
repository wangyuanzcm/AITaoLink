package org.jeecg.modules.taolink.integrations.taobao;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.taolink.entity.TaolinkShop;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 淘宝开放平台OAuth客户端
 * 用于处理店铺绑定的OAuth授权流程
 */
@Slf4j
@Component
public class TaobaoOauthClient {

    @Value("${taolink.taobao.appKey}")
    private String appKey;

    @Value("${taolink.taobao.appSecret}")
    private String appSecret;

    @Value("${taolink.taobao.redirectUri}")
    private String redirectUri;

    private static final String AUTHORIZE_URL = "https://oauth.taobao.com/authorize";
    private static final String TOKEN_URL = "https://oauth.taobao.com/token";

    /**
     * 生成授权URL
     * @param state 状态参数，用于防止CSRF攻击
     * @return 授权URL
     */
    public String generateAuthorizeUrl(String state) {
        Map<String, Object> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", appKey);
        params.put("redirect_uri", redirectUri);
        params.put("state", state);
        params.put("view", "web");

        StringBuilder urlBuilder = new StringBuilder(AUTHORIZE_URL);
        urlBuilder.append("?");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            urlBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return urlBuilder.substring(0, urlBuilder.length() - 1);
    }

    /**
     * 通过授权码获取访问令牌
     * @param code 授权码
     * @return 令牌信息
     */
    public TaobaoTokenInfo getToken(String code) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("client_id", appKey);
        params.put("client_secret", appSecret);
        params.put("redirect_uri", redirectUri);

        HttpResponse response = HttpRequest.post(TOKEN_URL)
                .form(params)
                .execute();

        if (response.isOk()) {
            String body = response.body();
            log.info("获取淘宝令牌响应: {}", body);
            JSONObject json = JSONUtil.parseObj(body);
            if (json.containsKey("access_token")) {
                TaobaoTokenInfo tokenInfo = new TaobaoTokenInfo();
                tokenInfo.setAccessToken(json.getStr("access_token"));
                tokenInfo.setRefreshToken(json.getStr("refresh_token"));
                tokenInfo.setExpiresIn(json.getInt("expires_in"));
                tokenInfo.setTokenType(json.getStr("token_type"));
                tokenInfo.setReExpiresIn(json.getInt("re_expires_in"));
                tokenInfo.setR1ExpiresIn(json.getInt("r1_expires_in"));
                tokenInfo.setR2ExpiresIn(json.getInt("r2_expires_in"));
                tokenInfo.setW1ExpiresIn(json.getInt("w1_expires_in"));
                tokenInfo.setW2ExpiresIn(json.getInt("w2_expires_in"));
                tokenInfo.setTaobaoUserId(json.getStr("taobao_user_id"));
                tokenInfo.setTaobaoUserName(json.getStr("taobao_user_nick"));
                return tokenInfo;
            }
        }
        log.error("获取淘宝令牌失败: {}", response.body());
        return null;
    }

    /**
     * 刷新访问令牌
     * @param refreshToken 刷新令牌
     * @return 新的令牌信息
     */
    public TaobaoTokenInfo refreshToken(String refreshToken) {
        Map<String, Object> params = new HashMap<>();
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", refreshToken);
        params.put("client_id", appKey);
        params.put("client_secret", appSecret);

        HttpResponse response = HttpRequest.post(TOKEN_URL)
                .form(params)
                .execute();

        if (response.isOk()) {
            String body = response.body();
            log.info("刷新淘宝令牌响应: {}", body);
            JSONObject json = JSONUtil.parseObj(body);
            if (json.containsKey("access_token")) {
                TaobaoTokenInfo tokenInfo = new TaobaoTokenInfo();
                tokenInfo.setAccessToken(json.getStr("access_token"));
                tokenInfo.setRefreshToken(json.getStr("refresh_token"));
                tokenInfo.setExpiresIn(json.getInt("expires_in"));
                tokenInfo.setTokenType(json.getStr("token_type"));
                tokenInfo.setReExpiresIn(json.getInt("re_expires_in"));
                tokenInfo.setR1ExpiresIn(json.getInt("r1_expires_in"));
                tokenInfo.setR2ExpiresIn(json.getInt("r2_expires_in"));
                tokenInfo.setW1ExpiresIn(json.getInt("w1_expires_in"));
                tokenInfo.setW2ExpiresIn(json.getInt("w2_expires_in"));
                tokenInfo.setTaobaoUserId(json.getStr("taobao_user_id"));
                tokenInfo.setTaobaoUserName(json.getStr("taobao_user_nick"));
                return tokenInfo;
            }
        }
        log.error("刷新淘宝令牌失败: {}", response.body());
        return null;
    }

    /**
     * 构建店铺信息
     * @param tokenInfo 令牌信息
     * @param tenantId 租户ID
     * @param ownerId 所有者ID
     * @return 店铺信息
     */
    public TaolinkShop buildShopInfo(TaobaoTokenInfo tokenInfo, String tenantId, String ownerId) {
        TaolinkShop shop = new TaolinkShop();
        shop.setTenantId(tenantId);
        shop.setTaobaoSellerNick(tokenInfo.getTaobaoUserName());
        shop.setApiSessionKey(tokenInfo.getAccessToken());
        shop.setStatus("active");
        shop.setOwnerId(ownerId);
        shop.setMonitoringEnabled(true);
        shop.setMonitoringDays(7);
        // 设置API过期时间
        long expireTime = System.currentTimeMillis() + (tokenInfo.getExpiresIn() * 1000L);
        shop.setApiExpireAt(new java.util.Date(expireTime));
        return shop;
    }

    /**
     * 令牌信息类
     */
    public static class TaobaoTokenInfo {
        private String accessToken;
        private String refreshToken;
        private Integer expiresIn;
        private String tokenType;
        private Integer reExpiresIn;
        private Integer r1ExpiresIn;
        private Integer r2ExpiresIn;
        private Integer w1ExpiresIn;
        private Integer w2ExpiresIn;
        private String taobaoUserId;
        private String taobaoUserName;

        // getter和setter方法
        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public Integer getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(Integer expiresIn) {
            this.expiresIn = expiresIn;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public Integer getReExpiresIn() {
            return reExpiresIn;
        }

        public void setReExpiresIn(Integer reExpiresIn) {
            this.reExpiresIn = reExpiresIn;
        }

        public Integer getR1ExpiresIn() {
            return r1ExpiresIn;
        }

        public void setR1ExpiresIn(Integer r1ExpiresIn) {
            this.r1ExpiresIn = r1ExpiresIn;
        }

        public Integer getR2ExpiresIn() {
            return r2ExpiresIn;
        }

        public void setR2ExpiresIn(Integer r2ExpiresIn) {
            this.r2ExpiresIn = r2ExpiresIn;
        }

        public Integer getW1ExpiresIn() {
            return w1ExpiresIn;
        }

        public void setW1ExpiresIn(Integer w1ExpiresIn) {
            this.w1ExpiresIn = w1ExpiresIn;
        }

        public Integer getW2ExpiresIn() {
            return w2ExpiresIn;
        }

        public void setW2ExpiresIn(Integer w2ExpiresIn) {
            this.w2ExpiresIn = w2ExpiresIn;
        }

        public String getTaobaoUserId() {
            return taobaoUserId;
        }

        public void setTaobaoUserId(String taobaoUserId) {
            this.taobaoUserId = taobaoUserId;
        }

        public String getTaobaoUserName() {
            return taobaoUserName;
        }

        public void setTaobaoUserName(String taobaoUserName) {
            this.taobaoUserName = taobaoUserName;
        }
    }
}
