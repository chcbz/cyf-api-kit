package cn.jia.wx.api;

import cn.jia.core.common.EsHandler;
import cn.jia.core.common.EsSecurityHandler;
import cn.jia.core.entity.JSONRequestPage;
import cn.jia.core.entity.JSONResult;
import cn.jia.core.entity.JSONResultPage;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.service.DictService;
import cn.jia.core.util.*;
import cn.jia.dwz.service.DwzService;
import cn.jia.isp.service.FileService;
import cn.jia.material.entity.VoteItem;
import cn.jia.material.entity.VoteQuestion;
import cn.jia.material.entity.VoteTick;
import cn.jia.material.service.VoteService;
import cn.jia.point.entity.*;
import cn.jia.point.service.GiftService;
import cn.jia.point.service.PointService;
import cn.jia.user.entity.User;
import cn.jia.user.service.UserService;
import cn.jia.wx.common.Constants;
import cn.jia.wx.common.ErrorConstants;
import cn.jia.wx.entity.MpInfo;
import cn.jia.wx.entity.MpUser;
import cn.jia.wx.service.MpInfoService;
import cn.jia.wx.service.MpUserService;
import cn.jia.wx.service.PayInfoService;
import com.github.binarywang.wxpay.bean.request.WxPaySendRedpackRequest;
import com.github.binarywang.wxpay.bean.result.WxPaySendRedpackResult;
import com.github.pagehelper.Page;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.material.WxMpMaterial;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialNews;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialNews.WxMpMaterialNewsArticle;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialUploadResult;
import me.chanjar.weixin.mp.bean.message.*;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.chanjar.weixin.mp.bean.result.WxMpUserList;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 微信公众号接口
 * @author chc
 * @date 2018年1月14日 下午4:53:24
 */
@Slf4j
@RestController
@RequestMapping("/wx/mp")
public class WxMpController {
	
	@Autowired
	private MpInfoService mpInfoService;
	@Autowired
	private PayInfoService payInfoService;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private UserService userService;
	@Autowired
	private MpUserService mpUserService;
	@Autowired
	private PointService pointService;
	@Autowired
	private GiftService giftService;
	@Autowired
	private DictService dictService;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private VoteService voteService;
	@Autowired
	private FileService fileService;
	@Autowired
    private DwzService dwzService;

