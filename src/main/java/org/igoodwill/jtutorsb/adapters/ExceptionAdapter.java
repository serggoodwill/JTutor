package org.igoodwill.jtutorsb.adapters;

import javax.servlet.http.HttpServletRequest;

import org.igoodwill.jtutorsb.model.admin.Users;
import org.igoodwill.jtutorsb.service.MailService;
import org.igoodwill.jtutorsb.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
class ExceptionAdapter {

	@Autowired
	UsersService usersService;

	@Autowired
	MailService mailService;

	@Value("${app.email.support}")
	private String supportEmail;

	@Value("${app.environment}")
	private String environment;

	public static final String DEFAULT_ERROR_VIEW = "error";

	@ExceptionHandler(value = Exception.class)
	public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
		// If the exception is annotated with @ResponseStatus rethrow it and let
		// the framework handle it - like the OrderNotFoundException example
		// at the start of this post.
		// AnnotationUtils is a Spring Framework utility class.
		if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null) {
			throw e;
		}

		// Otherwise setup and send the user to a default error-view.
		Users users = usersService.getLoggedInUser();
		ModelAndView mav = new ModelAndView();
		mav.addObject("exception", e);
		mav.addObject("url", req.getRequestURL());
		mav.addObject("user", users);
		mav.addObject("support", supportEmail);
		mav.setViewName(DEFAULT_ERROR_VIEW);

		if (!environment.equals("DEV")) {
			mailService.sendErrorEmail(e, req, users);
		}
		e.printStackTrace();
		return mav;
	}
}