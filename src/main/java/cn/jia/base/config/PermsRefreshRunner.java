package cn.jia.base.config;

import cn.jia.core.entity.Action;
import cn.jia.core.service.ActionService;
import cn.jia.core.util.ClassUtil;
import cn.jia.core.util.DateUtil;
import cn.jia.core.util.FileUtil;
import cn.jia.sms.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

@Component
@Slf4j
public class PermsRefreshRunner implements CommandLineRunner {
	
	@Autowired
	private ActionService actionService;

	@Override
	public void run(String... arg0) throws Exception {
		String packageName = "cn.jia";
		List<String> classNames = ClassUtil.getClassName(packageName, true);
		log.debug("PermsRefesh Class: " + classNames);
		RequestMapping classUrlAnno;
		PreAuthorize methodPermsAnno;
		RequestMapping methodUrlAnno;
		List<Action> actionList = new ArrayList<>();
		for (String className : classNames) {
			if(className.endsWith("Controller")){
				Class<?> clazz = Class.forName(className);
				classUrlAnno = clazz.getAnnotation(RequestMapping.class);
				if(classUrlAnno != null) {
					String moduleName = classUrlAnno.value()[0].replace("/", "");
					String rootUrl = classUrlAnno.value()[0].substring(1);
					Method[] methods = clazz.getMethods();
					for (Method method : methods) {
						methodPermsAnno = method.getAnnotation(PreAuthorize.class);
						methodUrlAnno = method.getAnnotation(RequestMapping.class);
						if (methodPermsAnno != null && methodUrlAnno != null) {
							methodUrlAnno.value();
							Action action = new Action();
							action.setResourceId("cyf-api-kit");
							action.setStatus(Constants.PERMS_STATUS_ENABLE);
							action.setModule(moduleName);
							action.setFunc(methodUrlAnno.value()[0].substring(1).replaceAll("/\\{\\w+}", "").replace("/", "_"));
							action.setUrl(rootUrl + methodUrlAnno.value()[0]);
//							action.setDescription(methodPermsAnno.value());
							actionList.add(action);
						}
					}
				}
			}
		}
		actionService.refresh(actionList);
	}

	public static void main(String[] args) throws IOException {
		String jarPath = "file:/C:/Users/Think/.m2/repository/cn/jia/jia-api-core/1.0.0-SNAPSHOT/jia-api-core-1.0.0-20190606.003552-4.jar!/cn/jia";
		int pathIndex = jarPath.lastIndexOf("!");
		String jarFilePath = jarPath.substring(0, pathIndex);
		JarFile jarFile;
		if (jarFilePath.endsWith(".jar")) {
			URL url = new URL(jarPath.substring(0, pathIndex));
			System.out.println(url.getPath());
			InputStream is = url.openStream();
			String filename = url.getPath().substring(url.getPath().lastIndexOf("/") + 1);
			filename = FileUtil.getName(filename) + "_" + DateUtil.getDateString() + "." + FileUtil.getExtension(filename);
			FileUtil.create(is, "/tmp/" + filename);
		}
	}
}