	/**
	 * 微信公众号接口认证
	 * @param signature
	 * @param nonce
	 * @param timestamp
	 * @param echostr
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/checksignature", method = RequestMethod.GET)
	public Object checkSignature(@RequestParam("signature") String signature,
			@RequestParam("nonce") String nonce,@RequestParam("timestamp") String timestamp,
			@RequestParam("echostr") String echostr,HttpServletRequest request) throws Exception {
		if(mpInfoService.findWxMpService(request).checkSignature(timestamp, nonce, signature)){
			return echostr;
		}else{
			return "";
		}
	}
	
	/**
	 * 接收公众号的用户消息，并转客服
	 * @param msg
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/checksignature", method = RequestMethod.POST)
	public Object receiveMsg(@RequestBody String msg,HttpServletRequest request) throws Exception {
		log.debug("---------------------receiveMsg---------------------\n\r" + msg);
		WxMpXmlMessage message = WxMpXmlMessage.fromXml(msg);
		//获取并设置oauth权限信息
//		request.getSession().setAttribute("current_user", message.getFromUser());
		String original = message.getToUser();
		WxMpService wxMpService = mpInfoService.findWxMpService(original);
		String appid = wxMpService.getWxMpConfigStorage().getAppId();
		MpInfo mpInfo = mpInfoService.findByKey(appid);
		//关注
		if(WxConsts.XmlMsgType.EVENT.equals(message.getMsgType()) && WxConsts.EventType.SUBSCRIBE.equals(message.getEvent())) {
			WxMpXmlOutTextMessage outMessage = new WxMpXmlOutTextMessage();
			outMessage.setCreateTime(message.getCreateTime());
			outMessage.setFromUserName(message.getToUser());
			outMessage.setToUserName(message.getFromUser());
			//初始化用户信息
			WxMpUser wxMpUser = wxMpService.getUserService().userInfoList(Collections.singletonList(message.getFromUser())).get(0);
			MpUser params = new MpUser();
			params.setOpenId(wxMpUser.getOpenId());
			params.setCountry(wxMpUser.getCountry());
			params.setProvince(wxMpUser.getProvince());
			params.setCity(wxMpUser.getCity());
			params.setSex(wxMpUser.getSex());
			params.setNickname(wxMpUser.getNickname());
			params.setSubscribe(true);
			params.setHeadImgUrl(wxMpUser.getHeadImgUrl());
			List<MpUser> uList = new ArrayList<>();
			uList.add(params);
			mpUserService.sync(uList);

			User user = userService.findByOpenid(message.getFromUser());
			if(user.getPoint() == 0){
				//初始化积分
				Record record = new Record();
				record.setJiacn(user.getJiacn());
				Record pointResult = pointService.init(record);
				outMessage.setContent(EsHandler.getMessage(request, "init.success", new String[]{wxMpUser.getNickname(),String.valueOf(pointResult.getChange())}));
				//如果是推荐进来的用户，则增加推荐人的积分
				if(user.getReferrer() != null) {
					//增加推荐人的积分
					Referral referral = new Referral();
					referral.setReferrer(user.getReferrer());
					referral.setReferral(user.getJiacn());
					Record referralResult = pointService.referral(referral);
					//获取推荐人openid
					User referrer = userService.findByJiacn(user.getReferrer());
					if(referrer != null && referrer.getOpenid() != null) {
						//通知推荐人
						WxMpKefuMessage kfmessage = new WxMpKefuMessage();
						kfmessage.setToUser(referrer.getOpenid());
						kfmessage.setMsgType(WxConsts.KefuMsgType.TEXT);
						kfmessage.setContent(EsHandler.getMessage(request, "referral.success", new String[] {user.getNickname(), String.valueOf(referralResult.getChange())}));
						wxMpService.getKefuService().sendKefuMessage(kfmessage);
					}
				}
			} else {
				outMessage.setContent(EsHandler.getMessage(request, "init.back"));
			}
			return outMessage.toXml();
		}
		//退订
		if(WxConsts.XmlMsgType.EVENT.equals(message.getMsgType()) && WxConsts.EventType.UNSUBSCRIBE.equals(message.getEvent())) {
			User user = userService.findByOpenid(message.getFromUser());
			List<String> subscribe = new ArrayList<>(Arrays.asList(user.getSubscribe().split(",")));
			if(subscribe.remove(appid)) {
				User upUser = new User();
				upUser.setId(user.getId());
				upUser.setSubscribe(Joiner.on(",").join(subscribe));
				userService.update(upUser);
			}
		}
		//签到
		if(WxConsts.XmlMsgType.EVENT.equals(message.getMsgType()) && WxConsts.EventType.LOCATION.equals(message.getEvent())) {
			WxMpXmlOutTextMessage outMessage = new WxMpXmlOutTextMessage();
			outMessage.setCreateTime(message.getCreateTime());
			outMessage.setFromUserName(message.getToUser());
			outMessage.setToUserName(message.getFromUser());
			User user = userService.findByOpenid(message.getFromUser());
			if(user == null) {
				outMessage.setContent(ErrorConstants.USER_NOT_EXIST);
				return outMessage.toXml();
			}
			Sign params = new Sign();
			params.setJiacn(user.getJiacn());
			params.setLatitude(String.valueOf(message.getLatitude()));
			params.setLongitude(String.valueOf(message.getLongitude()));
			try {
				Record pointResult = pointService.sign(params);
				outMessage.setContent(EsHandler.getMessage(request, "sign.success", new String[]{String.valueOf(pointResult.getChange())}));
			} catch (Exception ignored) {
				outMessage.setContent("");
			}
			return outMessage.toXml();
		}
		//点击菜单
		if(WxConsts.XmlMsgType.EVENT.equals(message.getMsgType()) && WxConsts.EventType.CLICK.equals(message.getEvent())){
			WxMpXmlOutTextMessage outMessage = new WxMpXmlOutTextMessage();
			outMessage.setCreateTime(message.getCreateTime());
			outMessage.setFromUserName(message.getToUser());
			outMessage.setToUserName(message.getFromUser());
			//查看积分
			if(Constants.EVENKEY_POINT_MINE.equals(message.getEventKey())) {
				User user = userService.findByOpenid(message.getFromUser());
				if(user == null) {
					outMessage.setContent(ErrorConstants.USER_NOT_EXIST);
					return outMessage.toXml();
				}
				outMessage.setContent(EsHandler.getMessage(request, "point.mine", new String[]{String.valueOf(user.getPoint())}));
			}
			//签到
			if(Constants.EVENKEY_POINT_SIGN.equals(message.getEventKey())) {
				User user = userService.findByOpenid(message.getFromUser());
				if(user == null) {
					outMessage.setContent(ErrorConstants.USER_NOT_EXIST);
					return outMessage.toXml();
				}
				Sign params = new Sign();
				params.setJiacn(user.getJiacn());
				params.setLatitude(String.valueOf(message.getLatitude()));
				params.setLongitude(String.valueOf(message.getLongitude()));
				try {
					Record pointResult = pointService.sign(params);
					outMessage.setContent(EsHandler.getMessage(request, "sign.success", new String[]{String.valueOf(pointResult.getChange())}));
				} catch (Exception ignored) {
					outMessage.setContent("");
				}
			}
			//赌积分
			else if(Constants.EVENKEY_POINT_LUCK.equals(message.getEventKey())) {
				User user = userService.findByOpenid(message.getFromUser());
				if(user == null) {
					outMessage.setContent(ErrorConstants.USER_NOT_EXIST);
					return outMessage.toXml();
				}
				Record params = new Record();
				params.setJiacn(user.getJiacn());
				params.setChange(2);
				Record pointResult = pointService.luck(params);
				Integer point = pointResult.getChange();
				if(point == null || point <= 0) {
					//当赚不了积分，进行红包补偿
					WxPaySendRedpackRequest sendRedpack = new WxPaySendRedpackRequest();
					sendRedpack.setMchBillNo(String.valueOf(DateUtil.genTime(new Date())));
					sendRedpack.setClientIp(HttpUtil.getIpAddr(request));
					sendRedpack.setActName("抽奖红包");
					sendRedpack.setReOpenid(message.getFromUser());
					sendRedpack.setSceneId("PRODUCT_2");
					sendRedpack.setWishing("幸运之神眷顾着你");
					sendRedpack.setRemark("多多关注我们，每天都有不一样的收获!");
					sendRedpack.setSendName(mpInfo.getName());
					sendRedpack.setAmtType("ALL_RAND");
					sendRedpack.setTotalAmount(200 + Integer.parseInt(DataUtil.getRandom(true, 3)));
					sendRedpack.setTotalNum(10);
					sendRedpack.setWxAppid(appid);
					WxPaySendRedpackResult result = payInfoService.findWxPayService(request).sendRedpack(sendRedpack);
					if("SUCCESS".equals(result.getResultCode())){
						outMessage.setContent(EsHandler.getMessage(request, "luck.redpack"));
					}else {
						outMessage.setContent(EsHandler.getMessage(request, "luck.failure"));
					}
				}else {
					outMessage.setContent(EsHandler.getMessage(request, "luck.success", new String[]{String.valueOf(point)}));
				}
				return outMessage.toXml();
			}
			//礼品列表
			else if(Constants.EVENKEY_POINT_GIFT.equals(message.getEventKey())) {
				WxMpXmlOutNewsMessage newsMessage = new WxMpXmlOutNewsMessage();
				newsMessage.setCreateTime(message.getCreateTime());
				newsMessage.setFromUserName(message.getToUser());
				newsMessage.setToUserName(message.getFromUser());
				GiftExample example = new GiftExample();
				example.setStatus(Constants.COMMON_ENABLE);
				Page<Gift> pointResult = giftService.list(1, 8, example);
				for(Gift gift : pointResult) {
					WxMpXmlOutNewsMessage.Item item = new WxMpXmlOutNewsMessage.Item();
					item.setDescription(String.valueOf(gift.getDescription()));
					item.setPicUrl(String.valueOf(gift.getPicUrl()));
					item.setTitle(EsHandler.getMessage(request, "gift.title", new String[] {String.valueOf(gift.getName()), String.valueOf(gift.getPoint())}));
					String pointWebUrl = dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_WX_CONFIG, Constants.WX_CONFIG_POINT_WEB_URL).getName();
					item.setUrl(pointWebUrl + "/pay?id=" + gift.getId());
					newsMessage.addArticle(item);
				}
				return newsMessage.toXml();
			}
			//获取分享二维码
			if(Constants.EVENKEY_POINT_QRCODE.equals(message.getEventKey())) {
				WxMpXmlOutImageMessage imageMessage = new WxMpXmlOutImageMessage();
				imageMessage.setCreateTime(message.getCreateTime());
				imageMessage.setFromUserName(message.getToUser());
				imageMessage.setToUserName(message.getFromUser());
				imageMessage.setMsgType(WxConsts.XmlMsgType.IMAGE);
				//生成二维码
				String shareHandlerUrl = dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_WX_CONFIG, Constants.WX_CONFIG_MP_SHARE_URL).getName();
				shareHandlerUrl = HttpUtil.addUrlValue(shareHandlerUrl, "referrer", message.getFromUser());
				String shareUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+appid+"&redirect_uri="+URLEncoder.encode(shareHandlerUrl, "utf-8")+"&response_type=code&scope=snsapi_userinfo&state=wx_snsapi_userinfo&connect_redirect=1#wechat_redirect";
				File qrFile = File.createTempFile("wx-qrcode", ".jpg");
				User user = userService.findByOpenid(message.getFromUser());
				String dwzServerUrl = dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_WX_CONFIG, Constants.WX_CONFIG_DWZ_SERVER_URL).getName();
				QRCodeUtil.encode(qrFile.getParent(), qrFile.getName(), dwzServerUrl + "/" + dwzService.gen(user.getJiacn(), shareUrl, null), 200, 200);
				String logoUrl = dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_WX_CONFIG, Constants.WX_CONFIG_MP_LOGO_URL).getName();
				File logoFile = File.createTempFile("wx-logo", ".jpg");
				IOUtils.write(ImgUtil.fromURL(logoUrl), new FileOutputStream(logoFile));
				QRCodeUtil.composeLogo(qrFile, logoFile, qrFile);
				//上传临时素材并获取mediaId
				WxMediaUploadResult imageResult = wxMpService.getMaterialService().mediaUpload(WxConsts.MaterialType.IMAGE, qrFile);
				imageMessage.setMediaId(imageResult.getMediaId());
				return imageMessage.toXml();
			}
			return outMessage.toXml();
		}
		//回答问题
        Object obj = redisTemplate.opsForValue().get("vote_" + message.getFromUser());
        if(obj != null && WxConsts.XmlMsgType.TEXT.equalsIgnoreCase(message.getMsgType())) {
			WxMpXmlOutTextMessage outMessage = new WxMpXmlOutTextMessage();
			outMessage.setCreateTime(message.getCreateTime());
			outMessage.setFromUserName(message.getToUser());
			outMessage.setToUserName(message.getFromUser());

			User user = userService.findByOpenid(message.getFromUser());
			if("TD".equalsIgnoreCase(message.getContent())) {
				List<String> subscribe = new ArrayList<>(Arrays.asList(user.getSubscribe().split(",")));
				subscribe.remove(cn.jia.user.common.Constants.SUBSCRIBE_VOTE);
				User upUser = new User();
				upUser.setId(user.getId());
				upUser.setSubscribe(Joiner.on(",").join(subscribe));
				userService.update(upUser);
				outMessage.setContent("退订成功咯！哼！！");
			} else {
				Integer questionId = (Integer)obj;
				VoteQuestion voteQuestion = voteService.findQuestion(questionId);
				VoteTick voteTick = new VoteTick();
				voteTick.setQuestionId(questionId);
				voteTick.setOpt(message.getContent());
				voteTick.setJiacn(user.getJiacn());
				boolean tick = voteService.tick(voteTick);
				if(tick) {
					outMessage.setContent("恭喜你，答案正确，增加" + voteQuestion.getPoint() + "积分！");
					pointService.add(user.getJiacn(), voteQuestion.getPoint(), cn.jia.point.common.Constants.POINT_TYPE_VOTE);
					//随机发放红包
					WxPaySendRedpackRequest sendRedpack = new WxPaySendRedpackRequest();
					sendRedpack.setMchBillNo(String.valueOf(DateUtil.genTime(new Date())));
					sendRedpack.setClientIp(HttpUtil.getIpAddr(request));
					sendRedpack.setActName("奖励红包");
					sendRedpack.setReOpenid(message.getFromUser());
					sendRedpack.setSceneId("PRODUCT_2");
					sendRedpack.setWishing("幸运之神眷顾着你");
					sendRedpack.setRemark("多多关注我们，每天都有不一样的收获!");
					sendRedpack.setSendName(mpInfo.getName());
					sendRedpack.setAmtType("ALL_RAND");
					sendRedpack.setTotalAmount(200 + Integer.parseInt(DataUtil.getRandom(true, 3)));
					sendRedpack.setTotalNum(10);
					sendRedpack.setWxAppid(appid);
					payInfoService.findWxPayService(request).sendRedpack(sendRedpack);
				} else {
					outMessage.setContent("很遗憾，回答错误，正确答案是" + voteQuestion.getOpt() + ",下次继续努力！");
				}

			}
			redisTemplate.delete("vote_" + message.getFromUser());
            return outMessage.toXml();
        }
        //处理消息是否送达成功推送
		if(WxConsts.XmlMsgType.EVENT.equals(message.getMsgType())
				&& (WxConsts.EventType.MASS_SEND_JOB_FINISH.equals(message.getEvent())
				|| WxConsts.EventType.TEMPLATE_SEND_JOB_FINISH.equals(message.getEvent()))) {
			WxMpXmlOutTextMessage outMessage = new WxMpXmlOutTextMessage();
			outMessage.setCreateTime(message.getCreateTime());
			outMessage.setFromUserName(message.getToUser());
			outMessage.setToUserName(message.getFromUser());
			outMessage.setContent("");
			return outMessage.toXml();
		}
        //我要做题
		if("我要做题".equals(message.getContent())) {
			WxMpXmlOutTextMessage outMessage = new WxMpXmlOutTextMessage();
			outMessage.setCreateTime(message.getCreateTime());
			outMessage.setFromUserName(message.getToUser());
			outMessage.setToUserName(message.getFromUser());

			User user = userService.findByOpenid(message.getFromUser());
			VoteQuestion question = voteService.findOneQuestion(user.getJiacn());
			if(question == null) {
				outMessage.setContent("你超级厉害，所有题都被你做完了！");
			} else {
				StringBuilder content = new StringBuilder();
				content.append(question.getTitle()).append("\n\n");
				for(VoteItem item : question.getItems()) {
					content.append(item.getOpt()).append(" ").append(item.getContent()).append("\n");
				}
				outMessage.setContent(content.toString());
				redisTemplate.opsForValue().set("vote_" + message.getFromUser(), question.getId(), 2, TimeUnit.HOURS);
			}
			return outMessage.toXml();
		} else if ("我的积分".equals(message.getContent())) {
			WxMpXmlOutTextMessage outMessage = new WxMpXmlOutTextMessage();
			outMessage.setCreateTime(message.getCreateTime());
			outMessage.setFromUserName(message.getToUser());
			outMessage.setToUserName(message.getFromUser());

			User user = userService.findByOpenid(message.getFromUser());
			if(user == null) {
				outMessage.setContent(ErrorConstants.USER_NOT_EXIST);
				return outMessage.toXml();
			}
			outMessage.setContent(EsHandler.getMessage(request, "point.mine", new String[]{String.valueOf(user.getPoint())}));
			return outMessage.toXml();
		}

        //转客服
        WxMpMessageHandler handler = (wxMessage, context, mpService, sessionManager) -> {
            WxMpXmlOutTransferKefuMessage out = new WxMpXmlOutTransferKefuMessage();
            out.setCreateTime(wxMessage.getCreateTime());
            out.setFromUserName(wxMessage.getToUser());
            out.setToUserName(wxMessage.getFromUser());
//					TransInfo transInfo = new TransInfo();
//					transInfo.setKfAccount("kf2001@jia__shun");
//					out.setTransInfo(transInfo);
            return out;
        };
        WxMpMessageRouter router = new WxMpMessageRouter(wxMpService);
        router.rule().async(false).handler(handler).end();
        WxMpXmlOutMessage outMessage = router.route(WxMpXmlMessage.fromXml(msg));
        return outMessage.toXml();
	}
	
	/*public static void main(String[] args) throws IOException {
		String shareUrl = "http://chcbz.net";
		File qrFile = File.createTempFile("wx-qrcode", ".png");
		QRCodeUtil.encode(qrFile.getParent(), qrFile.getName(), shareUrl, 200, 200);
	}*/
	
