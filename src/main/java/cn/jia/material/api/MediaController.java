package cn.jia.material.api;

import cn.jia.core.entity.JSONRequestPage;
import cn.jia.core.entity.JSONResult;
import cn.jia.core.entity.JSONResultPage;
import cn.jia.core.exception.EsRuntimeException;
import cn.jia.core.service.DictService;
import cn.jia.core.util.*;
import cn.jia.material.common.Constants;
import cn.jia.material.common.ErrorConstants;
import cn.jia.material.entity.Media;
import cn.jia.material.service.MediaService;
import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/media")
@Slf4j
public class MediaController {
	
	@Autowired
	private MediaService mediaService;
	@Autowired
	private DictService dictService;
	@Value("${mat.web.realpath}")
	private String webRealPath;
	
	/**
	 * 获取媒体信息
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/get")
	public Object findById(@RequestParam(name = "id", required = true) Integer id) throws Exception{
		Media media = mediaService.find(id);
		if(media == null) {
			throw new EsRuntimeException(ErrorConstants.MEDIA_NOT_EXIST);
		}
		media.setLink(dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_MODULE_URL, Constants.MODULE_URL_MATERIAL).getName() +"/"+media.getUrl());
		return JSONResult.success(media);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "/get/content")
	public Object findContentById(@RequestParam(name = "id", required = true) Integer id) throws Exception{
		Media media = mediaService.find(id);
		String content = FileUtil.readString(webRealPath + "/" + media.getUrl());
		if(content == null) {
			throw new EsRuntimeException(ErrorConstants.DATA_NOT_FOUND);
		}
		return JSONResult.success(content);
	}

	/**
	 * 创建媒体
	 * @param media
	 * @return
	 */
	@PostMapping(value = "/create")
	public Object create(@RequestBody Media media) {
		mediaService.create(media);
		return JSONResult.success();
	}

	/**
	 * 更新媒体信息
	 * @param media
	 * @return
	 */
	@PostMapping(value = "/update")
	public Object update(@RequestBody Media media) {
		mediaService.update(media);
		return JSONResult.success();
	}

	/**
	 * 删除媒体
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/delete")
	public Object delete(@RequestParam(name = "id") Integer id) {
		Media media = mediaService.find(id);
		if(media != null) {
			File file = new File(webRealPath + "/" + media.getUrl());
			file.delete();
			mediaService.delete(id);
		}
		
		return JSONResult.success();
	}
	
	/**
	 * 获取所有媒体信息
	 * @return
	 */
	@PostMapping(value = "/list")
	public Object list(@RequestBody JSONRequestPage<String> page) {
		Media example = JSONUtil.fromJson(page.getSearch(), Media.class);
		Page<Media> mediaList = mediaService.list(page.getPageNum(), page.getPageSize(), example);
		List<Media> medias = mediaList.getResult().stream().peek(media -> media.setLink(dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_MODULE_URL, Constants.MODULE_URL_MATERIAL).getName() +"/"+media.getUrl())).collect(Collectors.toList());
		JSONResultPage<Media> result = new JSONResultPage<>(medias);
		result.setPageNum(mediaList.getPageNum());
		result.setTotal(mediaList.getTotal());
		return result;
	}
	
	/**
     * 新闻图片上传，返回全路径
     * @param file  图片文件
     * @return  图片服务器全路径
     */
	@PostMapping(value = "/upload")
	public Object upload(@RequestPart(required = false, value = "file") MultipartFile file, Media media) throws Exception {
		if(media.getType() == null) {
			throw new EsRuntimeException(ErrorConstants.MEDIA_TYPE_NEED);
		}
		Long now = DateUtil.genTime(new Date());
		String mediaType = dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_MEDIA_TYPE, String.valueOf(media.getType())).getName();
		String filePath = webRealPath + "/" + mediaType;
		log.debug(filePath);
		File pathFile = new File(filePath);
		if(!pathFile.exists()) {
			pathFile.mkdirs();
		}
		//如果是文件上传，则直接保存文件
		if(file != null) {
			String fileName = file.getOriginalFilename();
			if(StringUtils.isEmpty(media.getTitle())) {
				media.setTitle(fileName);
			}
			media.setUrl(mediaType + "/" + now + "_" + fileName);
			
			try {
				file.transferTo(new File(filePath + "/" + now + "_" + fileName));
			} catch (IllegalStateException | IOException e) {
				throw new EsRuntimeException();
			}
		}
		//如果是内容上传，则将内容写到本地文件
		else {
			String fileName = media.getTitle();
			media.setUrl(mediaType + "/" + now + "_" + fileName);
			if(StringUtils.isEmpty(fileName)) {
				throw new EsRuntimeException(ErrorConstants.MEDIA_TITLE_NEED);
			}
			if(StringUtils.isEmpty(media.getContent())) {
				throw new EsRuntimeException(ErrorConstants.MEDIA_CONTENT_NEED);
			}
			StreamUtil.io(new ByteArrayInputStream(media.getContent().getBytes()), new FileOutputStream(filePath + "/" + now + "_" + fileName));
		}
		
		mediaService.create(media);
		media.setLink(dictService.selectByDictTypeAndDictValue(Constants.DICT_TYPE_MODULE_URL, Constants.MODULE_URL_MATERIAL).getName() +"/"+media.getUrl());
		return JSONResult.success(media);
	}
	
	/**
	 * 获取媒体缩略图
	 * @param id
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/thumbnail", method = RequestMethod.GET)
	public Object thumbnail(@RequestParam(name = "id", required = true) Integer id, HttpServletResponse response) throws IOException {
		Media media = mediaService.find(id);
		String filePath = webRealPath + "/thumbnail/" + media.getUrl();
		File file = new File(filePath);
		String parentPath = file.getParent();
		String fileName = FileUtil.getName(file.getName());
		String extension = FileUtil.getExtension(file.getName());
		file = new File(parentPath + "/" + fileName + ".jpg");
		if(!file.exists()) {
			FileUtil.mkdirs(parentPath);
			if("html".equalsIgnoreCase(extension) || "htm".equalsIgnoreCase(extension)) {
				String htmlText = FileUtil.readString(webRealPath + "/" + media.getUrl());
//				htmlText = "<div style=\"width:800px;height:600px;overflow:hidden;\">"+htmlText+"</div>";
				ImgUtil.html2Image(htmlText, file.getPath());
				Thumbnails.of(file).sourceRegion(Positions.TOP_LEFT, 750, 500).size(327, 218).keepAspectRatio(false).toFile(file);
//				ImgUtil.compressPic(file.getPath(), file.getPath(), 327, 218, true);
			} else if("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension) || "gif".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension)) {
				Thumbnails.of(webRealPath + "/" + media.getUrl()).size(327, 218).toFile(file.getPath());
			}
			
		}
		response.setContentType("image/jpg");
		FileInputStream fis = null;
		OutputStream os = null;
		try {
			fis = new FileInputStream(file);
			os = response.getOutputStream();
			int count = 0;
			byte[] buffer = new byte[1024 * 8];
			while ((count = fis.read(buffer)) != -1) {
				os.write(buffer, 0, count);
				os.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			fis.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "ok";
	}
}
