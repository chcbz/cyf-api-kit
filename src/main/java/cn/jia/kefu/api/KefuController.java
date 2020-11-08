package cn.jia.kefu.api;

import cn.jia.core.annotation.SysPermission;
import cn.jia.core.common.EsConstants;
import cn.jia.core.common.EsSecurityHandler;
import cn.jia.core.configuration.SpringContextHolder;
import cn.jia.core.entity.JSONRequestPage;
import cn.jia.core.entity.JSONResult;
import cn.jia.core.entity.JSONResultPage;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.FileUtil;
import cn.jia.isp.common.Constants;
import cn.jia.isp.entity.IspFile;
import cn.jia.isp.service.FileService;
import cn.jia.kefu.entity.KefuFAQ;
import cn.jia.kefu.entity.KefuMessage;
import cn.jia.kefu.service.KefuService;
import cn.jia.user.common.ErrorConstants;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 * 客服接口
 * @author chc
 */
@RestController
@RequestMapping("/kefu")
@SysPermission(name = "kefu")
public class KefuController {
	
	@Autowired
	private KefuService kefuService;
	@Autowired
	private FileService fileService;

	/**
	 * FAQ列表
	 * @param page 查询条件
	 * @return FAQ列表
	 */
	@PreAuthorize("hasAuthority('kefu-faq_list')")
	@RequestMapping(value = "/faq/list", method = RequestMethod.POST)
	public Object listFAQ(@RequestBody JSONRequestPage<KefuFAQ> page) {
		KefuFAQ example = page.getSearch();
		if(example == null) {
			example = new KefuFAQ();
		}
		example.setClientId(EsSecurityHandler.clientId());
		Page<KefuFAQ> list = kefuService.listFAQ(example, page.getPageNum(), page.getPageSize());
		JSONResultPage<KefuFAQ> result = new JSONResultPage<>(list.getResult());
		result.setPageNum(list.getPageNum());
		result.setTotal(list.getTotal());
		return result;
	}
	
	/**
	 * 获取FAQ信息
	 * @param id FAQID
	 * @return FAQ信息
	 */
	@PreAuthorize("hasAuthority('kefu-faq_get')")
	@RequestMapping(value = "/faq/get", method = RequestMethod.GET)
	public Object findFAQById(@RequestParam(name = "id") Integer id) throws Exception {
		KefuFAQ record = kefuService.findFAQ(id);
		if(record == null) {
			throw new EsRuntimeException(ErrorConstants.DATA_NOT_FOUND);
		}
		return JSONResult.success(record);
	}

	/**
	 * 创建FAQ
	 * @param record FAQ信息
	 * @return FAQ信息
	 */
	@PreAuthorize("hasAuthority('kefu-faq_create')")
	@RequestMapping(value = "/faq/create", method = RequestMethod.POST)
	public Object createFAQ(@RequestBody KefuFAQ record) {
		record.setClientId(EsSecurityHandler.clientId());
		kefuService.createFAQ(record);
		return JSONResult.success(record);
	}

	/**
	 * 更新FAQ信息
	 * @param record FAQ信息
	 * @return FAQ信息
	 */
	@PreAuthorize("hasAuthority('kefu-faq_update')")
	@RequestMapping(value = "/faq/update", method = RequestMethod.POST)
	public Object updateFAQ(@RequestBody KefuFAQ record) {
		kefuService.updateFAQ(record);
		return JSONResult.success(record);
	}

	/**
	 * 删除FAQ
	 * @param id FAQID
	 * @return 处理结果
	 */
	@PreAuthorize("hasAuthority('kefu-faq_delete')")
	@RequestMapping(value = "/faq/delete", method = RequestMethod.GET)
	public Object deleteFAQ(@RequestParam(name = "id") Integer id) throws Exception {
		KefuFAQ record = kefuService.findFAQ(id);
		if(record == null || !Objects.equals(EsSecurityHandler.clientId(), record.getClientId())) {
			throw new EsRuntimeException(ErrorConstants.DATA_NOT_FOUND);
		}
		kefuService.deleteFAQ(id);
		return JSONResult.success();
	}
	
	/**
	 * 消息列表
	 * @param page 查询条件
	 * @return 消息列表
	 */
	@PreAuthorize("hasAuthority('kefu-message_list')")
	@RequestMapping(value = "/message/list", method = RequestMethod.POST)
	public Object listMessage(@RequestBody JSONRequestPage<KefuMessage> page) {
		KefuMessage example = page.getSearch();
		if(example == null) {
			example = new KefuMessage();
		}
		example.setClientId(EsSecurityHandler.clientId());
		Page<KefuMessage> list = kefuService.listMessage(example, page.getPageNum(), page.getPageSize());
		JSONResultPage<KefuMessage> result = new JSONResultPage<>(list.getResult());
		result.setPageNum(list.getPageNum());
		result.setTotal(list.getTotal());
		return result;
	}
	