	/**
	 * 自定义菜单创建
	 * @param menu 菜单JSON字符串
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/menu/create")
	public Object menuCreate(@RequestBody String menu, HttpServletRequest request) throws Exception {
		mpInfoService.findWxMpService(request).getMenuService().menuCreate(menu);
		return JSONResult.success();
	}
	
	/**
	 * 自定义菜单查询接口
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/menu/get")
	public Object menuGet(HttpServletRequest request) throws Exception {
		return JSONResult.success(mpInfoService.findWxMpService(request).getMenuService().menuGet());
	}
	
	/**
	 * 自定义菜单删除
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/menu/delete")
	public Object menuDelete(HttpServletRequest request) throws Exception {
		mpInfoService.findWxMpService(request).getMenuService().menuDelete();
		return JSONResult.success();
	}
	
	/**
	 * 获取用户列表
	 * @param nextOpenid
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/user/get")
	public Object userGet(@RequestParam(value="next_openid", required=false) String nextOpenid, HttpServletRequest request) throws Exception {
		return JSONResult.success(mpInfoService.findWxMpService(request).getUserService().userList(nextOpenid));
	}
	
	/**
	 * 批量获取用户基本信息
	 * @param openids 数组
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/user/info/batchget")
	public Object userInfoBatchGet(@RequestBody List<String> openids, HttpServletRequest request) throws Exception {
		return JSONResult.success(mpInfoService.findWxMpService(request).getUserService().userInfoList(openids));
	}
	
	/**
	 * 将公众号的用户同步到自己系统
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/user/sync")
	public Object userSync(HttpServletRequest request) throws Exception {
		WxMpService wxMpService = mpInfoService.findWxMpService(request);
		String wxAppId = wxMpService.getWxMpConfigStorage().getAppId();
		String clientId = EsSecurityHandler.clientId();
		//先清空所有用户的公众号订阅情况
		MpUser example = new MpUser();
		example.setClientId(clientId);
		example.setAppid(wxAppId);
		mpUserService.unsubstribe(example);

		WxMpUserList userList = wxMpService.getUserService().userList(null);
		do {
			List<String> openids = userList.getOpenids();
			List<String> batchList = new ArrayList<>();
			List<MpUser> users;
			for(int i=0;i<openids.size();i++) {
				batchList.add(openids.get(i));
				if(i!=0 && ((i+1)%100==0 || i==openids.size()-1)) {
					users = new ArrayList<>();
					for(WxMpUser wxMpUser : wxMpService.getUserService().userInfoList(batchList)) {
						MpUser params = new MpUser();
						params.setOpenId(wxMpUser.getOpenId());
						params.setCountry(wxMpUser.getCountry());
						params.setProvince(wxMpUser.getProvince());
						params.setCity(wxMpUser.getCity());
						params.setSex(wxMpUser.getSex());
						params.setNickname(wxMpUser.getNickname());
						params.setSubscribe(true);
						params.setHeadImgUrl(wxMpUser.getHeadImgUrl());
						users.add(params);
					}
					mpUserService.sync(users);
					batchList = new ArrayList<>();
				}
			}
			userList = wxMpService.getUserService().userList(userList.getNextOpenid());
		}while(StringUtils.isNotEmpty(userList.getNextOpenid()));
		return JSONResult.success();
	}
	
	/**
	 * 生成授权URL
	 * @param redirectURI
	 * @param scope 1、snsapi_base 2、snsapi_userinfo
	 * @param state
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/oauth2/authorize_url")
	public Object oauth2Authorize(@RequestParam("redirect_uri") String redirectURI, @RequestParam("scope") String scope,
			@RequestParam("state") String state, HttpServletRequest request) throws Exception {
		return JSONResult.success(mpInfoService.findWxMpService(request).oauth2buildAuthorizationUrl(redirectURI, scope, state));
	}
	
	/**
	 * 通过code换取网页授权access_token
	 * @param code
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/oauth2/access_token")
	public Object oauth2AccessToken(@RequestParam("code") String code, HttpServletRequest request) throws Exception {
		return JSONResult.success(mpInfoService.findWxMpService(request).oauth2getAccessToken(code));
	}
	
	/**
	 * 拉取用户信息(需scope为 snsapi_userinfo)
	 * @param accessToken
	 * @param openId
	 * @param lang
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/oauth2/userinfo")
	public Object oauth2Userinfo(@RequestParam("access_token") String accessToken,
			@RequestParam("openid") String openId, @RequestParam("lang") String lang, HttpServletRequest request)
			throws Exception {
		WxMpOAuth2AccessToken oAuth2AccessToken = new WxMpOAuth2AccessToken();
		oAuth2AccessToken.setOpenId(openId);
		oAuth2AccessToken.setAccessToken(accessToken);
		return JSONResult.success(mpInfoService.findWxMpService(request).oauth2getUserInfo(oAuth2AccessToken, lang));
	}
	
	/**
	 * 刷新access_token（如果需要）
	 * @param refreshToken
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/oauth2/refresh_token")
	public Object oauth2RefreshToken(@RequestParam("refresh_token") String refreshToken, HttpServletRequest request)
			throws Exception {
		return JSONResult.success(mpInfoService.findWxMpService(request).oauth2refreshAccessToken(refreshToken));
	}

	/**
	 * 检验授权凭证（access_token）是否有效
	 * @param accessToken
	 * @param openId
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/oauth2/auth")
	public Object oauth2ValidateAccessToken(@RequestParam("access_token") String accessToken,
			@RequestParam("openid") String openId, HttpServletRequest request) throws Exception {
		WxMpOAuth2AccessToken oAuth2AccessToken = new WxMpOAuth2AccessToken();
		oAuth2AccessToken.setOpenId(openId);
		oAuth2AccessToken.setAccessToken(accessToken);
		return JSONResult.success(mpInfoService.findWxMpService(request).oauth2validateAccessToken(oAuth2AccessToken));
	}
	
	/**
	 * 获取所有客服账号
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/customservice/getkflist")
	public Object getkflist(HttpServletRequest request) throws Exception {
		return JSONResult.success(mpInfoService.findWxMpService(request).getKefuService().kfList());
	}
	
	/**
	 * 获取未接入会话列表
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/customservice/kfsession/getwaitcase")
	public Object getwaitcase(HttpServletRequest request) throws Exception {
		return JSONResult.success(mpInfoService.findWxMpService(request).getKefuService().kfSessionGetWaitCase().getKfSessionWaitCaseList());
	}
	
	/**
	 * 客服接口-发消息
	 * @param message
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/message/custom/send")
	public Object customMessageSend(@RequestBody WxMpKefuMessage message, HttpServletRequest request) throws Exception {
		return JSONResult.success(mpInfoService.findWxMpService(request).getKefuService().sendKefuMessage(message));
	}
	
	/**
	 * 发送模板消息
	 * @param templateMessage
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/message/template/send")
	public Object templateMessageSend(@RequestBody WxMpTemplateMessage templateMessage, HttpServletRequest request) throws Exception {
		return JSONResult.success(mpInfoService.findWxMpService(request).getTemplateMsgService().sendTemplateMsg(templateMessage));
	}
	
	/**
	 * 获取素材列表
	 * @param type
	 * @param offset
	 * @param count
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/material/batchget")
	public Object materialBatchGet(@RequestParam("type") String type, @RequestParam("offset") Integer offset,
			@RequestParam("count") Integer count, HttpServletRequest request) throws Exception {
		if(WxConsts.MaterialType.NEWS.equals(type)) {
			return JSONResult.success(mpInfoService.findWxMpService(request).getMaterialService().materialNewsBatchGet(offset, count));
		}else {
			return JSONResult.success(mpInfoService.findWxMpService(request).getMaterialService().materialFileBatchGet(type, offset, count));
		}
		
	}
	
	/**
	 * 添加图文素材
	 * @param news
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/material/add_news")
	public Object materialAddNews(@RequestBody WxMpMaterialNews news, HttpServletRequest request) throws Exception {
		for(WxMpMaterialNewsArticle article : news.getArticles()) {
			article.setContent(thsrc(article.getContent(), 0, request));
		}
		WxMpMaterialUploadResult result = mpInfoService.findWxMpService(request).getMaterialService().materialNewsUpload(news);
		if(result != null && StringUtils.isNotEmpty(result.getMediaId())) {
			return JSONResult.success(result);
		}else {
			return JSONResult.failure(String.valueOf(Objects.requireNonNull(result).getErrCode()), result.getErrMsg());
		}
	}
	
	/**
	 * 替换字符串中的src
	 * @param ynr 图文内容
	 * @param index 从第几个字符开始替换
	 * @return
	 */
	private String thsrc(String ynr, int index, HttpServletRequest request) throws Exception {
		int srcStart = ynr.indexOf("src=\"", index); // 获取src出现的位置
		int srcEnd = ynr.indexOf("\"", srcStart + 7);
		srcStart = srcStart + 5;
		String src = ynr.substring(srcStart, srcEnd); // 获取图片路径
		//下载文件到本地
		byte[] content = restTemplate.getForObject(src, byte[].class);
		File tempFile = File.createTempFile("wximg", ".jpg");
		StreamUtil.io(new ByteArrayInputStream(content), new FileOutputStream(tempFile));
		// 执行上传图片方法
		String newPath = mpInfoService.findWxMpService(request).getMaterialService().mediaImgUpload(tempFile).getUrl();
		// 替换字符串中该图片路径
		ynr = ynr.replace(src, newPath);
		// 查看字符串下方是否还有img标签
		int sfhyImg = ynr.indexOf("<img", srcEnd);
		if (sfhyImg == -1) {
			return ynr;
		} else {
			return thsrc(ynr, srcEnd, request);
		}
	}
	
