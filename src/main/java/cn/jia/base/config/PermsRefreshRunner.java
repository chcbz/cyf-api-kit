package cn.jia.base.config;

import cn.jia.core.configuration.SpringContextHolder;
import cn.jia.core.entity.Action;
import cn.jia.core.service.ActionService;
import cn.jia.sms.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class PermsRefreshRunner implements CommandLineRunner {
	
	@Autowired
	private ActionService actionService;

	@Override
	public void run(String... arg0) {
		AtomicReference<RequestMapping> classUrlAnno = new AtomicReference<>();
		AtomicReference<PreAuthorize> methodPermsAnno = new AtomicReference<>();
		AtomicReference<RequestMapping> methodUrlAnno = new AtomicReference<>();
		List<Action> actionList = new ArrayList<>();

		Map<String, Object> beansMap = SpringContextHolder.getApplicationContext().getBeansWithAnnotation(Controller.class);
		beansMap.values().forEach(value -> {
			Class<?> clazz = AopUtils.getTargetClass(value);
			classUrlAnno.set(clazz.getAnnotation(RequestMapping.class));
			if(classUrlAnno.get() != null) {
				String moduleName = classUrlAnno.get().value()[0].replace("/", "");
				String rootUrl = classUrlAnno.get().value()[0].substring(1);
				Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
				for (Method method : methods) {
					if (method.isAnnotationPresent(PreAuthorize.class) && method.isAnnotationPresent(RequestMapping.class)) {
						methodPermsAnno.set(method.getAnnotation(PreAuthorize.class));
						methodUrlAnno.set(method.getAnnotation(RequestMapping.class));
						Action action = new Action();
						action.setResourceId("cyf-api-kit");
						action.setStatus(Constants.PERMS_STATUS_ENABLE);
						action.setModule(moduleName);
						action.setFunc(methodUrlAnno.get().value()[0].substring(1).replaceAll("/\\{\\w+}", "").replace("/", "_"));
						action.setUrl(rootUrl + methodUrlAnno.get().value()[0]);
//							action.setDescription(methodPermsAnno.value());
						actionList.add(action);
					}
				}
			}
		});
		actionService.refresh(actionList);
	}
}