	/**
	 * 获取消息信息
	 * @param id 消息ID
	 * @return 消息信息
	 */
	@PreAuthorize("hasAuthority('kefu-message_get')")
	@RequestMapping(value = "/message/get", method = RequestMethod.GET)
	public Object findMessageById(@RequestParam(name = "id") Integer id) throws Exception {
		KefuMessage record = kefuService.findMessage(id);
		if(record == null) {
			throw new EsRuntimeException(ErrorConstants.DATA_NOT_FOUND);
		}
		return JSONResult.success(record);
	}

	/**
	 * 创建消息
	 * @param record 消息信息
	 * @return 消息信息
	 */
	@RequestMapping(value = "/message/create", method = RequestMethod.POST)
	public Object createMessage(@RequestPart(required = false, value = "attach") MultipartFile file, KefuMessage record) throws IOException {
		if(file != null && !file.isEmpty()) {
			String filePath = SpringContextHolder.getProperty("jia.file.path", String.class);
			String filename = DateUtil.getDateString() + "_" + file.getOriginalFilename();
			File pathFile = new File(filePath + "/kefu");
			//noinspection ResultOfMethodCallIgnored
			pathFile.mkdirs();
			File f = new File(filePath + "/kefu/" + filename);
			file.transferTo(f);

			//保存文件信息
			IspFile cf = new IspFile();
			cf.setClientId(EsSecurityHandler.clientId());
			cf.setExtension(FileUtil.getExtension(filename));
			cf.setName(file.getOriginalFilename());
			cf.setSize(file.getSize());
			cf.setType(Constants.FILE_TYPE_KEFU);
			cf.setUri("kefu/" + filename);
			fileService.create(cf);

			record.setAttachment("kefu/" + filename);
		}
		record.setClientId(EsSecurityHandler.clientId());
		kefuService.createMessage(record);
		return JSONResult.success(record);
	}

	/**
	 * 更新消息信息
	 * @param record 消息信息
	 * @return 消息信息
	 */
	@PreAuthorize("hasAuthority('kefu-message_update')")
	@RequestMapping(value = "/message/update", method = RequestMethod.POST)
	public Object updateMessage(@RequestBody KefuMessage record) {
		kefuService.updateMessage(record);
		return JSONResult.success(record);
	}

	/**
	 * 删除消息
	 * @param id 消息ID
	 * @return 处理结果
	 */
	@PreAuthorize("hasAuthority('kefu-message_delete')")
	@RequestMapping(value = "/message/delete", method = RequestMethod.GET)
	public Object deleteMessage(@RequestParam(name = "id") Integer id) throws Exception {
		KefuMessage record = kefuService.findMessage(id);
		if(record == null || !Objects.equals(EsSecurityHandler.clientId(), record.getClientId())) {
			throw new EsRuntimeException(ErrorConstants.DATA_NOT_FOUND);
		}
		kefuService.deleteMessage(id);
		return JSONResult.success();
	}

	/**
	 * 客服内容图片上传
	 * @param file 图片文件
	 * @return 图片文件信息
	 * @throws Exception 异常信息
	 */
	@PreAuthorize("hasAuthority('kefu-image_upload')")
	@RequestMapping(value = "/image/upload", method = RequestMethod.POST)
	public Object updateLogo(@RequestPart MultipartFile file) throws Exception {
		String filename = DateUtil.getDateString() + "_" + file.getOriginalFilename();
		String filePath = SpringContextHolder.getProperty("jia.file.path", String.class);
		File pathFile = new File(filePath + "/kefu");
		//noinspection ResultOfMethodCallIgnored
		pathFile.mkdirs();
		File f = new File(filePath + "/kefu/" + filename);
		file.transferTo(f);

		//保存文件信息
		IspFile cf = new IspFile();
		cf.setClientId(EsSecurityHandler.clientId());
		cf.setExtension(FileUtil.getExtension(filename));
		cf.setName(file.getOriginalFilename());
		cf.setSize(file.getSize());
		cf.setType(EsConstants.FILE_TYPE_AVATAR);
		cf.setUri("kefu/" + filename);
		fileService.create(cf);

		return JSONResult.success(cf);
	}
}