	/**
	 * 添加其他素材
	 * @param file 素材文件
	 * @param type 类型
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/material/add_material")
	public Object materialAdd(@RequestPart(required = false, value = "file") MultipartFile file, @RequestParam(value="type") String type, 
		HttpServletRequest request) throws Exception {
		WxMpMaterial material = new WxMpMaterial();
		String filePath = request.getServletContext().getRealPath("/")+"tmp/";
		File pathFile = new File(filePath);
		//noinspection ResultOfMethodCallIgnored
		pathFile.mkdirs();
		File tmpFile = new File(filePath+file.getOriginalFilename());
		file.transferTo(tmpFile);
		material.setFile(tmpFile);
		WxMpMaterialUploadResult result = mpInfoService.findWxMpService(request).getMaterialService().materialFileUpload(type, material);
		if(result != null && StringUtils.isNotEmpty(result.getMediaId())) {
			return JSONResult.success(result);
		}else {
			return JSONResult.failure(String.valueOf(Objects.requireNonNull(result).getErrCode()), result.getErrMsg());
		}
	}
	
	/**
	 * 获取公众号信息
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/info/get", method = RequestMethod.GET)
	public Object findMpInfoById(@RequestParam(name = "id") Integer id) throws Exception {
		MpInfo info = mpInfoService.find(id);
		if(info == null) {
			throw new EsRuntimeException(ErrorConstants.DATA_NOT_FOUND);
		}
		return JSONResult.success(info);
	}

	/**
	 * 创建公众号
	 * @param info
	 * @return
	 */
	@RequestMapping(value = "/info/create", method = RequestMethod.POST)
	public Object createMpInfo(@RequestBody MpInfo info) {
		mpInfoService.create(info);
		return JSONResult.success(info);
	}

	/**
	 * 更新公众号信息
	 * @param info
	 * @return
	 */
	@RequestMapping(value = "/info/update", method = RequestMethod.POST)
	public Object updateMpInfo(@RequestBody MpInfo info) {
		mpInfoService.update(info);
		return JSONResult.success(info);
	}

	/**
	 * 删除公众号
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/info/delete", method = RequestMethod.GET)
	public Object deleteMpInfo(@RequestParam(name = "id") Integer id) {
		mpInfoService.delete(id);
		return JSONResult.success();
	}
	
	/**
	 * 公众号列表
	 * @param page
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/info/list", method = RequestMethod.POST)
	public Object listMpInfo(@RequestBody JSONRequestPage<String> page, HttpServletRequest request) {
		MpInfo example = JSONUtil.fromJson(page.getSearch(), MpInfo.class);
		if (example == null) {
			example = new MpInfo();
		}
		example.setClientId(EsSecurityHandler.clientId());
		Page<MpInfo> list = mpInfoService.list(example, page.getPageNum(), page.getPageSize());
		@SuppressWarnings({"unchecked", "rawtypes"})
		JSONResultPage<Object> result = new JSONResultPage(list.getResult());
		result.setPageNum(list.getPageNum());
		result.setTotal(list.getTotal());
		return result;
	}
}
